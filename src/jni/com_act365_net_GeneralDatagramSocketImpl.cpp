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

#ifdef WIN32
#include <winsock2.h>
#include <ws2tcpip.h>
#else
#include <unistd.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <netdb.h>
#endif
   
#ifdef LINUX
#include <signal.h>
#else
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

    int ret = socket( addressFamily , type , protocol );

    if( ret == -1 ){

      jclass exceptionClass = env -> FindClass("java/net/SocketException");

      SocketUtils::throwError( env , exceptionClass , "socket()" );

      env -> DeleteLocalRef( exceptionClass );
    }

    if(  type == SOCK_RAW && headerincluded ){

        int one = 1 ; // Necessary for Windows

        if( setsockopt( ret , IPPROTO_IP , IP_HDRINCL , (char*) & one , sizeof( one ) ) ){

            jclass exceptionClass = env -> FindClass("java/net/SocketException");
             
            SocketUtils::throwError( env , exceptionClass , "setsockopt()" );

            env -> DeleteLocalRef( exceptionClass );
        }
    }

#ifdef LINUX
    setReceiveTimeout( ret , 0 );
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

          SocketUtils::throwError( env , exceptionClass , "bind()" );

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

      SocketUtils::throwError( env , exceptionClass , "close()" );

      env -> DeleteLocalRef( exceptionClass );
    }

#ifdef LINUX
    int eraseCount = eraseReceiveTimeout( socketDescriptor );
    assert( eraseCount == 1 );
#endif

    return ret ;
}

JNIEXPORT
void JNICALL Java_com_act365_net_GeneralDatagramSocketImpl__1receive( JNIEnv*  env ,
                                                                      jclass,
                                                                      jint     socketDescriptor ,
                                                                      jobject  dgram ,
                                                                      jint     flags ,
                                                                      jboolean useDNS )
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

#ifdef LINUX
  siginterrupt(SIGALRM,1);
  int receiveTimeout = getReceiveTimeout( socketDescriptor );
  if( receiveTimeout > 0 ){
    resetTimeoutFlag();
    signal( SIGALRM , setTimeoutFlag );
    alarm( receiveTimeout );
  }
#endif

  int nRead = recvfrom( socketDescriptor , (char*) pBuffer , length , flags , & sourceAddress , & addressLength );

  if( nRead < 0 ){

    int isTimeout ;

#ifdef WIN32
    isTimeout = WSAGetLastError() == WSAETIMEDOUT ;
#else
    isTimeout = errno == EINTR ;
#endif

    if( isTimeout ) {

      jclass interruptedIOClass = env -> FindClass("java/io/InterruptedIOException");

      SocketUtils::throwError( env , interruptedIOClass , "recvfrom()" );

      env -> DeleteLocalRef( interruptedIOClass );

    } else {

      jclass exceptionClass = env -> FindClass("java/io/IOException");

      SocketUtils::throwError( env , exceptionClass , "recvfrom()" );

      env -> DeleteLocalRef( exceptionClass );
    }

    delete [] pBuffer ;

    return;  
  } 

#ifdef LINUX
  alarm( 0 );
  resetTimeoutFlag();
#endif
  
  if( nRead == 0 ){

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
                             ((sockaddr_in&) sourceAddress ).sin_addr ,
                             useDNS );

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

#ifdef LINUX

  if( name == 4102 ){

    // SO_RCVTIMEO
    
    setReceiveTimeout( socketDescriptor , (int)( value / 1000. + 0.5 ) );

    return 0 ;
  }
#endif

  int optid , level ;

  if( ! SocketUtils::socketOptions( name , optid , level ) ){
    
    jclass exceptionClass = env -> FindClass("java/net/SocketException");

    env -> ThrowNew( exceptionClass , "Socket option not supported" );
    env -> DeleteLocalRef( exceptionClass );

    return -1 ;
  }

  int ret ;

  if( ret = setsockopt( socketDescriptor , level , optid , (char*) & value , sizeof( value ) ) ){

    jclass exceptionClass = env -> FindClass("java/net/SocketException");

    SocketUtils::throwError( env , exceptionClass , "setsockopt()" );

    env -> DeleteLocalRef( exceptionClass );
  }

  return ret ;
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

  int optid , level , ret ;

#ifdef LINUX
  if( name == 4102 ){

    // SO_RCVTIMEO

    intValue = getReceiveTimeout( socketDescriptor );

    assert( intValue != -1 );

  } else {
#endif

  if( ! SocketUtils::socketOptions( name , optid , level ) ){
    
    jclass exceptionClass = env -> FindClass("java/net/SocketException");

    env -> ThrowNew( exceptionClass , "Socket option not supported" );
    env -> DeleteLocalRef( exceptionClass );

    return jobject();
  }

  ret = getsockopt( socketDescriptor , level , optid , (char*) & intValue , (socklen_t*) & length );

  if( ret < 0 ){

    jclass exceptionClass = env -> FindClass("java/net/SocketException");

    SocketUtils::throwError( env , exceptionClass , "getsockopt()" );

    env -> DeleteLocalRef( exceptionClass );

    return value ;
  }
  
#ifdef LINUX
  }
#endif

  jclass integerClass = env -> FindClass("java/lang/Integer");

  jmethodID mid = env -> GetMethodID( integerClass , "<init>" , "(I)V" );

  value = env -> NewObject( integerClass , mid , intValue );

  env -> DeleteLocalRef( integerClass );

  return value ;
}
