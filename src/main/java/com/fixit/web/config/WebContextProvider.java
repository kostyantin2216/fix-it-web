/**
 * 
 */
package com.fixit.web.config;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * @author 		Kostyantin
 * @createdAt 	2017/05/09 22:27:32 GMT+3
 */
@Component
public class WebContextProvider implements ApplicationContextAware {

	private static ApplicationContext context;

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		context = applicationContext;
	}
	
	public static MessagesProperties getMessagesProperties() {
		return context.getBean(MessagesProperties.class);
	}
	
}
