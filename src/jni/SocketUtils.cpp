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

#include "SocketUtils.h"

#ifndef WIN32
#include <netinet/tcp.h>
#endif

#ifdef SOLARIS
#include <sys/socket.h>
#endif

#include <assert.h>

int SocketUtils::jbyteArrayToInAddr( JNIEnv*    env ,
                                     jbyteArray javaAddress ,
                                     in_addr*   pAddress )
{
    if( env -> GetArrayLength( javaAddress ) != 4 ){
            return FALSE ;
    }

    jbyte addressData[ 4 ];

    env -> GetByteArrayRegion( javaAddress , 0 , 4 , addressData );

#ifdef WIN32
    pAddress -> s_net   = (unsigned char) addressData[0];
    pAddress -> s_host  = (unsigned char) addressData[1];
    pAddress -> s_lh    = (unsigned char) addressData[2];
    pAddress -> s_impno = (unsigned char) addressData[3];
#elif SOLARIS
    pAddress -> s_addr = (unsigned char) addressData[0];
    pAddress -> s_addr <<= 8 ;
    pAddress -> s_addr += (unsigned char) addressData[1];
    pAddress -> s_addr <<= 8 ;
    pAddress -> s_addr += (unsigned char) addressData[2];
    pAddress -> s_addr <<= 8 ;
    pAddress -> s_addr += (unsigned char) addressData[3];
#else
    pAddress -> s_addr = (unsigned char) addressData[3];
    pAddress -> s_addr <<= 8 ;
    pAddress -> s_addr += (unsigned char) addressData[2];
    pAddress -> s_addr <<= 8 ;
    pAddress -> s_addr += (unsigned char) addressData[1];
    pAddress -> s_addr <<= 8 ;
    pAddress -> s_addr += (unsigned char) addressData[0];
#endif

    return TRUE ;
}

jint SocketUtils::unixAddressToJavaAddress( u_long unixAddress )
{
    jint javaAddress = unixAddress % 256 ;

    javaAddress <<= 8 ;
    unixAddress >>= 8 ; 
    javaAddress += unixAddress % 256 ;
    javaAddress <<= 8 ;
    unixAddress >>= 8 ; 
    javaAddress += unixAddress % 256 ;
    javaAddress <<= 8 ;
    unixAddress >>= 8 ; 
    javaAddress += unixAddress % 256 ;

    return javaAddress ;
}

jint SocketUtils::unixPortToJavaPort( u_short unixPort )
{
    u_short javaPort = ( unixPort >> 8 ) | ( unixPort << 8 );

    return javaPort ;
}

u_short SocketUtils::javaPortToUnixPort( jint javaPort )
{
    u_short jport = (u_short) javaPort ;

    return ( jport << 8 ) | ( jport >> 8 );
}

void SocketUtils::writeAddressToSocket( JNIEnv*            env ,
                                        jobject&           socket ,
                                        const int          socketDescriptor ,
                                        const sockaddr_in& clientAddress )
{
    jclass socketImplClass = env -> GetObjectClass( socket );

    jfieldID portID    = env -> GetFieldID( socketImplClass , "port" , "I" ) ,
             addressID = env -> GetFieldID( socketImplClass , "address" , "Ljava/net/InetAddress;" ),
             fdID      = env -> GetFieldID( socketImplClass , "fd" , "Ljava/io/FileDescriptor;" );

    env -> DeleteLocalRef( socketImplClass );

    env -> SetIntField( socket , portID , unixPortToJavaPort( clientAddress.sin_port ) );

    jobject inetAddress    = env -> GetObjectField( socket , addressID ),
            fileDescriptor = env -> GetObjectField( socket , fdID );

    writeAddress( env , 
                  inetAddress , 
                  clientAddress.sin_family , 
                  clientAddress.sin_addr );

    writeFileDescriptor( env , 
                         fileDescriptor ,
                         socketDescriptor );
 
    env -> DeleteLocalRef( inetAddress );
    env -> DeleteLocalRef( fileDescriptor );
}

void SocketUtils::writeAddress( JNIEnv*        env ,
                                jobject&       inetAddress ,
                                jint           family ,
                                const in_addr& address )
{
    hostent* pHost = gethostbyaddr( (char*) & address  , sizeof( address ) , AF_INET );

    jstring hostname = pHost ? env -> NewStringUTF( pHost -> h_name ) : jstring();

    jclass inetAddressClass = env -> GetObjectClass( inetAddress );

    jfieldID hostNameID    = env -> GetFieldID( inetAddressClass , "hostName" , "Ljava/lang/String;" ) ,
             inetAddressID = env -> GetFieldID( inetAddressClass , "address" , "I" ),
             familyID      = env -> GetFieldID( inetAddressClass , "family" , "I" );

    env -> DeleteLocalRef( inetAddressClass );

    env -> SetObjectField( inetAddress , hostNameID , hostname );
    env -> SetIntField( inetAddress , inetAddressID , unixAddressToJavaAddress( address.s_addr ) );
    env -> SetIntField( inetAddress , familyID , family );

    env -> DeleteLocalRef( hostname );
}

void SocketUtils::writeFileDescriptor( JNIEnv*   env ,
                                       jobject&  fileDescriptor ,
                                       const int socketDescriptor )
{
    jclass fileDescriptorClass = env -> GetObjectClass( fileDescriptor );

    jfieldID socketDescriptorID = env -> GetFieldID( fileDescriptorClass , "fd" , "I" );

    env -> DeleteLocalRef( fileDescriptorClass );

    env -> SetIntField( fileDescriptor , socketDescriptorID , socketDescriptor );
}

int SocketUtils::socketConstants( const int socketid )
{
    switch( socketid ){

    case 1 :

      return SOCK_STREAM ;

    case 2 :

      return SOCK_DGRAM ;

    default:
 
      return socketid ;
    }
}

int SocketUtils::socketOptions( const int optid , int& platform_optid , int& platform_level )
{
    switch( optid ){

    case 1 :

      platform_optid = TCP_NODELAY ;
      platform_level = IPPROTO_TCP ;

      break;

    case 4 :

      platform_optid = SO_REUSEADDR ;
      platform_level = SOL_SOCKET ;

      break;

    case 15 :

#ifdef LINUX
      return FALSE ;
#else
      platform_optid = SO_DEBUG | SO_ACCEPTCONN | SO_REUSEADDR | SO_KEEPALIVE ;
      platform_level = SOL_SOCKET ;
#endif

      break;

    case 16 :

      platform_optid = IP_MULTICAST_IF ;
      platform_level = IPPROTO_IP ;

      break ;

    case 128 :

      platform_optid = SO_LINGER ;
      platform_level = SOL_SOCKET ;

      break;

    case 4097 :

      platform_optid = SO_SNDBUF ;
      platform_level = SOL_SOCKET ;

      break;

    case 4098 :

      platform_optid = SO_RCVBUF ;
      platform_level = SOL_SOCKET ;

      break;

    case 4102 :

#ifdef LINUX
      return FALSE ;
#else
      platform_optid = SO_RCVTIMEO ;
      platform_level = SOL_SOCKET ;
#endif

      break;

    default :

      return FALSE ;
    }

    return TRUE ;
}

int SocketUtils::errorCode()
{
#ifdef WIN32
    return WSAGetLastError();
#else
    return errno ;
#endif
}

ostream& operator<<( ostream& str , const sockaddr_in& address )
{
    assert( address.sin_family == AF_INET );

    str << address.sin_addr << ':' << address.sin_port ;

    return str ;
}

ostream& operator<<( ostream& str , const sockaddr& address )
{
    switch( address.sa_family ){

    case AF_INET:
      str << (const sockaddr_in&) address ; 
      break;

#ifdef LINUX

    case AF_LOCAL:
      str << "LOCAL: " << address.sa_data ;
      break;

#endif

    default:
      str << address.sa_family << ": " << address.sa_data ;
      break;
    }

    return str ;
}
 
ostream& operator<<( ostream& str , const in_addr& address )
{
    u_long addr = address.s_addr ;

    str << addr % 256 ;
    addr >>= 8 ;
    str << '.'; 
    str << addr % 256 ;
    addr >>= 8 ;
    str << '.'; 
    str << addr % 256 ;
    addr >>= 8 ;
    str << '.'; 
    str << addr % 256 ;
    addr >>= 8 ;

    return str ;
}

ostream& operator<<( ostream& str , const hostent& host )
{
    assert( host.h_addrtype == AF_INET );
    assert( host.h_length == 4 );

    str << "Name: " << host.h_name ;

    char ** pAlias = host.h_aliases ;

    if( * host.h_aliases ){
        str << " (";
    }

    while( * pAlias ){
        if( pAlias != host.h_aliases ){
            str << ',';
        }
        str << * pAlias ++ ;
    }

    if( * host.h_aliases ){
        str << ')';
    }

    str << endl ;

    char ** pAddress = host.h_addr_list ;

    while( * pAddress ){
        str << * (in_addr*) * pAddress ++ << endl ;
    }

    return str ;
}

