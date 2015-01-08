package com.tealium.publisher;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;

import com.google.inject.Inject;
import com.mongodb.DBObject;

public abstract class AbstractFileContentGeneratorImpl implements
		FileContentGenerator {
	
	protected static final  Format DATE_FORMATTER=new SimpleDateFormat("yyyymmddHHMM");
	protected static final String UTVERSION = "##UTVERSION##";
	protected static final String UTYEAR = "##UTYEAR##";
	protected static final String UTSYNC = "##UTSYNC##";
	
	@Inject(optional = true)
	protected Charset encoding = StandardCharsets.UTF_8;

	@Override
	public boolean writeToFile(String path, String data) throws IOException {
		Path path2 = Paths.get(path);
		Files.write(path2, Arrays.asList(new String[] { data }), encoding);
		return true;
	}

	@Override
	public String readTemplateContent(String templatePath) throws IOException {
		Path path = Paths.get(templatePath);
		List<String> allLines = Files.readAllLines(path, encoding);
		StringBuilder sb = new StringBuilder();
		for (String line : allLines) {
			sb.append(line).append("\n");
		}
		return sb.toString();
	} 

	@Override
	public String generateContent() throws IOException {
		
		return generateContent(null, null);
	}
	

	@Override
	public String generateContent(DBObject profile, DBObject queryParams)
			throws IOException {
		return generateContent(profile, queryParams, null);
	}
	
}
