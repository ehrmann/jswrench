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

package com.act365.net.sniffer;

import com.act365.net.*;
import com.act365.net.dns.*;
import com.act365.net.icmp.*;
import com.act365.net.ip.*;
import com.act365.net.tcp.*;
import com.act365.net.udp.*;

import java.io.IOException ;
import java.net.*;

/**
 * Listens for received packets that implement a given protocol and writes
 * them to the screen.
 */

public class Sniffer {

  /**
   * Usage: <code>Sniffer -p protocol -x excluded</code>
   * <p><code>protocol</code> is an optional argument. When selected, only
   * packets with the given protocol will be displayed. The default is TCP.
   * <p><code>excluded</code> is an optional argument that allows messages
   * from a single named source address to be excluded from the output.
   */
  
	public static void main(String[] args) {

		String protocollabel = "TCP",
		       excluded = null ,
               localhost = null ;

		int i = 0;

		while( i < args.length) {
          if( args[ i ].equals("-p") && i < args.length - 1 ){
          	protocollabel = args[ ++ i ];
          } else if( args[ i ].equals("-x") && i < args.length - 1 ) {
          	excluded = args[ ++ i ];
          } else if( args[ i ].equals("-l") && i < args.length - 1 ) {
            localhost = args[ ++ i ];
          } else {
          	System.err.println("Syntax: Sniffer -p protocol -x excluded");
          	System.exit( 1 );
          }
          ++ i ;
		}
		
		try {

			new SocketWrenchSession();
            
            int protocol ;
            
            if( protocollabel.equalsIgnoreCase("TCP") ){
                protocol = SocketConstants.JSWPROTO_RAWTCP ;
            } else if( protocollabel.equalsIgnoreCase("UDP") ){
                protocol = SocketConstants.JSWPROTO_RAWUDP ;
            } else if( protocollabel.equalsIgnoreCase("ICMP") ){
                protocol = SocketConstants.JSWPROTO_ICMP ;
            } else if( protocollabel.equalsIgnoreCase("TCPJ") ){
                protocol = SocketConstants.JSWPROTO_RAWTCPJ ;
            } else {
                throw new IOException("Unsupported protocol");
            }
            
            SocketWrenchSession.setProtocol( protocol );
            
            byte[] excludedaddress = new byte[0];
            
            if( excluded instanceof String ){
            	excludedaddress = InetAddress.getByName( excluded ).getAddress();
            }
            
			JSWDatagramSocket socket = new JSWDatagramSocket();

            if( localhost instanceof String ){
                socket.setSourceAddress( InetAddress.getByName( localhost ).getAddress() );
            } else {
                socket.testChecksum( false ); 
            }
            
			IP4Message ip4Message = new IP4Message();
            ICMPMessage icmpMessage = new ICMPMessage();
            UDPMessage udpMessage = new UDPMessage();
            TCPMessage tcpMessage = new TCPMessage();

			while (true) {

                protocol = socket.receive( ip4Message ,
                                           icmpMessage ,
                                           udpMessage ,
                                           tcpMessage );

                if( excludedaddress.length == 4 &&
                    excludedaddress[0] == ip4Message.source[0] &&
                    excludedaddress[1] == ip4Message.source[1] &&
                    excludedaddress[2] == ip4Message.source[2] &&
                    excludedaddress[3] == ip4Message.source[3] ){
                    	continue;
                }

				System.out.println( ip4Message.toString() );

				switch (protocol) {
					
					case SocketConstants.IPPROTO_UDP :

                        if( udpMessage.sourceport == 53 ){
                            DNSMessage dnsMessage = new DNSMessage();
                        	DNSReader.read( dnsMessage , udpMessage.getData() , udpMessage.getOffset() , udpMessage.getCount() );
                            dnsMessage.dump( System.out );
                        } else {
                            System.out.println( udpMessage.toString() );
                            SocketUtils.dump( System.out , udpMessage.getData() , udpMessage.getOffset() , udpMessage.getCount() );
                        }
                        
						break;

					case SocketConstants.IPPROTO_TCP :
					case SocketConstants.IPPROTO_TCPJ :

                        System.out.println( tcpMessage.toString () );
                        SocketUtils.dump( System.out , tcpMessage.data , tcpMessage.datastart , tcpMessage.dataLength() ); 
                        
						break;

					case SocketConstants.IPPROTO_ICMP :
                                            
                        System.out.println( icmpMessage.toString() );
                        SocketUtils.dump( System.out , icmpMessage.getData() , icmpMessage.getOffset() , icmpMessage.getCount() );
                                                                   
                        break;
                          
					default :
					
                        SocketUtils.dump(System.out,ip4Message.data,ip4Message.dataOffset,ip4Message.dataCount);
					    
					    break;
					}
			}
		} catch (Exception e) {
			System.err.println(e.getMessage());e.printStackTrace();
            
		}
	}
}
