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

/**
 * INetworkServerImpl provides a basic set of server-specific network functions 
 * that the user is free to implement with any protocol.
 */

public interface INetworkServerImpl extends INetworkImplBase {
	
    /**
     * Initiates a network connection for the server, if the server
     * has not been initiated by <code>inetd</code>.
     * 
     * @param port - specify or set to zero for default
     */
     
    public void init( int port ) throws TFTPException ;

    /**
     * Initiates a network connection for the server, if the server
     * has not been initiated by <code>inetd</code>.
     * 
     * @param localhost - specify or set to null for default
     * @param port - specify or set to zero for default
     */
     
    public void init( String localhost , int port ) throws TFTPException ;

	 /**
	  * Returns a new network object to handle a new client connection.
	  * 
	  * @return new network connection
	  */
     
	 public INetworkImplBase open() throws TFTPException ;
}
