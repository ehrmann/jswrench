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

import com.act365.net.* ;

import java.io.*;
import java.util.*;

/**
 * The class TFTP implements a TFTP client.
 */

public class TFTP extends TFTPBase {

     String hostname = null ,
            localhostname = null ;
     
     boolean verbose = false ,
             interactive = false ,
             connected = false ;
             
     int port = 0 ,
         localport = 0 ,
         lastsend  = 0 ;
         
     // Finite State Machine function objects for the client.
          
     final static IFSMFunction[][] fsmFunctions = new IFSMFunction[][] { 
             
             { new FSMInvalid() ,        // [ sent = 0 ]        [ received = 0 ]
               new FSMInvalid() ,        // [ sent = 0 ]        [ received = OP_RRQ ]
               new FSMInvalid() ,        // [ sent = 0 ]        [ received = OP_WRQ ]
               new FSMInvalid() ,        // [ sent = 0 ]        [ received = OP_DATA ]
               new FSMInvalid() ,        // [ sent = 0 ]        [ received = OP_ACK ]
               new FSMInvalid() } ,      // [ sent = 0 ]        [ received = OP_ERROR ]
             
             { new FSMInvalid() ,        // [ sent = OP_RRQ ]   [ received = 0 ]
               new FSMInvalid() ,        // [ sent = OP_RRQ ]   [ received = OP_RRQ ]
               new FSMInvalid() ,        // [ sent = OP_RRQ ]   [ received = OP_WRQ ]
               new FSMReceiveDATA() ,    // [ sent = OP_RRQ ]   [ received = OP_DATA ]
               new FSMInvalid() ,        // [ sent = OP_RRQ ]   [ received = OP_ACK ]
               new FSMReceiveRQERR() } , // [ sent = OP_RRQ ]   [ received = OP_ERROR ]
             
             { new FSMInvalid() ,        // [ sent = OP_WRQ ]   [ received = 0 ]
               new FSMInvalid() ,        // [ sent = OP_WRQ ]   [ received = OP_RRQ ]
               new FSMInvalid() ,        // [ sent = OP_WRQ ]   [ received = OP_WRQ ]
               new FSMInvalid() ,        // [ sent = OP_WRQ ]   [ received = OP_DATA ]
               new FSMReceiveACK() ,     // [ sent = OP_WRQ ]   [ received = OP_ACK ]
               new FSMReceiveRQERR() } , // [ sent = OP_WRQ ]   [ received = OP_ERROR ]
             
             { new FSMInvalid() ,        // [ sent = OP_DATA ]  [ received = 0 ]
               new FSMInvalid() ,        // [ sent = OP_DATA ]  [ received = OP_RRQ ]
               new FSMInvalid() ,        // [ sent = OP_DATA ]  [ received = OP_WRQ ]
               new FSMInvalid() ,        // [ sent = OP_DATA ]  [ received = OP_DATA ]
               new FSMReceiveACK() ,     // [ sent = OP_DATA ]  [ received = OP_ACK ]
               new FSMError() } ,        // [ sent = OP_DATA ]  [ received = OP_ERROR ]
             
             { new FSMInvalid() ,        // [ sent = OP_ACK ]   [ received = 0 ]
               new FSMInvalid() ,        // [ sent = OP_ACK ]   [ received = OP_RRQ ]
               new FSMInvalid() ,        // [ sent = OP_ACK ]   [ received = OP_WRQ ]
               new FSMReceiveDATA() ,    // [ sent = OP_ACK ]   [ received = OP_DATA ]
               new FSMInvalid() ,        // [ sent = OP_ACK ]   [ received = OP_ACK ]
               new FSMError() } ,        // [ sent = OP_ACK ]   [ received = OP_ERROR ]
             
             { new FSMInvalid() ,        // [ sent = OP_ERROR ] [ received = 0 ]
               new FSMInvalid() ,        // [ sent = OP_ERROR ] [ received = OP_RRQ ]
               new FSMInvalid() ,        // [ sent = OP_ERROR ] [ received = OP_WRQ ]
               new FSMInvalid() ,        // [ sent = OP_ERROR ] [ received = OP_DATA ]
               new FSMInvalid() ,        // [ sent = OP_ERROR ] [ received = OP_ACK ]
               new FSMError() }          // [ sent = OP_ERROR ] [ received = OP_ERROR ]             
     };
    
     /**
      * Trivial File Transfer Protocol client
      * 
      * @param args - command line arguments
      */
     
     public static void main( String[] args ) {
     	
     	String hostname = null ,
               localhostname = null ,
               protocolLabel = "JDKUDP";
     	
     	boolean trace = false , 
     	        verbose = false ;
     	
        int localport = 0 ;
        
     	// Parse the command line options.
     	
     	int arg = 0 ;
     	
     	while( arg < args.length && args[ arg ].charAt( 0 ) == '-' ){
     		
     		switch( args[ arg ].charAt( 1 ) ){
     			
     			case 'h':
     			
     			  if( arg < args.length - 1 ){
     			    hostname = args[ ++ arg ];
     			  } else {
                      ErrorHandler.quit("-h requires another argument");
     			  }
     			  
     			  break;
     			  
     			case 't':
     			
     			  trace = true ;
     			  break;
     			  
     			case 'v':
     			
     			  verbose = true ;
     			  break;
     			  
                case 'n':
                
                  if( arg < args.length - 1 ){
                    protocolLabel = args[ ++ arg ];
                  } else {
                      ErrorHandler.quit("-n requires another argument");
                  }
                  
                  break;
                 
                case 'l':
                
                  if( arg < args.length - 2 ){
                      localhostname = args[ ++ arg ];
                      try {
                          localport = Integer.parseInt( args[ ++ arg ] );
                      } catch ( NumberFormatException e ) {
                          ErrorHandler.quit("Localport number should be an integer");
                      }
                  } else {
                      ErrorHandler.quit("-l requires two more arguments");
                  }
                  
                  break;
                   
     			default:
     			
                ErrorHandler.quit("Unknown command line option: " + args[ arg ] );
     		}
     		
     		++ arg ;
     	}

        new SocketWrenchSession();
        
        try {
            SocketWrenchSession.setProtocol( protocolLabel );
        } catch ( IOException e ) {
            ErrorHandler.quit("Unable to set protocol");
        }
        
        INetworkImpl network = null ;
        
        switch( SocketWrenchSession.getProtocol() ){
        
        case SocketConstants.IPPROTO_UDP:

            network = new UDPNetworkImpl();
            break;
            
        case SocketConstants.IPPROTO_TCP:
        case SocketConstants.IPPROTO_TCPJ:
        
            network = new TCPNetworkImpl();
            break;
            
        default:
        
            ErrorHandler.quit("Unsupported protocol");
        }
                                    
        try {	
     	    new TFTP( args , arg , hostname , localhostname , localport , trace , verbose , network );
        } catch ( TFTPException e ) {
            e.printStackTrace();
            System.exit( 1 );
        }
     }
     
     /**
      * Executes a TFTP session for each filename specified or, when no
      * filenames have been provided, for standard input.
      *  
      * @param args - list of command-line arguments
      * @param arg - position of first filename within command-line arguments
      * @param hostname - remote hostname
      * @param localhostname - locally-bound address
      * @param localport - locally-bound port
      * @param trace - whether debug is required 
      * @param verbose - whether verbosity is required
      * @param network - network implementation to use
      * @throws TFTPException - can't read from input or can't execute it
      */
     
     public TFTP( String[] args ,
                  int arg ,
                  String hostname ,
                  String localhostname ,
                  int localport ,
                  boolean debug ,
                  boolean verbose ,
                  INetworkImpl network ) throws TFTPException {
     	
        super( network );
        	
        this.hostname = hostname ;
        this.localhostname = localhostname ;
        this.localport = localport ;
        this.verbose = verbose ;

        ErrorHandler.setDebugStream( System.out );
        ErrorHandler.setTrace( debug );        
        
     	// Execute tftp on each filename specified or, in the case that no filenames
     	// have been provided, use standard input.
     	
   	    if( arg < args.length ){
   		    while( arg < args.length ){
                FileInputStream input = null ;
                try {
                    input = new FileInputStream( args[ arg ] );
                } catch ( FileNotFoundException e ){
                    System.out.println("File " + args[ ++ arg ] + " not found");
                    continue ;
                }
                tftp( input );
                ++ arg ;
  		    }
  	    } else {
            interactive = true ;
  		    tftp( System.in );
  	    }
     }
   
     /**
      * Reads TFTP commands from the input stream and executes them
      * until a 'quit' command is issued or end-of-file is encountered.
      * 
      * @param input - input stream from which to read commands
      */
     
     void tftp( InputStream input ) throws TFTPException {
     	
     	StreamTokenizer st = new StreamTokenizer( new InputStreamReader( input ) );

        st.resetSyntax();
        st.whitespaceChars( 0 , ' ');
        st.wordChars( ' ' + 1  , 255 ); 
        st.eolIsSignificant( true );
             	     	
     	if( interactive ){
     		System.out.print( TFTPConstants.prompt );
     	}

        out:
     
     	  while( true ){
         
            try {
            
              switch( st.nextToken() ){
     		  
                case StreamTokenizer.TT_EOF:
                  break out;
     		    
     		    case StreamTokenizer.TT_WORD:
     		      execute( st );

     		      break;
     		  
     		    case StreamTokenizer.TT_EOL:
     		      continue;
     		  
     		    case StreamTokenizer.TT_NUMBER:
                  ErrorHandler.quit("Unexpected numerical input " + st.nval );
                  
                default:
                  ErrorHandler.quit("Unexpected character");
     		  }
     		  
            } catch ( TFTPCommandException e ) {
              
              System.out.println("Syntax Error: " + e.getMessage() );	

            } catch ( TFTPSystemException e ) {
                
              System.out.println("System Error: " + e.getMessage() );
              
            } catch ( IOException e ) {
                
              System.out.println("Input Error");
            }

            try {            
           
                if( st.nextToken() != StreamTokenizer.TT_EOL ){
                    while( st.nextToken() != StreamTokenizer.TT_EOL );
                    System.out.println("Trailing text ignored");       
                }
            } catch ( IOException e ) {
                System.out.println("Input Error");
            }

			if( interactive ){
				System.out.print( TFTPConstants.prompt );
			}     		
     	  }
     }
     
     /**
      * Executes a single TFTP command.
      * 
      * @param st - StreamTokenizer with command to be executed as next token
      * 
      * @return whether the TFTP session is to terminate
      */     
     
     void execute( StreamTokenizer st ) throws TFTPException , IOException {
     	
     	int command = 0 ;
     	
     	while( command < TFTPConstants.commands.length ){
     		if( st.sval.equals( TFTPConstants.commands[ command ] ) ){
     			break ;
     		}
            ++ command ;
     	}
     	
     	if( command == TFTPConstants.commands.length ){
            ErrorHandler.command( "Unknown command " + st.sval );
     	}
     	
     	boolean exit = false ;
     	
     	switch ( command ){
     		
     		/*
     		 * ascii
     		 * 
     		 * Equivalent to 'mode ascii'
     		 */
     		 
     		case TFTPConstants.CMD_ASCII:
     		  modetype = TFTPConstants.MODE_ASCII ;
     		  break;
     		 
     		/*
     		 * binary
     		 * 
     		 * Equivalent to 'mode binary'
     		 */ 
     		 
     		case TFTPConstants.CMD_BINARY:
     		  modetype = TFTPConstants.MODE_BINARY ;
     		  break;
     		
     		/*
     		 * connect <hostname> [<port>]
     		 * 
     		 * Sets the hostname and optional port number for future transfers.
     		 * The port is the well-known port number of the TFTP server on
     		 * the other system, which will normally default to the value
     		 * specified in /etc/services (69).
     		 */ 
     		 
            case TFTPConstants.CMD_CONNECT:
            
              if( st.nextToken() == StreamTokenizer.TT_WORD ){
              	hostname = st.sval ;
              } else {
                  ErrorHandler.command("Hostname must be specified");
              }
              
              if( st.nextToken() == StreamTokenizer.TT_WORD ){
                try {
                    port = Integer.parseInt( st.sval );
                } catch ( NumberFormatException e ) {
                    ErrorHandler.command("Port number should be an integer");
                }
              }
              
              connected = true ;
              
              break;
            
            /*
             * exit or quit
             */  
             
            case TFTPConstants.CMD_EXIT:
            case TFTPConstants.CMD_QUIT:
             
              System.exit( 0 );
              break;
     		 
     	    /*
     	     * get <remotefilename> <localfilename>
     	     * 
     	     * NB <remotefilename> may be of the form <host>:<filename> in
     	     * order to specify the host.
     	     */ 
     	     
     	    case TFTPConstants.CMD_GET:
     	    
     	      {
     	      	String remotefilename = null ,
     	      	       localfilename = null ;
     	      	 
     	      	if( st.nextToken() == StreamTokenizer.TT_WORD ){
     	      	  remotefilename = st.sval ;
     	      	} else {
                    ErrorHandler.command("Remote filename must be specified");	 
     	      	}
     	      	
                int colonPos = remotefilename.indexOf('|');
                
                if( colonPos >= 0 ){
                    hostname = remotefilename.substring( 0 , colonPos );
                    remotefilename = remotefilename.substring( colonPos + 1 , remotefilename.length() );
                }
                                
				if( st.nextToken() == StreamTokenizer.TT_WORD ){
				  localfilename = st.sval ;
				} else {
                    ErrorHandler.command("Local filename must be specified");	 
				}
				
				if( localfilename.indexOf('|') != -1 ){
                    ErrorHandler.command("Cannot specify hostname for local file");
				}
				
				get( remotefilename , localfilename );
     	      }
     	      
     	      break;
     		
     		/*
     		 * help
     		 */  
     		
     		case TFTPConstants.CMD_HELP_I:
     		case TFTPConstants.CMD_HELP_II:
     		
     		  {
     		  	int i = 0 ;
     		  	
     		  	while( i < TFTPConstants.commands.length ){
     		  	  System.out.println( TFTPConstants.commands[ i ] );
     		  	}
     		  }
     		  
     		  break;
     		  
     		/*
     		 * mode ascii
     		 * mode binary
     		 * 
     		 * Sets the mode for file transfers.
     		 */
     		
     		case TFTPConstants.CMD_MODE:
     		
     		  {
     		    String mode = null ;
     			
			    if( st.nextToken() == StreamTokenizer.TT_WORD ){
				  mode = st.sval ;
			    } else {
                    ErrorHandler.command("Mode type must be specified");	 
			    }
			  
			    if( mode.equals("ascii") ){
			  	  modetype = TFTPConstants.MODE_ASCII ;
			    } else if( mode.equals("binary") ){
			  	  modetype = TFTPConstants.MODE_BINARY ;
			    } else {
                    ErrorHandler.command("Mode must be 'ascii' or 'binary'");
			    }
              }

              break;
              
			/*
			 * put <localfilename> <remotefilename>
			 * 
			 * NB <remotefilename> may be of the form <host>:<filename> in
			 * order to specify the host.
			 */ 
     	     
			case TFTPConstants.CMD_PUT:
     	    
			  {
				String remotefilename = null ,
					   localfilename = null ;
     	      	       
				if( st.nextToken() == StreamTokenizer.TT_WORD ){
				  localfilename = st.sval ;
				} else {
                    ErrorHandler.command("Local filename must be specified");	 
				}
								
				if( localfilename.indexOf('|') != -1 ){
                    ErrorHandler.command("Cannot specify hostname for local file");
				}

				if( st.nextToken() == StreamTokenizer.TT_WORD ){
				  remotefilename = st.sval ;
				} else {
                    ErrorHandler.command("Remote filename must be specified");	 
				}
     	      	
                int colonPos = remotefilename.indexOf('|');
                
                if( colonPos >= 0 ){
                    hostname = remotefilename.substring( 0 , colonPos );
                    remotefilename = remotefilename.substring( colonPos + 1 , remotefilename.length() );
                }
                
				if( hostname == null ){
                    ErrorHandler.command("Hostname must be specified");	      	
				}
     	      	    				
				put( localfilename , remotefilename );
			  }
     	      
			  break;
			  
			/*
			 * status
			 * 
			 * Shows current status.
			 */
			 
			case TFTPConstants.CMD_STATUS:
			
			  System.out.println( toString() );
			  break;
			 
			/*
			 * trace
			 * 
			 * Toggles debug mode.
			 */ 
			 
			case TFTPConstants.CMD_TRACE:
			
			  ErrorHandler.toggleTrace();
			  break;
			 
			/*
			 * verbose
			 * 
			 * Toggles verbose mode.
			 */  
			 
			case TFTPConstants.CMD_VERBOSE:
			
			  verbose = ! verbose ;
			  break;
     	}
     }
     
     /**
      * Provides a string representation of current status.
      */
     
     public String toString() {
     	
     	StringBuffer sb = new StringBuffer();
     	
     	if( connected ){
     	  sb.append("Connected");
     	} else {
     	  sb.append("Not connected");
     	}
     	
     	sb.append(", mode=");
     	
     	switch( modetype ){
     	  case TFTPConstants.MODE_ASCII:
     	    sb.append("netascii");
     	    break;
     	  case TFTPConstants.MODE_BINARY:
     	    sb.append("octet (binary)");
     	    break;
     	  default:
     	    sb.append("unknown");
     	}
     	
		sb.append(", verbose=");
     	
		if( verbose ){
		  sb.append("on");
		} else {
		  sb.append("off");
		}
     	
		sb.append(", trace=");
     	
		if( ErrorHandler.trace ){
		  sb.append("on");
		} else {
		  sb.append("off");
		}
     	
     	return sb.toString(); 
     }
     
     /**
      * Executes a get command - reads a remote file and stores it on the local system
      */

     void get( String remotefilename , String localfilename ) throws TFTPException {

         System.err.println("get " + remotefilename + " " + localfilename );
             	
        try {
            openOutputFile( new File( localfilename ) , 1 );
        } catch ( FileNotFoundException e ){
            ErrorHandler.system( "File " + localfilename + " not found" );
        }
        
        ((INetworkImpl) network ).open( hostname , port , localhostname , localport );

        totalbytes = 0 ;
		
		long starttime = new Date().getTime();

        sendRQ( TFTPConstants.OP_RRQ , remotefilename , modetype );     	
     	
     	fsmLoop( TFTPConstants.OP_RRQ );
     	
     	long endtime = new Date().getTime();
     	
     	network.close();
     	
     	close();
     	
     	double t = ( endtime - starttime )/ 1000. ;
     	
     	System.out.println("Received " + totalbytes + " bytes in " + t + "s");
     }
     
	/**
	 * Executes a put command - reads a local file and stores it on the remote system
	 */

     void put( String localfilename , String remotefilename ) throws TFTPException {
     	
        System.err.println("put " + localfilename + " " + hostname + ":" + remotefilename );
        
        try {
     	    openInputFile( new File( localfilename ) , 0 );
        } catch ( FileNotFoundException e ) {
            ErrorHandler.system("File " + localfilename + " not found");
        }
        
     	((INetworkImpl) network ).open( hostname , port , localhostname , localport );
     	
     	totalbytes = 0 ;
     	
     	long starttime = new Date().getTime();

        sendRQ( TFTPConstants.OP_WRQ , remotefilename , modetype );
        
        fsmLoop( TFTPConstants.OP_WRQ );
        
		long endtime = new Date().getTime();
     	
		network.close();
     	
		close();
     	
		double t = ( endtime - starttime )/ 1000. ;
     	
		System.out.println("Sent " + totalbytes + " bytes in " + t + "s");     	
     }
     
     /**
      * serverClose() is called from receiveDATA() but does nothing on the client-side.
      */
     
     void serverClose( TFTPMessage message ) throws TFTPException {}
     
     /**
      * Returns the Finite State Machine functions for the TFTP client.
      */
     
     IFSMFunction[][] getFSMFunctions() { return fsmFunctions ;}
}
