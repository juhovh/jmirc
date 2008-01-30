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

import java.io.IOException;

import javax.microedition.rms.RecordEnumeration;
import javax.microedition.rms.RecordStore;
import javax.microedition.rms.RecordStoreException;

public class ConfigHandler {
  private static ConfigHandler mConfigHandler = null;
  private RecordStore rs = null;
  private String recordstore_ID = "s4bb_recordstore";
	
  private ConfigHandler() {
    try {
      rs = RecordStore.openRecordStore(recordstore_ID ,true);
    }
    catch (Exception e) {
    }
  }
  
  public static ConfigHandler getInstance(){
    if(mConfigHandler == null)
      mConfigHandler = new ConfigHandler();
    return mConfigHandler;
  }

  public void saveConfig(GlobalConfiguration c) {
    try {
      byte[] data = c.toByteArray();
      if (getID()==-1) {
        rs.addRecord(data, 0, data.length );	      
      }
      else {
        rs.setRecord(this.getID(), data, 0, data.length );
      }
//      System.out.println("Config saved.");
    }
    catch( RecordStoreException e ){
      // handle the RMS error here
      e.printStackTrace();
    }
    catch( IOException e ){
      // handle the IO error here
      e.printStackTrace();
    }
  }

  private int getID() {
    int ID = -1;
   
    RecordEnumeration _enum = null;
    try {

      _enum = rs.enumerateRecords(null,null,false);
      while (_enum.hasNextElement()) {
        ID = _enum.nextRecordId();
      }
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    return ID;
  }

  
  public GlobalConfiguration loadConfig(GlobalConfiguration c) {
    if (this.getID()==-1) {
    // c.setFirstStart(true);
//      System.out.println("this should not happen");
      return c;
    }
   
    try {
    
      byte[] data = rs.getRecord(this.getID());
      c.fromByteArray( data );
      System.out.println("Config loaded.");
    }
    catch( RecordStoreException e ){
      // handle the RMS error here
      e.printStackTrace();
    }
    catch( IOException e ){
      // handle the IO error here
      e.printStackTrace();
    }
    return c;  
  }
 
}
