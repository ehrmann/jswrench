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
import com.act365.net.ip.*;
import com.act365.net.udp.*;
import com.act365.net.tcp.*;

import java.net.*;

/**
 * Listens for received packets that implement a given protocol and writes
 * them to the screen.
 */

public class Sniffer {

  /**
   * Usage: <code>Sniffer -p protocol</code>
   * <p><code>protocol</code> is an optional argument. When selected, only
   * packets with the given protocol will be displayed. The default is TCP.
   */
  
	public static void main(String[] args) {

		String protocollabel = "TCP";

		int i = 0;

		while( i < args.length) {
          if( args[ i ].equals("-p") && i < args.length - 1 ){
          	protocollabel = args[ ++ i ];
          } else {
          	System.err.println("Syntax: Sniffer -p protocol");
          	System.exit( 1 );
          }
          ++ i ;
		}
		
		try {

			new SocketWrenchSession();

            SocketUtils.setProtocol( protocollabel );
            
			byte[] buffer = new byte[512];

			DatagramPacket packet;
  
			DatagramSocket socket = new DatagramSocket();

			IP4Message ipmessage;

			while (true) {

				packet = new DatagramPacket(buffer, buffer.length);

				socket.receive(packet);

				ipmessage =	IP4Reader.read(packet.getData(), packet.getLength(), true);

				switch (ipmessage.protocol) {
					case SocketConstants.IPPROTO_UDP :

						UDPReader.read(
							ipmessage.data,
							0,
							ipmessage.data.length,
							true,
							ipmessage.source,
							ipmessage.destination);
						break;

					case SocketConstants.IPPROTO_TCP :

						TCPReader.read(
							ipmessage.data,
							0,
							ipmessage.data.length,
							true,
							ipmessage.source,
							ipmessage.destination);
						break;

					case SocketConstants.IPPROTO_ICMP :
					default :
						}

				System.out.println( ipmessage.toString() );

				SocketUtils.dump(
					System.out,
					packet.getData(),
					0,
					packet.getLength());
			}
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}
	}
}
