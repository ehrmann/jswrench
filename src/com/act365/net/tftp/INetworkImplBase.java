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

import java.io.InterruptedIOException ;

/**
 * INetworkImpl provides a basic set of network functions common to the
 * client and the server that the user is free to implement in the 
 * protocol of his own choice.
 */

public interface INetworkImplBase {
	
	/**
	 * Closes a network connection.
	 */
	 
	public void close() throws TFTPException ;
	 
	/**
	 * Sends a record to the client.
     * 
	 * @param buffer - buffer in which record is stored
     * @param count - number of bytes sent 
	 */
	 
	public void send( byte[] buffer ,int count ) throws TFTPException ;
	 
	/**
	 * Reads a record from the client.
	 * 
	 * @param buffer - buffer into which record is read
	 * 
	 * @return record length
	 */
	 
	public int receive( byte[] buffer ) throws TFTPException, InterruptedIOException ;
    
    /**
     * Sets the timeout for a network connection.
     * 
     * @param timeout - timeout/s
     */
    
    public void setTimeout( int timeout ) throws TFTPException ;
}
