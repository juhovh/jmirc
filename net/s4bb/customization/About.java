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
 * Created on 12.05.2006
 *
 * @author: developer
 * @description: <the base function of this file/class/etc.>
 *
 */
package net.s4bb.customization; 

import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.Image;
import javax.microedition.midlet.MIDlet;

public class About {
  public static final int COMMERCIAL    = 0;
  public static final int GPL           = 1;
  private static final String   _COMPANY    = "S4BB Ltd.";
  private static final String   _EMAIL      = "info@s4bb.net";
  private static final String   _LOGO       = "/s4bb_30x30.png";
  private static final int      _LICENSE    = GPL;

  public static Alert getAbout(String name, String version, String image, String company, String email, int license){
    Image logo = null;
    try {
      logo = Image.createImage(image);
    } catch( Exception e ){}
    String title = name + " " + version + " by " + company;
    String content = "";
    switch(license){
      case(COMMERCIAL):{
        content = "Copyright by ".concat(company).concat(". All rights reserved.");
        break;
      }
      case(GPL):{
        content = "This program is distributed under the terms of the GPL. The source code is available through: ".concat(email).concat(".");
        break;
      }
    }
    Alert about = new Alert( title, content, logo, null ); 
    about.setTimeout( Alert.FOREVER );
    return about;
  }
  
  /**
   * Returns an about alert with the parameters (name, version and vendor) from 
   * the given MIDlet and the default parameters using the default license.
   * You need to have the following variables defined in your JAD file
   * <ul>
   * <li><b>MIDlet-Name</b>: MIDP conform name</li>
   * <li><b>MIDlet-Version</b>: MIDP conform version</li>
   * <li><b>MIDlet-Vendor</b>: MIDP conform name of the vendor</li>
   * <li><b>Vendor-Support-EMail</b>: S4BB Ltd. defined support email entry</li>
   * <li><b>Vendor-Company-Logo</b>: S4BB Ltd. defined company logo</li>
   * </ul>
   * @param m The MIDlet
   * @return An "About" for your GUI
   */
  public static Alert getAbout(MIDlet m){
    String name     = "";
    String version  = "";
    String company  = _COMPANY;
    String email    = _EMAIL;
    String logo     = _LOGO;
    try {
      if(m.getAppProperty("MIDlet-Name")          != null) name     = m.getAppProperty("MIDlet-Name");
      if(m.getAppProperty("MIDlet-Version")       != null) version  = m.getAppProperty("MIDlet-Version");
      if(m.getAppProperty("MIDlet-Vendor")        != null) company  = m.getAppProperty("MIDlet-Vendor");
      if(m.getAppProperty("Vendor-Support-EMail") != null) email    = m.getAppProperty("Vendor-Support-EMail");
      if(m.getAppProperty("Vendor-Company-Logo")  != null) logo     = m.getAppProperty("Vendor-Company-Logo");
    } catch (Exception e) {
    }
    return getAbout(name, version, logo, company, email, _LICENSE);
  }
}
