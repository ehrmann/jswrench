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
import java.io.* ;

/**
 * TCPNetworkImpl implements standard client-side TFTP network functions with TCP/IP.
 */

public class TCPNetworkImpl extends TCPNetworkBase implements INetworkImpl {

    Socket socket = null ;
  
    /**
     * Creates an connected TCPNetworkImpl.
     * 
     * @param socket - socket that connects to the server
     */    
    
    public TCPNetworkImpl( Socket socket ) throws TFTPException {
    
        try {
            this.socket = socket ;
            this.input = socket.getInputStream();
            this.output = socket.getOutputStream();
            this.destAddress = socket.getInetAddress();
            this.destPort = socket.getPort();
        } catch ( IOException e ) {
            ErrorHandler.system("Problem with socket");
        }
    }
    
    /**
     * Creates an unconnected TCPNetworkImpl.
     */    
    
    public TCPNetworkImpl() {
        socket = null ;
    }
    
    /**
     * Opens a connection to a given port on a remote host.
     *       
     */
    
	public void open( String hostname , int port ) throws TFTPException {
        
        open( hostname , port , null , 0 );
    }
    
    /**
     * Opens a connection to a given port on a remote host
     * from a socket bound to a given local address.
     *       
     */
    
    public void open( String hostname , int port , String localhostname , int localport ) throws TFTPException {    

        InetAddress localhost = null ;
        
        destPort = port > 0 ? port : TFTPConstants.defaultPort ;
        
        try {
            destAddress = Inet4Address.getByName( hostname );
            if( localhostname instanceof String ){
                localhost = Inet4Address.getByName( localhostname );
            }
        } catch( UnknownHostException e ) {
            ErrorHandler.system("Unknown host " + hostname );
        }
        
        try {
            if( localhost instanceof InetAddress ){
                socket = new Socket( destAddress , destPort , localhost , localport );
            } else {
                socket = new Socket( destAddress , destPort );
            }
            input = socket.getInputStream();
            output = socket.getOutputStream();
        } catch( Exception e ){
            ErrorHandler.system("Cannot create socket: " + e.getMessage() );
        }
        
        ErrorHandler.debug("open: " + toString() );
	}
    
    /**
     * Closes a connection.
     */
    
    public void close() throws TFTPException {
        
        ErrorHandler.debug("close: " + toString() );
      
        try {  
            socket.close();
        } catch ( IOException e ) {
            ErrorHandler.system("Cannot close connection");
        }
    }
}
