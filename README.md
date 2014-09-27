JSocket Wrench

========
The author would like to apologize for the poor state of the documentation. I hope to have improved it significantly by mid-January 2005.

The JSocket Wrench library (which is distributed under the GNU General Public License) allows TCP, UDP and, in particular, raw sockets to be manipulated from within the Java programming language. The project aims to use as much Java and as little C++ as possible - only the lowest level I/O calls are coded in C++ . Everything else, including the header and checksum calculations, has been written in Java. The library implements subclasses of java.net.SocketImplFactory and java.net.DatagramSocketImpl in order to allow raw sockets to be created in a manner consistent with the existing JDK socket library. Of course, the fact that the code is written in Java means that it will execute more slowly than C/C++ routines would but the library doesn't claim to provide a high-performance TCP/IP stack - it aims to provide straightforward support for the various low-level internet protocols in a high-level and widely-understood programming language.

The project, which, as stated above, uses the JNI, builds on the following platforms:

* Linux
* Windows (Winsock 2)
* Solaris

Linux users will have to use the library with root permissions enabled if raw sockets are to be used. Default Windows installations on home PCs appear to provide users with sufficient permissions to open raw sockets.

Although Java is a platform-dependent language, apps writeen in Java will naturallly be constrainted by any limitations of the underlying operating system. The formal JDK java.net library has been chosen carefully in order to provide only functionality that is supported by Linux and all versions of Win32 - however, since raw sockets are supported unevenly across the different operating systems, it has proved impossible to guarantee consistent JSocket Wrench behaviour across all platforms.

The reference Windows platform is the Personal Edition of Windows XP.
