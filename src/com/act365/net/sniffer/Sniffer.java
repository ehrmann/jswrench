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

		String protocollabel = "RawTCP",
		       excluded = null ;

		int i = 0;

		while( i < args.length) {
          if( args[ i ].equals("-p") && i < args.length - 1 ){
          	protocollabel = args[ ++ i ];
          } else if( args[ i ].equals("-x") && i < args.length - 1 ) {
          	excluded = args[ ++ i ];
          } else {
          	System.err.println("Syntax: Sniffer -p protocol -x excluded");
          	System.exit( 1 );
          }
          ++ i ;
		}
		
		try {

			new SocketWrenchSession();

            SocketUtils.setProtocol( protocollabel );
            
            byte[] excludedaddress = new byte[0];
            
            if( excluded instanceof String ){
            	excludedaddress = InetAddress.getByName( excluded ).getAddress();
            }
            
			byte[] buffer = new byte[512];

			DatagramPacket packet;
  
			DatagramSocket socket = new DatagramSocket();

			IP4Message ipmessage;

			while (true) {

				packet = new DatagramPacket(buffer, buffer.length);

				socket.receive(packet);

				ipmessage =	IP4Reader.read(packet.getData(), 0 , packet.getLength(), true);

                if( excludedaddress.length == 4 &&
                    excludedaddress[0] == ipmessage.source[0] &&
                    excludedaddress[1] == ipmessage.source[1] &&
                    excludedaddress[2] == ipmessage.source[2] &&
                    excludedaddress[3] == ipmessage.source[3] ){
                    	continue;
                }

				System.out.println( ipmessage.toString() );

                int protocol = ipmessage.protocol >= 0 ? ipmessage.protocol : 0xffffff00 ^ ipmessage.protocol ;
                
				switch (protocol) {
					
					case SocketConstants.IPPROTO_UDP :

						UDPMessage udpmessage = UDPReader.read( ipmessage.data,
                                                                ipmessage.dataOffset,
                                                                ipmessage.dataCount,
                                                                true,
                                                                ipmessage.source,
                                                                ipmessage.destination );
                                                                
                        if( udpmessage.sourceport == 53 ){
                        	new DNSReader( 8 ).read( ipmessage.data ).dump(System.out);
                        } else {
                            System.out.println( udpmessage.toString() );
                            SocketUtils.dump( System.out , udpmessage.data , 0 , udpmessage.data.length );
                        }
                        
						break;

					case SocketConstants.IPPROTO_TCP :
					case SocketConstants.IPPROTO_TCPJ :

						TCPMessage tcpmessage = TCPReader.read(	ipmessage.data ,
                                                                ipmessage.dataOffset,
                                                                ipmessage.dataCount,
                                                                true,
                                                                ipmessage.source,
                                                                ipmessage.destination);
                                        

                        System.out.println( tcpmessage.toString () );
                        SocketUtils.dump( System.out , tcpmessage.data , 0 , tcpmessage.data.length );
                        
						break;

					case SocketConstants.IPPROTO_ICMP :
                                            
                        ICMPMessage icmpMessage = ICMPReader.read( ipmessage.data ,
                                                                   ipmessage.dataOffset ,
                                                                   ipmessage.dataCount ,
                                                                   true );
                        
                        System.out.println( icmpMessage.toString() );
                        SocketUtils.dump( System.out , icmpMessage.data , icmpMessage.offset , icmpMessage.count );
                                                                   
                        break;
                          
					default :
					
					    SocketUtils.dump(System.out,packet.getData(),0,packet.getLength());
					    
					    break;
					}
			}
		} catch (Exception e) {
            e.printStackTrace();
			System.err.println(e.getMessage());
		}
	}
}
