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
#include <errno.h>

#include <strstream>

#ifdef LINUX

/*
 * Linux doesn't support the SO_RCVTIMEO socket option, so the behaviour has to be
 * mimicked using alarm() and socket(). Since it's illegal to store the receive
 * timeout values within the sockets, they're stored here.
 */

#include <map>

std::map<int,int> receiveTimeouts ;

void setReceiveTimeout(int sd, int timeout){ receiveTimeouts[sd] = timeout ;}

int getReceiveTimeout(int sd)
{
  std::map<int,int>::const_iterator it = receiveTimeouts.find(sd);

  if( it != receiveTimeouts.end() ){
    return (*it).second ;
  } else {
    return -1 ;
  }
}

int eraseReceiveTimeout(int sd){ return receiveTimeouts.erase(sd);}

int timeoutFlag ;

void setTimeoutFlag(int) { timeoutFlag = TRUE ;}
void resetTimeoutFlag() { timeoutFlag = FALSE ;}
int getTimeoutFlag() { return timeoutFlag ;}

#endif

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
                                const in_addr& address ,
                                const jboolean useDNS )
{
    hostent* pHost = NULL ;

    if( useDNS ){
        pHost = gethostbyaddr( (char*) & address  , sizeof( address ) , AF_INET );
    }

    jstring hostname ;

    if( pHost ){

        hostname = env -> NewStringUTF( pHost -> h_name );

    } else {
        
        char iptext[16];

        ostrstream ipstream( iptext , 16 );

        ipstream << address << '\0'; 

        hostname = env -> NewStringUTF( iptext );
    }

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

void SocketUtils::throwError( JNIEnv*       env ,
                              const jclass& exceptionclass ,
                              const char*   prefix )
{
    // Determine the error code.

    int errorcode ;

#ifdef WIN32
    errorcode = WSAGetLastError();
#else
    errorcode = errno ;
#endif

    // Convert the error code into a text message.

    char errortext[100];

    if( prefix ){    
        sprintf( errortext , "%s: error %d: ", prefix , errorcode );
    } else {
        sprintf( errortext , "error %d: ", errorcode );
    }

#ifdef WIN32

    switch( errorcode ){

    case WSAEINTR:
        strcat( errortext , "Interrupted function call (WSAEINTR)" );
        break;

    case WSAEACCES:
        strcat( errortext , "Permission denied (WSAEACCES)" );
        break;

    case WSAEFAULT:
        strcat( errortext , "Bad address (WSAEFAULT)" );
        break;

    case WSAEINVAL:
        strcat( errortext , "Invalid argument (WSAEINVAL)" );
        break;

    case WSAEMFILE:
        strcat( errortext , "Too many open files (WSAEMFILE)" );
        break;

    case WSAEWOULDBLOCK:
        strcat( errortext , "Resource temporarily unavailable (WSAEWOULDBLOCK)" );
        break;

    case WSAEINPROGRESS:
        strcat( errortext , "Operation now in progress (WSAEINPROGRESS)" );
        break;

    case WSAEALREADY:
        strcat( errortext , "Operation already in progress (WSAEALREADY)" );
        break;

    case WSAENOTSOCK:
        strcat( errortext , "Socket operation on nonsocket (WSAENOTSOCK)" );
        break;

    case WSAEDESTADDRREQ:
        strcat( errortext , "Destination address required (WSAEDESTADDRREQ)" );
        break;

    case WSAEMSGSIZE:
        strcat( errortext , "Message too long (WSAEMSGSIZE)" );
        break;

    case WSAEPROTOTYPE:
        strcat( errortext , "Protocol wrong type for socket (WSAEPROTOTYPR)" );
        break;

    case WSAENOPROTOOPT:
        strcat( errortext , "Bad protocol option (WSAENOPROTOOPT)" );
        break;

    case WSAEPROTONOSUPPORT:
        strcat( errortext , "Protocol not supported (WSAEPROTONOSUPPORT)" );
        break;

    case WSAESOCKTNOSUPPORT:
        strcat( errortext , "Socket type not supported (WSASOCKTNOSUPPORT)" );
        break;

    case WSAEOPNOTSUPP:
        strcat( errortext , "Operation not supported (WSAEOPNOTSUPP)" );
        break;

    case WSAEPFNOSUPPORT:
        strcat( errortext , "Protocol family not supported (WSAEPFNOSUPPORT)" );
        break;

    case WSAEAFNOSUPPORT:
        strcat( errortext , "Address family not supported by protocol family (WSAEAFNOSUPPORT)" );
        break;

    case WSAEADDRINUSE:
        strcat( errortext , "Address already in use (WSAEADDRINUSE)" );
        break;

    case WSAEADDRNOTAVAIL:
        strcat( errortext , "Cannot assign requested address (WSAEADDRNOTAVAIL)" );
        break;

    case WSAENETDOWN:
        strcat( errortext , "Network is down (WSAENETDOWN)" );
        break;

    case WSAENETUNREACH:
        strcat( errortext , "Network is unreachable (WSAENETUNREACH)" );
        break;

    case WSAENETRESET:
        strcat( errortext , "Network dropped connection on reset (WSAENETRESET)" );
        break;

    case WSAECONNABORTED:
        strcat( errortext , "Software caused connection abort (WSAECONNABORTED)" );
        break;

    case WSAECONNRESET:
        strcat( errortext , "Connection reset by peer (WSAECONNRESET)" );
        break;

    case WSAENOBUFS:
        strcat( errortext , "No buffer space available (WSAENOBUFS)" );
        break;

    case WSAEISCONN:
        strcat( errortext , "Socket is already connected (WSAEISCONN)" );
        break;

    case WSAENOTCONN:
        strcat( errortext , "Socket is not connected (WSAENOTCONN)" );
        break;

    case WSAESHUTDOWN:
        strcat( errortext , "Cannot send after socket shutdown (WSAESHUTDOWN)" );
        break;

    case WSAETIMEDOUT:
        strcat( errortext , "Connection timed out (WSAETIMEDOUT)" );
        break;

    case WSAECONNREFUSED:
        strcat( errortext , "Connection refused (WSAECONNREFUSED)" );
        break;

    case WSAEHOSTDOWN:
        strcat( errortext , "Host is down (WSAEHOSTDOWN)" );
        break;

    case WSAEHOSTUNREACH:
        strcat( errortext , "No route to host (WSAEHOSTUNREACH)" );
        break;

    case WSAEPROCLIM:
        strcat( errortext , "Too many processes (WSAEPROCLIM)" );
        break;

    case WSASYSNOTREADY:
        strcat( errortext , "Network subsystem is unavailable (WSASYSNOTREADY)" );
        break;

    case WSAVERNOTSUPPORTED:
        strcat( errortext , "winsock.dll version out of range (WSAVERNOTSUPPORTED)" );
        break;

    case WSANOTINITIALISED:
        strcat( errortext , "Successful WSAStartup not yet performed (WSANOTINITIALISED)" );
        break;

    case WSAEDISCON:
        strcat( errortext , "Graceful shutdown in progress (WSAEDISCON)" );
        break;

    case WSATYPE_NOT_FOUND:
        strcat( errortext , "Class type not found (WSATYPE_NOT_FOUND)" );
        break;

    case WSAHOST_NOT_FOUND:
        strcat( errortext , "Host not found (WSAHOST_NOT_FOUND)" );
        break;

    case WSATRY_AGAIN:
        strcat( errortext , "Nonauthorative host not found (WSATRY_AGAIN)" );
        break;

    case WSANO_RECOVERY:
        strcat( errortext , "This is a nonrecoverable error (WSANO_RECOVERY)" );
        break;

    case WSANO_DATA:
        strcat( errortext , "Valid name, no data record of requested type (WSANO_DATA)" );
        break;

    case WSA_INVALID_HANDLE:
        strcat( errortext , "Specified event object handle is invalid (WSA_INVALID_HANDLE)" );
        break;

    case WSA_INVALID_PARAMETER:
        strcat( errortext , "One or more parameters is invalid (WSA_INVALID_PARAMETER)" );
        break;

    case WSA_IO_INCOMPLETE:
        strcat( errortext , "Overlapped I/O event object not in signalled state (WSA_IO_INCOMPLETE)" );
        break;

    case WSA_IO_PENDING:
        strcat( errortext , "Overlapped operations will complete later (WSA_IO_PENDING)" );
        break;

    case WSA_NOT_ENOUGH_MEMORY:
        strcat( errortext , "Insufficient memory available (WSA_NOT_ENOUGH_MEMORY)" );
        break;

    case WSA_OPERATION_ABORTED:
        strcat( errortext , "Overlapped operation aborted (WSA_OPERATION_ABORTED)" );
        break;
/*
    case WSAINVALIDPROCTABLE:
        strcat( errortext , "Invalid procedure table from service provider (WSAINVALIDPROCTABLE)" );
        break;

    case WSAINVALIDPROVIDER:
        strcat( errortext , "Invalid service provider version number (WSAINVALIDPROVIDER)" );
        break;

    case WSAPROVIDERFAILEDINIT:
        strcat( errortext , "Unable to initialize a service provider (WSAPROVIDERFAILEDINIT)" );
        break;
*/
    case WSASYSCALLFAILURE:
        strcat( errortext , "System call failure (WSASYSCALLFAILURE)" );
        break;

    default:
        strcat( errortext , "Unknown" );
        break;
    }

#else

    strcat( errortext , strerror( errorcode ) );

#endif

    env -> ThrowNew( exceptionclass , errortext );
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

