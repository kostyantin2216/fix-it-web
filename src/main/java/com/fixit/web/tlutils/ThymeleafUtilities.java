/**
 * 
 */
package com.fixit.web.tlutils;

/**
 * @author 		Kostyantin
 * @createdAt 	2017/05/09 22:16:06 GMT+3
 */
public class ThymeleafUtilities {
	
	private final static TLCalendarUtils mCalendarUtils = new TLCalendarUtils();

	public static TLCalendarUtils getCalendarUtils() {
		return mCalendarUtils;
	}
	
}
