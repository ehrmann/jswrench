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

import com.act365.net.icmp.*;
import com.act365.net.ip.* ;
import com.act365.net.tcp.* ;
import com.act365.net.udp.* ;

import java.io.* ;
import java.net.* ;

public class JSWDatagramSocket extends DatagramSocket {

    final static int ip4HeaderLength = 20 ,
                     udpHeaderLength = 8 ,
                     icmpHeaderLength = 8 ; 
             
    int sendBufferSize ,
        receiveBufferSize ,
        typeOfService ,
        timeToLive ;
               
    byte[] sendBuffer ,
           receiveBuffer ,
           sourceAddress ;
                       
    protected JSWDatagramSocket() throws SocketException {
        super();
        
        timeToLive = 255 ;
        typeOfService = IP4.TOS_DATA ;
        
        sendBufferSize = 1024 ;
        receiveBufferSize = 1024 ;
        
        sendBuffer = new byte[ sendBufferSize ];
        receiveBuffer = new byte[ receiveBufferSize ];
        
        try {
            sourceAddress = InetAddress.getLocalHost().getAddress();
        } catch ( UnknownHostException e ) {
            sourceAddress = new byte[] { 127 , 0 , 0 , 1 };
        }
    }

    /**
     * Sends a UDP message.
     * 
     * @param buffer
     * @param offset
     * @param count
     * @param srcPort
     * @param destAddress
     * @param destPort
     * @throws IOException
     */
    
    public void send( int srcPort ,
                      byte[] destAddress , 
                      int destPort ,
                      byte[] buffer ,
                      int offset ,
                      int count ) throws IOException {
    
        if( ! SocketWrenchSession.isRaw() && 
            SocketWrenchSession.getProtocol() != SocketConstants.IPPROTO_UDP ){
            throw new IOException("Sending of UDP messages incompatible with selected protocol");
        }
        
        int cursor = 0 ;
        
        if( SocketWrenchSession.includeHeader() ){

            cursor += IP4Writer.write( (byte) typeOfService ,
                                       (short) timeToLive ,
                                       (byte) SocketConstants.IPPROTO_UDP ,
                                       sourceAddress ,
                                       destAddress ,
                                       new byte[0] ,
                                       sendBuffer ,
                                       cursor );                                                   
        }
        
        if( SocketWrenchSession.isRaw()){
            
            cursor += UDPWriter.write( sourceAddress ,
                                       (byte) srcPort ,
                                       destAddress ,
                                       (short)  destPort ,
                                        buffer ,
                                        offset ,
                                        count ,
                                        sendBuffer ,
                                        cursor );
            
        } else {
            
            try {
                int i = 0 ;
            
                while( i < count ){
                    sendBuffer[ cursor ++ ] = buffer[ offset + i ++ ]; 
                }
                
            } catch ( ArrayIndexOutOfBoundsException e ) {                
                throw new IOException("UDP Write buffer overflow");
            }
        }
        
        send( new DatagramPacket( sendBuffer , cursor , GeneralSocketImpl.createInetAddress( SocketConstants.AF_INET , destAddress ) , destPort ) );
    }

    public void send( int identifier ,
                      int type ,
                      int code ,
                      byte[] data ,
                      int dataOffset ,
                      int dataCount ,
                      byte[] destAddress ) throws IOException {
    
        if( ! SocketWrenchSession.isRaw() ){
            throw new IOException("Sending of ICMP messages incompatible with selected protocol");
        }
        
        int cursor = 0 ;
        
        if( SocketWrenchSession.includeHeader() ){

            cursor += IP4Writer.write( (byte) typeOfService ,
                                       (short) timeToLive ,
                                       (byte) SocketConstants.IPPROTO_ICMP ,
                                       sourceAddress ,
                                       destAddress ,
                                       new byte[0] ,
                                       sendBuffer ,
                                       cursor );                                                   
        }

        cursor += new ICMPWriter( (short) identifier ).write( (byte) type , (byte) code , data , dataOffset , dataCount , sendBuffer , cursor );
                        
        send( new DatagramPacket( sendBuffer , cursor , GeneralSocketImpl.createInetAddress( SocketConstants.AF_INET , destAddress ) , 0 ) );
    }

    public void send( int sourcePort ,
                      byte[] destAddress ,
                      int destPort ,
                      int seqNumber ,
                      int ackNumber ,
                      boolean ack ,
                      boolean rst ,
                      boolean syn ,
                      boolean fin ,
                      boolean psh ,
                      int windowSize ,
                      TCPOptions options ,
                      byte[] writeBuffer ,
                      int writeStart ,
                      int writeEnd ) throws IOException {
                                
      if( ! SocketWrenchSession.isRaw() && 
            SocketWrenchSession.getProtocol() != SocketConstants.IPPROTO_TCP ){
            throw new IOException("Sending of TCP messages incompatible with selected protocol");
      }
        
      int cursor = 0 ;
        
      if( SocketWrenchSession.includeHeader() ){

          cursor += IP4Writer.write( (byte) typeOfService ,
                                     (short) timeToLive ,
                                     (byte) SocketConstants.IPPROTO_UDP ,
                                     sourceAddress ,
                                     destAddress ,
                                     new byte[0] ,
                                     sendBuffer ,
                                     cursor );                                                   
      }
        
      if( SocketWrenchSession.isRaw()){
                      
          cursor += TCPWriter.write( sourceAddress ,
                                     (short) sourcePort ,
                                     destAddress ,
                                     (short) destPort ,
                                     seqNumber ,
                                     ackNumber ,
                                     ack ,
                                     rst ,
                                     syn ,
                                     fin ,
                                     psh ,
                                     (short) windowSize ,
                                     options ,
                                     writeBuffer ,
                                     writeStart ,
                                     writeEnd ,
                                     sendBuffer ,
                                     cursor );
      } else {
            
          try {
 
              int i = 0 ,
                  count = ( writeEnd - writeStart )% writeBuffer.length ;
            
              while( i < count ){
                  sendBuffer[ cursor ++ ] = writeBuffer[( writeStart + i ++ )% writeBuffer.length ]; 
              }
                
          } catch ( ArrayIndexOutOfBoundsException e ) {                
              throw new IOException("UDP Write buffer overflow");
          }
      }
        
      send( new DatagramPacket( sendBuffer , cursor , GeneralSocketImpl.createInetAddress( SocketConstants.AF_INET , destAddress ) , destPort ) );
    }
                             
    public int receive( IP4Message ip4Message ,
                        UDPMessage udpMessage ) throws IOException {
    
        DatagramPacket dgram = new DatagramPacket( receiveBuffer , receiveBuffer.length );
        
        receive( dgram );
        
        int cursor = 0 ,
            length = dgram.getLength() ,
            size ;
        
        if( SocketWrenchSession.isRaw() && ip4Message instanceof IP4Message ){
            size = IP4Reader.read( ip4Message , receiveBuffer , cursor , length , true );
        } else {
            size = ip4HeaderLength ;
        }
    
        cursor += size ;
        length -= size ;
        
        size = UDPReader.read( udpMessage , receiveBuffer , cursor , length , true , sourceAddress , dgram.getAddress().getAddress() );
        
        cursor += size ;
        length -= size ;
        
        if( cursor != dgram.getLength() || length != 0 ){
            throw new IOException("Illegal UDP message format");
        }
        
        return SocketConstants.IPPROTO_UDP ;
    }
    
    public int receive( IP4Message ip4Message ,
                        ICMPMessage icmpMessage ) throws IOException {
    
        DatagramPacket dgram = new DatagramPacket( receiveBuffer , receiveBuffer.length );
        
        receive( dgram );
        
        int cursor = 0 ,
            length = dgram.getLength() ,
            size ;
        
        if( SocketWrenchSession.isRaw() && ip4Message instanceof IP4Message ){
            size = IP4Reader.read( ip4Message , receiveBuffer , cursor , length , true );
        } else {
            size = ip4HeaderLength ;
        }
    
        cursor += size ;
        length -= size ;
        
        size = ICMPReader.read( icmpMessage , receiveBuffer , cursor , length , true );
        
        cursor += size ;
        length -= size ;
        
        if( cursor != dgram.getLength() || length != 0 ){
            throw new IOException("Illegal ICMP message format");
        }
        
        return SocketConstants.IPPROTO_ICMP ;
    }
    
    public int receive( IP4Message ip4Message ,
                        TCPMessage udpMessage ) throws IOException {
    
        DatagramPacket dgram = new DatagramPacket( receiveBuffer , receiveBuffer.length );
        
        receive( dgram );
        
        int cursor = 0 ,
            length = dgram.getLength() ,
            size ;
        
        if( SocketWrenchSession.isRaw() && ip4Message instanceof IP4Message ){
            size = IP4Reader.read( ip4Message , receiveBuffer , cursor , length , true );
        } else {
            size = ip4HeaderLength ;
        }
    
        cursor += size ;
        length -= size ;
        
        size = TCPReader.read( udpMessage , receiveBuffer , cursor , length , true , sourceAddress , dgram.getAddress().getAddress() );
        
        cursor += size ;
        length -= size ;
        
        if( cursor != dgram.getLength() || length != 0 ){
            throw new IOException("Illegal TCP message format");
        }
        
        return SocketConstants.IPPROTO_TCP ;
    }
    
    public int receive( IP4Message ip4Message ,
                        ICMPMessage icmpMessage ,
                        UDPMessage udpMessage ,
                        TCPMessage tcpMessage ) throws IOException {

        if( ! SocketWrenchSession.isRaw() ){
            throw new IOException("Invalid read for non-raw sockets");
        } 

        DatagramPacket dgram = new DatagramPacket( receiveBuffer , receiveBuffer.length );
        
        receive( dgram );
        
        int cursor = 0 ,
            length = dgram.getLength() ,
            size ;
        
        size = IP4Reader.read( ip4Message , receiveBuffer , cursor , length , true );
    
        cursor += size ;
        length -= size ;

        int protocol = ip4Message.protocol >= 0 ? ip4Message.protocol : ip4Message.protocol ^ 0xffffff00 ;
         
        switch( protocol ){
            
            case SocketConstants.IPPROTO_ICMP:
                size = ICMPReader.read( icmpMessage , receiveBuffer , cursor , length , true );
                break;
                
            case SocketConstants.IPPROTO_UDP:
                size = UDPReader.read( udpMessage , receiveBuffer , cursor , length , true , sourceAddress , dgram.getAddress().getAddress() );
                break;
                
            case SocketConstants.IPPROTO_TCP:
            case SocketConstants.IPPROTO_TCPJ:
            default:
                throw new IOException("Unsupported protocol");
        }
        
        cursor += size ;
        length -= size ;
        
        if( cursor != dgram.getLength() || length != 0 ){
            throw new IOException("Illegal message format");
        }
        
        return protocol ;
    }

    public void setReceiveBufferSize( int receiveBufferSize ){
        this.receiveBufferSize = receiveBufferSize ;       
        receiveBuffer = new byte[ receiveBufferSize ]; // Ought to preserve contents
    }

    public int getReceiveBufferSize(){
        return receiveBufferSize ;
    }
    
    public void setSendBufferSize( int sendBufferSize ){
        this.sendBufferSize = sendBufferSize ;       
        sendBuffer = new byte[ sendBufferSize ]; // Ought to preserve contents   
    }

    public int getSendBufferSize(){
        return sendBufferSize ;
    }
    
    public void setTimeToLive( int ttl ){
        timeToLive = ttl ; 
    }
    
    public int getTimeToLive(){
        return timeToLive ;
    }
    
    public void setTypeOfService( int typeOfService ){
        this.typeOfService = typeOfService ;
    }
    
    public int getTypeOfService(){
        return typeOfService ;
    }
    
    public void setSourceAddress( byte[] sourceAddress ){
        this.sourceAddress = sourceAddress ;
    }
}
