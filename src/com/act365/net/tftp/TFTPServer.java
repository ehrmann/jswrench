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

public class TFTPServer extends ErrorHandler implements Runnable {
	
    /**
     * TFTPServer accepts the following command-line options
     * 
     * -p port#     port to listen on
     * -l logfile   turns on debug
     * -i           interactive (instead of daemon)
     * 
     * @param args - command-line argument
     */
    
    public static void main( String[] args ) throws Exception {
        
        OutputStream debug = null ;
        
        int port = 0 ;
        
        String logfile = "";
        
        // Parse the command line options.
        
        int arg = 0 ;
        
        while( arg < args.length && args[ arg ].charAt( 0 ) == '-' ){
            
            switch( args[ arg ].charAt( 1 ) ){
                
                case 'p':
                
                  if( arg < args.length - 1 ){
                    try {
                      port = Integer.parseInt( args[ ++ arg ] );
                    } catch ( NumberFormatException e ) {
                      quit("port must be an integer value");
                    }
                  } else {
                    quit("-p requires another argument");
                  }
                  
                  break;
                  
                case 'l':
                
                  if( arg < args.length - 1 ){
                      logfile = args[ ++ arg ];
                  } else {
                    quit("-l requires another argument");
                  }
                  
                  if( logfile.equals("-") ){
                      debug = System.err ;
                  } else {
                      try {
                          debug = new FileOutputStream( logfile );
                      } catch ( FileNotFoundException e ) {
                          quit("File " + logfile + " not found");
                      }
                  }
                  
                  break;
        
                default:
                
                  quit("Unknown command line option: " + args[ arg ] );
            }
            
            ++ arg ;
        }

        new SocketWrenchSession();
        
        SocketUtils.setProtocol("UDP");
        
        UDPNetworkServerImpl network = new UDPNetworkServerImpl( debug );
                            
        try {  
            network.init( port );
            new Thread( new TFTPServer( network , debug ) ).run();
        } catch ( TFTPException e ) {
            e.printStackTrace();
            quit( e );
        }
    }
    
    // State variables
    
    UDPNetworkServerImpl network ;
    
    /**
     * Creates a server with a given network connection and optional debug.
     * 
     * @param network - network connection
     * @param debug - where debug is to be written
     */
    
    public TFTPServer( UDPNetworkServerImpl network , OutputStream debug ){
        
        super( debug );
        
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
            new Thread( new TFTPWorker( newNetwork , debug ) ).run();
          } catch ( TFTPException e ) {    
            e.printStackTrace();
            quit( e );
          }
        }
    }
}
