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

import javax.microedition.lcdui.*;

public class TextBox_Form extends Form implements ItemStateListener {
  private TextField _textField = null;
  private TextBox_Form_Listener _textBox_Form_Listener = null;
  private String separator;
  private char replacechar = 0;

  public TextBox_Form(String title, String text, int maxSize, int constraints){
    super(title);
    _textField = new TextField("", text, maxSize, constraints);
    this.setItemStateListener(this);
    this.append(_textField);
    
    String sep = System.getProperty("line.separator");;
    if (sep==null) {
      separator = "\n";
    }
    else{
      separator = sep;
    }
  }
  
  public void setTextBox_Form_Listener(TextBox_Form_Listener t){
    this._textBox_Form_Listener = t;
  }
  
  public void itemStateChanged(Item item){
    if (item instanceof TextField) {
      if (item!=null) {
        String s = ((TextField)item).getString();
        if (s!=null) {
          if (s.indexOf(separator)>0) { // return pressed -> lets finish
            //remove the return
            s = this.removeReturns(s);
            if(_textBox_Form_Listener!=null && s!=null){
//              ((TextField)item).setString(s);
              s = s.trim();
              _textBox_Form_Listener.textEntered(this, s);
            }
            //            String text = removeReturns(s);
//            if(_textBox_Form_Listener!=null && text!=null){
//              _textBox_Form_Listener.textEntered(this, text);
//            }
          }
        }
      }
    }
  }
  private String removeReturns(String s) {
    return s.replace('\n', replacechar);
//    String s2 = null;
//    int sepIdx = s.indexOf(separator);
//    int length = s.length();
//    if(sepIdx+1 < length){
//      s2 = s.substring(0,sepIdx);
//      if(s.substring(sepIdx+1).startsWith("\r")){
//        s2 = s2.concat(s.substring(sepIdx+2));
//      }//if
//      else{
//        s2 = s2.concat(s.substring(sepIdx+1));
//      }//else
//    }//if
//    return s2;
  }
  
  public void setReplaceChar(char c){
    replacechar = c;
  }
  
  public void insert(String s, int i){
    if(_textField!=null)
      _textField.insert(s, i);
  }
  
  public String getString(){
    return (_textField!=null) ? _textField.getString() : null;
  }
}
