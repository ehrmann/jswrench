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

#ifndef INCLUDE_SOCKETUTILS
#define INCLUDE_SOCKETUTILS

#ifdef WIN32
#include <winsock2.h>
#include <ws2tcpip.h>
#else
#define TRUE 1
#define FALSE 0
#include <netdb.h>
#include <netinet/in.h>
#include <sys/socket.h>
#endif

#ifdef LINUX
int getReceiveTimeout(int sd);
void setReceiveTimeout(int sd,int timeout);
int eraseReceiveTimeout(int sd);

void setTimeoutFlag(int);
void resetTimeoutFlag();
int getTimeoutFlag();
#endif

#include <jni.h>

class SocketUtils
{
public:

    static int jbyteArrayToInAddr( JNIEnv* env , jbyteArray javaAddress , in_addr* pAddress );
    static void writeAddressToSocket( JNIEnv* env , jobject& socket , const int socketDescriptor , const sockaddr_in& address );
    static void writeAddress( JNIEnv* env , jobject& inetAddress , jint family , const in_addr& address , const jboolean useDNS = TRUE );
    static void writeFileDescriptor( JNIEnv* env , jobject& socket , const int socketDescriptor );
    static jint unixAddressToJavaAddress( u_long unixAddress );
    static jint unixPortToJavaPort( u_short unixPort );
    static u_short javaPortToUnixPort( jint javaPort );
    static int socketConstants( const int socketid );
    static int socketOptions( const int optid , int& platform_optid , int& platform_level ); 
    static void throwError( JNIEnv* env , const jclass& exceptionclass , const char*  prefix = NULL );
};

#include <ostream>

using namespace std ;

ostream& operator<<( ostream& , const sockaddr_in& address );
ostream& operator<<( ostream& , const sockaddr& address );
ostream& operator<<( ostream& , const in_addr& address );
ostream& operator<<( ostream& , const hostent& host );

#endif

