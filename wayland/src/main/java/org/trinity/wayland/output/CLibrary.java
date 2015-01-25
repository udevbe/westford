//Copyright 2015 Erik De Rijcke
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
package org.trinity.wayland.output;

import com.sun.jna.LastErrorException;
import com.sun.jna.Library;

public interface CLibrary extends Library {
    String JNA_LIBRARY_NAME = "c";

    /* command values */
    int F_DUPFD = 0;	/* duplicate file descriptor */
    int F_GETFD = 1;		/* get file descriptor flags */
    int F_SETFD = 2;		/* set file descriptor flags */
    int F_GETFL = 3;		/* get file status flags */
    int F_SETFL = 4;		/* set file status flags */

    int F_GETOWN = 5;	/* get SIGIO/SIGURG proc/pgrp */
    int F_SETOWN = 6;	/* set SIGIO/SIGURG proc/pgrp */

    int F_GETLK  = 7;	/* get record locking information */
    int F_SETLK  = 8;	/* set record locking information */
    int F_SETLKW = 9;	/* F_SETLK; wait if blocked */

    /* file descriptor flags (F_GETFD, F_SETFD) */
    int FD_CLOEXEC = 1;	/* close-on-exec flag */

    /* record locking flags (F_GETLK, F_SETLK, F_SETLKW) */
    int F_RDLCK = 1;	/* shared or read lock */
    int F_UNLCK = 2;	/* unlock */
    int F_WRLCK = 3;	/* exclusive or write lock */

    int F_WAIT  = 0x010;		/* Wait until lock is granted */
    int F_FLOCK = 0x020; 	/* Use flock(2) semantics for lock */
    int F_POSIX = 0x040; 	/* Use POSIX semantics for lock */


    int write(int fd,
              byte[] buffer,
              int n_byte) throws LastErrorException;

    int close(int fd) throws LastErrorException;

    void read(int fd,
              byte[] buffer,
              int n_byte) throws LastErrorException;

    int fcntl(int fd,
              int operation,
              Object... args) throws LastErrorException;

    int pipe(int[] pipeFds) throws LastErrorException;
}
