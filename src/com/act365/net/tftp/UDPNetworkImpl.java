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

package com.act365.net.tftp;

import java.io.* ;
import java.net.* ;

/**
 * UDPNetworkImpl implements standard client-side TFTP network functions with UDP/IP.
 */

public class UDPNetworkImpl extends UDPNetworkBase implements INetworkImpl {

    /**
     * Creates a UDPNetworkImpl with optional debug.
     * 
     * @param debug - where debug is to be written (null for no debug)
     */    
    
    public UDPNetworkImpl( OutputStream debug ){
        super( debug ); 
    }
    
    /**
     * Opens a connection to a given port on a remote host.
     *       
     */
    
	public void open( String hostname , int port ) throws TFTPException {

        destPort = port > 0 ? port : TFTPConstants.defaultPort ;
        
        try {
            destAddress = InetAddress.getByName( hostname );
        } catch( UnknownHostException e ) {
            system("Unknown host " + hostname );
        }
        
        try {
            socket = new DatagramSocket();
        } catch( SocketException e ){
            system("Cannot create socket");
        }
        
        debug("open: " + toString() );
        
        receiveFirst = true ;
	}

    /**
     * Receives data.
     */
    
	public int receive(byte[] buffer) throws TFTPException , InterruptedIOException {
        
        DatagramPacket packet = new DatagramPacket( buffer , buffer.length );   
     
        try {   
            socket.receive( packet );
        } catch ( InterruptedIOException i ) {
            throw i ;
        } catch ( IOException e ) {
            system("Receive error");
        }
        
        debug("receive: " + packet.getLength() + 
              " bytes from host " + packet.getAddress().toString() + 
              ", port# " + packet.getPort() );
        
        /*
         * The TFTP client using UDP/IP has a funny requirement due to
         * the use of UDP for a "connection-oriented" protocol. The server
         * will change its port after it has received the first packet from
         * a client rather than tie up the well-known port number.
         * 
         * See Section 4 of the RFC.  
         */
         
        if( receiveFirst ){
            
            if( packet.getPort() == destPort ){
                dump("first receive from port#" + packet.getPort() ); 
            } 
            
            destPort     = packet.getPort();
            receiveFirst = false ;
            
        } else if( packet.getPort() != destPort ){
            
            dump("received from port# " + packet.getPort() + ", expected from port# " + destPort );
        }
        
		return packet.getLength() ;
	}
}
