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
 * Constants used in the Trivial File Transfer Protocol
 */

public class TFTPConstants {

  // Op codes
  
  public final static int OP_NULL  = 0 , // NULL
                          OP_RRQ   = 1 , // Read request
                          OP_WRQ   = 2 , // Write request
                          OP_DATA  = 3 , // Data
                          OP_ACK   = 4 , // Acknowledgement
                          OP_ERROR = 5 ; // Error
  
  public final static String[] opCodes = { "" ,
                                           "RRQ" ,
                                           "WRQ" ,
                                           "DATA" ,
                                           "ACK" ,
                                           "ERROR" };
  
  // Modes
  
  public final static int MODE_ASCII  = 0 ,
                          MODE_BINARY = 1 ;
                          
  public final static String[] modes = { "netascii" ,
  	                                     "octet" };
                                                  
  // Error codes
  
  public final static int ERR_UNDEF   = 0 ,
                          ERR_NOFILE  = 1 ,
                          ERR_ACCESS  = 2 ,
                          ERR_NOSPACE = 3 ,
                          ERR_BADOP   = 4 ,
                          ERR_BADID   = 5 ,
                          ERR_FILE    = 6 ,
                          ERR_NOUSER  = 7 ;
  
  public final static String[] errorMessages = { "Not defined" ,
  	                                             "File not found" ,
  	                                             "Access violation" ,
  	                                             "Disk full or allocation exceeded" ,
  	                                             "Illegal TFTP operation" ,
  	                                             "Unknown port number" ,
  	                                             "File already exists" ,
  	                                             "No such user" };
  	
  // Commands
  
  public final static int CMD_HELP_I  = 0 ,
                          CMD_ASCII   = 1 ,
                          CMD_BINARY  = 2 ,
                          CMD_CONNECT = 3 ,
                          CMD_EXIT    = 4 ,
                          CMD_GET     = 5 ,
                          CMD_HELP_II = 6 ,
                          CMD_MODE    = 7 ,
                          CMD_PUT     = 8 ,
                          CMD_QUIT    = 9 ,
                          CMD_STATUS  = 10 ,
                          CMD_TRACE   = 11 ,
                          CMD_VERBOSE = 12 ;
                             
  public final static String[] commands = { "?" ,
  	                                        "ascii" ,
  	                                        "binary" ,
  	                                        "connect" ,
  	                                        "exit" ,
  	                                        "get" ,
  	                                        "help" ,
  	                                        "mode" ,
  	                                        "put" ,
  	                                        "quit" ,
  	                                        "status" ,
  	                                        "trace" ,
  	                                        "verbose" };    
                                            
  // Interactive
  
  public final static String prompt = "tftp> "; 
  
  // Implementation parameters
  
  public final static int maxData = 512 , // Max data size per packet
                          maxBuffer = 1024 ; // Receive and send buffer size
                          
  public final static int defaultPort = 69 ; 	                                                                              
}
