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

import com.act365.net.ip.* ;

import java.io.* ;
import java.net.* ;

/**
 * A JSWDatagramSocket socket appends the necessary headers (potentially an
 * IP4 header and a protocol-specific header) to a datagram packet prior to
 * transmission and strips the headers from a received packet during receipt. 
 */

public class JSWDatagramSocket extends DatagramSocket {

    final static int ip4HeaderLength = 20 ,
                     udpHeaderLength = 8 ,
                     icmpHeaderLength = 8 ; 
    
    int sendBufferSize ,
        receiveBufferSize ,
        typeOfService ,
        timeToLive ,
        sourcePort ;
    
    boolean testChecksum ;
               
    byte[] sendBuffer ,
           receiveBuffer ,
           sourceAddress ;

    PrintStream debug ;
    
    /**
     * Creates a JSWDatagramSocket with 1Kb send- and recive-buffers, which
     * is bound to localhost (127.0.0.1) and uses a time-to-live value of 255.
     * 
     * @throws SocketException socket cannot be created
     */                       
    
    public JSWDatagramSocket() throws SocketException {
        
        super();
        
        reset();
         
        sourceAddress = new byte[] { 127 , 0 , 0 , 1 }; 
        sourcePort = 0 ;
    }

    public JSWDatagramSocket( int port ) throws SocketException {
        
        super( port );
        
        reset();

        sourceAddress = new byte[] { 127 , 0 , 0 , 1 };
        sourcePort = port ;   
    }
    
    public JSWDatagramSocket( int port , InetAddress localAddr ) throws SocketException {
        
        super( port , localAddr );        
        
        reset();

        sourceAddress = localAddr.getAddress();       
        sourcePort = port ;        
    }
    
    void reset(){
        
        timeToLive = 255 ;
        typeOfService = IP4.TOS_DATA ;
        
        sendBufferSize = 1024 ;
        receiveBufferSize = 1024 ;
        
        sendBuffer = new byte[ sendBufferSize ];
        receiveBuffer = new byte[ receiveBufferSize ];
        
        debug = null ;
        
        testChecksum = true ;        
    }
    
    public void send( IProtocolMessage message ,
                      byte[] destAddress ) throws IOException {
    
        if( SocketWrenchSession.isRaw() ){
            if( message.usesPortNumbers() && sourcePort == 0 ){
                throw new IOException("Source port should be defined for raw " + message.getProtocolName() + " messages");
            }
        } else {
            if( message.isRaw() ){
                throw new IOException("Sending of raw " + message.getProtocolName() + " messages incompatible with selected protocol");
            } else if( message.getProtocol() != SocketWrenchSession.getProtocol() ){
                throw new IOException("Selected SocketWrenchSession protocol is incompatible with " + message.getProtocolName() );
            }
        }
        
        int cursor = 0 ;
        
        if( SocketWrenchSession.includeHeader() ){

            IP4Message ip4Message = new IP4Message( (byte) typeOfService ,
                                                    (short) timeToLive ,
                                                    (byte) message.getProtocol() ,
                                                    sourceAddress ,
                                                    destAddress ,
                                                    new byte[0] ,
                                                    sendBuffer ,
                                                    cursor );
                                                    
            cursor += ip4Message.write( sendBuffer , cursor , null , null );                                                   
        }

        cursor += message.write( sendBuffer , cursor , sourceAddress , destAddress );
                        
        if( debug instanceof PrintStream ){
            debug.println("SEND:");
            SocketUtils.dump( debug , sendBuffer , 0 , cursor );
        }
        
        send( new DatagramPacket( sendBuffer , cursor , GeneralSocketImpl.createInetAddress( SocketConstants.AF_INET , destAddress ) , message.getDestinationPort() ) );
    }

    public int receive( IP4Message ip4Message ,
                        IProtocolMessage message ) throws IOException {
    
        DatagramPacket dgram ;
        
        int protocol = 0 ;
        
        while( true ) {
            
            dgram = new DatagramPacket( receiveBuffer , receiveBuffer.length );      
            
            receive( dgram );
        
            int cursor = 0 ,
                length = dgram.getLength() ,
                size ;
        
            byte[] source ,
                   destination ;
                   
            if( SocketWrenchSession.isRaw() ){
                if( ip4Message instanceof IP4Message ){
                    size = ip4Message.read( receiveBuffer , cursor , length , testChecksum , null , null );
                    protocol = ( ip4Message.protocol >= 0 ? ip4Message.protocol : ip4Message.protocol ^ 0xffffff00 );
                    source = ip4Message.source ;
                    destination = ip4Message.destination ;
                    if( protocol != message.getProtocol() ){
                        throw new IOException("Non-" + message.getProtocolName() + " Datagram packet");    
                    }
                    if( ( sourceAddress[ 0 ] != 0 ||
                          sourceAddress[ 1 ] != 0 ||
                          sourceAddress[ 2 ] != 0 ||
                          sourceAddress[ 3 ] != 0 ) && 
                        ( sourceAddress[ 0 ] != ip4Message.destination[ 0 ] ||
                          sourceAddress[ 1 ] != ip4Message.destination[ 1 ] ||
                          sourceAddress[ 2 ] != ip4Message.destination[ 2 ] ||
                          sourceAddress[ 3 ] != ip4Message.destination[ 3 ] ) ){
                              continue ;
                    }
                } else {
                    size = ip4HeaderLength ;
                    protocol = message.getProtocol();
                    source = dgram.getAddress().getAddress();
                    destination = sourceAddress ;
                }
            } else {                
                size = 0 ; 
                protocol = message.getProtocol();
                source = dgram.getAddress().getAddress();
                destination = sourceAddress ;
                if( ip4Message instanceof IP4Message ){
                    ip4Message.source = source ;
                    ip4Message.destination = destination ;
                    ip4Message.length = (short) size ;
                }                
            }
    
            cursor += size ;
            length -= size ;
        
            size = message.read( receiveBuffer , cursor , length , testChecksum , source , destination );
        
            if( message.usesPortNumbers() && sourcePort != 0 && message.getDestinationPort() != 0 && sourcePort != message.getDestinationPort() ){
                continue ;
            }
            
            cursor += size ;
            length -= size ;
        
            if( cursor != dgram.getLength() || length != 0 ){
                throw new IOException("Illegal " + message.getProtocolName() + " message format");
            }
            
            break;
        }
        
        if( debug instanceof PrintStream ){
            debug.println("RECEIVE:");
            if( ip4Message != null ){
                debug.println( ip4Message.toString() );
            }
            debug.println( message.toString() );
            SocketUtils.dump( debug , message.getData() , message.getCount() , message.getOffset() );
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
    
    public byte[] getSourceAddress() {
        return sourceAddress ;
    }
    
    public void setSourcePort( int sourcePort ){
        this.sourcePort = sourcePort ;
    }
    
    public int getSourcePort() {
        return sourcePort ;
    }
    
    public void setDebug( PrintStream debug ){
        this.debug = debug ;
    }
    
    public boolean testChecksum(){
        return testChecksum ;
    }
    
    public void testChecksum( boolean testChecksum ){
        this.testChecksum = testChecksum ;
    }
}
