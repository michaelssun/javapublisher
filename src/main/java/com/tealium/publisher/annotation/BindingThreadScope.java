package com.tealium.publisher.annotation;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.google.inject.BindingAnnotation;

@Target({ TYPE })
@Retention(RUNTIME)
@BindingAnnotation
public @interface BindingThreadScope {
	String[] tags() default "";

	String createdBy() default "Tealium Engineering";

	String lastModified() default "12/21/2014";
}
