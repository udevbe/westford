Trinity Shell
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

Dependencies
============

 - JDK8.
 - Jogl. Available on maven central.
 - Google Auto-Value. Available on maven central.
 - Google Auto-Factory. Available on maven central.
 - Google Dagger. Available on maven central.
 - Google Guava. Available on maven central.
 - Findbugs. Available on maven central.
 - SLF4J. Available on maven central.
 - Wayland-Java-Bindings. Available [here](https://github.com/Zubnix/wayland-java-bindings).
 - Pixman-Java-Bindings. Available [here](https://github.com/Zubnix/pixman-java-bindings).
 - Jglm. Available [here](https://github.com/jroyalty/jglm).

State
=====
 - X OpenGL rendering back-end works.
 - Moving clients works.

Known Issues
============
 - None.

TODO
====
 - Finish wl_shell implementation.
 - Start xdg_shell implementation.
 - Start XWayland support.
 - Unit tests.

License
=======
   Copyright 2015 Erik De Rijcke

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
