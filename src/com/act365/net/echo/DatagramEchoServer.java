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
 DatagramEchoServer acts as the server for the UDP/IP echo service.
*/

public class DatagramEchoServer {

  public static void main( String[] args ){
    
    int i    = -1 ,
        port = 0 ,
        maxDatagramLength = 512 ;

    while( ++ i < args.length ){
      if( args[ i ].equals("-p") && i < args.length - 1 ){
        try {
          port = Integer.parseInt( args[ ++ i ] );
        } catch( NumberFormatException e ){
          System.err.println("Invalid port number");
          System.exit( 1 );
        }
      } else if( args[ i ].equals("-l") && i < args.length - 1 ){
        try {
          maxDatagramLength = Integer.parseInt( args[ ++ i ] );
        } catch( NumberFormatException e ){
          System.err.println("Invalid datagram length");
          System.exit( 2 );
        }
      } else {
        System.err.println("DatagramEchoServer -p port -l datagramlength");
        System.err.println("Use java -Dimpl.prefix in order to specify socket type e.g. java -Dimpl.prefix=UDP");
        System.exit( 2 );
      }
    }

    new SocketWrenchSession();

    new DatagramEchoServer( maxDatagramLength , port );
  }

  DatagramEchoServer( int maxDatagramLength ,
                      int port ){

    try {

      DatagramSocket server = new DatagramSocket( port , InetAddress.getByName( null ) );
  
      System.err.println("Local port: " + server.getLocalPort() );

      int bytesRead ;

      byte[] buffer = new byte[ maxDatagramLength ];

      DatagramPacket received = null ;

      while( true ){

        received = new DatagramPacket( buffer , maxDatagramLength );

        server.receive( received );

        server.send( new DatagramPacket( received.getData() , 
                                         received.getLength() , 
                                         received.getAddress() ,
                                         received.getPort() ) );
      }

    } catch ( SocketException se ) {
      System.err.println( se.getMessage() );
      System.exit( 4 );
    } catch ( IOException ioe ) {
      System.err.println( ioe.getMessage() );
      System.exit( 5 );
    } catch( Exception e ){
      System.err.println( e.getMessage() );
      System.exit( 6 );
    }
  }
}
