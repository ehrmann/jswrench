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

package com.act365.net.ip ;

/**
 Stores the contents of an IP4 message.
*/

public class IP4Message {
 
  public byte version ;
  public byte headerlength ;
  public byte typeofservice ;
  public short length ;
  public short identifier ;
  public byte flags ;
  public short offset ;
  public short timetolive ;
  public byte protocol ;
  public short checksum ;
  public byte[] source ;
  public byte[] destination ;
  public int[] options ;
  public byte[] data ;
  public int dataOffset ;
  public int dataCount ;

  /**
   * Calculates the message-length in bytes.
   */
  
  public int length(){
      return 4 * headerlength + dataCount ;
  }
  
  /**
   Writes the message to a string.
  */

  public String toString() {

    StringBuffer sb = new StringBuffer();

    sb.append("IP4: source-");
    sb.append( source[0] >= 0 ? source[0] : 0xffffff00 ^ source[0] );
    sb.append('.');
    sb.append( source[1] >= 0 ? source[1] : 0xffffff00 ^ source[1] );
    sb.append('.');
    sb.append( source[2] >= 0 ? source[2] : 0xffffff00 ^ source[2] );
    sb.append('.');
    sb.append( source[3] >= 0 ? source[3] : 0xffffff00 ^ source[3] );
    sb.append(" destination-");
    sb.append( destination[0] >= 0 ? destination[0] : 0xffffff00 ^ destination[0] );
    sb.append('.');
    sb.append( destination[1] >= 0 ? destination[1] : 0xffffff00 ^ destination[1] );
    sb.append('.');
    sb.append( destination[2] >= 0 ? destination[2] : 0xffffff00 ^ destination[2] );
    sb.append('.');
    sb.append( destination[3] >= 0 ? destination[3] : 0xffffff00 ^ destination[3] );
    sb.append(" length-");
    sb.append( length );
    sb.append(" bytes");

    return sb.toString();
  } 
}

