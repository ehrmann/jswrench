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

import com.act365.net.* ;

import java.io.IOException ;

/**
 * Stores a TCP message.
 */

public class TCPMessage implements IProtocolMessage {

  public short sourceport ;
  public short destinationport ;
  public int sequencenumber ;
  public int acknowledgementnumber ;
  public int headerlength ;
  public boolean urg ;
  public boolean ack ;
  public boolean psh ;
  public boolean rst ;
  public boolean syn ;
  public boolean fin ;
  public short windowsize ;
  public short checksum ;
  public short urgentpointer ;
  public byte[] options = new byte[ 0 ];
  public byte[] data = new byte[ 0 ]; 
  public int datastart ;
  public int dataend ;
  
  /**
   * Creates a blank TCPMessage that will be populated by a call to read().
   */
  
  public TCPMessage(){
  }
  
  /**
   * Creates a populated TCPMessage.
   */
  
  public TCPMessage( short sourceport ,
                     short destinationport ,
                     int sequencenumber ,
                     int acknowledgementnumber ,
                     boolean ack ,
                     boolean rst ,
                     boolean syn ,
                     boolean fin ,
                     boolean psh ,
                     short windowsize ,
                     TCPOptions options ,
                     byte[] writebuffer ,
                     int writestart ,
                     int writeend ) {
  
    this.sourceport = sourceport ;
    this.destinationport = destinationport ;
    this.sequencenumber = sequencenumber ;
    this.acknowledgementnumber = acknowledgementnumber ;
    headerlength = 5 ;
    urg = false ;
    this.ack = ack ;
    this.psh = psh ;
    this.rst = rst ;
    this.syn = syn ;
    this.fin = fin ;
    this.windowsize = windowsize ; 
    checksum = 0 ;
    urgentpointer = 0 ;

    byte[] optionsbuffer = new byte[ 128 ];

    int cursor = 0 ;

    if( options.isMaxSegmentSizeSet() ){
      optionsbuffer[ cursor ] = 2 ;
      optionsbuffer[ cursor + 1 ] = 4 ;
      SocketUtils.shortToBytes( options.getMaxSegmentSize() , optionsbuffer , cursor + 2 );
      cursor += 4 ;
    }
 
    if( options.isWindowScaleFactorSet() ){
      optionsbuffer[ cursor ] = 1 ;
      optionsbuffer[ cursor + 1 ] = 3 ;
      optionsbuffer[ cursor + 2 ] = 3 ;
      optionsbuffer[ cursor + 3 ] = options.getWindowScaleFactor() ;
      cursor += 4 ;
    }

    if( options.isTimestampSet() ){
      optionsbuffer[ cursor ] = 1 ;
      optionsbuffer[ cursor + 1 ] = 1 ;
      optionsbuffer[ cursor + 2 ] = 8 ;
      optionsbuffer[ cursor + 3 ] = 10 ;
      SocketUtils.intToBytes( options.getTimestampValue() , optionsbuffer , cursor + 4 );
      SocketUtils.intToBytes( options.getTimestampEchoReply() , optionsbuffer , cursor + 8 );
      cursor += 12 ;
    }

    headerlength += cursor / 4 ; 
    this.options = new byte[ cursor ];

    int i = -1 ;

    while( ++ i < cursor ){
      this.options[ i ] = optionsbuffer[ i ];
    }

    data = writebuffer ;
    datastart = writestart ;
    dataend = writeend ;
  }

  /**
   * Returns the protocol code, e.g. 6 for TCP or 17 for UDP.
   * @return protocol code
   */
    
  public int getProtocol(){
      return SocketConstants.IPPROTO_TCP ;
  }
    
  /**
   * Returns the protocol name, e.g. "TCP" or "UDP".
   * @return protocol name
   */
    
  public String getProtocolName(){
      return "TCP" ;
  }
    
  /**
   * Indicates whether the protocol will work only with a raw socket.
   */
    
  public boolean isRaw(){
      return false ;
  }
    
  /**
   * Indicates whether the protocol uses port numbers.
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
  
  public void setSourcePort( int port ){
      sourceport = (short) port ;  
  }
  
  /**
   * Returns the port number to which the message will be sent.
   * Protocols that don't use port numbers should return 0.
   */
    
  public int getDestinationPort(){
      return destinationport >= 0 ? destinationport : 0xffffff00 ^ destinationport ;
  }
  
  /**
   * Sets the destination port.
   */
  
  public void setDestinationPort( int port ){
      destinationport = (short) port ;  
  }
  
  /**
   * Calculates the header length in bytes.
   */
  
  public int headerLength() {
      return 4 * headerlength ;
  }
  
  /**
   * Writes a textual description of the message. 
   * @return textual description
   */
    
  public String toString() {
  	
  	StringBuffer sb = new StringBuffer();
  	
  	sb.append("TCP: ");
  	
  	sb.append( sourceport >= 0 ? sourceport : sourceport ^ 0xffffff00 );
  	sb.append("-");
  	sb.append( destinationport >= 0 ? destinationport : destinationport ^ 0xffffff00 );
  	sb.append(" ");
  	
  	int count = 0 ;
  	
  	if( urg ){
  		count ++ ;
  		sb.append("URG");  		
  	}
  	
	if( psh ){
		if( count ++ > 0 ){
			sb.append(",");
		}
		sb.append("PSH");
	}
  	
	if( rst ){
		if( count ++ > 0 ){
			sb.append(",");
		}
		sb.append("RST");
	}
  	
	if( syn ){
		if( count ++ > 0 ){
			sb.append(",");
		}
		sb.append("SYN");
	}
  	
	if( fin ){
		if( count ++ > 0 ){
			sb.append(",");
		}
		sb.append("FIN");
	}
  	
	if( ack ){
		if( count ++ > 0 ){
			sb.append(",");
		}
		sb.append("ACK");
	}

    int unsigned ;
      	
	sb.append(" seq-");

	unsigned = sequencenumber >= 0 ? sequencenumber : 0xffffff00 ^ sequencenumber ;

	if( unsigned < 16 ){
	  sb.append('0');
	}
	
	sb.append( Integer.toHexString( unsigned ) );
	
	sb.append(" ack-");

	unsigned = acknowledgementnumber >= 0 ? acknowledgementnumber : 0xffffff00 ^ acknowledgementnumber ;

	if( unsigned < 16 ){
	  sb.append('0');
	}
	
	sb.append( Integer.toHexString( unsigned ) );
  	
	sb.append(" window-");
	sb.append( windowsize );
      	
    sb.append(" length-");
    sb.append( getCount() );
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
      return datastart ;
  }

  /**
   * Returns the size of the optional data segment.
   */
     
  public int getCount(){
      return data.length > 0 ? ( dataend - datastart )% data.length : 0 ;      
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
   * @param source - source IP address
   * @param destination destination IP address
   * @return the number of bytes read in order to populate the message
   * @throws IOException cannot construct a message from the buffer contents
   */ 
    
  public int read( byte[] buffer ,
                   int offset , 
                   int count , 
                   boolean testchecksum , 
                   byte[] source , 
                   byte[] destination ) throws IOException {

      if( SocketWrenchSession.isRaw() ){
      
          sourceport = SocketUtils.shortFromBytes( buffer , offset );
          destinationport = SocketUtils.shortFromBytes( buffer , offset + 2 );
          sequencenumber = SocketUtils.intFromBytes( buffer , offset + 4 );
          acknowledgementnumber = SocketUtils.intFromBytes( buffer , offset + 8 );
    
          headerlength = ( buffer[ offset + 12 ] >= 0 ? buffer[ offset + 12 ] : 0xffffff00 ^ buffer[ offset + 12 ] ) >> 4 ;
          urg = ( buffer[ offset + 13 ] & TCP.URG ) != 0 ;
          ack = ( buffer[ offset + 13 ] & TCP.ACK ) != 0 ;
          psh = ( buffer[ offset + 13 ] & TCP.PSH ) != 0 ;
          rst = ( buffer[ offset + 13 ] & TCP.RST ) != 0 ;
          syn = ( buffer[ offset + 13 ] & TCP.SYN ) != 0 ;
          fin = ( buffer[ offset + 13 ] & TCP.FIN ) != 0 ;
          windowsize = SocketUtils.shortFromBytes( buffer , offset + 14 );
          checksum = SocketUtils.shortFromBytes( buffer , offset + 16 );
          urgentpointer = SocketUtils.shortFromBytes( buffer , offset + 18 );
   
          if( count < 4 * headerlength ){
              throw new IOException("TCP message should be longer");
          }

          options = new byte[ 4 * headerlength - 20 ];

          int i = offset + 19 ;

          while( ++ i < offset + 4 * headerlength ){
              options[ i - offset - 20 ] = buffer[ i ];
          }

          data = buffer ;
          datastart = i ;
          dataend = ( offset + count )% buffer.length ;

          if( testchecksum ){

              short checksum = SocketUtils.checksum( source ,
                                                     destination ,
                                                     (byte) getProtocol() ,
                                                     buffer ,
                                                     offset ,
                                                     count ); 

              if( checksum != 0 ){
                  throw new IOException("Checksum error: " + checksum );
              }
          }

          return 4 * headerlength + getCount();
      } else {
          data = buffer ;
          datastart = offset ;
          dataend = ( offset + count )% buffer.length ;
          
          return count ;              
      }
  }

  /**
   * Writes the message into a byte-stream at the given position.
   * 
   * @param buffer 
   * @param offset
   * @param source source IP address
   * @param destination destination IP address
   * @return number of bytes written
   */
    
  public int write( byte[] buffer , int offset , byte[] source , byte[] destination ) throws IOException {
  
      int length ;
      
      try {
          length = simpleWrite( buffer , offset );
      } catch( ArrayIndexOutOfBoundsException e ){
          throw new IOException("TCP Write buffer overflow");
      }
    
      if( SocketWrenchSession.isRaw() ){
          checksum = SocketUtils.checksum( source , destination , (byte) getProtocol() , buffer , offset , length );
          SocketUtils.shortToBytes( checksum , buffer , offset + 16 );
      }
      
      return length ;
  }
  
  int simpleWrite( byte[] buffer , int offset ){

      final int length = SocketWrenchSession.isRaw() ? 4 * headerlength + getCount() : getCount() ;

      int i = 0 ,
          dataoffset = datastart ;
      
      if( SocketWrenchSession.isRaw() ){
          
          SocketUtils.shortToBytes( sourceport , buffer , offset );
          SocketUtils.shortToBytes( destinationport , buffer , offset + 2 );
          SocketUtils.intToBytes( sequencenumber , buffer , offset + 4 );
          SocketUtils.intToBytes( acknowledgementnumber , buffer , offset + 8 );
    
          buffer[ offset + 12 ] = (byte)( headerlength << 4 );
          buffer[ offset + 13 ] = 0 ;

          if( urg ) buffer[ offset + 13 ] |= TCP.URG ;
          if( ack ) buffer[ offset + 13 ] |= TCP.ACK ;
          if( psh ) buffer[ offset + 13 ] |= TCP.PSH ;
          if( rst ) buffer[ offset + 13 ] |= TCP.RST ;
          if( syn ) buffer[ offset + 13 ] |= TCP.SYN ;
          if( fin ) buffer[ offset + 13 ] |= TCP.FIN ;

          SocketUtils.shortToBytes( windowsize , buffer , offset + 14 );
          SocketUtils.shortToBytes( checksum , buffer , offset + 16 );
          SocketUtils.shortToBytes( urgentpointer , buffer , offset + 18 );

          i = 20 ;

          while( i < 20 + options.length ){
              buffer[ offset + i ] = options[ i - 20 ];
              ++ i ;
          }
          
          dataoffset -= i ;
      }

      while( i < length ){
        buffer[ offset + i ] = data[( dataoffset + i )% data.length ];
        ++ i ;
      }

      return length ;
  }
}

