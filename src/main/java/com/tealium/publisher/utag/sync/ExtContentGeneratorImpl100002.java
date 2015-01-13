package com.tealium.publisher.utag.sync;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.mongodb.DBObject;
import com.tealium.publisher.util.UtagSyncUtil;

/**
 * Extension content generator for id = 100002
 *
 */
public class ExtContentGeneratorImpl100002 implements ExtContentGenerator {
	private static Logger logger = LoggerFactory
			.getLogger(ExtContentGeneratorImpl100002.class);



	@SuppressWarnings("rawtypes")
	@Override
	public String generateExtensionContent(DBObject customizedExtensionData,
			DBObject config, DBObject profile) {
		Preconditions.checkNotNull(config, "No configuration was defined!");

		if (customizedExtensionData == null) {
			return null;
		}
		StringBuilder sb = new StringBuilder();

		Map<String, Map<String, Map<String, String>>> condition = UtagSyncUtil
				.getCondition(customizedExtensionData);
		Collection<String> orConditionList = UtagSyncUtil
				.getOrCondition(condition);

		String filteredCond = null;
		if (orConditionList.size() == 1) {
			filteredCond = ((List) orConditionList).get(0).toString();
		} else if (orConditionList.size() > 1) {
			filteredCond = StringUtils.join(orConditionList, "||");
		}
		// generate join
		Collection<String> joinKeys = new ArrayList<String>();
		for (String key : customizedExtensionData.keySet()) {
			if (KEY_PATTERN_ONE_DIGITAL_SET.matcher(key).find()) {
				Matcher matcher = DIGIT_PATTERN.matcher(key);
				while (matcher.find()) {
					joinKeys.add(matcher.group());
				}
			}
		}

		Collection<String> concatValues = getConcatValues(joinKeys,
				customizedExtensionData);
		StringBuilder sb1 = new StringBuilder();
		if (concatValues.size() > 0) {
			sb1.append("c=[");
			sb1.append(StringUtils.join(concatValues, ",")).append("];");

			if (!Strings.isNullOrEmpty((String) customizedExtensionData
					.get("defaultvalue"))) {
				sb1.append("for(d=0;d<c.length;d++){if(typeof c[d]=='undefined'||c[d]=='')c[d]='");
				sb1.append(customizedExtensionData.get("defaultvalue")).append(
						"'};");
			}
			if (customizedExtensionData.get("var") != null) {
				String output = customizedExtensionData.get("var").toString()
						.replace("js.", "");
				sb1.append("b['").append(output).append("']=");
				sb1.append((customizedExtensionData.get("leadingdelimiter") != null && "yes"
						.equals(customizedExtensionData.get("leadingdelimiter")
								.toString())) ? customizedExtensionData
						.get("delimiter") : "");
				sb1.append("c.join('")
						.append(customizedExtensionData.get("delimiter"))
						.append("')");
			} else {
				logger.error("extension data does not have 'var' defined - "
						+ customizedExtensionData);
			}
		}

		sb.append("function(a,b,c,d){if(");
		sb.append(filteredCond);
		sb.append("){");
		sb.append(sb1);
		sb.append("}}");

		return sb.toString();
	}

	private Collection<String> getConcatValues(Collection<String> joinKeys,
			DBObject data) {
		Collection<String> values = new ArrayList<String>();
		for (String key : joinKeys) {
			String val = (String) data.get(key + "_set");
			if (!Strings.isNullOrEmpty(val)) {
				if (JS_PATTERN.matcher(val).find()) {
					val = val.replace("js.", "");
				}
			} else {
				val = (String) data.get(key + "_set_text");
				val.replaceAll("\\", "\\\\");
				val.replaceAll("\'", "\\'");
			}
			values.add(val);
		}

		return values;
	}
}

//my $conditionHash;
//for my $key(sort { $a <=> $b }keys %{$data}){
//        if($key =~ /^(\d+)_source$/){
//                $conditionHash->{$1}->{1}->{source} = $data->{$key};
//                $conditionHash->{$1}->{1}->{filter} = $data->{$1."_filter"};
//                $conditionHash->{$1}->{1}->{filtertype} = $data->{$1."_filtertype"};
//        }elsif($key =~ /^(\d+)_(\d+)_source$/){
//                $conditionHash->{$1}->{$2}->{source} = $data->{$key};
//                $conditionHash->{$1}->{$2}->{filter} = $data->{$1."_".$2."_filter"};
//                $conditionHash->{$1}->{$2}->{filtertype} = $data->{$1."_".$2."_filtertype"};
//        }
//}

//#generate condition
//my @orCondition;
//for my $key(sort {$a <=> $b} keys %$conditionHash){
//        my @andCondition;
//        for my $clause(sort {$a <=> $b} keys %{$conditionHash->{$key}}){
//                my $source = $conditionHash->{$key}->{$clause}->{source};
//                $source =~ s/^js\.//;
//                my $condition = generateConditionFilter({ input => "b['$source']", operator => $conditionHash->{$key}->{$clause}->{filtertype}, filter => $conditionHash->{$key}->{$clause}->{filter} });
//                if(length($condition)>0){
//                        push @andCondition, $condition;
//                }
//        }
//
//        my $conditionSize = @andCondition;
//        if($conditionSize == 1){
//                push @orCondition, $andCondition[0];
//        }else{
//                push @orCondition, '('.(join '&&', @andCondition).')';
//        }
//}
//
//my $conditionSize = @orCondition;
//my $condition = 1;
//if($conditionSize > 0){
//        $condition = join '||', @orCondition;
//}
//
//# generate join
//my %joinKeys;
//for my $key(keys %{$data}){
//        if($key =~ /^(\d+)_set/){
//                $joinKeys{$1} = 1;
//        }
//}
//
//my @concatValues;
//for my $key(sort { $a <=> $b } keys %joinKeys ){
//        my $value;
//        if ($data->{$key.'_set'} =~ /[.]/){
//            $value = $data->{$key.'_set'};
//            if($value =~ /^js\.(.*)$/){
//                $value = $1;
//            }
//            push @concatValues, "b['".$value."']";
//        } else {
//            $value = $data->{$key.'_set_text'};
//            $value =~ s/\\/\\\\/g;
//            $value =~ s/\'/\\'/g;
//            push @concatValues, "'".$value."'";
//        }
//}
//
//if(@concatValues){
//        my $code='c=['.(join(',',@concatValues)).'];';
//        if($data->{defaultvalue} ne ""){
//                $code.="for(d=0;d<c.length;d++){if(typeof c[d]=='undefined'||c[d]=='')c[d]='".$data->{defaultvalue}."'};";
//        }
//        my $outputVar = $data->{var};
//        $outputVar =~ s/^js\.//;
//        $code.="b['$outputVar']=".(($data->{leadingdelimiter} eq 'yes')?"'$data->{delimiter}'+":"")."c.join('".$data->{delimiter}."')";
//        #$extension = "function(a,b,c,d){$code}";
//        $extension = "function(a,b,c,d){if($condition){$code}}";
//}


