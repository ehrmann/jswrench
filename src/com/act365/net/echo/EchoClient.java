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
 EchoClient acts as the client for the TCP echo service.
 Usage: <code>EchoClient -p protocol -i inputfile -o outputfile -l localhost localport hostname hostport</code>.
 <p><code>-p protocol</code> (optional) defines the socket protocol to be used.
 By default, the JDK TCP implementation will be used. The alternatives are
 TCPJ, RawTCP or RawTCPJ. (NB The RawTCP protocol will behave identically to TCP
 because no <code>DatagramSocket</code> objects will be instantiated. However, a
 <code>DatagramSocket</code> is always used by TCPJ, so the behaviour of TCPJ
 and RawTCPJ will differ).
 <p><code>-l localhost localport</code> (optional) should be specified if the protocol
 has been set to RawTCPJ. The information will be used to construct the IP header.
 <p><code>hostname port</code> define the remote echo server.
*/

class EchoClient {

  public static void main( String[] args ){

    final String errortext = "Usage: EchoClient -p protocol -l localhost localport hostname port";
    
    if( args.length < 2 ){
    	System.err.println( errortext );
    	System.exit( 1 );
    }
    
    int i    = -1 ,
        port = 0 ,
        localport = 0 ;

    String hostname   = args[ args.length - 2 ],
           protocollabel = "" ,
           localhost = null ;

	try {
	  port = Integer.parseInt( args[ args.length - 1 ]  );
	} catch( NumberFormatException e ){
	  System.err.println("Invalid port number");
	  System.exit( 3 );
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
		  System.exit( 3 );
		}
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
    
	InetAddress dstaddr = null ,
				localaddr = null ;

	try {
	  dstaddr = InetAddress.getByName( hostname );
	  if( localhost instanceof String ){
		localaddr = InetAddress.getByName( localhost );
	  }
	} catch( UnknownHostException e ){
	  System.err.println("Address " + e.getMessage() + " is unknown");
	  System.exit( 4 );
	}

    Socket client = null ;

    try { 
      if( localaddr instanceof InetAddress ){
      	client = new Socket( dstaddr , port , localaddr , localport );   	
      } else {
		client = new Socket( dstaddr , port );
      }
    } catch( UnknownHostException e ){
      System.err.println( e.getMessage() );
      System.exit( 5 );
    } catch( IOException e ){
      System.err.println( e.getMessage() );
      System.exit( 6 );
    }

    System.err.println( client.toString() );
    
    InputStream serverIn = null ;

    OutputStream serverOut = null ;

    try {
      serverIn = client.getInputStream();
    } catch( IOException e ){
      System.err.println( e.getMessage() );
      System.exit( 8 );
    }

    try {
      serverOut = client.getOutputStream();
    } catch( IOException e ){
      System.err.println( e.getMessage() );
      System.exit( 10 );
    }

    new EchoClient( new InputStreamReader( System.in ) , 
                    new OutputStreamWriter( serverOut ),
                    serverIn ,
                    new OutputStreamWriter( System.out ) ); 

    System.exit( 0 );
  }

  public EchoClient( InputStreamReader  localIn , 
                     OutputStreamWriter serverOut ,
                     InputStream        serverIn ,
                     OutputStreamWriter localOut ){

    try {

      BufferedReader localReader  = new BufferedReader( localIn );

      String line ;

      StringBuffer buffer = new StringBuffer();

      int ch ;

      while( ( line = localReader.readLine() ) != null ){

        serverOut.write( line );
        serverOut.write( '\n' );
        serverOut.flush();

        ch = serverIn.read();

        while( ch > -1 ){
          if( ch != '\n' ){
            buffer.append((char) ch );
          } else {
            localOut.write( buffer.toString() );
            localOut.write( '\n' );
            localOut.flush();
          
            buffer = new StringBuffer();

            break;
          }
          ch = serverIn.read();
        }
      }

      localReader.close();
      serverIn.close();

    } catch( Exception e ){
      System.err.println( e.getMessage() );
    }
  }
}


