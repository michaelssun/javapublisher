package com.tealium.publisher.annotation;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;

import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Target;

import com.google.inject.ScopeAnnotation;

@Target({ TYPE, METHOD })
@Retention(RUNTIME)
@ScopeAnnotation
public @interface ThreadScope {
	String[] tags() default "";

	String createdBy() default "Tealium Engineering";

	String lastModified() default "12/21/2014";
}
