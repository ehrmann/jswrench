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
 * INetworkImpl provides a basic set of client-specific network functions that the
 * user is free to implement in the protocol of his choice.
 */

public interface INetworkImpl extends INetworkImplBase {
    
    /**
     * Opens a network connection.
     * 
     * @param hostname - name of remote server
     * @param port - specify or set to zero for default (69) 
     */
     
    public void open( String hostname , int port ) throws TFTPException ;    
    
    /**
     * Opens a network connection with a socket that is bound to
     * a given local address.
     * 
     * @param hostname - name of remote server
     * @param port - specify or set to zero for default (69)
     * @param localhostname - name of localhost for binding 
     * @param localport - local port for binding  
     */
     
    public void open( String hostname , int port , String localhostname , int localport ) throws TFTPException ;    
}
