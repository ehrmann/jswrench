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

import com.act365.net.RetransmissionTimer ;

import java.io.*;

/**
 * Utilities common to client and server
 */

public abstract class TFTPBase {

  protected TFTPBase( INetworkImplBase network ){
      this.network = network ;
  }
  
  // File-handling functions
  
  protected int nextBlockNumber = 0 ;
  	
  boolean lastChar = false ; // Whether last character was a carriage-return
  
  int nextChar = 0 ;
  
  protected InputStream localInput = null ;
  
  protected OutputStream localOutput = null ;
  
  /**
   * Opens a file for output. Specify '-' as the filename in order
   * to open standard output.
   * 
   * @param file - file to open
   * @param initialBlockNumber - used for first data packet
   *
   * @throws FileNotFoundException 
   */
  
  protected void openOutputFile( File file , 
                                 int initialBlockNumber ) throws FileNotFoundException {

	if( file.getName().equals("-") ){
	  localOutput = System.out ;
	} else {
	  localOutput = new FileOutputStream( file );
	}

	nextBlockNumber = initialBlockNumber ;
	lastChar = false ;
	nextChar = -1 ;
        
	ErrorHandler.debug("openOutputFile: opened " + file.getName() );
  }

  /**
   * Opens a file for input. Specify '-' as the filename in order
   * to open standard input.
   * 
   * @param file - file to open
   * @param initialBlockNumber - used for first data packet
   * 
   * @throws FileNotFoundException 
   */
  
  protected void openInputFile( File file , 
                                int initialBlockNumber ) throws FileNotFoundException {

	if( file.getName().equals("-") ){
	  localInput = System.in ;
	} else {
	  localInput = new FileInputStream( file );
	}

	nextBlockNumber = initialBlockNumber ;
	lastChar = false ;
	nextChar = -1 ;
        
    ErrorHandler.debug("openInputFile: opened " + file.getName() );
  }

  /**
   * Checks state variables prior to stream closure.
   * @throws TFTPException - problem with state variables
   */
  
  protected void close() throws TFTPException {
    
    if( lastChar ){
        ErrorHandler.dump("final character was a CR");
    } else if( nextChar >= 0 ) {
        ErrorHandler.dump("nextChar >= 0");
    }

    try {
      
      if( localInput instanceof InputStream ){
          localInput.close();   
      }
      
      if( localOutput instanceof OutputStream ){
          localOutput.close();   
      }
      
    } catch ( IOException e ) {
        ErrorHandler.system( "Cannot close file");
    }
  }

  /**
   * Reads data from the local input stream .
   * 
   * @param buffer - buffer read into
   * @param offset - buffer position where read is to begin
   * @param count - maximum number of bytes to read
   * @param mode - binary or ascii
   * @return number of bytes read
   * @throws TFTPException can't read from stream
   */
  
  protected int read( byte[] buffer , int offset , int count , int mode ) throws TFTPException {
  	
  	int size = 0 ;
  	
  	switch( mode ){
  		
  	  case TFTPConstants.MODE_BINARY:

        try {  	  
  	      size = localInput.read( buffer , offset , count );
        } catch ( IOException e ) {
            ErrorHandler.dump( e.getMessage() );
        }
  	  
  	    if( size < 0 ){
            ErrorHandler.dump("read error on local file");  
  	    }
  	    
  	    break;
  	    
  	  case TFTPConstants.MODE_ASCII:
  	  
        { 
          int c = 0 ;
          
          while( size < count ){
        	
            if( nextChar >= 0 ){
        	  buffer[ offset ++ ] = (byte) nextChar ;
        	  nextChar = -1 ;
              ++ size ;
        	  continue ;	  
            }
 
            try {
                c = localInput.read();
            } catch( IOException e ) {
                ErrorHandler.dump( e.getMessage() );           
            }
            
            if( c == -1 ){ // End-of-file
            	return size ;
            } else if( c == '\n' ){ // newline -> CR, LF
              c = '\r';
              nextChar = '\n';
            } else if( c == '\r' ){ // carriage return -> CR, NULL
              nextChar = '\0';
            } else {
              nextChar = -1 ;
            }
            
            buffer[ offset ++ ] = (byte) c ;
            
            ++ size ;
          }
        }
        
  	    break;
  	    
  	  default:
  	  
      ErrorHandler.dump("unknown MODE value");
  	}
  	
  	return size ;
  }
  
  /**
   * Writes a buffer to the local output stream. Netascii conversions are performed
   * where necessary.
   * 
   * @param buffer - buffer to read from
   * @param offset - buffer position to read from
   * @param count - number of bytes to read
   * @param mode - binary or ascii
   * @throws TFTPException - can't write to file
   */
  
  protected void write( byte[] buffer , int offset , int count , int mode ) throws TFTPException {

  	switch( mode ){
  		
  	  case TFTPConstants.MODE_BINARY:
  	  
  	    try {
            localOutput.write( buffer , offset , count );
  	    } catch( IOException e ) {
            ErrorHandler.dump( e.getMessage() );
  	    }
  	    
  	    break;
  	    
  	  case TFTPConstants.MODE_ASCII:
  	  
  	    /* 
  	     * The following netascii conversions must be performed:
  	     * 
  	     * CR, LF            -> newline = '\n'
  	     * CR, NULL          -> CR      = '\r'
  	     * CR, anything else -> undefined
  	     */
  	     
  	    {
  	      int i = 0 ,
  	          c = 0 ;
  	      
  	      while( i ++ < count ){
  	      	
  	      	c = buffer[ offset ++ ];
  	      	
  	      	if( lastChar ){
  	      		
  	      	  if( c == '\n' ){
  	      	  	c = '\n'; // Doh!
  	      	  } else if( c == '\0' ){
  	      	  	c = '\r';
  	      	  } else {
                  ErrorHandler.dump("CR followed by " + (char) c );
  	      	  }
  	      	  
  	      	  lastChar = false ;
  	      	  
  	      	} else if( c == '\r' ) {
  	          
  	          lastChar = true ;
  	          
  	          continue ;
  	      	}
  	      	
            try {
                localOutput.write( c );
            } catch( IOException e ) {
                ErrorHandler.dump( e.getMessage() );
            }
  	      }
  	    }
  	    
  	    break;
  	    
  	  default:
  	  
      ErrorHandler.dump("unknown MODE value");
  	}
  }
  
  // Functions to send and receive packets.
  
  int lastSize = 0 ;
  
  protected int modetype = TFTPConstants.MODE_ASCII ;
         
  protected long totalbytes = 0 ;
  
  protected INetworkImplBase network = null ;

  byte[] receiveBuffer = new byte[ TFTPConstants.maxBuffer ] ,
         sendBuffer    = new byte[ TFTPConstants.maxBuffer ] ;
  
  TFTPMessage sendMessage = new TFTPMessage();
  
  /**
   * Sends the cached TFTP message to the server.
   */
  
  void send() throws TFTPException {
      
    final int sendSize = TFTPWriter.write( sendMessage , sendBuffer );
      
    network.send( sendBuffer , sendSize );
  }
  
  /**
   * Sends a request to a remote server.
   */

  void sendRQ( int opcode , String filename , int mode ) throws TFTPException {

	sendMessage.opcode = (short) opcode ;
    sendMessage.filename = filename ;
    sendMessage.mode = mode ;
     	
    send();
  }
     
  /**
   * Handles received error messages.
   */
     
  boolean receiveRQERR( TFTPMessage message ) throws TFTPException {

      ErrorHandler.debug("ERROR received, " + message.length() + " bytes, error code " + message.errorcode );
       
    System.out.println("Error# " + message.errorcode + ": " + message.errortext );
       
    return true ;       
  }

  void sendACK( int blockNumber ) throws TFTPException {
                           	
      ErrorHandler.debug("sending ACK for block# " + blockNumber );
    
    sendMessage.opcode = (short) TFTPConstants.OP_ACK ;
    sendMessage.block = (short) blockNumber ;
    
    send();
  }
  
  void sendDATA( int blockNumber ) throws TFTPException {
                            	
      ErrorHandler.debug("sending " + sendMessage.count + " bytes of DATA with block# " + blockNumber );
                            	
    sendMessage.opcode = (short) TFTPConstants.OP_DATA ;
    sendMessage.block = (short) blockNumber ;
    
    send();
  }
  
  boolean receiveDATA( TFTPMessage message ) throws TFTPException {
                             	
    int receivedBlockNumber = message.block ;
    
    ErrorHandler.debug("DATA received, " + message.count + " bytes, block# " + receivedBlockNumber );
    
    if( receivedBlockNumber == nextBlockNumber ){
    	
    	// The data packet is as expected.
    	
    	++ nextBlockNumber ;
    	
    	totalbytes += message.count ;
    	
    	if( message.count > 0 ){
    		
    	  /*
    	   * The final data packet could have a size of zero, so only write
    	   * if there is data.
    	   */
    	   
    	  write( message.data , message.offset , message.count , message.mode );
    	}
    	
    	/*
    	 * This is the last data block if its size is between 0-511.
    	 * The server closes the file at this point - however, the
    	 * 'get' command performs the closure on the client.
    	 */
    	 
    	serverClose( message );
    	
    } else if( receivedBlockNumber < nextBlockNumber - 1 ) {
    	                           	
      /*
       * We've just received data block #n (or earlier) whereas we expected
       * data block #(n+2). Since we're at #(n+2), we must have received
       * #(n+1), so the other end has reverted in time. Something is wrong. 
       */
       
       ErrorHandler.dump("receivedBlockNumber < nextBlockNumber - 1 ");
       
    } else if( receivedBlockNumber > nextBlockNumber ) {
    	
    	/*
    	 * We've just received data block #n (or later) whereas we expected
    	 * data block #(n-1), which means that the other end must have
    	 * received an ACK for block #(n-1). Something is wrong.
    	 */
    	 
        ErrorHandler.dump("receivedBlockNumber > nextBlockNumber");
    
    } 
    
    /*
     * The case not handled above is receivedBlockNumber = nextBlockNumber - 1,
     * which means that the other side hasn't received our ACK for the last data
     * packet and has retransmitted. Ignore the retransmission and send another
     * ACK. 
     */
    
    sendACK( receivedBlockNumber );   
	
	/*
	 * When the length of the data packet is 0-511, we've recieved the final
	 * data packet. Otherwise, there's more to come.
	 */
	 
	return message.count != TFTPConstants.maxData ; 
  }
  
  /**
   * Performs the closure sometimes demanded by receiveData().
   */
  
  abstract void serverClose( TFTPMessage message ) throws TFTPException ; 
  
  /*
   * ACK packet received, so send more data. Called by the finite state machine
   * and by receiveRRQ() in order to start the transmission of a file to a client.
   */
   
  boolean receiveACK( TFTPMessage message ) throws TFTPException {
  
    int receivedBlockNumber = message.block ;
    
    ErrorHandler.debug("ACK received, block# " + receivedBlockNumber );
    
    if( receivedBlockNumber == nextBlockNumber ){
      
      /*
       * The ACK refers to the expected data packet, so fill the transmit
       * buffer with the next block of data to send, or possibly finish if
       * there's no more data to send. Note that we must send a final data
       * packet of length 0-511 bytes. This packet must be of size 0 if the
       * previous packet had a size of exactly 512 bytes. 
       */	
    
      sendMessage.offset = 0 ;
      	
      if( ( sendMessage.count = read( sendMessage.data , sendMessage.offset , TFTPConstants.maxData , modetype ) ) == 0 ){
      	
      	if( lastSize < TFTPConstants.maxData ){
      		return true ; 
      	}
      }
      
      lastSize = sendMessage.count ;
      
      totalbytes += sendMessage.count ;

      sendDATA( ++ nextBlockNumber );
      
      return false ;
    
    } else if( receivedBlockNumber < nextBlockNumber - 1 ) {
      
      /*
       * We've just received the ACK for block #n (or earlier) whereas we
       * expected the ACK for block #(n+2). Since we expect the ACK for
       * block #(n+2), we must have already received the ACK for block #(n+1),
       * so the other end must have gone backwards.  
       */
       
      throw new TFTPException("receivedBlockNumber < nextBlockNumber - 1");
      
    } else if( receivedBlockNumber > nextBlockNumber ) {
    	
      /*
       * We've just received the ACK for block #n (or later) whereas we 
       * expected the ACK for block #(n-1). Since the other side has sent
       * the ACK for block #n, it must have received block #n, so
       * something is wrong.
       */
       
      throw new TFTPException("receivedBlockNumber > nextBlockNumber");
    
    } else {
    
      /* 
       * It must be the case that receivedBlockNumber == nextBlockNumber - 1,
       * which means that we have received a duplicate ACK, i.e.:
       * i. the other side didn't receive the last data packet, or
       * ii. the ACK from the other side has been delayed
       * 
       * Simply ignore the duplicate ACK. The finite state machine will
       * initiate another receive. 
       */
       
       return false ;
    }
  }    	
  
  /*
   * Processes read requests on the server.
   */
   
  boolean receiveRRQ( TFTPMessage message ) throws TFTPException {
  	
  	if( receiveXRQ( message ) ){ // Verify the RRQ packet.
        return true ;
  	}
  
    /*
     * Call receiveACK() as though we've received an ACK. The first data
     * block will be sent to the client.
     */
    
    lastSize = TFTPConstants.maxData ;
     
    TFTPMessage ack = new TFTPMessage();
    
    ack.opcode = TFTPConstants.OP_ACK ;
    ack.block = 0 ;    
    
    receiveACK( ack );
    	
  	return false ;
  }
  
  /*
   * Processes write requests on the server.
   */
   
  boolean receiveWRQ( TFTPMessage message ) throws TFTPException {
  
    if( receiveXRQ( message ) ){ // Verify the WRQ packet.
        return true ;
    }
    
    /*
     * Acknowledge block #0, after which the client should send block #1.
     */
     
    nextBlockNumber = 1 ;
    
    sendACK( 0 );
    
    return false ;	
  }
  
  /*
   * Processes RRQ and WRQ requests.
   */
   
  boolean receiveXRQ( TFTPMessage message ) throws TFTPException {
  
  	/*
  	 * Validate filename.
  	 * 
  	 * Since there are no user-access checks in TFTP, only 
  	 * publicly-accessible files are available to read.
  	 * 
  	 * Since the server runs as a daemon, the home directory is root,
  	 * so all filenames must be absolute. 
  	 */
  	
  	File file = new File( message.filename );
  	
  	if( ! file.isAbsolute() ){
  	  sendERROR( new TFTPException( TFTPConstants.ERR_ACCESS , "Filename must be absolute") );
      return true ;
  	}
  	
    SecurityManager security = System.getSecurityManager();
    
  	switch( message.opcode ){
  		
  	  case TFTPConstants.OP_RRQ:
  	  
  	    /*
  	     * Verify that the file exists and has global read permissions.
  	     */
  	     
  	    try {
  	      openInputFile( file , 0 );
          if( security instanceof SecurityManager ){
  	        security.checkRead( ( (FileInputStream) localInput ).getFD() );
          }
  	    } catch ( FileNotFoundException e ) {
  	      sendERROR( new TFTPException( TFTPConstants.ERR_NOFILE , e.getMessage() ) );
          return true ;
        } catch( IOException io ) {
            ErrorHandler.dump( io.getMessage() );
  	    } catch ( SecurityException se ) {
  	      sendERROR( new TFTPException( TFTPConstants.ERR_ACCESS , "File lacks global read access") );
          return true ;
  	    }
  	    
  	    break;
  	    
  	  case TFTPConstants.OP_WRQ:
  	  
  	    /*
  	     * Verify that the file exists and that the directory has global write permissions.
  	     */
  	    
  	    try {
  	      openOutputFile( file , 0 );
          if( security instanceof SecurityManager ){
  	        security.checkWrite( ( (FileOutputStream) localOutput ).getFD() );
          }
  	    } catch ( FileNotFoundException fe ) {
  	      sendERROR( new TFTPException( TFTPConstants.ERR_NOFILE , fe.getMessage() ) );
          return true ;
        } catch( IOException io ) {
            ErrorHandler.dump( io.getMessage() );
  	    } catch ( SecurityException se ) {
  	      sendERROR( new TFTPException( TFTPConstants.ERR_ACCESS , "Directory lacks global write permissions") );
          return true ;
  	    }
  	    
  	    break;
  	    
  	  default:
  	  
  	    throw new TFTPException("unknown opcode");
  	}
    
    return false ;
  }
  
  /*
   * Sends an error packet. Since acknowledgement isn't required, no
   * retransmission will ever be necessary, so exit occurs immediately 
   * after transmission.
   */
   
  void sendERROR( TFTPException e ) throws TFTPException {
  	
      ErrorHandler.debug("sending ERROR, code = " + e.getErrorCode() + ", string = " + e.getMessage() );
  	
  	sendMessage.opcode = TFTPConstants.OP_ERROR ;
  	sendMessage.errorcode = (short) e.getErrorCode() ;
  	sendMessage.errortext = e.getMessage() ;
  	
    send();
  }
  
  // Finite State Machine
  
  RetransmissionTimer timer = new RetransmissionTimer();
  
  /**
   * Gets the function objects for the Finite State Machine.
   */
  
  abstract IFSMFunction[][] getFSMFunctions();
  
  /**
   * Main loop of the Finite State Machine
   * 
   * Called on the client after a RRQ or a WRQ has been sent.
   * 
   * Called on the server after a RRQ or a WRQ has been received,
   * in which case opCode will be set to OP_NULL because nothing 
   * has been sent.
   * 
   * @param opCode - op code sent
   */
  
  protected void fsmLoop( int opCode ) throws TFTPException {
      
      final IFSMFunction[][] fsmFunctions = getFSMFunctions();
      
      TFTPMessage receiveMessage = new TFTPMessage();

      timer.newPacket();
              
      while( true ){

          int size = 0 ;
          
          try {
              network.setTimeout( timer.start() );
          } catch( TFTPException e ){
              sendERROR( e );
              return ;
          }
          
          try {
              size = network.receive( receiveBuffer );
          } catch( InterruptedIOException e ) {
              
              // Timeout 
              
              if( timer.timeout() ){
                  ErrorHandler.system( "Transmission timeout" );
              }
              ErrorHandler.debug( timer.toString() );
              
              // Retransmit 
              
              send();
              continue ;
          }
       
          timer.stop();
          ErrorHandler.debug( timer.toString() );
          
          try {
              TFTPReader.read( receiveMessage , receiveBuffer , 0 , size );
          } catch ( TFTPException e ) {
              sendERROR( e );
              return ;
          }
          
          if( fsmFunctions[ sendMessage.opcode ][ receiveMessage.opcode ].receive( this , receiveMessage ) ){         
              return ;
          }
      }
  }
}
