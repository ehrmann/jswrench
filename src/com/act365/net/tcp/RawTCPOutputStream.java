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
 * Writes to a Raw TCP socket.
 */

public class RawTCPOutputStream extends OutputStream {

  RawTCPSocketImpl socket ;

  /**
   * Creates an output stream to write to the given socket.
   */

  public RawTCPOutputStream( RawTCPSocketImpl socket ){
    this.socket = socket ;
  }

  /**
   * Writes a single byte.
   */
  
  public void write( int oneByte ) throws IOException {

    byte buffer[] = { (byte) oneByte };

    socket.write( buffer , 0 , 1 );
  }

  /**
   * Writes an entire buffer.
   */
  
  public void write( byte[] buffer ) throws IOException {
    
    socket.write( buffer , 0 , buffer.length );
  }

  /**
   * Writes a partial buffer.
   */
  
  public void write( byte[] buffer , int offset , int count ) throws IOException {
  
    socket.write( buffer , offset , count );
  }

  /**
   * Closes the output stream.
   */
  
  public void close() throws IOException {

    socket.close();
  }
}

