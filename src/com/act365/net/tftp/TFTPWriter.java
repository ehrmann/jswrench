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

package com.act365.net.tftp;

import com.act365.net.*;

/**
 * TFTPWriter writes TFTP messages as byte streams.
 */ 

public class TFTPWriter {

  /**
   * Writes a TFTPMessage into a buffer provided by the user.
   * 
   * @param message
   * @param buffer
   * 
   * @return message length
   *  
   * @throws TFTPException - message won't fit into buffer
   */
  
  public static int write( TFTPMessage message , byte[] buffer ) throws TFTPException {
  	
    final int messageLength = message.length();
    
    if( buffer.length < messageLength ){
        throw new TFTPException("Buffer overrun");
    }
    
    int offset = 0 ;
    
  	switch( message.opcode ){
  		
  		case TFTPConstants.OP_RRQ:
  		case TFTPConstants.OP_WRQ:

  	      offset = SocketUtils.shortToBytes( message.opcode , buffer , offset );
  	      offset = SocketUtils.stringToBytes( message.filename , buffer , offset );
  	      buffer[ offset ++ ] = 0 ;
  	      offset = SocketUtils.stringToBytes( TFTPConstants.modes[ message.mode ] , buffer , offset );
  	      buffer[ offset ++ ] = 0 ;
  	      
  	      break;
  	      
  		case TFTPConstants.OP_DATA:
  		
  		  offset = SocketUtils.shortToBytes( message.opcode , buffer , offset );
  		  offset = SocketUtils.shortToBytes( message.block , buffer , offset );
  		  offset = SocketUtils.dataToBytes( message.data , message.offset , message.count , buffer , offset );
  		  
  		  break;
  		  
  		case TFTPConstants.OP_ACK:
  		
  		  offset = SocketUtils.shortToBytes( message.opcode , buffer , offset );
  		  offset = SocketUtils.shortToBytes( message.block , buffer , offset );
  		  
  		  break;
  		  
  		case TFTPConstants.OP_ERROR:
  		
  		  offset = SocketUtils.shortToBytes( message.opcode , buffer , offset );
  		  offset = SocketUtils.shortToBytes( message.errorcode , buffer , offset );
  		  offset = SocketUtils.stringToBytes( message.errortext , buffer , offset );
  		  buffer[ offset ++ ] = 0 ;
  		  
  		  break;
  		  
  		default:
  		
  		  throw new TFTPException("Error in TFTP message format");
  	}
  	
    if( offset != messageLength ){
        throw new TFTPException("Error in TFTP message format");
    }
    
  	return offset ;
  }
}
