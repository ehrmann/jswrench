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
 * TFTPWorker instances are spawned by the server in order to handle 
 * new client requests. 
 */

public class TFTPWorker extends TFTPBase implements Runnable {

    // Finite State Machine function objects for the server
     
    final static IFSMFunction[][] fsmFunctions = new IFSMFunction[][] { 
             
            { new FSMInvalid() ,        // [ sent = 0 ]        [ received = 0 ]
              new FSMReceiveRRQ() ,     // [ sent = 0 ]        [ received = OP_RRQ ]
              new FSMReceiveWRQ() ,     // [ sent = 0 ]        [ received = OP_WRQ ]
              new FSMInvalid() ,        // [ sent = 0 ]        [ received = OP_DATA ]
              new FSMInvalid() ,        // [ sent = 0 ]        [ received = OP_ACK ]
              new FSMInvalid() } ,      // [ sent = 0 ]        [ received = OP_ERROR ]
             
            { new FSMInvalid() ,        // [ sent = OP_RRQ ]   [ received = 0 ]
              new FSMInvalid() ,        // [ sent = OP_RRQ ]   [ received = OP_RRQ ]
              new FSMInvalid() ,        // [ sent = OP_RRQ ]   [ received = OP_WRQ ]
              new FSMInvalid() ,        // [ sent = OP_RRQ ]   [ received = OP_DATA ]
              new FSMInvalid() ,        // [ sent = OP_RRQ ]   [ received = OP_ACK ]
              new FSMInvalid() } ,      // [ sent = OP_RRQ ]   [ received = OP_ERROR ]
             
            { new FSMInvalid() ,        // [ sent = OP_WRQ ]   [ received = 0 ]
              new FSMInvalid() ,        // [ sent = OP_WRQ ]   [ received = OP_RRQ ]
              new FSMInvalid() ,        // [ sent = OP_WRQ ]   [ received = OP_WRQ ]
              new FSMInvalid() ,        // [ sent = OP_WRQ ]   [ received = OP_DATA ]
              new FSMInvalid() ,        // [ sent = OP_WRQ ]   [ received = OP_ACK ]
              new FSMInvalid() } ,      // [ sent = OP_WRQ ]   [ received = OP_ERROR ]
             
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
     * Creates a new thread with a given network connection and
     * optional debug.
     * 
     * @param network - network connection
     * @param debug - where debug is to be written
     */
    
    public TFTPWorker( INetworkImplBase network ){
        super( network );
    }
    
    /**
     *  Closes the server.
     */
        
    void serverClose( TFTPMessage message ) throws TFTPException {
        
        if( message.count < TFTPConstants.maxData - 4 ){
            close();
        }
    }

    /**
     * Returns Finite State Machine functions for the TFTP server.
     */
     
    IFSMFunction[][] getFSMFunctions() { return fsmFunctions ;}

    /**
     * Processes client request.
     */
    
	public void run() {
        try {
            fsmLoop( TFTPConstants.OP_NULL );
            network.close();
        } catch ( TFTPException e ){
            e.printStackTrace();
        } 
	}
}
