/*
  * JSocket Wrench
  * 
  * Copyright (C) act365.com March 2005
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

package com.act365.net.rip;

import com.act365.net.* ;
import com.act365.net.ip.* ;

import java.io.IOException ;
import java.net.* ;

/**
 * RIPQuery asks routers for their routing tables.
 */

public class RIPQuery {

  
    /**
     * The RIPQuery object sends a RIP request for the entire routing table from
     * a router.
     */

    public RIPQuery( InetAddress router ,
                     InetAddress source ) throws SocketException , IOException {
    
        new SocketWrenchSession();
  
        JSWDatagramSocket socket = new JSWDatagramSocket( 520 );

        if( source instanceof InetAddress ){
            socket.setSourceAddress( source.getAddress() );
        }
        socket.setTypeOfService( IP4.TOS_COMMAND );
        socket.setDebug( System.err );
    
        RIPMessage ripMessage = new RIPMessage( 1 );

        socket.send( ripMessage , 520 , router );            
        socket.receive( null , ripMessage );

        ripMessage.dump( System.out );
    }

    /**
     * Performs RIP router queries.
     */
    
	public static void main( String[] args ) throws Exception {

        final String errortext = "Usage: RIPQuery -p protocol -l localhost router";

        if( args.length < 1 ){
            throw new Exception( errortext );
        }
    
        String routername = args[ args.length - 1 ] ,
               protocollabel = "JDKUDP",
               localhost = null ;

        int i = -1 ;

        while( ++ i < args.length - 1 ){
            if( args[ i ].equals("-p") && i < args.length - 2 ){
                protocollabel = args[ ++ i ];
            } else if( args[ i ].equals("-l") && i < args.length - 2 ){
                localhost = args[ ++ i ];
            } else {
                throw new Exception( errortext );
            }
        }

        try {
            SocketWrenchSession.setProtocol( protocollabel );
        } catch ( java.io.IOException e ) {
            throw new Exception("Unsupported protocol");
        }

        if( SocketWrenchSession.getProtocol() != SocketConstants.IPPROTO_UDP ){
            throw new Exception("A UDP protocol should be selected");
        }
    
        InetAddress router = null ,
                    source = null ;

        try {
          router = InetAddress.getByName( routername );
          if( localhost instanceof String ){
            source = InetAddress.getByName( localhost );
          }
        } catch( UnknownHostException e ){
          throw new Exception("Router " + e.getMessage() + " is unknown");
        }

        new RIPQuery( router , source );    
    
        SocketWrenchSession.shutdown();
	}
}
