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

/**
 * 
 * SocketUtils provides various utility functions e.g. checksum calculations,
 * functions to read and write numbers from bytestreams and a bytestream
 * dump printer.
 */

public class SocketUtils {
	
	/**
	 Standard internet checksum algorithm shared by IP, ICMP, UDP and TCP.
	*/

	public static short checksum( byte[] message , int offset , int count ) {
     
	  // Sum consecutive 16-bit words.

	  int sum = 0 ,
          cursor = 0 ;

	  while( cursor < count - 1 ){

		sum += (int) integralFromBytes( message , offset + cursor , 2 );

		cursor += 2 ;
	  } 
    
	  if( cursor == count - 1 ){

		sum += ( message[offset+cursor] >= 0 ? message[offset+cursor] : message[offset+cursor] ^ 0xffffff00 ) << 8 ;
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
								  byte[] message ,
								  int    offset ,
                                  int    count ) {

	  int bufferlength = count + 12 ;

	  boolean odd = count % 2 == 1 ;

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

	  shortToBytes( (short) count  , buffer , 10 );

	  int i = 11 ;

	  while( ++ i < count + 12 ){
		buffer[ i ] = message[ i + offset - 12 ] ;
	  }

	  if( odd ){
		buffer[ i ] = (byte) 0 ;
	  }

	  return checksum( buffer , 0 , buffer.length );
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

	public static int longToBytes( long value , byte[] buffer , int offset ) {
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
	  
	  return offset + 8 ; 
	}

	/**
	 Writes an int into a buffer.
	*/

	public static int intToBytes( int value , byte[] buffer , int offset ){
	  buffer[ offset + 3 ] = (byte)( value & 0xff ); 
	  value = value >> 8 ;
	  buffer[ offset + 2 ] = (byte)( value & 0xff ); 
	  value = value >> 8 ;
	  buffer[ offset + 1 ] = (byte)( value & 0xff ); 
	  value = value >> 8 ;
	  buffer[ offset ] = (byte)( value );
	  
	  return offset + 4 ;
	}

	/**
	 Writes a short into a buffer.
	*/

	public static int shortToBytes( short value , byte[] buffer , int offset ){
	  buffer[ offset + 1 ] = (byte)( value & 0xff ); 
	  value = (short)( value >> 8 );
	  buffer[ offset ] = (byte)( value );
	  
	  return offset + 2 ;
	}

    /**
     * Writes a String to a buffer.
     */

    public static int stringToBytes( String string , byte[] buffer , int offset ){
    
        byte[] data = new byte[0];
        
        try {
          data = string.getBytes("UTF8");
        } catch ( UnsupportedEncodingException e ){	
        }
        
    	return dataToBytes( data , 0 , data.length , buffer , offset );    
    }
    
    /**
     * Writes from one buffer to another. 
     */
     
    public static int dataToBytes( byte[] data , int dataOffset , int dataCount , byte[] buffer , int offset ){
    	
    	int cursor = 0 ;
    	
    	while( cursor < dataCount ){
    		buffer[ offset ++ ] = data[ dataOffset + cursor ++ ];
    	}
    	
    	return offset ;
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

		  if( i > offset ){

			str.append(' ');
 
			j = i - 9 ;

			while( ++ j < i ){

			  if( j < offset + count ){
				if( buffer[ j % buffer.length ] >= 32 ){
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

		  int unsigned = buffer[ i % buffer.length ] >= 0 ? buffer[ i % buffer.length ] : 0xffffff00 ^ buffer[ i % buffer.length ] ;

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
    
    /**
     * Creates a blank message of the type appropriate for the 
     * globally-selected protocol.
     */

    public static IProtocolMessage createMessage(){
    
        switch( SocketWrenchSession.getProtocol() ){
            case SocketConstants.IPPROTO_UDP:
                return new com.act365.net.udp.UDPMessage();
            case SocketConstants.IPPROTO_ICMP:
                return new com.act365.net.icmp.ICMPMessage();
            case SocketConstants.IPPROTO_TCP:
                return new com.act365.net.tcp.TCPMessage();
            case SocketConstants.IPPROTO_TCPJ:
                return new com.act365.net.tcp.TCPJMessage();
            default:
                return null ;
        }
    }   
}
