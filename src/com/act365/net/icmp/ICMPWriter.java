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

package com.act365.net.icmp ;
 
import com.act365.net.*;

/**
 ICMPWriter writes ICMPMessage objects to a byte stream.
*/

public class ICMPWriter {

  short identifier ;

  short counter ; 

  /** 
   Creates a writer to write to DatagramPacket objects.
   The identifier will appear in the transmitted datagram packet
   in modified form in order to enable any reply to be associated 
   with the sender. Typically, the field might be populated with 
   a short generated from the hash code of the transmitting object.
  */

  public ICMPWriter( short identifier ){
    this.identifier = identifier ;
    counter = 0 ;
  }

  static byte[] write( ICMPMessage message ){

    byte[] buffer = new byte[ message.data.length + 8 ];

    buffer[0] = message.type ;
    buffer[1] = message.code ;
    buffer[2] = (byte)( message.checksum >>> 8 );
    buffer[3] = (byte)( message.checksum & 0xff );
    buffer[4] = (byte)( message.identifier >>> 8 );
    buffer[5] = (byte)( message.identifier & 0xff );
    buffer[6] = (byte)( message.sequence_number >>> 8 );
    buffer[7] = (byte)( message.sequence_number & 0xff );

    int i = 7 ;

    while( ++ i < buffer.length ){
      buffer[i] = message.data[i-8];
    }

    return buffer ;
  }

  /**
   Builds a full ICMPMessage object and writes it to a buffer.
  */

  public byte[] write( byte type ,
                       byte code ,
                       byte[] data ) 
  {
    ICMPMessage message = new ICMPMessage();

    message.type = type ;
    message.code = code ;
    message.checksum = 0 ;
    message.identifier = identifier ;
    message.sequence_number = counter ++ ;
    message.data = data ;

    message.checksum = SocketUtils.checksum( write( message ) , 0 , 8 + data.length );
       
    return write( message );
  }
}

