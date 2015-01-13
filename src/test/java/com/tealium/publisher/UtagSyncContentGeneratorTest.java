package com.tealium.publisher;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.*;

import static org.junit.Assert.*;

import com.google.inject.*;
import com.google.inject.name.Names;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import com.tealium.publisher.utag.sync.ExtContentGenerator;
import com.tealium.publisher.utag.sync.ExtContentGeneratorFactory;

public class UtagSyncContentGeneratorTest {

	private static final Map<String, String> PRELOADED_DATA = new HashMap<String, String>();
	private static final String MAIN_PROFILE_HYATT_ORIG_JSON = "main.profile.hyatt.orig.json";
	private static final String MAIN_PROFILE_HYATT_PRETTY_JSON = "main.profile.hyatt.pretty.json";
	private FileContentGenerator fileContentGenerator;
	private DBObject profile;
	private DBObject config;

	@Before
	public void before() throws IOException {

		Injector injector = Guice.createInjector(new AbstractModule() {
			@Override
			protected void configure() {

				bind(ExtContentGeneratorFactory.class).toInstance(
						new ExtContentGeneratorFactory());
				bind(String.class)
						.annotatedWith(
								Names.named(AbstractFileContentGeneratorImpl.GENERATED_SYNC_FILE_PATH))
						.toInstance(
								System.getProperty("user.home") + "/tmp/aaa.js");

				bind(FileContentGenerator.class).to(
						UtagSyncContentGeneratorImpl.class);
			}

		});
		fileContentGenerator = injector.getInstance(FileContentGenerator.class);
		assertNotNull(fileContentGenerator);

		profile = getObject(readJson(MAIN_PROFILE_HYATT_PRETTY_JSON));
		config = getObject(readJson(AbstractFileContentGeneratorImpl.CONFIG_PUBLISH));
		assertNotNull(profile);
		assertNotNull(config);
		PRELOADED_DATA.put("multiScopeLoad", "sync");
		PRELOADED_DATA.put("status", "active");
	}

	@Test
	public void testParseJsonFile() throws IOException {
		DBObject object = config;
		assertNotNull(object);
		assertNotNull("should have " + object.get("template_dir"),
				object.get("template_dir").toString().contains("stratocaster"));

		object = profile;
		System.out.println(object);
		assertNotNull(object);
		assertNotNull("should be " + object.get("account"),
				object.get("account").toString().contains("hyatt"));
		assertNotNull(
				"should be "
						+ ((DBObject) object.get("settings")).get("account"),
				((DBObject) object.get("settings")).get("account").toString()
						.contains("hyatt"));
		assertNotNull(
				"should be "
						+ ((DBObject) ((DBObject) object.get("manage"))
								.get("50")).get("tag_name"),
				((DBObject) ((DBObject) object.get("manage")).get("50"))
						.get("tag_name").toString()
						.contains("Affiliate Window"));
		assertNotNull(
				"should be "
						+ ((DBObject) object.get("publish")).get("profile"),
				((DBObject) object.get("publish")).get("profile").toString()
						.contains("main"));
	}

	@Test
	public void testUtagSyncContent100001() throws IOException {
		DBObject id100001 = (DBObject) ((DBObject) profile
				.get(AbstractFileContentGeneratorImpl.SUBOBJECT_CUSTOMIZATIONS))
				.get("100001");
		id100001.put("all", "no");
		String content = fileContentGenerator.generateContent(profile, config);
		assertNotNull(content);
		assertTrue("should be " + content, content.contains("_awin_iata"));

		id100001 = (DBObject) ((DBObject) profile
				.get(AbstractFileContentGeneratorImpl.SUBOBJECT_CUSTOMIZATIONS))
				.get("100001");
		id100001.put("all", "yes");

		content = fileContentGenerator.generateContent(profile, config);
		assertNotNull(content);
		assertTrue("should be " + content, content.contains("GV(b)"));
	}

	@Test
	public void testUtagSyncContent100002() throws IOException {
		addId100002Data(profile);

		String content = fileContentGenerator.generateContent(profile, config);
		assertNotNull(content);
		assertTrue("should be " + content,
				content.contains("urchase_tax_amount"));
	}

	@Test
	public void testUtagSyncContent100003() throws IOException {
		addId100003Data(profile);

		String content = fileContentGenerator.generateContent(profile, config);
		assertNotNull(content);
		assertTrue("should be " + content,
				content.contains("b.number_of_rooms"));
	}

	@Test
	public void testUtagSyncContent100004() throws IOException {
		addId100004Data(profile);

		String content = fileContentGenerator.generateContent(profile, config);
		assertNotNull(content);
		assertTrue("should be " + content,
				content.contains("cp.src"));
	}

	public static String readJson(String name) throws IOException {
		Path path = Paths.get(UtagSyncContentGeneratorTest.class
				.getClassLoader().getResource(name).getPath());
		List<String> allLines = Files.readAllLines(path,
				Charset.defaultCharset());
		StringBuilder sb = new StringBuilder();
		for (String line : allLines) {
			sb.append(line).append("\n");
		}
		return sb.toString();
	}

	public static DBObject getObject(String json) {
		return (DBObject) JSON.parse(json);
	}

	private static void addId100002Data(DBObject profile) {
		DBObject customizations = (DBObject) profile
				.get(AbstractFileContentGeneratorImpl.SUBOBJECT_CUSTOMIZATIONS);
		Set<String> keySet = customizations.keySet();
		DBObject obj100002 = null;
		String id100002 = "100002";
		for (String key : keySet) {
			if (customizations.get(key).toString().contains("_source")) {
				String json100002 = customizations.get(key).toString()
						.replace(key, id100002);
				obj100002 = getObject(json100002);
				break;
			}
		}

		pushDataObj(customizations, obj100002, id100002);
	}

	private static void addId100003Data(DBObject profile) {
		DBObject customizations = (DBObject) profile
				.get(AbstractFileContentGeneratorImpl.SUBOBJECT_CUSTOMIZATIONS);
		Set<String> keySet = customizations.keySet();
		DBObject obj100003 = null;
		String id100003 = "100003";
		for (String key : keySet) {
			Object object = customizations.get(key) == null ? ""
					: customizations.get(key);
			if (object.toString().contains("_source")
					&& object.toString().contains("_setoption")) {
				String json100003 = object.toString().replace(key, id100003);
				obj100003 = getObject(json100003);
				break;
			}
		}

		pushDataObj(customizations, obj100003, id100003);
	}
	private static void addId100004Data(DBObject profile) {
		DBObject customizations = (DBObject) profile
				.get(AbstractFileContentGeneratorImpl.SUBOBJECT_CUSTOMIZATIONS);
		Set<String> keySet = customizations.keySet();
		DBObject obj100004 = null;
		String id100004 = "100004";
		for (String key : keySet) {
			Object object = customizations.get(key) == null ? ""
					: customizations.get(key);
			if (object.toString().contains(ExtContentGenerator.ELEMENT_ALLOWUPDATE)
					&& object.toString().contains("once")) {
				String json100004 = object.toString().replace(key, id100004);
				obj100004 = getObject(json100004);
				break;
			}
		}

		pushDataObj(customizations, obj100004, id100004);
	}

	private static void pushDataObj(DBObject customizations, DBObject data,
			String id) {
		if (data != null) {
			data.put("id", id);
			data.putAll(PRELOADED_DATA);
			customizations.put(id, data);
		} else {
			throw new RuntimeException("No data created!");
		}
	}
}
