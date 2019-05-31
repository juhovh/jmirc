/*
Copyright (C) 2004-2019  Juho Vähä-Herttua

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
Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
*/

package jmirc;

import java.io.*;
import java.util.*;
import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;
import javax.microedition.io.*;

public class TextArea {
	private int left;
	private int top;
	private int width;
	private int height;
	private Font font;
	private int fontheight;

	private int position, emptylines;
	private boolean scrollbar;

	private String[][] scrollbuffer;
	private int bufindex;

	private final int MAX_LINES;;
		
	public TextArea(int left, int top, int width, int height, Font font, int buflines, boolean scrollbar) {
		position = 0;

		this.left = left;
		this.top = top;
		this.width = width;
		this.height = height;
		this.font = font;
		this.scrollbar = scrollbar;
		fontheight = font.getHeight();

		MAX_LINES = buflines;
		scrollbuffer = new String[MAX_LINES][];
		emptylines = MAX_LINES;
	}

	public void setSize(int newtop, int newheight) {
		boolean end = isAtEndpos();
		this.top = newtop;
		this.height = newheight;
		if (end) setPosition(-1);
		else setPosition(position);
	}		

	// -1 is end, -2 is pageup, -3 is pagedown
	// returns true if repaint needed
	public boolean setPosition(int pos) {
		int screenlines = (height / fontheight);
		int maxpos = MAX_LINES - emptylines - screenlines;
		int oldpos = position;
		if (maxpos < 0) maxpos = 0;

		if (pos == -3) updatePosition(screenlines);
		else if (pos == -2) updatePosition(0-screenlines);
		else if (pos < 0) position = maxpos;
		else if (pos > maxpos) position = maxpos;
		else position = pos;

		if (oldpos == position) return false;
		else return true;
	}

	public boolean updatePosition(int dpos) {
		if (position+dpos < 0) return setPosition(0);
		else return setPosition(position+dpos);
	}

	public boolean isAtEndpos() {
		return (position >= MAX_LINES - emptylines - (height / fontheight));
	}

	private void addLine(String[] strings) {
		boolean end = isAtEndpos();
		
		scrollbuffer[bufindex] = strings;
		bufindex = (bufindex+1)%MAX_LINES;

		if (!end && emptylines == 0)
			updatePosition(-1);

		if (emptylines > 0) emptylines--;
	}

	/* First character or String is the colour with following rules:
	     lowest 4 bits indicate the foreground colour
	     next 4 bits indicate the background colour
	     next 4 bits indicate the font style as in Font.STYLE_* (only 3 in use) */
	public synchronized void addText(String[] strings) {
		String tmpline;
		Font font;
		Vector rets, retc;
		int new_width;
		boolean endspace;

		rets = new Vector();
		retc = new Vector();
		font = this.font;
		new_width = 0;
		endspace = false;

		for (int i=0; i<strings.length; i++) {
			String line;
			String[] s;
			char currentcol;

			line = null;
			tmpline = null;
			currentcol = strings[i].charAt(0);
			strings[i] = strings[i].substring(1);

			font = Font.getFont(font.getFace(), (currentcol >> 8)&0x0f, font.getSize());
			s = Utils.splitString(strings[i], " ");
			for (int j=0; j<s.length; j++) {
				// Notice that width includes now scrollbar so it's decreased here in 3 places
				// using 2 pixels for scrollbar so we decrease 5 (1 pixel left, 1 right)
				if (tmpline == null) {
					// colour just changed, we need special handling
					tmpline = s[j];

					// also special linechange handling
					if (new_width + font.stringWidth(tmpline) > width-5) {
						if (endspace) {
							line = "";
							tmpline = " " + tmpline;
						}
						else {
							int k;
							for(k=1; new_width + font.stringWidth(tmpline.substring(0, k))<width-5; k++);
							line = tmpline.substring(0, k-1);
							tmpline = " " + tmpline.substring(k-1);
						}
						new_width = 0;
					}
					new_width += font.stringWidth(tmpline);
				}
				// linechange handling
				else if (new_width + font.stringWidth(" " + s[j]) > width-5) {
					line = tmpline;
					tmpline = " " + s[j];
					new_width = font.stringWidth(tmpline);
				}
				// normal adding
				else {
					tmpline += " " + s[j];
					new_width += font.stringWidth(" " + s[j]);
				}

				while (line != null) {
					// we don't want to add an empty line
					if (!line.equals("")) {
						rets.addElement(line);
						retc.addElement(new Character(currentcol));
					}

					String[] sarray = new String[rets.size()];
					for (int k=0; k<sarray.length; k++)
						sarray[k] = ((Character) retc.elementAt(k)).charValue() + 
						            (String) rets.elementAt(k);
					addLine(sarray);

					rets.removeAllElements();
					retc.removeAllElements();

					if (font.stringWidth(tmpline) > width-5) {
						int k;
						for(k=1; font.stringWidth(tmpline.substring(0, k))<width-5; k++);
						line = tmpline.substring(0, k-1);
						tmpline = " " + tmpline.substring(k-1);
						new_width = font.stringWidth(tmpline);
					}
					else line = null;
				}
			}
			rets.addElement(tmpline);
			retc.addElement(new Character(currentcol));

			if (tmpline.length() > 0 && tmpline.charAt(tmpline.length()-1) == ' ') endspace = true;
			else endspace = false;
		}

		if (rets.size() > 0 && retc.size() > 0) {
			String[] sarray = new String[rets.size()];
			for (int i=0; i<sarray.length; i++) {
				sarray[i] = ((Character) retc.elementAt(i)).charValue() + 
				            (String) rets.elementAt(i);
			}
			addLine(sarray);

			rets.removeAllElements();
			retc.removeAllElements();
		}
	}
	
	public void draw(Graphics g) {
		int mls = (height / fontheight); // max lines in screen
		char lastcolour;

		g.setFont(font);
		g.setColor(0);

		// loops through every line on screen
		for(int i=0; i<mls; i++) {
			int leftpixels = 1 + left;
			int idx = (bufindex+emptylines+position+i)%scrollbuffer.length;
			String[] strings = scrollbuffer[idx];

			lastcolour = 0;
			if (strings == null) break; // we get null and stop iterating
			if (strings[0].charAt(1) == ' ')
				leftpixels += g.getFont().stringWidth(" ");

			for (int j=0; j<strings.length; j++) {
				char currentcol = strings[j].charAt(0);
				String currentstr = strings[j].substring(1);

				if (currentstr.charAt(0) == ' ' && j==0) currentstr = currentstr.substring(1);
				if (currentcol != lastcolour) {
					// set new style
					g.setFont(Font.getFont(font.getFace(), (currentcol >> 8)&0x0f, font.getSize()));
					// set background colour and paint it to screen
					g.setColor(getColor((currentcol >> 4)&0x0f));
					g.fillRect(leftpixels, top+i*fontheight, g.getFont().stringWidth(currentstr), fontheight);
					// set font colour and update lastcolour
					g.setColor(getColor(currentcol&0x0f));
					lastcolour = currentcol;
				}
				g.drawString(currentstr, leftpixels, top+i*fontheight, g.LEFT | g.TOP);
				leftpixels += g.getFont().stringWidth(currentstr);
			}
		}

		if (scrollbar && MAX_LINES - emptylines > mls) {
			int startpos = ((position*height) / (MAX_LINES-emptylines-mls));

			g.setColor(200,200,200);
			g.fillRect(width-3, top, 2, height);
			g.setColor(0,0,0);
			g.fillRect(width-3, top+startpos, 2, 10);
		}
	}

        private int getColor(int numb) {
		numb &= 0x0f;

		// These are taken from http://www.jbisbee.com/linux/colors/ defaults
		switch (numb) {
			case 0:  return 0x00000000;
			case 1:  return 0x00aa0000;
			case 2:  return 0x0000d200;
			case 3:  return 0x00aa5522;
			case 4:  return 0x000000aa;
			case 5:  return 0x00aa00aa;
			case 6:  return 0x0000aaaa;
			case 7:  return 0x00aaaaaa;
			case 8:  return 0x00444444;
			case 9:  return 0x00ff4444;
			case 10:  return 0x0044ff44;
			case 11:  return 0x00ffff44;
			case 12:  return 0x004444ff;
			case 13:  return 0x00ff44ff;
			case 14:  return 0x0044ffff;
			case 15:  return 0x00ffffff;
		}
		return 0x00FFFFFF;
	}

	public void clear() {
		scrollbuffer = new String[MAX_LINES][];
		emptylines = MAX_LINES;
	}
}
