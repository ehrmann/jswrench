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

import java.io.IOException ;
import java.util.*;

/**
 Represents a DNS Query Message.
*/

public class Query {
 
  byte[] query_name ;
  short query_type ;
  short query_class ;
  String domain_name ;

  /**
   Trivial Query constructor
  */

  public Query( byte[] query_name , short query_type , short query_class , String domain_name ) {
  
    this.query_name = query_name ;
    this.query_type = query_type ;
    this.query_class = query_class ;
    this.domain_name = domain_name ;
  }

  /**
   Constructs a Query from a string representation of
   a single domain name.
  */

  public Query( int dnstype , String domain_name ) throws IOException {

    this.domain_name = domain_name ;

    query_name = new byte[ domain_name.length() + 2 ];

    parse( domain_name , query_name , 0 );

    query_type  = (short) dnstype ;
    query_class = (short) 1 ;
  }

  /**
   Message length
  */

  public int length() {
    return 4 + query_name.length ;
  }

  /**
   String representation
  */

  public String toString() {
      StringBuffer sb = new StringBuffer();
      if( query_type < DNSMessage.dnsTypes.length ){
          sb.append("type ");
          sb.append( DNSMessage.dnsTypes[query_type] );
          sb.append(':');
      }
      sb.append( domain_name );
      return sb.toString();
  }

  /**
   Converts a string representation of a domain name, e.g. "www.act365.com"
   into the byte stream expected by the query_name member.
  */

  static int parse( String name , byte[] buffer , int offset ) throws IOException {

    StringTokenizer parser = new StringTokenizer( name , "." );

    byte[] label ;

    int i ;

    while( parser.hasMoreTokens() ){
      label = parser.nextToken().getBytes("UTF8");
      if( label.length > 63 ){
        throw new IOException("Maximum label length exceeded");
      }
      buffer[ offset ++ ] = (byte) label.length ;
      i = -1 ;
      while( ++ i < label.length ){
        buffer[ offset ++ ] = label[ i ];
      }
    }

    buffer[ offset ++ ] = (byte) 0 ;

    return offset ;
  }
}

