/* Copyright (C) 2010-2011, Mamadou Diop.
 *  Copyright (C) 2011, Doubango Telecom.
 *
 * Contact: Mamadou Diop <diopmamadou(at)doubango(dot)org>
 *	
 * This file is part of imsdroid Project (http://code.google.com/p/imsdroid)
 *
 * imsdroid is free software: you can redistribute it and/or modify it under the terms of 
 * the GNU General Public License as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *	
 * imsdroid is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU General Public License for more details.
 *	
 * You should have received a copy of the GNU General Public License along 
 * with this program; if not, write to the Free Software Foundation, Inc., 
 * 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package com.yiqiding.ktvbox.libutils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.content.Context;
import android.text.format.DateUtils;

public class DateTimeUtils {
//	static final DateFormat sDefaultDateFormat = new SimpleDateFormat(
//			"yyyy-MM-dd HH:mm:ss");

	public static String now(String dateFormat) {
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
		return sdf.format(cal.getTime());
	}

	public static String now() {
//		Calendar cal = Calendar.getInstance();
//		return sDefaultDateFormat.format(cal.getTime());
		DateFormat sDefaultDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return sDefaultDateFormat.format(new Date());
	}
	
	/**
	 * 板子重启会默认1970年 
	 * @return
	 */
	public static String now(long milltimes) {
//		Calendar cal = Calendar.getInstance();
//		cal.setTimeInMillis(milltimes);
//		return sDefaultDateFormat.format(cal.getTime());
		DateFormat sDefaultDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return sDefaultDateFormat.format(new Date(milltimes));
	}
	
	/**
	 * 同一天的不显示日期，其他的显示日期
	 */
	public static String format(long todayTime, long millTimes) {
		Calendar c = Calendar.getInstance();
		
		c.setTimeInMillis(todayTime);
		int tYear = c.get(Calendar.YEAR);
		int tDay = c.get(Calendar.DAY_OF_MONTH);
		int tMon = c.get(Calendar.MONTH);
		
		c.setTimeInMillis(millTimes);
		int year = c.get(Calendar.YEAR);
		int day = c.get(Calendar.DAY_OF_MONTH);
		int mon = c.get(Calendar.MONTH);
		
		String h = (c.get(Calendar.HOUR_OF_DAY) < 10 ? "0" : "") + c.get(Calendar.HOUR_OF_DAY);
		String min = (c.get(Calendar.MINUTE) < 10 ? "0" : "") + c.get(Calendar.MINUTE);
		String m = (c.get(Calendar.MONTH) < 9 ? "0" : "") + (c.get(Calendar.MONTH) + 1);
		String d = (day < 10 ? "0" : "") + day;
		
		if (year == tYear && tMon == mon && day == tDay) {
			return h + ":" + min;
		}
		return ((year != tYear) ? (year + "-") : "") + m + "-" + d + " " + h + ":" + min;
	}

	public static String formatTime(String time) {
		int flags = DateUtils.FORMAT_ABBREV_RELATIVE;
		CharSequence cs = DateUtils.getRelativeTimeSpanString(
				Long.parseLong(time), System.currentTimeMillis(),
				DateUtils.MINUTE_IN_MILLIS, flags);
		return cs.toString();
	}

	public static Date parseDate(String date, DateFormat format) {
		if (!"".equals(date) && date != null) {
			try {
				DateFormat sDefaultDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				return format == null ? sDefaultDateFormat.parse(date) : format
						.parse(date);
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
		return new Date();
	}

	public static Date parseDate(String date) {
		return parseDate(date, null);
	}

	public static boolean isSameDay(Date d1, Date d2) {
		return d1.getDay() == d2.getDay() && d1.getMonth() == d2.getMonth()
				&& d1.getYear() == d2.getYear();
	}

	public static String formatDateRange(Context mContext, String startTime,
			String endTime) {
		return DateUtils.formatDateRange(mContext, Long.parseLong(startTime),
				Long.parseLong(endTime), DateUtils.FORMAT_SHOW_TIME
						| DateUtils.FORMAT_SHOW_DATE
						| DateUtils.FORMAT_SHOW_WEEKDAY
						| DateUtils.FORMAT_SHOW_YEAR);
	}

	public static String formatDatetime(Context mContext, String startTime) {
		return DateUtils.formatDateTime(mContext, Long.parseLong(startTime),
				DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE
						| DateUtils.FORMAT_SHOW_YEAR);
	}


	public static int compareDate(String d1,String d2){
		System.out.println("dt1="+d1+",dt2="+d2);
		DateFormat sDefaultDateFormat = new SimpleDateFormat("yyMMdd");
		Date date1 = parseDate(d1,sDefaultDateFormat);
		Date date2 = parseDate(d2,sDefaultDateFormat);
		if (date1.getTime() > date2.getTime()) {
			System.out.println("dt1 在dt2前");
			return 1;
		} else if (date1.getTime() < date2.getTime()) {
			System.out.println("dt1在dt2后");
			return -1;
		} else {//相等
			System.out.println("dt1=dt2");
			return 0;
		}
	}
}
