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

class RawTCPSocketImpl extends SocketImpl implements PropertyChangeListener {
  
  static long msltimeout ;

  static int maxwindowsize ,
             minEphemeralPort ,
             maxEphemeralPort ,
             firstTimeout ;
                   
  static boolean debug ,
                 avoidhalfcloseserver ,
                 avoidackdelay ,
                 modelpacketloss ;

  static double packetloss ;

  final static InetAddress ip0 = GeneralSocketImpl.createInetAddress() ,
                           iplocalhost = GeneralSocketImpl.createInetAddress( SocketConstants.AF_INET , new byte[] {127,0,0,1} );
  
  static int nextEphemeralPort = 1024 ;
   
  JSWDatagramSocket socket ;

  InetAddress localhost ;

  int state ,
      previousstate ,
      localseqnum ,
      destseqnum ,
      acknowledgedseqnum ,
      windowsize ,
      destwindowsize ,
      readoffset ,
      readcount ,
      writestart ,
      writeend ;

  byte[] readbuffer ,
         writebuffer ;

  TCPAcknowledger acknowledger ;

  TCPMSLTimer msltimer ;

  RetransmissionTimer retransmissionTimer ;
  
  Random random ;

  static {
  	
  	// Reads system property list
  	
	msltimeout = Integer.getInteger("tcpj.msltimeout", 5000 ).longValue();
    firstTimeout = Integer.getInteger("tcpj.firsttimeout", 3 ).intValue();
	maxwindowsize = Integer.getInteger("tcpj.maxwindowsize", 32767 ).intValue();
	minEphemeralPort = Integer.getInteger("tcpj.minephemeralport", 1024 ).intValue();
	maxEphemeralPort = Integer.getInteger("tcpj.maxephemeralport", 5000 ).intValue();
  	
	debug = Boolean.getBoolean("tcpj.debug");
	avoidhalfcloseserver = Boolean.getBoolean("tcpj.avoidhalfcloseserver");
	avoidackdelay = Boolean.getBoolean("tcpj.avoidackdelay");
	modelpacketloss = Boolean.getBoolean("tcpj.packetloss.model");
  	
	String packetlosslabel = System.getProperty("tcpj.packetloss.rate");

    packetloss = 0.1 ;
    	
	if( packetlosslabel instanceof String ){    
		try {
			packetloss = Double.valueOf( packetlosslabel ).doubleValue();
		} catch ( NumberFormatException e ) {
		}
	}  	
  }

  /**
   * Default constructor
   * @throws SocketException if the underlying <code>DatagramSocket</code> object cannot be opened
   */
  
  public RawTCPSocketImpl() throws SocketException {

    super();
    
    resetSocket();
 
    socket = new JSWDatagramSocket();

    socket.setTypeOfService( IP4.TOS_COMMAND );
    socket.setTimeToLive( 255 );
    if( debug ){
        socket.setDebug( System.out );    
    }
    
    msltimer = new TCPMSLTimer( this , msltimeout );

    retransmissionTimer = new RetransmissionTimer( firstTimeout );
    
    RawTCPListener.getInstance().addPropertyChangeListener( this );

    if( modelpacketloss ){
      random = new Random();
    }
  }
  
  /** 
   Sends a TCP message to the destination and awaits acknowledgement where
   appropriate. Data is broadcast in chunks consistent with the window size
   advertised by the destination. 
  */ 

  synchronized void send( int flags , TCPOptions options , boolean awaitACK ) throws IOException {

	if( ( flags & TCP.PSH ) > 0 ){
	  
	  int sendsize ,
	      count = ( writeend - writestart )% writebuffer.length ;
	      
	  while( count > 0 ){
	  	while( destwindowsize == 0 ){
	  		try {
	  			wait(); // Persist timer
	  		} catch( InterruptedException ie ) {
	  		}
	  	} 
		sendsize = Math.min( count , destwindowsize );
        send( flags , options , writestart , ( writestart + sendsize )% writebuffer.length , awaitACK );
	    writestart += sendsize ;
		count = ( writeend - writestart )% writebuffer.length ;
	  }
	} else {
	  send( flags , options , 0 , 0 , awaitACK );
	}
	
	notifyAll();
  }

  void send( int flags , boolean awaitACK ) throws IOException {
  	send( flags , new TCPOptions() , awaitACK );
  }
  
  void send( int flags ) throws IOException {
  	send( flags , new TCPOptions() , false );
  }
  
  /**
   * Transmits a message, including the data in the interval [start,end) from
   * the write buffer, and awaits acknowledgement if appropriate. 
   * Retransmission will occur where timely acknowledgement isn't received.
   * Updates the estimate of the connection round-trip time.
   */
   
  synchronized void send( int flags , TCPOptions options , int start , int end , boolean awaitACK ) throws IOException {
  	
	int seqnum = localseqnum ;
	
	send( flags , options , start , end , seqnum );
    
    if( ( flags & TCP.SYN ) > 0 || ( flags & TCP.FIN ) > 0 ){
      ++ localseqnum ;
    } else if( ( flags & TCP.PSH ) > 0 ) {
      localseqnum += ( end - start )% writebuffer.length ;
    }  

    if( awaitACK ){

        int sendsize = 1 ;
        
        retransmissionTimer.newPacket();
          	
		try {
		  wait( retransmissionTimer.start() * 1000 );
		  
		  if( acknowledgedseqnum == 0 ){
		  	sendsize = 1 ;
		  } else {
		  	start = ( end - ( localseqnum - acknowledgedseqnum ) )% writebuffer.length ;
		  	sendsize = ( end - start )% writebuffer.length ;
		  	seqnum = acknowledgedseqnum ; 
		  }
		  
          if( acknowledgedseqnum > 0 && sendsize == 0 ){
              retransmissionTimer.stop();
          }

          while( acknowledgedseqnum == 0 || sendsize > 0 ){
              if( retransmissionTimer.timeout() ){
                  break;
              }
              send( flags , options , start , end , seqnum );
              wait( retransmissionTimer.start() * 1000 );

              if( acknowledgedseqnum == 0 ){
                  sendsize = 1 ;
              } else {
                  start = ( end - ( localseqnum - acknowledgedseqnum ) )% writebuffer.length ;
                  sendsize = ( end - start )% writebuffer.length ;
              }	
              
              if( acknowledgedseqnum > 0 && sendsize == 0 ){
                  retransmissionTimer.stop();
              }
          } 
		  
		} catch ( Exception e ) {
		  System.err.println("Exception: " + e.getMessage() );
		}      
  
        if( sendsize > 0 ){
          throw new IOException("Connection reset");
        }

        if( debug ){
            System.err.println( retransmissionTimer.toString() );
        }
    }
  }
  
  /** 
   Transmits a TCP message. Acknowledgement is not awaited.
  */ 

  synchronized void send( int flags , TCPOptions options , int start , int end , int seqnum ) throws IOException {

    if( localhost.equals( ip0 ) ){
      throw new IOException("Local address not yet set");
    } else if( address.equals( ip0 ) ){
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

    if( acknowledger != null && acknowledger.isAlive() ){
      acknowledger.interrupt();
      flags |= TCP.ACK ;
    } else if( destseqnum != 0 ) {
      flags |= TCP.ACK ;
    }

    TCPMessage message = TCP.createMessage( (short) localport ,
                                            (short) port ,
                                            seqnum ,
                                            destseqnum ,
                                            ( flags & TCP.ACK ) > 0 ,
                                            ( flags & TCP.RST ) > 0 ,
                                            ( flags & TCP.SYN ) > 0 ,
                                            ( flags & TCP.FIN ) > 0 ,
                                            ( flags & TCP.PSH ) > 0 ,
                                            (short) windowsize ,
                                            options ,
                                            writebuffer ,  
                                            writestart ,
                                            writeend );
    
    socket.setSourceAddress( localhost.getAddress() );
    socket.setSourcePort( localport );
    socket.send( message , address );
  }

  /**
   * Acknowledges a received TCP message.
   */
  
  public synchronized void acknowledge() throws IOException {

    if( avoidackdelay ){
      send( TCP.ACK );
    } else if( acknowledger != null && acknowledger.isAlive() ) {
      acknowledger.interrupt();
      send( TCP.ACK );
    } else {
      acknowledger = new TCPAcknowledger( this , 200 );
      acknowledger.start();
      
      while( ! acknowledger.isAlive() );
    }
  }

  /**
   * Resets a socket to its default values.
   */
  
  synchronized void resetSocket(){

	address = localhost = ip0 ;
	port = localport = 0 ;
    
    state = previousstate = TCP.CLOSED ; 
    localseqnum = ISNCounter.getCounter();
    destseqnum = 0 ;
    acknowledgedseqnum = 0 ;
    windowsize = maxwindowsize ;
    destwindowsize = 0 ;

    readbuffer = new byte[ maxwindowsize ];
    readoffset = 0 ;
    readcount  = 0 ;
        
    writebuffer = new byte[ maxwindowsize ];
    writestart = 0 ;
    writeend = 0 ;

    acknowledger = null ;
  }

  /**
   * Updates the state variables associated with acknowledgement
   * in order to release waiting send() calls.
   */

  synchronized void updateACK( TCPMessage message ) {

    if( message.ack ){
      acknowledgedseqnum = message.acknowledgementnumber ;
    }

    if( message.syn || message.fin ){
      destseqnum = message.sequencenumber + 1 ;
    } else if( message.psh ) {
      destseqnum = message.sequencenumber + message.data.length ;
    }
      
    destwindowsize = message.windowsize ;
 
    notifyAll();
  }
  
  /**
   * Updates the TCP state in light of a received message.
   */

  synchronized void updateState( TCPMessage message ) throws IOException {

    flush();
        
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
        if( avoidhalfcloseserver ){
			send( TCP.ACK );
			previousstate = state ;
			state = TCP.CLOSE_WAIT ;
			notifyAll();
			return ;
        } else {
		  send( TCP.FIN | TCP.ACK );
		  previousstate = state ;
		  state = TCP.LAST_ACK ;
		  notifyAll();
		  return;
        }
      } else if( message.psh ){
        {
          int i = - 1 ;
          while( ++ i < message.data.length ){
            readbuffer[( readoffset + readcount + i ) % maxwindowsize ] = message.data[( message.datastart + i )% message.data.length ];
          }
        }
        readcount += message.getCount();
        windowsize -= message.getCount();
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
      System.err.println("RST from state: " + TCP.statelabels[ state ] );
      System.err.println( message );
      send( TCP.RST );
    }
    
    resetSocket();

    throw new IOException("Connection reset by peer");
  }
  
  /**
   * Polls until the writebuffer is empty.
   */
  
  synchronized void flush() {
  	try {
  		while( writestart != writeend ){
  			wait();
  		}
  	} catch ( InterruptedException e ) {
  	  System.err.println("flush() " + e.getMessage() );
  	}
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

  synchronized void activeClose() throws IOException {

    switch( state ){

      case TCP.ESTABLISHED:
      case TCP.SYN_RCVD:
        state = TCP.FIN_WAIT_1 ;
        send( TCP.FIN , true );
        break;
      case TCP.SYN_SENT:
        resetSocket();
        break;  
      case TCP.CLOSE_WAIT:
        state = TCP.LAST_ACK ;
        send( TCP.FIN , true );
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

    resetSocket();
    
    socket.close();
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

    if( state != TCP.ESTABLISHED && state != TCP.CLOSE_WAIT ){
      throw new IOException("Write not possible from current state");
    }

    int i = -1 ;
    
    while( ++ i < count ){
      writebuffer[ writeend ++ ] = buffer[ offset ++ ];
      writeend %= writebuffer.length ;
      if( writeend == writestart ){
        throw new IOException("Write buffer overflow");
      }
    }
    
    send( TCP.PSH , true );
  }

  /**
   * Reads a buffer.
   */
  
  public synchronized int read( byte[] buffer , int offset , int count ) throws IOException {

    if( state != TCP.ESTABLISHED && state != TCP.FIN_WAIT_1 && state != TCP.FIN_WAIT_2 ){
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

    if( localhost.getAddress() == null || address.getAddress() == null ){
      return ;
    }
    
    IP4Message ipmessage = (IP4Message) evt.getOldValue();

    if( ! address.equals( ip0 ) ){

        byte[] destinationaddress = address.getAddress();

        if( destinationaddress[ 0 ] != ipmessage.source[ 0 ] || 
            destinationaddress[ 1 ] != ipmessage.source[ 1 ] ||
            destinationaddress[ 2 ] != ipmessage.source[ 2 ] ||
            destinationaddress[ 3 ] != ipmessage.source[ 3 ] ){
            return ;
        }
    }

    if( ! localhost.equals( ip0 ) ){
 
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

    try {
      if( address.equals( ip0 ) ){
      	address = GeneralSocketImpl.createInetAddress( SocketConstants.AF_INET , ipmessage.source );
      }
      if( port == 0 ){
        port = tcpmessage.sourceport ;
      }

	  if( modelpacketloss && random.nextFloat() < packetloss ){
		return ;
	  }

      updateACK( tcpmessage );
      updateState( tcpmessage );
    } catch( Exception e ) {
      System.err.println("propertyChange: " + e.getMessage() );
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

    localhost = inetAddress.equals( ip0 ) ? iplocalhost : inetAddress ;

    if( port != (short) port ){
      throw new IOException("Illegal port number");
//    } else if( port == 0 ) {
//      throw new IOException("Use of port 0 is prohibited");
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
   * Connects to a socket address.
   */
  
  public void connect( SocketAddress address , int timeout ) throws IOException {
  	
  	connect( ((InetSocketAddress) address ).getAddress() , ((InetSocketAddress) address ).getPort() );
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

    send( TCP.SYN , options , true );

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
      	System.err.println("Interrupted Exception in accept()");
      }
    }

    RawTCPSocketImpl tcpjsocket = (RawTCPSocketImpl) newSocket ;

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

    if( ! address.equals( ip0 ) ){
      tcpjsocket.address = GeneralSocketImpl.createInetAddress( SocketConstants.AF_INET , address.getAddress() );
    } else {
      tcpjsocket.address = GeneralSocketImpl.createInetAddress();
    }

    if( ! localhost.equals( ip0 ) ){
      tcpjsocket.localhost = GeneralSocketImpl.createInetAddress( SocketConstants.AF_INET , localhost.getAddress() );
    } else {
      tcpjsocket.localhost = GeneralSocketImpl.createInetAddress();
    }

    port  = 0 ;
    address = GeneralSocketImpl.createInetAddress();
    state = TCP.LISTEN ;
  }

  /**
   * Closes a connection.
   */
  
  public void close() throws IOException {
    activeClose();
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
   * Returns the number of bytes available to be read without polling.
   */
  
  public int available() {
  	return readcount ;
  }

  /**
   * Gets an input stream to read.
   */
  
  public InputStream getInputStream() throws IOException {
    return new RawTCPInputStream( this );
  }

  /**
   * Gets an output stream to write.
   */
  
  public OutputStream getOutputStream() throws IOException {
    return new RawTCPOutputStream( this );
  }
  
  /**
   * Urgent data is not supported.
   */
  
  public void sendUrgentData(int data) throws IOException {
	throw new IOException("Urgent data not supported");
  } 
  
  /**
   * Writes to a string.
   */ 
  
  public String toString() {
    
    StringBuffer sb = new StringBuffer();
    
    int count = 0 ;
    
    if( fd instanceof FileDescriptor ){
      count ++ ;
      sb.append("sd=");
      sb.append( GeneralSocketImpl.getSocketDescriptor( fd ) );
    }
    
    if( localhost instanceof InetAddress ){
      if( count ++ > 0 ){
        sb.append(",");  	
      }
      sb.append("local=");
      sb.append( localhost.toString() );
      sb.append(":");
      sb.append( localport );
    }
    
    if( address instanceof InetAddress ){
      if( count ++ > 0 ){
        sb.append(",");
      }
      sb.append("remote=");
      sb.append( address.toString() );
      sb.append(":");
      sb.append( port );
    }
    
    if( count ++ > 0 ){
    	sb.append(",");
    }
    sb.append("state=");
    sb.append( TCP.statelabels[ state ] );
    
    return sb.toString();    
  }
}
