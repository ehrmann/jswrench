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

package com.act365.net;

import java.io.IOException ;

/**
 * IProtocolMessage defines a template for any protocol message that is
 * to be read or written by the JSWDatagramSocket class. The intention
 * is that the public data members of the class should be of type int
 * or boolean - the use of the byte and short types in earlier versions
 * of the library led to issues with unintentional casts to negative
 * int values. 
 *
 * @see JSWDatagramSocket
 */

public interface IProtocolMessage {
    
    /**
     * Returns the protocol code, e.g. 6 for TCP or 17 for UDP.
     * @return protocol code
     */
    
    public int getProtocol();
    
    /**
     * Returns the protocol name, e.g. "TCP" or "UDP".
     * @return protocol name
     */
    
    public String getProtocolName();
    
    /**
     * Indicates whether the protocol will work only with a raw socket.
     */
    
    public boolean isRaw();
    
    /**
     * Calculates the message length in bytes, excluding the IP header.
     * @return message length in bytes
     */
    
    public int length();
    
    /**
     * Writes a textual description of the message. 
     * @return textual description
     */
    
    public String toString();
    
    /**
     * Returns the byte-array that stores the optional data segment in the message. 
     */
    
    public byte[] getData();
    
    /**
     * Returns the offset within the byte-array where the optional data segment starts.
     */

    public int getOffset();

    /**
     * Returns the size of the optional data segment.
     */
     
    public int getCount();
    
    /**
     * Populates an IProtocol message instance according to
     * the contents of a byte-stream. Returns the number of bytes read in order
     * to populate the message. Many protocols implement a checksum safety feature.
     * In principle, the checksum should always be tested, though there might be
     * circumstances (e.g. the Reader might not have access to all of the data used 
     * to calculate the original checksum) where the called might choose to avoid
     * the test.
     * 
     * @param buffer - contains the byte-stream
     * @param offset - the position of the first byte to read
     * @param count - the number of bytes available to read
     * @param testchecksum - whether to calculate the checksum
     * @return the number of bytes read in order to populate the message
     * @throws IOException cannot construct a message from the buffer contents
     */
    
    public int read( byte[] buffer , int offset , int count , boolean testchecksum ) throws IOException ;

    /**
     * Writes the message into a byte-stream at the given position.
     * 
     * @param buffer
     * @param offset
     * @return number of bytes written
     */
    
    public int write( byte[] buffer , int offset ) throws IOException ;
}
