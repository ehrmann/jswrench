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

import java.beans.* ;
import java.io.* ;
import java.net.* ;
import java.util.*;

/**
 * Implements the TCPJ protocol, which is a clone of TCP that would typically be
 * used with a non-standard IP protocol setting.
 */

class TCPJSocketImpl extends SocketImpl implements PropertyChangeListener {
  
  final static long msltimeout = 10000 ;

  final static int transmissionlimit = 3 ,
                   maxwindowsize = 32767 ,
                   minEphemeralPort = 1024 ,
                   maxEphemeralPort = 5000 ,
                   protocol = SocketUtils.getProtocol();

  final static boolean debug = false ,
                       closeserver = true ,
                       includeipheader = SocketUtils.includeHeader() ,
                       implementackdelay = true ,
                       modelpacketloss = false ;

  final static double alpha = 0.9 ,
                      beta = 2.0 ,
                      packetloss = 0.1 ;

  static int nextEphemeralPort = 1024 ;
 
  DatagramSocket socket ;

  InetAddress localhost ;

  int state ,
      previousstate ,
      localseqnum ,
      destseqnum ,
      acknowledgedseqnum ,
      windowsize ,
      destwindowsize ,
      readoffset ,
      readcount ;
  
  long rto ,
       rtt ,
       sendtime ,
       receivetime ;

  byte[] readbuffer ;

  TCPAcknowledger acknowledger ;

  TCPMSLTimer msltimer ;

  Random random ;

  /**
   * Default constructor
   * @throws SocketException if the underlying <code>DatagramSocket</code> object cannot be opened
   */
  
  public TCPJSocketImpl() throws SocketException {

    resetSocket();
 
    socket = new DatagramSocket();

    acknowledger = new TCPAcknowledger( this , 200 );
 
    msltimer = new TCPMSLTimer( this , msltimeout );

    TCPJListener.getInstance().addPropertyChangeListener( this );

    if( modelpacketloss ){
      random = new Random();
    }
  }
  
  /** 
   Sends a TCP message to the destination but doesn't await acknowledgement. 
   Checks whether the receiver is ready to receive.
  */ 

  void send( int flags , TCPOptions options , byte[] buffer , int offset , int count , boolean retransmission ) throws IOException {

    int sendsize ;

    if( ( flags & TCP.PSH ) > 0 ){
      sendsize = localseqnum - acknowledgedseqnum ;
      while( count > 0 ){
        while( sendsize >= destwindowsize ){
          try {
            wait(); // Persist timer
          } catch( InterruptedException ie ) {
          }
        }
        sendsize = Math.min( count , acknowledgedseqnum + destwindowsize - localseqnum );
        transmit( flags , options , buffer , offset , sendsize , retransmission );
        offset += sendsize ;
        count -= sendsize ; 
      } 
    } else {
      transmit( flags , options , buffer , offset , count , retransmission );
    }
  }

  /**
   Updates the Retransmission Timeout Value (RTO).
  */

  void updateRTO(){
  
    if( sendtime > 0 && receivetime > 0 ){
      rtt = (long)( alpha * rtt + ( 1 - alpha )*( receivetime - sendtime ) );
      rto = (long)( beta * rtt );
      sendtime = 0 ;
      receivetime = 0 ;

      if( debug ){
        System.err.println("Updated rto: " + rto );
      }
    }
  }

  /**
   Sends a message and awaits acknowledgement. Updates the estimate 
   of the connection round-trip time.
  */
   
  synchronized void sendAndAwaitACK( int flags , TCPOptions options , byte[] buffer , int offset , int count ) throws IOException {

    int sendsize = 1 , 
        counter = 1 ;

    long delay = rto ;

    send( flags , options , buffer , offset , count , false );

    try {
      wait( delay );
      delay *= 2 ;
      sendsize = acknowledgedseqnum == 0 ? 0 : localseqnum - acknowledgedseqnum ;
      while( ( acknowledgedseqnum == 0 || sendsize > 0  ) && counter ++ < transmissionlimit ){
        send( flags , options , buffer , Math.max( offset - sendsize , 0 ) , Math.min( sendsize , buffer.length ) , true );
        wait( delay );
        delay *= 2 ;
        sendsize = acknowledgedseqnum == 0 ? 0 : localseqnum - acknowledgedseqnum ;
      } 
    } catch ( Exception e ) {
      System.err.println("Exception: " + e.getMessage() );
    }
  
    if( sendsize > 0 ){
      throw new IOException("Connection reset");
    }

    updateRTO();
  }

  /**
   Sends a message with no data but doesn't await acknowledgement.
  */

  void send( int flags ) throws IOException {
    send( flags , new TCPOptions() , new byte[0] , 0 , 0 , false );
  }

  /**
   Sends a message with no data and awaits acknowledgement.
  */

  void sendAndAwaitACK( int flags ) throws IOException {
    sendAndAwaitACK( flags , new TCPOptions() , new byte[0] , 0 , 0 );
  }

  /**
   Sends a message with no data but some options and awaits acknowledgement.
  */

  void sendAndAwaitACK( int flags , TCPOptions options ) throws IOException {
    sendAndAwaitACK( flags , options , new byte[0] , 0 , 0 );
  }

  /** 
   Transmits a TCP message to the destination. No check is made beforehand
   to see whether the receiver is ready to receive.
  */ 

  void transmit( int flags , TCPOptions options , byte[] buffer , int offset , int count , boolean retransmit ) throws IOException {

    if( localhost == null ){
      throw new IOException("Local address not yet set");
    } else if( address == null ){
      throw new IOException("Destination address not yet set");
    } else if( port == 0 ){
      throw new IOException("Destination port not yet set");
    }

    if( localport == 0 ){
      localport = nextEphemeralPort ++ ;
      if( nextEphemeralPort == maxEphemeralPort ){
        nextEphemeralPort = minEphemeralPort ;
      }
    } 

    if( acknowledger.isAlive() ){
      acknowledger.interrupt();
      flags |= TCP.ACK ;
    } else if( destseqnum != 0 ) {
      flags |= TCP.ACK ;
    }

    if( retransmit ){
      if( ( flags & TCP.SYN ) > 0 || ( flags & TCP.FIN ) > 0 ){
        -- localseqnum ;
      } else if( ( flags & TCP.PSH ) > 0 ) {
        localseqnum -= count ;
      }
      sendtime = 0 ;
    } else {
      sendtime = new Date().getTime();
    }

    byte[] sendbuffer = TCPWriter.write( localhost.getAddress() ,
                                         (short) localport ,
                                         address.getAddress() ,
                                         (short) port ,
                                         localseqnum ,
                                         destseqnum ,
                                         ( flags & TCP.ACK ) > 0 ,
                                         ( flags & TCP.RST ) > 0 ,
                                         ( flags & TCP.SYN ) > 0 ,
                                         ( flags & TCP.FIN ) > 0 ,
                                         ( flags & TCP.PSH ) > 0 ,
                                         (short) windowsize ,
                                         options ,
                                         buffer ,  
                                         offset ,
                                         count  );

    if( includeipheader ){

      sendbuffer = IP4Writer.write( IP4.TOS_COMMAND ,
                                    (short) 255 ,
                                    (byte) protocol ,
                                    localhost.getAddress() ,
                                    address.getAddress() , 
                                    sendbuffer );
    }

    if( debug ){
      System.err.println("SEND:");
      SocketUtils.dump( System.err , sendbuffer , 0 , sendbuffer.length );
    }

    if( ( flags & TCP.SYN ) > 0 || ( flags & TCP.FIN ) > 0 ){
      ++ localseqnum ;
    } else if( ( flags & TCP.PSH ) > 0 ) {
      localseqnum += count ;
    }

    socket.send( new DatagramPacket( sendbuffer , sendbuffer.length , address , port ) );
  }

  /**
   * Acknowledges a received TCP message.
   */
  
  public void acknowledge() throws IOException {

    if( ! implementackdelay ){
      send( TCP.ACK );
    } else if( acknowledger.isAlive() ) {
      acknowledger.interrupt();
      send( TCP.ACK );
    } else {
      acknowledger.start();
    }
  }

  /**
   * Resets a socket to its default values.
   */
  
  void resetSocket(){

    address = null ;
    port = 0 ;
    
    try {
      localport = 0 ;
      if( localhost == null ){
        localhost = InetAddress.getLocalHost();
      }
    } catch ( UnknownHostException uhe ) {
      localhost = null ;
    }
     
    state = previousstate = TCP.CLOSED ; 
    localseqnum = ISNCounter.getCounter();
    destseqnum = 0 ;
    acknowledgedseqnum = 0 ;
    windowsize = maxwindowsize ;
    destwindowsize = 0 ;

    readbuffer = new byte[ maxwindowsize ];
    readoffset = 0 ;
    readcount  = 0 ;
 
    rtt = 1000 ;
    rto = (long)( beta * rtt );
    sendtime = 0 ;
    receivetime = 0 ;
  }

  /**
   * Handles an received TCPMessage
   * @throws IOException message is illegal
   */

  synchronized void receive( TCPMessage message ) throws IOException {

    if( modelpacketloss && random.nextFloat() < packetloss ){
      return ;
    }

    receivetime = new Date().getTime();

    if( message.ack ){
      acknowledgedseqnum = message.acknowledgementnumber ;
    }

    if( message.syn || message.fin ){
      destseqnum = message.sequencenumber + 1 ;
    } else if( message.psh ) {
      destseqnum = message.sequencenumber + message.data.length ;
    }
      
    destwindowsize = message.windowsize ;

    switch( state ){

    case TCP.CLOSED:
      break;
    case TCP.LISTEN:
      if( message.syn ){
        send( TCP.SYN | TCP.ACK );
        previousstate = state ;
        state = TCP.SYN_RCVD ;
        notifyAll();
      } 
      return ;
    case TCP.SYN_RCVD:
      if( message.ack ){
        previousstate = state ;
        state = TCP.ESTABLISHED ;
        notifyAll();
        return;
      } else if( message.rst && previousstate == TCP.LISTEN ) {
        previousstate = state ;
        state = TCP.LISTEN ;
        notifyAll();
        return ;
      }
      break;
    case TCP.SYN_SENT:
      if( message.syn && message.ack ){
        send( TCP.ACK );
        previousstate = state ;
        state = TCP.ESTABLISHED ;
        notifyAll();
        return ;
      } else if( message.syn ){
        send( TCP.SYN | TCP.ACK );
        previousstate = state ;
        state = TCP.SYN_RCVD ;
        notifyAll();
        return;
      }
      break;
    case TCP.ESTABLISHED:
      if( message.fin ){
        if( closeserver ){
          send( TCP.FIN | TCP.ACK );
          previousstate = state ;
          state = TCP.LAST_ACK ;
          notifyAll();
          return;
        } else {
          send( TCP.ACK );
          previousstate = state ;
          state = TCP.CLOSE_WAIT ;
          notifyAll();
          return ;
        }
      } else if( message.psh ){
        {
          int i = - 1 ;
          while( ++ i < message.data.length ){
            readbuffer[( readoffset + readcount + i ) % maxwindowsize ] = message.data[ i ];
          }
        }
        readcount += message.data.length ;
        windowsize -= message.data.length ;
        acknowledge();
        notifyAll();
        return ;
      } else if( message.ack ){
        notifyAll();
        return ;
      }
      break;
    case TCP.CLOSE_WAIT :
      return;
    case TCP.FIN_WAIT_1 :
      if( message.fin && message.ack ){
        send( TCP.ACK );
        timeWait();
        return ;
      } else if( message.fin ){
        send( TCP.ACK );
        previousstate = state ;
        state = TCP.CLOSING ;
        notifyAll();
        return ;
      } else if( message.ack ){
        previousstate = state ;
        state = TCP.FIN_WAIT_2 ;
        notifyAll();
        return ;
      }
      break;
    case TCP.CLOSING :
      if( message.ack ){
        timeWait();
        return;
      }
      break;
    case TCP.LAST_ACK :
      if( message.ack ){
        resetSocket();
        notifyAll();
        return;
      }
      break;
    case TCP.FIN_WAIT_2 :
      if( message.fin ){
        send( TCP.ACK );
        timeWait();
        return ;
      }
      break;
    case TCP.TIME_WAIT :
      if( message.fin ){
        send( TCP.ACK );
        notifyAll();
        return ;
      }
      break;
    }
  
    if( ! message.rst ){
      System.err.println("RST from state: " + state );
      send( TCP.RST );
    }

    resetSocket();

    throw new IOException("Connection reset by peer");
  }
  
  void activeOpen( InetAddress address , short port ) throws IOException {

    if( state != TCP.CLOSED ){
      throw new IOException("Active Open only permitted from CLOSED state");
    }

    this.address = address ;
    this.port = port ;

    localseqnum = ISNCounter.getCounter();
    destseqnum = 0 ;

    TCPOptions options = new TCPOptions();

    options.setMaxSegmentSize( (short) 1440 );

    state = TCP.SYN_SENT ; 
  }

  void passiveOpen() throws IOException {
   
    if( state != TCP.CLOSED ){
      throw new IOException("Passive Open only permitted from CLOSED state");
    }

    state = TCP.LISTEN ;
  }

  synchronized void tcpjClose() throws IOException {

    switch( state ){

      case TCP.ESTABLISHED:
      case TCP.SYN_RCVD:
        state = TCP.FIN_WAIT_1 ;
        sendAndAwaitACK( TCP.FIN );
        break;
      case TCP.SYN_SENT:
        resetSocket();
        break;  
      case TCP.CLOSE_WAIT:
        state = TCP.LAST_ACK ;
        sendAndAwaitACK( TCP.FIN );
        break;
      default:
        // Will reach here if an exception has been thrown during socket set-up
    }

    while( state != TCP.CLOSED ){
      try {
        wait();
      } catch( InterruptedException ie ){
      }
    }

    msltimer.interrupt();

    if( debug ){
      System.err.println("Connection closed");
    }

    resetSocket();
  }
      
  synchronized void setState( int state ){
    this.state = state ;
    notifyAll();
  }

  synchronized void timeWait() throws IOException {
    
    state = TCP.TIME_WAIT ; 

    msltimer.start();
  }

  /**
   * Writes a buffer and awaits acknowledgement.
   */

  public synchronized void write( byte[] buffer , int offset , int count ) throws IOException {

    if( state != TCP.ESTABLISHED ){
      throw new IOException("Write not possible from current state");
    }

    sendAndAwaitACK( TCP.PSH , new TCPOptions() , buffer , offset , count );
  }

  /**
   * Reads a buffer.
   */
  
  public synchronized int read( byte[] buffer , int offset , int count ) throws IOException {

    if( state != TCP.ESTABLISHED ){
      return 0 ;
    }

    final int initialcount = count ,
              initialoffset = offset ;

    while( count > 0 ){
      while( readcount == 0 ){
        try {
          wait();
        } catch( InterruptedException ie ) {
        }
      }
      windowsize += Math.min( count , readcount );
      while( count > 0 && readcount > 0 ){
        buffer[ offset ] = readbuffer[ readoffset % maxwindowsize ];
        ++ offset ;
        ++ readoffset ;
        -- readcount ;
        -- count ;
      }
    }
        
    return initialcount ;
  }

  /**
   * Called by <code>TCPJListener</code> if a message is received.
   * @see TCPJListener
   */
  
  public void propertyChange( PropertyChangeEvent evt ){

    IP4Message ipmessage = (IP4Message) evt.getOldValue();

    if( address instanceof InetAddress ){

        byte[] destinationaddress = address.getAddress();

        if( destinationaddress[ 0 ] != ipmessage.source[ 0 ] || 
            destinationaddress[ 1 ] != ipmessage.source[ 1 ] ||
            destinationaddress[ 2 ] != ipmessage.source[ 2 ] ||
            destinationaddress[ 3 ] != ipmessage.source[ 3 ] ){
            return ;
        }
    }

    if( localhost instanceof InetAddress ){
 
        byte[] localaddress = localhost.getAddress();

        if( localaddress[ 0 ] != ipmessage.destination[ 0 ] ||
            localaddress[ 1 ] != ipmessage.destination[ 1 ] ||
            localaddress[ 2 ] != ipmessage.destination[ 2 ] ||
            localaddress[ 3 ] != ipmessage.destination[ 3 ] ){
            return ;
        }
    }

    TCPMessage tcpmessage = (TCPMessage) evt.getNewValue();

    if( port != 0 && port != tcpmessage.sourceport || localport != tcpmessage.destinationport ){
      return ;
    }

    if( debug ){
      System.err.println("RECEIVE:");
      SocketUtils.dump( System.err , ipmessage.data , 0 , ipmessage.data.length );
    }

    try {
      if( address == null ){
        address = InetAddress.getByName( ipmessage.source[ 0 ] + "." +
                                         ipmessage.source[ 1 ] + "." +
                                         ipmessage.source[ 2 ] + "." +
                                         ipmessage.source[ 3 ] );
      }
      if( port == 0 ){
        port = tcpmessage.sourceport ;
      }
      receive( tcpmessage );
    } catch( Exception e ) {
    }
  }

  /**
   * Creates a new TCPJ socket.
   */
  
  public void create( boolean stream ) throws IOException {

    if( ! stream ){
      throw new IOException("TCP/J does not support datagram sockets");
    }

    resetSocket();
  }

  /**
   * Binds a socket to the given address and port.
   */
  
  public void bind( InetAddress inetAddress , int port ) throws IOException {

    localhost = inetAddress ;

    if( port != (short) port ){
      throw new IOException("Illegal port number");
    } else if( port == 0 ) {
      throw new IOException("Use of port 0 is prohibited");
    } else if( port >= minEphemeralPort && port <= maxEphemeralPort ) {
      throw new IOException("Ports " + minEphemeralPort + " to " + maxEphemeralPort + " are reserved for ephemeral use");
    } else {
      localport = (short) port ;
    }
  }

  /**
   * Connects to a remote address.
   */
  
  public void connect( String hostname , int port ) throws IOException {

    connect( InetAddress.getByName( hostname ) , port );
  }

  /**
   * Connects to a remote address.
   */
  
  public synchronized void connect( InetAddress dst , int remotePort ) throws IOException {

    if( remotePort != (short) remotePort ){
      throw new IOException("Illegal port number");
    }

    TCPOptions options = new TCPOptions();

    options.setMaxSegmentSize( (short) 1440 );

    activeOpen( dst , (short) remotePort );

    sendAndAwaitACK( TCP.SYN , options );

    while( state != TCP.ESTABLISHED ){
      try {
        wait();
      } catch( InterruptedException ie ) {
      }
    }
  }

  /**
   * Accepts a server connection.
   */
  
  public synchronized void accept( SocketImpl newSocket ) {

    while( state != TCP.ESTABLISHED ){
      try {
        wait();
      } catch ( InterruptedException ie ) {
      }
    }

    TCPJSocketImpl tcpjsocket = (TCPJSocketImpl) newSocket ;

    tcpjsocket.resetSocket();

    tcpjsocket.localport = localport ;
    tcpjsocket.port = port ;
    tcpjsocket.state = state ;
    tcpjsocket.previousstate = previousstate ;
    tcpjsocket.localseqnum = localseqnum ;
    tcpjsocket.destseqnum = destseqnum ;
    tcpjsocket.acknowledgedseqnum = acknowledgedseqnum ;
    tcpjsocket.windowsize = windowsize ;
    tcpjsocket.destwindowsize = destwindowsize ;

    if( address instanceof InetAddress ){
      try {
        tcpjsocket.address = InetAddress.getByName( address.getHostName() );
      } catch( UnknownHostException e ){
        tcpjsocket.address = null ;
      }
    } else {
      tcpjsocket.address = null ;
    }

    if( localhost instanceof InetAddress ){
      try {
        tcpjsocket.localhost = InetAddress.getByName( localhost.getHostName() );
      } catch( UnknownHostException e ){
        tcpjsocket.localhost = null ;
      }
    } else {
      tcpjsocket.localhost = null ;
    }

    port  = 0 ;
    address = null ;
    state = TCP.LISTEN ;
  }

  /**
   * Closes a connection.
   */
  
  public void close() throws IOException {

    tcpjClose();
  }

  /**
   * Sets a server to listen for a client connection. 
   * Note that the backlog argument is ignored - currently,
   * TCPJ cannot stack connection requests.
   */
  
  public void listen( int backlog ) throws IOException {

/*
    if( backlog != 1 ){
      throw new IOException("TCP/J only stacks a single connection request");
    }
*/
    passiveOpen();
  }

  /**
   * Sets a TCP option.
   */
  
  public void setOption( int optID , Object value ) throws SocketException {
    throw new SocketException("No options supported");
  }

  /**
   * Gets a TCP option.
   */
  
  public Object getOption( int optID ) throws SocketException {
    throw new SocketException("No options supported");
  }

  /**
   * <code>available()</code> isn't implemented.
   */
  
  public int available() throws IOException {
    throw new IOException("available() not supported");
  }

  /**
   * Gets an input stream to read.
   */
  
  public InputStream getInputStream() throws IOException {
    return new TCPJInputStream( this );
  }

  /**
   * Gets an output stream to write.
   */
  
  public OutputStream getOutputStream() throws IOException {
    return new TCPJOutputStream( this );
  }
}
