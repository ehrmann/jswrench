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

package com.act365.net;

import java.util.Date ;

/**
 * A RetransmissionTimer object stores all of the information about 
 * round-trip times on a network connection in order to calculate
 * the retransmission intervals necessary to provide a reliable service.
 */

public class RetransmissionTimer {
    
    final static int[] exponentialBackoff = new int[] { 1 , 2 , 4 , 8 , 16 };
    
    final static int minRetransmissionTimeout = 2 ,
                     maxRetransmissionTimeout = 120 ,
                     maxRetransmissions = exponentialBackoff.length - 1 ;
                            
    final static double defaultFirstTimeout = 3 ;
    
    double roundTripTime ;
    double smoothedRoundTripTime ;
    double roundTripTimeDeviation ;
    int    nRetransmissions ;
    int    currentRetransmissionTimeout ;
    int    nextRetransmissionTimeout ;
    long   startTime ;
    
    /**
     * Creates a RetransmissionTimer object that will (if necessary) 
     * schedule a first retransmission after a given time.
     * 
     * @param firstTimeout - first retransmission time /s
     */
    
    public RetransmissionTimer( double firstTimeout ) {
        
        roundTripTime = 0 ;
        smoothedRoundTripTime = 0 ;
        roundTripTimeDeviation = ( firstTimeout - smoothedRoundTripTime )/ 2 ;
        nRetransmissions = 0 ;
        currentRetransmissionTimeout = 0 ;
        nextRetransmissionTimeout = 0 ;
        startTime = 0 ;
    }

    /**
     * Creates a RetransmissionTimer object that will (if necessary) 
     * schedule a first retransmission after 3s.
     */
        
    public RetransmissionTimer(){
        this( defaultFirstTimeout );
    }    
    
    /**
     * Called before a new packet is sent for the first time.
     */
    
    public void newPacket(){
        nRetransmissions = 0 ;
    }
    
    /**
     * Starts the timer. 
     * The function should be called immediately before the alarm
     * is invoked prior to the receipt of a packet. 
     * 
     * @return the RTO value for the alarm
     */
    
    public int start(){
        
        if( nRetransmissions > 0 ){

            /*
             * Since this is a retransmission, the RTT won't be updated
             * - simply apply the exponential backoff.
             */    
             
            currentRetransmissionTimeout *= exponentialBackoff[ nRetransmissions ];
            
            return currentRetransmissionTimeout ;        
        }
        
        startTime = new Date().getTime();
        
        if( nextRetransmissionTimeout > 0 ){
            
            /*
             * This is the first retransmission of a packet AND the last
             * packet had to be retransmitted. Therefore, we'll use the
             * final RTO for the previous packet as the starting RTO for
             * this packet.  
             */
             
            currentRetransmissionTimeout = nextRetransmissionTimeout ;
            nextRetransmissionTimeout = 0 ;
            
            return currentRetransmissionTimeout ;
        }
        
        /*
         * Calculate the timeout value from the current estimators:
         *     smoothed RTT plus twice the deviation.
         */
         
        int retransmissionTimeout = (int)( smoothedRoundTripTime + 2 * roundTripTimeDeviation + 0.5 ); 
        
        if( retransmissionTimeout < minRetransmissionTimeout ){
            retransmissionTimeout = minRetransmissionTimeout ;
        } else if( retransmissionTimeout > maxRetransmissionTimeout ){
            retransmissionTimeout = maxRetransmissionTimeout ;
        } 
        
        return currentRetransmissionTimeout = retransmissionTimeout ;
    }
    
    /**
     * Stops the timer. 
     * The function should be called immediately after the alarm
     * has been disabled after the receipt of a packet or after
     * a timeout. 
     */
        
    public void stop(){
        
        if( nRetransmissions > 0 ){
            
            /*
             * The response is for a retransmitted packet, so we don't
             * update the estimators. However, the RTO is saved as it
             * will be used for the next packet.
             */
             
            nextRetransmissionTimeout = currentRetransmissionTimeout ;
            return ;
        }
        
        nextRetransmissionTimeout = 0 ; // For next call to start().
        
        long stopTime = new Date().getTime();
        
        roundTripTime = ( stopTime - startTime )/ 1000 ; 
        
        /*
         * Update estimates of RTT and the mean deviation of RTT.
         * See the Jacobson SIGCOMM '88 paper (Appendix A) for details.
         */

        double err = roundTripTime - smoothedRoundTripTime ;
        
        smoothedRoundTripTime = err / 8 ;
        
        if( err < 0 ){
            err = - err ;        
        }
        
        roundTripTimeDeviation += ( err - roundTripTimeDeviation )/ 4 ;
    }
    
    /**
     * Called immediately after a timeout has occurred.
     * 
     * @return whether it's time to abort
     */
    
    public boolean timeout(){
        
        stop();
        
        return ++ nRetransmissions > maxRetransmissions ;
    }
    
    /**
     * String representation
     */
    
    public String toString(){
        return "RTT = " + roundTripTime + 
               "s, Smoothed RTT = " + smoothedRoundTripTime +
               "s, RTT deviation = " + roundTripTimeDeviation +
               "s, RTO = " + currentRetransmissionTimeout + "s"; 
    }
}
