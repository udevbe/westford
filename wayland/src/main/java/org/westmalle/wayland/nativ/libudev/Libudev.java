//Copyright 2016 Erik De Rijcke
//
//Licensed under the Apache License,Version2.0(the"License");
//you may not use this file except in compliance with the License.
//You may obtain a copy of the License at
//
//http://www.apache.org/licenses/LICENSE-2.0
//
//Unless required by applicable law or agreed to in writing,software
//distributed under the License is distributed on an"AS IS"BASIS,
//WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,either express or implied.
//See the License for the specific language governing permissions and
//limitations under the License.
package org.westmalle.wayland.nativ.libudev;

import org.freedesktop.jaccall.Lib;
import org.freedesktop.jaccall.Ptr;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
@Lib("udev")
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
