/*
 * Created on 26-Aug-2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package com.act365.net.tftp;

import java.io.IOException ;
import java.net.* ;

/**
 * UDPNetworkImpl implements standard shared client- and server-side TFTP network functions with UDP/IP.
 */

public class UDPNetworkBase extends ErrorHandler {

  protected DatagramSocket socket ;
  
  protected InetAddress destAddress = null ;
        
  protected int destPort = 0 ;
    
  protected boolean receiveFirst = false ;

  /**
   * Creates a UDPNetworkBase instance with optional debug.
   * 
   * @param trace - whether debug is required
   */
  
  protected UDPNetworkBase( boolean trace ){
      super( trace );
  }

  /**
   * Closes a connection.
   */
    
  public void close() {
        
      debug("close: " + toString() );
        
      socket.close();
  }
    
  /**
   * Transmits data. 
   */
    
  public void send( byte[] buffer , int count ) throws TFTPException {

      debug("send: sent " + count + " bytes to " + toString() );
      
      try {
          socket.send( new DatagramPacket( buffer , count , destAddress , destPort ) );
      } catch ( IOException e ) {
          dump("send error", e );
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
          dump( e );
      }
  }
}
