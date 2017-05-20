/**
 * 
 */
package com.fixit.web.forms.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * @author 		Kostyantin
 * @createdAt 	2017/05/08 01:30:29 GMT+3
 */
public class WorkingAreasConstraintValidator implements ConstraintValidator<WorkingAreas, String> {

	@Override
	public void initialize(WorkingAreas constraintAnnotation) {	}

	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {
		// TODO Auto-generated method stub
		return false;
	}

}
