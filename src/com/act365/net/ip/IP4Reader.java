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

package com.act365.net.ip ;

import com.act365.net.*;

import java.io.* ;

/**
 IP4Reader reads IP4Message objects from a data buffer.
*/

public class IP4Reader {
    
  /**
   * @deprecated Use the other form of read()
   */

  public static IP4Message read( byte[] buffer , int offset , int count , boolean testchecksum ) throws IOException {
      
      IP4Message message = new IP4Message();
      
      read( message , buffer , offset , count , testchecksum );
      
      return message ;
  }

  /**
   read() populates an IP4 message instance from the contents of a buffer. 
   IP6 is not supported.
  */

  public static int read( IP4Message message , byte[] buffer , int offset , int count , boolean testchecksum ) throws IOException {

    if( count < 20 ) {
      throw new IOException("IP4 messages must be at least twenty bytes long");
    }

    if( ( message.version = (byte)( buffer[ offset ] >>> 4 ) ) != 4 ){
      throw new IOException("Only IP v4 is supported");
    }

    message.headerlength = (byte)( buffer[ offset ] & 0xf );
    message.typeofservice = buffer[offset+1];
    message.length = SocketUtils.shortFromBytes( buffer , offset + 2 );
    message.identifier = SocketUtils.shortFromBytes( buffer , offset + 4 );
    message.flags = (byte)( buffer[ offset + 6 ] & 0x60 );

    if( ( message.flags & IP4.MUST_FRAGMENT ) != 0 ){
      throw new IOException("Fragmentation is not supported");
    }

    message.offset = (short)( SocketUtils.shortFromBytes( buffer , offset + 6 ) & 0x1fff );
    message.timetolive = buffer[ offset + 8 ];
    message.protocol = buffer[ offset + 9 ];
    message.checksum = SocketUtils.shortFromBytes( buffer , offset + 10 );

    message.source = new byte[4];
    message.source[0] = buffer[ offset + 12 ];
    message.source[1] = buffer[ offset + 13 ];
    message.source[2] = buffer[ offset + 14 ];
    message.source[3] = buffer[ offset + 15 ];

    message.destination = new byte[4];
    message.destination[0] = buffer[ offset + 16 ];
    message.destination[1] = buffer[ offset + 17 ];
    message.destination[2] = buffer[ offset + 18 ];
    message.destination[3] = buffer[ offset + 19 ];

    message.options = new int[ message.headerlength - 5 ];

    byte b = 4 ;

    while ( ++ b < message.headerlength ){
      message.options[ b - 5 ] = SocketUtils.intFromBytes( buffer , offset + 4 * b );
    } 

    short checksum ;

    if( testchecksum && ( checksum = SocketUtils.checksum( buffer , offset , 4 * message.headerlength ) ) != 0 ){
      throw new IOException("Checksum error: " + checksum );
    }

    message.data = buffer ;
    message.dataOffset = offset + 4 * message.headerlength ;
    message.dataCount = message.length - 4 * message.headerlength ;
    
    return message.length();
  }
}
