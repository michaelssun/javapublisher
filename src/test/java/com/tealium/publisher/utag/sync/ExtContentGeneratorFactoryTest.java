package com.tealium.publisher.utag.sync;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Before;
import org.junit.Test;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;

public class ExtContentGeneratorFactoryTest {
	private ExtContentGeneratorFactory factory;

	@Before
	public void before() {
		Injector injector = Guice.createInjector(new AbstractModule() {
			@Override
			protected void configure() {
				bind(ExtContentGeneratorFactory.class).toInstance(new ExtContentGeneratorFactory()); 
			}
		});
		factory=injector.getInstance(ExtContentGeneratorFactory.class);
	}
	
	@Test
	public void testInstance(){
		assertNotNull(factory.getExtContentGenerator("100001"));
		assertNull(factory.getExtContentGenerator("100004"));
		assertNull(factory.getExtContentGenerator("100005"));
	}	 
}
