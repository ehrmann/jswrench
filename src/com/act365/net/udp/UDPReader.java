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

package com.act365.net.udp ;

import com.act365.net.* ;

import java.io.* ;

/**
 The class UDPReader reads UDP packets.
*/

public class UDPReader {

  /**
   read() constructs a UDPMessage object from a buffer.
  */

  public static UDPMessage read( byte[] buffer , int offset , int length ) throws IOException {
    return read( buffer , offset , length , false , new byte[0] , new byte[0] );
  }
        
  /**
   read() constructs a UDPMessage object from a buffer. When testchecksum is selected,
   the function assumes the IP header to start at offset=0.
  */

  public static UDPMessage read( byte[]  buffer , 
                                 int     offset , 
                                 int     length , 
                                 boolean testchecksum ,
                                 byte[]  source ,
                                 byte[]  destination ) throws IOException {

    if( length < 8 ){
      throw new IOException("UDP messages must be at least eight bytes long");
    }

    UDPMessage message = new UDPMessage();

    message.sourceport = SocketUtils.shortFromBytes( buffer , offset );
    message.destinationport = SocketUtils.shortFromBytes( buffer , offset + 2 );
    message.length = SocketUtils.shortFromBytes( buffer , offset + 4 );
    message.checksum = SocketUtils.shortFromBytes( buffer , offset + 6 );

    if( message.length != length ){
      throw new IOException("IP and UDP header lengths differ");
    }

    message.data = new byte[ message.length - 8 ];

    int i = 0 ;

    while( i < message.length - 8 ){
      message.data[ i ] = buffer[ offset + 8 + i ];
      ++ i ;
    }

    if( testchecksum ){

      short checksum = SocketUtils.checksum( source ,
                                             destination ,
                                             (byte) SocketConstants.IPPROTO_UDP ,
                                             buffer ,
                                             offset ,
                                             message.length );

      if( checksum != 0 ){
        throw new IOException("Checksum error: " + checksum );
      }
    }

    return message ;
  }
}

