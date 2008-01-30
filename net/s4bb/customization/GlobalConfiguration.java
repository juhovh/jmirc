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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Vector;

import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.StringItem;

import net.rim.device.api.system.Display;

public class GlobalConfiguration {
  public static GlobalConfiguration myGlobalConfiguration = null;
  public static final Font font = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_SMALL);
  public static final int si_layout = StringItem.LAYOUT_NEWLINE_BEFORE;
  private boolean overwrite_tcp_settings  = false;
  private boolean deviceside              = Util.B_DIRECT_TCP;
  
  public static final short KEY_UP = 0;
  public static final short KEY_DOWN = 1;
  public static final short KEY_LEFT = 2;
  public static final short KEY_RIGHT = 3;
  public static final short KEY_SWITCH = 4;
  public static final short KEY_ZOOM_FACTOR = 5;
  
  // indicates the version of the config file to warn a user on updating
  // to a new release with an incompatible config file
  private static final int CONFIG_VERSION = 1;
  
  public GlobalConfiguration() {
    keys = new Vector();
    if (Display.getWidth()>Display.getHeight()) {
      // breites display -> 75XX series
      keys.addElement(new Key("UP","Panning north and zooming in",101)); //2
      keys.addElement(new Key("DOWN","Panning south and zooming out",120)); //8
      keys.addElement(new Key("LEFT","Panning west",115)); //4
      keys.addElement(new Key("RIGHT","Panning east",102)); //6
      keys.addElement(new Key("SWITCH","Switching pan/zoom mode",100)); //5
      keys.addElement(new Key("ZOOM FACTOR","Changing the zoom factor",32)); //space
      }
      else {
        //71XX series
        keys.addElement(new Key("UP","Panning north and zooming in",50)); //2
        keys.addElement(new Key("DOWN","Panning south and zooming out",56)); //8
        keys.addElement(new Key("LEFT","Panning west",52)); //4
        keys.addElement(new Key("RIGHT","Panning east",54)); //6
        keys.addElement(new Key("SWITCH","Switching pan/zoom mode",53)); //5
        keys.addElement(new Key("ZOOM FACTOR","Changing the zoom factor",48)); //0/space
      }

  }
  
  public void fromByteArray( byte[] data ) throws IOException {
    ByteArrayInputStream bin = new ByteArrayInputStream(data);
    DataInputStream din = new DataInputStream( bin );
    int configVersion = din.readInt();
    if (configVersion!=CONFIG_VERSION) {
    }
    else {
      try {
        int vectorSize = din.readInt();
    
        for (int i=0;i<vectorSize;i++) {
          ((Key)keys.elementAt(i)).value = din.readInt();
        }
        overwrite_tcp_settings = din.readBoolean();
        deviceside = din.readBoolean();
      }
      catch (Exception e) {
      }
    }
    din.close();
  }

  public byte[] toByteArray() throws IOException {
    ByteArrayOutputStream bout = new ByteArrayOutputStream();
    DataOutputStream dout = new DataOutputStream( bout );

    dout.writeInt(CONFIG_VERSION);
    dout.writeInt(keys.size());
    
    for (int i=0;i<keys.size();i++) {
      dout.writeInt(((Key)keys.elementAt(i)).value);
    }
    dout.writeBoolean(overwrite_tcp_settings);
    dout.writeBoolean(deviceside);
    
    dout.close();
    return bout.toByteArray();
  }  
  
  public Key getKey(short id) {
    return (Key)keys.elementAt(id);
  }
  
  
  public static GlobalConfiguration getInstance() {
//    if (myGlobalConfiguration==null) {
//      myGlobalConfiguration = new GlobalConfiguration();
//    }
//    return myGlobalConfiguration;
    return (myGlobalConfiguration==null) ? myGlobalConfiguration = new GlobalConfiguration() : myGlobalConfiguration;
  }

  private Vector keys;
  /**
   * @return Returns the keys.
   */
  public Vector getKeys() {
    return keys;
  }

  public static int getCONFIG_VERSION() {
    return CONFIG_VERSION;
  }
  public boolean isDeviceside() {
    return deviceside;
  }
  public void setDeviceside(boolean deviceside) {
    this.deviceside = deviceside;
  }
  public boolean isOverwrite_tcp_settings() {
    return overwrite_tcp_settings;
  }
  public void setOverwrite_tcp_settings(boolean overwrite_tcp_settings) {
    this.overwrite_tcp_settings = overwrite_tcp_settings;
  }
}
