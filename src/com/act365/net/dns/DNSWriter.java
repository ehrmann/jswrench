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
 
import com.act365.net.*;

import java.io.IOException ;

/**
 DNSWriter writes DNS messages to a byte stream.
*/

public class DNSWriter {

  public final static int QUERY = 0 ,
                          RESPONSE = 1 ,
                          STANDARD_QUERY = 0 ,
                          INVERSE_QUERY = 1 ,
                          SERVER_STATUS_REQUEST = 2 ,
                          AUTHORITATIVE_ANSWER = 1 ,
                          TRUNCATED = 1 ,
                          RECURSION_DESIRED = 1 ,
                          ITERATIVE_QUERY = 0 ,
                          RECURSIVE_QUERY = 1 ,
                          RECURSION_AVAILABLE = 1 ,
                          NO_ERROR = 0 ,
                          NAME_ERROR = 3 ;
 
  static int write( Query query , byte[] buffer , int offset ){
    
    final int length = query.query_name.length + 4 ;

    int i = -1 ;

    while( ++ i < query.query_name.length ){
      buffer[ i + offset ] = query.query_name[ i ];
    }

    SocketUtils.shortToBytes( query.query_type , buffer , offset + query.query_name.length );
    SocketUtils.shortToBytes( query.query_class , buffer , offset + query.query_name.length + 2 );

    return length ;
  }

  static int write( ResourceRecord record , byte[] buffer , int offset ){
    
    final int length = record.domain_name.length + 10 + record.data.length ;

    int i = -1 ;

    while( ++ i < record.domain_name.length ){
      buffer[ i + offset ] = record.domain_name[i];
    }

    SocketUtils.shortToBytes( record.type , buffer , offset + record.domain_name.length );
    SocketUtils.shortToBytes( record.resource_class , buffer , offset + record.domain_name.length + 2 );
    SocketUtils.intToBytes( record.time_to_live , buffer , offset + record.domain_name.length + 4 );
    SocketUtils.shortToBytes( (short) record.data.length , buffer , offset + record.domain_name.length + 8 );

    i = record.domain_name.length + 10 - 1 ;

    while( ++ i < length ){
      buffer[ i + offset ] = record.data[ i - record.domain_name.length - 10 ];
    }

    return length ;
  }

  static int write( DNSMessage message , byte[] buffer , int offset , int count ) throws IOException {

    final int length = message.length();

    if( count < length ){
        throw new IOException("DNS Write buffer overflow");    
    }
    
    SocketUtils.shortToBytes( message.identification , buffer , offset );
    SocketUtils.shortToBytes( message.flags , buffer , offset + 2 );
    SocketUtils.shortToBytes( (short) message.questions.length , buffer , offset + 4 );
    SocketUtils.shortToBytes( (short) message.answers.length , buffer , offset + 6 );
    SocketUtils.shortToBytes( (short) message.authority_records.length , buffer , offset + 8 );
    SocketUtils.shortToBytes( (short) message.additional_records.length , buffer , offset + 10 );
   
    int cursor = offset + 12 ;

    int i = -1 ;

    while( ++ i < message.questions.length ){
      cursor += write( message.questions[ i ] , buffer , cursor );
    }

    i = -1 ;

    while( ++ i < message.answers.length ){
      cursor += write( message.answers[ i ] , buffer , cursor );
    }

    i = -1 ;

    while( ++ i < message.authority_records.length ){
      cursor += write( message.authority_records[ i ] , buffer , cursor );
    }

    i = -1 ;

    while( ++ i < message.additional_records.length ){
      cursor += write( message.additional_records[ i ] , buffer , cursor );
    }

    return length ;
  }

  /**
   Generates a standard DNS Query.
  */

  public static int write( short identification ,
                           boolean recursion_desired ,
                           String domain ,
                           byte[] buffer ,
                           int offset ,
                           int count ) throws IOException {

    short flags = 0 ;

    flags &= (byte)( QUERY << 15 );
    flags &= (byte)( STANDARD_QUERY << 11 );

    if( recursion_desired ){
      flags &= (byte)( RECURSION_DESIRED << 8 );
    }

    Query[] questions = new Query[ 1 ];

    questions[ 0 ] = new Query( domain );

    DNSMessage message = new DNSMessage();

    message.identification = identification ;
    message.flags = flags ;
    message.questions = questions ;
    message.answers = new ResourceRecord[ 0 ];
    message.authority_records = new ResourceRecord[ 0 ];
    message.additional_records = new ResourceRecord[ 0 ];
 
    return write( message , buffer , offset , count );
  }
}

