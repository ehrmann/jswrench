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

#include "com_act365_net_GeneralSocketInputStream.h"

#ifdef WIN32
#include <winsock2.h>
#else
#include <unistd.h>
#include <sys/types.h>
#include <sys/socket.h>
#endif

#include <errno.h>

JNIEXPORT 
jint JNICALL Java_com_act365_net_GeneralSocketInputStream__1read( JNIEnv *   env , 
                                                                  jclass, 
                                                                  jint       socketDescriptor , 
                                                                  jbyteArray buffer , 
                                                                  jint       offset , 
                                                                  jint       count )
{
  jboolean isCopy ;

  jbyte* pBuffer = env -> GetByteArrayElements( buffer , & isCopy );
  char*  pStream = (char*) pBuffer + offset ;

  int ret = 0 , nLeft = count , nRead ;

  while( nLeft > 0 ){
    nRead = recv( socketDescriptor , pStream , nLeft , 0 );
    if( nRead < 0 ){

      jclass exceptionClass = env -> FindClass("java/io/IOException");

      switch( errno ){
  
      case EINTR:
        env -> ThrowNew( exceptionClass , "read(): Interrupted by signal" );
        break;

      case EAGAIN:
        env -> ThrowNew( exceptionClass , "read(): No data available with non-blocking I/O selected" );
        break;

      case EIO:
        env -> ThrowNew( exceptionClass , "read(): Low-level I/O error" );
        break;

      case EISDIR:
        env -> ThrowNew( exceptionClass , "read(): Socket descriptor refers to a directory" );
        break;

      case EBADF:
        env -> ThrowNew( exceptionClass , "read(): Socket descriptor is not open to be read");
        break;

      case EINVAL:
        env -> ThrowNew( exceptionClass , "read(): Socket descriptor attached to unreadable object");
        break;

      case EFAULT:
        env -> ThrowNew( exceptionClass , "read(): Buffer lies outside accessible address space");
        break;

      default:
          {
              char errorText[50];
              sprintf( errorText , "read(): Unknown I/O error: %d" , errno );
              env -> ThrowNew( exceptionClass , errorText );
          }
          break;
      }

      env -> DeleteLocalRef( exceptionClass );

      ret = nRead ;

      break;
    } else if( nRead == 0 ){
      break;
    }

    nLeft   -= nRead ;
    pStream += nRead ;
  }

  if( ret >= 0 ){
    ret = count - nLeft ;
  }

  if( isCopy ){
    env -> ReleaseByteArrayElements( buffer , pBuffer , 0 );
  }

  return ret ;
}

JNIEXPORT
jint JNICALL Java_com_act365_net_GeneralSocketInputStream__1close( JNIEnv * env ,
                                                                   jclass ,
                                                                   jint     socketDescriptor )
{
#ifdef WIN32
  return closesocket( socketDescriptor );
#else 
  return close( socketDescriptor );
#endif
}
  
