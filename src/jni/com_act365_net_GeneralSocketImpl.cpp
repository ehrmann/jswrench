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

#include "com_act365_net_GeneralSocketImpl.h"

#include "SocketUtils.h"

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
jint JNICALL Java_com_act365_net_GeneralSocketImpl__1socket( JNIEnv* env , 
                                                             jclass, 
                                                             jint    addressFamily,
                                                             jint    socketType , 
                                                             jint    protocol )
{
    int ret = socket( addressFamily , SocketUtils::socketConstants( socketType ) , protocol );

    if( ret == -1 ){

      jclass exceptionClass = env -> FindClass("java/io/IOException");

      switch( errno ){

        case ENFILE:
          env -> ThrowNew( exceptionClass , "socket(): Not enough kernel memory" );
          break;

        case EMFILE:
          env -> ThrowNew( exceptionClass , "socket(): Process file table overflow" );
          break;

        case EACCES:
          env -> ThrowNew( exceptionClass , "socket(): Permission denied" );
          break;

        case EINVAL:
          env -> ThrowNew( exceptionClass , "socket(): Unknown protocol" );
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

    return ret ;
}

JNIEXPORT
jint JNICALL Java_com_act365_net_GeneralSocketImpl__1bind( JNIEnv*    env , 
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

          jclass exceptionClass = env -> FindClass("java/io/IOException");

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
jint JNICALL Java_com_act365_net_GeneralSocketImpl__1connect( JNIEnv*    env , 
                                                              jclass , 
                                                              jint       socketDescriptor , 
                                                              jbyteArray address , 
                                                              jint       port )
{
    sockaddr_in internetAddress ;

    internetAddress.sin_family = AF_INET ;
    internetAddress.sin_port   = SocketUtils::javaPortToUnixPort( port );

    int ret ;

    if( SocketUtils::jbyteArrayToInAddr( env , address , & internetAddress.sin_addr ) ){
        ret = connect ( socketDescriptor , (sockaddr*) & internetAddress , sizeof( internetAddress ) );
   
        if( ret == -1 ){

          jclass exceptionClass = env -> FindClass("java/io/IOException");

          switch( errno ){

            case EBADF:
              env -> ThrowNew( exceptionClass , "connect(): Bad descriptor" );
              break;

            case EFAULT:
              env -> ThrowNew( exceptionClass , "connect(): Socket outside user space" );
              break;

            case EAGAIN:
              env -> ThrowNew( exceptionClass , "connect(): No more free local ports" );
              break;

            case EACCES:
            case EPERM:
              env -> ThrowNew( exceptionClass , "connect(): Permission denied" );
              break;

#ifndef WIN32
            case ENOTSOCK:
              env -> ThrowNew( exceptionClass , "connect(): Descriptor describes file" );
              break;

            case EISCONN:
              env -> ThrowNew( exceptionClass , "connect(): Socket already connected" );
              break;

            case ECONNREFUSED:
              env -> ThrowNew( exceptionClass , "connect(): Connection refused" );
              break;

            case ETIMEDOUT:
              env -> ThrowNew( exceptionClass , "connect(): Server timed out" );
              break;

            case ENETUNREACH:
              env -> ThrowNew( exceptionClass , "connect(): Network unreachable" );
              break;

            case EADDRINUSE:
              env -> ThrowNew( exceptionClass , "connect(): Address in use" );
              break;

            case EINPROGRESS:
              env -> ThrowNew( exceptionClass , "connect(): Non-blocking socket cannot connect" );
              break;

            case EALREADY:
              env -> ThrowNew( exceptionClass , "connect(): Non-blocking socket already busy" );
              break;

            case EAFNOSUPPORT:
              env -> ThrowNew( exceptionClass , "connect(): Address family not supported" );
              break;
#endif

            default:
                {
                    char errorText[50];
                    sprintf( errorText , "connect(): Unknown I/O error: %d" , errno );
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
jint JNICALL Java_com_act365_net_GeneralSocketImpl__1listen( JNIEnv* env , 
                                                             jclass, 
                                                             jint    socketDescriptor , 
                                                             jint    backlog )
{
    int ret = listen( socketDescriptor , backlog );

    if( ret == -1 ){

      jclass exceptionClass = env -> FindClass("java/io/IOException");

      switch( errno ){

        case EBADF:
          env -> ThrowNew( exceptionClass , "listen(): Invalid descriptor" );
          break;

        case ENOENT:
          env -> ThrowNew( exceptionClass , "listen(): No such file or directory" );
          break;

#ifndef WIN32
        case ENOTSOCK:
          env -> ThrowNew( exceptionClass , "listen(): Descriptor describes file" );
          break;

        case EOPNOTSUPP:
          env -> ThrowNew( exceptionClass , "listen(): Not supported by socket" );
          break;
#endif

        default:
          {
            char errorText[50];
            sprintf( errorText , "listen(): Unknown I/O error: %d" , errno );
            env -> ThrowNew( exceptionClass , errorText );
          }
          break;
      }

      env -> DeleteLocalRef( exceptionClass );
    }

    return ret ;
}

JNIEXPORT 
jint JNICALL Java_com_act365_net_GeneralSocketImpl__1accept( JNIEnv* env , 
                                                             jclass, 
                                                             jint    sd , 
                                                             jobject newSocket )
{
    sockaddr_in clientAddress ;

    socklen_t size = sizeof( clientAddress );

    int clientSD = accept( sd , (sockaddr*) & clientAddress , & size );

    if( clientSD >= 0 ){

      SocketUtils::writeAddressToSocket( env , newSocket , clientSD , clientAddress );

    } else {
      
      jclass exceptionClass = env -> FindClass("java/io/IOException");

      switch( errno ){

        case EAGAIN:
          env -> ThrowNew( exceptionClass , "accept(): No connections present for non-blocking socket");
          break;

        case EBADF:
          env -> ThrowNew( exceptionClass , "accept(): Invalid descriptor" );
          break;

        case EFAULT:
          env -> ThrowNew( exceptionClass , "accept(): Address not in user space" );
          break;

        case EPERM:
          env -> ThrowNew( exceptionClass , "accept(): Permission denied" );
          break;

#ifndef WIN32
        case ENOBUFS:
#endif

        case ENOMEM:
          env -> ThrowNew( exceptionClass , "accept(): Insufficient memory" );
          break;

#ifndef WIN32
        case ENOTSOCK:
          env -> ThrowNew( exceptionClass , "accept(): Descriptor describes file" );
          break;

        case EOPNOTSUPP:
          env -> ThrowNew( exceptionClass , "accept(): Not a stream socket" );
          break;
#endif

        default:
          {
            char errorText[50];
            sprintf( errorText , "accept(): Unknown I/O error: %d" , errno );
            env -> ThrowNew( exceptionClass , errorText );
          }
          break;
      }

      env -> DeleteLocalRef( exceptionClass );
    }

    return clientSD ;
}

JNIEXPORT 
jint JNICALL Java_com_act365_net_GeneralSocketImpl__1close(JNIEnv* env , 
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

      jclass exceptionClass = env -> FindClass("java/io/IOException");
   
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
jint JNICALL Java_com_act365_net_GeneralSocketImpl__1getSocketDescriptor(JNIEnv* env , 
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
void JNICALL Java_com_act365_net_GeneralSocketImpl__1setSocketDescriptor(JNIEnv* env , 
                                                                         jclass, 
                                                                         jobject fileDescriptor , 
                                                                         jint    sd )
{
	if( fileDescriptor ){
        SocketUtils::writeFileDescriptor( env , fileDescriptor , sd );
	}
}

JNIEXPORT
jobject JNICALL Java_com_act365_net_GeneralSocketImpl__1createInetAddress(JNIEnv*    env ,
                                                                          jclass,
                                                                          jint       family ,
                                                                          jbyteArray address )
{
    jclass inetAddressClass = env -> FindClass("java/net/Inet4Address");

    jmethodID mid = env -> GetMethodID( inetAddressClass , "<init>" , "()V" );

    jobject inetAddress = env -> NewObject( inetAddressClass , mid );

    env -> DeleteLocalRef( inetAddressClass );

    in_addr unixAddress ;

    if( SocketUtils::jbyteArrayToInAddr( env , address , & unixAddress ) ){
        SocketUtils::writeAddress( env , 
                                   inetAddress , 
                                   family , 
                                   unixAddress );
    }

    return inetAddress ;
}

JNIEXPORT 
jint JNICALL Java_com_act365_net_GeneralSocketImpl__1setOption(JNIEnv* env , 
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

  return setsockopt( socketDescriptor , level , optid , (char*) value , sizeof( value ) );
}

JNIEXPORT 
jobject JNICALL Java_com_act365_net_GeneralSocketImpl__1getOption(JNIEnv* env , 
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
