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

package com.act365.net.ping ;

import com.act365.net.*;
import com.act365.net.icmp.* ;
import com.act365.net.ip.* ;

import java.net.*;
import java.util.*;

/**
 * 
 * <code>PingSender</code> objects transmit Ping packets.
 */

public class PingSender extends Thread {

  DatagramSocket socket ;

  InetAddress hostaddr ,
              localaddr ;

  short identifier ,
        ttl ;

  Ping ping ;

  int count ,
      nbytes ;

  /**
   * Sole constructor
   * 
   * @param socket socket to be used to transmit
   * @param hostaddr remote address to be pinged
   * @param localaddr local address to appear in IP header (RawICMP only)
   * @param identifier identifier to appear in ICMP message
   * @param ping object that created the <code>PingSender</code> object
   * @param count number of packets to be transmitted
   * @param nbytes size of packets to be transmitted
   * @param ttl time-to-live of packets (RawICMP only)
   */
  
  public PingSender( DatagramSocket socket ,
                     InetAddress hostaddr ,
                     InetAddress localaddr ,
                     short identifier ,
                     Ping ping ,
                     int count ,
                     int nbytes ,
                     short ttl ) {

    this.socket = socket ;
    this.hostaddr = hostaddr ;
    this.localaddr = localaddr ;
    this.identifier = identifier ; 
    this.ping = ping ;
    this.count = count ;
    this.nbytes = nbytes ;
    this.ttl = ttl ;
  }

  /**
   * Starts the transmission process.
   */
  
  public void run() {

    ICMPWriter writer = new ICMPWriter( identifier );

    byte[] buffer ,
           databuffer = new byte[ nbytes ];

    int i = -1 ;

    while( ++ i < databuffer.length ){
      databuffer[i] = (byte) i ;
    }

    try {
      while( count -- != 0 ){
        if( nbytes >= 8 ){
            SocketUtils.longToBytes( new Date().getTime() , databuffer , 0 );
        }
        buffer = writer.write( ICMP.ICMP_ECHO , (byte) 0 , databuffer , 0 , databuffer.length );
        
        if( SocketWrenchSession.includeHeader() ){
		  buffer = IP4Writer.write( IP4.TOS_ICMP , 
		  						    ttl , 
								    (byte) SocketConstants.IPPROTO_ICMP , 
								    localaddr != null ? localaddr.getAddress() : new byte[4], 
								    hostaddr.getAddress() , 
								    buffer );
        }

		socket.send( new DatagramPacket( buffer , buffer.length , hostaddr , 0 ) );

        sleep( 1000 );
      }
      ping.interrupt();
    } catch ( Exception e ) {
      System.err.println( e.getMessage() );
      System.exit( 1 );
    }
  }
}

