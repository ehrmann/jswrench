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

import java.io.* ;

/**
 * ErrorHandler provides error-handling for the entire TFTP project.
 */

public final class ErrorHandler {
    
    static PrintWriter writer = null ;

    static boolean trace = false ;
        
    /**
     * Creates an error-handler
     * 
     * @param debug - where debug is to be written (null if no debug required)
     */
    
    public static void setDebugStream( OutputStream debug ){
        if( debug instanceof OutputStream ){
            writer = new PrintWriter( debug , true );
        } else {
            writer = null ;       
        }
    }

    /**
     * Switches the trace facility on or off.
     * 
     * @param newTrace
     */
    
    public static void setTrace( boolean newTrace ){
        trace = newTrace ;
    }
    
    /**
     * Toggles the trace facility.
     */
        
    public static void toggleTrace(){
        trace = ! trace ;
    }
    
    /**
     * Writes debug to the chosen stream.
     * 
     * @param errortext - debug text
     */
    
    public static void debug( String errortext ){       
        if( trace && writer instanceof PrintWriter ){
            writer.println( errortext );
        }
    }
    
    /**
     * Handles abort errors.
     *  
     * @param errortext
     * @throws TFTPException
     */
    
    public static void dump( String errortext ) throws TFTPException {
        throw new TFTPException( errortext );
    }
    
    /**
     * Handles system errors.
     *  
     * @param errortext
     * 
     * @throws TFTPException
     */
        
    public static void system( String errortext ) throws TFTPSystemException {
        throw new TFTPSystemException( errortext );
    }
    
    /**
     * Handles TFTP command-line errors.
     * 
     * @param errortext
     * @throws TFTPCommandException
     */
        
    public static void command( String errortext ) throws TFTPCommandException {
        throw new TFTPCommandException( errortext );
    }
    
    /**
     * Handles fatal errors
     * 
     * @param errortext
     */
    
    public static void quit( String errortext ){
        System.err.println( errortext );
        System.exit( 1 );
    }
    
    /**
     * Handles fatal system errors
     * 
     * @param errortext
     * @param e
     */
    
    public static void quit( Exception e ){        
        quit( e.getMessage() );
    }
}
