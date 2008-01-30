/*
Copyright (C) 2004  Juho Vähä-Herttua

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307,
USA.
*/

package net.s4bb.bbirc;

import java.util.*;
import java.io.*;
import javax.microedition.lcdui.*;
import javax.microedition.rms.*;

public class UIHandler {
	private Display display;
	private Window console;
	private boolean header;
	private boolean timestamp;
	private boolean winlock;
	private boolean usecol;
	private boolean mirccol;
	private int fontsize, buflines;
	private Hashtable channels, privates;
	private Vector windows; // excludes console
	private Vector favourites;
	private int currentwin;
	private String hilight;

	public String nick;
	public boolean keylock;

	public UIHandler(Database db, Display disp) {
		nick = db.nick;
		keylock = false;

		header = db.header;
		timestamp = db.timestamp;
		hilight = db.hilight;
		fontsize = db.fontsize;
		buflines = db.buflines;
		usecol = db.usecolor;
		mirccol = db.usemirccol;
		display = disp;

		winlock = false;
		channels = new Hashtable();
		privates = new Hashtable();
		windows = new Vector();
		loadFavs();
		currentwin = 0;
		console = new Window(this, "Status", Window.TYPE_CONSOLE, hilight, header, timestamp, usecol, mirccol, fontsize, buflines);
		addWindow(console);
	}

	public Window getConsole() {
		return console;
	}

	public Window getChannel(String channel) {
		Window win;

		channel = channel.trim();
		win = (Window) channels.get(channel.toUpperCase());

		if (win == null) {
			win = new Window(this, channel, Window.TYPE_CHANNEL, hilight, header, timestamp, usecol, mirccol, fontsize, buflines);
			channels.put(channel.toUpperCase(), win);
			addWindow(win);
		}
		return win;
	}

	public Window getPrivate(String priv) {
		Window win;

		priv = priv.trim();
		win = (Window) privates.get(priv.toUpperCase());
		if (win == null) {
			win = new Window(this, priv, Window.TYPE_PRIVATE, hilight, header, timestamp, usecol, mirccol, fontsize, buflines);
			privates.put(priv.toUpperCase(), win);
			addWindow(win);

			// new private message coming, we play the alarm
			if (keylock) playAlarm(true);
		}
		return win;
	}

	public void addWindow(Window win) {
		windows.addElement(win);
		displayWindow(-1);
	}

	public int[] getIndicators() {
		int[] ret = new int[windows.size()];

		for (int i=0; i<windows.size(); i++) {
			if (i == currentwin) ret[i] = Window.STATE_SELECTED;
			else ret[i] = ((Window) windows.elementAt(i)).getState();
		}
		return ret;
	}

	public void setHeader(boolean visible) {
		header = visible;

		for (int i=0; i<windows.size(); i++)
			((Window) windows.elementAt(i)).setHeaderVisible(header);
	}

	public void displayNextWindow() {
		displayWindow(currentwin+1);
	}

	public void displayPreviousWindow() {
		displayWindow(currentwin-1);
	}

	public void displayWindow(int num) {
		Window win;

		if (winlock) return; // no window changing on winlock

		if (num >= windows.size()) num = 0;
		if (num < 0) num = windows.size()-1;

		if (num != currentwin) {
			((Window) windows.elementAt(currentwin)).setState(Window.STATE_NONE);
			setDisplay((Window) windows.elementAt(num));
			currentwin = num;
		}
		System.gc();
	}

	public void deleteWindow(Window win) {
		if (win.getType() == Window.TYPE_PRIVATE)
			privates.remove(win.getName().toUpperCase());
		if (win.getType() == Window.TYPE_CHANNEL)
			channels.remove(win.getName().toUpperCase());

		if (windows.indexOf(win) <= currentwin) {
			currentwin--;
			setDisplay((Window) windows.elementAt(currentwin));
			windows.removeElement(win);
		}
		else {
			windows.removeElement(win);
			repaint();
		}
	}

	public void setDisplay(Displayable disp) {
		display.setCurrent(disp);
	}

	public Hashtable getChannels() {
		return channels;
	}

	public void clearChanPriv() {
		console.enterExitMode();

		if (currentwin >= 0) {
			setDisplay(console);
			currentwin = 0;
		}

		for (int i=windows.size()-1; i>=1; i--) {
			// we don't clear the windows, just put to closing mode
			((Window) windows.elementAt(i)).enterExitMode();
		}
		console.repaint();
	}

	public void cleanup() {
		windows.removeAllElements();
		channels.clear();
		privates.clear();
		currentwin = -1;

		System.gc();
		setDisplay(jmIrc.mainform);
	}

	public void repaint() {
		if (windows.size() > 0)
			((Window) windows.elementAt(currentwin)).repaint();
	}

	public void setWinlock(boolean lock) {
		winlock = lock;
	}

	public boolean playAlarm(boolean louder) {
		if (louder)
			return AlertType.ALARM.playSound(display);
		else
			return AlertType.INFO.playSound(display);
	}


       /* Favourites handler largerly copied from Virca */

	public void loadFavs() {
		try {
			RecordStore rs = RecordStore.openRecordStore("jmircfav", true);
			favourites = new Vector();
			if (rs.getNumRecords() > 0) {
				byte[] record = rs.enumerateRecords(null, null, false).nextRecord();
				DataInputStream dis = new DataInputStream(new ByteArrayInputStream(record));
				int count = dis.readInt();

				for (int i=0; i<count; i++)
					favourites.addElement(dis.readUTF());

				dis.close();
			}
			rs.closeRecordStore();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void saveFavs() {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(baos);
		try {
			dos.writeInt(favourites.size());
			for (int i=0; i<favourites.size(); i++)
				dos.writeUTF((String) favourites.elementAt(i));
			dos.close();
			baos.close();

			RecordStore.deleteRecordStore("jmircfav");
			RecordStore rs = RecordStore.openRecordStore("jmircfav", true);
			byte[] bytes = baos.toByteArray();
			rs.addRecord(bytes, 0, bytes.length);
			rs.closeRecordStore();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void addFav(String input) {
		for (int i=0; i<favourites.size(); i++) {
			if (input.compareTo((String) favourites.elementAt(i)) < 0) {
				favourites.insertElementAt(input, i);
				return;
			}
		}
		favourites.addElement(input);
	}

	public void removeFav(String input) {
		favourites.removeElement(input);
	}

	public Vector getFavs() {
		return favourites;
	}
}
