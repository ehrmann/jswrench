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

import com.act365.net.*;
import com.act365.net.ip.*;

import java.beans.*;
import java.io.*;
import java.net.*;

/**
 <code>RawTCPListener</code> is a singleton class that polls for TCP or TCPJ messages.
 It raises <code>PropertyChangeEvent</code> objects that, unconventionally,
 contain an <code>IPMessage</code> object as the old value and a <code>TCPMessage</code> 
 objects as the new.
*/

public class RawTCPListener extends Thread {

  PropertyChangeSupport pcs ;

  boolean alive ;

  JSWDatagramSocket socket ;

  static int protocol ;
  
  static RawTCPListener listener = new RawTCPListener();

  static {
  	if( ( protocol = SocketWrenchSession.getProtocol() ) == 0 ){
  		protocol = SocketConstants.IPPROTO_TCPJ ;
  	}  	
  }
  
  RawTCPListener(){
    alive = true ;
    pcs = new PropertyChangeSupport( this );
 
    try {
      socket = new JSWDatagramSocket();
      // Unwanted packets will be filtered out by RawTCPSocketImpl
      socket.setSourceAddress( new byte[]{ 0 , 0 , 0 , 0 } );
      socket.setSourcePort( 0 );
    } catch( SocketException e ){
      System.err.println( getClass() + ": " + e.getMessage() );
    }
  }

  /**
   * Returns a reference to the single <code>RawTCPListener</code> object.
   */

  public static RawTCPListener getInstance() {
    return listener ;
  }

  /**
   * Starts the listener.
   */
  
  public void run() {

    while( socket == null ); // The socket takes a while to start.

    IP4Message ip4Message = new IP4Message();
    TCPMessage tcpMessage = TCP.createMessage();

    try {

      while( alive ){

        if( socket.receive( ip4Message , tcpMessage ) != protocol ){
            continue ;
        }

        pcs.firePropertyChange( "TCPJ", ip4Message , tcpMessage );
      }
    } catch( IOException e ) {
      System.err.println( getClass() + ": " + e.getMessage() );
      return ;
    }
  }
 
  /**
   * Terminates the listener.
   */
  
  public void terminate() {
    alive = false ;
  }

  /**
   * Registers an object to be notified if a message is received.
   * @param l object to be registered
   */
  
  public synchronized void addPropertyChangeListener( PropertyChangeListener l ){
    pcs.addPropertyChangeListener( l );
  }

  /**
   * Deregisters an object.
   * @param l object to be deregistered
   */
  
  public synchronized void removePropertyChangeListener( PropertyChangeListener l ){
    pcs.removePropertyChangeListener( l );
  }
}

