/*
 * Created on 26-Aug-2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package com.act365.net.tftp;

import java.io.* ;
import java.net.* ;

/**
 * UDPNetworkImpl implements standard shared client- and server-side TFTP network functions with UDP/IP.
 */

public class UDPNetworkBase {

  protected DatagramSocket socket ;
  
  protected InetAddress destAddress = null ;
        
  protected int destPort = 0 ;
    
  protected boolean receiveFirst = false ;

  /**
   * Closes a connection.
   */
    
  public void close() {
        
      ErrorHandler.debug("close: " + toString() );
        
      socket.close();
  }
    
  /**
   * Transmits data. 
   */
    
  public void send( byte[] buffer , int count ) throws TFTPException {

      ErrorHandler.debug("send: sent " + count + " bytes to " + toString() );
      
      try {
          socket.send( new DatagramPacket( buffer , count , destAddress , destPort ) );
      } catch ( IOException e ) {
          ErrorHandler.system("Retransmission error");
      }
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
      try {
          socket.setSoTimeout( timeout * 1000 );
      } catch ( SocketException e ){
          ErrorHandler.system("Cannot set timeout for connection");
      }
  }
}
