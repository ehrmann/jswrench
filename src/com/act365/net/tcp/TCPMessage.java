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

/**
 * Stores a TCP message.
 */

public class TCPMessage {

  public short sourceport ;
  public short destinationport ;
  public int sequencenumber ;
  public int acknowledgementnumber ;
  public int headerlength ;
  public boolean urg ;
  public boolean ack ;
  public boolean psh ;
  public boolean rst ;
  public boolean syn ;
  public boolean fin ;
  public short windowsize ;
  public short checksum ;
  public short urgentpointer ;
  public byte[] options ;
  public byte[] data ; 
}

