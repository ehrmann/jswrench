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

package com.act365.net.udp ;

import com.act365.net.*;

import java.io.IOException ;

/**
 Objects of the class UDPMessage store UDP messages.
*/

public class UDPMessage implements IProtocolMessage {

  public short sourceport ;
  public short destinationport ;
  public short checksum ;
  
  byte[] data ;
  int offset ;
  int count ;

  /**
   * Creates a blank UDP message that will be populated by read().
   */
  
  public UDPMessage(){
  }
  
  /**
   * Creates a UDP message.
   * @param sourceport the UDP port from which the message has been sent
   * @param destinationport the UDP port to which the message will be sent
   * @param data array that contains data segment
   * @param offset offset of data segment within data array
   * @param count length of data segment
   */
  
  public UDPMessage( short  sourceport ,
                     short  destinationport ,
                     byte[] data ,
                     int    offset ,
                     int    count ){
      this.sourceport = sourceport ;
      this.destinationport = destinationport ;
      this.checksum = 0 ;
      this.data = data ;
      this.offset = offset ;
      this.count = count ;
  }
  
  /**
   * Returns 17, the UDP protocol code.
   */
    
  public int getProtocol(){
      return SocketConstants.IPPROTO_UDP ;
  }
    
  /**
   * Returns "UDP", the protocol name.
   */ 
    
  public String getProtocolName(){
      return "UDP";
  }
    
  /**
   * UDP messages work with raw and standard sockets.
   */
    
  public boolean isRaw(){
      return false ;
  }
  
  /**
   * UDP messages use port numbers.
   */  
  
  public boolean usesPortNumbers(){
      return true ;
  }
  
  /**
   * Returns the port number from which the message has been sent.
   * Protocols that don't use port numbers should return 0.
   */
    
  public int getSourcePort(){
      return sourceport >= 0 ? sourceport : 0xffffff00 ^ sourceport ;
  }
  
  /**
   * Sets the source port.
   */
  
  public void setSourcePort( int sourcePort ){
      sourceport = (short) sourcePort ;  
  }
  
  /**
   * Returns the destination port.
   */
  
  public int getDestinationPort(){
      return destinationport >= 0 ? destinationport : destinationport ^ 0xffffff00 ;
  }
  
  /**
   * Sets the destination port.
   */
  
  public void setDestinationPort( int destinationPort ){
      destinationport = (short) destinationPort ;
  }
  
  /**
   Writes the message to a string.
  */

  public String toString() {

    StringBuffer sb = new StringBuffer();

    sb.append("UDP: source port-");
    sb.append( sourceport >= 0 ? sourceport : sourceport ^ 0xffffff00 );
    sb.append(" destination port-");
    sb.append( destinationport >= 0 ? destinationport : destinationport ^ 0xffffff00 );
    sb.append(" length-");
    sb.append( count );
    sb.append(" bytes");

    return sb.toString();
  }  
  
  /**
   * Calculates the header length in bytes.
   */
  
  public int headerLength() {
      return 8 ;
  }
  
  /**
   * Returns the array that contains the message data. 
   */
  
  public byte[] getData(){
      return data ;
  }
  
  /**
   * Returns the length of the message data.
   */
  
  public int getCount(){
      return count ;
  }
  
  /**
   * Returns the offset of the message data within the data array.
   */
  
  public int getOffset(){
      return offset ;
  }
      
  /**
   * Populates an IProtocol message instance according to
   * the contents of a byte-stream. Returns the number of bytes read in order
   * to populate the message. Many protocols implement a checksum safety feature.
   * In principle, the checksum should always be tested, though there might be
   * circumstances (e.g. the Reader might not have access to all of the data used 
   * to calculate the original checksum) where the called might choose to avoid
   * the test.
   * 
   * @param buffer - contains the byte-stream
   * @param offset - the position of the first byte to read
   * @param count - the number of bytes available to read
   * @param testchecksum - whether to calculate the checksum
   * @param source IP address of source
   * @param destination IP address of destination
   * @return the number of bytes read in order to populate the message
   * @throws IOException cannot construct a message from the buffer contents
   */
    
  public int read( byte[] buffer , int offset , int count , boolean testchecksum , byte[] source , byte[] destination ) throws IOException {

      if( SocketWrenchSession.isRaw() ){      
          if( count < 8 ){
              throw new IOException("UDP messages must be at least eight bytes long");
          }
          sourceport = SocketUtils.shortFromBytes( buffer , offset );
          destinationport = SocketUtils.shortFromBytes( buffer , offset + 2 );
          this.count = SocketUtils.shortFromBytes( buffer , offset + 4 ) - 8 ;
          checksum = SocketUtils.shortFromBytes( buffer , offset + 6 );
          data = buffer ;
          this.offset = offset + 8 ;
          if( testchecksum ){
              if( source == null || destination == null ){
                  throw new IOException("Cannot test checksum unless source and destination IP addresses are known");
              }
              short checksum = SocketUtils.checksum( source ,
                                                     destination ,
                                                     (byte) SocketConstants.IPPROTO_UDP ,
                                                     buffer ,
                                                     offset ,
                                                     count );

              if( checksum != 0 ){
                  throw new IOException("Checksum error: " + checksum );
              }
          }
          return this.count + 8 ;
      } else {
          data = buffer ;
          this.offset = offset ;
          this.count = count ;
          return count ;
      }
  }

  /**
   * Writes the message into a byte-stream at the given position.
   * @param buffer buffer into which the message is to be written
   * @param offset position at which the message will be written
   * @param source source IP address
   * @param destination destination IP address
   * @return number of bytes written
   */
    
  public int write( byte[] buffer , int offset , byte[] source , byte[] destination ) throws IOException {
  
      int length ;
      
      try {
          length = simpleWrite( buffer , offset );
      } catch( ArrayIndexOutOfBoundsException e ){
          throw new IOException("UDP Write buffer overflow");
      }
    
      if( SocketWrenchSession.isRaw() ){
          checksum = SocketUtils.checksum( source , destination , (byte) SocketConstants.IPPROTO_UDP , buffer , offset , length );
          SocketUtils.shortToBytes( checksum , buffer , offset + 6 );
      }
      
      return length ;
  }

  int simpleWrite( byte[] buffer , int offset ) {

    final short length = (short)( count + 8 );
    
    if( SocketWrenchSession.isRaw() ){
        SocketUtils.shortToBytes( sourceport , buffer , offset );
        SocketUtils.shortToBytes( destinationport , buffer , offset + 2 );
        SocketUtils.shortToBytes( length , buffer , offset + 4 );
        SocketUtils.shortToBytes( checksum , buffer , offset + 6 );
        
        offset += 8 ;
    }
    
    int i = 0 ;

    while( i < count ){
      buffer[ offset + i ] = data[ this.offset + i ];
      ++ i ;
    }

    return SocketWrenchSession.isRaw() ? length : count ;
  }
}

