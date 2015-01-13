package com.tealium.publisher.utag.sync;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.mongodb.DBObject;
import com.tealium.publisher.util.UtagSyncUtil;

/**
 * Extension content generator for id = 100003
 *
 */
public class ExtContentGeneratorImpl100003 implements ExtContentGenerator {
	private static Logger logger = LoggerFactory
			.getLogger(ExtContentGeneratorImpl100003.class);

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
		Map<String, Map<String, String>> setHash =UtagSyncUtil. getSetHash(customizedExtensionData,KEY_PATTERN_ONE_DIGITAL_SET_CHARS);

		String filteredCond = null;
		if (orConditionList.size() == 1) {
			filteredCond = ((List) orConditionList).get(0).toString();
		} else if (orConditionList.size() > 1) {
			filteredCond = StringUtils.join(orConditionList, "||");
		} 
		// #generate set content
		Collection<String> setContentList = getSetContent(setHash);
		if (setContentList.size() > 0) {
			sb.append("function(a,b){if(").append(filteredCond).append("){")
					.append(StringUtils.join(setContentList, ";")).append("}}");
		}

		return sb.toString();
	}

	private Collection<String> getSetContent(
			Map<String, Map<String, String>> setHash) {
		Collection<String> setContentList = new ArrayList<String>();

		for (String key : UtagSyncUtil.getAlphabeticSortedKeys(setHash.keySet())) {
			Map<String, String> map = setHash.get(key);
			if (map.get("set") != null) {
				map.put("set", map.get("set").replace("js.", ""));
			}

			if (map.get("setoption") != null) {
				String option = map.get("setoption");
				switch (option) {
				case "var":
					map.put("set", map.get("settovar").replace("js.", ""));
					setContentList.add("b['" + map.get("set") + "']=b['"
							+ map.get("settovar") + "']");
					break;
				case "text":
					setContentList.add("b['" + map.get("set") + "']='"
							+ map.get("settotext") + "'");
					break;
				case "code":
					setContentList.add("try{b['" + map.get("set") + "']="
							+ map.get("settotext") + "}catch(e){}");
					break;

				default:
					logger.error("No option [" + option + "] match: " + setHash);
					break;
				}

			}
		}

		return setContentList;
	}

}

//}elsif($id eq '100003'){
//    my $setHash;
//    my $conditionHash;
//    for my $key(sort { $a <=> $b }keys %{$data}){
//            if($key =~ /^(\d+)_(set.*)$/){
//                    $setHash->{$1}->{$2} = $data->{$key};
//            }elsif($key =~ /^(\d+)_source$/){
//                    $conditionHash->{$1}->{1}->{source} = $data->{$key};
//                    $conditionHash->{$1}->{1}->{filter} = $data->{$1."_filter"};
//                    $conditionHash->{$1}->{1}->{filtertype} = $data->{$1."_filtertype"};
//            }elsif($key =~ /^(\d+)_(\d+)_source$/){
//                    $conditionHash->{$1}->{$2}->{source} = $data->{$key};
//                    $conditionHash->{$1}->{$2}->{filter} = $data->{$1."_".$2."_filter"};
//                    $conditionHash->{$1}->{$2}->{filtertype} = $data->{$1."_".$2."_filtertype"};
//            }
//    }
//
//    #generate condition
//    my @orCondition;
//    for my $key(sort {$a <=> $b} keys %$conditionHash){
//            my @andCondition;
//            for my $clause(sort {$a <=> $b} keys %{$conditionHash->{$key}}){
//                    my $source = $conditionHash->{$key}->{$clause}->{source};
//                    $source =~ s/^js\.//;
//                    my $condition = generateConditionFilter({ input => "b['$source']", operator => $conditionHash->{$key}->{$clause}->{filtertype}, filter => $conditionHash->{$key}->{$clause}->{filter} });
//                    if(length($condition)>0){
//                            push @andCondition, $condition;
//                    }
//            }
//
//            my $conditionSize = @andCondition;
//            if($conditionSize == 1){
//                    push @orCondition, $andCondition[0];
//            }else{
//                    push @orCondition, '('.(join '&&', @andCondition).')';
//            }
//    }
//
//    my $conditionSize = @orCondition;
//    my $condition = 1;
//    if($conditionSize > 0){
//            $condition = join '||', @orCondition;
//    }
//
//    #generate set
//    my @setContent;
//    for my $key(sort { $a <=> $b } keys %$setHash){
//            $setHash->{$key}->{set} =~ s/^js\.//;
//            if($setHash->{$key}->{setoption} eq 'var'){
//                    $setHash->{$key}->{settovar} =~ s/^js\.//;
//                    push @setContent, "b['$setHash->{$key}->{set}']=b['$setHash->{$key}->{settovar}']";
//            }elsif($setHash->{$key}->{setoption} eq 'text'){
//                    push @setContent, "b['$setHash->{$key}->{set}']='$setHash->{$key}->{settotext}'";
//            }elsif($setHash->{$key}->{setoption} eq 'code'){
//                    push @setContent, "try{b['$setHash->{$key}->{set}']=$setHash->{$key}->{settotext}}catch(e){}";
//            }
//    }
//
//    my $setSize = @setContent;
//    my $clauseSize = @orCondition;
//    if($setSize > 0){
//            $extension = "function(a,b){if($condition){".join(';',@setContent)."}}";
//    }

