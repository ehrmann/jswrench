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
 * A Route object represents a route advertised in a RIP message.
 */

public class Route {

    short addressFamily ;
    short routeTag ;
    byte[] ipAddress ;
    byte[] subnetMask ;
    byte[] nextHop ;
    int metric ;
    String password ;
    
    /**
     * Creates an empty Route ready to be populated by the read() method.
     */
    
    public Route() {
        metric = RIPMessage.INFINITY ;
        ipAddress = new byte[] { 0 , 0 , 0 , 0 };
    }
    
    /**
     * Creates a RIP v1 route.
     */

    public Route( short addressFamily ,
                  byte[] ipAddress ,
                  int metric ){
        this.addressFamily = addressFamily ;
        this.metric = metric ;

        this.ipAddress = new byte[4];
        this.ipAddress[0] = ipAddress[0];
        this.ipAddress[1] = ipAddress[1];
        this.ipAddress[2] = ipAddress[2];
        this.ipAddress[3] = ipAddress[3];
    }
    
    /**
     * Creates a RIP v2 route.
     */
    
    public Route( short addressFamily ,
                  short routeTag ,
                  byte[] ipAddress ,
                  byte[] subnetMask ,
                  byte[] nextHop ,
                  int metric ){
        this.addressFamily = addressFamily ;
        this.routeTag = routeTag ;
        this.metric = metric ;

        this.ipAddress = new byte[4];
        this.ipAddress[0] = ipAddress[0];
        this.ipAddress[1] = ipAddress[1];
        this.ipAddress[2] = ipAddress[2];
        this.ipAddress[3] = ipAddress[3];

        this.subnetMask = new byte[4];
        this.subnetMask[0] = subnetMask[0];
        this.subnetMask[1] = subnetMask[1];
        this.subnetMask[2] = subnetMask[2];
        this.subnetMask[3] = subnetMask[3];

        this.nextHop = new byte[4];
        this.nextHop[0] = nextHop[0];
        this.nextHop[1] = nextHop[1];
        this.nextHop[2] = nextHop[2];
        this.nextHop[3] = nextHop[3];
    }
    
    /**
     * Creates a RIP v2 password route.
     */
    
    public Route( String password ){
        addressFamily = -1 ;
        routeTag = 2 ;
        this.password = password ;
    }
    
    /**
     * A RIP route occupies 20 bytes.
     */

    public static int length(){
        return 20 ;
    }
    
    /**
     * Reads a RIP route from a bytestream.
     */

    public int read( byte[] buffer , int offset , int count ) throws IOException {
        
        int length = 0 ;
        
        if( count >= 20 ){
            addressFamily = SocketUtils.shortFromBytes( buffer , offset );
            offset += 2 ;
            routeTag = SocketUtils.shortFromBytes( buffer , offset );
            offset += 2 ;
            if( addressFamily == -1 && routeTag == 2 ){
                password = new String( buffer , offset , 16 );
                offset += 16 ;
            } else {
                ipAddress[0] = buffer[offset++];
                ipAddress[1] = buffer[offset++];
                ipAddress[2] = buffer[offset++];
                ipAddress[3] = buffer[offset++];
                if( routeTag != 0 ){
                    
                    subnetMask[0] = buffer[offset++];
                    subnetMask[1] = buffer[offset++];
                    subnetMask[2] = buffer[offset++];
                    subnetMask[3] = buffer[offset++];
                    
                    nextHop[0] = buffer[offset++];
                    nextHop[1] = buffer[offset++];
                    nextHop[2] = buffer[offset++];
                    nextHop[3] = buffer[offset++];
                    
                } else {
                    offset += 8 ;
                }
                metric = SocketUtils.intFromBytes( buffer , offset );
                offset += 4 ;
            }
            count -= 20 ;
            length += 20 ;
        }
        
        if( count != 0 ){
            throw new IOException("RIP message format error");
        }
        
        return length ;
    }

    /**
     * Writes a RIP record into a bytestream.
     */

    public int write( byte[] buffer , int offset ) throws IOException {
        
        SocketUtils.shortToBytes( addressFamily , buffer , offset );
        SocketUtils.shortToBytes( routeTag , buffer , offset + 2 );
        
        if( addressFamily == -1 && routeTag == 2 ){
            SocketUtils.stringToBytes( password , buffer , offset + 4 );
        } else {
            buffer[offset+4] = ipAddress[0]; 
            buffer[offset+5] = ipAddress[1]; 
            buffer[offset+6] = ipAddress[2]; 
            buffer[offset+7] = ipAddress[3]; 
            
            buffer[offset+8] = routeTag != 0 ? subnetMask[0] : 0 ;
            buffer[offset+9] = routeTag != 0 ? subnetMask[1] : 0 ;
            buffer[offset+10] = routeTag != 0 ? subnetMask[2] : 0 ;
            buffer[offset+11] = routeTag != 0 ? subnetMask[3] : 0 ;
            
            buffer[offset+12] = routeTag != 0 ? nextHop[0] : 0 ;
            buffer[offset+13] = routeTag != 0 ? nextHop[1] : 0 ;
            buffer[offset+14] = routeTag != 0 ? nextHop[2] : 0 ;
            buffer[offset+15] = routeTag != 0 ? nextHop[3] : 0 ;
            
            SocketUtils.intToBytes( metric , buffer , offset + 16 );
        }
        
        return length();
    }
        
    /**
     * Produces a text representation of a RIP route.
     */
    
    public String toString() {
        
        StringBuffer sb = new StringBuffer();
        
        if( addressFamily == -1 && routeTag == 2 ){
            sb.append("Password: ");
            sb.append( password );
        } else {
            if( routeTag != 0 ){
                sb.append("Route Tag: ");
                sb.append( routeTag >= 0 ? routeTag : 0xffff0000 ^ routeTag );
                sb.append('\n');
            }
            
            sb.append( ipAddress[0] >= 0 ? ipAddress[0] : 0xffffff00 ^ ipAddress[0] );
            sb.append('.');
            sb.append( ipAddress[1] >= 0 ? ipAddress[1] : 0xffffff00 ^ ipAddress[1] );
            sb.append('.');
            sb.append( ipAddress[2] >= 0 ? ipAddress[2] : 0xffffff00 ^ ipAddress[2] );
            sb.append('.');
            sb.append( ipAddress[3] >= 0 ? ipAddress[3] : 0xffffff00 ^ ipAddress[3] );
            
            if( routeTag != 0 ){
                sb.append(" Subnet mask: ");
                sb.append( subnetMask[0] >= 0 ? subnetMask[0] : 0xffffff00 ^ subnetMask[0] );
                sb.append('.');
                sb.append( subnetMask[1] >= 0 ? subnetMask[1] : 0xffffff00 ^ subnetMask[1] );
                sb.append('.');
                sb.append( subnetMask[2] >= 0 ? subnetMask[2] : 0xffffff00 ^ subnetMask[2] );
                sb.append('.');
                sb.append( subnetMask[3] >= 0 ? subnetMask[3] : 0xffffff00 ^ subnetMask[3] );

                sb.append(" Next-hop: ");
                sb.append( nextHop[0] >= 0 ? nextHop[0] : 0xffffff00 ^ nextHop[0] );
                sb.append('.');
                sb.append( nextHop[1] >= 0 ? nextHop[1] : 0xffffff00 ^ nextHop[1] );
                sb.append('.');
                sb.append( nextHop[2] >= 0 ? nextHop[2] : 0xffffff00 ^ nextHop[2] );
                sb.append('.');
                sb.append( nextHop[3] >= 0 ? nextHop[3] : 0xffffff00 ^ nextHop[3] );
            }
            
            sb.append(" Metric: ");
            sb.append( metric );
        }
        sb.append('\n');
        
        return sb.toString();
    }
}
