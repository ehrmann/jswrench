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

package com.act365.net.echo ;

import com.act365.net.* ;
import com.act365.net.ip.*;
import com.act365.net.udp.*;

import java.io.*;
import java.net.*;

/**
 DatagramEchoClient acts as the client for the UDP echo service.
 Usage: <code>DatagramEchoClient -p protocol -d datagramlength -i inputfile -o outputfile -l localhost localport hostname hostport</code>.
 <p><code>-p protocol</code> (optional) defines the socket protocol to be used.
 By default, the JDK UDP implementation will be used. The alternatives are
 UDP, RawUDP or SystemUDP.
 <p><code>-d datagramlength</code> (optional) defines the maximum lenght of
 datagram packet that will be supported by the application. The default is 512
 bytes.
 <p><code>-l localhost localport</code> (optional) should be specified if the protocol
 has been set to RawUDP. The information will be used to construct the IP header.
 <p><code>hostname port</code> define the remote echo server.
 <p>The UDP protocol should be used on XP while the RawUDP protocol should be 
 used on Linux. The issue is that for a raw socket, <code>recvfrom()</code> 
 includes the IP header in its calculation of the number of bytes read on Linux 
 but excludes it on XP. 
*/

class DatagramEchoClient {

  public static void main( String[] args ){
	
    final String errortext = "DatagramEchoClient -p protocol -d datagramlength -l localhost localport hostname hostport";
    
    if( args.length < 2 ){
    	System.err.println( errortext );
    	System.exit( 1 );
    }
    
    String hostname   = args[ args.length - 2 ],
           localhost  = null ,
           protocollabel = "JDKUDP";

	int i    = -1 ,
		port = 0 ,
		localport = 1024 ,
		maxDatagramLength = 512 ;

	try {
	  port = Integer.parseInt( args[ args.length - 1 ] );
	} catch( NumberFormatException e ){
	  System.err.println("Invalid port number");
	  System.exit( 2 );
	}
    
    while( ++ i < args.length - 2 ){
      if( args[ i ].equals("-p") && i < args.length - 3 ){
      	protocollabel = args[ ++ i ];
      } else if( args[ i ].equals("-l") && i < args.length - 4 ){
      	localhost = args[ ++ i ];
        try {
          localport = Integer.parseInt( args[ ++ i ]  );
        } catch( NumberFormatException e ){
          System.err.println("Invalid localport number");
          System.exit( 4 );
        }
      } else if( args[ i ].equals("-d") && i < args.length - 3 ){
        try {
          maxDatagramLength = Integer.parseInt( args[ ++ i ]  );
        } catch( NumberFormatException e ){
          System.err.println("Invalid datagram length");
          System.exit( 5 );
        }
      } else {
        System.err.println( errortext );
        System.exit( 1 );
      }
    }

    InetAddress dstaddr = null ,
                localaddr = null ;

    try {
      dstaddr = InetAddress.getByName( hostname );
      if( localhost instanceof String ){
        localaddr = InetAddress.getByName( localhost );
      }
    } catch( UnknownHostException e ){
      System.err.println("Address " + e.getMessage() + " is unknown");
      System.exit( 6 );
    }

    new SocketWrenchSession();

	try {
	  SocketWrenchSession.setProtocol( protocollabel );
	} catch ( java.io.IOException e ) {
	  System.err.println("Unsupported protocol");
	  System.exit( 3 );
	}
    
	if( SocketWrenchSession.getProtocol() != SocketConstants.IPPROTO_UDP ){
        System.err.println("UDP protocol must be selected");
        System.exit( 7 );
	}
    
    JSWDatagramSocket socket = null ;

    try {
    	if( localaddr instanceof InetAddress ){
			socket = new JSWDatagramSocket( localport , localaddr );
    	} else {
    		socket = new JSWDatagramSocket( localport );
    	}
    } catch( SocketException se ){
      System.err.println( se.getMessage() );
      System.exit( 8 );
    }

	System.err.println("Local address: " + ( localaddr instanceof InetAddress ? localaddr : socket.getLocalAddress() ).toString() );
	System.err.println("Local port: " + socket.getLocalPort() );

    new DatagramEchoClient( socket , maxDatagramLength , dstaddr , port , localaddr , socket.getLocalPort() ); 

    System.exit( 0 );
  }

  public DatagramEchoClient( JSWDatagramSocket socket ,
                             int            maxDatagramLength , 
                             InetAddress    dest ,
                             int            port ,
                             InetAddress    localaddr ,
                             int            localport ){
    try {

      int bufferlength ;

      byte[] buffer = new byte[ maxDatagramLength ];

      IP4Message ip4Message = new IP4Message();
      UDPMessage udpMessage = new UDPMessage();
      
      while( ( bufferlength = System.in.read( buffer ) ) > -1 ){
        udpMessage = new UDPMessage( (short) localport , (short) port , buffer , 0 , bufferlength );
        socket.send( udpMessage , dest );
        socket.receive( ip4Message , udpMessage );
        System.out.write( udpMessage.getData() , udpMessage.getOffset() , udpMessage.getCount() );
      }

    } catch( IOException e ){
      System.err.println( e.getMessage() );
    }
  }
}


