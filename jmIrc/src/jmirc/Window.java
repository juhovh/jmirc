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
Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
*/

package jmirc;

import java.io.*;
import java.util.*;
import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;
import javax.microedition.io.*;

public class Window extends Canvas implements CommandListener {
	public static final int TYPE_CONSOLE = 0;
	public static final int TYPE_CHANNEL = 1;
	public static final int TYPE_PRIVATE = 2;

	public static final int STATE_NONE = 0;
	public static final int STATE_INFO = 1;
	public static final int STATE_MSG = 2;
	public static final int STATE_HILIGHT = 3;
	public static final int STATE_SELECTED = 4;

	public static final char MODE_NONE = 0;
	public static final char MODE_VOICE = 1;
	public static final char MODE_HALFOP = 2;
	public static final char MODE_OP = 4;

	private static boolean initialized = false;

	private static String JOIN_CHANNEL, CHANGE_NICK, OPEN_QUERY, ADD_FAVORITE;;

	private int type, state;
	private UIHandler uihandler;
	private Vector names;

	private TextBox textbox;
	private Form favform;

	/* commands common for all windows */
	private static Command cmd_msg, cmd_join, cmd_query, cmd_favourites, cmd_traffic, cmd_timestamp, cmd_disconnect;
	private static Command cmd_closeconsole, cmd_closenamfav;
	private static Command cmd_addfav, cmd_delfav, cmd_sendfav;

	private static Command cmd_close;

	private static Command cmd_whois;
	private static Command cmd_names;
	
	private static Command cmd_send, cmd_cancel;
	private static Command cmd_ok;

	private ChoiceGroup cg_favourites;

	private StringBuffer chanmodes;
	private String name, header, chansize, hilight;
	private long keylocktime;
	private int buflines;
	private boolean timestamp;
	private TextArea textarea;
	private boolean showheader;
	private boolean usecol;
	private boolean mirccol;

	private final static int MAX_LIST_PERSONS = 20;
	private int person_position = 0;

	private List nameslist;
	private List namecmdlist;
	
	public Window(UIHandler uihandler, String name, int type, String hilight, boolean showheader, boolean timestamp, boolean usecol, boolean mirccol, int fontsize, int buflines) {
		super();

		this.uihandler = uihandler;
		this.name = name;
		this.header = name;
		this.chansize = "";
		this.type = type;
		this.hilight = hilight;
		this.showheader = showheader;
		this.timestamp = timestamp;
		this.usecol = usecol;
		this.mirccol = mirccol;
		this.buflines = buflines;

		state = STATE_NONE;
		int textfontsize = 0;
		if (fontsize == 0)
			textfontsize = Font.SIZE_SMALL;
		if (fontsize == 2)
			textfontsize = Font.SIZE_LARGE;
		
		textarea = new TextArea(0, 0, getWidth(), getHeight(), Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, textfontsize), buflines, true);;
		chanmodes = new StringBuffer();
		names = new Vector();
		setHeaderVisible(showheader);

		namecmdlist = new List(name, List.IMPLICIT);
		namecmdlist.append("[Back]",null);
		namecmdlist.append("Query",null);
		namecmdlist.append("Whois",null);
		namecmdlist.append("Kick",null);
		namecmdlist.append("Op",null);
		namecmdlist.append("Deop",null);
		namecmdlist.append("Voice",null);
		namecmdlist.append("Devoice",null);
		namecmdlist.setCommandListener(this);

		if (!initialized) {
			JOIN_CHANNEL = "Join channel";
			OPEN_QUERY = "Query";
			CHANGE_NICK = "Change nick";
			ADD_FAVORITE = "Add favourite";

			cmd_ok = new Command("Ok", Command.OK, 10);

			cmd_send = new Command("Send", Command.OK, 10);
			cmd_cancel = new Command("Cancel", Command.CANCEL, 20);

			cmd_msg = new Command("Msg", Command.OK, 10);
			cmd_join = new Command("Join", Command.SCREEN, 20);
			cmd_query = new Command("Query", Command.SCREEN, 30);
			cmd_favourites = new Command("Favourites", Command.SCREEN, 40);
			cmd_traffic = new Command("Bytecounter", Command.SCREEN, 50);
			cmd_timestamp = new Command("Timestamp on", Command.SCREEN, 60);
			cmd_disconnect = new Command("Disconnect", Command.SCREEN, 70);

			cmd_close = new Command("Close", Command.SCREEN, 65);
			cmd_whois = new Command("Whois", Command.SCREEN, 35);
			cmd_names = new Command("Names", Command.SCREEN, 35);

			cmd_sendfav = new Command("Send", Command.OK, 10);
			cmd_addfav = new Command("Add new", Command.SCREEN, 20);
			cmd_delfav = new Command("Delete selected", Command.SCREEN, 30);

			cmd_closeconsole = new Command("Close", Command.CANCEL, 10);
			cmd_closenamfav = new Command("Close", Command.CANCEL, 90);

			initialized = true;
		}
		
		addMenu();
		this.setCommandListener(this); 
	}
	
	/* get and set methods here */
	public String getName() {
		return name;
	}
	
	public int getType() {
		return type;
	}

	public int getState() {
		return state;
	}

	public void setState(int newstate) {
		state = newstate;
	}

        /* change nick from Listener.java */
	public void nickChangeAction() {
		textbox = new TextBox(CHANGE_NICK, "", 30, TextField.ANY);
		textbox.setCommandListener(new TextboxListener());
		textbox.addCommand(cmd_ok);
		uihandler.setWinlock(true);
		uihandler.setDisplay(textbox);
	}

	/* edit header visibility and update it */
	public void setHeaderVisible(boolean visible) {
		int headerheight;
		if (visible) {
			int hheight = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_BOLD, Font.SIZE_SMALL).getHeight();
			textarea.setSize(hheight, getHeight()-hheight);
		}
		else
			textarea.setSize(0, getHeight());

		showheader = visible;
	}

	private void updateHeader() {
		chansize = " [" + names.size() + "]";
	}

	/* show and close methods */
	private void show() {
		uihandler.setDisplay(this);
	}

	public void clear() {
		textarea.clear();
	}

	public void enterExitMode() {
		this.addCommand(cmd_closeconsole);
		deleteMenu();
	}

	public void close() {
		uihandler.deleteWindow(this);
	}

	/* sending data from textbox on cmd_send, handles '/command' commands */
	private void handleMsg(String str) {
		if (str != null && str.length() > 0) {
			if (str.charAt(0) =='/') {
				String[] s = Utils.splitString(str, " ");
				String command = s[0].toUpperCase();
				if (s.length > 1) {
					boolean temp;

					if (((temp = command.equals("/CTCP")) || command.equals("/MSG")) && s.length > 2) {
						Window win;

						if (Utils.isChannel(s[1]))
							win = uihandler.getChannel(s[1]);
						else
							win = uihandler.getPrivate(s[1]);

						if (temp)
							jmIrc.writeLine("PRIVMSG " + s[1] +  " :\001" + s[2].toUpperCase() + str.substring(7 + s[1].length() + s[2].length()) + "\001");
						else {
							jmIrc.writeLine("PRIVMSG " + s[1] +  " :" + str.substring(6 + s[1].length()));
							win.write(uihandler.nick, str.substring(6 + s[1].length()));
							textbox = null;
							win.show();
							return;
						}
					}
					else if (command.equals("/ME")) {
						jmIrc.writeLine("PRIVMSG " + name +  " :\001ACTION " + str.substring(4) + "\001");
						writeAction("* " + uihandler.nick + " " + str.substring(4));
					}
					else if (command.equals("/RAW")) {
						jmIrc.writeLine(str.substring(5));
						uihandler.getConsole().write("rawcmd", str.substring(5));
					}
					else if (command.equals("/WII") || command.equals("/WHOIS")) {
						jmIrc.writeLine("WHOIS " + s[1] + " " + s[1]);
					}
					else if (command.equals("/NICK")) {
						jmIrc.writeLine("NICK " + s[1]);
						uihandler.nick = s[1];
					}
					else if (command.equals("/TOPIC")) {
						if (type == TYPE_CHANNEL)
							jmIrc.writeLine("TOPIC " + name + " :" + str.substring(7));
					}
					else if (command.equals("/PART")) {
						if (Utils.isChannel(s[1])) {
							jmIrc.writeLine("PART " + s[1] + " :" + (s.length > 2 ? str.substring(6 + s[1].length()) : ""));
						}
						else if (type == TYPE_CHANNEL)
							jmIrc.writeLine("PART " + name + " :" + str.substring(6));
						else
							uihandler.getConsole().writeInfo("Can only part from channels");
					}
					else if (command.equals("/KICK")) {
						if (Utils.isChannel(s[1]) && s.length > 2)
							jmIrc.writeLine("KICK " + s[1] + " " + s[2] + " :" + (s.length > 3 ? str.substring(8 + s[1].length() + s[2].length()) : "") );
						else if (type == TYPE_CHANNEL)
							jmIrc.writeLine("KICK " + name + " " + s[1] + " :" + (s.length > 2 ? str.substring(7 + s[1].length()) : ""));
						else
							uihandler.getConsole().writeInfo("Can only kick from channels");
					}
					else if (command.equals("/QUIT")) {
						jmIrc.disconnect("QUIT :" + str.substring(6));
					}
					else {
						uihandler.getConsole().writeInfo("Unknown command");
					}
				}
				else {
					uihandler.getConsole().writeInfo("Not enough parameters");
				}
			}
			else if (type != TYPE_CONSOLE) {
				String[] lines = Utils.splitString(str, "\n");
				for (int i=0; i<lines.length; i++) {
					jmIrc.writeLine("PRIVMSG " + name + " :" + lines[i]);
					write(uihandler.nick, lines[i]);
				}
			}
		}
		textbox = null;
		show();
	}
	
	/* menu editing methods start from here */
	private void addMenu() {
		this.addCommand(cmd_msg);
		this.addCommand(cmd_join);
		this.addCommand(cmd_query);
		this.addCommand(cmd_favourites);
		this.addCommand(cmd_traffic);
		this.addCommand(cmd_timestamp);

		if (type == Window.TYPE_PRIVATE || type == Window.TYPE_CHANNEL) {
			this.addCommand(cmd_close);

			if (type == Window.TYPE_PRIVATE)
				this.addCommand(cmd_whois);
			else
				this.addCommand(cmd_names);
		}

		this.addCommand(cmd_disconnect);
	}

	private void deleteMenu() {
		this.removeCommand(cmd_msg);
		this.removeCommand(cmd_join);
		this.removeCommand(cmd_query);
		this.removeCommand(cmd_favourites);
		this.removeCommand(cmd_traffic);
		this.removeCommand(cmd_timestamp);

		if (type == Window.TYPE_PRIVATE || type == Window.TYPE_CHANNEL) {
			this.removeCommand(cmd_close);

			if (type == Window.TYPE_PRIVATE)
				this.removeCommand(cmd_whois);
			else
				this.removeCommand(cmd_names);
		}

		this.removeCommand(cmd_disconnect);
	}

	/* Text writing methods start here */
	public void write(String nick, String text) {
		boolean usehilight = false;
		String[] hilightarr = Utils.splitString(hilight, " ");

		for (int i=0; i<hilightarr.length; i++) {
			if (!hilightarr[i].equals("") && text.indexOf(hilightarr[i]) >= 0) {
				usehilight = true;
				break;
			}
		}

		if (usehilight) {
			write(new String[] {"<", nick, "> ", text}, new char[] {0xf0,0xf4,0xf0,0x2f4});
			if (state<STATE_HILIGHT) state = STATE_HILIGHT;
			if (uihandler.keylock) uihandler.playAlarm(false);
		}
		else {
			write(new String[] {"<", nick, "> " + text}, new char[] {0xf0,0xf4,0xf0});
			if (state<STATE_MSG) state = STATE_MSG;
		}
	}
	
	public void writeAction(String str) {
		write(new String[] {str}, new char[] {0xf4});
		if (state<STATE_MSG) state = STATE_MSG;
	}

	public void writeInfo(String str) {
		write(new String[] {"*** " + str}, new char[] {0xf2});
		if (state<STATE_INFO) state = STATE_INFO;
	}
	
	public void write(String[] strings, char[] colours) {
		String[] rets;
		boolean end = textarea.isAtEndpos();

		if (timestamp) {
			Calendar cal = Calendar.getInstance();
			String time = "[" + cal.get(Calendar.HOUR_OF_DAY) + ":" +
			              (cal.get(Calendar.MINUTE)<10?"0":"") + cal.get(Calendar.MINUTE) + "]";
			strings[0] = time + " " + strings[0];
		}

		if (usecol) {
			// colour parser
			if (mirccol) {
				rets = new String[0];
				for (int i=0; i<strings.length; i++)
					rets = Utils.mergeStringArray(rets, parseMircColours(colours[i], strings[i]));
			}
			else {
				rets = new String[strings.length];
				for (int i=0; i<strings.length; i++)
					rets[i] = colours[i] + strings[i];
			}
		}
		else {
			rets = new String[1];
			StringBuffer combined = new StringBuffer();
			for (int i=0; i<strings.length; i++)
				combined.append(strings[i]);
			rets[0] = ((char) 0xf0) + combined.toString();
		}

		textarea.addText(rets);
		if (end) textarea.setPosition(-1);

		uihandler.repaint();
	}
	
	/* Nicklist editing functions start from here */
	public void addNick(char mode, String nick) {
		int i, nsize = names.size();
		String upnick = nick.toUpperCase();

		for (i=0; i<nsize; i++) {
			String str = (String) names.elementAt(i);

			if (str.charAt(0) > mode) continue;
			if (upnick.compareTo(str.substring(1).toUpperCase()) < 1 ||
			    str.charAt(0) < mode) {
				names.insertElementAt(mode + nick, i);
				break;
			}
		}
		if (i == nsize) names.addElement(mode + nick);
		updateHeader();
	}

	public boolean hasNick(String nick) {
		if (getNickIndex(nick) < 0) return false;
		else return true;
	}

	public void changeNick(String oldnick, String newnick) {
		int idx;

		if ((idx = getNickIndex(oldnick)) >= 0) {
			char mode = ((String) names.elementAt(idx)).charAt(0);
			deleteNick(oldnick);
			addNick(mode, newnick);
		}
	}

	public void changeNickMode(char mode, String nick, boolean add) {
		int idx;
		
		if (nick == null)
			return;
		if ((idx = getNickIndex(nick)) >= 0) {
			char oldmode = ((String) names.elementAt(idx)).charAt(0);
			deleteNick(nick);
			if (add)
				addNick((char) (oldmode | mode), nick);
			else
				addNick((char) (oldmode & ~mode), nick);
		}
	}

	public void changeChanMode(char mode, boolean add) {
		int idx = chanmodes.toString().indexOf(""+mode);
		if (add && idx < 0) {
			int i;
			for (i=0; i<chanmodes.length(); i++) {
				if (chanmodes.charAt(i) > mode)
					break;
			}
			chanmodes.insert(i, mode);
		}
		else if (!add && idx >= 0) {
			chanmodes.deleteCharAt(idx);
		}
	}

	public void setChanModes(String modes) {
		chanmodes = new StringBuffer(modes);
		repaint();
	}

	public void deleteNick(String nick) {
		int idx;

		if ((idx = getNickIndex(nick)) >= 0) {
			names.removeElementAt(idx);
			updateHeader();
		}
	}

	public void printNicks() {
		if (names.size() > 10)
			writeInfo("There are " + names.size() + " persons in this channel.");
		else {
			String temp, str = "";
			Enumeration e = names.elements();

			while(e.hasMoreElements()) {
				char mode;

				temp = ((String) e.nextElement());
				if (temp.charAt(0) == MODE_OP) mode = '@';
				else if (temp.charAt(0) == MODE_HALFOP) mode = '%';
				else if (temp.charAt(0) == MODE_VOICE) mode = '+';
				else mode = ' ';
				str += mode + temp.substring(1);
				if (e.hasMoreElements())
					str +=", ";
			}
			writeInfo("Nicks in channel: " + str);
		}
	}

	private int getNickIndex(String nick) {
		int nsize = names.size();

		for (int i=0; i<nsize; i++) {
			String n = (String) names.elementAt(i);
			if (n.substring(1).equals(nick)) {
				return i;
			}
		}
		return -1;
	}

	private void listnames(Command c, Displayable s) {
		nameslist = new List("Names", List.IMPLICIT);
		int mp = (person_position+1)*MAX_LIST_PERSONS;
		if (person_position>0) {
			nameslist.append("[Previous]",null);
		}
		
		if (names.size()>mp) {
			nameslist.append("[Next]",null);					
		}
	
		for(int i=(person_position*MAX_LIST_PERSONS); i<mp && i <names.size(); i++) {
			String n = (String) names.elementAt(i);
			char mode;
			if (n.charAt(0) == MODE_OP) mode = '@';
			else if (n.charAt(0) == MODE_HALFOP) mode = '%';
			else if (n.charAt(0) == MODE_VOICE) mode = '+';
			else mode = ' ';
			nameslist.append(mode + n.substring(1), null);
		}
		nameslist.addCommand(cmd_closenamfav);
		nameslist.setCommandListener(this);
		uihandler.setDisplay(nameslist);
	}

	/* graphics paint method */
	public void paint(Graphics g) {
		g.setColor(0xffffff);
		g.fillRect(0, 0, getWidth(), getHeight());

		if (showheader) {
			Font headerfont = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_BOLD, Font.SIZE_SMALL);
			int i;

			g.setColor(140, 140, 230);
			g.fillRect(0, 0, getWidth(), headerfont.getHeight());

			// indicators drawn here
			int[] wins = uihandler.getIndicators();
			for (i=0; i<wins.length; i++) {
				switch (wins[i]) {
					case STATE_NONE:
						g.setColor(255, 255, 255);
						break;
					case STATE_INFO:
						g.setColor(170, 170, 170);
						break;
					case STATE_MSG:
						g.setColor(170, 0, 170);
						break;
					case STATE_HILIGHT:
						g.setColor(170, 0, 0);
						break;
					case STATE_SELECTED:
						g.setColor(0, 0, 0);
						break;
				}
				g.fillRect(3+(i*5), 1, 3, 3);
			}

			// draw header text
			g.setFont(headerfont);
			g.setColor(0x000000);
			if (chanmodes.length() > 0 && headerfont.stringWidth(header + "(+" + chanmodes.toString() + ")" + chansize) < getWidth()-5-i*5)
				g.drawString(header + "(+" + chanmodes.toString() + ")" + chansize, getWidth()-2, 0, g.RIGHT | g.TOP);
			else if (headerfont.stringWidth(header + chansize) < getWidth()-5-i*5)
				g.drawString(header + chansize, getWidth()-2, 0, g.RIGHT | g.TOP);
			else {
				// not enough space, we need to cut
				int textwidth = getWidth()-5-i*5-headerfont.stringWidth(chansize);
				textwidth -= headerfont.stringWidth(".." + header.substring(header.length()-2));
				for (i=header.length()-3; i>=0 && headerfont.substringWidth(header, 0, i)>textwidth; i--);
				g.drawString(header.substring(0, i) + ".." + header.substring(header.length()-2) + chansize, getWidth()-2, 0, g.RIGHT | g.TOP);
			}

			if (uihandler.keylock) {
				int keyx=3, keyy=5;

				g.setColor(0, 0, 0);
				g.drawLine(keyx, keyy+1, keyx, keyy+3);
				g.drawLine(keyx+2, keyy+1, keyx+2, keyy+3);
				g.drawLine(keyx+4, keyy+1, keyx+4, keyy+2);
				g.drawLine(keyx+6, keyy+1, keyx+6, keyy+2);
				g.drawLine(keyx, keyy+1, keyx+3, keyy+1);
				g.drawLine(keyx+5, keyy, keyx+5, keyy);
				g.drawLine(keyx+5, keyy+3, keyx+5, keyy+3);
			}
		}
		textarea.draw(g);
	}

	/* triggers and callbacks start from here */
	protected void keyPressed(int keyCode) {
		if (keyCode == KEY_NUM5) {
			keylocktime = System.currentTimeMillis();

			if (!uihandler.keylock)
				jmIrc.forceUpdate();

			return; // no other function for 5
		}
		else if (keyCode == KEY_POUND) {
			if (System.currentTimeMillis()-keylocktime < 1000) {
				if (!uihandler.keylock) {
					Alert a = new Alert("Keylock", "Keypad locked!", null, AlertType.INFO);
					a.setTimeout(1000);
					uihandler.setDisplay(a);
					uihandler.setWinlock(true);
					deleteMenu();
				}
				else {
					Alert a = new Alert("Keylock", "Keylock removed!", null, AlertType.INFO);
					a.setTimeout(1000);
					uihandler.setDisplay(a);			
					uihandler.setWinlock(false);
					addMenu();
				}

				uihandler.keylock = !uihandler.keylock;
				keylocktime = 0;
				repaint();
				return;
			}
		}
		else keylocktime = 0;

		if (uihandler.keylock) {
		}
		else if ((keyCode >= 97 && keyCode <= 122) || (keyCode >= 65 && keyCode <= 90)) { // BlackBerry
			textbox = new TextBox("Write text", null, 512, TextField.ANY);
			textbox.insert("" + (char) keyCode, 0);
			textbox.setCommandListener(new TextboxListener());
			textbox.addCommand(cmd_send);
			textbox.addCommand(cmd_cancel);
			uihandler.setWinlock(true);
			uihandler.setDisplay(textbox);
		}
		else if (keyCode == 137) { // another BlackBerry
			this.commandAction(cmd_msg, null);
		}
		else if (keyCode == KEY_NUM2 || getGameAction(keyCode) == UP) {
			if (textarea.updatePosition(-1)) repaint();
		}
		else if (keyCode == KEY_NUM8 || getGameAction(keyCode) == DOWN) {
			if (textarea.updatePosition(1)) repaint();
		}
		else if (keyCode == KEY_NUM4 || getGameAction(keyCode) == LEFT) {
			uihandler.displayPreviousWindow();
		}
		else if (keyCode == KEY_NUM6 || getGameAction(keyCode) == RIGHT) {
			uihandler.displayNextWindow();
		}
		else if (keyCode == KEY_NUM1) {
			if (textarea.setPosition(0)) repaint();
		}
		else if (keyCode == KEY_NUM7) {
			if (textarea.setPosition(-1)) repaint();
		}
		else if (keyCode == KEY_NUM3) {
			if (textarea.setPosition(-2)) repaint();
		}
		else if (keyCode == KEY_NUM9) {
			if (textarea.setPosition(-3)) repaint();
		}
		else if (keyCode == KEY_NUM0) {
			this.commandAction(cmd_favourites, null);
		}
		else if (keyCode == KEY_POUND) {
			uihandler.setHeader(!showheader);
			repaint();
		}
		else if (getGameAction(keyCode) == FIRE) {
			this.commandAction(cmd_msg, null);
		}
	}

	protected void keyReleased(int keyCode) {
		if (!uihandler.keylock && keyCode == KEY_STAR) {
			this.commandAction(cmd_msg, null);
		}
	}

	protected void keyRepeated(int keyCode) {
		keyPressed(keyCode);
	}

	public void commandAction(Command c, Displayable s) {
		if (c == cmd_msg || c == cmd_join || c == cmd_query) {
			if (c == cmd_msg) {
				textbox = new TextBox("Write text","",1000,TextField.ANY);
				textbox.addCommand(cmd_send);
			}
			else {
				textbox = new TextBox(c == cmd_join ? JOIN_CHANNEL : OPEN_QUERY,"",100,TextField.ANY);
				textbox.addCommand(cmd_ok);
			}
			textbox.addCommand(cmd_cancel);
			textbox.setCommandListener(new TextboxListener());
			uihandler.setWinlock(true);
			uihandler.setDisplay(textbox);
		}
		else if (c == cmd_disconnect) {
			jmIrc.disconnect("QUIT :used jmIrc\r\n");
			uihandler.clearChanPriv();
			uihandler.cleanup();
		}
		else if (c == cmd_closeconsole) {
			uihandler.cleanup();
		}
		else if (c == cmd_traffic) {
			String str="Bytes in:" + jmIrc.getBytesIn() + "\n";
			str +="Bytes out:" + jmIrc.getBytesOut() + "\n";
			str +="Total:" + (jmIrc.getBytesOut() + jmIrc.getBytesIn()) ;
			Alert a = new Alert("ByteCounter",str,null, AlertType.INFO);
			a.setTimeout(a.FOREVER);
			uihandler.setDisplay(a);
		}
		else if (c == cmd_timestamp) {
			removeCommand(cmd_timestamp);
			if (timestamp)
				cmd_timestamp = new Command("Timestamp on", Command.SCREEN, 60);
			else
				cmd_timestamp = new Command("Timestamp off", Command.SCREEN, 60);				
			addCommand(cmd_timestamp);
			timestamp = !timestamp;
			repaint();
		}
		else if (c == cmd_names) {
			listnames(c,s);
		}
		else if (c == cmd_whois) {
			jmIrc.writeLine("WHOIS " + name + " " + name);
		}
		else if (c == cmd_close) {
			if (type == Window.TYPE_CHANNEL)
				jmIrc.writeLine("PART " + name);
			close();
		}
		else if (c == cmd_closenamfav) {
			nameslist = null;
			cg_favourites = null;
			favform = null;
			uihandler.setWinlock(false);
			show();
		}
		else if (c == cmd_favourites) {
			favform = new Form("Favourites");
			Vector list = uihandler.getFavs();

			cg_favourites = new ChoiceGroup("Favourites", Choice.MULTIPLE);
			for (int i=0; i<list.size(); i++)
				cg_favourites.append((String) list.elementAt(i), null);
			favform.append(cg_favourites);

			favform.addCommand(cmd_sendfav);
			favform.addCommand(cmd_addfav);
			favform.addCommand(cmd_delfav);
			favform.addCommand(cmd_closenamfav);
			favform.setCommandListener(this);
			uihandler.setWinlock(true);
			uihandler.setDisplay(favform);
		}
		else if (c == cmd_sendfav) {
			boolean[] bools = new boolean[cg_favourites.size()];
			cg_favourites.getSelectedFlags(bools);
			for (int i=0; i<bools.length; i++) {
				if (bools[i]) {
					String str = cg_favourites.getString(i);
					handleMsg(str);
				}
			}
			uihandler.setWinlock(false);
			show();
		}
		else if (c == cmd_addfav) {
			textbox = new TextBox(ADD_FAVORITE, "", 128, TextField.ANY);
			textbox.setCommandListener(new TextboxListener());
			textbox.addCommand(cmd_ok);
			textbox.addCommand(cmd_cancel);
			uihandler.setDisplay(textbox);
		}
		else if (c == cmd_delfav) {
			boolean[] bools = new boolean[cg_favourites.size()];
			cg_favourites.getSelectedFlags(bools);
			for (int i=0; i<bools.length; i++)
				if (bools[i]) uihandler.removeFav(cg_favourites.getString(i));

			Vector list = uihandler.getFavs();
			while(cg_favourites.size() > 0) cg_favourites.delete(0);
			for (int i=0; i<list.size(); i++)
				cg_favourites.append((String) list.elementAt(i), null);

			uihandler.saveFavs();
		}			
		else if (nameslist != null && c == List.SELECT_COMMAND) {
			String name = nameslist.getString(nameslist.getSelectedIndex());
			if (name.equals("[Next]")) {
				person_position++;
				listnames(c, s);
			}
			else if (name.equals("[Previous]")) {
				person_position--;
				listnames(c, s);
			}
			else {
				uihandler.setDisplay(namecmdlist);
				nameslist = null;
			}
		}
		else if (namecmdlist != null && c == List.SELECT_COMMAND) {
			String name = namecmdlist.getString(namecmdlist.getSelectedIndex());
			String nick = namecmdlist.getTitle();

			if (name.equals("[Back]")) {
				listnames(c, s);
				namecmdlist = null;
			}
			else if (name.equals("Query")) {
				Window p = uihandler.getPrivate(nick.substring(1));
				state = STATE_NONE;
				p.show();
				namecmdlist = null;
			}
			else if (name.equals("Whois")) {
				jmIrc.writeLine("WHOIS " + nick.substring(1) + " " + nick.substring(1));
				show();
				namecmdlist = null;
			}
			else if (name.equals("Kick")) {
				jmIrc.writeLine("KICK " + this.name + " " + nick.substring(1) + " :" + uihandler.nick);
				show();
				namecmdlist = null;
			}
			else {
				String mode = "";
				if (name.equals("Op"))
					mode = "+o";
				else if (name.equals("Deop"))
					mode = "-o";
				else if (name.equals("Voice"))
					mode = "+v";
				else if (name.equals("Devoice"))
					mode = "-v";

				jmIrc.writeLine("MODE " + this.name + " " + mode + " " + nick.substring(1));
				show();
				namecmdlist = null;
			}
		}
	}	

	private class TextboxListener implements CommandListener {
		public void commandAction(Command c, Displayable s) {
			uihandler.setWinlock(false);
			if (c == cmd_send)
				handleMsg(textbox.getString());
			else if (c == cmd_cancel) {
				if (textbox.getTitle().equals(ADD_FAVORITE))
					uihandler.setDisplay(favform);
				else
					show();

				textbox = null;
			}
			else if (c == cmd_ok) {
				if (textbox.getString().trim().equals("")) return;
				String tbtitle = textbox.getTitle();

				if (tbtitle.equals(JOIN_CHANNEL)) {
					String str = textbox.getString();
					if (!Utils.isChannel(str))
						str = "#" + str;
					show();
					jmIrc.writeLine("JOIN " + str);
					jmIrc.writeLine("MODE " + str);
					textbox = null;
				}
				else if (tbtitle.equals(CHANGE_NICK)) {
					String str = textbox.getString();
					jmIrc.writeLine("NICK " + str);
					uihandler.nick = str;
					show();
					textbox = null;
				}
				else if (tbtitle.equals(OPEN_QUERY)) {
					String str = textbox.getString();
					uihandler.setDisplay(uihandler.getPrivate(str));
					textbox = null;
				}
				else if (tbtitle.equals(ADD_FAVORITE)) {
					String str = textbox.getString();
					uihandler.addFav(str);
					uihandler.saveFavs();

					Vector list = uihandler.getFavs();
					while(cg_favourites.size() > 0) cg_favourites.delete(0);
					for (int i=0; i<list.size(); i++)
						cg_favourites.append((String) list.elementAt(i), null);
					uihandler.setDisplay(favform);
				}
			}
		}	
	}

	// colour conversion table using irssi style conversion
	private int[] mirccols = {15, 0, 4, 2, 9, 1, 5, 2, 11, 10, 6, 14, 12, 13, 8, 7};
	private String[] parseMircColours(int startcol, String str) {
		String[] strs = null;
		Vector rets, retc;
		int colour = startcol;

		rets = new Vector();
		retc = new Vector();

		for (int i=0; i<str.length(); i++) {
			char ch = str.charAt(i);
			if (ch == 2 || ch == 3 || ch == 22 | ch == 31) {
				if (!str.substring(0, i).equals("")) {
					rets.addElement(str.substring(0, i));
					retc.addElement(new Character((char) colour));
				}

				str = str.substring(i+1);
				i = -1;

				switch (ch) {
					case 2:  // BOLD
						colour ^= (Font.STYLE_BOLD << 8);
						break;
					case 31: //UNDERLINE
						colour ^= (Font.STYLE_UNDERLINED << 8);
						break;
					case 22: // REVERSE
						int oldcol = colour;
						colour &= ~0xff;
						colour |= (oldcol&0x0f) << 4;
						colour |= (oldcol&0xf0) >> 4;
						break;
					case 3:  // mIRC COLOUR
						String[] cols = new String[2];
						for (int j=0; j<str.length(); j++) {
							char c = str.charAt(j);
							if (c != ',' && !Character.isDigit(c)) {
								if (j==0) break;
								if (cols[0] == null)
									cols[0] = str.substring(0, j);
								else cols[1] = str.substring(0, j);
								str = str.substring(j);
								break;
							}
							else if (c == ',' && cols[0] != null) {
								if (j==0) break;
								cols[1] = str.substring(0, j);
								str = str.substring(j);
								break;
							}
							else if (c == ',') {
								if (j==0) {
									str.substring(j+1);
									break;
								}
								cols[0] = str.substring(0, j);

								if (j == str.length()-1 || !Character.isDigit(str.charAt(j+1))) {
									str = str.substring(j);
									break;
								}
								else {
									str = str.substring(j+1);
									j=0;
								}
							}
						}
						if (cols[0] == null) {
							colour &= ~0xff;
							colour |= startcol&0xff;
						}
						else {
							if (cols[0] != null) {
								colour &= ~0x0f;
								colour |= mirccols[Integer.parseInt(cols[0])&0x0f];
							}
							if (cols[1] != null) {
								colour &= ~0xf0;
								colour |= mirccols[(Integer.parseInt(cols[1])&0x0f)] << 4;
							}
						}
						break;
				}
			}
		}
		if (!str.equals("")) {
			rets.addElement(str);
			retc.addElement(new Character((char) colour));
		}

		strs = new String[rets.size()];
		for (int i=0; i<strs.length; i++)
			strs[i] = ((Character) retc.elementAt(i)).charValue() + (String) rets.elementAt(i);

		return strs;
	}
}
