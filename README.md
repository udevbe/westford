Westmalle
=====================

A Wayland OpenGL compositor written in pure Java.
This is a work in progress and not ready for day to day use, however
certain features like client movement and render output on X are considered ready.

Javadoc
=======
Not available yet.

Building
========
Run `maven install` in the root of the project.

Running
=======
Go in the `bootstrap/target` folder. Type `java -jar bootstrap-1.0.0-SNAPSHOT.jar`.
Next fire up some test clients from the Weston compositor (eg `weston-terminal`). 
Make sure you use Weston 1.4 as more recent versions depend on xdg-shell which is not 
yet implemented by Westmalle.

Dependencies
============
Java:

 - JDK8.
 - Google Auto-Factory. Available on maven central.
 - Google Auto-Value. Available on maven central.
 - Google Dagger 2. Available on maven central.
 - jsr305. Available on maven central.
 - SLF4J. Available on maven central.
 - Wayland-Java-Bindings. Available on maven central.
 
Native:

 - libc
 - pixman-1
 - EGL
 - GLESv2
 - X11
 - xcb
 - X11-xcb
 - xkbcommon
 - xkbcommon-x11
 - linux

State
=====
[![Build Status](https://travis-ci.org/udevbe/westmalle.svg?branch=master)](https://travis-ci.org/udevbe/westmalle)

| Functionality                  | Implemented        |
| :-------------------------:    | :----------------: |
| OpenGL on HTML5 (experimental) | :heavy_check_mark: |
| OpenGL on X                    | :heavy_check_mark: |
| OpenGL on KMS                  | :x:                |
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
| DRM/KMS       | 0%        |
| multi seat    | 100%      |

License
=======

Westmalle Wayland Compositor.
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
