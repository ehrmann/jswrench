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

import com.act365.net.tcp.*;

import java.io.*;
import java.net.*;

/**
 * 
 * SocketUtils provides various utility functions e.g. checksum calculations,
 * functions to read and write numbers from bytestreams and a bytestream
 * dump printer.
 */

public class SocketUtils {
	
	/**
	 * The protocol in use in the current session.
	 */
    
	static int protocol = 0 ;
	
	static boolean includeheader = false ;
	
	/**
	 * Sets the <code>DatagramSocket</code> factory to be used in the program.
	 * The TCP, TCP/J, UDP and ICMP protocols are supported. TCP/J is a clone
	 * of TCP that uses the IP protocol code 152. The addition of the Raw 
	 * prefix to the protocol name indicates that the user wishes to write his 
	 * own IP headers for the socket. (The option is not supported on Windows).
	 * The <code>Socket</code> and <code>ServerSocket</code> factory objects 
	 * will be set for TCP-based protocols.
	 * 
	 * @param protocol protocol to be used by <code>DatagramSocket</code> objects in the app
	 */
	
	public static void setProtocol( String protocollabel ) throws IOException {
	  if( protocollabel.equals("") ){
	  } else if( protocollabel.equalsIgnoreCase("ICMP") ){
		protocol = SocketConstants.IPPROTO_ICMP ;
		includeheader = false ; 
		DatagramSocket.setDatagramSocketImplFactory( new ICMPDatagramSocketImplFactory() );
	  } else if( protocollabel.equalsIgnoreCase("HdrICMP") ){
		protocol = SocketConstants.IPPROTO_ICMP ;
		includeheader = true ;
		DatagramSocket.setDatagramSocketImplFactory( new HdrICMPDatagramSocketImplFactory() );
	  } else if( protocollabel.equalsIgnoreCase("TCP") ){
		protocol = SocketConstants.IPPROTO_TCP ;
		includeheader = false ;
		Socket.setSocketImplFactory( new TCPSocketImplFactory() );
		ServerSocket.setSocketFactory( new TCPSocketImplFactory() );
	  } else if( protocollabel.equalsIgnoreCase("RawTCP") ){
		protocol = SocketConstants.IPPROTO_TCP ;
		includeheader = false ;
		DatagramSocket.setDatagramSocketImplFactory( new RawTCPDatagramSocketImplFactory() );
		Socket.setSocketImplFactory( new RawTCPSocketImplFactory() );
		ServerSocket.setSocketFactory( new RawTCPSocketImplFactory() );
	  } else if( protocollabel.equalsIgnoreCase("RawHdrTCP") ){
		protocol = SocketConstants.IPPROTO_TCP ;
		includeheader = true ;
		DatagramSocket.setDatagramSocketImplFactory( new RawHdrTCPDatagramSocketImplFactory() );
		Socket.setSocketImplFactory( new RawTCPSocketImplFactory() );
		ServerSocket.setSocketFactory( new RawTCPSocketImplFactory() );
	  } else if( protocollabel.equalsIgnoreCase("RawTCPJ") ){
		protocol = SocketConstants.IPPROTO_TCPJ ;
		includeheader = false ;
		DatagramSocket.setDatagramSocketImplFactory( new RawTCPJDatagramSocketImplFactory() );
		Socket.setSocketImplFactory( new RawTCPSocketImplFactory() );
		ServerSocket.setSocketFactory( new RawTCPSocketImplFactory() );      	
	  } else if( protocollabel.equalsIgnoreCase("RawHdrTCPJ") ){
		protocol = SocketConstants.IPPROTO_TCPJ ;
		includeheader = true ;
		DatagramSocket.setDatagramSocketImplFactory( new RawHdrTCPJDatagramSocketImplFactory() );
		Socket.setSocketImplFactory( new RawTCPSocketImplFactory() );
		ServerSocket.setSocketFactory( new RawTCPSocketImplFactory() );      	
	  } else if( protocollabel.equalsIgnoreCase("UDP") ){
		protocol = SocketConstants.IPPROTO_UDP ;
		includeheader = false ;
		DatagramSocket.setDatagramSocketImplFactory( new UDPDatagramSocketImplFactory() );
	  } else if( protocollabel.equalsIgnoreCase("RawUDP") ){
		protocol = SocketConstants.IPPROTO_UDP ;
		includeheader = false ;
		DatagramSocket.setDatagramSocketImplFactory( new RawUDPDatagramSocketImplFactory() );
	  } else if( protocollabel.equalsIgnoreCase("RawHdrUDP") ){
		protocol = SocketConstants.IPPROTO_UDP ;
		includeheader = true ;
		DatagramSocket.setDatagramSocketImplFactory( new RawHdrUDPDatagramSocketImplFactory() );
	  } else {
		throw new IOException("Protocol" + protocollabel + " is not supported");
	  }
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
	 Standard internet checksum algorithm shared by IP, ICMP, UDP and TCP.
	*/

	public static short checksum( byte[] message , int length , int offset ) {
     
	  // Sum consecutive 16-bit words.

	  int sum = 0 ;

	  while( offset < length - 1 ){

		sum += (int) integralFromBytes( message , offset , 2 );

		offset += 2 ;
	  } 
    
	  if( offset == length - 1 ){

		sum += ( message[offset] >= 0 ? message[offset] : message[offset] ^ 0xffffff00 ) << 8 ;
	  }

	  // Add upper 16 bits to lower 16 bits.

	  sum = ( sum >>> 16 ) + ( sum & 0xffff );

	  // Add carry

	  sum += sum >>> 16 ;

	  // Ones complement and truncate.

	  return (short) ~sum ;
	} 

	/**
	 Specific checksum calculation used for the UDP and TCP pseudo-header.
	*/

	public static short checksum( byte[] source ,
								  byte[] destination ,
								  byte   protocol ,
								  short  length ,
								  byte[] message ,
								  int    offset ) {

	  int bufferlength = length + 12 ;

	  boolean odd = length % 2 == 1 ;

	  if( odd ){
		++ bufferlength ;
	  }

	  byte[] buffer = new byte[ bufferlength ];

	  buffer[0] = source[0];
	  buffer[1] = source[1];
	  buffer[2] = source[2];
	  buffer[3] = source[3];

	  buffer[4] = destination[0];
	  buffer[5] = destination[1];
	  buffer[6] = destination[2];
	  buffer[7] = destination[3];

	  buffer[8] = (byte) 0 ;
	  buffer[9] = protocol ;

	  shortToBytes( length , buffer , 10 );

	  int i = 11 ;

	  while( ++ i < length + 12 ){
		buffer[ i ] = message[ i + offset - 12 ] ;
	  }

	  if( odd ){
		buffer[ i ] = (byte) 0 ;
	  }

	  return checksum( buffer , buffer.length , 0 );
	}

	/**
	 Forms an integral type from consecutive bytes in a buffer
	*/

	static long integralFromBytes( byte[] buffer , int offset , int length ){
   
	  long answer = 0 ;

	  while( -- length >= 0 ) {
		answer = answer << 8 ;
		answer |= buffer[offset] >= 0 ? buffer[offset] : 0xffffff00 ^ buffer[offset] ;
		++ offset ;
	  }

	  return answer ;
	}

	/**
	 Converts consecutive bytes from a buffer into a short.
	*/

	public static short shortFromBytes( byte[] buffer , int offset ) {
	  return (short) integralFromBytes( buffer , offset , 2 );
	}

	/**
	 Converts consecutive bytes from a buffer into an int.
	*/

	public static int intFromBytes( byte[] buffer , int offset ) {
	  return (int) integralFromBytes( buffer , offset , 4 );
	}

	/**
	 Converts consecutive bytes from a buffer into a long.
	*/

	public static long longFromBytes( byte[] buffer , int offset ) {
	  return integralFromBytes( buffer , offset , 8 );
	}

	/**
	 Writes a long into a buffer.
	*/

	public static void longToBytes( long value , byte[] buffer , int offset ) {
	  buffer[ offset + 7 ] = (byte)( value & 0xff ); 
	  value = value >>> 8 ;
	  buffer[ offset + 6 ] = (byte)( value & 0xff ); 
	  value = value >>> 8 ;
	  buffer[ offset + 5 ] = (byte)( value & 0xff ); 
	  value = value >>> 8 ;
	  buffer[ offset + 4 ] = (byte)( value & 0xff ); 
	  value = value >>> 8 ;
	  buffer[ offset + 3 ] = (byte)( value & 0xff ); 
	  value = value >>> 8 ;
	  buffer[ offset + 2 ] = (byte)( value & 0xff ); 
	  value = value >>> 8 ;
	  buffer[ offset + 1 ] = (byte)( value & 0xff ); 
	  value = value >>> 8 ;
	  buffer[ offset ] = (byte)( value ); 
	}

	/**
	 Writes an int into a buffer.
	*/

	public static void intToBytes( int value , byte[] buffer , int offset ){
	  buffer[ offset + 3 ] = (byte)( value & 0xff ); 
	  value = value >> 8 ;
	  buffer[ offset + 2 ] = (byte)( value & 0xff ); 
	  value = value >> 8 ;
	  buffer[ offset + 1 ] = (byte)( value & 0xff ); 
	  value = value >> 8 ;
	  buffer[ offset ] = (byte)( value );
	}

	/**
	 Writes a short into a buffer.
	*/

	public static void shortToBytes( short value , byte[] buffer , int offset ){
	  buffer[ offset + 1 ] = (byte)( value & 0xff ); 
	  value = (short)( value >> 8 );
	  buffer[ offset ] = (byte)( value );
	}

	/**
	 Dumps a buffer in printable form.
	*/

	public static void dump( PrintStream printer , byte[] buffer , int offset , int count ){

      if( count == 0 ){
      	return ;    
      }
      
	  final int upper_bound = offset + ( count / 8 + 1 )* 8 ;

	  int i = offset ;
 
	  StringBuffer str = new StringBuffer();

	  while( i <= upper_bound ){

		int j ;

		String tmpstr ;

		if( ( i - offset ) % 8 == 0 ){

		  if( i > 0 ){

			str.append(' ');
 
			j = i - 9 ;

			while( ++ j < i ){

			  if( j < offset + count ){
				if( buffer[ j ] >= 32 ){
				  try {
					tmpstr =  new String( buffer , j , 1 , "UTF8" );
				  } catch ( UnsupportedEncodingException e ){
					tmpstr = null ;
				  }
				} else {
				  tmpstr = null ;
				}
			  } else {
				tmpstr = " ";
			  }
 
			  if( tmpstr != null ){
				str.append( tmpstr );
			  } else {
				str.append('.');
			  }
			}

			printer.println( str.toString() );
		  }

		  str = new StringBuffer( 38 );

		  tmpstr = Integer.toHexString( i - offset );

		  j = -1 ;

		  while( ++ j < 4 - tmpstr.length() ){
			str.append('0');
		  }

		  str.append( tmpstr );
		}

		str.append(' ');

		if( i < offset + count ){

		  int unsigned = buffer[ i - offset ] >= 0 ? buffer[ i - offset ] : 0xffffff00 ^ buffer[ i - offset ] ;

		  if( unsigned < 16 ){
			str.append('0');
		  }

		  str.append( Integer.toHexString( unsigned ) );
		} else {
		  str.append("  ");
		}

		++ i ;
	  }
	}
}
