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

import com.act365.net.* ;

import java.io.* ;
import java.net.InetAddress ;

/**
 * TCPNetworkImpl implements standard shared client- and server-side TFTP network functions with TCP/IP.
 */

public class TCPNetworkBase {

  protected InputStream input = null ;
  
  protected OutputStream output = null ;
  
  protected InetAddress destAddress = null ;
  
  protected int destPort = 0 ;
    
  /**
   * Transmits data. The TFTP message is prefaced with a 2-byte record
   * that describes the length of the record. 
   */
    
  public void send( byte[] buffer , int count ) throws TFTPException {

      ErrorHandler.debug("send: sent " + count + " bytes to " + toString() );

      byte[] countBuffer = new byte[ 2 ];
      
      SocketUtils.shortToBytes( (short) count , countBuffer , 0 );      
      
      try {
          output.write( countBuffer , 0 , 2 );
          output.write( buffer , 0 , count );
      } catch ( IOException e ) {
          ErrorHandler.system("Transmission error");
      }
  }

  /**
   * Receives data.
   */
    
  public int receive(byte[] buffer) throws TFTPException , InterruptedIOException {
      
      int size = 0 ;
      
      short count = 0 ;
      
      byte[] countBuffer = new byte[ 2 ];
        
      try {
          size = input.read( countBuffer );
          if( size != 2 ){
              ErrorHandler.dump("receive: incorrect size for length prefix");
          }
          count = SocketUtils.shortFromBytes( countBuffer , 0 );   
          size = input.read( buffer , 0 , count );
          if( size != count ){
              ErrorHandler.dump("receive: incorrect size for record");
          }
      } catch ( InterruptedIOException i ) {
          throw i ;
      } catch ( IOException e ) {
          ErrorHandler.system("Receive error");
      }
        
      ErrorHandler.debug("receive: " + size + " bytes from " + toString() );
               
      return size ;
  }

  /**
   * Provides string representation.
   */
    
  public String toString(){
        
      return "host " + destAddress.toString() + ", port# " + destPort ;
  }
    
  /**
   * Sets timeout.
   */
    
  public void setTimeout( int timeout ) throws TFTPException {
  }
}
