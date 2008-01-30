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

import javax.microedition.lcdui.ChoiceGroup;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.ItemStateListener;
import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.List;
import javax.microedition.midlet.MIDlet;


public class GlobalSettings_Form extends Form implements
    CommandListener, ItemStateListener {
  private MIDlet 	midlet;
  private Displayable pred;
  private Command command_back, command_select;
//  private ChoiceGroup choiceGroup_saveLocationProvider;
  private ChoiceGroup choiceGroup_overwriteTCPsettings;
  private ChoiceGroup choiceGroup_tcpsettings;
  private int choiceGroup_tcpsettings_ID = -1;
  
  public GlobalSettings_Form(MIDlet midlet, Displayable pred){
    super(Util.S_GLOBAL_SETTINGS);
    this.midlet = midlet;
    this.pred = pred;
    //commands
    command_back = new Command(Util.S_CANCEL, Command.CANCEL, 1);
    command_select = new Command(Util.S_SAVE, Command.ITEM, 1);
    addCommand(command_back);
    addCommand(command_select);
    setCommandListener(this);
    
    /* *** Blackberry network settings *** */
    choiceGroup_overwriteTCPsettings = new ChoiceGroup(null, ChoiceGroup.MULTIPLE);
    choiceGroup_overwriteTCPsettings.append("Overwrite Network Defaults", null);
    choiceGroup_overwriteTCPsettings.setSelectedIndex(0, GlobalConfiguration.getInstance().isOverwrite_tcp_settings());
    append(choiceGroup_overwriteTCPsettings);
    setItemStateListener(this);
    enableTCPsettings(GlobalConfiguration.getInstance().isOverwrite_tcp_settings());
  }
  
  private void enableTCPsettings(boolean enable){
    if(enable){
      choiceGroup_tcpsettings = new ChoiceGroup(null, ChoiceGroup.EXCLUSIVE);
      choiceGroup_tcpsettings.append("Direct TCP (Non-BES-Mode)", null);
      choiceGroup_tcpsettings.append("Proxy TCP (BES-Mode)", null);
      if(GlobalConfiguration.getInstance().isDeviceside())
        choiceGroup_tcpsettings.setSelectedIndex(0, true);
      else
        choiceGroup_tcpsettings.setSelectedIndex(1, true);
      choiceGroup_tcpsettings_ID = append(choiceGroup_tcpsettings);      
    }//if
    else{
      if(choiceGroup_tcpsettings_ID>=0 && choiceGroup_tcpsettings_ID<size()){
        delete(choiceGroup_tcpsettings_ID);
        choiceGroup_tcpsettings = null;
      }
    }//else
  }//enableTCPsettings(boolean enable)
  
  public void commandAction(Command c, Displayable arg1) {
    if(c == command_back)
    {
      Display.getDisplay(midlet).setCurrent(pred);
      return;
    }//if
    if(c.getCommandType() == Command.ITEM | c == List.SELECT_COMMAND)
    {
     GlobalConfiguration.getInstance().setOverwrite_tcp_settings(choiceGroup_overwriteTCPsettings.isSelected(0));
     if(GlobalConfiguration.getInstance().isOverwrite_tcp_settings() & choiceGroup_tcpsettings!=null){
       if(choiceGroup_tcpsettings.isSelected(0)){
         GlobalConfiguration.getInstance().setDeviceside(Util.B_DIRECT_TCP);
       }//if
       else{
         GlobalConfiguration.getInstance().setDeviceside(Util.B_PROXY_TCP);
       }//else
     }//if
     
     ConfigHandler.getInstance().saveConfig(GlobalConfiguration.getInstance());
     return;
    }//if
  }//commandAction()

  public void itemStateChanged(Item item){
    if(item == choiceGroup_overwriteTCPsettings){
      enableTCPsettings(choiceGroup_overwriteTCPsettings.isSelected(0));
    }//if
  }//itemStateChanged(Item item)
}
