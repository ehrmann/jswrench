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

package com.act365.net.tcp ;

import java.util.Random ;

/**
 Class ISNCounter provides access to a counter that is
 updated each four milliseconds.
*/

public class ISNCounter {

  static int counter ;

  static ISNUpdater updater ;

  static Random random ;

  static {
    random = new Random();
    update();
    updater = new ISNUpdater( 4 );
    updater.start();
  }

  public static int getCounter() {
    return counter ;
  }

  static void update() {
    counter = random.nextInt();
  }

  public static void terminate(){
    updater.terminate();
  }
}
