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

import java.io.* ;

/**
 * TFTPServer implements the TFTP protocol on the server side.
 */

public class TFTPServer implements Runnable {
	
    /**
     * TFTPServer accepts the following command-line options
     * 
     * -p port#     port to listen on
     * -f logfile   turns on debug
     * -n protocol  network protocol
     * -l localhost local address for binding
     * 
     * @param args - command-line argument
     */
    
    public static void main( String[] args ) {
        
        OutputStream debug = null ;
        
        int port = 0 ;
        
        String logfile = "" ,
               protocolLabel = "JDKUDP" ,
               localhostname = null ;
        
        // Parse the command line options.
        
        int arg = 0 ;
        
        while( arg < args.length && args[ arg ].charAt( 0 ) == '-' ){
            
            switch( args[ arg ].charAt( 1 ) ){
                
                case 'p':
                
                  if( arg < args.length - 1 ){
                    try {
                      port = Integer.parseInt( args[ ++ arg ] );
                    } catch ( NumberFormatException e ) {
                      ErrorHandler.quit("port must be an integer value");
                    }
                  } else {
                      ErrorHandler.quit("-p requires another argument");
                  }
                  
                  break;
                  
                case 'f':
                
                  if( arg < args.length - 1 ){
                      logfile = args[ ++ arg ];
                  } else {
                      ErrorHandler.quit("-f requires another argument");
                  }
                  
                  if( logfile.equals("-") ){
                      debug = System.err ;
                  } else {
                      try {
                          debug = new FileOutputStream( logfile );
                      } catch ( FileNotFoundException e ) {
                          ErrorHandler.quit("File " + logfile + " not found");
                      }
                  }
                  
                  break;
        
                case 'n':
                
                  if( arg < args.length - 1 ){
                      protocolLabel = args[ ++ arg ];
                  } else {
                      ErrorHandler.quit("-n requires another argument");
                  }
                  
                  break;
                  
                case 'l':
                
                  if( arg < args.length - 1 ){
                      localhostname = args[ ++ arg ];
                  } else {
                      ErrorHandler.quit("-l requires another argument");
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
        
        INetworkServerImpl network = null ;
        
        switch( SocketWrenchSession.getProtocol() ){
        
        case SocketConstants.IPPROTO_UDP:

            network = new UDPNetworkServerImpl();
            break;
            
        case SocketConstants.IPPROTO_TCP:
        case SocketConstants.IPPROTO_TCPJ:
        
            network = new TCPNetworkServerImpl();
            break;
            
        default:
        
            ErrorHandler.quit("Unsupported protocol");
        }
            
        ErrorHandler.setDebugStream( debug );
        ErrorHandler.setTrace( true );
                                
        try {  
            network.init( localhostname , port );
            new Thread( new TFTPServer( network ) ).run();
        } catch ( TFTPException e ) {
            e.printStackTrace();
            ErrorHandler.quit( e );
        }
    }
    
    // State variables
    
    INetworkServerImpl network ;
    
    /**
     * Creates a server with a given network connection and optional debug.
     * 
     * @param network - network connection
     */
    
    public TFTPServer( INetworkServerImpl network ){
        
        this.network = network ;
    }
    
    /**
     * Continually polls for client requests and spawns new processes to
     * handle them.
     */
    
    public void run() {
        
        INetworkImplBase newNetwork = null ;
        
        while( true ){   
          try {     
            newNetwork = network.open();
            new Thread( new TFTPWorker( newNetwork ) ).run();
          } catch ( TFTPException e ) {    
            e.printStackTrace();
            ErrorHandler.quit( e );
          }
        }
    }
}
