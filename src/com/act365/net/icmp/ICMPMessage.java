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

package com.act365.net.icmp ;

import com.act365.net.*;
import com.act365.net.ip.*;

import java.io.IOException ;

/**
 Stores the contents of an ICMP message.
*/

public class ICMPMessage implements IProtocolMessage {
 
  public byte type ;
  public byte code ;
  public short checksum ;
  public short identifier ;
  public short sequence_number ;
  
  byte[] icmpData ;
  int icmpOffset ;
  int icmpCount ;
  
  IP4Message ip4Message ;  

  /**
   * All ICMP messages issued from a single app share a common identifier.
   */
  
  public static short icmpIdentifier ;
  
  /**
   * All ICMP messages issued from a single app are numbered a sequence.
   */
  
  public static short counter ;
  
  /**
   * Creates s blank ICMP message that will be populated by a call to read().
   */
  
  public ICMPMessage(){
      type = -1 ;
      code = -1 ;    
  }

  /**
   * Creates a new ICMP message.
   * @param type ICMP type
   * @param code ICMP code
   * @param data data buffer
   * @param offset offset within buffer at which data starts
   * @param count length of data in bytes
   */  
  
  public ICMPMessage( byte   type ,
                      byte   code ,
                      byte[] data ,
                      int    offset ,
                      int    count ) {

      this.type = type ;
      this.code = code ;
      checksum = 0 ;
      identifier = icmpIdentifier ;
      sequence_number = counter ++ ;
      icmpData = data ;
      icmpOffset = offset ;
      icmpCount = count ;
  }
  
  public int getProtocol(){
      return SocketConstants.IPPROTO_ICMP ;
  }
  
  public String getProtocolName(){
      return "ICMP";
  }
  
  /**
   * ICMP messages have to be sent using raw sockets.
   */

  public boolean isRaw(){
      return true ;
  }
  
  public boolean usesPortNumbers(){
      return false ;
  }
  
  public int getSourcePort(){
      return 0 ;
  }
  
  public void setSourcePort( int port ){
  }
  
  public int getDestinationPort(){
      return 0 ;
  }
  
  public void setDestinationPort( int port ){
  }
  
  public int headerLength(){
      return isQuery() ? 8 : 4 ;
  }
  
  public String toString() {
      
      StringBuffer sb = new StringBuffer();
      
      sb.append("ICMP: ");
      sb.append( getTypeLabel() );
      
      final String codeLabel = getCodeLabel();
      
      if( codeLabel.length() > 0 ){
          sb.append(" (");
          sb.append( codeLabel );
          sb.append(')');
      }
      
      if( isQuery() ){      
          sb.append(" identifier-");
          sb.append( identifier >= 0 ? identifier : identifier ^ 0xffffff00 );
          sb.append(" seq-");
          sb.append( sequence_number >= 0 ? sequence_number : sequence_number ^ 0xffffff00 );
      }

      sb.append(" length-");
      sb.append( icmpCount );
      sb.append(" bytes");
      
      return sb.toString();
  }

  /**
   * Returns the array that contains the message data. 
   */
  
  public byte[] getData(){
      return icmpData ;
  }
  
  /**
   * Returns the length of the message data.
   */
  
  public int getCount(){
      return icmpCount ;
  }
  
  /**
   * Returns the offset of the message data within the data array.
   */
  
  public int getOffset(){
      return icmpOffset ;
  }
  
  public String getTypeLabel(){
      return ICMP.typeLabels[ type ];
  }
  
  public String getCodeLabel(){
      switch( type ){
          case ICMP.ICMP_DEST_UNREACH:
              return ICMP.unreachLabels[ code ];
              
          case ICMP.ICMP_REDIRECT:
              return ICMP.redirectLabels[ code ];
              
          case ICMP.ICMP_TIME_EXCEEDED:
              return ICMP.timeExceededLabels[ code ];
              
          default:
              return "";
      }
  }

  public boolean isQuery(){

      switch( type ){

      case ICMP.ICMP_ECHOREPLY:
      case ICMP.ICMP_ECHO:
      case ICMP.ICMP_ROUTERADVERT:
      case ICMP.ICMP_ROUTERSOLICIT:
      case ICMP.ICMP_TIMESTAMP:
      case ICMP.ICMP_TIMESTAMPREPLY:
      case ICMP.ICMP_INFO_REQUEST:
      case ICMP.ICMP_INFO_REPLY:
      case ICMP.ICMP_ADDRESS:
      case ICMP.ICMP_ADDRESSREPLY:

        return true ;

      case ICMP.ICMP_DEST_UNREACH:
      case ICMP.ICMP_SOURCE_QUENCH:
      case ICMP.ICMP_REDIRECT:
      case ICMP.ICMP_TIME_EXCEEDED:
      case ICMP.ICMP_PARAMETERPROB:
      default:
      
        return false ;
      }        
  }  

  /**
   * Reads an ICMP message from a bytestream.
   * The checksum is optionally tested. The source and destination
   * arguments are ignored because they are not used in the ICMP checksum
   * calculation.
   */
  
  public int read( byte[] buffer , int offset , int count , boolean testchecksum , byte[] source , byte[] destination ) throws IOException {
      
      if( count < 4 ) {
        throw new IOException("ICMP messages must be at least four bytes long");
      }

      short checksum ;

      if( testchecksum && ( checksum = SocketUtils.checksum( buffer , offset , count ) ) != 0 ){
        throw new IOException("Checksum error: " + checksum );
      }

      type = buffer[ offset ];
      code = buffer[ offset + 1 ];
      checksum = SocketUtils.shortFromBytes( buffer , offset + 2 );

      boolean isquery ;

      int datastart ;

      boolean isQuery = isQuery();
     
      if( isQuery ){

          if( count < 8 ) {
            throw new IOException("ICMP query messages must be at least eight bytes long");
          }

          identifier = SocketUtils.shortFromBytes( buffer , offset + 4 );
          sequence_number = SocketUtils.shortFromBytes( buffer , offset + 6 );

          datastart = 8 ;
      } else {
          datastart = 4 ;
      } 
     
      icmpData = buffer ;
      icmpOffset = offset + datastart ;
      icmpCount = count - datastart ;

      if( ! isQuery ){

          int i = 0 ;
     
          while( i < 4 ){
              if( icmpData[ icmpOffset + i ++ ] != 0 ){
                  throw new IOException("ICMP error message lacks zero padding");
              }
          }
         
          ip4Message = new IP4Message();
         
          ip4Message.read( icmpData , icmpOffset + 4 , icmpCount , false , null , null );
      }
     
      return headerLength() + icmpCount ;
  }
    
  /**
   * Writes the message into a byte-stream at the given position.
   * The source and destination arguments will be ignored because they 
   * are not involved in the ICMP checksum calculation.
   * @param buffer buffer into which to write
   * @param offset position within buffer at which to write
   * @param source source IP address (unused)
   * @param destination destination IP address (unused)
   * @return number of bytes written
   */
  
  public int write( byte[] buffer , int offset , byte[] source , byte[] destination ) throws IOException {
  
      int length ;
      
      try {
          length = simpleWrite( buffer , offset );
      } catch( ArrayIndexOutOfBoundsException e ){
          throw new IOException("ICMP Write buffer overflow");
      }
    
      checksum = SocketUtils.checksum( buffer , offset , length );
      SocketUtils.shortToBytes( checksum , buffer , offset + 2 );
       
      return length ;
  }
    
  int simpleWrite( byte[] buffer , int offset ) {
      
      final int length = headerLength() + icmpCount ;
     
      buffer[ offset ]     = type ;
      buffer[ offset + 1 ] = code ;    
      SocketUtils.shortToBytes( checksum , buffer , offset + 2 );
      SocketUtils.shortToBytes( identifier , buffer , offset + 4 );
      SocketUtils.shortToBytes( sequence_number , buffer , offset + 6 );
    
      int i = 0 ;

      while( i < icmpCount ){
        buffer[ offset + 8 + i ] = icmpData[ i + icmpOffset ];
        ++ i ;
      }

      return length ;
  }  
}

