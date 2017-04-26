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
package org.westford.nativ.libudev

import org.freedesktop.jaccall.Lib
import org.freedesktop.jaccall.Ptr

import javax.inject.Singleton

@Singleton
@Lib(value = "udev", version = 1)
class Libudev {

    @Ptr
    external fun udev_unref(@Ptr udev: Long): Long

    @Ptr
    external fun udev_new(): Long

    @Ptr
    external fun udev_enumerate_new(@Ptr udev: Long): Long

    external fun udev_enumerate_add_match_subsystem(@Ptr udev_enumerate: Long,
                                                    @Ptr(String::class) subsystem: Long): Int

    external fun udev_enumerate_add_match_sysname(@Ptr udev_enumerate: Long,
                                                  @Ptr(String::class) sysname: Long): Int

    external fun udev_enumerate_scan_devices(@Ptr udev_enumerate: Long): Int

    @Ptr
    external fun udev_enumerate_get_list_entry(@Ptr udev_enumerate: Long): Long

    @Ptr(String::class)
    external fun udev_list_entry_get_name(@Ptr list_entry: Long): Long

    @Ptr
    external fun udev_device_new_from_syspath(@Ptr udev: Long,
                                              @Ptr(String::class) syspath: Long): Long

    @Ptr(String::class)
    external fun udev_device_get_property_value(@Ptr udev_device: Long,
                                                @Ptr(String::class) key: Long): Long

    @Ptr
    external fun udev_device_unref(@Ptr udev_device: Long): Long

    @Ptr
    external fun udev_device_get_parent_with_subsystem_devtype(@Ptr udev_device: Long,
                                                               @Ptr(String::class) subsystem: Long,
                                                               @Ptr(String::class) devtype: Long): Long

    @Ptr(String::class)
    external fun udev_device_get_sysattr_value(@Ptr udev_device: Long,
                                               @Ptr(String::class) sysattr: Long): Long

    @Ptr
    external fun udev_enumerate_unref(@Ptr udev_enumerate: Long): Long

    @Ptr
    external fun udev_list_entry_get_next(@Ptr list_entry: Long): Long

    @Ptr(String::class)
    external fun udev_device_get_sysnum(@Ptr udev_device: Long): Long

    @Ptr(String::class)
    external fun udev_device_get_devnode(@Ptr udev_device: Long): Long

    companion object {

        val DEFAULT_SEAT = "seat0"
    }
}
