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

package com.act365.net.tcp ;

import com.act365.net.* ;

import java.io.IOException ;

/**
 * Reads TCP messages.
 */

public class TCPReader {

  /**
   * Reads a TCP message from a buffer without a checksum test.
   */

  public static TCPMessage read( byte[] buffer , int offset , int count ) throws IOException {
    return read( buffer , offset , count , false , new byte[0] , new byte[0] );
  }

  /**
   * Reads a TCP message from a buffer and performs a checksum test
   * @param buffer buffer to be read from
   * @param offset position within buffer to read from
   * @param count number of bytes to read
   * @param testchecksum whether to test the checksum in the message
   * @param source message source (used in checksum test)
   * @param destination message destination (used in checksum test)
   * @return the decoded message
   * @throws IOException checksum error
   */
  
  public static TCPMessage read( byte[] buffer , 
                                 int offset , 
                                 int count ,
                                 boolean testchecksum ,
                                 byte[] source ,
                                 byte[] destination ) throws IOException {

    TCPMessage message = new TCPMessage();

    message.sourceport = SocketUtils.shortFromBytes( buffer , offset );
    message.destinationport = SocketUtils.shortFromBytes( buffer , offset + 2 );
    message.sequencenumber = SocketUtils.intFromBytes( buffer , offset + 4 );
    message.acknowledgementnumber = SocketUtils.intFromBytes( buffer , offset + 8 );
    
    int headerlength = buffer[ offset + 12 ] >= 0 ? buffer[ offset + 12 ] : 0xffffff00 ^ buffer[ offset + 12 ];

    message.headerlength = headerlength >> 4 ;
    message.urg = ( buffer[ offset + 13 ] & TCP.URG ) != 0 ;
    message.ack = ( buffer[ offset + 13 ] & TCP.ACK ) != 0 ;
    message.psh = ( buffer[ offset + 13 ] & TCP.PSH ) != 0 ;
    message.rst = ( buffer[ offset + 13 ] & TCP.RST ) != 0 ;
    message.syn = ( buffer[ offset + 13 ] & TCP.SYN ) != 0 ;
    message.fin = ( buffer[ offset + 13 ] & TCP.FIN ) != 0 ;
    message.windowsize = SocketUtils.shortFromBytes( buffer , offset + 14 );
    message.checksum = SocketUtils.shortFromBytes( buffer , offset + 16 );
    message.urgentpointer = SocketUtils.shortFromBytes( buffer , offset + 18 );
   
    if( count < 4 * message.headerlength ){
      throw new IOException("TCP message should be longer");
    }

    message.options = new byte[ 4 * message.headerlength - 20 ];

    int i = offset + 19 ;

    while( ++ i < offset + 4 * message.headerlength ){
      message.options[ i - offset - 20 ] = buffer[ i ];
    }

    message.data = buffer ;
    message.datastart = offset + 4 * message.headerlength ;
    message.dataend = offset + count ;

    if( testchecksum ){

      short checksum = SocketUtils.checksum( source ,
                                             destination ,
                                             (byte) SocketConstants.IPPROTO_TCP ,
                                             buffer ,
                                             offset ,
                                             count ); 

      if( checksum != 0 ){
        throw new IOException("Checksum error: " + checksum );
      }
    }

    return message ;
  }
}


