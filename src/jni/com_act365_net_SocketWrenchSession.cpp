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

#include "com_act365_net_SocketWrenchSession.h"

#ifdef WIN32
#include <winsock2.h>
#endif

JNIEXPORT jint JNICALL Java_com_act365_net_SocketWrenchSession__1startup (JNIEnv* env , jclass)
{
#ifdef WIN32

    WSADATA wsaData;

    WORD wVersionRequested = MAKEWORD( 2, 0 );

    int err = WSAStartup( wVersionRequested, &wsaData );

    if ( err != 0 ) {
        
        jclass exceptionClass = env -> FindClass("java/net/SocketException");
        
        env -> ThrowNew( exceptionClass , "Cannot find a WinSock DLL" );
        env -> DeleteLocalRef( exceptionClass );
        
        return -1 ;
    }
    
    if ( LOBYTE( wsaData.wVersion ) < 2 ) {

        jclass exceptionClass = env -> FindClass("java/net/SocketException");
        
        env -> ThrowNew( exceptionClass , "Socket Wrench requires at least WinSock 2.0" );
        env -> DeleteLocalRef( exceptionClass );
        
        WSACleanup( );

        return -2 ; 
    }

#endif

    return 0 ;
}

JNIEXPORT jint JNICALL Java_com_act365_net_SocketWrenchSession__1shutdown (JNIEnv* env , jclass)
{
#ifdef WIN32
  WSACleanup();
#endif

  return 0 ;
}

