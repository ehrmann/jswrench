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

package com.act365.net.tcp ;

import java.io.*;

/**
 * <code>TCPJInputStream</code> reads data from a TCPJ socket.
 */

public class RawTCPInputStream extends InputStream {

  RawTCPSocketImpl socket ;
  
  /*
   * Creates an input stream to read from the named socket.
   */
   
  public RawTCPInputStream( RawTCPSocketImpl socket ){
    this.socket = socket ;
  }

  /**
   * Reads a single byte.
   */
  
  public int read() throws IOException {

    byte[] buffer = new byte[ 1 ];

    socket.read( buffer , 0 , 1 );

    return (int) buffer[ 0 ];
  }

  /**
   * Reads an entire buffer.
   */
  
  public int read( byte[] buffer ) throws IOException {
    
    return socket.read( buffer , 0 , buffer.length ); 
  }

  /**
   * Reads a partial buffer.
   */
  
  public int read( byte[] buffer , int offset , int count ) throws IOException {
  
    return socket.read( buffer , offset , count );
  }

  /**
   * Closes the stream.
   */
  
  public void close() throws IOException {
  
    socket.close();
  }
}

