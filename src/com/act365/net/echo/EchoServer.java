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

import java.io.*;
import java.net.*;

/**
 EchoServer acts as the server for the TCP echo service.
 Usage: <code>EchoServer -p protocol -l localhost localport</code>.
 NB When a TCPJ protocol has been chosen, the server will be created with a
 backlog of 1 (as against the standard Berkeley value of 5) because of a limitation
 in the current TCPJ implementation.
 <p><code>-p protocol</code> (optional) defines the socket protocol to be used.
 By default, the JDK TCP implementation will be used. The alternatives are
 TCPJ, RawTCP or RawTCPJ. (NB The RawTCP protocol will behave identically to TCP
 because no <code>DatagramSocket</code> objects will be instantiated. However, a
 <code>DatagramSocket</code> is always used by TCPJ, so the behaviour of TCPJ
 and RawTCPJ will differ).
 <p><code>-l localhost</code> (optional) should be specified if the protocol
 has been set to RawTCPJ. The information will be used to construct the IP header.
 <p><code>localport</code> is the port to be used by the echo server.
*/

public class EchoServer extends Thread {

  ServerSocket server = null ;

  public static void main( String[] args ){
    
	final String errortext = "EchoServer -p protocol -l localhost port";
    
	if( args.length == 0 ){
		System.err.println( errortext );
		System.exit( 1 );
	}
    
	int i    = -1 ,
		port = 0 ;

	String protocollabel = "" ,
		   localhost = null ;

	try {
	  port = Integer.parseInt( args[ args.length - 1 ]  );
	} catch( NumberFormatException e ){
	  System.err.println("Invalid port number");
	  System.exit( 3 );
	}

	while( ++ i < args.length - 1 ){
	  if( args[ i ].equals("-p") && i < args.length - 2 ){
		protocollabel = args[ ++ i ];
	  } else if( args[ i ].equals("-l") && i < args.length - 2 ){
		localhost = args[ ++ i ];
	  } else {
		System.err.println( errortext );
		System.exit( 1 );
	  }
	}

    new SocketWrenchSession();

	try {
	  SocketWrenchSession.setProtocol( protocollabel );
	} catch ( java.io.IOException e ) {
	  System.err.println("Unsupported protocol");
	  System.exit( 2 );
	}
    
	if( SocketWrenchSession.getProtocol() != SocketConstants.IPPROTO_TCP &&
        SocketWrenchSession.getProtocol() != SocketConstants.IPPROTO_TCPJ ){
            System.err.println("Unsupported protocol");
            System.exit( 2 );            
        }
    
	InetAddress localaddr = null ;
    
	try {
	  if( localhost instanceof String ){
		localaddr = InetAddress.getByName( localhost );
	  }
	} catch( UnknownHostException e ){
	  System.err.println("Address " + e.getMessage() + " is unknown");
	  System.exit( 4 );
	}

    new EchoServer( port , localaddr );
  }

  EchoServer( int port , InetAddress localaddr ){

	try { 
	  if( localaddr instanceof InetAddress ){
		server = new ServerSocket( port , 1 , localaddr );   	
	  } else {
		server = new ServerSocket( port );
	  }
	} catch( IOException e ){
	  System.err.println( e.getMessage() );
	  System.exit( 5 );
	}

    System.err.println("Server Socket: " + server.toString() );
    System.err.println("Local port: " + server.getLocalPort() );

    start();
  }

  public void run() {
    Socket socket ;
    while( true ){
      try {
        socket = server.accept();
		System.err.println("Socket is connected to: " + socket.getInetAddress() );
        ( new EchoWorker( socket.getInputStream() , socket.getOutputStream() ) ).start();
      } catch( IOException e ) {
        System.err.println( e.getMessage() );
        System.exit( 2 );
      }
    }
  }

  protected void finalize() {
    if( server != null ){
      try {
        server.close();
      } catch( IOException e ){
        System.err.println( e.getMessage() );
        System.exit( 3 );
      }
    }
  }
}

