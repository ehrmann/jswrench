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

/**
 * Reads DNS messages from bytestreams.
 */

public class DNSReader {

  int headerLength ;

  /**
   Constructs a parser to decode DNS bytestreams. 
   @param headerLength length of the transport header, e.g. 8 for UDP or 20 for TCP
  */

  public DNSReader( int headerLength ){
    this.headerLength = headerLength ;
  }

  /**
   Extracts a domain name string from a DNS message, even if compressed.
  */

  int domainName( byte[] buffer , int offset , StringBuffer name ) throws Exception {

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
        domainName( buffer , compressed_offset + headerLength , compressed );
        name.append( compressed.toString() );
        break;
      } else {
        name.append( new String( buffer , offset , inext , "UTF8" ) );
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

  String dataString( byte[] buffer , int offset , int count , short type ) throws Exception {

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

        domainName( buffer , offset , name );

        break;

      default:

    }
        
    return name.toString();
  }

  /**
   Reads all of the information from a DNS response.
  */

  public DNSMessage read( byte[] buffer ) throws Exception {

    DNSMessage message = new DNSMessage();

    message.identification = SocketUtils.shortFromBytes( buffer , headerLength );
    message.flags = SocketUtils.shortFromBytes( buffer , headerLength + 2 );

    short n_questions  = SocketUtils.shortFromBytes( buffer , headerLength + 4 ) ,
          n_answers    = SocketUtils.shortFromBytes( buffer , headerLength + 6 ) ,
          n_authority  = SocketUtils.shortFromBytes( buffer , headerLength + 8 ) ,
          n_additional = SocketUtils.shortFromBytes( buffer , headerLength + 10 );

    message.questions = new Query[ n_questions ];
    message.answers = new ResourceRecord[ n_answers ];
    message.authority_records = new ResourceRecord[ n_authority ];
    message.additional_records = new ResourceRecord[ n_additional ];

    int i = -1 , j , offset = headerLength + 12 ;

    StringBuffer name ;

    String datastring ;

    byte[] namebuffer , databuffer ;

    short type_value , class_value , data_length ;

    int time_to_live ;

    while( ++ i < n_questions ){
      name = new StringBuffer();
      offset += domainName( buffer , offset , name );
      namebuffer = name.toString().getBytes("UTF8");
      type_value = SocketUtils.shortFromBytes( buffer , offset );
      offset += 2 ;
      class_value = SocketUtils.shortFromBytes( buffer , offset ); 
      offset += 2 ;

      message.questions[ i ] = new Query( namebuffer , type_value , class_value , name.toString() );
    }

    i = -1 ;

    while( ++ i < n_answers ){
      name = new StringBuffer();
      offset += domainName( buffer , offset , name );
      namebuffer = name.toString().getBytes("UTF8");
      type_value = SocketUtils.shortFromBytes( buffer , offset );
      offset += 2 ;
      class_value = SocketUtils.shortFromBytes( buffer , offset ); 
      offset += 2 ;
      time_to_live = SocketUtils.intFromBytes( buffer , offset );
      offset += 4 ;
      data_length = SocketUtils.shortFromBytes( buffer , offset );
      offset += 2 ;
      datastring = dataString( buffer , offset , data_length , type_value );
      databuffer = new byte[ data_length ];
      j = -1 ;
      while( ++ j < data_length ){
        databuffer[ j ] = buffer[ offset ++ ];
      }

      message.answers[ i ] = new ResourceRecord( namebuffer , 
                                                 type_value , 
                                                 class_value ,
                                                 time_to_live ,
                                                 databuffer ,
                                                 datastring );
    }

    i = -1 ;

    while( ++ i < n_authority ){
      name = new StringBuffer();
      offset += domainName( buffer , offset , name );
      namebuffer = name.toString().getBytes("UTF8");
      type_value = SocketUtils.shortFromBytes( buffer , offset );
      offset += 2 ;
      class_value = SocketUtils.shortFromBytes( buffer , offset ); 
      offset += 2 ;
      time_to_live = SocketUtils.intFromBytes( buffer , offset );
      offset += 4 ;
      data_length = SocketUtils.shortFromBytes( buffer , offset );
      offset += 2 ;
      datastring = dataString( buffer , offset , data_length , type_value );
      databuffer = new byte[ data_length ];
      j = -1 ;
      while( ++ j < data_length ){
        databuffer[ j ] = buffer[ offset ++ ];
      }

      message.authority_records[ i ] = new ResourceRecord( namebuffer , 
                                                           type_value , 
                                                           class_value ,
                                                           time_to_live ,
                                                           databuffer ,
                                                           datastring );
    }

    i = -1 ;

    while( ++ i < n_additional ){
      name = new StringBuffer();
      offset += domainName( buffer , offset , name );
      namebuffer = name.toString().getBytes("UTF8");
      type_value = SocketUtils.shortFromBytes( buffer , offset );
      offset += 2 ;
      class_value = SocketUtils.shortFromBytes( buffer , offset ); 
      offset += 2 ;
      time_to_live = SocketUtils.intFromBytes( buffer , offset );
      offset += 4 ;
      data_length = SocketUtils.shortFromBytes( buffer , offset );
      offset += 2 ;
      datastring = dataString( buffer , offset , data_length , type_value );
      databuffer = new byte[ data_length ];
      j = -1 ;
      while( ++ j < data_length ){
        databuffer[ j ] = buffer[ offset ++ ];
      }

      message.additional_records[ i ] = new ResourceRecord( namebuffer , 
                                                            type_value , 
                                                            class_value ,
                                                            time_to_live ,
                                                            databuffer ,
                                                            datastring );
    }
      
    return message ;
  }
}


