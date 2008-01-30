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
 * Created on 14.05.2006
 *
 * @author: developer
 * @description: <the base function of this file/class/etc.>
 *
 */
package net.s4bb.customization; 

import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.midlet.MIDlet;

public class BES_NonBES_Requester implements CommandListener{
  private MIDlet _midlet = null;
  private Displayable pred = null;
  private Command command_YES = null;
  private Command command_NO  = null;
  
  public BES_NonBES_Requester(MIDlet m) throws NullPointerException{
    _midlet = m;
    if(_midlet==null)
      throw new NullPointerException("The MIDlet must not be null!");
    ConfigHandler.getInstance().loadConfig(GlobalConfiguration.getInstance());
  }
  
  public void initUserRequest(){
    if(!GlobalConfiguration.getInstance().isOverwrite_tcp_settings()){
      pred = Display.getDisplay(_midlet).getCurrent();
      command_YES = new Command( "YES", Command.OK, 0);
      command_NO  = new Command( "NO", Command.CANCEL, 0);
      Alert a = new Alert( "BES?", "Are you using a BES / MDS based environment?", null, AlertType.CONFIRMATION );
      a.addCommand(command_YES);
      a.addCommand(command_NO);
      a.setTimeout(Alert.FOREVER);
      a.setCommandListener(this);
      Display.getDisplay(_midlet).setCurrent(a);
    }
  }
  
  private void saveConfig(){
  }
  
  public void commandAction(Command c, Displayable d){
    if (c == command_YES) { // BES/MDS using
      GlobalConfiguration.getInstance().setOverwrite_tcp_settings(true);
      GlobalConfiguration.getInstance().setDeviceside(false);
    }
    else if (c == command_NO) { // No BES/MDS using
      GlobalConfiguration.getInstance().setOverwrite_tcp_settings(true);
      GlobalConfiguration.getInstance().setDeviceside(true);
    }
    saveConfig();
    ConfigHandler.getInstance().saveConfig(GlobalConfiguration.getInstance());
    Display.getDisplay(_midlet).setCurrent(pred);
  }
}
