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

/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class com_act365_net_GeneralDatagramSocketImpl */

#ifndef _Included_com_act365_net_GeneralDatagramSocketImpl
#define _Included_com_act365_net_GeneralDatagramSocketImpl
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     com_act365_net_GeneralDatagramSocketImpl
 * Method:    _socket
 * Signature: (IIIZ)I
 */
JNIEXPORT jint JNICALL Java_com_act365_net_GeneralDatagramSocketImpl__1socket
  (JNIEnv *, jclass, jint, jint, jint, jboolean);

/*
 * Class:     com_act365_net_GeneralDatagramSocketImpl
 * Method:    _bind
 * Signature: (I[BI)I
 */
JNIEXPORT jint JNICALL Java_com_act365_net_GeneralDatagramSocketImpl__1bind
  (JNIEnv *, jclass, jint, jbyteArray, jint);

/*
 * Class:     com_act365_net_GeneralDatagramSocketImpl
 * Method:    _close
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_com_act365_net_GeneralDatagramSocketImpl__1close
  (JNIEnv *, jclass, jint);

/*
 * Class:     com_act365_net_GeneralDatagramSocketImpl
 * Method:    _receive
 * Signature: (ILjava/net/DatagramPacket;IZ)V
 */
JNIEXPORT void JNICALL Java_com_act365_net_GeneralDatagramSocketImpl__1receive
  (JNIEnv *, jclass, jint, jobject, jint, jboolean);

/*
 * Class:     com_act365_net_GeneralDatagramSocketImpl
 * Method:    _send
 * Signature: (I[BI[BI)V
 */
JNIEXPORT void JNICALL Java_com_act365_net_GeneralDatagramSocketImpl__1send
  (JNIEnv *, jclass, jint, jbyteArray, jint, jbyteArray, jint);

/*
 * Class:     com_act365_net_GeneralDatagramSocketImpl
 * Method:    _join
 * Signature: (I[B)V
 */
JNIEXPORT void JNICALL Java_com_act365_net_GeneralDatagramSocketImpl__1join
  (JNIEnv *, jclass, jint, jbyteArray);

/*
 * Class:     com_act365_net_GeneralDatagramSocketImpl
 * Method:    _joinGroup
 * Signature: (ILjava/net/InetSocketAddress;Ljava/net/NetworkInterface;)V
 */
JNIEXPORT void JNICALL Java_com_act365_net_GeneralDatagramSocketImpl__1joinGroup
  (JNIEnv *, jclass, jint, jobject, jobject);

/*
 * Class:     com_act365_net_GeneralDatagramSocketImpl
 * Method:    _leave
 * Signature: (I[B)V
 */
JNIEXPORT void JNICALL Java_com_act365_net_GeneralDatagramSocketImpl__1leave
  (JNIEnv *, jclass, jint, jbyteArray);

/*
 * Class:     com_act365_net_GeneralDatagramSocketImpl
 * Method:    _leaveGroup
 * Signature: (ILjava/net/InetSocketAddress;Ljava/net/NetworkInterface;)V
 */
JNIEXPORT void JNICALL Java_com_act365_net_GeneralDatagramSocketImpl__1leaveGroup
  (JNIEnv *, jclass, jint, jobject, jobject);

/*
 * Class:     com_act365_net_GeneralDatagramSocketImpl
 * Method:    _setTimeToLive
 * Signature: (IB)V
 */
JNIEXPORT void JNICALL Java_com_act365_net_GeneralDatagramSocketImpl__1setTimeToLive
  (JNIEnv *, jclass, jint, jbyte);

/*
 * Class:     com_act365_net_GeneralDatagramSocketImpl
 * Method:    _getSocketDescriptor
 * Signature: (Ljava/io/FileDescriptor;)I
 */
JNIEXPORT jint JNICALL Java_com_act365_net_GeneralDatagramSocketImpl__1getSocketDescriptor
  (JNIEnv *, jclass, jobject);

/*
 * Class:     com_act365_net_GeneralDatagramSocketImpl
 * Method:    _setSocketDescriptor
 * Signature: (Ljava/io/FileDescriptor;I)V
 */
JNIEXPORT void JNICALL Java_com_act365_net_GeneralDatagramSocketImpl__1setSocketDescriptor
  (JNIEnv *, jclass, jobject, jint);

/*
 * Class:     com_act365_net_GeneralDatagramSocketImpl
 * Method:    _setOption
 * Signature: (IILjava/lang/Object;)I
 */
JNIEXPORT jint JNICALL Java_com_act365_net_GeneralDatagramSocketImpl__1setOption
  (JNIEnv *, jclass, jint, jint, jobject);

/*
 * Class:     com_act365_net_GeneralDatagramSocketImpl
 * Method:    _getOption
 * Signature: (II)Ljava/lang/Object;
 */
JNIEXPORT jobject JNICALL Java_com_act365_net_GeneralDatagramSocketImpl__1getOption
  (JNIEnv *, jclass, jint, jint);

#ifdef __cplusplus
}
#endif
#endif
