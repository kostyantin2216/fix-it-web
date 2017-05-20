/**
 * 
 */
package com.fixit.web.forms.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;

/**
 * @author 		Kostyantin
 * @createdAt 	2017/05/06 16:42:14 GMT+3
 */
public class TelephoneConstraintValidator implements ConstraintValidator<Telephone, String> {

	@Override
	public void initialize(Telephone telephone) {
		
	}

	@Override
	public boolean isValid(String input, ConstraintValidatorContext context) {
		if(input == null) {
			return false;
		}
		PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
		PhoneNumber number;
		try {
			number = phoneUtil.parseAndKeepRawInput(input, "");
		} catch (NumberParseException e) {
			return false;
		}
		return phoneUtil.isValidNumber(number);
	}
	
	

}
