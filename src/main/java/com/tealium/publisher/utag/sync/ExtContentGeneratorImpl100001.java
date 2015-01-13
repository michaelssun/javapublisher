package com.tealium.publisher.utag.sync;

import com.mongodb.DBObject;

public class ExtContentGeneratorImpl100001 implements ExtContentGenerator {

	private static final String EXT_TEMPLATE_YES = "function(a,b,c){for(c in utag.loader.GV(b)){try{b[c] = (b[c] instanceof Array || b[c] instanceof Object) ? b[c] : b[c].toString().toLowerCase()}catch(e){}}}";
	private static final String EXT_TEMPLATE_NO = "function(a,b,c,d){c=['_var_'];for(d=0;d<c.length;d++){try{b[c[d]] = (b[c[d]] instanceof Array "
			+ "|| b[c[d]] instanceof Object) ? b[c[d]] : b[c[d]].toString().toLowerCase()}catch(e){}}}";


	@Override
	public String generateExtensionContent(DBObject data, DBObject config, DBObject profile) {
		
		
		StringBuilder sb = new StringBuilder();
		if (VALUE_YES.equalsIgnoreCase((String) data.get(VALUE_ALL))) {
			sb.append(EXT_TEMPLATE_YES);
		} else if (VALUE_NO.equalsIgnoreCase((String) data.get(VALUE_ALL))) {
			StringBuilder values = new StringBuilder();
			// my @varArr;
			// for my $key(keys %$data){
			// if($key =~ /^\d+_set$/){
			// my $var = $data->{$key};
			// $var =~ s/^js\.//;
			// push @varArr, $var;
			// }
			// }
			// my $varSize = @varArr;
			// if($varSize > 0){
			for (String key : data.keySet()) {
				if (KEY_PATTERN_ONE_DIGITAL_SET.matcher(key).find()) {
					if (values.length() > 0) {
						values.append(",");
					}
					values.append(data.get(key).toString()
							.replaceAll(REMOVE_PATTERN_JS, ""));
				}
			}
			if (values.length() > 0) {
				sb.append(EXT_TEMPLATE_NO.replace("_var_", values));
			}

		}
		return sb.toString();
	}

}
