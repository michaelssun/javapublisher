package com.tealium.publisher.utag.sync;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ExtContentGeneratorFactory {
	private static final String UTAG_SYNC_PACKAGE = "com.tealium.publisher.utag.sync.ExtContentGeneratorImpl";
	private static final Map<String, ExtContentGenerator> ExtContentGeneratorMap = new ConcurrentHashMap<String, ExtContentGenerator>();
	private static final int ID_UPPER = 4;// 35;

	static {
		for (int id = 1; id < ID_UPPER + 1; id++) {
			ExtContentGenerator instance;
			String className = UTAG_SYNC_PACKAGE
					+ ExtContentGenerator.SYNC_ID_PREFIX + id;
			try {
				instance = (ExtContentGenerator) Class.forName(className)
						.newInstance();
			} catch (InstantiationException | IllegalAccessException
					| ClassNotFoundException e1) {
				throw new RuntimeException(e1);
			}

			ExtContentGeneratorMap.put(ExtContentGenerator.SYNC_ID_PREFIX + id,
					instance);
		}
	}

	public ExtContentGeneratorFactory() {

	}

	public ExtContentGenerator getExtContentGenerator(String id) {
		return ExtContentGeneratorMap.get(id);
	}

}
