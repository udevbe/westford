/*
 * Westford Wayland Compositor.
 * Copyright (C) 2016  Erik De Rijcke
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.westford.compositor.drm

import org.freedesktop.jaccall.Pointer.nref
import org.freedesktop.jaccall.Pointer.wrap
import org.freedesktop.wayland.server.Display
import org.freedesktop.wayland.server.jaccall.WaylandServerCore
import org.westford.launch.Privileges
import org.westford.nativ.glibc.Libc
import org.westford.nativ.libdrm.*
import org.westford.nativ.libdrm.Libdrm.Companion.DRM_MODE_CONNECTED
import org.westford.nativ.libudev.Libudev
import java.util.*
import javax.inject.Inject

//TODO tests tests tests!
class DrmPlatformFactory @Inject internal constructor(private val libudev: Libudev,
                                                      private val libdrm: Libdrm,
                                                      private val display: Display,
                                                      private val drmOutputFactory: DrmOutputFactory,
                                                      private val drmEventBusFactory: DrmEventBusFactory,
                                                      private val privateDrmPlatformFactory: PrivateDrmPlatformFactory,
                                                      private val privileges: Privileges) {

    fun create(): DrmPlatform {
        val udev = this.libudev.udev_new()
        if (udev == 0L) {
            throw RuntimeException("Failed to initialize udev")
        }
        //TODO seat from config
        val drmDevice = findPrimaryGpu(udev,
                                       "seat0")
        if (drmDevice == 0L) {
            throw RuntimeException("No drm capable gpu device found.")
        }

        val drmFd = initDrm(drmDevice)

        val drmOutputs = createDrmRenderOutputs(drmFd)

        val drmEventBus = this.drmEventBusFactory.create(drmFd)
        this.display.eventLoop.addFileDescriptor(drmFd,
                                                 WaylandServerCore.WL_EVENT_READABLE,
                                                 drmEventBus)

        this.privileges.setDrmMaster(drmFd)

        return this.privateDrmPlatformFactory.create(drmDevice,
                                                     drmFd,
                                                     drmEventBus,
                                                     drmOutputs)
    }

    /*
     * Find primary GPU
     * Some systems may have multiple DRM devices attached to a single seat. This
     * function loops over all devices and tries to find a PCI device with the
     * boot_vga sysfs attribute set to 1.
     * If no such device is found, the first DRM device reported by udev is used.
     */
    private fun findPrimaryGpu(udev: Long,
                               seat: String): Long {

        val udevEnumerate = this.libudev.udev_enumerate_new(udev)
        this.libudev.udev_enumerate_add_match_subsystem(udevEnumerate,
                                                        nref("drm").address)
        this.libudev.udev_enumerate_add_match_sysname(udevEnumerate,
                                                      nref("card[0-9]*").address)

        this.libudev.udev_enumerate_scan_devices(udevEnumerate)
        var drmDevice = 0L

        var entry = this.libudev.udev_enumerate_get_list_entry(udevEnumerate)
        while (entry != 0L) {

            val path = this.libudev.udev_list_entry_get_name(entry)
            val device = this.libudev.udev_device_new_from_syspath(udev,
                                                                   path)
            if (device == 0L) {
                //no device, process next entry
                entry = this.libudev.udev_list_entry_get_next(entry)
                continue

            }
            val deviceSeat: String
            val seatId = this.libudev.udev_device_get_property_value(device,
                                                                     nref("ID_SEAT").address)
            if (seatId == 0L) {
                //device does not have a seat, assign it a default one.
                deviceSeat = Libudev.DEFAULT_SEAT
            }
            else {
                deviceSeat = wrap<String>(String::class.java,
                                          seatId).get()
            }
            if (deviceSeat != seat) {
                //device has a seat, but not the one we want, process next entry
                this.libudev.udev_device_unref(device)
                entry = this.libudev.udev_list_entry_get_next(entry)
                continue
            }

            val pci = this.libudev.udev_device_get_parent_with_subsystem_devtype(device,
                                                                                 nref("pci").address,
                                                                                 0L)
            if (pci != 0L) {
                val id = this.libudev.udev_device_get_sysattr_value(pci,
                                                                    nref("boot_vga").address)
                if (id != 0L && wrap<String>(String::class.java,
                                             id).get() == "1") {
                    if (drmDevice != 0L) {
                        this.libudev.udev_device_unref(drmDevice)
                    }
                    drmDevice = device
                    break
                }
            }

            if (drmDevice == 0L) {
                drmDevice = device
            }
            else {
                this.libudev.udev_device_unref(device)
            }
            entry = this.libudev.udev_list_entry_get_next(entry)
        }

        this.libudev.udev_enumerate_unref(udevEnumerate)
        return drmDevice
    }

    private fun initDrm(device: Long): Int {
        val sysnum = this.libudev.udev_device_get_sysnum(device)
        val drmId: Int
        if (sysnum != 0L) {
            drmId = Integer.parseInt(wrap<String>(String::class.java,
                                                  sysnum).get())
        }
        else {
            drmId = 0
        }
        if (sysnum == 0L || drmId < 0) {
            throw RuntimeException("Failed to open drm device.")
        }

        val filename = this.libudev.udev_device_get_devnode(device)
        val fd = this.privileges.open(filename,
                                      Libc.O_RDWR)
        if (fd < 0) {
            throw RuntimeException("Failed to open drm device.")
        }

        return fd
    }

    private fun createDrmRenderOutputs(drmFd: Int): List<DrmOutput> {
        val resources = this.libdrm.drmModeGetResources(drmFd)
        if (resources == 0L) {
            throw RuntimeException("Getting drm resources failed.")
        }

        val drmModeRes = wrap<DrmModeRes>(DrmModeRes::class.java,
                                          resources).get()

        val countConnectors = drmModeRes.count_connectors()
        val drmOutputs = ArrayList<DrmOutput>(countConnectors)
        val usedCrtcs = HashSet<Int>()

        for (i in 0..countConnectors - 1) {
            val connector = this.libdrm.drmModeGetConnector(drmFd,
                                                            drmModeRes.connectors().get(i))
            if (connector == 0L) {
                continue
            }

            val drmModeConnector = wrap<DrmModeConnector>(DrmModeConnector::class.java,
                                                          connector).get()

            if (drmModeConnector.connection() == DRM_MODE_CONNECTED) {
                findCrtcIdForConnector(drmFd,
                                       drmModeRes,
                                       drmModeConnector,
                                       usedCrtcs).ifPresent { crtcId ->
                    drmOutputs.add(createDrmRenderOutput(drmModeRes,
                                                         drmModeConnector,
                                                         crtcId))
                }
            }
        }

        return drmOutputs
    }

    private fun findCrtcIdForConnector(drmFd: Int,
                                       drmModeRes: DrmModeRes,
                                       drmModeConnector: DrmModeConnector,
                                       crtcAllocations: MutableSet<Int>): Optional<Int> {

        for (j in 0..drmModeConnector.count_encoders() - 1) {
            val encoder = this.libdrm.drmModeGetEncoder(drmFd,
                                                        drmModeConnector.encoders().get(j))
            if (encoder == 0L) {
                return Optional.empty<Int>()
            }

            //bitwise flag of available crtcs, each bit represents the index of crtcs in drmModeRes
            val possibleCrtcs = wrap<DrmModeEncoder>(DrmModeEncoder::class.java,
                                                     encoder).get().possible_crtcs()
            this.libdrm.drmModeFreeEncoder(encoder)

            for (i in 0..drmModeRes.count_crtcs() - 1) {
                if (possibleCrtcs and (1 shl i) != 0 && crtcAllocations.add(drmModeRes.crtcs().get(i))) {
                    return Optional.of(drmModeRes.crtcs().get(i))
                }
            }
        }

        return Optional.empty<Int>()
    }

    private fun createDrmRenderOutput(drmModeRes: DrmModeRes,
                                      drmModeConnector: DrmModeConnector,
                                      crtcId: Int): DrmOutput {
        /* find highest resolution mode: */
        var area = 0
        var mode: DrmModeModeInfo? = null
        for (i in 0..drmModeConnector.count_modes() - 1) {
            val currentMode = drmModeConnector.modes().get(i)
            val current_area = currentMode.hdisplay() * currentMode.vdisplay()
            if (current_area > area) {
                mode = currentMode
                area = current_area
            }
        }

        if (mode == null) {
            throw RuntimeException("Could not find a valid mode.")
        }

        //FIXME deduce an output name from the drm connector
        return this.drmOutputFactory.create(drmModeRes,
                                            drmModeConnector,
                                            crtcId,
                                            mode)
    }
}
