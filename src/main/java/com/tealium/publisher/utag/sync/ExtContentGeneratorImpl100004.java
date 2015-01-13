package com.tealium.publisher.utag.sync;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.mongodb.DBObject;
import com.tealium.publisher.util.UtagSyncUtil;

/**
 * Extension content generator for id = 100004
 *
 */
public class ExtContentGeneratorImpl100004 implements ExtContentGenerator { 
	public static final String PATTERN_UTAG_MAIN_CHARS = "utag_main_";

	private static final Pattern PATTERN_UTAG_MAIN = Pattern.compile("^"+PATTERN_UTAG_MAIN_CHARS);

	public static final String REGEX_START_CP = "^cp\\.";

	public static final Pattern PATTERN_START_CP = Pattern.compile(REGEX_START_CP);

	private static Logger logger = LoggerFactory
			.getLogger(ExtContentGeneratorImpl100004.class);

	public static final Pattern PATTERN_SOURCE_100004=Pattern.compile("^qp\\.|^cp\\.|^dom\\.|^meta\\.|^location\\.|^document\\.|^js_page\\.|^va\\.");
	public static final Pattern PATTERN_SOURCE_100004_REPLACE=Pattern.compile("^\\w+\\.");

	@SuppressWarnings("rawtypes")
	@Override
	public String generateExtensionContent(DBObject customizedExtensionData,
			DBObject config, DBObject profile) {
		Preconditions.checkNotNull(config, "No configuration was defined!");

		if (customizedExtensionData == null) {
			return null;
		}
		StringBuilder sb = new StringBuilder();

		String source = (String) customizedExtensionData.get(ELEMENT_SOURCE);

		if (!Strings.isNullOrEmpty(source)
				&& PATTERN_SOURCE_100004.matcher(source).find()) {
			source = source.replaceFirst(
					PATTERN_SOURCE_100004_REPLACE.toString(), "");
			customizedExtensionData.put(ELEMENT_SOURCE, source);
		}

		String code = "";
		String value  = getValue(customizedExtensionData, profile);
		
		String setToVar=(String) customizedExtensionData.get(ELEMENT_VAR);
		setToVar=setToVar!=null?(PATTERN_START_CP.matcher(setToVar).find()?setToVar.replaceFirst(REGEX_START_CP, ""):setToVar):"";
		String cookieVal=value!=null?value:"";
		String cookieExpire="Thu, 31 Dec 2099 00:00:00 GMT";
		String persistence=(String) customizedExtensionData.get(ELEMENT_PERSISTENCE);
		if (!Strings.isNullOrEmpty(persistence)) {
			switch (persistence) {
			case "visitor": 
				break; 
			case "session":
				cookieVal += "+';exp-session'";
				cookieExpire = "";
				break;
			case "hours":
				cookieVal += "+';exp-"
						+ customizedExtensionData.get(ELEMENT_PERSISTENCETEXT)
						+ "h'";
				cookieExpire = "\"+(function(){var d=new Date();d.setTime(d.getTime()+("
						+ customizedExtensionData.get(ELEMENT_PERSISTENCETEXT)
						+ "*3600000)); return d.toGMTString()})()+\"";
				break;
			case "days":
				 cookieVal += "+';exp-"+customizedExtensionData.get(ELEMENT_PERSISTENCETEXT)+"d'";
                 cookieExpire = "\"+(function(){var d=new Date();d.setTime(d.getTime()+("+customizedExtensionData.get(ELEMENT_PERSISTENCETEXT)+"*86400000)); return d.toGMTString()})()+\"";         
				break;
			default:
				logger.error("invalid persistence value: " + persistence
						+ " for customizations( "
						+ customizedExtensionData.get(ELEMENT_MONGO_ID)
						+ "|profile(" + profile.get(ELEMENT_MONGO_ID) + ")");
				break;
			}

		}
		
		//set cookie
		if (PATTERN_UTAG_MAIN.matcher(setToVar).find()) {
			code = new StringBuilder().append(code)
					.append("utag.loader.SC('utag_main',{'")
					.append(setToVar.replaceFirst(PATTERN_UTAG_MAIN_CHARS, ""))
					.append("':").append(cookieVal).append("});b['cp.")
					.append(setToVar).append("']=").append(value).append(";")
					.toString();
		} else {
			code = new StringBuilder().append(code).append("document.cookie=\"")
					.append(setToVar).append("=\"+").append(value)
					.append("+\";path=/;domain=\"+utag.cfg.domain+\";expires=")
					.append(cookieExpire).append("\"").append(";b['cp.")
					.append(setToVar).append("']=").append(value).append(";")
					.toString();
		}
		
		//ALLOW Update
		String allowupdate = (String) customizedExtensionData.get(ELEMENT_ALLOWUPDATE);
		if (!Strings.isNullOrEmpty(  allowupdate)&&allowupdate.equals("once")) {
			if (PATTERN_UTAG_MAIN.matcher(setToVar).find()) {
				code = new StringBuilder()
						.append("if(typeof b['cp.utag_main_")
						.append(setToVar.replaceFirst(PATTERN_UTAG_MAIN_CHARS, ""))
						.append("']=='undefined'){").append(code).append("}") 
						.toString();
			} else {
				code = new StringBuilder().append("if(typeof b[\'cp.")
						.append(setToVar).append("']=='undefined'){").append(code).append("}")
						.toString();
			}
		}
		
		Map<String, Map<String, Map<String, String>>> condition = UtagSyncUtil
				.getCondition(customizedExtensionData);
		Collection<String> orConditionList = UtagSyncUtil
				.getOrCondition(condition);

		sb.append("function(a,b){" );
		if (orConditionList.size() > 0) {
			sb.append("if(").append(StringUtils.join(orConditionList, "||"))
					.append("){").append(code).append("}");
		} else {
			String filter = (String) customizedExtensionData
					.get(ELEMENT_FILTER);
			String filtertype = (String) customizedExtensionData
					.get(ELEMENT_FILTER_TYPE);
			if (!Strings.isNullOrEmpty(filter)
					&& !Strings.isNullOrEmpty(filtertype)) {
				String conditionFilter = UtagSyncUtil.getConditionFilter("b['"
								+ customizedExtensionData.get(ELEMENT_SOURCE)
								+ "']", filtertype,
								filter);
				sb.append("if(")
						.append(conditionFilter).append("){")
						.append(code).append("}");
			}
		}
		
		sb.append("}");
		return sb.toString();
	}

	private String getValue(DBObject customizedExtensionData, DBObject profile ) {
		String value=null;
		String setoption = (String) customizedExtensionData
				.get(ELEMENT_SETOPTION);
		if (!Strings.isNullOrEmpty(setoption) && setoption.equals(VALUE_TEXT)) {
			value = new StringBuilder().append("'")
					.append(customizedExtensionData.get(ELEMENT_SETTOTEXT))
					.append("'").toString();
		} else {
			if (customizedExtensionData.get(ELEMENT_SETTOVAR) != null) {
				String settovar = (String) customizedExtensionData
						.get(ELEMENT_SETTOVAR);
				value = "b['"
						+ (JS_PATTERN.matcher(settovar).find() ? settovar
								.replaceFirst("js.", "") : settovar) + "']";
			} else {
				logger.error(ELEMENT_FILTER
						+ " - not defined in customizations( "
						+ customizedExtensionData.get(ELEMENT_MONGO_ID)
						+ "|profile(" + profile.get(ELEMENT_MONGO_ID) + ")");
			}
		}
		return value;
	}
	
	
}


//}elsif($id eq '100004'){
//    $extension = "";
//    if($data->{source} !~ /^qp\.|^cp\.|^dom\.|^meta\.|^location\.|^document\.|^js_page\.|^va\./){
//        $data->{source} =~ s/^\w+\.//;
//    }
//
//    my $code = '';
//    my $value = "";
//    if($data->{setoption} eq "text"){
//        $value = '\'' . $data->{settotext} . '\'';
//    }else{
//        if($data->{settovar} =~ /^js\./){
//            $data->{settovar} =~ s/^js\.//;
//        }
//        $value = "b['$data->{settovar}']";
//    }
//
//    my $setToVar = $data->{var};
//    $setToVar =~ s/^cp\.//;
//
//    my $cookieExpire = 'Thu, 31 Dec 2099 00:00:00 GMT';
//    my $cookieVal = $value;
//    if($data->{persistence}){
//        if($data->{persistence} eq 'visitor'){
//            #do not add anything
//        }elsif($data->{persistence} eq 'session'){
//            $cookieVal .= '+\';exp-session\'';
//            $cookieExpire = '';
//        }elsif($data->{persistence} eq 'hours'){
//            $cookieVal .= '+\';exp-'.$data->{persistencetext}.'h\'';
//            $cookieExpire = '"+(function(){var d=new Date();d.setTime(d.getTime()+(' . $data->{persistencetext} . '*3600000)); return d.toGMTString()})()+"';
//        }elsif($data->{persistence} eq 'days'){
//            $cookieVal .= '+\';exp-'.$data->{persistencetext}.'d\'';
//            $cookieExpire = '"+(function(){var d=new Date();d.setTime(d.getTime()+(' . $data->{persistencetext} . '*86400000)); return d.toGMTString()})()+"';
//        }
//    }
//
//    #SET COOKIE
//    if($setToVar =~ /^utag_main_/){
//        my $x = $setToVar;
//        $x =~ s/^utag_main_//;
//        $code .= 'utag.loader.SC(\'utag_main\',{\'' . $x . '\':' . $cookieVal . '});b[\'cp.' . $setToVar . '\']=' . $value . ';';
//    }else{
//        $code .= 'document.cookie="' . $setToVar . '="+'. $value . '+";path=/;domain="+utag.cfg.domain+";expires=' . $cookieExpire . '"';
//        $code .= ';b[\'cp.' . $setToVar . '\']=' . $value . ';';
//    }
//
//    #ALLOW Update
//    if(defined $data->{allowupdate} && $data->{allowupdate} eq 'once'){
//        if($setToVar =~ /^utag_main_/){
//            my $x = $setToVar;
//            $x =~ s/^utag_main_//;
//            $code = 'if(typeof b[\'cp.utag_main_'.$x.'\']==\'undefined\'){' . $code . '}';
//        }else{
//            $code = 'if(typeof b[\'cp.'.$setToVar.'\']==\'undefined\'){' . $code . '}';
//        }
//    }
//
//    # Look for composite filter conditions
//    my $filterHash;
//    my $conditionHash;
//    for my $key(sort { $a <=> $b }keys %{$data}){
//        if($key =~ /^(\d+)_(filter.*)$/){
//            $filterHash->{$1}->{$2} = $data->{$key};
//        }elsif($key =~ /^(\d+)_source$/){
//            $conditionHash->{$1}->{1}->{source} = $data->{$key};
//            $conditionHash->{$1}->{1}->{filter} = $data->{$1."_filter"};
//            $conditionHash->{$1}->{1}->{filtertype} = $data->{$1."_filtertype"};
//        }elsif($key =~ /^(\d+)_(\d+)_source$/){
//            $conditionHash->{$1}->{$2}->{source} = $data->{$key};
//            $conditionHash->{$1}->{$2}->{filter} = $data->{$1."_".$2."_filter"};
//            $conditionHash->{$1}->{$2}->{filtertype} = $data->{$1."_".$2."_filtertype"};
//        }
//    }
//
//    # Generate composite conditions
//    my @orCondition;
//    for my $key(sort {$a <=> $b} keys %$conditionHash){
//        my @andCondition;
//        for my $clause(sort {$a <=> $b} keys %{$conditionHash->{$key}}){
//            my $source = $conditionHash->{$key}->{$clause}->{source};
//            $source =~ s/^js\.//;
//            my $condition = generateConditionFilter({ input => "b['$source']", operator => $conditionHash->{$key}->{$clause}->{filtertype}, filter => $conditionHash->{$key}->{$clause}->{filter} });
//            if(length($condition)>0){
//                push @andCondition, $condition;
//            }
//        }
//
//        my $conditionSize = @andCondition;
//        if($conditionSize == 1){
//            push @orCondition, $andCondition[0];
//        }else{
//            push @orCondition, '('.(join '&&', @andCondition).')';
//        }
//    }
//
//    my $conditionSize = @orCondition;
//    my $condition = 1;
//    if($conditionSize > 0){
//        $condition = join '||', @orCondition;
//        $code = "if($condition){$code}";
//    } elsif (defined $data->{filter} && defined $data->{filtertype}){ # Backward compatibility support for non-composite conditions.
//        $code = 'if('. generateConditionFilter({ input => "b['$data->{source}']", operator => $data->{filtertype}, filter => $data->{filter} }) . '){' . $code . '}';
//    }
//
//    $extension = "function(a,b){" . $code . "}";

