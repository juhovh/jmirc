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
import javax.microedition.io.*;

public class SocketIrc extends IrcConnection {
	private DataInputStream in;
	private DataOutputStream out;
	private String encoding, outbuf;
	private boolean pollmode, connected;

	private int bytein;
	private int byteout;

	public SocketIrc(boolean pollmode, String charset) {
		encoding = charset;

		bytein = 0;
		byteout = 0;
		this.pollmode = pollmode;
		connected = false;
	}

	public String connect(String host, int port, String init) {
		StreamConnection connector;
		String ret = null;

		try {
			connector = (StreamConnection) Connector.open("socket://" + host + ":" + port, Connector.READ_WRITE, true);
			in = connector.openDataInputStream();
			out = connector.openDataOutputStream(); 

			outbuf = null;
			connected = true;
			ret = writeData(init);

		} catch (Exception e) {
			ret = "Error trying to connect to IRC server, aborting... ";
			ret += "Exception: " + e.getMessage();
			return ret;
		}
		return null;
	}

	public void disconnect() {
		if (connected) {
			try {
				if (out != null) {
					out.flush();
					out.close();
				}
				if (in != null)
					in.close();

				connected = false;
			} catch(Exception e) {}
		}
	}
	
	public String updateConnection() {
		byte[] tmp;
		String ret;

		if (outbuf != null && connected) {
			try {
				tmp = stringToByteArray(outbuf, encoding);
				out.write(tmp);
				out.flush();
				byteout += tmp.length;
				outbuf = null;
			}
			catch (Exception e) {
				ret = "Error reading/writing to IRC socket, aborting... ";
				ret += "Exception: " + e.getMessage();
				connected = false;
				return ret;
			}
		}
		return null;
	}

	public String readLine() {
		int ch;
		String ret;
		byte[] buf;

		ret = null;
		try {
			buf = Utils.readLine(in);
			if (buf==null) return null;
			bytein += buf.length + 40;
			ret = byteArrayToString(buf, encoding);

		} catch(Exception e) {
			connected = false;
		}
		return ret;
	}
	
	public String writeData(String input) {
		if (outbuf == null) outbuf = input;
		else outbuf += input;

		return updateConnection();
	}

	public boolean hasDataInBuffer() {
		if (pollmode) {
			try {
				return (in.available() > 0);
			} catch (Exception ioe) {
				return false;
			}
		}
		else return connected;
	}

	public boolean isConnected() {
		return connected;
	}

	public int getBytesIn() {
		return bytein;
	}

	public int getBytesOut() {
		return byteout;
	}
}
