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
 * <code>GeneralSocketInputStream</code> reads from a TCP socket. 
 * The class should be used in conjunction with <code>GeneralSocketImpl</code>.
 * @see GeneralSocketImpl
 */

public class GeneralSocketInputStream extends InputStream {

  int socketDescriptor ;

  /**
   * Creates an input stream to read from the TCP socket with the given descriptor.
   * @param socketDescriptor socket file descriptor
   */
  
  public GeneralSocketInputStream( int socketDescriptor ){
    this.socketDescriptor = socketDescriptor ;
  }

  /**
   * Reads a single character from the TCP stream.
   */
  
  public int read() throws IOException {

    byte[] buffer = new byte[ 1 ];

    _read( socketDescriptor , buffer , 0 , 1 );

    return (int) buffer[ 0 ];
  }

  /**
   * Reads a string of characters from the TCP stream into a buffer.
   * @param buffer the buffer the characters are to be written into
   */
  
  public int read( byte[] buffer ) throws IOException {
    
    int ret = _read( socketDescriptor , buffer , 0 , buffer.length ); 

    if( ret == 0 ){
      ret = -1 ;
    }

    return ret ;
  }

  /**
   * Reads a string of characters from the TCP stream into a given
   * location in a buffer.
   * @param buffer the buffer the characters are to be read into
   * @param offset the array location where the input is to be written
   * @param count the number of characters to be read
   */
  
  public int read( byte[] buffer , int offset , int count ) throws IOException {
  
    int ret = _read( socketDescriptor , buffer , offset , count );

    if( ret == 0 ){
      ret = -1 ;
    }

    return ret ;
  }

  static native int _read( int socketDescriptor , byte[] buffer , int offset , int count );

  /**
   * Closes the input stream.
   */
  
  public void close() throws IOException {
  
    int ret = _close( socketDescriptor );
  }

  static native int _close( int socketDescriptor );
}

