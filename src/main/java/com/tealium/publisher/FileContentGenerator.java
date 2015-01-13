package com.tealium.publisher;

import java.io.IOException;

import com.mongodb.DBObject;

public interface FileContentGenerator {
	public String readTemplateContent(String templatePath) throws IOException;	
	
	public String generateContent() throws IOException;	 
	
	public boolean writeToFile(String path, String data) throws IOException;
	
	public String getTemplateLocation();

	String generateContent(DBObject profile, DBObject config)
			throws IOException;

	String generateContent(DBObject profile, DBObject queryParams,
			DBObject config) throws IOException;
}
