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

package com.act365.net.udp ;

/**
 Objects of the class UDPMessage store UDP messages.
*/

public class UDPMessage {

  public short sourceport ;
  public short destinationport ;
  public short length ;
  public short checksum ;
  public byte[] data ;
  public int offset ;
  public int count ;

  /**
   Writes the message to a string.
  */

  public String toString() {

    StringBuffer sb = new StringBuffer();

    sb.append("UDP: source port-");
    sb.append( sourceport >= 0 ? sourceport : sourceport ^ 0xffffff00 );
    sb.append(" destination port-");
    sb.append( destinationport >= 0 ? destinationport : destinationport ^ 0xffffff00 );
    sb.append(" length-");
    sb.append( length );
    sb.append(" bytes");

    return sb.toString();
  }  
  
  /**
   * Calculates the message length in bytes.
   */
  
  public int length() {
      return 8 + count ;  
  }
}

