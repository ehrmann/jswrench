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

package com.act365.net.rip ;

import com.act365.net.* ;

import java.io.IOException ;

/**
 * RIPMessage supports the RIP v1 and v2 dynamic routing protocols.
 */

public class RIPMessage implements IServiceMessage {
    
    public final static int REQUEST = 1 ,
                            RESPONSE = 2 ,
                            TRACE_ON = 3 ,
                            TRACE_OFF = 4 ,
                            RESERVED = 5 ,
                            INFINITY = 16 ;
                            
    public final static String[] commandLabels = { "" ,
                                                   "Request" ,
                                                   "Response" ,
                                                   "Trace On (obsolete)" ,
                                                   "Trace Off (obsolete)" ,
                                                   "Reserved" };
                                                   
    // Members
    
    byte command ;
    byte version ;
    short routingDomain ;
    Route[] routes ;
    
    /**
     * Creates a default RIPMessage ready to be populated by the read() method.
     */
    
    public RIPMessage(){
    }
    
    /**
     * Creates a RIP request message to ask for the entire router table.
     */
    
    public RIPMessage( int version ){
        command = REQUEST ;
        this.version = (byte) version ;
        if( version > 1 ){
            routingDomain = (short) hashCode();
        }
        routes = new Route[1];
        routes[0] = new Route(); 
    }
    
    /**
     * Returns the service name.
     */
    
    public String getServiceName() {
        return "RIP v" + version ;
    }
    
    /**
     * Returns 520, the well-known port number associated with the service.
     */
    
    public int getWellKnownPort() {
        return 520 ;
    }
    
    /**
     * Calculates the message length in bytes.
     */
    
    public int length(){
        return 4 + 20 * routes.length ;
    }
    
    /**
     * Writes a textual description of the message. 
     */
    
    public void dump( java.io.PrintStream printer ){
        printer.print( commandLabels[ command ] );
        if( routingDomain != 0 ){
            printer.print(" : Routing domain " + routingDomain );
        }
        printer.println();
        int r = 0 ;
        while( r < routes.length ){
            printer.println( routes[r++].toString() );
        }
    }
    
    /**
     * Reads a RIP message from a bytestream.
     */ 
    
    public int read( byte[] buffer , int offset , int count ) throws IOException {
        int length ;
        command = buffer[offset++];
        version = buffer[offset++];
        if( version > 1 ){
            routingDomain = SocketUtils.shortFromBytes( buffer , offset );
        } else {
            routingDomain = 0 ;
        }
        offset += 2 ;
        count -= 4 ;
        length = 4 ;
        final int nRoutes = count / Route.length() ;
        routes = new Route[nRoutes];
        int r = 0 ;
        while( r < nRoutes ){
            routes[r] = new Route();
            length = routes[r++].read( buffer , offset , count );
            offset += length ;
            count -= length ;
        }
        
        if( count != 0 ){
            throw new IOException("RIP message format error");
        }
        
        return length ;
    }

    /**
     * Writes the RIP message into a bytestream at the given position.
     */
    
    public int write( byte[] buffer , int offset ) throws IOException {
        
        final int length = length();
        
        buffer[offset++] = command ;
        buffer[offset++] = version ;

        if( version > 1 ){
            SocketUtils.shortToBytes( routingDomain , buffer , offset );
            offset += 2 ;
        } else {
            buffer[offset++] = 0 ;
            buffer[offset++] = 0 ;
        }
        
        int r = 0 ,
            cursor = offset ;
        
        while( r < routes.length ){
            cursor += routes[r++].write( buffer , cursor );
        }
        
        return length ;
    }
}
