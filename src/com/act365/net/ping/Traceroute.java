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
import com.act365.net.ip.*;
import com.act365.net.udp.*;

import java.io.InterruptedIOException;
import java.net.*;
import java.util.*;

/**
 Implements the well-known Traceroute network utility. The app supports
 the broadcast of UDP or ICMP ECHO_REQUEST packets.
*/

public class Traceroute {
   
  /**
   Executes the Traceroute service.
   <p>Usage: <code>Traceroute -p protocol -l localhost -d -f first_ttl host</code>
   <p><code>protocol</code> is the protocol to be used for broadcast. The default 
   is HdrICMP - the alternative is HdrUDP.
   <p><code>localhost</code> is the local host address, which should be specified
   if HdrUDP is used. (The information is used to calculate the UDP checksum).
   <p><code>-d</code> should be specified if debug is required.
   <p><code>-f first_ttl</code> is the TTL value used for the first packet.
   (The default value is 1). 
   <p><code>host</code> is the remote hostname and is mandatory.
  */

  public static void main( String[] args ){

    final String errortext = "Usage: Traceroute -p protocol -l localhost -d -f first_ttl host";
    
    if( args.length == 0 ){
    	System.err.println( errortext );
    	System.exit( 1 );
    }
    
    String hostname = args[ args.length - 1 ] ,
           localhost = null ;

    String protocollabel = null ;

	short ttl = 1 ;
   
    boolean debug = false ;

    int i = 0 ;

    while( i < args.length - 1 ){
      if( args[ i ].equals("-p") && i < args.length - 2 ){
        protocollabel = args[ ++ i ];
        if( ! protocollabel.equalsIgnoreCase("HdrICMP") && 
            ! protocollabel.equalsIgnoreCase("RawHdrUDP") ){
          System.err.println("Unsupported protocol");
          System.exit( 2 );
        }
      } else if( args[ i ].equals("-l") && i < args.length - 2 ){
        localhost = args[ ++ i ]; 
      } else if( args[ i ].equals("-d") ){
        debug = true ;
      } else if( args[ i ].equals("-f") ){
		try {
		  ttl = Short.parseShort( args[ ++ i ] );  
		} catch( NumberFormatException e ){
		  System.err.println("Invalid TTL value");
		  System.exit( 3 );
		}
      } else {
        System.err.println( errortext );
        System.exit( 1 );
      }
      ++ i ;
    }

    try {
		SocketWrenchSession.setProtocol("HdrICMP");
    } catch ( java.io.IOException e ) {
    	System.err.println("Unsupported protocol");
    	System.exit( 4 );
    }

    int protocol = protocollabel instanceof String && protocollabel.equalsIgnoreCase("RawHdrUDP") 
                               ? SocketConstants.IPPROTO_UDP : SocketConstants.IPPROTO_ICMP ;
    
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
      System.exit( 5 );
    }

    if( protocol == SocketConstants.IPPROTO_UDP && localaddr == null ){
      System.err.println("localhost must be defined if RawHdrUDP is to be used");
      System.exit( 6 );
    }

    DatagramSocket socket = null ;

    try {
      socket = new DatagramSocket();
    } catch ( SocketException e ) {
      System.err.println( e.getMessage() );
      System.exit( 7 );
    }

    try {

      final int maxdatagramlength = 512 ;

      byte[] recvbuffer = new byte[ maxdatagramlength ] ,
             timebuffer = new byte[ 8 ] ,
             messagebuffer ,
             sendbuffer ;

      DatagramPacket packet ;

      ICMPWriter writer = new ICMPWriter( (short) socket.hashCode() );

      ICMPMessage message = null ;

      float sumdt = 0 ,
            mindt = Float.MAX_VALUE ,
            maxdt = Float.MIN_VALUE ;

      int sourceport = 42000 ,
          destinationport = 64000 ;

      socket.setSoTimeout( 3000 );
      
      while( message == null || message.type != ICMP.ICMP_ECHOREPLY && message.code != ICMP.ICMP_PORT_UNREACH ){

        SocketUtils.longToBytes( new Date().getTime() , timebuffer , 0 );

        switch( protocol ){
    
        case SocketConstants.IPPROTO_ICMP:
          messagebuffer = writer.write( ICMP.ICMP_ECHO , (byte) 0 , timebuffer );
          break;

        case SocketConstants.IPPROTO_UDP:
          messagebuffer = UDPWriter.write( localaddr.getAddress() , 
                                           (short) sourceport ++ , 
                                           hostaddr.getAddress() , 
                                           (short) destinationport ++ ,
                                           timebuffer ,
                                           0 ,
                                           timebuffer.length );
          break;

        default:
          messagebuffer = new byte[0];
        }


        sendbuffer = IP4Writer.write( IP4.TOS_ICMP , 
                                      ttl , 
                                      (byte) protocol , 
                                      localaddr != null ? localaddr.getAddress() : new byte[ 4 ] , 
                                      hostaddr.getAddress() , 
                                      messagebuffer );

        if( debug ){
          System.err.println("SEND:");
          SocketUtils.dump( System.err , sendbuffer , 0 , sendbuffer.length );
        }

        socket.send( new DatagramPacket( sendbuffer , sendbuffer.length , hostaddr , 0 ) );

        packet = new DatagramPacket( recvbuffer , maxdatagramlength );

        try {
          socket.receive( packet );
        } catch( InterruptedIOException e ){
          message = null ;
          System.out.println( ttl ++ + ". *.*.*.*/*.*.*.*");            
          continue ;
        }
        
        if( ( message = ICMPReader.read( packet.getData() , 20 , packet.getLength() - 20 , false ) ) != null ){ 

          if( message.isQuery() && ( message.identifier != socket.hashCode() ) ){
              continue ;
          }
          
          if( debug ){
            System.err.println("RECEIVE:");
            System.err.println( message.toString() );
            if( message.ip4Message != null ){
                System.err.println( message.ip4Message );
            }
            SocketUtils.dump( System.err , packet.getData() , 0 , packet.getLength() );
          }  

          System.out.println( ttl ++ + ". " + packet.getAddress() );
          
          if( message.type != ICMP.ICMP_TIME_EXCEEDED &&
              message.type != ICMP.ICMP_ECHOREPLY && 
              message.code != ICMP.ICMP_PORT_UNREACH ){
            System.out.println( message.toString() );
          } 
        }
      }

    } catch ( Exception e ) {
      System.err.println( e.getMessage() );
      System.exit( 8 );
    }
  }
}

