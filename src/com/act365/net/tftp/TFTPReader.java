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

import java.io.* ;

/**
 * TFTPReader reads TFTPMessage objects from byte streams.
 */

public class TFTPReader {

  /**
   * Populates a TFTPMessage object according to the contents of a byte stream.
   * 
   * @param message 
   * @param buffer
   * @param offset
   * @param count
   * 
   * @throws TFTPException - problem with message format
   */
  
  public static void read( TFTPMessage message , byte[] buffer , int offset , int count ) throws TFTPException {
      
    int length = 0 ;  

    StringBuffer sb = null ;
        
    message.opcode = SocketUtils.shortFromBytes( buffer , offset );
    offset += 2 ;
    count -= 2 ;
          
    if( message.opcode <= TFTPConstants.OP_NULL || message.opcode > TFTPConstants.OP_ERROR ){
        throw new TFTPException("invalid opcode received: " + message.opcode );
    }
    
  	switch( message.opcode ){
  		
  		case TFTPConstants.OP_RRQ:
  		case TFTPConstants.OP_WRQ:

        {            
          String mode = null ;
          
          sb = new StringBuffer();
          
          try {       
  		    length = readString( buffer , offset , count , sb );
            offset += length ;
            count -= length ;
            message.filename = sb.toString();
          } catch ( TFTPException e ) {
            throw new TFTPException("Invalid filename");
          }
          
          sb = new StringBuffer();
          
          try {
            length = readString( buffer , offset , count , sb );
            offset += length ;
            count -= length ;
            mode = sb.toString();
          } catch ( TFTPException e ) {
            throw new TFTPException("Invalid mode");
          }
  		  
  		  int i = 0 ;
  		  
  		  while( i < TFTPConstants.modes.length ){
  		  	if( mode.equalsIgnoreCase( TFTPConstants.modes[ i ] ) ){
  		  		message.mode = i ;
  		  		break ;
  		  	}
            ++ i ;
  		  }
  		  
  		  if( i == TFTPConstants.modes.length ){
  		  	throw new TFTPException( TFTPConstants.ERR_BADOP , "Unsupported data mode");
  		  }
        }
          
  		break;
  		  
  		case TFTPConstants.OP_DATA:
  		
  		  message.block = SocketUtils.shortFromBytes( buffer , offset );
  		  offset += 2 ;
          count -= 2 ;
  		  
  		  message.offset = offset ;
  		  message.count = count ;
  		  message.data = buffer ;
  		  
          offset += message.count ;
          count -= message.count ;
          
  		  break;

		case TFTPConstants.OP_ACK:
  		
		  message.block = SocketUtils.shortFromBytes( buffer , offset );
		  offset += 2 ;
          count -= 2 ;
		  
		  break;
		  
		case TFTPConstants.OP_ERROR:
  		
		  message.errorcode = SocketUtils.shortFromBytes( buffer , offset );
		  offset += 2 ;
          count -= 2 ;
		  
          sb = new StringBuffer();
          
		  length = readString( buffer , offset , count , sb );
          offset += length ;
          count -= length ;
          
          message.errortext = sb.toString();
           
		  break;
    }

	if( count != 0 ){
	  throw new TFTPException("Error in TFTP message format");
	} 
  }

  /**
   * Reads a zero-terminated string from a buffer.
   */

  static int readString( byte[] buffer , int offset , int count , StringBuffer sb ) throws TFTPException {
  	
  	int cursor = 0 ;
  	
  	while( cursor < count && buffer[ offset + cursor ] != 0 ){
        ++ cursor ;
  	}

    if( cursor == count ){
      throw new TFTPException();
    }
    
    try {
      sb.append( new String( buffer , offset , cursor , "UTF8" ) );
    } catch ( UnsupportedEncodingException e ) {
      cursor = 0 ;
    }
  	
  	return cursor + 1 ;
  }
}
