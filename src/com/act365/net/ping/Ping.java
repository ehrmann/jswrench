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
import com.act365.net.icmp.*;

import java.net.*;
import java.util.*;

/**
 Implements the well-known ping network utility.
*/

public class Ping {

  DatagramSocket socket ;

  int transmitted ;

  boolean isinterrupted ;

  /**
   Creates a Ping object to listen for ICMP messages.
  */

  public Ping( DatagramSocket socket , int transmitted ){
    this.socket = socket ;
    this.transmitted = transmitted ;
    isinterrupted = false ;
  }

  /**
   Interrupts the receive cycle.
  */

  public void interrupt(){
    isinterrupted = true ;
  }

  /**
   Listens for ICMP packets.
  */

  public void receive() {

    try {

      final int maxdatagramlength = 512 ;

      byte[] buffer = new byte[ maxdatagramlength ];

      DatagramPacket packet ;

      ICMPReader reader = new ICMPReader( (short) socket.hashCode() );

      ICMPMessage message = null ;

      int received = 0 ;

      float sumdt = 0 ,
            mindt = Float.MAX_VALUE ,
            maxdt = Float.MIN_VALUE ;

      try {
        socket.setSoTimeout( 2500 );
      } catch ( SocketException e ){
      }

      while( !( transmitted >= 0  && received >= transmitted ) && ! isinterrupted ){
        packet = new DatagramPacket( buffer , maxdatagramlength );
        socket.receive( packet );

        if( ( message = reader.read( packet.getData() , packet.getLength() , 20 , false ) ) != null ){ 

          long t1 , t2 ;
 
          float dt ;

          switch( message.type ) {

            case ICMP.ICMP_ECHOREPLY:

              ++ received ;

              System.out.print( message.data.length + 8 + " bytes from ");
              System.out.print( packet.getAddress() + ": ");
              System.out.print( "icmp_seq=" + message.sequence_number + " " );

              if( message.data.length >= 8 ){

                t1 = SocketUtils.longFromBytes( message.data , 0 );
                t2 = new Date().getTime();

                dt = t2 - t1 ;

                sumdt += dt ;

                if( dt < mindt ){
                  mindt = dt ;
                }

                if( dt > maxdt ){
                  maxdt = dt ;
                }

                System.out.println( "time=" + dt + " ms" );

              } else {
                System.out.println();
              }
              break;

            case ICMP.ICMP_DEST_UNREACH:

              throw new Exception("Host unreachable");
          }
        } else {
          System.err.println("Null message received");
        }
      }

      if( transmitted > 0 ){
        System.out.println("----ping statistics----");
        System.out.print( transmitted + " packets transmitted, " );
        System.out.print( received + " packets received, " );
        System.out.println( 100.0 * ( transmitted - received )/ transmitted + "% packet loss" );

        if( sumdt > 0 && received > 0 ){
          System.out.print("round-trip min/avg/max = ");
          System.out.println( mindt + "/" + sumdt / received + "/" + maxdt + " ms" );
        }
      }

    } catch ( Exception e ) {
      System.err.println( e.getMessage() );
      System.exit( 1 );
    }
  }

  /**
   Executes the Ping service.
   Usage: <code>Ping -c count -s nbytes -p protocol -l localhost -t ttl hostname</code>.
   <p><code>-c count</code> (optional) defines the number of packets to be broadcast.
   By default, the broadcast will continue endlessly. 
   <p><code>-s nbytes</code> (optional) defines the number of bytes to appear
   in each packet. The default is 56.
   <p><code>-p protocol</code> (optional) defines the socket protocol to be used.
   By default, ICMP will be used - the alternative is HdrICMP.
   <p><code>-l localhost</code> (optional) should be specified if the protocol
   has been set to HdrICMP. The information will be used to construct the IP header.
   <p><code>-t ttl</code> (optional) is the time-to-live to be used if the HdrICMP
   protocol has been selected. The default is 64.
   <p><code>hostname</code> define the remote host.
  */

  public static void main( String[] args ){

    final String errortext = "Usage Ping -c count -s nbytes -p protocol -l localhost hostname";
    
    if( args.length == 0 ){
    	System.err.println( errortext );
    	System.exit( 1 );
    }
    
    String hostname = args[ args.length - 1 ] ,
           protocollabel = "ICMP",
           localhost = null ;

    int count = -1 ;

    int i = -1 ,
        nbytes = 56 ;

    short ttl = 64 ;
    
    while( ++ i < args.length - 1 ){

      if( args[ i ].equals("-c") && i < args.length - 2 ){
        try {
          count = Integer.parseInt( args[ ++ i ] );  
        } catch( NumberFormatException e ){
          System.err.println("Invalid packet count");
          System.exit( 3 );
        }
	  } else if( args[ i ].equals("-s") && i < args.length - 2 ){
		try {
		  nbytes = Integer.parseInt( args[ ++ i ] );
		} catch( NumberFormatException e ){
		  System.err.println("Invalid packet size");
		  System.exit( 4 );
		}
	  } else if( args[ i ].equals("-t") && i < args.length - 2 ){
		try {
		  ttl = Short.parseShort( args[ ++ i ] );
		} catch( NumberFormatException e ){
		  System.err.println("Invalid time-to-live");
		  System.exit( 5 );
		}
      } else if( args[ i ].equals("-p") && i < args.length - 2 ){
      	protocollabel = args[ ++ i ];
      	if( ! protocollabel.equalsIgnoreCase("ICMP") &&
      	    ! protocollabel.equalsIgnoreCase("HdrICMP") ){
      	    	System.err.println("Unsupported protocol");
      	    	System.exit( 2 );
      	    }
      } else if( args[ i ].equals("-l") && i < args.length - 2 ){
      	localhost = args[ ++ i ];
      } else {
        System.err.println( errortext );
        System.exit( 1 );
      }
    }

	try {
		SocketUtils.setProtocol( protocollabel );
	} catch ( java.io.IOException e ){
		System.err.println("Protocol not supported");
		System.exit( 2 );
	}
      
    new SocketWrenchSession();
    
    InetAddress hostaddr = null ,
                localaddr = null ;

    try {
      hostaddr = InetAddress.getByName( hostname );
      if( localhost instanceof String ){
      	localaddr = InetAddress.getByName( localhost );
      }
    } catch ( UnknownHostException e ) {
      System.err.println( e.getMessage() );
      System.exit( 6 );
    }

    DatagramSocket socket = null ;

    try {
      socket = new DatagramSocket();
    } catch ( SocketException e ) {
      System.err.println( e.getMessage() );
      System.exit( 7 );
    }

    Ping receiver = new Ping( socket , count );
 
    ( new PingSender( socket , 
                      hostaddr , 
                      localaddr , 
                      (short) socket.hashCode() , 
                      receiver , 
                      count , 
                      nbytes , 
                      ttl ) ).start();

    receiver.receive();
  }
}
