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
import java.util.Vector;

public class HttpIrc extends IrcConnection {
	private Vector inqueue;
	private String encoding, identifier, gwhost, gwpasswd, outbuf;
	private int gwport;
	private boolean connected, closeconn;

	private int bytein;
	private int byteout;

	public HttpIrc(String gwhost, int gwport, String gwpasswd, String charset) {
		this.gwhost = gwhost;
		this.gwport = gwport;
		this.gwpasswd = gwpasswd;
		encoding = charset;
		inqueue = new Vector();
		outbuf = null;

		bytein = 0;
		byteout = 0;
		connected = closeconn = false;
		identifier = "";
	}

	public String connect(String host, int port, String init) {
		HttpConnection c = null;
		String url, ret = "";
		int response;

		url = "http://" + gwhost + ":" + gwport + "/connect?host=" + Utils.URLEncode(host.getBytes()) +
		      "&port=" + Utils.URLEncode((""+port).getBytes()) + "&passwd=" + Utils.URLEncode(gwpasswd.getBytes()) +
		      "&data=" + Utils.URLEncode(stringToByteArray(init, encoding));

		try {
			c = (HttpConnection) Connector.open(url);
			c.setRequestMethod(HttpConnection.GET);
			response = c.getResponseCode();
			identifier = c.getHeaderField("X-Identifier");
			if (c != null) c.close();
		}
		catch (Exception e) {
			ret += "Error trying to connect to HTTP proxy server, aborting... ";
			ret += "Exception: " + e.getMessage();
			return ret;
		}

		if (response != HttpConnection.HTTP_OK) {
			ret += "Error trying to connect to IRC server, reason: ";

			switch(response) {
				case HttpConnection.HTTP_FORBIDDEN:
					ret += "Wrong password";
					break;
				case HttpConnection.HTTP_BAD_GATEWAY:
					ret += "Bad gateway";
					break;
				case HttpConnection.HTTP_NOT_FOUND:
					ret += "IRC connection not found";
					break;
				default:
					ret += "HTTP response code: " + response;
					break;
			}

			return ret;
		}
		else {
			connected = true;
			return null;
		}
	}

	public void disconnect() {
		if (connected)
			closeconn = true;
	}

	public String readLine() {
		String ret;

		if (inqueue.size() > 0) {
			ret = (String) inqueue.firstElement();
			inqueue.removeElementAt(0);
		}
		else ret = "";

		return ret;
	}

	public String updateConnection() {
		String url, ret;

		url = "http://" + gwhost + ":" + gwport + "/" + identifier;
		if (outbuf != null) {
			url += "?data=" + Utils.URLEncode(stringToByteArray(outbuf, encoding));
			byteout += url.getBytes().length;
			outbuf = null;
		}
		ret = handleRequest(url, true);
		if (closeconn) connected = false;

		return ret;
	}

	public String writeData(String data) {
		if (outbuf == null) outbuf = data;
		else outbuf += data;

		return null;
	}

	private String handleRequest(String url, boolean get) {
		HttpConnection c = null;
		InputStream is = null;
		ByteArrayInputStream bais;
		byte[] buf;
		String temp, ret = "";
		int response, len, i;
		boolean encok;

		try {
			c = (HttpConnection) Connector.open(url);
			if (get) c.setRequestMethod(HttpConnection.GET);
			else c.setRequestMethod(HttpConnection.HEAD);

			response = c.getResponseCode();

			if (get) {
				is = c.openInputStream();

				// warning, loss of precision
				len = (int) c.getLength();
				if (len > 0) {
					byte[] data = new byte[len];
					for (i=0; i<len; i++) {
						data[i] = (byte) is.read();
					}
					bytein += data.length;

					bais = new ByteArrayInputStream(data);
					while (bais.available() > 0) {
						buf = Utils.readLine(bais);
						if (buf != null) {
							temp = byteArrayToString(buf, encoding);
							inqueue.addElement(temp);
						}
					}
				}
			}
			if (is != null) is.close();
			if (c != null) c.close();

		} catch (Exception e) {
			ret += "Request failed, continuing...";
			return ret;
		}

		if (response != HttpConnection.HTTP_OK) {
			// if connection not found it has just disconnected between polling
			if (response != HttpConnection.HTTP_NOT_FOUND) {
				ret += "Error in connection to IRC server, aborting... ";
				ret += "Error: HTTP response code: " + response;
			}

			connected = false;
			return ret;
		}
		else return null;
	}

	public boolean hasDataInBuffer() {
		if (inqueue.size() == 0) return false;
		else return true;
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
