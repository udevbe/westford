Westford
=====================

A Wayland compositor written in Kotlin.

This is a work in progress and not ready for day to day use, however
certain features like client movement and render output on X are considered ready.

This compositor was originally written in Java. The latest Java commit can still be found [here](https://github.com/udevbe/westford/tree/8510ee7b3387a8693a2fa364a590e6b7caf0784a)

KDoc
=======
Not available yet.

Building
========
Prerequisites: 
- gradle
- cmake 
- docker (optional, needed for build-in cross compilation) 

Run `gradle build` in the root of the project. Westford is still under heavy 
development, so occasionally tests might fail. If this is the case you can run the build
with `gradle build -x test`.

To cross compile for a specific architecture, set the corresponding arch variable in the gradle build command.
Available profiles are:

| Architecture | arch        |
|:------------:|:-----------:|
| aarch64      |linux-aarch64|
| armv7hf      |linux-armv7hf| 
| armv7sf      |linux-armv7sf|
| armv6hf      |linux-armv6hf|
| x86_64       |linux-x86_64 |
| i686         |linux-i686   |
| all of above |all          |

So if we were to build for `armv7hf`, our build command would becomes `gradle build -x test -Parch=linux-armv7hf`.
This will trigger a cross compilation inside a cleanly isolated docker container.

If no profile is selected, the gradle build will default to the `native` profile, which corresponds to
the architecture that you're currently building on, without the use of docker.

Running
=======
Westford can be launched using different back-ends and configurations. These live as separate projects
under `launch`

Currently the following back-ends exist:
- `launch.x11`A back-end that outputs to a regular X11 window, one window per (virtual) screen. Ideal for quick testing.
- `launch.drm.direct` Uses the kernel's drm/kms system to directly output to the screen, without the use of X11. Root user only.
- `launch.drm.indirect`Uses the kernel's drm/kms system to directly output to the screen, without the use of X11. All users. Uses setuid.


Running under X11
=================
To create an runnable distribution, go into the `launch.x11` folder. Type `gradle installDist`.
This will create an executable `launch.x11` in `build/install/launch.x11/bin`
Next fire up some test clients from the Weston compositor (eg `weston-terminal`). 
Make sure you use Weston 1.4 as more recent versions depend on xdg-shell which is not 
yet implemented by Westford.

To configure the X11 back-end, open up `X11PlatformConfigSimple.kt` 
found in `westford/launch.x11/src/main/kotlin/org/westford/compositor/launch/x11` and adjust as required.

Running with drm/kms
====================
//TODO

Dependencies
============
The following native libraries are expected, depending on the features used:
 - libc (core)
 - pixman-1 (core)
 - EGL (core)
 - GLESv2 (core)
 - libdrm (drm)
 - libudev (drm)
 - libinput (drm)
 - X11 (x11)
 - xcb (x11)
 - X11-xcb (x11)
 - xkbcommon (core)
 - xkbcommon-x11 (x11)
 - linux (core)

State
=====
[![Build Status](https://travis-ci.org/udevbe/westford.svg?branch=master)](https://travis-ci.org/udevbe/westford)

| Functionality                  | Implemented        |
| :-------------------------:    | :----------------: |
| OpenGL on HTML5 (experimental) | :heavy_check_mark: |
| OpenGL on X                    | :heavy_check_mark: |
| OpenGL on KMS                  | :heavy_check_mark: |
| Software rendering on X        | :x:                |
| Software rendering on KMS      | :x:                |
| Window moving                  | :heavy_check_mark: |
| Window resizing                | :heavy_check_mark: |
| Mouse input                    | :heavy_check_mark: |
| Keyboard input                 | :heavy_check_mark: |
| Touch input                    | :heavy_check_mark: |
| Drag and Drop                  | :x:                |

Known Issues
============
 - None.

Roadmap
====
| Topic         | Progress  |
| :-----------: | :-------: |
| unit tests    | 90%       |
| core protocol | 80%       |
| wl_shell      | 60%       |
| sw rendering  | 0%        |
| xdg_shell     | 0%        |
| xwayland      | 0%        |
| DRM/KMS       | 90%       |
| multi seat    | 100%      |

License
=======

Westford Wayland Compositor.
Copyright (C) 2016  Erik De Rijcke

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
