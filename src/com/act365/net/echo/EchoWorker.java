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

package com.act365.net.echo ;

import java.io.* ;

/**
 * An instance of the <code>EchoWorker</code> is created by the <code>EchoServer</code>
 * class to handle each new client that is accepted. The <code>EchoWorker</code>
 * objects will simply echo all input it receives back to the output.
 */

class EchoWorker extends Thread {

  InputStream in ;

  OutputStreamWriter out ;

  public EchoWorker( InputStream in , OutputStream out ){
    super();
    this.in  = in ;
    this.out = new OutputStreamWriter( out );
  }

  public void run() {
  
    try {

      StringBuffer buffer = new StringBuffer();

      int ch = in.read();
      
      while( ch > -1 ){          
        if( ch == 0 ){
          break;
        } else if( ch != '\n' ){
          buffer.append((char) ch );
        } else {
          out.write( buffer.toString() );
          out.write('\n');
          out.flush();

          buffer = new StringBuffer();
        }
        ch = in.read();
      }

    } catch( Exception e ){
      System.err.println( e.getClass().getName() );
      System.err.println( e.getMessage() );
    }
  }
}

