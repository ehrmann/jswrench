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

package com.act365.net.tcp ;

import com.act365.net.* ;

/**
 * Class <code>TCP</code> defines constants associated with the TCP protocol.
 */

public class TCP {

  // TCP flags

  public final static byte FIN = 0x01 ,
                           SYN = 0x02 ,
                           RST = 0x04 ,
                           PSH = 0x08 ,
                           ACK = 0x10 ,
                           URG = 0x20 ;

  // TCP states

  public final static int CLOSED = 1 ,
                          LISTEN = 2 ,
                          SYN_RCVD = 3 ,
                          SYN_SENT = 4 ,
                          ESTABLISHED = 5 ,
                          CLOSE_WAIT = 6 ,
                          FIN_WAIT_1 = 7 ,
                          CLOSING = 8 ,
                          LAST_ACK = 9 ,
                          FIN_WAIT_2 = 10 ,
                          TIME_WAIT = 11 ;
                          
  public final static String[] statelabels = { "UNSET" ,
                                               "CLOSED" ,
                                               "LISTEN" ,
                                               "SYN_RCVD" ,
                                               "SYN_SENT" ,
                                               "ESTABLISHED" ,
                                               "CLOSE_WAIT" ,
                                               "FIN_WAIT_1" ,
                                               "CLOSING" ,
                                               "LAST_ACK" ,
                                               "FIN_WAIT_2" ,
                                               "TIME_WAIT" };
               
  /**
   * Creates a blank TCPMessage or TCPJMessage object, depending upon
   * the global protocol that has been chosen.
   */
               
  public static TCPMessage createMessage() {
      switch( SocketWrenchSession.getProtocol() ){
          case SocketConstants.IPPROTO_TCP:
              return new TCPMessage();
          case SocketConstants.IPPROTO_TCPJ:
              return new TCPJMessage();
          default:
              return null ;                                    
      }
  }
  
  /**
   * Creates a populated TCPMessage or TCPJMessage object, depending upon
   * the global protocol that has been chosen.
   */         

  public static TCPMessage createMessage( short sourceport ,
                                          short destinationport ,
                                          int sequencenumber ,
                                          int acknowledgementnumber ,
                                          boolean ack ,
                                          boolean rst ,
                                          boolean syn ,
                                          boolean fin ,
                                          boolean psh ,
                                          short windowsize ,
                                          TCPOptions options ,
                                          byte[] writebuffer ,
                                          int writestart ,
                                          int writeend ){
                         
      switch( SocketWrenchSession.getProtocol() ){
          
          case SocketConstants.IPPROTO_TCP:
          
              return new TCPMessage( sourceport ,
                                     destinationport ,
                                     sequencenumber ,
                                     acknowledgementnumber ,
                                     ack ,
                                     rst ,
                                     syn ,
                                     fin ,
                                     psh ,
                                     windowsize ,
                                     options ,
                                     writebuffer ,
                                     writestart ,
                                     writeend );
          
          case SocketConstants.IPPROTO_TCPJ:
          
              return new TCPJMessage( sourceport ,
                                      destinationport ,
                                      sequencenumber ,
                                      acknowledgementnumber ,
                                      ack ,
                                      rst ,
                                      syn ,
                                      fin ,
                                      psh ,
                                      windowsize ,
                                      options ,
                                      writebuffer ,
                                      writestart ,
                                      writeend );
          
          default:
              return null ;
      }
  }
}

