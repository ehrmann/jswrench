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

#include "com_act365_net_GeneralDatagramSocketImpl.h"

#include "SocketUtils.h"

#include <assert.h>
#include <errno.h>

#ifdef WIN32
#include <winsock.h>
#else
#include <unistd.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <netdb.h>
#endif
   
#ifndef LINUX
#define socklen_t int
#endif

JNIEXPORT
jint JNICALL Java_com_act365_net_GeneralDatagramSocketImpl__1socket( JNIEnv* env , 
                                                                     jclass, 
                                                                     jint     addressFamily,
                                                                     jint     socketType , 
                                                                     jint     protocol ,
                                                                     jboolean headerincluded )
{
    int type = SocketUtils::socketConstants( socketType );

#ifdef WIN32
    if( type == SOCK_RAW ){

      jclass exceptionClass = env -> FindClass("java/net/SocketException");

      env -> ThrowNew( exceptionClass , "User-defined IP headers not supported on Windows");

      return -1 ;
    }
#endif

    int ret = socket( addressFamily , type , protocol );

    if( ret == -1 ){

      jclass exceptionClass = env -> FindClass("java/net/SocketException");

      switch( errno ){

        case EINVAL:
          env -> ThrowNew( exceptionClass , "socket(): Unknown protocol" );
          break;

        case ENFILE:
          env -> ThrowNew( exceptionClass , "socket(): Not enough kernel memory" );
          break;

        case EMFILE:
          env -> ThrowNew( exceptionClass , "socket(): Process file table overflow" );
          break;

        case EACCES:
          env -> ThrowNew( exceptionClass , "socket(): Permission denied" );
          break;

#ifndef WIN32
        case ENOBUFS:
#endif

        case ENOMEM:
          env -> ThrowNew( exceptionClass , "socket(): Insufficient memory available" );
          break;

#ifndef WIN32
        case EPROTONOSUPPORT:
          env -> ThrowNew( exceptionClass , "socket(): Protocol not supported" );
          break;
#endif

        default:
            {
                char errorText[50];
                sprintf( errorText , "socket(): Unknown I/O error: %d" , errno );
                env -> ThrowNew( exceptionClass , errorText );
            }
            break;
      }

      env -> DeleteLocalRef( exceptionClass );
    }

#ifndef WIN32
    if( type == SOCK_RAW ){
      setsockopt( ret , IPPROTO_IP , IP_HDRINCL , (char*) & headerincluded , sizeof( headerincluded ) );
    }
#endif

    return ret ;
}

JNIEXPORT
jint JNICALL Java_com_act365_net_GeneralDatagramSocketImpl__1bind( JNIEnv*    env , 
                                                                   jclass     , 
                                                                   jint       socketDescriptor , 
                                                                   jbyteArray address , 
                                                                   jint       port )
{
    sockaddr_in internetAddress ;

    internetAddress.sin_family = AF_INET ;
    internetAddress.sin_port   = SocketUtils::javaPortToUnixPort( port );

    int ret ;

    if( SocketUtils::jbyteArrayToInAddr( env , address , & internetAddress.sin_addr ) ){
        ret = bind ( socketDescriptor , (sockaddr*) & internetAddress , sizeof( internetAddress ) );

        if( ret == -1 ){

          jclass exceptionClass = env -> FindClass("java/net/SocketException");

          switch( errno ){

            case EBADF:
              env -> ThrowNew( exceptionClass , "bind(): Invalid descriptor" );
              break;

            case EINVAL:
              env -> ThrowNew( exceptionClass , "bind(): Socket already bound" );
              break;

            case EACCES:
              env -> ThrowNew( exceptionClass , "bind(): Address is protected" );
              break;

            case ENOENT:
              env -> ThrowNew( exceptionClass , "bind(): No such file or directory" );
              break;

#ifndef WIN32
            case ENOTSOCK:
              env -> ThrowNew( exceptionClass , "bind(): Descriptor describes file" );
              break;
#endif

            default:
                {
                    char errorText[50];
                    sprintf( errorText , "bind(): Unknown I/O error: %d" , errno );
                    env -> ThrowNew( exceptionClass , errorText );
                }
                break;
          }

          env -> DeleteLocalRef( exceptionClass );
        } 
     
    } else {
        ret = -1 ;
    }

    return ret ;
}

JNIEXPORT
jint JNICALL Java_com_act365_net_GeneralDatagramSocketImpl__1close(JNIEnv* env , 
                                                                   jclass, 
                                                                   jint    socketDescriptor )
{
    int ret = 0 ;

    if( socketDescriptor >= 0 ){
#ifdef WIN32
      ret = closesocket( socketDescriptor );
#else
      ret = close( socketDescriptor );
#endif
    }

    if( ret == -1 ){

      jclass exceptionClass = env -> FindClass("java/net/SocketException");
   
      switch( errno ){

        case EBADF:
          env -> ThrowNew( exceptionClass , "close(): Invalid descriptor" );
          break;

        default:
            {
                char errorText[50];
                sprintf( errorText , "close(): Unknown I/O error: %d" , errno );
                env -> ThrowNew( exceptionClass , errorText );
            }
            break;
      }

      env -> DeleteLocalRef( exceptionClass );
    }

    return ret ;
}

JNIEXPORT
void JNICALL Java_com_act365_net_GeneralDatagramSocketImpl__1receive( JNIEnv* env ,
                                                                      jclass,
                                                                      jint    socketDescriptor ,
                                                                      jobject dgram ,
                                                                      jint    flags )
{
  jclass datagramPacketClass = env -> GetObjectClass( dgram );

  jfieldID bufID     = env -> GetFieldID( datagramPacketClass , "buf" , "[B" ) ,
           offsetID  = env -> GetFieldID( datagramPacketClass , "offset" , "I" ),
           lengthID  = env -> GetFieldID( datagramPacketClass , "length" , "I" ),
           addressID = env -> GetFieldID( datagramPacketClass , "address" , "Ljava/net/InetAddress;" ),
           portID    = env -> GetFieldID( datagramPacketClass , "port" , "I" );

  env -> DeleteLocalRef( datagramPacketClass );

  jint length = env -> GetIntField( dgram , lengthID ),
       offset = env -> GetIntField( dgram , offsetID );

  jobject inetAddress = env -> GetObjectField( dgram , addressID );

  jbyteArray buf = (jbyteArray) env -> GetObjectField( dgram , bufID );

  jbyte* pBuffer = new jbyte[ length ];

  sockaddr sourceAddress ;

  socklen_t addressLength = sizeof( sourceAddress );

  int nRead = recvfrom( socketDescriptor , (char*) pBuffer , length , flags , & sourceAddress , & addressLength );

  if( nRead < 0 ){

    jclass exceptionClass = env -> FindClass("java/io/IOException");

    switch( errno ){
  
    case EINTR:
      env -> ThrowNew( exceptionClass , "Interrupted by signal" );
      break;

    case EAGAIN:
      env -> ThrowNew( exceptionClass , "No data available with non-blocking I/O selected" );
      break;

    case EIO:
      env -> ThrowNew( exceptionClass , "Low-level I/O error" );
      break;

    case EISDIR:
      env -> ThrowNew( exceptionClass , "Socket descriptor refers to a directory" );
      break;

    case EBADF:
      env -> ThrowNew( exceptionClass , "Socket descriptor is invalid");
      break;

    case EINVAL:
      env -> ThrowNew( exceptionClass , "Socket descriptor attached to unreadable object");
      break;

    case EFAULT:
      env -> ThrowNew( exceptionClass , "Buffer lies outside accessible address space");
      break;

    case ENOENT:
      env -> ThrowNew( exceptionClass , "No such file or directory" );
      break;

#ifndef WIN32

    case ENOTCONN:
      env -> ThrowNew( exceptionClass , "Socket is not connected" );
      break;

    case ENOTSOCK:
      env -> ThrowNew( exceptionClass , "Descriptor does not refer to a socket" );
      break;

    case ECONNRESET:
      env -> ThrowNew( exceptionClass , "Connection reset by peer" );
      break ;

#endif

    default:
      {
        char errorText[50];
        sprintf( errorText , "Unknown I/O error: %d" , errno );
        env -> ThrowNew( exceptionClass , errorText );
      }
      break;
    }

    env -> DeleteLocalRef( exceptionClass );

    delete [] pBuffer ;

    return;
  
  } else if( nRead == 0 ){

    delete [] pBuffer ;

    return;
  }

  env -> SetByteArrayRegion( buf , offset , nRead , pBuffer );

  delete [] pBuffer ;

  assert( addressLength == sizeof( sourceAddress ) );
  assert( sourceAddress.sa_family == AF_INET ); 

  env -> SetIntField( dgram , portID , SocketUtils::unixPortToJavaPort( ((sockaddr_in&) sourceAddress ).sin_port ) );

  SocketUtils::writeAddress( env , 
                             inetAddress , 
                             (jint) AF_INET ,  
                             ((sockaddr_in&) sourceAddress ).sin_addr );

  env -> SetIntField( dgram , lengthID , nRead );

  env -> DeleteLocalRef( inetAddress );
  env -> DeleteLocalRef( buf );
}

JNIEXPORT
void JNICALL Java_com_act365_net_GeneralDatagramSocketImpl__1send( JNIEnv*    env ,
                                                                   jclass ,
                                                                   jint       socketDescriptor ,
                                                                   jbyteArray ipAddress ,
                                                                   jint       port ,
                                                                   jbyteArray buffer ,
                                                                   jint       length )
{
  jboolean isCopy ;

  jbyte* pBuffer = env -> GetByteArrayElements( buffer , & isCopy );
  char*  pStream = (char*) pBuffer ;

  sockaddr_in internetAddress ;

  internetAddress.sin_family = AF_INET ;
  internetAddress.sin_port   = SocketUtils::javaPortToUnixPort( port );

  if( ! SocketUtils::jbyteArrayToInAddr( env , ipAddress , & internetAddress.sin_addr ) ){

    jclass exceptionClass = env -> FindClass("java/io/IOException");
    env -> ThrowNew( exceptionClass , "Cannot interpret IP address");
    env -> DeleteLocalRef( exceptionClass );

    return ;
  }
    
  int nLeft = length , nWritten ;

  while( nLeft > 0 ){
    nWritten = sendto( socketDescriptor ,  pStream , nLeft , 0 , (sockaddr*) & internetAddress , sizeof( internetAddress ) );
    if( nWritten <= 0 ){
      break;
    }

    nLeft   -= nWritten ;
    pStream += nWritten ;
  }

  if( isCopy ){
    env -> ReleaseByteArrayElements( buffer , pBuffer , JNI_ABORT );
  }
}

JNIEXPORT 
jint JNICALL Java_com_act365_net_GeneralDatagramSocketImpl__1getSocketDescriptor(JNIEnv* env , 
                                                                                 jclass, 
                                                                                 jobject fileDescriptor )
{
    if( ! fileDescriptor ){
      return -1 ;
    }

    jclass fileDescriptorClass = env -> GetObjectClass( fileDescriptor );

    jfieldID socketDescriptorID = env -> GetFieldID( fileDescriptorClass , "fd" , "I" );

    env -> DeleteLocalRef( fileDescriptorClass );

    return env -> GetIntField( fileDescriptor , socketDescriptorID );
}

JNIEXPORT 
void JNICALL Java_com_act365_net_GeneralDatagramSocketImpl__1setSocketDescriptor(JNIEnv* env , 
                                                                                 jclass, 
                                                                                 jobject fileDescriptor , 
                                                                                 jint    sd )
{
    if( fileDescriptor ){
      SocketUtils::writeFileDescriptor( env , fileDescriptor , sd );
    }
}

JNIEXPORT 
jint JNICALL Java_com_act365_net_GeneralDatagramSocketImpl__1setOption(JNIEnv* env , 
                                                                       jclass, 
                                                                       jint    socketDescriptor , 
                                                                       jint    name , 
                                                                       jobject newValue )
{
  jclass integerClass = env -> GetObjectClass( newValue );

  jfieldID valueID = env -> GetFieldID( integerClass , "value" , "I" );

  env -> DeleteLocalRef( integerClass );

  int value = env -> GetIntField( newValue  , valueID );

  int optid , level ;

  if( ! SocketUtils::socketOptions( name , optid , level ) ){
    
    jclass exceptionClass = env -> FindClass("java/net/SocketException");

    env -> ThrowNew( exceptionClass , "Socket option not supported" );
    env -> DeleteLocalRef( exceptionClass );

    return -1 ;
  }

  return setsockopt( socketDescriptor , level , optid , (char*) & value , sizeof( value ) );
}

JNIEXPORT 
jobject JNICALL Java_com_act365_net_GeneralDatagramSocketImpl__1getOption(JNIEnv* env , 
                                                                          jclass, 
                                                                          jint socketDescriptor , 
                                                                          jint name )
{
  jobject value = jobject();

  int intValue , 
      length = sizeof( intValue );

  int optid , level ;

  if( ! SocketUtils::socketOptions( name , optid , level ) ){
    
    jclass exceptionClass = env -> FindClass("java/net/SocketException");

    env -> ThrowNew( exceptionClass , "Socket option not supported" );
    env -> DeleteLocalRef( exceptionClass );

    return jobject();
  }

  int ret = getsockopt( socketDescriptor , level , optid , (char*) & intValue , (socklen_t*) & length );

  if( ret < 0 ){

    jclass exceptionClass = env -> FindClass("java/net/SocketException");

    env -> ThrowNew( exceptionClass , "Unable to read option in getsockopt" );
    env -> DeleteLocalRef( exceptionClass );

    return value ;
  }
  
  jclass integerClass = env -> FindClass("java/lang/Integer");

  jmethodID mid = env -> GetMethodID( integerClass , "<init>" , "(I)V" );

  value = env -> NewObject( integerClass , mid , intValue );

  env -> DeleteLocalRef( integerClass );

  return value ;
}
