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
 * Implements various options that are permitted to appear in a TCP message.
 */

public class TCPOptions {

  boolean maxSegmentSizeSet ,
          windowScaleFactorSet ,
          timestampSet ;

  short maxSegmentSize ;

  byte windowScaleFactor ;

  int timestampValue ,
      timestampEchoReply ;

  public boolean isMaxSegmentSizeSet(){
    return maxSegmentSizeSet ;
  }

  public short getMaxSegmentSize() {
    return maxSegmentSize ;
  }

  public void setMaxSegmentSize( short maxSegmentSize ) {
    this.maxSegmentSize = maxSegmentSize ;
    maxSegmentSizeSet = true ;    
  }

  public boolean isWindowScaleFactorSet(){
    return windowScaleFactorSet;
  }

  public byte getWindowScaleFactor() {
    return windowScaleFactor ;
  }

  public void setWindowScaleFactor( byte windowScaleFactor ){
    this.windowScaleFactor = windowScaleFactor ;
    windowScaleFactorSet = true ;
  }

  public boolean isTimestampSet(){
    return timestampSet ;
  }
  
  public int getTimestampValue() {
    return timestampValue ;
  }

  public int getTimestampEchoReply() {
    return timestampEchoReply ;
  }

  public void setTimestamp( int timestampValue , int timestampEchoReply ){
    this.timestampValue = timestampValue ;
    this.timestampEchoReply = timestampEchoReply ;
    timestampSet = true ;
  }
  
  public int length(){
      
      int l = 0 ;
      
      if( maxSegmentSizeSet ){
          l += 4 ;
      }
      
      if( windowScaleFactorSet ){
          l += 4 ;
      }
      
      if( timestampSet ){
          l += 12 ;
      }
      
      return l ;
  }  
}


