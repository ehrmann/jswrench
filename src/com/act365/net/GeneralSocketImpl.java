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
 * GeneralDatagramSocketImpl extends <code>java.net.SocketImpl</code>
 * and provides native implementations of all of its abstract methods. The
 * class remains abstract because it leaves its <code>create()</code> method
 * undefined - it is up to subclasses to specify the parameters that will
 * be used to create the underlying socket. The native code calls into the
 * local Berkeley sockets implementation. 
 */

public abstract class GeneralSocketImpl extends SocketImpl {

  /**
   Creates a new unconnected TCP socket.
   @see com.act365.net.SocketConstants
   @param socketType the socket type as defined in <code>SocketConstants</code> e.g. SOCK_RAW
   @param protocol the protocol as defined in <code>SocketConstants</code> e.g. IPPROTO_UDP
  */

  public void create ( int socketType , int protocol ) throws IOException {

    int sd = _socket( SocketConstants.AF_INET , socketType , protocol );

    fd = new FileDescriptor();

    setSocketDescriptor( fd , sd );
  }

  native static int _socket( int addressFamily , int socketType , int protocol );

  /**
   Binds this socket to the local port.
   @param inetAddress local IP address
   @param port number of the local port
  */

  public void bind( InetAddress inetAddress , int port ) throws IOException {

    _bind( getSocketDescriptor( fd ) , inetAddress.getAddress() , port );

    address   = createInetAddress( SocketConstants.AF_INET , inetAddress.getAddress() ); 
    localport = port ;
  }

  native static int _bind( int sd , byte[] ipAddress , int port );

  /**
   Connects this socket to a named host.
   @param hostName name of the remote host
   @param port port number on the remote host
  */

  public void connect( String hostName , int port ) throws IOException {

    connect( InetAddress.getByName( hostName ) , port );
  }

  /**
   Connects this socket to a given destination.
   @param dst address of the remote host
   @param remotePort port number on the remote host
  */

  public void connect( InetAddress dst , int remotePort ) throws IOException {

    int ret = _connect( getSocketDescriptor( fd ) ,
                        dst.getAddress() ,
                        remotePort );

    port    = remotePort ;
    address = dst ;
  }

  /**
   * Connects to the specified host.
   */
  
  public void connect( SocketAddress address , int timeout ) throws IOException {
  	throw new IOException("SocketAddress is not supported");
  }
  
  native static int _connect( int sd , byte[] ipAddress , int port );

  /**
   Listens for connection requests on this socket.
   @param backlog maxmimum number of pending requests to be serviced simultaneously
  */

  public void listen( int backlog ) throws IOException {

    _listen( getSocketDescriptor( fd ) , backlog );
  }

  native static int _listen( int sd , int backlog );

  /**
   Accepts a connection request for this socket.
   @param newSocket socket to be populated with the details of the remote client
  */

  public void accept( SocketImpl newSocket ) throws IOException {

    _accept( getSocketDescriptor( fd ) , newSocket );
  }

  native static int _accept( int sd , SocketImpl newSocket );

  /**
   Closes this socket.
  */

  public void close() throws IOException {

    _close( getSocketDescriptor( fd ) );
  }

  native static int _close( int sd );

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
   Creates a <code>java.net.Inet4Address</code> object with a given IP address.
   @param family the socket family - normally SocketConstants.AF_INET
   @param ipAddress the IP address e.g. new byte[] { 127, 0, 0, 1 }
  */

  public static InetAddress createInetAddress( int family , byte[] ipAddress ){
	return _createInetAddress( family , ipAddress );
  }

  /**
   Creates a <code>java.net.Inet4Address</code> object with no IP address.
   In general, the field will be populated by a subsequent call to receive().
  */

  public static InetAddress createInetAddress(){
	return _createInetAddress( SocketConstants.AF_INET , null );
  }

  native static InetAddress _createInetAddress( int family , byte[] ipAddress );

  /**
   Sets the value of a socket option.The value has to be an 
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

    return _getOption( getSocketDescriptor( fd ) , optID );
  }

  static native Object _getOption( int socketDescriptor , int optionName ) throws SocketException ;

  /**
   Gets the output stream.
  */

  public OutputStream getOutputStream() throws IOException {
    return new GeneralSocketOutputStream( getSocketDescriptor( fd ) );
  }

  /**
   * Gets the input stream.
   */
  
  public InputStream getInputStream() throws IOException {
    return new GeneralSocketInputStream( getSocketDescriptor( fd ) );
  }

  /**
   * <code>available()</code> isn't supported.
   */
  
  public int available() throws IOException {
    throw new IOException("available() not supported");
  }
  
  /**
   * Urgent data isn't supported.
   */

  public void sendUrgentData( int data ) throws IOException {
  	throw new IOException("Urgent data not supported");  
  }
};
