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

import java.io.IOException ;

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

  static int write( ICMPMessage message , byte[] buffer , int offset , int count ) throws IOException {

    final int length = message.length();
     
    if( count < length ){
        throw new IOException("ICMP Write buffer overflow");
    }
    
    buffer[ offset ]     = message.type ;
    buffer[ offset + 1 ] = message.code ;    
    SocketUtils.shortToBytes( message.checksum , buffer , offset + 2 );
    SocketUtils.shortToBytes( message.identifier , buffer , offset + 4 );
    SocketUtils.shortToBytes( message.sequence_number , buffer , offset + 6 );
    
    int i = 0 ;

    while( i < message.count ){
      buffer[ offset + 8 + i ] = message.data[ i + message.offset ];
      ++ i ;
    }

    return length ;
  }

  /**
   Builds a full ICMPMessage object and writes it to a buffer.
  */

  public int write( byte type , 
                    byte code , 
                    byte[] data , 
                    byte[] buffer , 
                    int offset , 
                    int count ) throws IOException 
  {
    ICMPMessage message = new ICMPMessage();

    message.type = type ;
    message.code = code ;
    message.checksum = 0 ;
    message.identifier = identifier ;
    message.sequence_number = counter ++ ;
    message.data = data ;

    final int length = write( message , buffer , offset , count );
    
    message.checksum = SocketUtils.checksum( buffer , offset , length );
    SocketUtils.shortToBytes( message.checksum , buffer , offset + 2 );
       
    return length ;
  }

  /**
   * @deprecated Use the other form of write(), which avoids a buffer copy
   */
  
  public byte[] write( byte type ,
                       byte code ,
                       byte[] data ) throws IOException  
  {
      byte[] buffer = new byte[ 8 + data.length ];
      
      write( type , code , data , buffer , 0 , data.length );
      
      return buffer ;
  }
}

