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

#include "com_act365_net_GeneralSocketOutputStream.h"

#ifdef WIN32
#include <winsock2.h>
#else
#include <unistd.h>
#include <sys/types.h>
#include <sys/socket.h>
#endif

JNIEXPORT 
jint JNICALL Java_com_act365_net_GeneralSocketOutputStream__1send(JNIEnv *   env , 
                                                                  jclass, 
                                                                  jint       socketDescriptor , 
                                                                  jbyteArray buffer , 
                                                                  jint       offset , 
                                                                  jint       count )
{
  jboolean isCopy ;

  jbyte*  pBuffer = env -> GetByteArrayElements( buffer , & isCopy );
  char*   pStream = (char*) pBuffer + offset ;

  int ret = 1 , nLeft = count , nWritten ;

  while( nLeft > 0 ){
    nWritten = send( socketDescriptor ,  pStream , nLeft , 0 );
    if( nWritten <= 0 ){
      ret = nWritten ;
      break;
    }

    nLeft   -= nWritten ;
    pStream += nWritten ;
  }

  if( ret > 0 ){
    ret = count - nLeft ;
  }

  if( isCopy ){
    env -> ReleaseByteArrayElements( buffer , pBuffer , JNI_ABORT );
  }
  
  return ret ;
}

JNIEXPORT
jint JNICALL Java_com_act365_net_GeneralSocketOutputStream__1close(JNIEnv *  env ,
                                                                   jclass ,
                                                                   jint      socketDescriptor )
{
#ifdef WIN32
  return closesocket( socketDescriptor );
#else 
  return close( socketDescriptor );
#endif
}

