package com.tealium.publisher;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mongodb.DBObject;
import com.tealium.publisher.utag.sync.ExtContentGeneratorFactory;
import com.tealium.publisher.util.UtagSyncUtil;

public class UtagSyncContentGeneratorImpl extends
		AbstractFileContentGeneratorImpl {
	private static Logger logger = LoggerFactory
			.getLogger(UtagSyncContentGeneratorImpl.class);


	@Inject(optional = true)
	@Named(GENERATED_SYNC_FILE_PATH)
	private String generatedSyncFilePath;
	
	@Inject
	ExtContentGeneratorFactory extContentGeneratorFactory;

	@Override
	public String generateContent(DBObject profile, DBObject queryParams,DBObject config)
			throws IOException {
		Preconditions.checkArgument(profile != null, "Empty profile!");
		String templateContent = this
				.readTemplateContent(getTemplateLocation());

		String date = DATE_FORMATTER.format(Calendar.getInstance().getTime());
		int year = Calendar.getInstance().get(Calendar.YEAR);

		templateContent = templateContent.replace(UTVERSION, date);
		templateContent = templateContent.replace(UTSYNC,
				generateSyncedExtensions(profile, queryParams, config));
		templateContent = templateContent.replace(UTYEAR, String.valueOf(year));

		this.writeToFile(generatedSyncFilePath, templateContent);

		return  templateContent;
	}

	@Override
	public String getTemplateLocation() {
		return this.getClass().getClassLoader().getResource("utag.sync.js").getPath(); 
	}

	@SuppressWarnings("unchecked")
	private String generateSyncedExtensions(DBObject profile, DBObject queryParams,DBObject config) {
		DBObject extensionData=(DBObject) profile.get(SUBOBJECT_CUSTOMIZATIONS);
		StringBuilder sb=new StringBuilder();
		 
		for (String key : UtagSyncUtil.getAlphabeticSortedKeys(extensionData.keySet())) {
			DBObject customization = (DBObject)extensionData.get(key);
			Object status=  customization.get("status");
			Object multiScopeLoad=customization.get("multiScopeLoad");
			
			if (status!=null&&"active".equals(status)&&multiScopeLoad!=null&&multiScopeLoad.toString().indexOf("sync")>=0) {
				String id=(String) customization.get("id"); 
				if (!Strings.isNullOrEmpty(id)) {
					String ext=extContentGeneratorFactory.getExtContentGenerator(id).generateExtensionContent((DBObject) extensionData.get(key), config, profile);
					if (!Strings.isNullOrEmpty(ext)) {
						sb.append("\n").append(ext);
					}else{
						if (logger.isDebugEnabled()) {
							logger.debug("no extension content for id: "+id);
						}
					}
				}else{
					logger.error("null or empty ID for profile "+profile.get("_id"));
				}
				
			}
		}
		
		return sb.toString();
	}

}


/*
$log->debug("Sync file content : ["+$syncContent+"]");

my $syncOutfile = "$data->{config}->{revision_publish_dir}/utag.sync.js";
my $template = getTemplate($data, $profileData, "utag.sync.js", "utag.sync.js");
my @content;

if(open IN, $template){
        @content = <IN>;
        close IN;

        for(my $i=0; $i<@content; $i++){
                if($content[$i] =~ /##UTVERSION##/){
                        my $date = Tealium::UTUI::util::getDateId();
                        my $year = substr($date,0,4);
                        $content[$i] =~ s/##UTYEAR##/$year/;
                        $content[$i] =~ s/##UTVERSION##/$date/;

                }elsif($content[$i] =~ /##UTSYNC##/){
                        $content[$i] =~ s/##UTSYNC##/$syncContent/;
                }

        }
}

# Write out the utag.sync.js
if(open OUT, ">$syncOutfile"){
    if(defined $profileData->{publish}->{minify} && $profileData->{publish}->{minify} eq "yes"){
        eval {
            my $minifiedContent = "//tealium universal tag - utag.sync ut4.0.$date, Copyright $year Tealium.com Inc. All Rights Reserved.\n";
            $minifiedContent .= minify( input => (join "", @content) );
            print OUT $minifiedContent;
        };
        if ($@){
            $minifyFail = "utag.sync.js";
        }
    }else{
        print OUT (join "", @content);
    }
    close OUT;
    push @returnFiles, "utag.sync.js";
}else{
    $log->warn("createLoader: cant write outfile: $syncOutfile");
}
*/