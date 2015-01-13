package com.tealium.publisher.util;

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

import com.google.common.base.Strings;
import com.mongodb.DBObject;

import static com.tealium.publisher.utag.sync.ExtContentGenerator.*;

public class UtagSyncUtil {

	public static final String CONDITION_FILTER_PATTERN_REGEX = "[\\/\\$\\%\\(\\)\\*\\+\\.\\?\\[\\\\\\]\\{\\|\\}]";
	public static Pattern CONDITION_FILTER_PATTERN = Pattern
			.compile(CONDITION_FILTER_PATTERN_REGEX);
	/**
	 * retrieve special data from input customization element to fulfill condition
	 * @param customizationEntry
	 * @param pattern
	 * @return
	 */
	public static Map<String, Map<String, String>> getSetHash(DBObject customizationEntry, Pattern pattern) {
		Map<String, Map<String, String>> setHash = new HashMap<String, Map<String, String>>();
		
		for (String key : UtagSyncUtil.getAlphabeticSortedKeys(customizationEntry.keySet())) {
			Matcher matcher = pattern.matcher(key);
			if (matcher.find()) {
				Map<String, String> pair = new HashMap<String, String>();
				String[] vals = matcher.group().split("_");

				pair.put(vals[1], (String) customizationEntry.get(key));
				if (setHash.get(vals[0])!=null) {
					setHash.get(vals[0]).putAll(pair);
				}else{					
					setHash.put(vals[0], pair);
				}
			}
		}

		return setHash;
	}
	/**
	 * remove the characters at first occurance matching pattern
	 * 
	 * @param firstOccurPattern
	 * @param src
	 * @param charsRemoved
	 * @return
	 */
	public static String removeFirstPatternedChars(Pattern firstOccurPattern,String src,String charsRemoved){
		if (!Strings.isNullOrEmpty(src)&&firstOccurPattern.matcher(src).find()) {
			return src.replaceFirst(charsRemoved, "");
		}
		return src;
	}
	
	/**
	 * sorting a string collection 
	 * @param keys
	 * @return
	 */
	public static Collection<String> getAlphabeticSortedKeys(Collection<String> keys) {
		List<String> sortedKeys = new ArrayList<String>(keys);
		Collections.sort(sortedKeys);

		return sortedKeys;
	}
	
	/**
	 * get conditions for [0..9]+_source*  or [0..9]+_[0..9]+_source* keys
	 * 
	 * @param data
	 * @return
	 */
	public static  Map<String, Map<String, Map<String, String>>> getCondition(
			DBObject data) {
		Map<String, Map<String, Map<String, String>>> condition = new HashMap<String, Map<String, Map<String, String>>>();
  
		for (String key : UtagSyncUtil.getAlphabeticSortedKeys(data.keySet())) {
			if (KEY_PATTERN_ONE_DIGITAL_SOURCE.matcher(key).find()
					|| KEY_PATTERN_TWO_DIGITAL_SOURCE.matcher(key).find()) {
				condition.put(key.substring(0, key.indexOf("_")),
						getSubCondition(key, data));

			}
		}

		return condition;
	}

	public static  Map<String, Map<String, String>> getSubCondition(String key,
			DBObject data) {

		Map<String, Map<String, String>> level1Condition = new HashMap<String, Map<String, String>>();

		boolean is2DigitsKey = key.indexOf("_") != key.lastIndexOf("_");

		Map<String, String> subCondition = new HashMap<String, String>();
		subCondition.put(ELEMENT_SOURCE, (String) data.get(key));
		String digitPart = key.substring(0, key.indexOf("_"));
		String secDigitPart = key.substring(key.indexOf("_"),
				key.indexOf("_source"));

		String keyPrefix = is2DigitsKey ? digitPart + "_" + secDigitPart
				: digitPart;
		subCondition.put(ELEMENT_FILTER_TYPE,
				(String) data.get(keyPrefix + "_filtertype"));
		subCondition.put(ELEMENT_FILTER, (String) data.get(keyPrefix + "_filter"));

		level1Condition.put(is2DigitsKey ? secDigitPart : "1", subCondition);
		return level1Condition;
	}
	
	/**
	 * retrieve conditions for filter and filter type
	 * 
	 * @param condition
	 * @return
	 */
	public static Collection<String> getOrCondition(
			Map<String, Map<String, Map<String, String>>> condition) {
		Collection<String> orConditionList = new ArrayList<String>();

		for (String key1 : UtagSyncUtil.getAlphabeticSortedKeys(condition.keySet())) {
			List<String> andConditioin = new ArrayList<String>();
			Map<String, Map<String, String>> level1Condition = condition
					.get(key1);
			Set<String> keys2 = level1Condition.keySet();

			Collections.sort(new ArrayList<String>(keys2));

			for (String key2 : keys2) {
				String sourceValue = level1Condition.get(key2).get(ELEMENT_SOURCE)
						.replace(REMOVE_PATTERN_JS, "");

				String conditionFilter = UtagSyncUtil.getConditionFilter("b['"
						+ sourceValue + "']",
						level1Condition.get(key2).get(ELEMENT_FILTER_TYPE),
						level1Condition.get(key2).get(ELEMENT_FILTER));
				if (!Strings.isNullOrEmpty(conditionFilter)) {
					andConditioin.add(conditionFilter);
				}
			}

			if (andConditioin.size() == 1) {
				orConditionList.addAll(andConditioin);
			} else if (andConditioin.size() > 1) {
				StringBuilder sb = new StringBuilder().append("(")
						.append(StringUtils.join(andConditioin, "&&"))
						.append(")");
				orConditionList.add(sb.toString());
			}

		}

		return orConditionList;
	}
	
	
	/**
	 * get condition filter based on operator value
	 * 
	 * @param input
	 * @param operator
	 * @param filter
	 * @return
	 */
	public static String getConditionFilter(String input, String operator,
			String filter) {

		if ("equals".equalsIgnoreCase(operator)) {
//	        return "$obj->{input}=='$obj->{filter}'";
			return input + "=='" + filter + "'";

		} else if ("equals_ignore_case".equalsIgnoreCase(operator)) {
//	        return "$obj->{input}.toString().toLowerCase()=='$obj->{filter}'.toLowerCase()";
			return input + ".toString().toLowerCase() ==" + filter
					+ ".toLowerCase()";

		} else if ("starts_with".equalsIgnoreCase(operator)) {
			String temp = filter.replaceAll(CONDITION_FILTER_PATTERN_REGEX,
					"\\$&");
			// $temp =~ s/$match/\\$&/g; #escape filter (e.g. / to \/)

//	        return '/^'. $temp .'/.test('.$obj->{input}.')';
			return "/^" + temp + "/.test(" + input + ")";
		} else if ("starts_with_ignore_case".equalsIgnoreCase(operator)) {

			String temp = filter.replaceAll(CONDITION_FILTER_PATTERN_REGEX,
					"\\$&");
//	        return '/^'. $temp .'/i.test('.$obj->{input}.')';
			return "/^" + temp + "/i.test(" + input + ")";
		} else if ("does_not_start_with".equalsIgnoreCase(operator)) {

			String temp = filter.replaceAll(CONDITION_FILTER_PATTERN_REGEX,
					"\\$&");
//	        return '!/^'. $temp .'/.test('.$obj->{input}.')';
			return "!/^" + temp + "/.test(" + input + ")";
		} else if ("does_not_start_with_ignore_case".equalsIgnoreCase(operator)) {
			String temp = filter.replaceAll(CONDITION_FILTER_PATTERN_REGEX,
					"\\$&");
//	        return '!/^'. $temp .'/i.test('.$obj->{input}.')';
			return "!/^" + temp + "/i.test(" + input + ")";

		} else if ("does_not_equal".equalsIgnoreCase(operator)) {
			return input + "!='" + filter + "'";

		} else if ("does_not_equal_ignore_case".equalsIgnoreCase(operator)) {
			return input + ".toString().toLowerCase()!='" + filter
					+ "'.toLowerCase()";

		} else if ("ends_with".equalsIgnoreCase(operator)) {
			String temp = filter.replaceAll(CONDITION_FILTER_PATTERN_REGEX,
					"\\$&");
			return "/" + temp + "$/.test(" + input + ")";

		} else if ("ends_with_ignore_case".equalsIgnoreCase(operator)) {

			String temp = filter.replaceAll(CONDITION_FILTER_PATTERN_REGEX,
					"\\$&");
			return "/" + temp + "$/i.test(" + input + ")";

		} else if ("does_not_end_with".equalsIgnoreCase(operator)) {

			String temp = filter.replaceAll(CONDITION_FILTER_PATTERN_REGEX,
					"\\$&");
			return "!/" + temp + "$/.test(" + input + ")";

		} else if ("does_not_end_with_ignore_case".equalsIgnoreCase(operator)) {
			String temp = filter.replaceAll(CONDITION_FILTER_PATTERN_REGEX,
					"\\$&");
			return "!/" + temp + "$/i.test(" + input + ")";

		} else if ("contains".equalsIgnoreCase(operator)) {
			return input + ".toString().indexOf(" + filter + ")>-1";

		} else if ("contains_ignore_case".equalsIgnoreCase(operator)) {
			return input + ".toString().toLowerCase().indexOf(" + filter
					+ ".toLowerCase())>-1";
		} else if ("does_not_contain".equalsIgnoreCase(operator)) {
			return input + ".toString().indexOf(" + filter + ")<0";

		} else if ("does_not_contain_ignore_case".equalsIgnoreCase(operator)) {
			return input + ".toString().toLowerCase().indexOf(" + filter
					+ ".toLowerCase())<0";

		} else if ("is_badge_assigned".equalsIgnoreCase(operator)) {
			return "typeof " + input + "!='undefined'";

		} else if ("is_badge_not_assigned".equalsIgnoreCase(operator)) {
			// return "typeof $obj->{input}=='undefined'";
			return "typeof " + input + "=='undefined'";

		} else if ("defined".equalsIgnoreCase(operator)) {
			// return "typeof $obj->{input}!='undefined'";
			return "typeof " + input + "!='undefined'";

		} else if ("notdefined".equalsIgnoreCase(operator)) {
			// return "typeof $obj->{input}=='undefined'";
			return "typeof " + input + "=='undefined'";

		} else if ("populated".equalsIgnoreCase(operator)) {
			// return "typeof $obj->{input}!='undefined'&&$obj->{input}!=''";
			return "typeof " + input + "!='undefined'&&" + input + "!=''";

		} else if ("notpopulated".equalsIgnoreCase(operator)) {
			// return "typeof $obj->{input}!='undefined'&&$obj->{input}==''";
			return "typeof " + input + "!='undefined'&&" + input + "==''";

		} else if ("greater_than".equalsIgnoreCase(operator)) {
			// return "parseFloat($obj->{input})>parseFloat($obj->{filter})";
			return "parseFloat(" + input + ")>parseFloat(" + filter + ")";

		} else if ("greater_than_equal_to".equalsIgnoreCase(operator)) {
			// return "parseFloat($obj->{input})>=parseFloat($obj->{filter})";
			return "parseFloat(" + input + ")>=parseFloat(" + filter + ")";
		} else if ("less_than".equalsIgnoreCase(operator)) {
			// return "parseFloat($obj->{input})<parseFloat($obj->{filter})";
			return "parseFloat(" + input + ")<parseFloat(" + filter + ")";

		} else if ("less_than_equal_to".equalsIgnoreCase(operator)) {
			// return "parseFloat($obj->{input})<=parseFloat($obj->{filter})";
			return "parseFloat(" + input + ")<=parseFloat(" + filter + ")";

		} else if ("regular_expression".equalsIgnoreCase(operator)) {
			if (filter.indexOf("/") != 0) {
				filter = "/" + filter + "/";
			}

			// return "$obj->{filter}.test($obj->{input})";
			return filter + ".test(" + input + ")";

		}

		return null;
	}

}

//input => "b['$source']", operator => $conditionHash->{$key}->{$clause}->{filtertype}, filter => $conditionHash->{$key}->{$clause}->{filter}

//sub generateConditionFilter{
//    my($obj) = @_;
//    #Regex to escape any characters which would break the generated javascript regex filter
//    my $match = qr/[\/\$\%\(\)\*\+\.\?\[\\\]\{\|\}]/;
//
//    if($obj->{operator} eq "equals"){
//        return "$obj->{input}=='$obj->{filter}'";
//
//    }elsif($obj->{operator} eq 'equals_ignore_case'){
//        return "$obj->{input}.toString().toLowerCase()=='$obj->{filter}'.toLowerCase()";
//
//    }elsif($obj->{operator} eq 'starts_with'){
//    my $temp = $obj->{filter};
//    $temp =~ s/$match/\\$&/g; #escape filter (e.g. / to \/)
//        return '/^'. $temp .'/.test('.$obj->{input}.')';
//
//    }elsif($obj->{operator} eq 'starts_with_ignore_case'){
//    my $temp = $obj->{filter};
//    $temp =~ s/$match/\\$&/g;
//        return '/^'. $temp .'/i.test('.$obj->{input}.')';
//
//    }elsif($obj->{operator} eq 'does_not_start_with'){
//    my $temp = $obj->{filter};
//    $temp =~ s/$match/\\$&/g;
//        return '!/^'. $temp .'/.test('.$obj->{input}.')';
//
//    }elsif($obj->{operator} eq 'does_not_start_with_ignore_case'){
//    my $temp = $obj->{filter};
//    $temp =~ s/$match/\\$&/g;
//        return '!/^'. $temp .'/i.test('.$obj->{input}.')';
//
//    }elsif($obj->{operator} eq 'does_not_equal'){
//        return "$obj->{input}!='$obj->{filter}'";
//
//    }elsif($obj->{operator} eq 'does_not_equal_ignore_case'){
//        return "$obj->{input}.toString().toLowerCase()!='$obj->{filter}'.toLowerCase()";
//
//    }elsif($obj->{operator} eq 'ends_with'){
//    my $temp = $obj->{filter};
//    $temp =~ s/$match/\\$&/g;
//        return '/'. $temp .'$/.test('.$obj->{input}.')';
//
//    }elsif($obj->{operator} eq 'ends_with_ignore_case'){
//    my $temp = $obj->{filter};
//    $temp =~ s/$match/\\$&/g;
//        return '/'. $temp .'$/i.test('.$obj->{input}.')';
//
//    }elsif($obj->{operator} eq 'does_not_end_with'){
//    my $temp = $obj->{filter};
//    $temp =~ s/$match/\\$&/g;
//        return '!/'. $temp .'$/.test('.$obj->{input}.')';
//
//    }elsif($obj->{operator} eq 'does_not_end_with_ignore_case'){
//    my $temp = $obj->{filter};
//    $temp =~ s/$match/\\$&/g;
//        return '!/'. $temp .'$/i.test('.$obj->{input}.')';
//
//    }elsif($obj->{operator} eq 'contains'){
//        return "$obj->{input}.toString().indexOf('$obj->{filter}')>-1";
//
//    }elsif($obj->{operator} eq 'contains_ignore_case'){
//        return "$obj->{input}.toString().toLowerCase().indexOf('$obj->{filter}'.toLowerCase())>-1";
//
//    }elsif($obj->{operator} eq 'does_not_contain'){
//        return "$obj->{input}.toString().indexOf('$obj->{filter}')<0";
//
//    }elsif($obj->{operator} eq 'does_not_contain_ignore_case'){
//        return "$obj->{input}.toString().toLowerCase().indexOf('$obj->{filter}'.toLowerCase())<0";
//
//    }elsif($obj->{operator} eq 'is_badge_assigned'){
//        return "typeof $obj->{input}!='undefined'";
//
//    }elsif($obj->{operator} eq 'is_badge_not_assigned'){
//        return "typeof $obj->{input}=='undefined'";
//
//    }elsif($obj->{operator} eq 'defined'){
//        return "typeof $obj->{input}!='undefined'";
//
//    }elsif($obj->{operator} eq 'notdefined'){
//        return "typeof $obj->{input}=='undefined'";
//
//    }elsif($obj->{operator} eq 'populated'){
//        return "typeof $obj->{input}!='undefined'&&$obj->{input}!=''";
//
//    }elsif($obj->{operator} eq 'notpopulated'){
//        return "typeof $obj->{input}!='undefined'&&$obj->{input}==''";
//
//    }elsif($obj->{operator} eq 'greater_than'){
//        return "parseFloat($obj->{input})>parseFloat($obj->{filter})";
//
//    }elsif($obj->{operator} eq 'greater_than_equal_to'){
//        return "parseFloat($obj->{input})>=parseFloat($obj->{filter})";
//
//    }elsif($obj->{operator} eq 'less_than'){
//        return "parseFloat($obj->{input})<parseFloat($obj->{filter})";
//
//    }elsif($obj->{operator} eq 'less_than_equal_to'){
//        return "parseFloat($obj->{input})<=parseFloat($obj->{filter})";
//
//    }elsif($obj->{operator} eq 'regular_expression'){
//        if(index($obj->{filter},'/')!=0){
//            $obj->{filter} = '/'.$obj->{filter}.'/';
//        }
//        return "$obj->{filter}.test($obj->{input})";
//
//    }
//}