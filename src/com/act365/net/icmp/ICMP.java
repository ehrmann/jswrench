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

package com.act365.net.icmp ;

/**
 The ICMP interface defines the constants associated with the ICMP protocol.
*/

public interface ICMP {

  /**
   ICMP types
  */

  public static byte ICMP_ECHOREPLY      = 0 ,
                     ICMP_DEST_UNREACH   = 3 ,
                     ICMP_SOURCE_QUENCH  = 4 ,
                     ICMP_REDIRECT       = 5 ,
                     ICMP_ECHO           = 8 ,
                     ICMP_ROUTERADVERT   = 9 ,
                     ICMP_ROUTERSOLICIT  = 10 ,
                     ICMP_TIME_EXCEEDED  = 11 ,
                     ICMP_PARAMETERPROB  = 12 ,
                     ICMP_TIMESTAMP      = 13 ,
                     ICMP_TIMESTAMPREPLY = 14 ,
                     ICMP_INFO_REQUEST   = 15 ,
                     ICMP_INFO_REPLY     = 16 ,
                     ICMP_ADDRESS        = 17 ,
                     ICMP_ADDRESSREPLY   = 18 ;

  /**
   Labels associated with ICMP types
  */

  public static String[] typeLabels = { "Echo Reply" ,
                                        "" ,
                                        "" ,
                                        "Destination Unreachable" ,
                                        "Source Quench" ,
                                        "Redirect" ,
                                        "" ,
                                        "" ,
                                        "Echo Request" ,
                                        "Router Advertisment" ,
                                        "Router Solicitation" ,
                                        "Time Exceeded" ,
                                        "Parameter Problem" ,
                                        "Timestamp Request" ,
                                        "Timestamp Reply" ,
                                        "Information Request" ,
                                        "Information Reply" ,
                                        "Address Mask Request",
                                        "Address Mask Reply" };

  /**
   Codes for the ICMP_DEST_UNREACH type
  */ 

  public static byte ICMP_NET_UNREACH    = 0 ,
                     ICMP_HOST_UNREACH   = 1 ,
                     ICMP_PROT_UNREACH   = 2 ,
                     ICMP_PORT_UNREACH   = 3 ,
                     ICMP_FRAG_NEEDED    = 4 ,
                     ICMP_SR_FAILED      = 5 ,
                     ICMP_NET_UNKNOWN    = 6 ,
                     ICMP_HOST_UNKNOWN   = 7 ,
                     ICMP_HOST_ISOLATED  = 8 ,
                     ICMP_NET_ANO        = 9 ,
                     ICMP_HOST_ANO       = 10 ,
                     ICMP_NET_UNR_TOS    = 11 ,
                     ICMP_HOST_UNR_TOS   = 12 ,
                     ICMP_PKT_FILTERED   = 13 ,
                     ICMP_PREC_VIOLATION = 14 ,
                     ICMP_PREC_CUTOFF    = 15 ;

  /**
   Labels associated with the ICMP_DEST_UNREACH type
  */

  public static String[] unreachLabels = { "Network Unreachable" ,
                                           "Host Unreachable" ,
                                           "Protocol Unreachable" ,
                                           "Port Unreachable" ,
                                           "Fragmentation necessary but DF bit set" ,
                                           "Source Route failed" ,
                                           "Destination Network Unknown" ,
                                           "Destination Host Unknown" ,
                                           "Source Host Isolated (obsolete)" ,
                                           "Destination Network Administratively Prohibited" ,
                                           "Destination Host Administratively Prohibited" ,
                                           "Network Unreachable for TOS" ,
                                           "Host Unreachable for TOS" ,
                                           "Communication Administratively Prohibited by Filtering" ,
                                           "Host Precedence Violation" ,
                                           "Precedence Cutoff in effect" };

  /**
   Codes associated with the ICMP_REDIRECT type
  */

  public static int ICMP_REDIR_NET     = 0 ,
                    ICMP_REDIR_HOST    = 1 ,
                    ICMP_REDIR_NETTOS  = 2 ,
                    ICMP_REDIR_HOSTTOS = 3 ;

  /**
   Labels associated with the ICMP_REDIRECT type
  */

  public static String[] redirectLabels = { "Redirect for Network" ,
                                            "Redirect for Host" ,
                                            "Redirect for Type-of-Service and Network" ,
                                            "Redirect for Type-of-Service and Host" };

  /**
   Codes associated with the ICMP_TIME_EXCEEDED type
  */

  public static int ICMP_EXC_TTL      = 0 ,
                    ICMP_EXC_FRAGTIME = 1 ;

  /**
   Labels associated with the ICMP_TIME_EXCEEDED type
  */

  public static String[] timeExceededLabels = { "TTL count exceeded during transit" ,
                                                "TTL count exceeded during reassembly" };
}

