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
 DatagramEchoClient acts as the client for the UDP echo service.
 Usage: <code>DatagramEchoClient -p protocol -d datagramlength -i inputfile -o outputfile -l localhost localport hostname hostport</code>.
 <p><code>-p protocol</code> (optional) defines the socket protocol to be used.
 By default, the JDK UDP implementation will be used. The alternatives are
 UDP, RawUDP or SystemUDP.
 <p><code>-d datagramlength</code> (optional) defines the maximum lenght of
 datagram packet that will be supported by the application. The default is 512
 bytes.
 <p><code>-i inputfile</code> (optional) defines the file from which input will
 be read. By default, standard input will be used.
 <p><code>-o outfile</code> (optional) defines the file to which all output will
 be written. By default, standard output will be used.
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
	
    final String errortext = "DatagramEchoClient -p protocol -d datagramlength -i inputfile -o outputfile -l localhost localport hostname hostport";
    
    if( args.length < 2 ){
    	System.err.println( errortext );
    	System.exit( 1 );
    }
    
    String hostname   = args[ args.length - 2 ],
           localhost  = null ,
           protocollabel = "",
           inputFile  = null ,
           outputFile = null ;

	int i    = -1 ,
		port = 0 ,
		localport = 0 ,
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
		if( ! protocollabel.equalsIgnoreCase("UDP") &&
			! protocollabel.equalsIgnoreCase("RawUDP") &&
			! protocollabel.equalsIgnoreCase("SystemUDP") ){
		  System.err.println("Unsupported protocol");
		  System.exit( 3 );
		}
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
      } else if( args[ i ].equals("-i") && i < args.length - 3 ){
        inputFile = args[ ++ i ];
      } else if( args[ i ].equals("-o") && i < args.length - 3 ){
        outputFile = args[ ++ i ];
      } else {
        System.err.println( errortext );
        System.exit( 1 );
      }
    }

	try {
	  SocketUtils.setProtocol( protocollabel );
	} catch ( java.io.IOException e ) {
	  System.err.println("Unsupported protocol");
	  System.exit( 3 );
	}
    
	final int protocol = SocketUtils.getProtocol();
    
	boolean includeheader = SocketUtils.includeHeader();
    
	new SocketWrenchSession();

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

    DatagramSocket socket = null ;

    try {
    	if( localaddr instanceof InetAddress ){
			socket = new DatagramSocket( localport , localaddr );
    	} else {
    		socket = new DatagramSocket( localport );
    	}
    } catch( SocketException se ){
      System.err.println( se.getMessage() );
      System.exit( 7 );
    }

	System.err.println("Local address: " + socket.getLocalAddress() );
	System.err.println("Local port: " + socket.getLocalPort() );

    InputStream localIn  = null ;

    OutputStream localOut  = null ;

    if( inputFile instanceof String ){
      try {
        localIn = new FileInputStream( inputFile );
      } catch ( FileNotFoundException e ) {
        System.err.println( e.getMessage() );
        System.exit( 8 );
      }
    } else {
      localIn = System.in ;
    }

    if( outputFile instanceof String ){
      try {
        localOut = new FileOutputStream( outputFile );
      } catch ( IOException e ) {
        System.err.println( e.getMessage() );
        System.exit( 9 );
      }
    } else {
      localOut = System.out ;
    }

    new DatagramEchoClient( socket , maxDatagramLength , dstaddr , port , localIn , localOut ); 

    System.exit( 0 );
  }

  public DatagramEchoClient( DatagramSocket socket ,
                             int            maxDatagramLength , 
                             InetAddress    dest ,
                             int            port ,
                             InputStream    localIn , 
                             OutputStream   localOut ){
    try {

      int bytesRead ;

      byte[] buffer = new byte[ maxDatagramLength ];

      while( ( bytesRead = localIn.read( buffer ) ) > -1 ){

        socket.send( new DatagramPacket( buffer , bytesRead , dest , port ) );

        DatagramPacket received = new DatagramPacket( buffer , maxDatagramLength );

        socket.receive( received );
        
        localOut.write( received.getData() , 0 , received.getLength() );
      }

    } catch( IOException e ){
      System.err.println( e.getMessage() );
    }
  }
}


