/*
  * JSocket Wrench
  * 
  * Copyright (C) act365.com December 2004
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

package com.act365.net;

import java.io.IOException ;

/**
 * IServiceMessage defines a template for any message associated with
 * a TCP or UDP service.
 */

public interface IServiceMessage {
    
    /**
     * Returns the service name, e.g. "DNS".
     * @return protocol name
     */
    
    public String getServiceName();
    
    /**
     * Returns the well-known port number associated with the service.
     */
    
    public int getWellKnownPort();
    
    /**
     * Calculates the message length in bytes.
     * @return message length in bytes
     */
    
    public int length();
    
    /**
     * Writes a textual description of the message. 
     */
    
    public void dump( java.io.PrintStream printer );
    
    /**
     * Populates an IServiceMessage instance according to
     * the contents of a byte-stream. Returns the number of bytes read in order
     * to populate the message. 
     * 
     * @param buffer - contains the byte-stream
     * @param offset - the position of the first byte to read
     * @param count - the number of bytes available to read
     * @return the number of bytes read in order to populate the message
     * @throws IOException cannot construct a message from the buffer contents
     */ 
    
    public int read( byte[] buffer , int offset , int count ) throws IOException ;

    /**
     * Writes the message into a byte-stream at the given position.
     * 
     * @param buffer 
     * @param offset
     * @return number of bytes written
     */
    
    public int write( byte[] buffer , int offset ) throws IOException ;
}
