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

import java.net.* ;
import java.io.IOException ;

/**
 * TCPNetworkImpl implements standard client-side TFTP network functions with TCP/IP.
 */

public class TCPNetworkImpl extends TCPNetworkBase implements INetworkImpl {

    Socket socket = null ;
  
    /**
     * Creates an connected TCPNetworkImpl with optional debug.
     * 
     * @param socket - socket that connects to the server
     * @param trace - whether debug is required
     */    
    
    public TCPNetworkImpl( Socket socket , boolean trace ) throws TFTPException {
    
        super( trace );
        
        try {
            this.socket = socket ;
            this.input = socket.getInputStream();
            this.output = socket.getOutputStream();
            this.destAddress = socket.getInetAddress();
            this.destPort = socket.getPort();
        } catch ( IOException e ) {
            system("Problem with socket");
        }
    }
    
    /**
     * Creates an unconnected TCPNetworkImpl with optional debug.
     * 
     * @param trace - whether debug is required
     */    
    
    public TCPNetworkImpl( boolean trace ){
        super( trace ); 
    }
    
    /**
     * Opens a connection to a given port on a remote host.
     *       
     */
    
	public void open( String hostname , int port ) throws TFTPException {

        destPort = port > 0 ? port : TFTPConstants.defaultPort ;
        
        try {
            destAddress = Inet4Address.getByName( hostname );
        } catch( UnknownHostException e ) {
            system("Unknown host " + hostname );
        }
        
        try {
            socket = new Socket( destAddress , destPort );
            input = socket.getInputStream();
            output = socket.getOutputStream();
        } catch( Exception e ){
            system("Cannot create socket");
        }
        
        debug("open: " + toString() );
	}

    /**
     * Closes a connection.
     */
    
    public void close() throws TFTPException {
        
        debug("close: " + toString() );
      
        try {  
            socket.close();
        } catch ( IOException e ) {
            system("Cannot close connection");
        }
    }
}
