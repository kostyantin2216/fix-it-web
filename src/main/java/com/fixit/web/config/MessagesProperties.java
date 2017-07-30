/**
 * 
 */
package com.fixit.web.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

/**
 * @author 		Kostyantin
 * @createdAt 	2017/05/08 00:41:29 GMT+3
 */
@Component
public class MessagesProperties {
	
	public final static String ALREADY_REGISTERED = "aready.registered";
	
	public static final String ERROR_UNEXPECTED = "error.unexpected";

	public final static String LEAD_ID_EMPTY = "lead.id.empty";
	public final static String LEAD_ID_INVALID = "lead.id.invalid";
	
	public final static String LOGO_EMPTY = "validation.logo.empty";
	public final static String FEATURE_IMAGE_EMPTY = "validation.feature.image.empty";
	
	public final static String TRADESMAN_REGISTRATION_SUCCESS = "tradesman.registration.success";
	
	private final MessageSource msgs;
	
	@Autowired
	public MessagesProperties(MessageSource messageSource) {
		this.msgs = messageSource;
	}
	
	public String getMsg(String code) {
		return msgs.getMessage(code, null, LocaleContextHolder.getLocale());
	}
}
