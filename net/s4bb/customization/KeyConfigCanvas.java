package net.s4bb.customization;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Font;
import javax.microedition.midlet.MIDlet;

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
public class KeyConfigCanvas extends Canvas implements CommandListener {
  
  private int currentKey = 0;
  private MIDlet 	midlet;
  private Displayable pred;
  
  public KeyConfigCanvas(MIDlet midlet, Displayable pred) {
    this.midlet = midlet;
    this.pred = pred;
    this.setTitle(Util.S_KEY_CONFIGURATION);
    setCommandListener(this);
    addCommand(new Command(Util.S_NEXT_KEY, Command.ITEM, 1));
    addCommand(new Command(Util.S_CANCEL, Command.CANCEL, 1));
  }
  
  public void paint(Graphics g) {
    g.setColor(255,255,255);
    g.fillRect(0,0,this.getWidth(),this.getHeight());
    g.setColor(0,0,0);    
    int heightSpacer = Font.getDefaultFont().getHeight() + 5;
    // draw key name
    g.drawString(((Key)GlobalConfiguration.getInstance().getKeys().elementAt(currentKey)).name,this.getWidth()/2,heightSpacer,Graphics.BASELINE | Graphics.HCENTER);
    // draw key description
    g.drawString(((Key)GlobalConfiguration.getInstance().getKeys().elementAt(currentKey)).description,this.getWidth()/2,heightSpacer*2,Graphics.BASELINE | Graphics.HCENTER);
    // draw key code
    g.drawString(new Integer(((Key)GlobalConfiguration.getInstance().getKeys().elementAt(currentKey)).value).toString(),this.getWidth()/2,heightSpacer*3,Graphics.BASELINE | Graphics.HCENTER);
  }
  
  protected void keyReleased(int keyCode) {
    // set new keyCode for current key
    ((Key)GlobalConfiguration.getInstance().getKeys().elementAt(currentKey)).value = keyCode;
    // repaint the display
    this.repaint();
  }

  public void commandAction(Command command, Displayable displayable) {
    if (command.getCommandType() == Command.ITEM) {
      // switch to next key or finish
      if (currentKey<GlobalConfiguration.getInstance().getKeys().size()-1) {
        currentKey++;
        this.repaint();
      }
      else {
        // finished
        Display.getDisplay(midlet).setCurrent(pred);
        ConfigHandler.getInstance().saveConfig(GlobalConfiguration.getInstance());
      }
    }
    if (command.getCommandType() == Command.CANCEL) {
      Display.getDisplay(midlet).setCurrent(pred);
    }
  }

}
