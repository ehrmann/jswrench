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

package com.act365.net.dns ;

import com.act365.net.* ;
import com.act365.net.ip.* ;
import com.act365.net.udp.* ;

import java.net.*;

/**
 Implements a DNS client. 
 Usage: <code>DNSLookup -r -p protocol -l localhost DNSServer domain</code>.
 <code>-r</code> (optional) indicates whether recursion is desired, i.e. 
 whether a query that cannot be resolved by the named server should be
 passed on to another server.
 <p><code>-p protocol</code> (optional) defines the socket protocol to be used.
 By default, the JDK UDP implementation will be used. The alternatives are
 UDP, RawUDP and RawHdrUDP. 
 <p><code>-l localhost</code> (optional) should be specified if the protocol
 has been set to RawHdrUDP. The information will be used to construct the IP header.
 <p><code>DNSServer</code> is the IP address of the DNS server.
 <p><code>domain</code> is the domain name to be resolved.
*/

public class DNSLookup {

  /**
   Sends a DNS query to a DNS server.
  */

  public static void main( String[] args ) {

    final String errortext = "Usage: DNSLookup -r -p protocol -l localhost DNSServer domain";

    if( args.length < 2 ){
    	System.err.println( errortext );
    	System.exit( 1 );
    }
    
    int i = -1 ;

    boolean recursion_desired = false ;

    String servername = args[ args.length - 2 ] ,
           domainname = args[ args.length - 1 ] ,
           protocollabel = "",
           localhost = null ;

    while( ++ i < args.length - 2 ){
      if( args[ i ].equals("-r") ){
        recursion_desired = true ;
      } else if( args[ i ].equals("-p") && i < args.length - 3 ){
      	protocollabel = args[ ++ i ];
      	if( ! protocollabel.equalsIgnoreCase("UDP") && 
		    ! protocollabel.equalsIgnoreCase("RawUDP") &&
			! protocollabel.equalsIgnoreCase("RawHdrUDP") ){
      	    	System.err.println("Unsupported protocol");
      	    	System.exit( 2 );
      	    }
      } else if( args[ i ].equals("-l") && i < args.length - 3 ){
      	localhost = args[ ++ i ];
      } else {
        System.err.println( errortext );
        System.exit( 1 );
      }
    }

    try {
      SocketUtils.setProtocol( protocollabel );
    } catch ( java.io.IOException e ) {
      System.err.println("Unsupported protocol");
      System.exit( 2 );
    }
    
    final boolean israw = protocollabel.equalsIgnoreCase("RawUDP") ||
                          protocollabel.equalsIgnoreCase("RawHdrUDP");
    
    final boolean includeheader = SocketUtils.includeHeader();
    
    new SocketWrenchSession();
    
    InetAddress server = null ,
                source = null ;

    try {
      server = InetAddress.getByName( servername );
      if( localhost instanceof String ){
        source = InetAddress.getByName( localhost );
      }
    } catch( UnknownHostException e ){
      System.err.println("DNS server " + e.getMessage() + " is unknown");
      System.exit( 2 );
    }

    DatagramSocket socket = null ;

    try {
      if( source instanceof InetAddress ){
        socket = new DatagramSocket( 53 , source );
      } else {
        socket = new DatagramSocket( 53 );
      }
    } catch ( SocketException e ) {
      System.err.println( e.getMessage() );
      System.exit( 3 );
    }

    final int maxdatagramlength = 512 ;

    byte[] sendbuffer = null ,
           recvbuffer = new byte[ maxdatagramlength ];

    DatagramPacket packet = null ;

    DNSMessage message = null ;

    // A raw UDP header comprises 20 bytes of IP4 info and 8 bytes of UDP.
    
    DNSReader reader = new DNSReader( israw ? 28 : 0 ); 

    try {
      
      sendbuffer = DNSWriter.write( (short) socket.hashCode() , recursion_desired , domainname );

      if( israw ){
	    sendbuffer = UDPWriter.write( source.getAddress() , (short) 53 , server.getAddress() , (short) 53 , sendbuffer , 0 , sendbuffer.length );
      }
      
      if( includeheader ){

		sendbuffer = IP4Writer.write( IP4.TOS_COMMAND ,
									 (short) 255 ,
									 (byte) SocketConstants.IPPROTO_UDP ,
									 source.getAddress() ,
									 server.getAddress() ,
									 sendbuffer );
      }

	  SocketUtils.dump( System.out , sendbuffer , 0 , sendbuffer.length );
      
      socket.send( new DatagramPacket( sendbuffer , sendbuffer.length , server , 53 ) );

      packet = new DatagramPacket( recvbuffer , maxdatagramlength );

      socket.receive( packet );

      SocketUtils.dump( System.out , packet.getData() , 0 , packet.getLength() );
      
      reader.read( packet.getData() ).dump( System.out );

    } catch ( Exception e ) {
      System.err.println( e.getMessage() );
      e.printStackTrace();
      System.exit( 4 );
    }
  }

}

