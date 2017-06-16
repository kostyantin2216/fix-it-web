/**
 * 
 */
package com.fixit.web.tlutils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fixit.web.config.MessagesProperties;

/**
 * @author 		Kostyantin
 * @createdAt 	2017/05/09 22:16:06 GMT+3
 */
@Component
public class ThymeleafUtilities {
	
	private final TLCalendarUtils mCalendarUtils;

	@Autowired
	public ThymeleafUtilities(MessagesProperties messagesProperties) {
		mCalendarUtils = new TLCalendarUtils(messagesProperties);
	}
	
	public TLCalendarUtils getCalendarUtils() {
		return mCalendarUtils;
	}
	
}
