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

import java.io.IOException ;

/**
 IP4Writer writes IP4Message objects to a buffer.
 Fragmentation isn't supported - all messages are created
 with the DONT_FRAGMENT bit set and with offset set to 0.
 However, the identified is increased in order to identify
 packets uniquely.
*/

public class IP4Writer {

  static short identifier = 0 ;

  static int write( IP4Message message , byte[] buffer , int offset , int count ) throws IOException {

    final int length = message.length();
    
    if( count < length ){
        throw new IOException("IP4 Write buffer overflow");
    }
    
    buffer[ offset ] = (byte)( message.version << 4 | message.headerlength ); 
    buffer[ offset + 1 ] = message.typeofservice ;

    SocketUtils.shortToBytes( message.length , buffer , offset + 2 );
    SocketUtils.shortToBytes( message.identifier , buffer , offset + 4 );
    SocketUtils.shortToBytes( message.offset , buffer , offset + 6 );

    buffer[ offset + 6 ] |= message.flags ;

    buffer[ offset + 8 ] = (byte) message.timetolive ;
    buffer[ offset + 9 ] = message.protocol ;

    SocketUtils.shortToBytes( message.checksum , buffer , offset + 10 );

    buffer[ offset + 12 ] = message.source[0];
    buffer[ offset + 13 ] = message.source[1];
    buffer[ offset + 14 ] = message.source[2];
    buffer[ offset + 15 ] = message.source[3];

    buffer[ offset + 16 ] = message.destination[0];
    buffer[ offset + 17 ] = message.destination[1];
    buffer[ offset + 18 ] = message.destination[2];
    buffer[ offset + 19 ] = message.destination[3];
  
    byte b = 4 ;

    while( ++ b < message.headerlength ){
      SocketUtils.intToBytes( message.options[ b - 5 ] , buffer , offset + 4 * b );
    }

    int i = 0 ;
    
    while( i < message.dataCount ){
      buffer[ offset + i ] = message.data[ i + message.dataOffset ];
      ++ i ;
    }
    
    return length ;
  }

  /**
   Builds a full IP4Message object and writes it to a buffer.
   It is assumed that no options will be specified.
  */
  
  public static int write( byte typeofservice ,
                           short timetolive ,
                           byte protocol ,
                           byte[] source ,
                           byte[] destination ,
                           byte[] data ,
                           byte[] buffer ,
                           int offset ,
                           int count ) throws IOException
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

    write( message , buffer , offset , count );
    
    /*
     * The checksum is calculated from the IP header alone -
     * the data that follows is ignored. 
     */
     
    message.checksum = SocketUtils.checksum( buffer , offset , 20 );

    SocketUtils.shortToBytes( message.checksum , buffer , offset + 10 );

    return message.length();       
  }
  
  /**
   * @deprecated Use the other form of write() - it avoids buffer copy
   */

  public static byte[] write( byte typeofservice ,
                              short timetolive ,
                              byte protocol ,
                              byte[] source ,
                              byte[] destination ,
                              byte[] data ) throws IOException
  {
      byte[] buffer = new byte[ 20 + data.length ];
      
      write( typeofservice , timetolive , protocol , source , destination , data , buffer , 0 , buffer.length );
      
      return buffer ;  
  }
  
}

