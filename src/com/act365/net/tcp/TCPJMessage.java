/*
  * JSocket Wrench
  * 
  * Copyright (C) act365.com December 2004
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

/**
 * Stores a TCP/J message. TCP/J is similar to TCP in all respects
 * other than in name and its protocol code.
 */

public class TCPJMessage extends TCPMessage {

    /**
     * Creates a blank TCPJMessage that will be populated by a call to read().
     */
  
    public TCPJMessage(){
    }
  
    /**
     * Creates a populated TCPJMessage.
     */
  
    public TCPJMessage( short sourceport ,
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
                            
        super( sourceport ,
               destinationport ,
               sequencenumber ,
               acknowledgementnumber ,
               ack ,
               rst ,
               syn ,
               fin ,
               psh ,
               windowsize ,
               options ,
               writebuffer ,
               writestart ,
               writeend );
    }
                        
    /**
     * Returns the protocol code, e.g. 6 for TCP or 17 for UDP.
     * @return protocol code
     */
    
    public int getProtocol(){
        return com.act365.net.SocketConstants.IPPROTO_TCPJ ;
    }
    
    /**
     * Returns the protocol name, e.g. "TCP" or "UDP".
     * @return protocol name
     */
    
    public String getProtocolName(){
        return "TCP/J";
    }      
}
