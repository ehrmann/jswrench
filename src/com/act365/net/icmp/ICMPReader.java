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

  short identifier ;

  /**
   Creates a reader to read from DatagramPacket objects.
   The reader will only accept ICMP packets with the
   specified identifier.
  */

  public ICMPReader( short identifier ){
    this.identifier = identifier ;
  }

  /**
   read() constructs an ICMP message from a buffer. An exception will
   be thrown if the message is not in the ICMP format or if there is a
   checksum error but the message will simply be ignored should the 
   identifier does not match.
   @return The ICMPMessage contained in the packet or null if the
           identifier did not match.
  */

  public ICMPMessage read( byte[] buffer, int length, int offset, boolean testchecksum ) throws IOException {

     if( length - offset < 8 ) {
       throw new IOException("ICMP messages must be at least eight bytes long");
     }

     short checksum ;

     if( testchecksum && ( checksum = SocketUtils.checksum( buffer , length , offset ) ) != 0 ){
       throw new IOException("Checksum error: " + checksum );
     }

     ICMPMessage message = new ICMPMessage();

     message.type = buffer[ offset ];
     message.code = buffer[ offset + 1 ];
     message.checksum = SocketUtils.shortFromBytes( buffer , offset + 2 );

     boolean isquery ;

     int datastart ;

     switch( message.type ){

     case ICMP.ICMP_ECHOREPLY:
     case ICMP.ICMP_ECHO:
     case ICMP.ICMP_ROUTERADVERT:
     case ICMP.ICMP_ROUTERSOLICIT:
     case ICMP.ICMP_TIMESTAMP:
     case ICMP.ICMP_TIMESTAMPREPLY:
     case ICMP.ICMP_INFO_REQUEST:
     case ICMP.ICMP_INFO_REPLY:
     case ICMP.ICMP_ADDRESS:
     case ICMP.ICMP_ADDRESSREPLY:

       isquery = true ;
       datastart = 8 ;
       break;

     case ICMP.ICMP_DEST_UNREACH:
     case ICMP.ICMP_SOURCE_QUENCH:
     case ICMP.ICMP_REDIRECT:
     case ICMP.ICMP_TIME_EXCEEDED:
     case ICMP.ICMP_PARAMETERPROB:

       isquery = false ;
       datastart = 4 ;
       break;

     default:

       return null ;
     } 

     if( isquery ){

       message.identifier = SocketUtils.shortFromBytes( buffer , offset + 4 );

       if( identifier != message.identifier ){
         return null ;
       }

       message.sequence_number = SocketUtils.shortFromBytes( buffer , offset + 6 );
     }

     byte[] data = new byte[ length - offset - datastart ];

     int i = offset + datastart ;

     while( i < length ){
       data[ i - offset - datastart ] = buffer[i];
       ++ i ;
     }

     message.data = data ;

     return message ;
  }
}
