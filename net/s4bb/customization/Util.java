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
 */
package net.s4bb.customization; 

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.util.Vector;

import javax.microedition.io.Connector;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

public final class Util {
  /* *** static strings for everything *** */
  public final static String S_OK 										= "OK";
  public final static String S_EXIT 									= "Exit";
  public final static String S_YES 										= "Yes";
  public final static String S_NO 										= "No";
  public final static String S_BACK 									= "Back";
  public final static String S_SELECT 								= "Select";
  public final static String S_ADD 										= "Add";
  public final static String S_DELETE 								= "Delete";
  public final static String S_EDIT					 					= "Edit";
  public final static String S_DETAILS 								= "Details";
  public final static String S_CANCEL 								= "Cancel";
  public final static String S_SAVE 									= "Save";
  public final static String S_NEXT_KEY				 				= "Next Key";
  public final static String S_BROWSE			 						= "Browse";
  public final static String S_OPTIONS								= "Options";
  public final static String S_PROPERTIES							= "Properties";
  public final static String S_BOOKMARK					 			= "Bookmark";
  public final static String S_HELP										= "Help";
  public final static String S_GLOBAL_SETTINGS   			= "Global Settings";
  public final static String S_RENAME									= "Rename";
  public final static String S_ABOUT                  = "About";
  public final static String S_URL_INPUT							= "URL Input";
  public final static String S_KEY_CONFIG							= "Key Config";
  public final static String S_KEY_CONFIGURATION	   	= "Key Configuration";
  public final static String S_HARDWARE_INFO			    = "Hardware Info";
  public final static String S_LOG										= "Log";
  public final static String S_NAME										= "Name";

  /* *** Blackberry TCP/IP Connection Settings *** */
  public final static boolean B_DIRECT_TCP  = true;   //directly sent to web server
  public final static boolean B_PROXY_TCP   = false;  //sent through BES to web server
  
  /**
   * Reads a complete line from a DataInputStream till a "carriage return" 
   * or a "line feed" comes. 
   * 
   * @param dis DataInputStream from which a line should be read
   * @return the read line as String
   * @throws IOException if an I/O error occurs.
   */
  public static String readLine(DataInputStream dis) throws IOException, EOFException{
    int CR = 13;
    int LF = 10;    
    
    StringBuffer s = new StringBuffer();
    int i;
    while( ((i = dis.read()) != CR) && (i != LF) ){//&& (b != LS.getBytes()[0])){
      s.append((char)i);
    }
    return s.toString();
  }
 
  public static String LS;
  static
  {
    String lineseperator = System.getProperty("line.separator");
    if(lineseperator!=null)
      LS = lineseperator;
    else
      LS = "\n";
  }
  /**
   * Appends a given string to the form with a line break defined by 
   * carriage return. 
   * 
   * @param form the form the string should be added to
   * @param s the string that should be added
   * @return the assigned index of the Item
   */
  public static int appendln(Form form, String s){
    return (s!=null) ? form.append(s + LS) : 0;
  }
  public static int appendln(Form form){
    return form.append(LS);
  }

  /**
   * Appends a given string to the form. 
   * 
   * @param form the form the string should be added to
   * @param s the string that should be added
   * @return the assigned index of the Item
   */
  public static int append(Form form, String s){
    return (s!=null) ? form.append(s) : 0;
  }

  /**
   * Devides a string into an array of the strings. The delimitter
   * is the comma ",". So there will be a conversion from csv to
   * a string array.
   * 
   * @param s the string that wants to be devided
   * @return string array without commas
   */
  public static String[] csv2stringArray(String s){
    Vector ssv = new Vector(0,1);
    String port="";
    for(int i=0; i<s.length(); i++){
      if(s.charAt(i)!=','){
        port+=s.charAt(i);
      }
      else{
        ssv.addElement(port);
        port="";
      }
    }
    ssv.addElement(port);
    String[] ss = new String[ssv.size()];
    ssv.copyInto(ss);
    return ss;
  }
  
  /**
   * Devides a string into an array of the strings. The delimitter
   * is the comma " ". 
   * 
   * @param s
   * @return the "words" array of the given string 
   */
  public static String[] string2stringArray_woSpaces(String s){
    Vector ssv = new Vector(0,1);
    String e="";
    for(int i=0; i<s.length(); i++){
      if(s.charAt(i)!=' '){
        e+=s.charAt(i);
      }
      else{
        ssv.addElement(e);
        e="";
      }
    }
    ssv.addElement(e);
    String[] ss = new String[ssv.size()];
    ssv.copyInto(ss);
    return ss;
  }//string2stringArray_woSpaces()
  
  /**
   * 
   * @param ss
   * @return
   */
  public static String timeToString(int hours, int minutes, int seconds){
    String time = "";
    if(hours<10) time += "0";
    time += hours + ":";
    if(minutes<10) time += "0";
    time += minutes + ":";
    if(seconds<10) time += "0";
    time += seconds;
    return time;
  }
  
  /**
   * Converts a string array to a single string where the strings
   * are devided with blanks.
   * 
   * @param ss the string array that wants to be reunioned
   * @return the reunioned string containing all string array elements
   */
  public static String stringArray2string(String[] ss){
    String s="";
    for(int i=0; i<ss.length; i++)
      s+=ss[i]+' ';
    return s;
  } 
  
  public static final byte UNKNOWN = -1;
  public static final byte CLDC1_0 = 0;
  public static final byte CLDC1_1 = 1;
  public static byte getCLDCversion(){
    String cldc = System.getProperty("microedition.configuration"); //-> Name and version of the supported configuration .
    if(cldc.endsWith("1.0"))
      return CLDC1_0;
    if(cldc.endsWith("1.1"))
      return CLDC1_1;
    return UNKNOWN;
  }

  public static final byte MIDP1_0 = 0;
  public static final byte MIDP2_0 = 1;
  public static byte getMIDPversion(){
    String midp = System.getProperty("microedition.profiles"); //->Names of all supported profiles.
    if(midp.endsWith("1.0"))
      return MIDP1_0;
    if(midp.endsWith("2.0"))
      return MIDP2_0;
    return UNKNOWN;
  }
//System.out.println(System.getProperty("microedition.encoding")); //->Default character encoding set used by the platform .
//System.out.println(System.getProperty("microedition.locale")); //->Name of the platform's current locale .
//System.out.println(System.getProperty("microedition.platform")); //->Name of the host platform or device .
  public static Image scaleImage(Image src, int destinationWidth, int destinationHeight) throws IllegalArgumentException {

    int sourceWidth = src.getWidth();
    int sourceHeight = src.getHeight();
    Image tmp;
    try {
		tmp = Image.createImage(destinationWidth, sourceHeight);
		
    }
    catch (IllegalArgumentException e)
    {
      tmp = null;
      System.out.println("scaleImage failed."); 
    }
    if (tmp!=null) {
    
    Graphics g = tmp.getGraphics();
		int delta = (sourceWidth << 16) / destinationWidth;
		int pos = delta/2;
	
		for (int x = 0; x < destinationWidth; x++) {
			g.setClip(x, 0, 1, sourceHeight);
			g.drawImage(src, x - (pos >> 16), 0, Graphics.LEFT | Graphics.TOP);
			pos += delta;
		}
	
        Image dst = null;
         try {
           dst = Image.createImage(destinationWidth, destinationHeight);
         }
         catch (IllegalArgumentException e)
         {
           System.out.println("scaleImage failed."); 
         }
         if (dst!=null) {
	
		g = dst.getGraphics();
		delta = (sourceHeight << 16) / destinationHeight;
		pos = delta/2;
	
		for (int y = 0; y < destinationHeight; y++) {
			g.setClip(0, y, destinationWidth, 1);
			g.drawImage(tmp, 0, y - (pos >> 16), Graphics.LEFT | Graphics.TOP);
			pos += delta;	
		}
         }
		return dst;		
    }
    else {
      return null;
    }
  }//scaleImage
  
  /***
   * Overwrites the default TCP/IP settings of your Blackberry device. 
   * Direct TCP/IP or proxy TCP/IP connections might be created depending on the 
   * global setting in GlobalConfiguration. This method will only change the 
   * given URI if it starts with "http" or "socket". So this has only effect to
   * HTTP and socket connections.
   * 
   * @param uri The URI you want to connect to
   * @return the Connection of the specified URI
   * @throws IOException
   */
  public static javax.microedition.io.Connection openConnection(String uri) throws IOException{
    if(uri.startsWith("http") || uri.startsWith("socket")){
      if(GlobalConfiguration.getInstance().isOverwrite_tcp_settings()){
        if(GlobalConfiguration.getInstance().isDeviceside()){ //B_DIRECT_TCP
          uri = uri.concat(";deviceside=true");
        }//if
        else{                                                 //B_PROXY_TCP
          uri = uri.concat(";deviceside=false");
        }//else
      }//if
    }//if
    //LogForm.getInstance().appendlnINFO(uri);
    return Connector.open(uri);
  }//openConnection(String uri)
  
  public static javax.microedition.io.Connection openConnection(String uri, int mode, boolean timeouts) throws IOException{
    if(uri.startsWith("http") || uri.startsWith("socket")){
      if(GlobalConfiguration.getInstance().isOverwrite_tcp_settings()){
        if(GlobalConfiguration.getInstance().isDeviceside()){ //B_DIRECT_TCP
          uri = uri.concat(";deviceside=true");
        }//if
        else{                                                 //B_PROXY_TCP
          uri = uri.concat(";deviceside=false");
        }//else
      }//if
    }//if
    //LogForm.getInstance().appendlnINFO(uri);
    return Connector.open(uri, mode, timeouts);
  }//openConnection(String uri)
  
  public static javax.microedition.io.Connection openConnection(String uri, int mode) throws IOException{
    if(uri.startsWith("http") || uri.startsWith("socket")){
      if(GlobalConfiguration.getInstance().isOverwrite_tcp_settings()){
        if(GlobalConfiguration.getInstance().isDeviceside()){ //B_DIRECT_TCP
          uri = uri.concat(";deviceside=true");
        }//if
        else{                                                 //B_PROXY_TCP
          uri = uri.concat(";deviceside=false");
        }//else
      }//if
    }//if
    //LogForm.getInstance().appendlnINFO(uri);
    return Connector.open(uri, mode);
  }//openConnection(String uri)
}//Util
