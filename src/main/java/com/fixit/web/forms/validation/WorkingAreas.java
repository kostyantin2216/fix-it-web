/**
 * 
 */
package com.fixit.web.forms.validation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

/**
 * @author 		Kostyantin
 * @createdAt 	2017/05/08 01:30:03 GMT+3
 */
@Documented
@Constraint(validatedBy = TelephoneConstraintValidator.class)
@Retention(RUNTIME)
@Target({ FIELD, METHOD })
public @interface WorkingAreas {
	String message() default "Invalid Working Areas";
    
    Class<?>[] groups() default {};
     
    Class<? extends Payload>[] payload() default {};
}
