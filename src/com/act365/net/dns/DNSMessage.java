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

import com.act365.net.* ;

import java.io.* ;

/**
 * Represents a DNS message.
 */

public class DNSMessage implements IServiceMessage {

  // Type and Query Type values

    public final static int A     = 1 ,
                            NS    = 2 ,
                            CNAME = 5 ,
                            PTR   = 12 ,
                            HINFO = 13 ,
                            MX    = 15 ,
                            AXFR  = 252 ,
                            ANY   = 255 ;

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

  // Members
   
  public short identification ;
  public short flags ;
  public Query[] questions ;
  public ResourceRecord[] answers ;
  public ResourceRecord[] authority_records ;
  public ResourceRecord[] additional_records ;

  /**
   * Creates a blank DNS query.
   */
  
  public DNSMessage() {
  }
  
  /**
   Creates a standard DNS Query.
  */

  public DNSMessage( short identification ,
                     boolean recursion_desired ,
                     String domain ) throws IOException {

    short flags = 0 ;

    flags &= (byte)( QUERY << 15 );
    flags &= (byte)( STANDARD_QUERY << 11 );

    if( recursion_desired ){
      flags &= (byte)( RECURSION_DESIRED << 8 );
    }

    Query[] questions = new Query[ 1 ];

    questions[ 0 ] = new Query( domain );

    this.identification = identification ;
    this.flags = flags ;
    this.questions = questions ;
    this.answers = new ResourceRecord[ 0 ];
    this.authority_records = new ResourceRecord[ 0 ];
    this.additional_records = new ResourceRecord[ 0 ];
  }

  /**
   * Returns the service name, e.g. "DNS".
   * @return protocol name
   */
    
  public String getServiceName(){
      return "DNS";
  }
    
  /**
   * Returns the well-known port number associated with the service.
   */
    
  public int getWellKnownPort(){
      return 53 ;
  }
  
  /**
   * Dumps a message description.
   */  
  
  public void dump( PrintStream printer ) {  
  
    int i = -1 ;

    while( ++ i < questions.length ){
	  printer.println( "QUERY " + (int)( i + 1 ) );
	  printer.println( questions[ i ].toString() );
    }

    i = -1 ;

    while( ++ i < answers.length ){
	  printer.println( "ANSWER " + (int)( i + 1 ) );
	  printer.println( new String( answers[ i ].domain_name ) + ": " + answers[ i ].toString() );
    }

    i = -1 ;

    while( ++ i < authority_records.length ){
	  printer.println( "AUTHORITY RECORD " + (int)( i + 1 ) );
	  printer.println( new String( authority_records[ i ].domain_name ) + ": " + authority_records[ i ].toString() );
    }

    i = -1 ;

    while( ++ i < additional_records.length ){
	  printer.println( "ADDITIONAL RECORD " + (int)( i + 1 ) );
	  printer.println( new String( additional_records[ i ].domain_name ) + ": " + additional_records[ i ].toString() );
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

  /**
   Extracts a domain name string from a DNS message, even if compressed.
  */

  static int domainName( int initialOffset ,
                         byte[] buffer , 
                         int offset , 
                         StringBuffer name ) {

    final int start = offset ;

    byte next = buffer[ offset ++ ];

    int inext = next >= 0 ? next : 0xffffff00 ^ next ;

    while( inext != 0x00 ){
      if( inext >>> 6 == 0x03 ){
        StringBuffer compressed = new StringBuffer();
        int compressed_offset = ( inext & 0x3F ) << 8 ;
        next = buffer[ offset ++ ];
        inext = next >= 0 ? next : 0xffffff00 ^ next ;
        compressed_offset |= inext ;
        domainName( initialOffset , buffer , compressed_offset + initialOffset , compressed );
        name.append( compressed.toString() );
        break;
      } else {
        name.append( new String( buffer , offset , inext ) );
        offset += inext ;
      }
      next = buffer[ offset ++ ];
      inext = next >= 0 ? next : 0xffffff00 ^ next ;
      if( inext > 0 ){
        name.append('.');
      }
    }

    return offset - start ;
  }

  /**
   Converts the data in a ResourceRecord object into an appropriate string.
  */

  static String dataString( int initialOffset , byte[] buffer , int offset , int count , short type ) {

    StringBuffer name = new StringBuffer();

    final int start = offset ;

    switch( type ){

      case DNSMessage.A :

        while( offset < start + count ){
          int datum = buffer[ offset ];
          name.append( datum >= 0 ? datum : 0xffffff00 ^ datum );
          if( offset ++ < start + count - 1 ){
            name.append('.');
          }
        }
 
        break;

      case DNSMessage.NS :
      case DNSMessage.CNAME :
      case DNSMessage.PTR :

        domainName( initialOffset , buffer , offset , name );

        break;

      default:

    }
        
    return name.toString();
  }
  
  /**
   Reads all of the information from a DNS response.
  */

  public int read( byte[] buffer , int offset , int count ) throws IOException {
      
      int length = 0 ;
      try {
          count -= ( length = read( buffer , offset ) );
      } catch ( ArrayIndexOutOfBoundsException e ) {
          throw new IOException("DNS Read buffer overflow");
      }
      
      if( count != 0 ){
          throw new IOException("DNS message format error");
      }
      
      return length ;
  }
  
  int read( byte[] buffer , int offset ) throws UnsupportedEncodingException {

    final int initialOffset = offset ;
    
    identification = SocketUtils.shortFromBytes( buffer , offset );
    offset += 2 ;
    flags = SocketUtils.shortFromBytes( buffer , offset );
    offset += 2 ;

    short n_questions ,
          n_answers ,
          n_authority ,
          n_additional ;

    n_questions  = SocketUtils.shortFromBytes( buffer , offset );
    offset += 2 ;
    n_answers    = SocketUtils.shortFromBytes( buffer , offset );
    offset += 2 ;
    n_authority  = SocketUtils.shortFromBytes( buffer , offset );
    offset += 2 ;
    n_additional = SocketUtils.shortFromBytes( buffer , offset );
    offset += 2 ;
    
    questions = new Query[ n_questions ];
    answers = new ResourceRecord[ n_answers ];
    authority_records = new ResourceRecord[ n_authority ];
    additional_records = new ResourceRecord[ n_additional ];

    int i = -1 , j ;

    StringBuffer name ;

    String datastring ;

    byte[] namebuffer , databuffer ;

    short type_value , class_value , data_length ;

    int time_to_live ;

    while( ++ i < n_questions ){
      name = new StringBuffer();
      offset += domainName( initialOffset , buffer , offset , name );
      namebuffer = name.toString().getBytes("UTF8");
      type_value = SocketUtils.shortFromBytes( buffer , offset );
      offset += 2 ;
      class_value = SocketUtils.shortFromBytes( buffer , offset ); 
      offset += 2 ;

      questions[ i ] = new Query( namebuffer , type_value , class_value , name.toString() );
    }

    i = -1 ;

    while( ++ i < n_answers ){
      name = new StringBuffer();
      offset += domainName( initialOffset , buffer , offset , name );
      namebuffer = name.toString().getBytes("UTF8");
      type_value = SocketUtils.shortFromBytes( buffer , offset );
      offset += 2 ;
      class_value = SocketUtils.shortFromBytes( buffer , offset ); 
      offset += 2 ;
      time_to_live = SocketUtils.intFromBytes( buffer , offset );
      offset += 4 ;
      data_length = SocketUtils.shortFromBytes( buffer , offset );
      offset += 2 ;
      datastring = dataString( initialOffset , buffer , offset , data_length , type_value );
      databuffer = new byte[ data_length ];
      j = -1 ;
      while( ++ j < data_length ){
        databuffer[ j ] = buffer[ offset ++ ];
      }

      answers[ i ] = new ResourceRecord( namebuffer , 
                                         type_value , 
                                         class_value ,
                                         time_to_live ,
                                         databuffer ,
                                         datastring );
    }

    i = -1 ;

    while( ++ i < n_authority ){
      name = new StringBuffer();
      offset += domainName( initialOffset , buffer , offset , name );
      namebuffer = name.toString().getBytes("UTF8");
      type_value = SocketUtils.shortFromBytes( buffer , offset );
      offset += 2 ;
      class_value = SocketUtils.shortFromBytes( buffer , offset ); 
      offset += 2 ;
      time_to_live = SocketUtils.intFromBytes( buffer , offset );
      offset += 4 ;
      data_length = SocketUtils.shortFromBytes( buffer , offset );
      offset += 2 ;
      datastring = dataString( initialOffset , buffer , offset , data_length , type_value );
      databuffer = new byte[ data_length ];
      j = -1 ;
      while( ++ j < data_length ){
        databuffer[ j ] = buffer[ offset ++ ];
      }

      authority_records[ i ] = new ResourceRecord( namebuffer , 
                                                   type_value , 
                                                   class_value ,
                                                   time_to_live ,
                                                   databuffer ,
                                                   datastring );
    }

    i = -1 ;

    while( ++ i < n_additional ){
      name = new StringBuffer();
      offset += domainName( initialOffset , buffer , offset , name );
      namebuffer = name.toString().getBytes("UTF8");
      type_value = SocketUtils.shortFromBytes( buffer , offset );
      offset += 2 ;
      class_value = SocketUtils.shortFromBytes( buffer , offset ); 
      offset += 2 ;
      time_to_live = SocketUtils.intFromBytes( buffer , offset );
      offset += 4 ;
      data_length = SocketUtils.shortFromBytes( buffer , offset );
      offset += 2 ;
      datastring = dataString( initialOffset , buffer , offset , data_length , type_value );
      databuffer = new byte[ data_length ];
      j = -1 ;
      while( ++ j < data_length ){
        databuffer[ j ] = buffer[ offset ++ ];
      }

      additional_records[ i ] = new ResourceRecord( namebuffer , 
                                                    type_value , 
                                                    class_value ,
                                                    time_to_live ,
                                                    databuffer ,
                                                    datastring );
    }
      
    return offset - initialOffset ;
  }

  /**
   * Writes the message into a byte-stream at the given position.
   * 
   * @param buffer 
   * @param offset
   * @return number of bytes written
   */

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

  /**
   * Writes the message into a buffer.
   */
  
  public int write( byte[] buffer , int offset ) throws IOException {

    final int length = length();

    SocketUtils.shortToBytes( identification , buffer , offset );
    SocketUtils.shortToBytes( flags , buffer , offset + 2 );
    SocketUtils.shortToBytes( (short) questions.length , buffer , offset + 4 );
    SocketUtils.shortToBytes( (short) answers.length , buffer , offset + 6 );
    SocketUtils.shortToBytes( (short) authority_records.length , buffer , offset + 8 );
    SocketUtils.shortToBytes( (short) additional_records.length , buffer , offset + 10 );
   
    int cursor = offset + 12 ;

    int i = -1 ;

    while( ++ i < questions.length ){
      cursor += write( questions[ i ] , buffer , cursor );
    }

    i = -1 ;

    while( ++ i < answers.length ){
      cursor += write( answers[ i ] , buffer , cursor );
    }

    i = -1 ;

    while( ++ i < authority_records.length ){
      cursor += write( authority_records[ i ] , buffer , cursor );
    }

    i = -1 ;

    while( ++ i < additional_records.length ){
      cursor += write( additional_records[ i ] , buffer , cursor );
    }

    return length ;
  }    
}

