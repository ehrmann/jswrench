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

/**
 IP4Writer writes IP4Message objects to a DatagramPacket.
 Fragmentation isn't supported - all messages are created
 with the DONT_FRAGMENT bit set and with offset set to 0.
 However, the identified is increased in order to identify
 packets uniquely.
*/

public class IP4Writer {

  static short identifier = 0 ;

  static byte[] write( IP4Message message ){

    byte[] buffer = new byte[ message.length ];

    buffer[0] = (byte)( message.version << 4 | message.headerlength ); 
    buffer[1] = message.typeofservice ;

    SocketUtils.shortToBytes( message.length , buffer , 2 );
    SocketUtils.shortToBytes( message.identifier , buffer , 4 );
    SocketUtils.shortToBytes( message.offset , buffer , 6 );

    buffer[6] |= message.flags ;

    buffer[8] = (byte) message.timetolive ;
    buffer[9] = message.protocol ;

    SocketUtils.shortToBytes( message.checksum , buffer , 10 );

    buffer[12] = message.source[0];
    buffer[13] = message.source[1];
    buffer[14] = message.source[2];
    buffer[15] = message.source[3];

    buffer[16] = message.destination[0];
    buffer[17] = message.destination[1];
    buffer[18] = message.destination[2];
    buffer[19] = message.destination[3];
  
    byte b = 4 ;

    while( ++ b < message.headerlength ){
      SocketUtils.intToBytes( message.options[ b - 5 ] , buffer , 4 * b );
    }

    int i = 0 ;
    
    while( i < message.dataCount ){
      buffer[i] = message.data[ i + message.dataOffset ];
      ++ i ;
    }
    
    return buffer ;
  }

  /**
   Builds a full IP4Message object and writes it to a buffer.
   It is assumed that no options will be specified.
  */

  public static byte[] write( byte typeofservice ,
                              short timetolive ,
                              byte protocol ,
                              byte[] source ,
                              byte[] destination ,
                              byte[] data ) 
  {
    IP4Message message = new IP4Message();

    message.version = 4 ;
    message.headerlength = 5 ;
    message.typeofservice = typeofservice ;
    message.length = (short)( 20 + data.length );
    message.identifier = identifier ++ ;
    message.flags = IP4.DONT_FRAGMENT ;
    message.offset = (short) 0 ;
    message.timetolive = timetolive ;
    message.protocol = protocol ;
    message.checksum = 0 ;
    message.source = source ;
    message.destination = destination ;
    message.options = new int[0];
    message.data = data ;

    message.checksum = SocketUtils.checksum( write( message ) , 20 , 0 );
       
    return write( message );
  }
}

