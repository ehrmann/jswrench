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

/**
 Defines platform-independent constants. Platform-dependent constants
 are defined within native code.
*/

public interface SocketConstants {

  // Address family

  public final static int AF_INET  = 2 ;

  // Socket type

  public final static int SOCK_STREAM = 1 ,
                          SOCK_DGRAM  = 2 ,
                          SOCK_RAW    = 3 ;

  // Protocol

  public final static int IPPROTO_IP   = 0 ,
                          IPPROTO_ICMP = 1 ,
                          IPPROTO_TCP  = 6 ,
                          IPPROTO_UDP  = 17 ,
                          IPPROTO_TCPJ = 152 ,
                          IPPROTO_RAW  = 255 ;

  // Send/receive flags

  public final static int MSG_OOB       = 1 ,
                          MSG_PEEK      = 2 ,
                          MSG_DONTROUTE = 4 ;
};

