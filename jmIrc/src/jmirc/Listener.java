/*
Copyright (C) 2004-2009  Juho Vähä-Herttua

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

public class Listener extends Thread {
	private IrcConnection irc;
	private UIHandler uihandler;
	private Hashtable whois;
	private boolean needupdate, nicktried, pollmode, showinput;

	private String altnick, host, password, username, realname;
	private int port, polltime;
	private String[] channels;
	
	public Listener(Database db, IrcConnection irc, UIHandler uihandler) {
		this.irc = irc;
		this.uihandler = uihandler;

		this.altnick = db.altnick;
		this.host = db.host;
		this.password = db.passwd;
		this.username = db.username;
		this.realname = db.realname;
		this.port = db.port;
		if (db.usehttp) {
			this.pollmode = false;
			uihandler.getConsole().writeInfo("using HTTP proxy server to connect");
		}
		else
			this.pollmode = db.usepoll;
		this.polltime = db.polltime;
		this.showinput = db.showinput;
		channels = db.getChannels();

		whois = new Hashtable();
		needupdate = false;
		nicktried = false;
	}
	
	public void run() {
		String initstr = "", ret;

		String host = this.host;
		int port = this.port;
		uihandler.getConsole().writeInfo("connecting to " + host + " at port " + port);

		String tmpnick = uihandler.nick.equals("") ? "jmIrc_usr" : uihandler.nick;
		String tmpuser = username.equals("") ? tmpnick : username;
		String tmpreal = realname.equals("") ? "jmIrc user" : realname;

		if (!Utils.hasNoValue(this.password))
			initstr += "PASS " + this.password + "\r\n";
		initstr += "NICK " + tmpnick + "\r\n";
		initstr += "USER " + tmpuser + " 8 * :" + tmpreal + "\r\n";

		ret = irc.connect(host, port, initstr);

		if (ret == null)
			listen();
		else
			uihandler.getConsole().writeInfo(ret);

		uihandler.clearChanPriv();
	}

	private void listen() {
		String data, ret = null;
		int counter;

		counter = 0;
		while(irc.isConnected()) {
			try {Thread.sleep(500);} catch(InterruptedException ie){}
			counter++;

			// check poll time here
			if (counter >= polltime*2 && polltime != 0) {
				needupdate = true;
			}

			if (needupdate || pollmode) {
				ret = irc.updateConnection();
				needupdate = false;
				counter = 0;
			}

			if (ret == null) {
				while (irc.hasDataInBuffer()) {
					data = irc.readLine();
					if (data != null) {
						data = Utils.trim(data);
						if (!data.equals(""))
							checkMessage(data);
					}
				}
			}
			if (ret != null) {
				uihandler.getConsole().writeInfo(ret);
				ret = null;
			}
		}
		uihandler.getConsole().writeInfo("Disconnected from server");
	}
	
	private void checkMessage(String response) {
		String[] cmdline, command;
		String fromnick, firstparam;
		Window ch;
		char temp;
		int cmdnum;

		try {
			if (response.indexOf("ERROR ") == 0) {
				uihandler.getConsole().writeInfo(response);
				irc.disconnect();
				return;
			}

			cmdline = parseLine(response);

			if (cmdline[1] == null) return;

			command = Utils.splitString(cmdline[1], " ");
			if (command[0].equals("PING")) {
				jmIrc.writeLine("PONG" + response.substring(4));
				return;
			}

			if (cmdline[0] == null) return;
			if (cmdline[0].indexOf('!') >= 0)
				fromnick = cmdline[0].substring(0, cmdline[0].indexOf('!'));
			else fromnick = cmdline[0];

			if (cmdline[2] == null) firstparam = "";
			else if (cmdline[2].indexOf(" ") != -1)
				firstparam = cmdline[2].substring(0, cmdline[2].indexOf(" "));
			else
				firstparam = cmdline[2];

			try { cmdnum = Integer.parseInt(command[0]); }
			catch (NumberFormatException nfe) { cmdnum = 0; }

			if (cmdnum == 0) {
				if (command[0].equals("MODE")) {
					String abmodes = "beIqd,k";
					String cmodes = "lfJ";
					
					if (Utils.isChannel(command[1])) {
						String[] mparams = new String[3];
						String newmodes;
						String chaninfo;
						int counter = 0;
						boolean adding = true;

						ch = uihandler.getChannel(command[1]);
						if (command.length >= 3) {
							mparams[2] = (command.length==5)?cmdline[2]:null;
							mparams[1] = (command.length==5)?command[4]:null;
							mparams[1] = (command.length==4)?cmdline[2]:mparams[1];
							mparams[0] = (command.length>=4)?command[3]:cmdline[2];
							newmodes = command[2];
						}
						else
							newmodes = cmdline[2];

						for (int i=0; i<newmodes.length(); i++) {
							char modechar = newmodes.charAt(i);
							switch(modechar) {
								case '+':
									adding = true;
									break;
								case '-':
									adding = false;
									break;
								case 'o':
									if (mparams[counter] == null) break;
									ch.changeNickMode(Window.MODE_OP, mparams[counter], adding);
									counter++;
									break;
								case 'h':
									if (mparams[counter] == null) break;
									ch.changeNickMode(Window.MODE_HALFOP, mparams[counter], adding);
									counter++;
									break;
								case 'v':
									if (mparams[counter] == null) break;
									ch.changeNickMode(Window.MODE_VOICE, mparams[counter], adding);
									counter++;
									break;
								default:
									ch.changeChanMode(modechar, adding);
									if (abmodes.indexOf(""+modechar) >= 0 ||
									    (cmodes.indexOf(""+modechar) >= 0 && adding))
										counter++;
									break;
							}
							if (counter > 2) break;
						}
						chaninfo = "* " + fromnick + " changed mode: '" + newmodes + "' ";
						for (int i=0; i < 3 && mparams[i] != null; i++)
							chaninfo += " " + mparams[i];

						ch.writeAction(chaninfo);
					}
				}
				else if (command[0].equals("PRIVMSG")) {
					temp = command[1].charAt(0);
					if (cmdline[2].indexOf('\001') != -1) {
						// found a ctcp command in line, parse all tagged data from string
						int ctcpidx1, ctcpidx2 = -1;
						while((ctcpidx1 = cmdline[2].indexOf('\001', ctcpidx2+1)) != -1) {
							if ((ctcpidx2 = cmdline[2].indexOf('\001', ctcpidx1+1)) == -1)
								break;
							String ctcpcmd;
							String ctcpbody = cmdline[2].substring(ctcpidx1+1, ctcpidx2);
							if (ctcpbody.indexOf(' ') != -1) {
								ctcpcmd = ctcpbody.substring(0, ctcpbody.indexOf(' ')).toUpperCase();
								ctcpbody = ctcpbody.substring(ctcpbody.indexOf(' '));
							}
							else {
								ctcpcmd = ctcpbody.toUpperCase();
								ctcpbody = "";
							}
							if (ctcpcmd.equals("ACTION")) {
								if (temp == '#' || temp == '&' || temp == '!') {
									uihandler.getChannel(command[1]).writeAction("* " + fromnick + " " + ctcpbody);
								}
								else {
									uihandler.getPrivate(fromnick).writeAction("* " + fromnick + " " + ctcpbody);
								}
							}
							else if (ctcpcmd.equals("PING")) {
								jmIrc.writeLine("NOTICE " + fromnick + " :\001PING" + ctcpbody + "\001");
							}
							else if (ctcpcmd.equals("VERSION")) {
								String platf = System.getProperty("microedition.platform");
								if (platf == null) platf = "J2ME device";

								jmIrc.writeLine("NOTICE " + fromnick + " :\001VERSION jmIrc v" + jmIrc.VERSION + " on " + platf + "\001");
							}
							else uihandler.getConsole().writeInfo("* Requested unknown CTCP \'" + ctcpcmd + "\' from " + fromnick + ":" + ctcpbody);
						}
					}
					else if (temp == '#' || temp == '&' || temp == '!') {
						uihandler.getChannel(command[1]).write(fromnick, cmdline[2]);
					}
					else {
						uihandler.getPrivate(fromnick).write(fromnick, cmdline[2]);
					}
				}
				else if (command[0].equals("NOTICE")) {
					uihandler.getConsole().writeAction("-" + fromnick + "- " + cmdline[2]);
				}
				else if (command[0].equals("NICK")) {
					String str;

					if (fromnick.equals(uihandler.nick)) {
						str = "You're now known as " + firstparam;
						uihandler.nick = firstparam;
					}
					else {
						str = fromnick + " is now known as " + firstparam;
					}
					Hashtable ht = uihandler.getChannels();
					Enumeration en = ht.elements();
					while (en.hasMoreElements()) {
						ch = (Window) en.nextElement();
						if (ch.hasNick(fromnick)) {
							ch.writeInfo(str);
							ch.changeNick(fromnick, firstparam);
						}
					}
				}
				else if (command[0].equals("QUIT")) {
					Hashtable ht = uihandler.getChannels();
					Enumeration en = ht.elements();
					while(en.hasMoreElements()) {
						ch = (Window) en.nextElement();
						if (ch.hasNick(fromnick)) {
							ch.writeInfo(fromnick + " has quit irc (" + cmdline[2] + ")");
							ch.deleteNick(fromnick);
						}
					}
				}
				else if (command[0].equals("INVITE")) {
					uihandler.getConsole().writeAction("* You have been invited to " + cmdline[2]);
				}
				else if (command[0].equals("JOIN")) {
					ch = uihandler.getChannel(firstparam);
					ch.writeInfo(fromnick + " has joined " + firstparam);
					if (!fromnick.equals(uihandler.nick))
						ch.addNick(Window.MODE_NONE, fromnick);
				}
				else if (command[0].equals("PART")) {
					if (!fromnick.equals(uihandler.nick)) {
						if (command.length == 1) {
							ch = uihandler.getChannel(cmdline[2]);
							ch.writeInfo(fromnick + " has left " + cmdline[2]);
						}
						else {
							ch = uihandler.getChannel(command[1]);
							ch.writeInfo(fromnick + " has left " + command[1] + " (" + cmdline[2] + ")");
						}
						ch.deleteNick(fromnick);
					}
					else {
						if (command.length == 1)
							uihandler.getChannel(cmdline[2]).close();
						else
							uihandler.getChannel(command[1]).close();
					}
				}
				else if (command[0].equals("KICK")) {
					if (command[2].equals(uihandler.nick)) { // you got kicked
						uihandler.getConsole().writeInfo("You were kicked from " + command[1] + " by " + fromnick + "(" + cmdline[2] + ")");
						uihandler.getChannel(command[1]).close();
					}
					else {
						ch = uihandler.getChannel(command[1]);
						ch.writeInfo(command[2] + " was kicked by " + fromnick + "(" + cmdline[2] + ")");
						ch.deleteNick(command[2]);
						
					}
				}
				else if (command[0].equals("TOPIC")) {
					ch = uihandler.getChannel(command[1]);
					ch.writeInfo(fromnick + " changed topic to '" + cmdline[2] + "'");
				}
				else {
					if (showinput)
						uihandler.getConsole().writeAction("-" + response);
				}
			}
			else {
				switch(cmdnum) {
					case 001:  // RPL_WELCOME
						uihandler.getConsole().writeInfo("Connected to server, joining channels");
						String[] channels = this.channels;
						if (channels !=null) {
							for (int i=0; i<channels.length; i++) {
								jmIrc.writeLine("JOIN " + channels[i].trim());
								jmIrc.writeLine("MODE " + channels[i].trim());
							}
						}
						needupdate = true;
						break;
					case 301:  // RPL_AWAY
						uihandler.getConsole().writeAction("* " + command[2] + " is marked as away: " + cmdline[2]);
						break;
					case 305:  // RPL_UNAWAY
					case 306:  // RPL_NOWAWAY
						uihandler.getConsole().writeAction("* " + cmdline[2]);
						break;
					case 311:  // RPL_WHOISUSER
					case 314:  // RPL_WHOWASUSER
						String string = "Nick: " + command[2] + "\n";
						string += "Name: " + cmdline[2] + "\n";
						string += "Address: " + command[3] + "@" + command[4] + "\n";
						addWhois(command[2].toUpperCase(), string);
						break;
					case 312:  // RPL_WHOISSERVER
						addWhois(command[2].toUpperCase(), "Server: " + cmdline[2] + "\n");
						break;
					case 317:  // RPL_WHOISIDLE
						addWhois(command[2].toUpperCase(), "Idle: " + parseTime(command[3]) + " \n");
						break;
					case 318:  // RPL_ENDOFWHOIS
					case 369:  // RPL_ENDOFWHOWAS
						Alert a = new Alert("Whois",
						                    (String) whois.get(command[2].toUpperCase()),
						                    null,
						                    AlertType.INFO);
						whois.remove(command[2].toUpperCase());
						a.setTimeout(a.FOREVER);
						uihandler.setDisplay(a);
						break;
					case 319:  // RPL_WHOISCHANNELS
						addWhois(command[2], "Channels: " + cmdline[2] + "\n");
						break;
					case 321:  // RPL_LISTSTART
					case 322:  // RPL_LIST
					case 323:  // RPL_LISTEND
						// FIXME: channel list not implemented
						break;
					case 324:  // RPL_CHANNELMODEIS
						String chanmodes;
						
						ch = uihandler.getChannel(command[2]);
						if (command.length > 3)
							chanmodes = command[3];
						else
							chanmodes = cmdline[2];
						if (chanmodes.charAt(0) == '+')
							ch.setChanModes(chanmodes.substring(1));
						break;
					case 331:  // RPL_NOTOPIC
						uihandler.getChannel(command[2]).writeInfo("Channel has no topic");
						break;
					case 332:  // RPL_TOPIC
						uihandler.getChannel(command[2]).writeInfo("Topic is '" + cmdline[2] + "'");
						break;
					case 333:  // RPL_TOPICWHOTIME
						String topicwho = "Topic set by ";
						int exclidx = command[3].indexOf('!');
						if (exclidx >= 0) {
							topicwho += "'" + command[3].substring(0, exclidx) + "'";
						} else {
							topicwho += "'" + command[3] + "'";
						}
						topicwho += " on " + Utils.formatDateMillis(Long.parseLong(cmdline[2])*1000);
						uihandler.getChannel(command[2]).writeInfo(topicwho);
						break;
					case 341:  // RPL_INVITING
						if (command.length == 3)
							uihandler.getConsole().writeAction("* Inviting " + command[2] + " to " + cmdline[2]);
						break;
					case 342:  // RPL_SUMMONING
					case 351:  // RPL_VERSION
					case 352:  // RPL_WHOREPLY
					case 315:  // RPL_ENDOFWHO
						// FIXME: not implemented
						break;
					case 353:  // RPL_NAMREPLY
						ch = uihandler.getChannel(command[3]);
						String[] s = Utils.splitString(cmdline[2].trim(), " ");
						for (int i=0; i<s.length; i++) {
							String ret;
							char mode = Window.MODE_NONE;
							if (s[i].charAt(0) == '@') mode = Window.MODE_OP;
							if (s[i].charAt(0) == '+') mode = Window.MODE_VOICE;

							if (mode != Window.MODE_NONE) ret = s[i].substring(1);
							else ret = s[i];

							ch.addNick(mode, ret);
						}
						break;
					case 366:  // RPL_ENDOFNAMES
						uihandler.getChannel(command[2]).printNicks();
						break;
					case 367:  // RPL_BANLIST
					case 368:  // RPL_ENDOFBANLIST
						// FIXME: no ban implemented
						break;
					case 371:  // RPL_INFO
					case 374:  // RPL_ENDOFINFO
						uihandler.getConsole().writeInfo(cmdline[2]);
						break;
					case 375:  // RPL_MOTDSTART
					case 372:  // RPL_MOTD
					case 376:  // RPL_ENDOFMOTD
						// skip message of the day
						break;
					case 381:  // RPL_YOUREOPER
					case 382:  // RPL_REHASHING
					case 391:  // RPL_TIME
					case 392:  // RPL_USERSSTART
					case 393:  // RPL_USERS
					case 394:  // RPL_ENDOFUSERS
					case 395:  // RPL_NOUSERS
						// FIXME: not implemented, most can be just handled elsewhere
						break;

					case 431:  // ERR_NONICKNAMEGIVEN
					case 432:  // ERR_ERRONEUSNICKNAME
					case 433:  // ERR_NICKNAMEINUSE
						if (!nicktried && !altnick.trim().equals("")) {
							uihandler.getConsole().writeInfo("Nickname in use, trying \'" + altnick + "\'");
							jmIrc.writeLine("NICK " + altnick);
							uihandler.nick = altnick;
							nicktried = true;
						}
						else {
							Window co = uihandler.getConsole();
							co.nickChangeAction();
						}
						break;
					case 471:  // ERR_CHANNELISFULL
					case 473:  // ERR_INVITEONLYCHAN
					case 474:  // ERR_BANNEDFROMCHAN
					case 475:  // ERR_BADCHANNELKEY
						uihandler.getConsole().writeInfo(cmdline[2] + " joining " + command[2]);
						break;
					default:
						if (showinput)
							uihandler.getConsole().writeAction("-" + response);
						break;
				}
			}
		}
		catch (Exception e) { e.printStackTrace(); }
	}
	
	public void setNeedUpdate(boolean value) {
		needupdate = value;
	}

	private void addWhois(String nick, String str) {
		String s = (String) whois.get(nick);
		if (s != null)
			s += str;
		else
			s = str;
		whois.put(nick,s);
	}

	private String[] parseLine(String input) {
		int idx1, idx2;
		String[] ret = new String[3];
		
		if (input.charAt(0) == ':') {
			ret[0] = input.substring(1, input.indexOf(' '));
			idx1 = input.indexOf(" ")+1;
		}
		else {
			ret[0] = null;
			idx1 = 0;
		}

		idx2 = input.indexOf(":", idx1);
		if (idx2 != -1) {
			ret[1] = input.substring(idx1, idx2);
			ret[2] = input.substring(idx2+1);
		}
		else if ((idx2 = input.lastIndexOf(' ')) != -1) {
			ret[1] = input.substring(idx1, idx2);
			ret[2] = input.substring(idx2+1);
		}
		else {
			ret[1] = input.substring(idx1);
			ret[2] = null;
		}

		return ret;
	}

	private String parseTime(String seconds) {
		int sec, min, hour, day;
		String ret = "";

		try {
			sec = Integer.parseInt(seconds);
			day = sec/86400;
			sec -= day*86400;
			hour = sec/3600;
			sec -= hour*3600;
			min = sec/60;
			sec -= min*60;
			ret = new String(day + "d " + hour + "h " + min + "m " + sec + "s");
		} catch (NumberFormatException nfe) { ; }
		return ret;
	}
}
