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

package com.act365.net ;

import java.io.*;

/**
 * <code>GeneralSocketOutputStream</code> writes to a TCP socket. 
 * The class should be used in conjunction with <code>GeneralSocketImpl</code>.
 * @see GeneralSocketImpl
 */

public class GeneralSocketOutputStream extends OutputStream {

  int socketDescriptor ;

  /**
   * Creates an output stream to write to the TCP socket with the given descriptor.
   * @param socketDescriptor socket file descriptor
   */

  public GeneralSocketOutputStream( int socketDescriptor ){
    this.socketDescriptor = socketDescriptor ;
  }

  /**
   * Writes a single character to the TCP stream.
   */
  
  public void write( int oneByte ) throws IOException {

    byte buffer[] = { (byte) oneByte };

    _send( socketDescriptor , buffer , 0 , 1 );
  }

  /**
   * Writes a string of characters into the TCP stream from a buffer.
   * @param buffer the buffer the characters are to be read from
   */
  
  public void write( byte[] buffer ) throws IOException {
    
    int sent = _send( socketDescriptor , buffer , 0 , buffer.length );
  }

  /**
   * Writes a string of characters into the TCP stream from a given
   * location in a buffer.
   * @param buffer the buffer the characters are to be read from
   * @param offset the array location from where the input is to be read
   * @param count the number of characters to be read
   */
  
  public void write( byte[] buffer , int offset , int count ) throws IOException {
  
    _send( socketDescriptor , buffer , offset , count );
  }

  static native int _send( int socketDescriptor , byte[] buffer , int offset , int count );

  /**
   * Closes the output stream.
   */
  
  public void close() throws IOException {

    int ret = _close( socketDescriptor );
  }

  static native int _close( int socketDescriptor );
}

