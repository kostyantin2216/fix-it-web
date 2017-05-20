/**
 * 
 */
package com.fixit.web.tlutils;

import com.fixit.web.config.MessagesProperties;
import com.fixit.web.config.WebContextProvider;

/**
 * @author 		Kostyantin
 * @createdAt 	2017/05/09 22:16:43 GMT+3
 */
public class TLCalendarUtils {
	
	private final static String[] namesOfWeekDays = new String[7];
	
	{
		MessagesProperties messagesProperties = WebContextProvider.getMessagesProperties();
		namesOfWeekDays[0] = messagesProperties.getMsg("sunday");
		namesOfWeekDays[1] = messagesProperties.getMsg("monday");
		namesOfWeekDays[2] = messagesProperties.getMsg("tuesday");
		namesOfWeekDays[3] = messagesProperties.getMsg("wednesday");
		namesOfWeekDays[4] = messagesProperties.getMsg("thursday");
		namesOfWeekDays[5] = messagesProperties.getMsg("friday");
		namesOfWeekDays[6] = messagesProperties.getMsg("saturday");
 	}
	
	public String getNameOfWeekDay(int day) {
		int index = day - 1;
		if(index >= 0 && index <= namesOfWeekDays.length) {
			return namesOfWeekDays[day - 1];
		}
		return "";
	}

}
