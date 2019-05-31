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
Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, 
USA.
*/

package jmirc;

import java.util.*;
import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;

public class jmIrc extends MIDlet implements CommandListener {
	public final static String VERSION = "0.97";

	private final static int FORM_MAIN = 0;
	private final static int FORM_PROFILES = 1;
	private final static int FORM_CONFIG = 2;
	private final static int FORM_CONFIG_EDIT = 3;
	private final static int FORM_ADVANCED = 4;
	private final static int FORM_INTERFACE = 5;
	private final static int FORM_HTTP = 6;

	private Display display;
	protected static Form mainform;
	private int currentform;
	private boolean running;

	// these are initialized on connection time
	private static IrcConnection irc;
	private static Listener listener; // irc listener

	private static UIHandler uihandler;
	private static Database db;

	private Command cmd_connect, cmd_profiles, cmd_advanced, cmd_interface, cmd_http, cmd_exit;
	private Command cmd_profile_add, cmd_profile_edit, cmd_profile_delete;
	private Command cmd_ok, cmd_cancel;

	private TextField tf_profilename, tf_nick, tf_altnick, tf_host, tf_port, tf_channels, tf_username, tf_realname;
	private TextField tf_hilight, tf_passwd, tf_buflines;
	private TextField tf_gwhost, tf_gwport, tf_gwpasswd, tf_polltime;

	private ChoiceGroup cg_misc, cg_interface, cg_fontsize, cg_encoding;
	private ChoiceGroup cg_usehttp;

	private List list_profile;

	public jmIrc() {
		display = Display.getDisplay(this);

		cmd_connect = new Command("Connect", Command.OK, 1);
		cmd_profiles = new Command("Profiles", Command.SCREEN, 2);
		cmd_advanced = new Command("Advanced", Command.SCREEN, 3);
		cmd_interface = new Command("Interface", Command.SCREEN, 4);
		cmd_http = new Command("HTTP Config", Command.SCREEN, 5);
		cmd_exit = new Command("Exit", Command.EXIT, 6);

		cmd_profile_add = new Command("Add new profile", Command.SCREEN, 2);
		cmd_profile_edit = new Command("Edit profile", Command.SCREEN, 3);
		cmd_profile_delete = new Command("Delete profile", Command.SCREEN, 4);

		cmd_ok = new Command("Save", Command.OK, 1);
		cmd_cancel = new Command("Cancel", Command.EXIT, 10);

		mainform = new Form("jmIrc");
		mainform.addCommand(cmd_connect);
		mainform.addCommand(cmd_profiles);
		mainform.addCommand(cmd_advanced);
		mainform.addCommand(cmd_interface);
		mainform.addCommand(cmd_http);
		mainform.addCommand(cmd_exit);
		mainform.setCommandListener(this);

		db = new Database();
		running = false;
	}

	private void updateMainform() {
		String selprofile = db.profilename;
		if ("".equals(selprofile))
			selprofile = "Default";
		
		for (int i=mainform.size()-1; i>=0; i--)
			mainform.delete(i);
		mainform.append("jmIrc " + VERSION + "\n");
		mainform.append("By juhovh\n");
		mainform.append("Profile: " + selprofile);
	}

	public void startApp() {
		if (!running) {
			db.load();
			updateMainform();
			display.setCurrent(mainform);
			currentform = FORM_MAIN;
			running = true;
		}
	}

	public void commandAction(Command cmd, Displayable disp) {
		if (cmd == cmd_connect) {
			if (db.usehttp)
				irc = (IrcConnection) new HttpIrc(db.gwhost, db.gwport, db.gwpasswd, db.encoding);
			else
				irc = (IrcConnection) new SocketIrc(db.usepoll, db.encoding);

			irc.setUnicodeMode(db.utf8detect, db.utf8output);
			uihandler = new UIHandler(db, display);
			uihandler.setDisplay(uihandler.getConsole());
			listener = new Listener(db, irc, uihandler);
			listener.start();
			System.gc();
		}
		else if (cmd == cmd_exit) {
			try {
				destroyApp(true);
			} catch (MIDletStateChangeException msce) { ; } // this never happens
			notifyDestroyed();
		}
		else if ((cmd == cmd_ok || cmd == cmd_cancel || cmd == List.SELECT_COMMAND) && currentform != FORM_MAIN) {
			if (currentform == FORM_PROFILES) {
				currentform = FORM_MAIN;

				db.profileidx = list_profile.getSelectedIndex();
				db.save_profile();
				list_profile = null;

				db.setProfile(db.profileidx);
				updateMainform();
				display.setCurrent(mainform);
			}
			else if (currentform == FORM_CONFIG || currentform == FORM_CONFIG_EDIT) {
				boolean editing = (currentform == FORM_CONFIG_EDIT);

				if (cmd == cmd_ok) {
					if (tf_nick.getString().equals("")) {
						Alert a = new Alert("Warning", "Nick must be set", null, AlertType.WARNING);
						a.setTimeout(a.FOREVER);
						display.setCurrent(a);
						return;
					}
					currentform = FORM_PROFILES;
					db.profilename = tf_profilename.getString();
					db.nick = tf_nick.getString();
					db.altnick = tf_altnick.getString();
					db.host = tf_host.getString();
					db.port = parseInt(tf_port.getString());
					db.channels = tf_channels.getString();
					db.username = tf_username.getString();
					db.realname = tf_realname.getString();
					db.passwd = tf_passwd.getString();

					if (editing)
						db.editProfile(list_profile.getSelectedIndex());
					else
						db.addProfile();
				}
				currentform = FORM_PROFILES;
				tf_profilename = null;
				tf_nick = null;
				tf_altnick = null;
				tf_host = null;
				tf_port = null;
				tf_channels = null;
				tf_username = null;
				tf_realname = null;
				tf_passwd = null;

				if (cmd == cmd_ok)
					commandAction(cmd_profiles, null);
				else
					display.setCurrent(list_profile);
			}
			else if (currentform == FORM_ADVANCED) {
				currentform = FORM_MAIN;
				if (cmd == cmd_ok) {
					db.usepoll = cg_misc.isSelected(0);
					db.showinput = cg_misc.isSelected(1);
					db.utf8detect = cg_misc.isSelected(2);
					db.utf8output = cg_misc.isSelected(3);
					db.encoding = cg_encoding.getString(cg_encoding.getSelectedIndex());
					db.buflines = parseInt(tf_buflines.getString());
					db.hilight = tf_hilight.getString();
					db.save_advanced();
				}
				cg_misc = null;
				cg_encoding = null;
				tf_buflines = null;
				tf_hilight = null;
				display.setCurrent(mainform);
			}
			else if (currentform == FORM_INTERFACE) {
				currentform = FORM_MAIN;
				if (cmd == cmd_ok) {
					db.header = cg_interface.isSelected(0);
					db.timestamp = cg_interface.isSelected(1);
					db.usecolor = cg_interface.isSelected(2);
					db.usemirccol = cg_interface.isSelected(3);
					db.fontsize = cg_fontsize.getSelectedIndex();
					db.save_interface();
				}
				cg_interface = null;
				cg_fontsize = null;
				display.setCurrent(mainform);
			}
			else if (currentform == FORM_HTTP) {
				currentform = FORM_MAIN;
				if (cmd == cmd_ok) {
					db.usehttp = cg_usehttp.isSelected(0);
					db.gwhost = tf_gwhost.getString();
					db.gwport = parseInt(tf_gwport.getString());
					db.gwpasswd = tf_gwpasswd.getString();
					db.polltime = parseInt(tf_polltime.getString());
					db.save_http();
				}
				cg_usehttp = null;
				tf_gwhost = null;
				tf_gwport = null;
				tf_gwpasswd = null;
				tf_polltime = null;
				display.setCurrent(mainform);
			}
		}
		else {
			Form cfgform;

			if (cmd == cmd_profiles) {
				String[] profiles;

				profiles = db.getProfiles();
				list_profile = new List("Profiles", List.IMPLICIT);
				for (int i=0; i<profiles.length; i++) {
					list_profile.append(profiles[i], null);
				}
				if (db.profileidx >= 0)
					list_profile.setSelectedIndex(db.profileidx, true);

				list_profile.addCommand(cmd_profile_add);
				list_profile.addCommand(cmd_profile_edit);
				list_profile.addCommand(cmd_profile_delete);
				list_profile.addCommand(cmd_ok);

				list_profile.setCommandListener(this);

				display.setCurrent(list_profile);
				currentform = FORM_PROFILES;
				return;
			}
			else if (cmd == cmd_profile_add || cmd == cmd_profile_edit) {
				if (cmd == cmd_profile_edit)
					db.setProfile(list_profile.getSelectedIndex());
				else
					db.setProfile(-1);

				tf_profilename = new TextField("Profile name", db.profilename, 10, TextField.ANY);
				tf_nick = new TextField("Nick", db.nick, 20, TextField.ANY);
				tf_altnick = new TextField("Alternative nick", db.altnick, 20, TextField.ANY);
				tf_host = new TextField("IRC server", db.host, 200, TextField.URL);
				tf_port = new TextField("IRC server port", new Integer(db.port).toString(), 5, TextField.NUMERIC);
				tf_channels = new TextField("Channels", db.channels, 600, TextField.ANY);
				tf_username = new TextField("Username", db.username, 25, TextField.ANY);
				tf_realname = new TextField("Real name", db.realname, 50, TextField.ANY);
				tf_passwd = new TextField("Server password", db.passwd, 25, TextField.PASSWORD);

				cfgform = new Form("Config");
				cfgform.append(tf_profilename);
				cfgform.append(tf_nick);
				cfgform.append(tf_altnick);
				cfgform.append(tf_host);
				cfgform.append(tf_port);
				cfgform.append(tf_channels);
				cfgform.append(tf_username);
				cfgform.append(tf_realname);
				cfgform.append(tf_passwd);

				if (cmd == cmd_profile_edit)
					currentform = FORM_CONFIG_EDIT;
				else
					currentform = FORM_CONFIG;
			}
			else if (cmd == cmd_profile_delete) {
				db.deleteProfile(list_profile.getSelectedIndex());
				commandAction(cmd_profiles, null);
				return;
			}
			else if (cmd == cmd_advanced) {
				cg_misc = new ChoiceGroup("Misc settings", ChoiceGroup.MULTIPLE);
				cg_misc.append("Use socket poll", null);
				cg_misc.append("Print unhandled input", null);
				cg_misc.append("Detect UTF-8", null);
				cg_misc.append("Output UTF-8", null);

				cg_misc.setSelectedIndex(0, db.usepoll);
				cg_misc.setSelectedIndex(1, db.showinput);
				cg_misc.setSelectedIndex(2, db.utf8detect);
				cg_misc.setSelectedIndex(3, db.utf8output);

				cg_encoding = new ChoiceGroup("Character encoding", ChoiceGroup.EXCLUSIVE);
				cg_encoding.append("ISO-8859-1", null);
				cg_encoding.append("ISO-8859-2", null);
				cg_encoding.append("UTF-8", null);
				cg_encoding.append("KOI8-R", null);
				cg_encoding.append("Windows-1251", null);
				cg_encoding.append("Windows-1255", null);
				if (db.encoding.equals("ISO-8859-1")) cg_encoding.setSelectedIndex(0, true);
				else if (db.encoding.equals("ISO-8859-2")) cg_encoding.setSelectedIndex(1, true);
				else if (db.encoding.equals("UTF-8")) cg_encoding.setSelectedIndex(2, true);
				else if (db.encoding.equals("KOI8-R")) cg_encoding.setSelectedIndex(3, true);
				else if (db.encoding.equals("Windows-1251")) cg_encoding.setSelectedIndex(4, true);
				else if (db.encoding.equals("Windows-1255")) cg_encoding.setSelectedIndex(5, true);
				else cg_encoding.setSelectedIndex(0, true);

				tf_buflines = new TextField("Backbuffer lines", new Integer(db.buflines).toString(), 3, TextField.NUMERIC);
				tf_hilight = new TextField("Highlight string", db.hilight, 50, TextField.ANY);

				cfgform = new Form("Advanced Config");
				cfgform.append(cg_misc);
				cfgform.append(cg_encoding);
				cfgform.append(tf_buflines);
				cfgform.append(tf_hilight);
				currentform = FORM_ADVANCED;
			}
			else if (cmd == cmd_interface) {
				cg_interface = new ChoiceGroup("Interface settings", ChoiceGroup.MULTIPLE);
				cg_interface.append("Use status header", null);
				cg_interface.append("Use timestamp", null);
				cg_interface.append("Use colours", null);
				cg_interface.append("Use mIRC colours", null);
				
				cg_interface.setSelectedIndex(0, db.header);
				cg_interface.setSelectedIndex(1, db.timestamp);
				cg_interface.setSelectedIndex(2, db.usecolor);
				cg_interface.setSelectedIndex(3, db.usemirccol);
				
				cg_fontsize = new ChoiceGroup("Font size", ChoiceGroup.EXCLUSIVE);
				cg_fontsize.append("Small", null);
				cg_fontsize.append("Medium", null);
				cg_fontsize.append("Large", null);
				cg_fontsize.setSelectedIndex(db.fontsize, true);

				cfgform = new Form("Interface Config");
				cfgform.append(cg_interface);
				cfgform.append(cg_fontsize);
				currentform = FORM_INTERFACE;
			}
			else if (cmd == cmd_http) {				
				cg_usehttp = new ChoiceGroup("HTTP", ChoiceGroup.MULTIPLE);
				cg_usehttp.append("Use HTTP proxy server", null);
				cg_usehttp.setSelectedIndex(0, db.usehttp);

				tf_gwhost = new TextField("Proxy server", db.gwhost, 200, TextField.URL);
				tf_gwport = new TextField("Proxy port", new Integer(db.gwport).toString(), 5, TextField.NUMERIC);
				tf_gwpasswd = new TextField("Proxy password", db.gwpasswd, 10, TextField.PASSWORD);
				tf_polltime = new TextField("HTTP poll time (sec):", new Integer(db.polltime).toString(), 2, TextField.NUMERIC);

				cfgform = new Form("HTTP Config");
				cfgform.append(cg_usehttp);
				cfgform.append(tf_gwhost);
				cfgform.append(tf_gwport);
				cfgform.append(tf_gwpasswd);
				cfgform.append(tf_polltime);
				currentform = FORM_HTTP;
			}
			else return;

			cfgform.addCommand(cmd_ok);
			cfgform.addCommand(cmd_cancel);
			cfgform.setCommandListener(this);

			display.setCurrent(cfgform);
		}
	}

	/**
	 * Time to pause, free any space we don't need right now.
	 */
	public void pauseApp() {
	}

	/**
	 * Destroy must cleanup everything.
	 */
	public void destroyApp(boolean unconditional) throws MIDletStateChangeException {
		if (irc != null && irc.isConnected()) {
			if (!unconditional)
				throw new MIDletStateChangeException("IRC is still connected");

			jmIrc.disconnect("QUIT :jmIrc destroyed by the OS");
		}
		if (db.usehttp) {
			try {Thread.sleep(1000);} catch (InterruptedException ie) {}
		}
		//if (uihandler != null) uihandler.cleanup();
	}
	

	private int parseInt(String input) {
		int ret = 0;

		try {
			ret = Integer.parseInt(input);
		} catch (NumberFormatException nfe) {}

		return ret;
	}


	// Static functions called from other classes
	public static void writeLine(String message) {
		if (irc.isConnected()) {
			String ret = irc.writeData(message + "\r\n");
			if (ret != null) {
				uihandler.getConsole().writeInfo(ret);
			}
			listener.setNeedUpdate(true);
		}
	}

	public static void forceUpdate() {
		listener.setNeedUpdate(true);
	}

	public static void disconnect(String quitmessage) {
		if (irc.isConnected()) {
			String ret = irc.writeData(quitmessage + "\r\n");
			if (ret != null) {
				uihandler.getConsole().writeInfo(ret);
			}
			// disconnect is handled in listener after a moment
			// irc.disconnect();

			listener.setNeedUpdate(true);
		}
	}

	public static int getBytesIn() {
		return irc.getBytesIn();
	}

	public static int getBytesOut() {
		return irc.getBytesOut();
	}
}
