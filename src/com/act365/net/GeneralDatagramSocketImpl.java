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

import java.io.*;
import java.net.*;

/**
 * GeneralDatagramSocketImpl extends <code>java.net.DatagramSocketImpl</code>
 * and provides native implementations of all of its abstract methods. The
 * class remains abstract because it leaves its <code>create()</code> method
 * undefined - it is up to subclasses to specify the parameters that will
 * be used to create the underlying socket.
 */

public abstract class GeneralDatagramSocketImpl extends DatagramSocketImpl {

  /**
   Creates a new unconnected socket. The socket is allowed to be of any type
   and use any protocol supported by the underlying operating system. The method
   <code>create()</code> - note no arguments - should be defined in a subclass
   and call the version in the superclass with the parameters set
   appropriately. When a raw socket is set up, the user has the option to write
   his own IP header sockets (set <code>headerincluded</code> to <code>true</code>)
   or to allow the operating system to write the headers. NB The option does
   not exist on Windows, where the <code>IP_HDRINCL</code> symbol is 
   unsupported.
   @see com.act365.net.SocketConstants
   @param socketType the socket type as defined in <code>SocketConstants</code> e.g. SOCK_RAW
   @param protocol the protocol as defined in <code>SocketConstants</code> e.g. IPPROTO_UDP
   @param headerincluded whether the user will complete the IP header 
  */

  public void create ( int socketType , int protocol , boolean headerincluded ) throws SocketException {

    int sd = _socket( SocketConstants.AF_INET , socketType , protocol , headerincluded );

    fd = new FileDescriptor();

    setSocketDescriptor( fd , sd );
  }

  native static int _socket( int addressFamily , int socketType , int protocol , boolean headerincluded );

  /**
   Binds this socket to a local port.
   @param localPort number of the local port
   @param inetAddress local IP address
  */

  public void bind( int localPort , InetAddress inetAddress ) throws SocketException {

    _bind( getSocketDescriptor( fd ) , inetAddress.getAddress() , localPort );

    this.localPort = localPort ;
  }

  native static int _bind( int sd , byte[] ipAddress , int port );

  /**
   Closes this socket.
  */

  public void close() {

    _close( getSocketDescriptor( fd ) );
  }

  native static int _close( int sd );

  /**
   Reads senders address from a datagram packet.
   @param sender address of sender - to be populated by the function call
  */

  public int peek( InetAddress sender ) throws IOException {
    byte[] buffer = new byte[0];
    DatagramPacket dgram = new DatagramPacket( buffer , buffer.length );
    dgram.setAddress( GeneralSocketImpl.createInetAddress( SocketConstants.AF_INET , new byte[]{0,0,0,0} ));
    _receive( getSocketDescriptor( fd ) , dgram , SocketConstants.MSG_PEEK );
    sender = dgram.getAddress();
    return dgram.getPort();
  }

  /**
   Reads a datagram packet.
   @param dgram packet to be populated
  */

  public void receive( DatagramPacket dgram ) throws IOException {
  	dgram.setAddress( GeneralSocketImpl.createInetAddress( SocketConstants.AF_INET , new byte[]{0,0,0,0} ));
    _receive( getSocketDescriptor( fd ) , dgram , 0 );
  }

  native static void _receive( int sd , DatagramPacket dgram , int flags );

  /**
   Sends a datagram packet.
   @param dgram packet to be sent
  */

  public void send( DatagramPacket dgram ) throws IOException {

    _send( getSocketDescriptor( fd ) , 
           dgram.getAddress().getAddress() , 
           dgram.getPort() , 
           dgram.getData() , 
           dgram.getLength() );
  }

  native static void _send( int sd , byte[] ipAddress , int port , byte[] data , int length );

  /**
   Retrieves time-to-live for multicast sockets.
   NB Multicast sockets are not yet supported.
  */

  public int getTimeToLive() throws IOException {
    throw new IOException("Multicast sockets not supported");
  }

  /**
   Retrieves time-to-live for multicast sockets.
   NB Multicast sockets are not yet supported.
   The method leads to a deprecation warning in JDK1.3 but has to be
   provided if <code>java.net.DatagramSocketImpl</code> is to be extended.
  */

  public byte getTTL() throws IOException {
    throw new IOException("Multicast sockets not supported");  	
  }
  
  /**
   Joins a multicast group.
   NB Multicast sockets are not yet supported.
  */

  public void join( InetAddress groupAddr ) throws IOException {
    throw new IOException("Multicast sockets not supported");
  }

  /**
   Leaves a multicast group.
   NB Multicast sockets are not yet supported.
  */

  public void leave( InetAddress groupAddr ) throws IOException {
    throw new IOException("Multicast sockets not supported");
  }

  /**
   Sets time-to-live for multicast sockets.
   NB Multicast sockets are not yet supported.
  */

  public void setTimeToLive( int ttl ) throws IOException {
    throw new IOException("Multicast sockets not supported");
  }

  /**
   Sets time-to-live for multicast sockets.
   NB Multicast sockets are not yet supported.
   The method leads to a deprecation warning in JDK1.3 but has to be
   provided if <code>java.net.DatagramSocketImpl</code> is to be extended.
  */

  public void setTTL( byte ttl ) throws IOException {
  	throw new IOException("Multicast sockets not supported");
  }
  
  /**
   Gets the socket descriptor from a <code>java.io.FileDescriptor</code> object.
   NB Java provides no public access to the value of the descriptor so
   it has to be extracted using native code.
  */

  public static int getSocketDescriptor( FileDescriptor fd ){
    return _getSocketDescriptor( fd );
  }

  native static int _getSocketDescriptor( FileDescriptor fd );

  /**
   Sets the socket descriptor for a <code>java.io.FileDescriptor</code> object.
   NB Java provides no public access to the descriptor so its value has to
   be set using native code.
  */

  public static void setSocketDescriptor( FileDescriptor fd , int sd ){
    _setSocketDescriptor( fd , sd );
  }

  native static void _setSocketDescriptor( FileDescriptor fd , int sd );

  /**
   Sets the value of a socket option. The value has to be an 
   <code>Integer</code> object.
   @param optID option as defined in <code>java.net.SocketConstants</code>
   @param value an <code>Integer</code> object that wraps the new value
  */

  public void setOption( int optID , Object value ) throws SocketException {

    if( _setOption( getSocketDescriptor( fd ) , optID , value ) < 0 ){
      throw new SocketException();
    }
  }

  static native int _setOption( int socketDescriptor , int optionName , Object newValue );

  /**
   Gets the value of a socket option. The returned value will be an
   <code>Integer</code> object.
   @param optID option as defined in <code>java.net.SocketConstants</code>
  */

  public Object getOption( int optID ) throws SocketException {

    Object value = _getOption( getSocketDescriptor( fd ) , optID );

    return value ;
  }

  static native Object _getOption( int socketDescriptor , int optionName ) throws SocketException ;
};


