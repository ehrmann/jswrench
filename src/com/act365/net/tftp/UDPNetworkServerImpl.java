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
import java.net.* ;

/**
 * UDPNetworkServerImpl implements standard server-side TFTP network functions with UDP/IP.
 */

public class UDPNetworkServerImpl extends UDPNetworkBase implements INetworkServerImpl {

    byte[] receiveBuffer = new byte[ TFTPConstants.maxData ];
    
    int receiveSize = -1 ;
    
	public void init( int port ) throws TFTPException {
        init( null , port );
    }
     
    public void init( String localhostname , int port ) throws TFTPException {
           
        int localport = port > 0 ? port : TFTPConstants.defaultPort ;
        
        InetAddress localhost = null ;
        
        if( localhostname instanceof String ){
            try {
                localhost = Inet4Address.getByName( localhostname );
            } catch ( UnknownHostException e ) {
                ErrorHandler.system("Unknown Host " + localhostname );
            }
        }
           
        try {
            if( localhost instanceof InetAddress ){
                socket = new DatagramSocket( localport , localhost );
            } else {
                socket = new DatagramSocket( localport );
            }
        } catch ( SocketException e ) {
            ErrorHandler.system("Cannot create socket");
        }
	}

	public INetworkImplBase open() throws TFTPException {

      int size = -1 ;
      
      /*
       * Block until the first message is received from the client.
       */        
       
      receiveFirst = true ;
      receiveSize = -1 ;
      
      while( size == -1 ){
          try {
              size = receive( receiveBuffer );
          } catch ( InterruptedIOException e ) {
          }
      }

      /*
       * Create a new network connection bound to an unknown port that connects
       * to the existing client.
       */      
       
      UDPNetworkServerImpl network = new UDPNetworkServerImpl();

      try {      
          network.socket = new DatagramSocket();
      } catch ( SocketException e ) {
          ErrorHandler.system("Cannot create socket");
      }
      
      network.destAddress = GeneralSocketImpl.createInetAddress( SocketConstants.AF_INET , destAddress.getAddress() );
      network.destPort = destPort ;
      network.receiveFirst = true ;
      network.receiveSize = size ;
      
      int i = 0 ;
      
      while( i < size ){
          network.receiveBuffer[ i ] = receiveBuffer[ i ];
          ++ i ;
      }
                
      return network ;
	}

	public void close() {
        super.close();
	}

	public void send( byte[] buffer , int count ) throws TFTPException {
        super.send( buffer , count );
	}

	public int receive( byte[] buffer ) throws TFTPException , InterruptedIOException {
        
        if( receiveSize >= 0 ){
            
            /*
             * The first message has been handled by open() and stored 
             * in receiveBuffer.
             */
            
            int size = receiveSize ;
            receiveSize = -1 ;
            
            /*
             * Surely it's necessary to copy from receiveBuffer to buffer.
             */
             
            int i = 0 ;
            
            while( i < size ){
                buffer[ i ] = receiveBuffer[ i ];
                ++ i ;
            }
            
            return size ;
        }

        DatagramPacket packet = new DatagramPacket( buffer , buffer.length );   
     
        try {   
            socket.receive( packet );
        } catch ( InterruptedIOException i ) {
            throw i ;
        } catch ( IOException e ) {
            ErrorHandler.system("Receive error");
        }
        
        ErrorHandler.debug("receive: " + packet.getLength() + 
                           " bytes from host " + packet.getAddress().toString() + 
                           ", port# " + packet.getPort() );
        
        /*
         * When receiveFirst is set, the client address is stored.
         */
         
        if( receiveFirst ){
            destAddress  = GeneralSocketImpl.createInetAddress( SocketConstants.AF_INET , packet.getAddress().getAddress() );
            destPort = packet.getPort();
            receiveFirst = false ;            
        } 
        
        /*
         * Ensure the message has been received from the expected client.
         */
         
        if( destPort != 0 && packet.getPort() != destPort ){
            ErrorHandler.dump("received from port# " + packet.getPort() + ", expected from port# " + destPort );
        }
        
        return packet.getLength() ;
	}
}
