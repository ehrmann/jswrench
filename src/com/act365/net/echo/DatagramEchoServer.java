
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
 DatagramEchoServer acts as the server for the UDP echo service.
 Usage: <code>DatagramEchoServer -p protocol -d datagramlength -l localhost localport</code>.
 <p><code>-p protocol</code> (optional) defines the socket protocol to be used.
 By default, the JDK UDP implementation will be used. The alternatives are
 UDP, RawUDP or SystemUDP. 
 <p><code>-d datagramlength</code> (optional) defines the maximum lenght of
 datagram packet that will be supported by the application. The default is 512
 bytes.
 <p><code>-l localhost</code> (optional) should be specified if the protocol
 has been set to RawUDP. The information will be used to construct the IP header.
 <p><code>localport</code> is the port to be used by the echo server.
 <p>The UDP protocol should be used on XP while the RawUDP protocol should be 
 used on Linux. The issue is that for a raw socket, <code>recvfrom()</code> 
 includes the IP header in its calculation of the number of bytes read on Linux 
 but excludes it on XP. 
*/

public class DatagramEchoServer {

  public static void main( String[] args ){
    
    final String errortext = "DatagramEchoServer -p protocol -d datagramlength -l localhost localport";
    
    if( args.length == 0 ){
    	System.err.println( errortext );
    	System.exit( 1 );
    }
    
    int i    = -1 ,
        port = 0 ,
        maxDatagramLength = 512 ;

    String protocollabel = "JDKUDP",
           localhost = null ;
    
    while( ++ i < args.length ){
      if( args[ i ].equals("-p") && i < args.length - 1 ){
      	protocollabel = args[ ++ i ];
      } else if( args[ i ].equals("-l") && i < args.length - 2 ){
      	localhost = args[ ++ i ];
        try {
          port = Integer.parseInt( args[ ++ i ] );
        } catch( NumberFormatException e ){
          System.err.println("Invalid localport number");
          System.exit( 2 );
        }
      } else if( args[ i ].equals("-d") && i < args.length - 1 ){
        try {
          maxDatagramLength = Integer.parseInt( args[ ++ i ] );
        } catch( NumberFormatException e ){
          System.err.println("Invalid datagram length");
          System.exit( 3 );
        }
      } else {
        System.err.println( errortext );
        System.exit( 1 );
      }
    }

    InetAddress localaddr = null ;
    
    try {
      if( localhost instanceof String ){
        localaddr = InetAddress.getByName( localhost );
      }
    } catch( UnknownHostException e ){
      System.err.println("Address " + e.getMessage() + " is unknown");
      System.exit( 5 );
    }

    new SocketWrenchSession();

	try {
	  SocketWrenchSession.setProtocol( protocollabel );
	} catch ( java.io.IOException e ) {
	  System.err.println("Unsupported protocol");
	  System.exit( 1 );
	}

    if( SocketWrenchSession.getProtocol() != SocketConstants.IPPROTO_UDP ){    
      System.err.println("UDP protocol must be selected");
      System.exit( 1 );
    }
    
    new DatagramEchoServer( localaddr , port , maxDatagramLength );
  }

  DatagramEchoServer( InetAddress localaddr ,
                      int port ,
                      int maxDatagramLength ){

    try {

      JSWDatagramSocket server = null ;

	  try { 
		if( localaddr instanceof InetAddress ){
		  server = new JSWDatagramSocket( port , localaddr );   	
		} else {
		  server = new JSWDatagramSocket( port );
		}
	  } catch( IOException e ){
		System.err.println( e.getMessage() );
		System.exit( 6 );
	  }

	  System.err.println("Local address: " + ( localaddr instanceof InetAddress ? localaddr : server.getLocalAddress() ).toString() );
	  System.err.println("Local port: " + server.getLocalPort() );
      
      IP4Message ip4Message = new IP4Message();
      
      UDPMessage udpMessage = new UDPMessage();
      
      while( true ){

        server.receive( ip4Message , udpMessage );

        udpMessage = new UDPMessage( (short) port ,
                                     (short) ( udpMessage.sourceport >= 0 ? udpMessage.sourceport : udpMessage.sourceport ^ 0xffffff00 ) ,
                                     udpMessage.getData() ,
                                     udpMessage.getOffset() ,
                                     udpMessage.getCount() );
        
        server.send( udpMessage , GeneralSocketImpl.createInetAddress( SocketConstants.AF_INET , ip4Message.source ) );
      }

    } catch ( SocketException se ) {
      System.err.println( se.getMessage() );
      System.exit( 4 );
    } catch ( IOException ioe ) {
      System.err.println( ioe.getMessage() );
      System.exit( 5 );
    } catch( Exception e ){
        e.printStackTrace();
      System.err.println( e.getMessage() );
      System.exit( 6 );
    }
  }
}
