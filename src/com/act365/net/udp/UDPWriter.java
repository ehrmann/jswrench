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

/**
 * Class <code>UDPWriter</code> writes UDP messages into bytestreams.
 */

public class UDPWriter {

  static byte[] write( UDPMessage message ){

    byte[] buffer = new byte[ message.length ];

    SocketUtils.shortToBytes( message.sourceport , buffer , 0 );
    SocketUtils.shortToBytes( message.destinationport , buffer , 2 );
    SocketUtils.shortToBytes( message.length , buffer , 4 );
    SocketUtils.shortToBytes( message.checksum , buffer , 6 );

    int i = 7 ;

    while( ++ i < buffer.length ){
      buffer[ i ] = message.data[ i - 8 ];
    }

    return buffer ;
  }

  /**
   * Builds a UDP message and writes it to a bytestream.
   */

  public static byte[] write( byte[] sourceaddress ,
                              short  sourceport ,
                              byte[] destinationaddress ,
                              short  destinationport ,
                              byte[] data ,
                              int    datalength ) {

    UDPMessage message = new UDPMessage();

    message.sourceport = sourceport ;
    message.destinationport = destinationport ;
    message.length = (short)( datalength + 8 );
    message.checksum = 0 ;
    message.data = data ;

    message.checksum = SocketUtils.checksum( sourceaddress ,
                                                   destinationaddress ,
                                                   (byte) SocketConstants.IPPROTO_UDP ,
                                                   message.length ,
                                                   write( message ) ,
                                                   (int) 0 );

    return write( message );
  }
}

