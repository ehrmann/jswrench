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
 DatagramEchoClient acts as the client for the UDP/IP echo service.
*/

class DatagramEchoClient {

  public static void main( String[] args ){

    int i    = -1 ,
        port = 0 ,
        localport = 0 ,
        maxDatagramLength = 512 ;

    String hostname   = "localhost",
           inputFile  = null ,
           outputFile = null ;

    while( ++ i < args.length ){
      if( args[ i ].equals("-p") && i < args.length - 1 ){
        try {
          port = Integer.parseInt( args[ ++ i ]  );
        } catch( NumberFormatException e ){
          System.err.println("Invalid port number");
          System.exit( 1 );
        }
      } else if( args[ i ].equals("-lp") && i < args.length - 1 ){
        try {
          localport = Integer.parseInt( args[ ++ i ]  );
        } catch( NumberFormatException e ){
          System.err.println("Invalid localport number");
          System.exit( 2 );
        }
      } else if( args[ i ].equals("-l") && i < args.length - 1 ){
        try {
          maxDatagramLength = Integer.parseInt( args[ ++ i ]  );
        } catch( NumberFormatException e ){
          System.err.println("Invalid datagram length");
          System.exit( 3 );
        }
      } else if( args[ i ].equals("-i") && i < args.length - 1 ){
        inputFile = args[ ++ i ];
      } else if( args[ i ].equals("-o") && i < args.length - 1 ){
        outputFile = args[ ++ i ];
      } else if( args[ i ].equals("-h") && i < args.length - 1 ){
        hostname = args[ ++ i ];
      } else {
        System.err.println("DatagramEchoClient -p port -lp localport -l datagramlength -i inputfile -o outputfile -h hostname");
        System.err.println("Use java -Dimpl.prefix in order to specify socket type e.g. java -Dimpl.prefix=UDP"); 
        System.exit( 4 );
      }
    }

    InetAddress dest = null ;

    try {
      dest = InetAddress.getByName( hostname );
    } catch ( UnknownHostException e ) {
      System.err.println( e.getMessage() );
      System.exit( 5 );
    }

    new SocketWrenchSession();

    DatagramSocket socket = null ;

    try {
      socket = new DatagramSocket( localport , InetAddress.getByName( null ) );
    } catch( SocketException se ){
      System.err.println( se.getMessage() );
      System.exit( 6 );
    } catch( UnknownHostException uhe ){
      System.err.println( uhe.getMessage() );
      System.exit( 7 );
    }

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

    new DatagramEchoClient( socket , maxDatagramLength , dest , port , localIn , localOut ); 

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


