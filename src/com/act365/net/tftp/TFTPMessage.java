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

/**
 * TFTPMessage stores all of the information that could possibly occur
 * in a TFTP message. 
 */

public class TFTPMessage {

  short opcode ;
  String filename ;
  int mode ;
  short block ;
  byte[] data = new byte[ TFTPConstants.maxData ];
  int offset ;
  int count ;
  short errorcode ;
  String errortext ;
  
  /**
   * Calculates the length of a TFTP message in bytes.
   * @return length in bytes
   */
  
  public int length() {
  	
  	int len = 0 ;
  	
  	switch( opcode ){
  	  
  	  case TFTPConstants.OP_RRQ:
  	  case TFTPConstants.OP_WRQ:
  	  
  	    len = filename.length() + TFTPConstants.modes[ mode ].length() + 4 ;
  	    break;
  	    
  	  case TFTPConstants.OP_DATA:
  	  
  	    len = count + 4 ;
  	    break;
  	    
  	  case TFTPConstants.OP_ACK:
  	    
  	    len = 4 ;
  	    break;
  	    
  	  case TFTPConstants.OP_ERROR:
  	  
  	    len = errortext.length() + 5 ;
  	    break;
  	}
  	
  	return len ;
  }
}
