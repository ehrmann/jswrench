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
 * TCPNetworkServerImpl implements standard server-side TFTP network functions with TCP/IP.
 */

public class TCPNetworkServerImpl extends TCPNetworkBase implements INetworkServerImpl {

    ServerSocket socket = null ;
    
    public TCPNetworkServerImpl( boolean trace ){
        super( trace );
    }
    
	public void init( int port ) throws TFTPException {
        
        try {
            socket = new ServerSocket( port > 0 ? port : TFTPConstants.defaultPort );
        } catch ( IOException e ) {
            system("Cannot create server socket");
        }
	}

	public INetworkImplBase open() throws TFTPException {

      Socket clientSocket = null ;
      
      try {
          clientSocket = socket.accept();
      } catch ( IOException e ) {
          system("Cannot accept connection from client");
      }
      
      return new TCPNetworkImpl( clientSocket , trace );
	}

    /**
     * Closes a connection.
     */
    
    public void close() throws TFTPException {
        
        debug("close: " + toString() );
      
        try {  
            socket.close();
        } catch ( IOException e ) {
            system("Cannot close server socket");
        }
    }
}
