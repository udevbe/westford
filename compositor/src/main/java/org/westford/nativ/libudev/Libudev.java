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
package org.westford.nativ.libudev;

import org.freedesktop.jaccall.Lib;
import org.freedesktop.jaccall.Ptr;

import javax.inject.Singleton;

@Singleton
@Lib(value = "udev",
     version = 1)
public class Libudev {

    public static final String DEFAULT_SEAT = "seat0";

    @Ptr
    public native long udev_unref(@Ptr long udev);

    @Ptr
    public native long udev_new();

    @Ptr
    public native long udev_enumerate_new(@Ptr long udev);

    public native int udev_enumerate_add_match_subsystem(@Ptr long udev_enumerate,
                                                         @Ptr(String.class) long subsystem);

    public native int udev_enumerate_add_match_sysname(@Ptr long udev_enumerate,
                                                       @Ptr(String.class) long sysname);

    public native int udev_enumerate_scan_devices(@Ptr long udev_enumerate);

    @Ptr
    public native long udev_enumerate_get_list_entry(@Ptr long udev_enumerate);

    @Ptr(String.class)
    public native long udev_list_entry_get_name(@Ptr long list_entry);

    @Ptr
    public native long udev_device_new_from_syspath(@Ptr long udev,
                                                    @Ptr(String.class) long syspath);

    @Ptr(String.class)
    public native long udev_device_get_property_value(@Ptr long udev_device,
                                                      @Ptr(String.class) long key);

    @Ptr
    public native long udev_device_unref(@Ptr long udev_device);

    @Ptr
    public native long udev_device_get_parent_with_subsystem_devtype(@Ptr long udev_device,
                                                                     @Ptr(String.class) long subsystem,
                                                                     @Ptr(String.class) long devtype);

    @Ptr(String.class)
    public native long udev_device_get_sysattr_value(@Ptr long udev_device,
                                                     @Ptr(String.class) long sysattr);

    @Ptr
    public native long udev_enumerate_unref(@Ptr long udev_enumerate);

    @Ptr
    public native long udev_list_entry_get_next(@Ptr long list_entry);

    @Ptr(String.class)
    public native long udev_device_get_sysnum(@Ptr long udev_device);

    @Ptr(String.class)
    public native long udev_device_get_devnode(@Ptr long udev_device);
}
