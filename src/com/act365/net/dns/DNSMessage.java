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
 * @see www.faqs.org/rfcs/rfc1035.html
 */

public class DNSMessage implements IServiceMessage {

  // Type and Query Type values

    public final static int A        = 1 ,    // Host address
                            NS       = 2 ,    // Authoritative name server
                            MD       = 3 ,    // Mail destination (obsolete)
                            MF       = 4 ,    // Mail forwarder (obsolete)
                            CNAME    = 5 ,    // Canonical name for alias
                            SOA      = 6 ,    // Start of zone of authority
                            MB       = 7 ,    // Mailbox name (experimental)
                            MG       = 8 ,    // Mailgroup member (experimental)
                            MR       = 9 ,    // Mail rename domain name (experimental)
                            NULL     = 10 ,   // Null RR (experimental)
                            WKS      = 11 ,   // Well-known service descriptor
                            PTR      = 12 ,   // Domain name pointer
                            HINFO    = 13 ,   // Host information
                            MINFO    = 14 ,   // Mail box or mail list information
                            MX       = 15 ,   // Mail exchange
                            TXT      = 16 ,   // Text strings
                            RP       = 17 ,   // Responsible person
                            AFSDB    = 18 ,   // AFS database location
                            X25      = 19 ,   // X.25 PSDN address
                            ISDN     = 20 ,   // ISDN address
                            RT       = 21 ,   // Route through
                            NSAP     = 22 ,   // NSAP-style A address
                            NSAP_PTR = 23 ,   // NSAP-style PTR address
                            SIG      = 24 ,   // Security signature
                            KEY      = 25 ,   // Security key
                            PX       = 26 ,   // X.400 mail-mapping information
                            GPOS     = 27 ,   // Geographical position
                            AAAA     = 28 ,   // IP6 address
                            LOC      = 29 ,   // Location information
                            NXT      = 30 ,   // Next domain (obsolete)
                            EID      = 31 ,   // Endpoint identifier
                            NIMLOC   = 32 ,   // Nimrod locator
                            SRV      = 33 ,   // Server selection
                            ATMA     = 34 ,   // ATM address
                            NAPTR    = 35 ,   // Naming authority pointer
                            KX       = 36 ,   // Key exchanger
                            CERT     = 37 ,
                            A6       = 38 ,
                            DNAME    = 39 ,
                            SINK     = 40 ,
                            OPT      = 41 ,
                            APL      = 42 ,
                            DS       = 43 ,   // Delegation signer
                            SSHFP    = 44 ,   // SSH key fingerprint
                            RRSIG    = 46 ,   
                            NSEC     = 47 ,
                            DNSKEY   = 48 ,
                            UINFO    = 100 ,
                            UID      = 101 ,
                            GID      = 102 ,
                            UNSPEC   = 103 ,
                            TKEY     = 249 ,  // Transaction key
                            TSIG     = 250 ,  // Transaction signature
                            IXFR     = 251 ,  // Incremental transfer
                            AXFR     = 252 ,  // Transfer of an entire zone
                            MAILB    = 253 ,  // Mailbox-related RRs
                            MAILA    = 254 ,  // Mail agent RRs
                            ANY      = 255 ;  // Request for all records

    public final static String dnsTypes[] = { "" ,
                                              "A",
                                              "NS",
                                              "MD",
                                              "MF",
                                              "CNAME",
                                              "SOA",
                                              "MB",
                                              "MG",
                                              "MR",
                                              "NULL",
                                              "WKS",
                                              "PTR",
                                              "HINFO",
                                              "MINFO",
                                              "MX",
                                              "TXT",
                                              "RP",
                                              "AFSDB",
                                              "X25",
                                              "ISDN",
                                              "RT",
                                              "NSAP",
                                              "NSAP_PTR",
                                              "SIG",
                                              "KEY",
                                              "PX",
                                              "GPOS",
                                              "AAAA",
                                              "LOC",
                                              "NXT",
                                              "EID",
                                              "NIMLOC",
                                              "SRV",
                                              "ATMA",
                                              "NAPTR",
                                              "KX",
                                              "CERT",
                                              "A6",
                                              "DNAME",
                                              "SINK",
                                              "OPT",
                                              "APL",
                                              "DS",
                                              "SSHFP",
                                              "RRSIG",
                                              "NSEC",
                                              "DNSKEY"
                                            };

    // Opcode values
    
    public final static int STANDARD_QUERY = 0 ,
                            INVERSE_QUERY = 1 , // Retired 
                            SERVER_STATUS_REQUEST = 2 ;

    public final static String dnsOpcodes[] = { "Standard query",
                                                "Inverse query",
                                                "Server status query" };
                                                
    // Error codes
    
    public final static int NOERROR = 0 ,
                            FORMERROR = 1 ,
                            SERVFAIL = 2 ,
                            NXDOMAIN = 3 ,
                            NOTIMP = 4 ,
                            REFUSED = 5 ,
                            YXDOMAIN = 6 ,
                            YXRRSET = 7 ,
                            NXRRSET = 8 ,
                            NOTAUTH = 9 ,
                            NOTZONE = 10 ;
            
    public final static String errorCodes[] = { "No error" ,
                                                "Format error" ,
                                                "Server failure" ,
                                                "Non-existent domain" ,
                                                "Not implemented" ,
                                                "Query refused" ,
                                                "Name exists when it should not" ,
                                                "RR Set exists when it should not" ,
                                                "RR Set that should exist does not" ,
                                                "Server not authoratative for zone" ,
                                                "Name not contained in zone" };                        
    
    // Various other constants
                            
    public final static int QUERY = 0 ,
                            RESPONSE = 1 ,
                            AUTHORITATIVE_ANSWER = 1 ,
                            TRUNCATED = 1 ,
                            RECURSION_DESIRED = 1 ,
                            ITERATIVE_QUERY = 0 ,
                            RECURSIVE_QUERY = 1 ,
                            RECURSION_AVAILABLE = 1 ;

  // Members
   
  public short identification ;
  public short flags ;
  public int dnstype ;
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
                     int dnstype ,
                     boolean recursion_desired ,
                     String domain ) throws IOException {

    short flags = 0 ;

    flags &= (byte)( QUERY << 15 );
    flags &= (byte)( STANDARD_QUERY << 11 );

    if( recursion_desired ){
      flags &= (byte)( RECURSION_DESIRED << 8 );
    }

    Query[] questions = new Query[ 1 ];

    questions[ 0 ] = new Query( dnstype , domain );

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
  
    StringBuffer sb = new StringBuffer();
    
    if( ( flags & 0x8000 ) == 1 ){
        sb.append("Query");
    } else {
        sb.append("Response");
    }
    
    int opcode = flags & 0x7800 ;
    
    if( opcode < dnsOpcodes.length ){
        sb.append(':');
        sb.append( dnsOpcodes[opcode] );
    }
    
    if( ( flags & 0x0400 ) == 1 ){
        sb.append(" Authoratative answer");
    }
    
    if( ( flags & 0x0200 ) == 1 ){
        sb.append(" Truncated");
    }
    
    if( ( flags & 0x0100 ) == 1 ){
        sb.append(" Recursion desired");
    }
    
    if( ( flags & 0x0080 ) == 1 ){
        sb.append(" Recursion available");
    }
    
    int rcode = flags & 0x000f ;
    
    if( rcode > 0 ){
        sb.append(':');
        sb.append( errorCodes[ rcode ] );
    }
    
    printer.println( sb.toString() );
    
    int i = -1 ;

    while( ++ i < questions.length ){
	  printer.println( "QUERY " + (int)( i + 1 ) );
	  printer.println( questions[ i ].toString() );
    }

    i = -1 ;

    while( ++ i < answers.length ){
	  printer.println( "ANSWER " + (int)( i + 1 ) );
	  printer.println( new String( answers[ i ].domain_name ) + ':' + answers[ i ].toString() );
    }

    i = -1 ;

    while( ++ i < authority_records.length ){
	  printer.println( "AUTHORITY RECORD " + (int)( i + 1 ) );
	  printer.println( new String( authority_records[ i ].domain_name ) + ':' + authority_records[ i ].toString() );
    }

    i = -1 ;

    while( ++ i < additional_records.length ){
	  printer.println( "ADDITIONAL RECORD " + (int)( i + 1 ) );
	  printer.println( new String( additional_records[ i ].domain_name ) + ':' + additional_records[ i ].toString() );
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
   * Extracts a character string from a DNS message.
   */
  
  static int characterString( byte[] buffer , int offset , StringBuffer name ){
      byte next = buffer[ offset ++ ];
      int inext = next >= 0 ? next : 0xffffff00 ^ next ;
      name.append( new String( buffer , offset , inext ) );    
      return inext + 1 ;
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

      case DNSMessage.TXT:
      
        while( offset < start + count ){
            offset += characterString( buffer , offset , name );
            if( offset < start + count - 1 ){
                name.append(':');
            }
        }
        
        break;
        
      case DNSMessage.NS :
      case DNSMessage.CNAME:  
      case DNSMessage.PTR :
      case DNSMessage.MB:
      case DNSMessage.MD: 
      case DNSMessage.MF:
      case DNSMessage.MG:
      case DNSMessage.MR:
      
        offset += domainName( initialOffset , buffer , offset , name );
 
        break;

      case DNSMessage.SOA :
      
        name.append("source-");
        offset += domainName( initialOffset , buffer , offset , name );
        name.append(" email-");
        offset += domainName( initialOffset , buffer , offset , name );
        name.append(" serial-");
        name.append( Integer.toString( SocketUtils.intFromBytes( buffer , offset ) ) );
        offset += 4 ;
        name.append(" refresh-");
        name.append( Integer.toString( SocketUtils.intFromBytes( buffer , offset ) ) );
        offset += 4 ;
        name.append(" retry-");
        name.append( Integer.toString( SocketUtils.intFromBytes( buffer , offset ) ) );
        offset += 4 ;
        name.append(" expire-");
        name.append( Integer.toString( SocketUtils.intFromBytes( buffer , offset ) ) );
        offset += 4 ;
        name.append(" minimum-");
        name.append( Integer.toString( SocketUtils.intFromBytes( buffer , offset ) ) );
        offset += 4 ;

        break;
      
      case DNSMessage.MX:
      
        name.append("preference-");
        name.append( Short.toString( SocketUtils.shortFromBytes( buffer , offset ) ) );
        offset += 2 ;
        name.append(" exchange-");
        offset += domainName( initialOffset , buffer , offset , name );
        
        break;
      
      case DNSMessage.MINFO:
      
        name.append("email-");
        offset += domainName( initialOffset , buffer , offset , name );
        name.append(" error email-");
        offset += domainName( initialOffset , buffer , offset , name );
        
        break;
        
      case DNSMessage.HINFO:
      
        name.append("cpu-");
        offset += characterString( buffer , offset , name );
        name.append(" os-");
        offset += characterString( buffer , offset , name );
        
        break;
        
      case DNSMessage.NULL:
      
        name.append( new String( buffer , offset , count ) );
        
        break;
              
      default:
      
        name.append(" unsupported");
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

