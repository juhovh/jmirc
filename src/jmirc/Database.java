/*
Copyright (C) 2005  Juho Vähä-Herttua

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
import javax.microedition.rms.*;

public class Database {
	// these are set in setprofile that is always called
	String profilename;
	String nick;
	String altnick;
	String host;
	int port;
	String channels;
	String username;
	String realname;
	String passwd;

	int[] idxarray;

	// other options
	int profileidx = -1;
	boolean usepoll=false;
	boolean showinput=false;
	String encoding = "ISO-8859-1";
	boolean utf8detect=true;
	boolean utf8output=false;
	String hilight = "";
	int buflines = 50;

	boolean header=true;
	boolean timestamp=false;
	boolean usecolor=true;
	boolean usemirccol=false;
	int fontsize=0;
	
	boolean usehttp=false;
	String gwhost =  "";
	int gwport = 8080;
	String gwpasswd =  "";
	int polltime = 10;

	private final static String STORE_CONFIG = "jmirccfg";
	private final static String STORE_PROFILE = "jmircprof";
	
	public Database() {
	}

	/**
	 * returns 
	 */
	public void load() {
		DataInputStream din;

		try {
			RecordStore rs = RecordStore.openRecordStore(STORE_CONFIG, true);

			int size = rs.getNumRecords();
			String version;

			try {
				byte[] temp = rs.getRecord(1);
				version = (new DataInputStream(new ByteArrayInputStream(temp))).readUTF();
			} catch (Exception e) {
				version = "";
			}

			if (!version.equals(jmIrc.VERSION)) {
				rs.closeRecordStore();
				try {
					RecordStore.deleteRecordStore(STORE_CONFIG);
					RecordStore.deleteRecordStore(STORE_PROFILE);
				} catch (Exception e) {}

				rs = RecordStore.openRecordStore(STORE_CONFIG, true);

				byte[] temp;
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				DataOutputStream dos = new DataOutputStream(baos);
				dos.writeUTF(jmIrc.VERSION);
				temp = baos.toByteArray();
				dos.close();

				rs.addRecord(temp, 0, temp.length);
				temp = new byte[0];
				rs.addRecord(temp, 0, temp.length);
				rs.addRecord(temp, 0, temp.length);
				rs.addRecord(temp, 0, temp.length);
				rs.addRecord(temp, 0, temp.length);

				save_profile();
				save_advanced();
				save_interface();
				save_http();
			}
			else {
				din = new DataInputStream(new ByteArrayInputStream(rs.getRecord(2)));
				profileidx = din.readInt();
				
				din = new DataInputStream(new ByteArrayInputStream(rs.getRecord(3)));
				usepoll = din.readBoolean();
				showinput = din.readBoolean();
				encoding = din.readUTF();
				utf8detect = din.readBoolean();
				utf8output = din.readBoolean();
				buflines = din.readInt();
				hilight = din.readUTF();
				
				din = new DataInputStream(new ByteArrayInputStream(rs.getRecord(4)));
				header = din.readBoolean();
				timestamp = din.readBoolean();
				usecolor = din.readBoolean();
				usemirccol = din.readBoolean();
				fontsize = din.readInt();
				
				din = new DataInputStream(new ByteArrayInputStream(rs.getRecord(5)));
				usehttp = din.readBoolean();
				gwhost = din.readUTF();
				gwport = din.readInt();
				gwpasswd = din.readUTF();
				polltime = din.readInt();

				din.close();
			}
			rs.closeRecordStore();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		getProfiles();
		setProfile(profileidx);
	}
	
	public String[] getProfiles() {
		String[] ret = null;

		try {
			byte[] temp;
			DataInputStream din;
			RecordStore rs = RecordStore.openRecordStore(STORE_PROFILE, true);

			if (rs.getNumRecords() == 0) {
				rs.addRecord(new byte[4], 0, 4);
				idxarray = new int[0];
				ret = new String[0];
			}
			else {
				din = new DataInputStream(new ByteArrayInputStream(rs.getRecord(1)));
				rs.closeRecordStore();

				int profiles = din.readInt();
				ret = new String[profiles];
				idxarray = new int[profiles];

				for (int i=0; i<ret.length; i++) {
					ret[i] = din.readUTF();
					idxarray[i] = din.readInt();
				}
				din.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return ret;
	}

	public void setProfile(int index) {
		if (index < 0) {
			profilename = "Default";
			nick = "";
			altnick = "";
			host =  "irc.us.ircnet.net";
			port = 6667;
			channels = "#jmIrc";
			username = "";
			realname = "jmIrc user";
			passwd = "";
		}
		else if (index < idxarray.length) {
			try {
				int rsidx = idxarray[index];
				RecordStore rs = RecordStore.openRecordStore(STORE_PROFILE, false);
				if (rsidx > 0) {
					DataInputStream dis = new DataInputStream(new ByteArrayInputStream(rs.getRecord(rsidx)));

					profilename = dis.readUTF();
					nick = dis.readUTF();
					altnick = dis.readUTF();
					host = dis.readUTF();
					port = dis.readInt();
					channels = dis.readUTF();
					username = dis.readUTF();
					realname = dis.readUTF();
					passwd = dis.readUTF();
				}
				rs.closeRecordStore();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void addProfile() {
		// this should handle the adding also just fine
		editProfile(idxarray.length);
	}

	public void editProfile(int index) {
		try {
			ByteArrayOutputStream baos;
			DataOutputStream dos;

			RecordStore rs = RecordStore.openRecordStore(STORE_PROFILE, false);
			editProfileName(rs, index, profilename);

			baos = new ByteArrayOutputStream();
			dos = new DataOutputStream(baos);
			dos.writeUTF(profilename);
			dos.writeUTF(nick);
			dos.writeUTF(altnick);
			dos.writeUTF(host);
			dos.writeInt(port);
			dos.writeUTF(channels);
			dos.writeUTF(username);
			dos.writeUTF(realname);
			dos.writeUTF(passwd);
			byte[] temp = baos.toByteArray();
			dos.close();

			rs.setRecord(idxarray[index], temp, 0, temp.length);
			rs.closeRecordStore();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void deleteProfile(int index) {
		try {
			RecordStore rs = RecordStore.openRecordStore(STORE_PROFILE, false);

			rs.deleteRecord(idxarray[index]);
			editProfileName(rs, index, null); // delete profile name

			rs.closeRecordStore();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void editProfileName(RecordStore rs, int idx, String newname) throws Exception {
		int i, profiles;
		DataInputStream dis;
		DataOutputStream dos;
		ByteArrayOutputStream baos;
		boolean createnew = false;

		byte[] temp = rs.getRecord(1);
		dis = new DataInputStream(new ByteArrayInputStream(temp));
		baos = new ByteArrayOutputStream();
		dos = new DataOutputStream(baos);

		profiles = dis.readInt();
		if (newname == null && idx >= 0 && idx < profiles) {
			// if we delete a profile then decrease the idxarray size
			profiles--;;
			idxarray = new int[profiles];
		}
		else if (idx < 0 || idx >= profiles) {
			// create a new profile
			profiles++;
			idxarray = new int[profiles];
			createnew = true;
		}
		else if (newname == null) {
			// can't delete a profile that doesn't exist
			return;
		}
		dos.writeInt(profiles);

		for (i=0; i<profiles; i++) {
			if (i == profiles-1 && createnew) break;

			if (i == idx) {
				if (newname != null) {
					dis.readUTF();
					dos.writeUTF(newname);
					idxarray[i] = dis.readInt();
					dos.writeInt(idxarray[i]);
					continue;
				}
				else {
					dis.readUTF();
					dis.readInt();
				}
			}
			dos.writeUTF(dis.readUTF());
			idxarray[i] = dis.readInt();
			dos.writeInt(idxarray[i]);
		}
		if (createnew) {
			// add a new profile to the end
			dos.writeUTF(newname);
			idxarray[i] = rs.getNextRecordID();
			dos.writeInt(idxarray[i]);

			// also add the empty record for it
			rs.addRecord(new byte[0], 0, 0);
		}

		temp = baos.toByteArray();
		dis.close();
		dos.close();
		rs.setRecord(1, temp, 0, temp.length);

		if (profileidx >= idxarray.length)
			profileidx = idxarray.length-1;
	}

	public String[] getChannels() {
		return Utils.hasNoValue(channels) ? null : Utils.splitString(channels, ",");
	}
	
	public void save_profile() {
		try{
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			DataOutputStream dos = new DataOutputStream(baos);
			byte[] byteout;

			dos.writeInt(profileidx);

			byteout = baos.toByteArray();
			dos.close();
			baos.close();

			RecordStore rs = RecordStore.openRecordStore(STORE_CONFIG, true);
			rs.setRecord(2, byteout, 0, byteout.length);
			rs.closeRecordStore();
		}
		catch(Exception e) {
			e.printStackTrace();
			// error in saving settings, should handle this
		}
	}

	public void save_advanced() {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			DataOutputStream dos = new DataOutputStream(baos);
			byte[] byteout;

			dos.writeBoolean(usepoll);
			dos.writeBoolean(showinput);
			dos.writeUTF(encoding);
			dos.writeBoolean(utf8detect);
			dos.writeBoolean(utf8output);
			dos.writeInt(buflines);
			dos.writeUTF(hilight);

			byteout = baos.toByteArray();
			dos.close();
			baos.close();

			RecordStore rs = RecordStore.openRecordStore(STORE_CONFIG, true);
			rs.setRecord(3, byteout, 0, byteout.length);
			rs.closeRecordStore();
		}
		catch(Exception e) {
			e.printStackTrace();
			// error in saving settings, should handle this
		}
	}

	public void save_interface() {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			DataOutputStream dos = new DataOutputStream(baos);
			byte[] byteout;

			dos.writeBoolean(header);
			dos.writeBoolean(timestamp);
			dos.writeBoolean(usecolor);
			dos.writeBoolean(usemirccol);
			dos.writeInt(fontsize);
			
			byteout = baos.toByteArray();
			dos.close();
			baos.close();

			RecordStore rs = RecordStore.openRecordStore(STORE_CONFIG, true);
			rs.setRecord(4, byteout, 0, byteout.length);
			rs.closeRecordStore();
		}
		catch(Exception e) {
			e.printStackTrace();
			// error in saving settings, should handle this
		}
	}
	
	public void save_http() {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			DataOutputStream dos = new DataOutputStream(baos);
			byte[] byteout;

			dos.writeBoolean(usehttp);
			dos.writeUTF(gwhost);
			dos.writeInt(gwport);
			dos.writeUTF(gwpasswd);
			dos.writeInt(polltime);

			byteout = baos.toByteArray();
			dos.close();
			baos.close();

			RecordStore rs = RecordStore.openRecordStore(STORE_CONFIG, true);
			rs.setRecord(5, byteout, 0, byteout.length);
			rs.closeRecordStore();
		}
		catch(Exception e) {
			e.printStackTrace();
			// error in saving settings, should handle this
		}
	}
}
