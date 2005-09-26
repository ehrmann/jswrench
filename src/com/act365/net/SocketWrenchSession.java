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

import com.act365.net.tcp.RawTCPSocketImplFactory ;

import java.io.IOException ;
import java.net.*;

/**
 * Manages the housekeeping tasks (mostly on Windows) associated with
 * the startup and the shutdown of the library.
 */

public class SocketWrenchSession {

  /**
   Loads the native library and starts Winsock 2 on Windows.
  */

  static {
    try {
      System.loadLibrary("com_act365_net_Sockets");
      _startup();
    } catch ( UnsatisfiedLinkError ule ){
      String libpath = System.getProperty("java.library.path");
      System.err.println("com_act365_net_Sockets not found on library path " + libpath );
    } catch ( Exception e ) {
      System.err.println( e.getMessage() );
    }
  }

  native static int _startup() throws SocketException ;

  /**
   Releases Winsock 2 resources on Windows.
  */

  protected void finalize() {
      shutdown();
  }

  native static int _shutdown() throws SocketException ;
  
  /**
   * Since the <code>System.runFinalizersOnExit()</code> method
   * has been deprecated, the user should call <code>shutdown()</code>
   * at the end of each app in order to ensure that all resources
   * (especially on Windows) are recovered.
   */
  
  public static void shutdown(){
      try {
        _shutdown();
      } catch ( SocketException e ) {
        System.err.println( e.getMessage() );
      }
  }
  
  /**
   * Removes the local service provider for the given protocol.
   * Typically, the function will be used to disable the local TCP
   * service on Windows (in favour of RawTCP). The function will
   * do nothing on Linux.
   * 
   * @param protocol the protocol to be disabled
   * @return whether the protocol service provider has been successfullly disabled 
   */
  
  static boolean deinstallProvider( int protocol ) throws SocketException {
      return _deinstallProvider( protocol ) > 0 ;
  }
  
  native static int _deinstallProvider( int protocol ) throws SocketException ;
  
  /**
   * The protocol in use in the current session.
   */
    
  static int protocol = 0 ;
    
  static boolean includeheader = false ,
                 isRaw = false ;
    
  /**
   * <p>Sets the factory classes for <code>Socket</code>, <code>ServerSocket</code>
   * and <code>DatagramSocket</code> according to the choice of protocol. 
   * The TCP, TCP/J, UDP and ICMP protocols are supported. (TCP/J is a clone
   * of TCP that uses the IP protocol code 156). The addition of the 'Raw' 
   * prefix to the protocol name indicates that a raw socket is used in the
   * underlying implementation, in which case the user will have to construct
   * the protocol-specific header for transmitted packets. The addition of
   * the 'Hdr' prefix to the protocol indicates that the user wishes to
   * construct the IP header in addition to the protocol-specific header.</p>
   *   
   * <p>The <code>Socket</code> and <code>ServerSocket</code> factory objects 
   * will be set for TCP-based protocols.</p>
   *
   * <p>The calls to <code>deinstallProvider</code> have been commented
   * out because the deinstallation of TCP providers does not enable raw
   * TCP sockets on Windows. Anyone who intends to experiment with the
   * deinstallation of service providers is advised to define a Restore
   * Point (see System Restore under the XP Help and Support menu) beforehand,
   * because the deinstallation involves permanent writes to the registry.
   * In many cases, the writes will be cancelled by the <code>shutdown()</code>
   * call - however, some apps, such as <code>Sniffer</code>, do not call 
   * <code>shutdown()</code>, which means they will effectively leave
   * TCP disabled upon exit.</p>
   *  
   * @param proto protocol to be used in the app
   */
    
  public static void setProtocol( int proto ) throws IOException {
        
      switch( proto ){
            
          case SocketConstants.JSWPROTO_NULL:
              break;
            
          case SocketConstants.JSWPROTO_ICMP:
              protocol = SocketConstants.IPPROTO_ICMP ;
              includeheader = false ;
              isRaw = true ; 
              DatagramSocket.setDatagramSocketImplFactory( new ICMPDatagramSocketImplFactory() );
              break;
                
          case SocketConstants.JSWPROTO_HDRICMP:
              protocol = SocketConstants.IPPROTO_ICMP ;
              includeheader = true ;
              isRaw = true ;
              DatagramSocket.setDatagramSocketImplFactory( new HdrICMPDatagramSocketImplFactory() );
              break;
                
          case SocketConstants.JSWPROTO_JDKTCP:
              protocol = SocketConstants.IPPROTO_TCP ;
              includeheader = false ;
              isRaw = false ;
              break;
                
          case SocketConstants.JSWPROTO_TCP:
              protocol = SocketConstants.IPPROTO_TCP ;
              includeheader = false ;
              isRaw = false ;
              Socket.setSocketImplFactory( new TCPSocketImplFactory() );
              ServerSocket.setSocketFactory( new TCPSocketImplFactory() );
              break;
                
          case SocketConstants.JSWPROTO_RAWTCP:
              protocol = SocketConstants.IPPROTO_TCP ;
              includeheader = false ;
              isRaw = true ;
//              deinstallProvider( SocketConstants.IPPROTO_TCP );
              DatagramSocket.setDatagramSocketImplFactory( new RawTCPDatagramSocketImplFactory() );
              Socket.setSocketImplFactory( new RawTCPSocketImplFactory() );
              ServerSocket.setSocketFactory( new RawTCPSocketImplFactory() );
              break;
                
          case SocketConstants.JSWPROTO_RAWHDRTCP:
              protocol = SocketConstants.IPPROTO_TCP ;
              includeheader = true ;
              isRaw = true ;
//              deinstallProvider( SocketConstants.IPPROTO_TCP );
              DatagramSocket.setDatagramSocketImplFactory( new RawHdrTCPDatagramSocketImplFactory() );
              Socket.setSocketImplFactory( new RawTCPSocketImplFactory() );
              ServerSocket.setSocketFactory( new RawTCPSocketImplFactory() );
              break;
                
          case SocketConstants.JSWPROTO_RAWTCPJ:
              protocol = SocketConstants.IPPROTO_TCPJ ;
              includeheader = false ;
              isRaw = true ;
              DatagramSocket.setDatagramSocketImplFactory( new RawTCPJDatagramSocketImplFactory() );
              Socket.setSocketImplFactory( new RawTCPSocketImplFactory() );
              ServerSocket.setSocketFactory( new RawTCPSocketImplFactory() );
              break;
                
          case SocketConstants.JSWPROTO_RAWHDRTCPJ:
              protocol = SocketConstants.IPPROTO_TCPJ ;
              includeheader = true ;
              isRaw = true ;
              DatagramSocket.setDatagramSocketImplFactory( new RawHdrTCPJDatagramSocketImplFactory() );
              Socket.setSocketImplFactory( new RawTCPSocketImplFactory() );
              ServerSocket.setSocketFactory( new RawTCPSocketImplFactory() );
              break;
                
          case SocketConstants.JSWPROTO_JDKUDP:
              protocol = SocketConstants.IPPROTO_UDP ;
              includeheader = false ;
              isRaw = false ;
              break;
                
          case SocketConstants.JSWPROTO_UDP:
              protocol = SocketConstants.IPPROTO_UDP ;
              includeheader = false ;
              isRaw = false ;
              DatagramSocket.setDatagramSocketImplFactory( new UDPDatagramSocketImplFactory() );
              break;
                
          case SocketConstants.JSWPROTO_RAWUDP:
              protocol = SocketConstants.IPPROTO_UDP ;
              includeheader = false ;
              isRaw = true ;
              DatagramSocket.setDatagramSocketImplFactory( new RawUDPDatagramSocketImplFactory() );
              break;
                
          case SocketConstants.JSWPROTO_RAWHDRUDP:
              protocol = SocketConstants.IPPROTO_UDP ;
              includeheader = true ;
              isRaw = true ;
              DatagramSocket.setDatagramSocketImplFactory( new RawHdrUDPDatagramSocketImplFactory() );
              break;
                
          default:
              throw new IOException("Protocol is not supported");
      }
  }
    
  /**
   * Sets the app protocol by label
   * @see setProtocol(int)
   */

  public static void setProtocol( String protocol ) throws IOException {
      int i = 0 ;
      while( i < SocketConstants.jswProtocolLabels.length ){
          if( protocol.equalsIgnoreCase( SocketConstants.jswProtocolLabels[ i ] ) ){
              setProtocol( i );
              return;
          }
          ++ i ;
      }
      throw new IOException("Protocol " + protocol + " is not supported");
  }
    
  /**
   Returns the protocol associated with the current <code>DatagramSocketImpl</code>.
  */

  public static int getProtocol() {
    return protocol ;
  }
    
  /**
   Indicates whether the chosen protocol requires the user to include the IP header.
  */

  public static boolean includeHeader() {
    return includeheader ;
  } 

  /**
   * Indicates whether the current protocol creates raw sockets.
   */
    
  public static boolean isRaw() {
      return isRaw ;
  }
}

