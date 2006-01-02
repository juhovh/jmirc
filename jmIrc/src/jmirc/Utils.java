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

public class Utils {
	public static byte[] readLine(InputStream is) throws IOException {
		byte[] ret, buf;
		int i;

		buf = new byte[512];
		for(i=0; i<512; i++) {
			int readbyte = is.read();
			if (readbyte == -1) throw new EOFException();

			buf[i] = (byte) readbyte;
			if (buf[i] == '\n')
				break;
		}
		if (i==512) return null;
		if (i>0 && buf[i-1] == '\r') i--;
		ret = new byte[i];
		System.arraycopy(buf, 0, ret, 0, i);
		return ret;
	}

	public static String[] splitString(String str, String delims) {
		if (str == null)
			return null;
		else if (str.equals("") || delims == null || delims.length() == 0)
		        return new String[] {str};

		String[] s;
	  	Vector v = new Vector();
		int pos, newpos;

		pos = 0;
		newpos = str.indexOf(delims, pos);

		while(newpos !=-1) {
			v.addElement(str.substring(pos, newpos));
			pos = newpos + delims.length();
			newpos = str.indexOf(delims, pos);
		}
		v.addElement(str.substring(pos));
		
		s = new String[v.size()];
		for(int i=0; i<s.length; i++) {
			s[i] = (String) v.elementAt(i);
		}
		return s;
	}
	
	public static boolean hasNoValue(String s) {
		return (s == null || s.equals("") || s.getBytes().length ==0);
	}

	public static String URLEncode(byte[] input) {
		StringBuffer ret;
		int i, temp;

		if (input==null) return null;

		ret = new StringBuffer();
		for (i=0; i<input.length; i++) {
			temp = input[i] & 0xff; // [-128,127] to [0,255]
			if ((temp >= 0x30 && temp <= 0x39) || // 0-9
			    (temp >= 0x41 && temp <= 0x5a) || // A-Z
			    (temp >= 0x61 && temp <= 0x7a) || // a-z
			     temp == 0x2e || temp == 0x2d ||  // . and -
			     temp == 0x2a || temp == 0x5f) {  // * and _
				ret.append((char) temp);
			}
			else if (temp == 0x20) {
				ret.append('+');
			}
			else {
				ret.append('%');
				if (temp < 16) ret.append('0');
				ret.append(Integer.toHexString(temp));
			}
		}

		return ret.toString();
	}

	public static boolean isChannel(String chan) {
		char temp = ' ';
		if (chan.length() > 0) temp = chan.charAt(0);
		if (temp == '#' || temp == '&' || temp == '!') return true;
		else return false;
	}

	public static String[] mergeStringArray(String[] inp1, String[] inp2) {
		String[] ret = new String[inp1.length + inp2.length];

		System.arraycopy(inp1, 0, ret, 0, inp1.length);
		System.arraycopy(inp2, 0, ret, inp1.length, inp2.length);
		return ret;
	}

	public static String formatDateMillis(long millis) {
		String weekdays = "SunMonTueWedThuFriSat";
		String months = "JanFebMarAprMayJunJulAugSepOctNovDec";
		Calendar cal = Calendar.getInstance();

		cal.setTime(new Date(millis));
		return weekdays.substring((cal.get(Calendar.DAY_OF_WEEK)-1)*3).substring(0, 3) + " " + 
		       months.substring(cal.get(Calendar.MONTH)*3).substring(0, 3) + " " + 
		       cal.get(Calendar.DATE) + " " +
		       cal.get(Calendar.HOUR_OF_DAY) + ":" + 
		       (cal.get(Calendar.MINUTE)<10?"0":"") + cal.get(Calendar.MINUTE) + " " +
		       cal.get(Calendar.YEAR);
	}

	public static String trim(String input) {
		int idx1, idx2;

		for (idx1=0; idx1<=input.length() && input.charAt(idx1)==' '; idx1++);
		for (idx2=input.length()-1; idx2>idx1 && input.charAt(idx2)==' '; idx2--);

		return input.substring(idx1, idx2+1);
	}
}
