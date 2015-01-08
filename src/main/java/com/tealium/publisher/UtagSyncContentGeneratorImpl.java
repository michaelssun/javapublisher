package com.tealium.publisher;

import java.io.IOException;
import java.util.Calendar;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mongodb.DBObject;

public class UtagSyncContentGeneratorImpl extends
		AbstractFileContentGeneratorImpl {

	private static final String UTAG_SYNC_DEFAULT = "//tealium universal tag - utag.sync ut4.0.$date, Copyright $year Tealium.com Inc. All Rights Reserved.\n";

	@Inject(optional = true)
	@Named("generatedSyncFilePath")
	private String generatedSyncFilePath;

	@Override
	public String generateContent(DBObject profile, DBObject queryParams,DBObject config)
			throws IOException {
		Preconditions.checkArgument(profile == null, "Empty profile!");
		String templateContent = this
				.readTemplateContent(getTemplateLocation());

		String date = DATE_FORMATTER.format(Calendar.getInstance().getTime());
		int year = Calendar.getInstance().get(Calendar.YEAR);

		templateContent = templateContent.replace(UTVERSION, date);
		templateContent = templateContent.replace(UTSYNC,
				getUtagSyncForAccount(profile, queryParams, config));
		templateContent = templateContent.replace(UTYEAR, String.valueOf(year));

		this.writeToFile(generatedSyncFilePath, templateContent);

		return UTAG_SYNC_DEFAULT + templateContent;
	}

	@Override
	public String getTemplateLocation() {
		// TODO Auto-generated method stub
		return null;
	}

	private String getUtagSyncForAccount(DBObject profile, DBObject queryParams,DBObject config) {
		throw new RuntimeException("to be implemented");
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