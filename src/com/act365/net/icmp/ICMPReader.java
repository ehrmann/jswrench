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

import java.io.* ;

/**
 ICMPReader reads ICMPMessage objects from a DatagramPacket.
*/

public class ICMPReader {

  /**
   read() constructs an ICMP message from a buffer. An exception will
   be thrown if the message is not in the ICMP format or if there is a
   checksum error.
   
   @return The ICMPMessage contained in the packet
  */

  public static ICMPMessage read( byte[] buffer, int offset, int count, boolean testchecksum ) throws IOException {

     if( count < 4 ) {
       throw new IOException("ICMP messages must be at least four bytes long");
     }

     short checksum ;

     if( testchecksum && ( checksum = SocketUtils.checksum( buffer , offset , count ) ) != 0 ){
       throw new IOException("Checksum error: " + checksum );
     }

     ICMPMessage message = new ICMPMessage();

     message.type = buffer[ offset ];
     message.code = buffer[ offset + 1 ];
     message.checksum = SocketUtils.shortFromBytes( buffer , offset + 2 );

     boolean isquery ;

     int datastart ;

     boolean isQuery = message.isQuery();
     
     if( isQuery ){

         if( count < 8 ) {
           throw new IOException("ICMP query messages must be at least eight bytes long");
         }

         message.identifier = SocketUtils.shortFromBytes( buffer , offset + 4 );
         message.sequence_number = SocketUtils.shortFromBytes( buffer , offset + 6 );

         datastart = 8 ;
     } else {
         datastart = 4 ;
     } 
     
     message.data = buffer ;
     message.offset = offset + datastart ;
     message.count = count - datastart ;

     return message ;
  }
}
