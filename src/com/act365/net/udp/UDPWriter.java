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

import com.act365.net.*;

import java.io.IOException ;

/**
 * Class <code>UDPWriter</code> writes UDP messages into bytestreams.
 */
 
public class UDPWriter {

  static int write( UDPMessage message , byte[] buffer , int offset , int count ) throws IOException {

    final int length = message.length();
    
    if( count < length ){
        throw new IOException("UDP Write buffer overflow");
    }
    
    SocketUtils.shortToBytes( message.sourceport , buffer , offset );
    SocketUtils.shortToBytes( message.destinationport , buffer , offset + 2 );
    SocketUtils.shortToBytes( message.length , buffer , offset + 4 );
    SocketUtils.shortToBytes( message.checksum , buffer , offset + 6 );

    int i = 0 ;

    while( i < message.count ){
      buffer[ offset + 8 + i ] = message.data[ message.offset + i ];
      ++ i ;
    }

    return length ;
  }

  /**
   * Builds a UDP message and writes it to a bytestream.
   */

  public static int write( byte[] sourceaddress ,
                           short  sourceport ,
                           byte[] destinationaddress ,
                           short  destinationport ,
                           byte[] data ,
                           int    dataOffset ,
                           int    dataCount ,
                           byte[] buffer ,
                           int    offset ,
                           int    count  ) throws IOException {

    UDPMessage message = new UDPMessage();

    message.sourceport = sourceport ;
    message.destinationport = destinationport ;
    message.length = (short)( dataCount + 8 );
    message.checksum = 0 ;
    message.data = data ;

    final int length = write( message , buffer , offset , count );
    
    message.checksum = SocketUtils.checksum( sourceaddress ,
                                             destinationaddress ,
                                             (byte) SocketConstants.IPPROTO_UDP ,
                                             buffer ,
                                             offset ,                                             
                                             length );

    SocketUtils.shortToBytes( message.checksum , buffer , offset + 6 );

    return length ;
  }

  /**
   * @deprecated Use the other form of write(), which avoids a buffer copy
   */

  public static byte[] write( byte[] sourceaddress ,
                              short  sourceport ,
                              byte[] destinationaddress ,
                              short  destinationport ,
                              byte[] data ,
                              int    dataOffset ,
                              int    dataCount ) throws IOException {
  
      byte[] buffer = new byte[ 8 + dataCount ];
      
      write( sourceaddress ,
             sourceport ,
             destinationaddress ,
             destinationport ,
             data ,
             dataOffset ,
             dataCount ,
             buffer ,
             0 ,
             buffer.length );
             
      return buffer ;                            
  }
}

