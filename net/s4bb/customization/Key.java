/***
 * Copyright 2006 by S4BB Ltd. ALL RIGHTS RESERVED.
 * 
 * confidential
 * 
 * This sourcecode is owned and copyrighted by <a href="http://www.s4bb.net" target="_top">S4BB Ltd.</a>. 
 * 
 * --
 * If this code is used in a GPL project its license is - of course - the GPL.
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, 
 * USA.
 * --
 *
 */
/*
 * Created on 2006-05-12
 *
 * @author: k
 * @description: <the base function of this file/class/etc.>
 *
 */package net.s4bb.customization; 


public class Key {
  
  public Key(String name, String description, int defaultValue) {
    this.name = name;
    this.description = description;
    this.defaultValue = defaultValue;
    this.value = defaultValue;
  }
  
  public String toString() {
    return name + " " + description + " " + value + " default: " + defaultValue;
  }
  
  public String name;
  public String description;
  public int value;
  public int defaultValue;
  
}
