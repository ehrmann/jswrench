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
 * Writes TCP messages to a bytestream.
 */

public class TCPWriter {

  static int write( TCPMessage message , byte[] buffer , int offset ) throws IOException {

    final int length = message.length();

    SocketUtils.shortToBytes( message.sourceport , buffer , offset );
    SocketUtils.shortToBytes( message.destinationport , buffer , offset + 2 );
    SocketUtils.intToBytes( message.sequencenumber , buffer , offset + 4 );
    SocketUtils.intToBytes( message.acknowledgementnumber , buffer , offset + 8 );
    
    buffer[ offset + 12 ] = (byte)( message.headerlength << 4 );
    buffer[ offset + 13 ] = 0 ;

    if( message.urg ) buffer[ offset + 13 ] |= TCP.URG ;
    if( message.ack ) buffer[ offset + 13 ] |= TCP.ACK ;
    if( message.psh ) buffer[ offset + 13 ] |= TCP.PSH ;
    if( message.rst ) buffer[ offset + 13 ] |= TCP.RST ;
    if( message.syn ) buffer[ offset + 13 ] |= TCP.SYN ;
    if( message.fin ) buffer[ offset + 13 ] |= TCP.FIN ;

    SocketUtils.shortToBytes( message.windowsize , buffer , offset + 14 );
    SocketUtils.shortToBytes( message.checksum , buffer , offset + 16 );
    SocketUtils.shortToBytes( message.urgentpointer , buffer , offset + 18 );

    int i = 20 ;

    while( i < 20 + message.options.length ){
      buffer[ offset + i ] = message.options[ i - 20 ];
      ++ i ;
    }

    while( i < length ){
      buffer[ offset + i ] = message.data[( message.datastart + i - 20 - message.options.length )% message.data.length ];
      ++ i ;
    }

    return length ;
  }

  /**
   * Writes a TCP message to a bytestream.
   */

  public static int write( byte[] sourceaddress ,
                           short sourceport ,
                           byte[] destinationaddress ,
                           short destinationport ,
                           int sequencenumber ,
                           int acknowledgementnumber ,
                           boolean ack ,
                           boolean rst ,
                           boolean syn ,
                           boolean fin ,
                           boolean psh ,
                           short windowsize ,
                           TCPOptions options ,
                           byte[] writebuffer ,
                           int writestart ,
                           int writeend ,
                           byte[] buffer ,
                           int offset ) throws IOException {
  
    TCPMessage message = new TCPMessage();

    message.sourceport = sourceport ;
    message.destinationport = destinationport ;
    message.sequencenumber = sequencenumber ;
    message.acknowledgementnumber = acknowledgementnumber ;
    message.headerlength = 5 ;
    message.urg = false ;
    message.ack = ack ;
    message.psh = psh ;
    message.rst = rst ;
    message.syn = syn ;
    message.fin = fin ;
    message.windowsize = windowsize ; 
    message.checksum = 0 ;
    message.urgentpointer = 0 ;

    byte[] optionsbuffer = new byte[ 128 ];

    int cursor = 0 ;

    if( options.isMaxSegmentSizeSet() ){
      optionsbuffer[ cursor ] = 2 ;
      optionsbuffer[ cursor + 1 ] = 4 ;
      SocketUtils.shortToBytes( options.getMaxSegmentSize() , optionsbuffer , cursor + 2 );
      cursor += 4 ;
    }
 
    if( options.isWindowScaleFactorSet() ){
      optionsbuffer[ cursor ] = 1 ;
      optionsbuffer[ cursor + 1 ] = 3 ;
      optionsbuffer[ cursor + 2 ] = 3 ;
      optionsbuffer[ cursor + 3 ] = options.getWindowScaleFactor() ;
      cursor += 4 ;
    }

    if( options.isTimestampSet() ){
      optionsbuffer[ cursor ] = 1 ;
      optionsbuffer[ cursor + 1 ] = 1 ;
      optionsbuffer[ cursor + 2 ] = 8 ;
      optionsbuffer[ cursor + 3 ] = 10 ;
      SocketUtils.intToBytes( options.getTimestampValue() , optionsbuffer , cursor + 4 );
      SocketUtils.intToBytes( options.getTimestampEchoReply() , optionsbuffer , cursor + 8 );
      cursor += 12 ;
    }

    message.headerlength += cursor / 4 ; 
    message.options = new byte[ cursor ];

    int i = -1 ;

    while( ++ i < cursor ){
      message.options[ i ] = optionsbuffer[ i ];
    }

    message.data = writebuffer ;
    message.datastart = writestart ;
    message.dataend = writeend ;

    int length = 0 ;
    
    try {
        length = write( message , buffer , offset );
    } catch ( ArrayIndexOutOfBoundsException e ) {
        throw new IOException("TCP Write buffer overflow");
    }
        
    message.checksum = SocketUtils.checksum( sourceaddress ,
                                             destinationaddress ,
                                             (byte) SocketConstants.IPPROTO_TCP ,
                                             buffer ,
                                             offset ,
                                             length );

    SocketUtils.shortToBytes( message.checksum , buffer , offset + 16 );

    return length ;
  }
}    
