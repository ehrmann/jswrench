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

package com.act365.net.dns ;

public class ResourceRecord {

  byte[] domain_name ;
  short type ;
  short resource_class ;
  int time_to_live ;
  byte[] data ;
  String data_string ;

  /**
   Trivial constructor
  */

  public ResourceRecord( byte[] domain_name ,
                         short type ,
                         short resource_class ,
                         int time_to_live ,
                         byte[] data ,
                         String data_string ) {
    this.domain_name = domain_name ;
    this.type = type ;
    this.resource_class = resource_class ;
    this.time_to_live = time_to_live ;
    this.data = data ;
    this.data_string = data_string ;
  }

  /**
   Length of byte array
  */

  public int length() {
    return 10 + domain_name.length + data.length ;
  }

  /**
   String representation
  */

  public String toString() {
      StringBuffer sb = new StringBuffer();
      if( type < DNSMessage.dnsTypes.length ){
          sb.append("type ");
          sb.append( DNSMessage.dnsTypes[type] );
          sb.append(':');
      }
      sb.append( data_string );
      return sb.toString();
  }
}

