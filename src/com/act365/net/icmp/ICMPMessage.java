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

import com.act365.net.ip.*;

/**
 Stores the contents of an ICMP message.
*/

public class ICMPMessage {
 
  public byte type ;
  public byte code ;
  public short checksum ;
  public short identifier ;
  public short sequence_number ;
  public byte[] data ;
  public int offset ;
  public int count ;
  public IP4Message ip4Message ;  

  public int length(){
      return 8 + count ;
  }
  
  public String getTypeLabel(){
      return ICMP.typeLabels[ type ];
  }
  
  public String getCodeLabel(){
      switch( type ){
          case ICMP.ICMP_DEST_UNREACH:
              return ICMP.unreachLabels[ code ];
              
          case ICMP.ICMP_REDIRECT:
              return ICMP.redirectLabels[ code ];
              
          case ICMP.ICMP_TIME_EXCEEDED:
              return ICMP.timeExceededLabels[ code ];
              
          default:
              return "";
      }
  }

  public boolean isQuery(){

      switch( type ){

      case ICMP.ICMP_ECHOREPLY:
      case ICMP.ICMP_ECHO:
      case ICMP.ICMP_ROUTERADVERT:
      case ICMP.ICMP_ROUTERSOLICIT:
      case ICMP.ICMP_TIMESTAMP:
      case ICMP.ICMP_TIMESTAMPREPLY:
      case ICMP.ICMP_INFO_REQUEST:
      case ICMP.ICMP_INFO_REPLY:
      case ICMP.ICMP_ADDRESS:
      case ICMP.ICMP_ADDRESSREPLY:

        return true ;

      case ICMP.ICMP_DEST_UNREACH:
      case ICMP.ICMP_SOURCE_QUENCH:
      case ICMP.ICMP_REDIRECT:
      case ICMP.ICMP_TIME_EXCEEDED:
      case ICMP.ICMP_PARAMETERPROB:
      default:
      
        return false ;
      }        
  }
  
  public String toString() {
      
      StringBuffer sb = new StringBuffer();
      
      sb.append("ICMP: ");
      sb.append( getTypeLabel() );
      
      final String codeLabel = getCodeLabel();
      
      if( codeLabel.length() > 0 ){
          sb.append(" (");
          sb.append( codeLabel );
          sb.append(')');
      }
      
      if( isQuery() ){      
          sb.append(" identifier-");
          sb.append( identifier >= 0 ? identifier : identifier ^ 0xffffff00 );
          sb.append(" seq-");
          sb.append( sequence_number >= 0 ? sequence_number : sequence_number ^ 0xffffff00 );
      }

      sb.append(" length-");
      sb.append( count );
      sb.append(" bytes");
      
      return sb.toString();
  }
}

