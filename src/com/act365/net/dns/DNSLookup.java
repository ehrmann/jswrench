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

package com.act365.net.dns ;

import com.act365.net.* ;
import com.act365.net.ip.* ;

import java.io.IOException;
import java.net.*;

/**
 Implements a DNS client. 
 Usage: <code>DNSLookup -r -p protocol -l localhost DNSServer domain</code>.
 <code>-r</code> (optional) indicates whether recursion, which is enabled by
 default, should be disabled, i.e. whether a query that cannot be resolved by 
 the named server should not be passed on to another server.
 <p><code>-p protocol</code> (optional) defines the socket protocol to be used.
 By default, the JDK UDP implementation will be used. The alternatives are
 UDP, RawUDP and RawHdrUDP. 
 <p><code>-l localhost</code> (optional) should be specified if the protocol
 has been set to RawHdrUDP. The information will be used to construct the IP header.
 <p><code>DNSServer</code> is the IP address of the DNS server.
 <p><code>domain</code> is the domain name to be resolved.
*/

public class DNSLookup {

  /**
   Sends a DNS query to a DNS server.
  */

  public static void main( String[] args ) {

    final String errortext = "Usage: DNSLookup -r -p protocol -l localhost -t type DNSServer domain";

    if( args.length < 2 ){
    	System.err.println( errortext );
    	System.exit( 1 );
    }
    
    int i = -1 ;

    boolean recursion_desired = true ;

    String servername = args[ args.length - 2 ] ,
           domainname = args[ args.length - 1 ] ,
           protocollabel = "JDKUDP",
           localhost = null ,
           type = null ;

    while( ++ i < args.length - 2 ){
      if( args[ i ].equals("-r") ){
        recursion_desired = false ;
      } else if( args[ i ].equals("-p") && i < args.length - 3 ){
      	protocollabel = args[ ++ i ];
      } else if( args[ i ].equals("-l") && i < args.length - 3 ){
      	localhost = args[ ++ i ];
      } else if( args[ i ].equals("-t") && i < args.length - 3 ){
          type = args[ ++ i ];
      } else {
        System.err.println( errortext );
        System.exit( 1 );
      }
    }

    try {
      SocketWrenchSession.setProtocol( protocollabel );
    } catch ( java.io.IOException e ) {
      System.err.println("Unsupported protocol");
      System.exit( 2 );
    }

    if( SocketWrenchSession.getProtocol() != SocketConstants.IPPROTO_UDP ){
        System.err.println("A UDP protocol should be selected");
        System.exit( 3 );    
    }
    
    int dnstype ;
    
    if( type instanceof String ){
        dnstype = -1 ;
        while( ! type.equalsIgnoreCase( DNSMessage.dnsTypes[++ dnstype ] ) );
        if( dnstype == DNSMessage.dnsTypes.length ){
            System.err.println("Unknown DNS lookup type");
            System.exit( 4 );
        }
    } else {
        // Determine whether the domain name is a host name or an IP address.
        dnstype = DNSMessage.A ;
    }
    
    InetAddress server = null ,
                source = null ;

    try {
      server = InetAddress.getByName( servername );
      if( localhost instanceof String ){
        source = InetAddress.getByName( localhost );
      }
    } catch( UnknownHostException e ){
      System.err.println("DNS server " + e.getMessage() + " is unknown");
      System.exit( 5 );
    }

    try {
        new DNSLookup( domainname , server , source , dnstype , recursion_desired );    
    } catch ( Exception e ) {
        System.err.println( e.getMessage() );
        e.printStackTrace();
        System.exit( 6 );
    }
  }
  
  public DNSLookup( String domainname ,
                    InetAddress server ,
                    InetAddress source ,
                    int dnstype ,
                    boolean recursion_desired ) throws SocketException , IOException {
    
      new SocketWrenchSession();
  
      JSWDatagramSocket socket = new JSWDatagramSocket( 1024 );

      if( source instanceof InetAddress ){
          socket.setSourceAddress( source.getAddress() );
      }
      socket.setTypeOfService( IP4.TOS_COMMAND );
    
      DNSMessage dnsMessage = new DNSMessage( (short) hashCode() , dnstype , recursion_desired , domainname );

      socket.send( dnsMessage , 1024 , server );            
      socket.receive( null , dnsMessage );

      dnsMessage.dump( System.out );
  }
}

