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

import java.io.* ;

/**
 * Represents a DNS message.
 */

public class DNSMessage {

  // Type and Query Type values

  public final static int A     = 1 ,
                          NS    = 2 ,
                          CNAME = 5 ,
                          PTR   = 12 ,
                          HINFO = 13 ,
                          MX    = 15 ,
                          AXFR  = 252 ,
                          ANY   = 255 ;

  public short identification ;
  public short flags ;
  public Query[] questions ;
  public ResourceRecord[] answers ;
  public ResourceRecord[] authority_records ;
  public ResourceRecord[] additional_records ;

  public void dump( PrintStream printer )throws UnsupportedEncodingException {  
  
    int i = -1 ;

    while( ++ i < questions.length ){
	  printer.println( "QUERY " + (int)( i + 1 ) );
	  printer.println( questions[ i ].toString() );
    }

    i = -1 ;

    while( ++ i < answers.length ){
	  printer.println( "ANSWER " + (int)( i + 1 ) );
	  printer.println( new String( answers[ i ].domain_name , "UTF8" ) + ": " + answers[ i ].toString() );
    }

    i = -1 ;

    while( ++ i < authority_records.length ){
	  printer.println( "AUTHORITY RECORD " + (int)( i + 1 ) );
	  printer.println( new String( authority_records[ i ].domain_name , "UTF8" ) + ": " + authority_records[ i ].toString() );
    }

    i = -1 ;

    while( ++ i < additional_records.length ){
	  printer.println( "ADDITIONAL RECORD " + (int)( i + 1 ) );
	  printer.println( new String( additional_records[ i ].domain_name , "UTF8" ) + ": " + additional_records[ i ].toString() );
    }
  }
  
  /**
   * Calculates the length in bytes of a DNS message.
   */

  public int length(){

      int len = 12 ;

      int i = -1 ;

      while( ++ i < questions.length ){
        len += questions[ i ].length();
      }

      i = -1 ;

      while( ++ i < answers.length ){
        len += answers[ i ].length();
      }

      i = -1 ;

      while( ++ i < authority_records.length ){
        len += authority_records[ i ].length();
      }

      i = -1 ;

      while( ++ i < additional_records.length ){
        len += additional_records[ i ].length();
      }

      return len ;
  }
}

