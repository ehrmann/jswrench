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

package com.act365.net.ip ;

import com.act365.net.*;

import java.io.IOException ;

/**
 Stores the contents of an IP4 message.
*/

public class IP4Message implements IProtocolMessage {
 
  static short ip4Identifier = 0 ;

  public byte version ;
  public byte headerlength ;
  public byte typeofservice ;
  public short length ;
  public short identifier ;
  public byte flags ;
  public short offset ;
  public short timetolive ;
  public byte protocol ;
  public short checksum ;
  public byte[] source ;
  public byte[] destination ;
  public int[] options ;

  byte[] data ;
  int dataOffset ;
  int dataCount ;

  /**
   * Builds an unpopulated IP4Message object.
   */
  
  public IP4Message() {
  }
  
  /**
   Builds a populated IP4Message object with no options.
  */
  
  public IP4Message( byte typeofservice ,
                     short timetolive ,
                     byte protocol ,
                     byte[] source ,
                     byte[] destination ,
                     byte[] data ,
                     byte[] buffer ,
                     int offset ) throws IOException
  {
    this.version = 4 ;
    this.headerlength = 5 ;
    this.typeofservice = typeofservice ;
    this.length = (short)( 20 + data.length );
    this.identifier = identifier ++ ;
    this.flags = IP4.DONT_FRAGMENT ;
    this.offset = (short) 0 ;
    this.timetolive = timetolive ;
    this.protocol = protocol ;
    this.checksum = 0 ;
    this.source = source ;
    this.destination = destination ;
    this.options = new int[0];
    this.data = data ;
  }
  
  /**
   * Returns the protocol code, e.g. 6 for TCP or 17 for UDP.
   * @return protocol code
   */
    
  public int getProtocol(){
      return SocketConstants.IPPROTO_IP ;
  }
    
  /**
   * Returns the protocol name, e.g. "TCP" or "UDP".
   * @return protocol name
   */
    
  public String getProtocolName(){
      return "IPv4";
  }
    
  /**
   * Indicates whether the protocol will work only with a raw socket.
   */
    
  public boolean isRaw(){
      return true ;
  }
    
  /**
   * Indicates whether the protocol uses port numbers.
   */
    
  public boolean usesPortNumbers(){
      return false ;
  }
    
  /**
   * Returns the port number from which the message has been sent.
   * Protocols that don't use port numbers should return 0.
   */
    
  public int getSourcePort(){
      return 0 ;
  }
  
  /**
   * IP4 has no destination port so the method does nothing.
   */
  
  public void setSourcePort( int port ){  
  }
  
  /**
   * Returns the port number to which the message will be sent.
   * Protocols that don't use port numbers should return 0.
   */
    
  public int getDestinationPort(){
      return 0 ;
  }
  
  /**
   * IP4 has no destination port so the method does nothing.
   */  
  
  public void setDestinationPort( int port ){  
  }
  
  /**
   * Calculates the header length in bytes.
   */
  
  public int headerLength(){
      return 4 * headerlength ;
  }
  
  /**
   Writes the message to a string.
  */

  public String toString() {

    StringBuffer sb = new StringBuffer();

    sb.append("IP4: source-");
    sb.append( source[0] >= 0 ? source[0] : 0xffffff00 ^ source[0] );
    sb.append('.');
    sb.append( source[1] >= 0 ? source[1] : 0xffffff00 ^ source[1] );
    sb.append('.');
    sb.append( source[2] >= 0 ? source[2] : 0xffffff00 ^ source[2] );
    sb.append('.');
    sb.append( source[3] >= 0 ? source[3] : 0xffffff00 ^ source[3] );
    sb.append(" destination-");
    sb.append( destination[0] >= 0 ? destination[0] : 0xffffff00 ^ destination[0] );
    sb.append('.');
    sb.append( destination[1] >= 0 ? destination[1] : 0xffffff00 ^ destination[1] );
    sb.append('.');
    sb.append( destination[2] >= 0 ? destination[2] : 0xffffff00 ^ destination[2] );
    sb.append('.');
    sb.append( destination[3] >= 0 ? destination[3] : 0xffffff00 ^ destination[3] );
    sb.append(" length-");
    sb.append( length );
    sb.append(" bytes");

    return sb.toString();
  } 

  /**
   * Returns the byte-array that stores the optional data segment in the message. 
   */
    
  public byte[] getData(){
      return data ;
  }
    
  /**
   * Returns the offset within the byte-array where the optional data segment starts.
   */

  public int getOffset(){
      return dataOffset ;
  }

  /**
   * Returns the size of the optional data segment.
   */
     
  public int getCount(){
      return dataCount ;
  }
    
  /**
   * Populates an IProtocol message instance according to
   * the contents of a byte-stream. Returns the number of bytes read in order
   * to populate the message. Many protocols implement a checksum safety feature.
   * In principle, the checksum should always be tested, though there might be
   * circumstances (e.g. the Reader might not have access to all of the data used 
   * to calculate the original checksum) where the called might choose to avoid
   * the test. Some protocols, e.g. UDP and TCP , use the source and destination
   * IP addresses, which are stored in the IP header, in order to form the 
   * checksum.
   * 
   * @param buffer - contains the byte-stream
   * @param offset - the position of the first byte to read
   * @param count - the number of bytes available to read
   * @param testchecksum - whether to calculate the checksum
   * @param source - ignored
   * @param destination ignored
   * @return the number of bytes read in order to populate the message
   * @throws IOException cannot construct a message from the buffer contents
   */ 
    
  public int read( byte[] buffer ,
                   int offset , 
                   int count , 
                   boolean testchecksum , 
                   byte[] source , 
                   byte[] destination ) throws IOException {

      if( count < 20 ) {
          throw new IOException("IP4 messages must be at least twenty bytes long");
      }

      if( ( version = (byte)( buffer[ offset ] >>> 4 ) ) != 4 ){
          throw new IOException("Only IP v4 is supported");
      }

      headerlength = (byte)( buffer[ offset ] & 0xf );
      typeofservice = buffer[offset+1];
      length = SocketUtils.shortFromBytes( buffer , offset + 2 );
      identifier = SocketUtils.shortFromBytes( buffer , offset + 4 );
      flags = (byte)( buffer[ offset + 6 ] & 0x60 );

      if( ( flags & IP4.MUST_FRAGMENT ) != 0 ){
          throw new IOException("Fragmentation is not supported");
      }

      offset = (short)( SocketUtils.shortFromBytes( buffer , offset + 6 ) & 0x1fff );
      timetolive = buffer[ offset + 8 ];
      protocol = buffer[ offset + 9 ];
      checksum = SocketUtils.shortFromBytes( buffer , offset + 10 );

      this.source = new byte[4];
      this.source[0] = buffer[ offset + 12 ];
      this.source[1] = buffer[ offset + 13 ];
      this.source[2] = buffer[ offset + 14 ];
      this.source[3] = buffer[ offset + 15 ];

      this.destination = new byte[4];
      this.destination[0] = buffer[ offset + 16 ];
      this.destination[1] = buffer[ offset + 17 ];
      this.destination[2] = buffer[ offset + 18 ];
      this.destination[3] = buffer[ offset + 19 ];

      options = new int[ headerlength - 5 ];

      byte b = 4 ;

      while ( ++ b < headerlength ){
          options[ b - 5 ] = SocketUtils.intFromBytes( buffer , offset + 4 * b );
      } 

      short checksum ;

      if( testchecksum && ( checksum = SocketUtils.checksum( buffer , offset , 4 * headerlength ) ) != 0 ){
          throw new IOException("Checksum error: " + checksum );
      }

      data = buffer ;
      dataOffset = offset + 4 * headerlength ;
      dataCount = length - 4 * headerlength ;
    
      return 4 * headerlength ;
  }

  /**
   * Writes the message into a byte-stream at the given position.
   * 
   * @param buffer 
   * @param offset
   * @param source ignored
   * @param destination ignored
   * @return number of bytes written
   */
    
  public int write( byte[] buffer , 
                    int offset ,
                    byte[] source , 
                    byte[] destination ) throws IOException {
  
      int count ;
      
      try {
          count = simpleWrite( buffer , offset );
      } catch( ArrayIndexOutOfBoundsException e ){
          throw new IOException("IPv4 Write buffer overflow");
      }
    
      /*
       * The checksum is calculated from the IP header alone -
       * the data that follows is ignored. 
       */
     
      checksum = SocketUtils.checksum( buffer , offset , count );
      SocketUtils.shortToBytes( checksum , buffer , offset + 10 );
      
      return count ;
  }
                    
  int simpleWrite( byte[] buffer , int offset ){
                             
      final int count = 4 * headerlength + dataCount ;
    
      buffer[ offset ] = (byte)( version << 4 | headerlength ); 
      buffer[ offset + 1 ] = typeofservice ;

      SocketUtils.shortToBytes( length , buffer , offset + 2 );
      SocketUtils.shortToBytes( identifier , buffer , offset + 4 );
      SocketUtils.shortToBytes( this.offset , buffer , offset + 6 );

      buffer[ offset + 6 ] |= flags ;

      buffer[ offset + 8 ] = (byte) timetolive ;
      buffer[ offset + 9 ] = protocol ;

      SocketUtils.shortToBytes( checksum , buffer , offset + 10 );

      buffer[ offset + 12 ] = source[0];
      buffer[ offset + 13 ] = source[1];
      buffer[ offset + 14 ] = source[2];
      buffer[ offset + 15 ] = source[3];

      buffer[ offset + 16 ] = destination[0];
      buffer[ offset + 17 ] = destination[1];
      buffer[ offset + 18 ] = destination[2];
      buffer[ offset + 19 ] = destination[3];
  
      byte b = 4 ;

      while( ++ b < headerlength ){
          SocketUtils.intToBytes( options[ b - 5 ] , buffer , offset + 4 * b );
      }

      int i = 0 ;
    
      while( i < dataCount ){
          buffer[ offset + i ] = data[ i + dataOffset ];
          ++ i ;
      }
    
      return count ;
  }
}

