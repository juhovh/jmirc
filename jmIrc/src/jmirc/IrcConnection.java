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

public abstract class IrcConnection {
	// We save about 1 kB when inputting these as strings instead of arrays
	protected char[] koi8rmap = "\u2500\u2502\u250C\u2510\u2514\u2518\u251C\u2524\u252C\u2534\u253C\u2580\u2584\u2588\u258C\u2590\u2591\u2592\u2593\u2320\u25A0\u2219\u221A\u2248\u2264\u2265\u00A0\u2321\u00B0\u00B2\u00B7\u00F7\u2550\u2551\u2552\u0451\u2553\u2554\u2555\u2556\u2557\u2558\u2559\u255A\u255B\u255C\u255D\u255E\u255F\u2560\u2561\u0401\u2562\u2563\u2564\u2565\u2566\u2567\u2568\u2569\u256A\u256B\u256C\u00A9\u044E\u0430\u0431\u0446\u0434\u0435\u0444\u0433\u0445\u0438\u0439\u043A\u043B\u043C\u043D\u043E\u043F\u044F\u0440\u0441\u0442\u0443\u0436\u0432\u044C\u044B\u0437\u0448\u044D\u0449\u0447\u044A\u042E\u0410\u0411\u0426\u0414\u0415\u0424\u0413\u0425\u0418\u0419\u041A\u041B\u041C\u041D\u041E\u041F\u042F\u0420\u0421\u0422\u0423\u0416\u0412\u042C\u042B\u0417\u0428\u042D\u0429\u0427\u042A".toCharArray();
	// notes: 9Ah is a non-breaking space
	protected char[] cp1251map = "\u0402\u0403\u201A\u0453\u201E\u2026\u2020\u2021\u20AC\u2030\u0409\u2039\u040A\u040C\u040B\u040F\u0452\u2018\u2019\u201C\u201D\u2022\u2013\u2014\uFFFD\u2122\u0459\u203A\u045A\u045C\u045B\u045F\u00A0\u040E\u045E\u0408\u00A4\u0490\u00A6\u00A7\u0401\u00A9\u0404\u00AB\u00AC\u00AD\u00AE\u0407\u00B0\u00B1\u0406\u0456\u0491\u00B5\u00B6\u00B7\u0451\u2116\u0454\u00BB\u0458\u0405\u0455\u0457\u0410\u0411\u0412\u0413\u0414\u0415\u0416\u0417\u0418\u0419\u041A\u041B\u041C\u041D\u041E\u041F\u0420\u0421\u0422\u0423\u0424\u0425\u0426\u0427\u0428\u0429\u042A\u042B\u042C\u042D\u042E\u042F\u0430\u0431\u0432\u0433\u0434\u0435\u0436\u0437\u0438\u0439\u043A\u043B\u043C\u043D\u043E\u043F\u0440\u0441\u0442\u0443\u0444\u0445\u0446\u0447\u0448\u0449\u044A\u044B\u044C\u044D\u044E\u044F".toCharArray();
	// notes: 98h not used in cp1252 so we encode it to '�'
	//        A0h is a non-breaking space, ADh is a soft hyphen
	protected char[] cp1255map = "\u02AC\uFFFD\u201A\u0192\u201E\u2026\u2020\u2021\u02C6\u2030\uFFFD\u2039\uFFFD\uFFFD\uFFFD\uFFFD\uFFFD\u2018\u2019\u201C\u201D\u2022\u2013\u2014\u02DC\u2122\uFFFD\u203A\uFFFD\uFFFD\uFFFD\uFFFD\u00A0\u00A1\u00A2\u00A3\u20AA\u00A5\u00A6\u00A7\u00A8\u00A9\u00D7\u00AB\u00AC\u00AD\u00AE\u00AF\u00B0\u00B1\u00B2\u00B3\u00B4\u00B5\u00B6\u00B7\u00B8\u00B9\u00F7\u00BB\u00BC\u00BD\u00BE\u00BF\u05B0\u05B1\u05B2\u05B3\u05B4\u05B5\u05B6\u05B7\u05B8\u05B9\uFFFD\u05BB\u05BC\u05BD\u05BE\u05BF\u05C0\u05C1\u05C2\u05C3\u05F0\u05F1\u05F2\u05F3\u05F4\uFFFD\uFFFD\uFFFD\uFFFD\uFFFD\uFFFD\uFFFD\u05D0\u05D1\u05D2\u05D3\u05D4\u05D5\u05D6\u05D7\u05D8\u05D9\u05DA\u05DB\u05DC\u05DD\u05DE\u05DF\u05E0\u05E1\u05E2\u05E3\u05E4\u05E5\u05E6\u05E7\u05E8\u05E9\u05EA\uFFFD\uFFFD\u200E\u200F\uFFFD".toCharArray();
	// notes: a lot of characters encoded as '�' (such a waste of codepoints, cp1255 sucks)
	//        A0h is a non-breaking space, FDh and FEh are base direction characters
	protected Hashtable hashmap = null;
	private boolean utf8detect, utf8output;

	public abstract String connect(String host, int port, String init);
	public abstract void disconnect();

	public abstract String updateConnection();
	public abstract String readLine();
	public abstract String writeData(String input);
	public abstract boolean hasDataInBuffer();
	
	public abstract boolean isConnected();
	public abstract int getBytesIn();
	public abstract int getBytesOut();

	IrcConnection() {
		this.utf8detect = false;
		this.utf8output = false;
	}

	public void setUnicodeMode(boolean utf8detect, boolean utf8output) {
		this.utf8detect = utf8detect;
		this.utf8output = utf8output;
	}

	protected String byteArrayToString(byte[] bytes, String encoding) {
		char[] map = null;
		String ret;

		if (utf8detect) {
			// we need to have some 1-byte fallback, let's use latin1
			if (encoding.equals("UTF-8"))
				encoding = "ISO-8859-1";

			try {
				ret = decodeUTF8(bytes, false);
				return ret;
			} catch (UTFDataFormatException udfe) {}
		}

		if (encoding.equals("KOI8-R"))
			map = koi8rmap;
		else if (encoding.equals("Windows-1251"))
			map = cp1251map;
		else if (encoding.equals("Windows-1255"))
			map = cp1255map;

		if (map != null) {
			char[] chars = new char[bytes.length];
			for (int i=0; i<bytes.length; i++) {
				byte b = bytes[i];
				chars[i] = (b >= 0) ? (char) b : map[b+128];
			}
			ret = new String(chars);
		}
		else if (encoding.equals("UTF-8")) {
			try {
				ret = decodeUTF8(bytes, true);
			} catch (UTFDataFormatException udfe) {
				// this should never happen when gracious decoding is true
				ret = new String(bytes);
			}
		}
		else {
			try {
				ret = new String(bytes, encoding);
			} catch (UnsupportedEncodingException uee) {
				ret = new String(bytes);
			}
		}

		return ret;
	}

	protected byte[] stringToByteArray(String string, String encoding) {
		byte[] ret;

		if (utf8output)
			encoding = "UTF-8";

		if (encoding.equals("KOI8-R") || encoding.equals("Windows-1251") || encoding.equals("Windows-1255")) {
			if (hashmap == null || !encoding.equals((String) hashmap.get("encoding"))) {
				if (encoding.equals("KOI8-R"))
					hashmap = generateHashmap(koi8rmap);
				else if (encoding.equals("Windows-1251"))
					hashmap = generateHashmap(cp1251map);
				else if (encoding.equals("Windows-1255"))
					hashmap = generateHashmap(cp1255map);
				hashmap.put("encoding", encoding);
			}

			char[] chars = string.toCharArray();
			byte[] bytes = new byte[chars.length];

			for (int i=0; i<chars.length; i++) {
				if (chars[i]<0x80)
					bytes[i] = (byte) chars[i];
				else {
					Byte b = (Byte) hashmap.get(new Character(chars[i]));
					bytes[i] = (b == null) ? (byte) 0x3f : b.byteValue(); // 0x3f is '?'
				}
			}

			ret = bytes;
		}
		else if (encoding.equals("UTF-8")) {
			ret = encodeUTF8(string);
		}
		else {
			try {
				ret = string.getBytes(encoding);
			} catch (UnsupportedEncodingException uee) {
				ret = string.getBytes();
			}
		}

		return ret;
	}

	private Hashtable generateHashmap(char[] encmap) {
		Hashtable ret = new Hashtable();

		for (int i=0; i<encmap.length; i++)
			ret.put(new Character(encmap[i]), new Byte((byte) (0x80+i)));

		return ret;
	}

	private byte[] encodeUTF8(String text) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] ret;
		
		for (int i=0; i<text.length(); i++) {
			char c = text.charAt(i);
			if (c != '\u0000' && c < '\u0080')
				baos.write(c);
			else if (c == '\u0000' || (c >= '\u0080' && c < '\u0800')) {
				baos.write((byte)(0xc0 | (0x1f & (c >> 6))));
				baos.write((byte)(0x80 | (0x3f & c)));
			}
			else {
				baos.write((byte)(0xe0 | (0x0f & (c >> 12))));
				baos.write((byte)(0x80 | (0x3f & (c >>  6))));
				baos.write((byte)(0x80 | (0x3f & c)));
			}
		}
		ret = baos.toByteArray();
		
		return ret;
	}
	
	private String decodeUTF8(byte[] data, boolean gracious) throws UTFDataFormatException {
		byte a, b, c;
		StringBuffer ret = new StringBuffer();
		
		for (int i=0; i<data.length; i++) {
			try {
				a = data[i];
				if ((a&0x80) == 0)
					ret.append((char) a);
				else if ((a&0xe0) == 0xc0) {
					b = data[i+1];
					if ((b&0xc0) == 0x80) {
						ret.append((char)(((a&0x1F) << 6) | (b&0x3F)));
						i++;
					}
					else {
						throw new UTFDataFormatException("Illegal 2-byte group");
					}
				}
				else if ((a&0xf0) == 0xe0) {
					b = data[i+1];
					c = data[i+2];
					if (((b&0xc0) == 0x80) && ((c&0xc0) == 0x80)) {
						ret.append((char)(((a&0x0F) << 12) | ((b&0x3F) << 6) | (c&0x3F)));
						i += 2;
					}
					else {
						throw new UTFDataFormatException("Illegal 3-byte group");
					}
				}
				else if (((a&0xf0) == 0xf0) || ((a&0xc0) == 0x80)) {
					throw new UTFDataFormatException("Illegal first byte of a group");
				}
			} catch (UTFDataFormatException udfe) {
				if (gracious)
					ret.append("?");
				else
					throw udfe;
			} catch (ArrayIndexOutOfBoundsException aioobe) {
				if (gracious)
					ret.append("?");
				else
					throw new UTFDataFormatException("Unexpected EOF");
			}
		}
		
		return ret.toString();
	}
}
