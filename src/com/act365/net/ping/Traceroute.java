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
import com.act365.net.udp.* ;

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
   is ICMP - the alternative is UDP.
   <p><code>localhost</code> is the local host address, which should be specified
   (though it isn't compulsory) to ensure the correct IP header is formed.
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

    int protocol = SocketConstants.IPPROTO_ICMP ;
    
    if( protocollabel instanceof String ){    
        if( protocollabel.equalsIgnoreCase("ICMP") ){
            protocol = SocketConstants.IPPROTO_ICMP ;
        } else if( protocollabel.equalsIgnoreCase("UDP") ){
            protocol = SocketConstants.IPPROTO_UDP ;
        } else {    
            System.err.println("Unsupported protocol");
            System.exit( 2 );
        }
    }
    
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

    new Traceroute( protocol , hostaddr , localaddr , ttl , debug );
  }
  
  public Traceroute( int protocol ,
                     InetAddress hostaddr ,
                     InetAddress localaddr ,
                     int ttl ,
                     boolean debug )
  {
    new SocketWrenchSession();
    
    try {
		SocketWrenchSession.setProtocol( SocketConstants.JSWPROTO_HDRICMP );
    } catch ( java.io.IOException e ) {
    	System.err.println("Unsupported protocol");
    	System.exit( 4 );
    }

    JSWDatagramSocket socket = null ;

    try {
      socket = new JSWDatagramSocket();
      socket.setTypeOfService( IP4.TOS_ICMP );
      if( localaddr instanceof InetAddress ){
          socket.setSourceAddress( localaddr.getAddress() );
      }
      if( debug ){
          socket.setDebug( System.err );
      }
    } catch ( SocketException e ) {
      System.err.println( e.getMessage() );
      System.exit( 7 );
    }

    final short identifier = ICMPMessage.icmpIdentifier = (short) hashCode();

    try {

      byte[] timebuffer = new byte[ 8 ];

      IP4Message ip4Message = new IP4Message();
      
      short recIdentifier = 0 ;
      
      ICMPMessage icmpMessage = new ICMPMessage();
      
      IProtocolMessage message = null ;
      
      float sumdt = 0 ,
            mindt = Float.MAX_VALUE ,
            maxdt = Float.MIN_VALUE ;

      int sourceport = 42000 ,
          destinationport = 64000 ;

      socket.setSoTimeout( 3000 );
      
      while( protocol == SocketConstants.IPPROTO_ICMP && icmpMessage.isQuery() && identifier != recIdentifier || 
             icmpMessage.type != ICMP.ICMP_ECHOREPLY && icmpMessage.code != ICMP.ICMP_PORT_UNREACH ){

        socket.setTimeToLive( ttl );

        SocketUtils.longToBytes( new Date().getTime() , timebuffer , 0 );

        switch( protocol ){
    
        case SocketConstants.IPPROTO_ICMP:
                  
            message = new ICMPMessage( ICMP.ICMP_ECHO , 
                                       (byte) 0 , 
                                       timebuffer , 
                                       0 , 
                                       timebuffer.length );
          
            break;

        case SocketConstants.IPPROTO_UDP:
        
            message = new UDPMessage( (short) sourceport , 
                                      (short) destinationport ++ ,
                                      timebuffer ,
                                      0 ,
                                      timebuffer.length ); 
        
            socket.setSourcePort( sourceport ++ );
          
            break;
        }
                                         
        socket.send( message , hostaddr );

        while(true){
        
            try {
                socket.receive( ip4Message , icmpMessage );
                recIdentifier = icmpMessage.identifier ;           
            } catch( InterruptedIOException e ){
                recIdentifier = 0 ;
                System.out.println( ttl + ". *.*.*.*/*.*.*.*");
                break;            
            }          
        
            if( protocol == SocketConstants.IPPROTO_ICMP && icmpMessage.isQuery() && ( recIdentifier != identifier ) ){
                continue ;
            }

            if( icmpMessage.type != ICMP.ICMP_TIME_EXCEEDED &&
                icmpMessage.type != ICMP.ICMP_ECHOREPLY && 
                icmpMessage.code != ICMP.ICMP_PORT_UNREACH ){
                continue ;
            } 
                
            System.out.println( ttl + ". " + GeneralSocketImpl.createInetAddress( SocketConstants.AF_INET , ip4Message.source ) );
            break;        
        }
        
        ++ ttl ;
      }

    } catch ( Exception e ) {
      System.err.println( e.getMessage() );
      System.exit( 8 );
    }
  }
}

