/*
 * JSocket Wrench
 * 
 * Copyright (C) act365.com October 2003
 * 
 * Web site: http://www.act365.com/wrench
 * E-mail: developers@act365.com
 * 
 * The JSocket Wrench library adds support for low-level Internet protocols
 * to the Java programming language.
 * 
 * This program is free software; you can redistribute it and/or modify it 
 * under the terms of the GNU General Public License as published by the Free 
 * Software Foundation; either version 2 of the License, or (at your option) 
 * any later version.
 *  
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General 
 * Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with 
 * this program; if not, write to the Free Software Foundation, Inc., 
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */

package com.act365.net;

import java.net.*;

/**
 * Manages the housekeeping tasks (mostly on Windows) associated with
 * the startup and the shutdown of the library.
 */

public class SocketWrenchSession {

  /**
   Loads the native library and starts Winsock 2 on Windows.
  */

  static {
    try {
      System.loadLibrary("com_act365_net_Sockets");
      _startup();
    } catch ( UnsatisfiedLinkError ule ){
      String libpath = System.getProperty("java.library.path");
      System.err.println("com_act365_net_Sockets not found on library path " + libpath );
    } catch ( Exception e ) {
      System.err.println( e.getMessage() );
    }
  }

  native static int _startup() throws SocketException ;

  /**
   Releases Winsock 2 resources on Windows.
  */

  protected void finalize() {
    try {
      _shutdown();
    } catch ( SocketException e ) {
      System.err.println( e.getMessage() );
    }
  }

  native static int _shutdown() throws SocketException ;
}

