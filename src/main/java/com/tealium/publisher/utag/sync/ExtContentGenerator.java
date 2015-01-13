package com.tealium.publisher.utag.sync;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.regex.Pattern;

import com.mongodb.DBObject;

public interface ExtContentGenerator {

	public static final Pattern KEY_PATTERN_ONE_DIGITAL_SET_CHARS = Pattern
			.compile("^[0-9]+_set.*");
	public static final Pattern KEY_PATTERN_ONE_DIGITAL_FILTER_CHARS = Pattern
			.compile("^[0-9]+_filter.*");
	public static final Pattern KEY_PATTERN_ONE_DIGITAL_SET = Pattern
			.compile("^[0-9]+_set");
	public static final Pattern KEY_PATTERN_TWO_DIGITAL_SET = Pattern
			.compile("^[0-9]+_[0-9]+_set");
	public static final Pattern KEY_PATTERN_ONE_DIGITAL_SOURCE = Pattern
			.compile("^[0-9]+_source");
	public static final Pattern KEY_PATTERN_TWO_DIGITAL_SOURCE = Pattern
			.compile("^[0-9]+_[0-9]+_source");
	public static final Pattern DIGIT_PATTERN = Pattern.compile("[0-9]+");
	
	public static final Pattern JS_PATTERN = Pattern.compile("^js\\.(.*)$");
	
	
	public static final String VALUE_NO = "no";
	public static final String VALUE_YES = "yes";
	public static final String VALUE_ALL = "all";
	public static final String VALUE_TEXT = "text";
	
	public static final String ELEMENT_FILTER = "filter";
	public static final String ELEMENT_FILTER_TYPE = "filtertype";
	public static final String ELEMENT_SOURCE = "source";
	public static final String ELEMENT_SETOPTION = "setoption";
	public static final String ELEMENT_SETTOTEXT = "settotext";
	public static final String ELEMENT_SETTOVAR = "settovar";
	public static final String ELEMENT_MONGO_ID = "_id";
	public static final String ELEMENT_VAR = "var";
	public static final String ELEMENT_PERSISTENCE = "persistence";
	public static final String ELEMENT_PERSISTENCETEXT = "persistencetext";
	public static final String ELEMENT_ALLOWUPDATE = "allowupdate";

	
	public static final String REMOVE_PATTERN_JS = "js.";
	public static final String SYNC_ID_PREFIX = "10000";
	public static final int SYNC_ID_MAX = 35;
	public static final String INVOCATION_SCOPE_SYNC="sync";

	public static final Collection<String> SYNC_ID_LIST_DEFAULT = Collections
			.unmodifiableCollection(Arrays.asList("100001", "100002"));

	/**
	 * generating extension content based on customized extension data and
	 * config data
	 * 
	 * @param customizedExtensionData
	 * @param config
	 * @param profile 
	 * @return
	 */
	public String generateExtensionContent(DBObject customizedExtensionData,
			DBObject config, DBObject profile);

}
//generateExtension("sync",$configData, $extensionData->{$k}->{id}, $extensionData->{$k}, $profileData)

//
//
///*
// * sub generateExtension{
//        my($invocationScope, $configData, $id, $data, $profileData) = @_;
//        my $extension = "";
//        my $log = Log::Log4perl->get_logger;
//
//        $log->debug("Commence generateExtension : $id - $invocationScope");
//
//        #100001:Lower-Casing - COMPLETE
//        if($id eq '100001'){
//                if($data->{all} eq 'yes'){
//                        $extension = "function(a,b,c){for(c in utag.loader.GV(b)){try{b[c] = (b[c] instanceof Array || b[c] instanceof Object) ? b[c] : b[c].toString().toLowerCase()}catch(e){}}}";
//                }elsif($data->{all} eq 'no'){
//                        my @varArr;
//                        for my $key(keys %$data){
//                                if($key =~ /^\d+_set$/){
//                                        my $var = $data->{$key};
//                                        $var =~ s/^js\.//;
//                                        push @varArr, $var;
//                                }
//                        }
//                        my $varSize = @varArr;
//                        if($varSize > 0){
//                                $extension = "function(a,b,c,d){c=['".(join "','", @varArr)."'];for(d=0;d<c.length;d++){try{b[c[d]] = (b[c[d]] instanceof Array || b[c[d]] instanceof Object) ? b[c[d]] : b[c[d]].toString().toLowerCase()}catch(e){}}}";
//                        }
//                }
//
//        #100002:Join Variables - COMPLETE
//        }elsif($id eq '100002'){
//                my $conditionHash;
//                for my $key(sort { $a <=> $b }keys %{$data}){
//                        #if($key =~ /^(\d+)_(set.*)$/){
//                                #$setHash->{$1}->{$2} = $data->{$key};
//                        #}els
//                        if($key =~ /^(\d+)_source$/){
//                                $conditionHash->{$1}->{1}->{source} = $data->{$key};
//                                $conditionHash->{$1}->{1}->{filter} = $data->{$1."_filter"};
//                                $conditionHash->{$1}->{1}->{filtertype} = $data->{$1."_filtertype"};
//                        }elsif($key =~ /^(\d+)_(\d+)_source$/){
//                                $conditionHash->{$1}->{$2}->{source} = $data->{$key};
//                                $conditionHash->{$1}->{$2}->{filter} = $data->{$1."_".$2."_filter"};
//                                $conditionHash->{$1}->{$2}->{filtertype} = $data->{$1."_".$2."_filtertype"};
//                        }
//                }
//
//                #generate condition
//                my @orCondition;
//                for my $key(sort {$a <=> $b} keys %$conditionHash){
//                        my @andCondition;
//                        for my $clause(sort {$a <=> $b} keys %{$conditionHash->{$key}}){
//                                my $source = $conditionHash->{$key}->{$clause}->{source};
//                                $source =~ s/^js\.//;
//                                my $condition = generateConditionFilter({ input => "b['$source']", operator => $conditionHash->{$key}->{$clause}->{filtertype}, filter => $conditionHash->{$key}->{$clause}->{filter} });
//                                if(length($condition)>0){
//                                        push @andCondition, $condition;
//                                }
//                        }
//
//                        my $conditionSize = @andCondition;
//                        if($conditionSize == 1){
//                                push @orCondition, $andCondition[0];
//                        }else{
//                                push @orCondition, '('.(join '&&', @andCondition).')';
//                        }
//                }
//
//                my $conditionSize = @orCondition;
//                my $condition = 1;
//                if($conditionSize > 0){
//                        $condition = join '||', @orCondition;
//                }
//
//                # generate join
//                my %joinKeys;
//                for my $key(keys %{$data}){
//                        if($key =~ /^(\d+)_set/){
//                                $joinKeys{$1} = 1;
//                        }
//                }
//
//                my @concatValues;
//                for my $key(sort { $a <=> $b } keys %joinKeys ){
//                        my $value;
//                        if ($data->{$key.'_set'} =~ /[.]/){
//                            $value = $data->{$key.'_set'};
//                            if($value =~ /^js\.(.*)$/){
//                                $value = $1;
//                            }
//                            push @concatValues, "b['".$value."']";
//                        } else {
//                            $value = $data->{$key.'_set_text'};
//                            $value =~ s/\\/\\\\/g;
//                            $value =~ s/\'/\\'/g;
//                            push @concatValues, "'".$value."'";
//                        }
//                }
//
//                if(@concatValues){
//                        my $code='c=['.(join(',',@concatValues)).'];';
//                        if($data->{defaultvalue} ne ""){
//                                $code.="for(d=0;d<c.length;d++){if(typeof c[d]=='undefined'||c[d]=='')c[d]='".$data->{defaultvalue}."'};";
//                        }
//                        my $outputVar = $data->{var};
//                        $outputVar =~ s/^js\.//;
//                        $code.="b['$outputVar']=".(($data->{leadingdelimiter} eq 'yes')?"'$data->{delimiter}'+":"")."c.join('".$data->{delimiter}."')";
//                        #$extension = "function(a,b,c,d){$code}";
//                        $extension = "function(a,b,c,d){if($condition){$code}}";
//                }
//
//        #100003:Set Variables - COMPLETE
//        }elsif($id eq '100003'){
//                my $setHash;
//                my $conditionHash;
//                for my $key(sort { $a <=> $b }keys %{$data}){
//                        if($key =~ /^(\d+)_(set.*)$/){
//                                $setHash->{$1}->{$2} = $data->{$key};
//                        }elsif($key =~ /^(\d+)_source$/){
//                                $conditionHash->{$1}->{1}->{source} = $data->{$key};
//                                $conditionHash->{$1}->{1}->{filter} = $data->{$1."_filter"};
//                                $conditionHash->{$1}->{1}->{filtertype} = $data->{$1."_filtertype"};
//                        }elsif($key =~ /^(\d+)_(\d+)_source$/){
//                                $conditionHash->{$1}->{$2}->{source} = $data->{$key};
//                                $conditionHash->{$1}->{$2}->{filter} = $data->{$1."_".$2."_filter"};
//                                $conditionHash->{$1}->{$2}->{filtertype} = $data->{$1."_".$2."_filtertype"};
//                        }
//                }
//
//                #generate condition
//                my @orCondition;
//                for my $key(sort {$a <=> $b} keys %$conditionHash){
//                        my @andCondition;
//                        for my $clause(sort {$a <=> $b} keys %{$conditionHash->{$key}}){
//                                my $source = $conditionHash->{$key}->{$clause}->{source};
//                                $source =~ s/^js\.//;
//                                my $condition = generateConditionFilter({ input => "b['$source']", operator => $conditionHash->{$key}->{$clause}->{filtertype}, filter => $conditionHash->{$key}->{$clause}->{filter} });
//                                if(length($condition)>0){
//                                        push @andCondition, $condition;
//                                }
//                        }
//
//                        my $conditionSize = @andCondition;
//                        if($conditionSize == 1){
//                                push @orCondition, $andCondition[0];
//                        }else{
//                                push @orCondition, '('.(join '&&', @andCondition).')';
//                        }
//                }
//
//                my $conditionSize = @orCondition;
//                my $condition = 1;
//                if($conditionSize > 0){
//                        $condition = join '||', @orCondition;
//                }
//
//                #generate set
//                my @setContent;
//                for my $key(sort { $a <=> $b } keys %$setHash){
//                        $setHash->{$key}->{set} =~ s/^js\.//;
//                        if($setHash->{$key}->{setoption} eq 'var'){
//                                $setHash->{$key}->{settovar} =~ s/^js\.//;
//                                push @setContent, "b['$setHash->{$key}->{set}']=b['$setHash->{$key}->{settovar}']";
//                        }elsif($setHash->{$key}->{setoption} eq 'text'){
//                                push @setContent, "b['$setHash->{$key}->{set}']='$setHash->{$key}->{settotext}'";
//                        }elsif($setHash->{$key}->{setoption} eq 'code'){
//                                push @setContent, "try{b['$setHash->{$key}->{set}']=$setHash->{$key}->{settotext}}catch(e){}";
//                        }
//                }
//
//                my $setSize = @setContent;
//                my $clauseSize = @orCondition;
//                if($setSize > 0){
//                        $extension = "function(a,b){if($condition){".join(';',@setContent)."}}";
//                }
//
//        #100004:Persist Variables
//        }elsif($id eq '100004'){
//            $extension = "";
//            if($data->{source} !~ /^qp\.|^cp\.|^dom\.|^meta\.|^location\.|^document\.|^js_page\.|^va\./){
//                $data->{source} =~ s/^\w+\.//;
//            }
//
//            my $code = '';
//            my $value = "";
//            if($data->{setoption} eq "text"){
//                $value = '\'' . $data->{settotext} . '\'';
//            }else{
//                if($data->{settovar} =~ /^js\./){
//                    $data->{settovar} =~ s/^js\.//;
//                }
//                $value = "b['$data->{settovar}']";
//            }
//
//            my $setToVar = $data->{var};
//            $setToVar =~ s/^cp\.//;
//
//            my $cookieExpire = 'Thu, 31 Dec 2099 00:00:00 GMT';
//            my $cookieVal = $value;
//            if($data->{persistence}){
//                if($data->{persistence} eq 'visitor'){
//                    #do not add anything
//                }elsif($data->{persistence} eq 'session'){
//                    $cookieVal .= '+\';exp-session\'';
//                    $cookieExpire = '';
//                }elsif($data->{persistence} eq 'hours'){
//                    $cookieVal .= '+\';exp-'.$data->{persistencetext}.'h\'';
//                    $cookieExpire = '"+(function(){var d=new Date();d.setTime(d.getTime()+(' . $data->{persistencetext} . '*3600000)); return d.toGMTString()})()+"';
//                }elsif($data->{persistence} eq 'days'){
//                    $cookieVal .= '+\';exp-'.$data->{persistencetext}.'d\'';
//                    $cookieExpire = '"+(function(){var d=new Date();d.setTime(d.getTime()+(' . $data->{persistencetext} . '*86400000)); return d.toGMTString()})()+"';
//                }
//            }
//
//            #SET COOKIE
//            if($setToVar =~ /^utag_main_/){
//                my $x = $setToVar;
//                $x =~ s/^utag_main_//;
//                $code .= 'utag.loader.SC(\'utag_main\',{\'' . $x . '\':' . $cookieVal . '});b[\'cp.' . $setToVar . '\']=' . $value . ';';
//            }else{
//                $code .= 'document.cookie="' . $setToVar . '="+'. $value . '+";path=/;domain="+utag.cfg.domain+";expires=' . $cookieExpire . '"';
//                $code .= ';b[\'cp.' . $setToVar . '\']=' . $value . ';';
//            }
//
//            #ALLOW Update
//            if(defined $data->{allowupdate} && $data->{allowupdate} eq 'once'){
//                if($setToVar =~ /^utag_main_/){
//                    my $x = $setToVar;
//                    $x =~ s/^utag_main_//;
//                    $code = 'if(typeof b[\'cp.utag_main_'.$x.'\']==\'undefined\'){' . $code . '}';
//                }else{
//                    $code = 'if(typeof b[\'cp.'.$setToVar.'\']==\'undefined\'){' . $code . '}';
//                }
//            }
//
//            # Look for composite filter conditions
//            my $filterHash;
//            my $conditionHash;
//            for my $key(sort { $a <=> $b }keys %{$data}){
//                if($key =~ /^(\d+)_(filter.*)$/){
//                    $filterHash->{$1}->{$2} = $data->{$key};
//                }elsif($key =~ /^(\d+)_source$/){
//                    $conditionHash->{$1}->{1}->{source} = $data->{$key};
//                    $conditionHash->{$1}->{1}->{filter} = $data->{$1."_filter"};
//                    $conditionHash->{$1}->{1}->{filtertype} = $data->{$1."_filtertype"};
//                }elsif($key =~ /^(\d+)_(\d+)_source$/){
//                    $conditionHash->{$1}->{$2}->{source} = $data->{$key};
//                    $conditionHash->{$1}->{$2}->{filter} = $data->{$1."_".$2."_filter"};
//                    $conditionHash->{$1}->{$2}->{filtertype} = $data->{$1."_".$2."_filtertype"};
//                }
//            }
//
//            # Generate composite conditions
//            my @orCondition;
//            for my $key(sort {$a <=> $b} keys %$conditionHash){
//                my @andCondition;
//                for my $clause(sort {$a <=> $b} keys %{$conditionHash->{$key}}){
//                    my $source = $conditionHash->{$key}->{$clause}->{source};
//                    $source =~ s/^js\.//;
//                    my $condition = generateConditionFilter({ input => "b['$source']", operator => $conditionHash->{$key}->{$clause}->{filtertype}, filter => $conditionHash->{$key}->{$clause}->{filter} });
//                    if(length($condition)>0){
//                        push @andCondition, $condition;
//                    }
//                }
//
//                my $conditionSize = @andCondition;
//                if($conditionSize == 1){
//                    push @orCondition, $andCondition[0];
//                }else{
//                    push @orCondition, '('.(join '&&', @andCondition).')';
//                }
//            }
//
//            my $conditionSize = @orCondition;
//            my $condition = 1;
//            if($conditionSize > 0){
//                $condition = join '||', @orCondition;
//                $code = "if($condition){$code}";
//            } elsif (defined $data->{filter} && defined $data->{filtertype}){ # Backward compatibility support for non-composite conditions.
//                $code = 'if('. generateConditionFilter({ input => "b['$data->{source}']", operator => $data->{filtertype}, filter => $data->{filter} }) . '){' . $code . '}';
//            }
//
//            $extension = "function(a,b){" . $code . "}";
//
//        #100005:E-commerce
//        }elsif($id eq '100005'){
//                my %outData;
//                for my $key(keys %$data){
//                        if($key =~ /^c/){
//                                $outData{$key} = $data->{$key};
//                                if($outData{$key} =~ /^js\./){
//                                        $outData{$key} =~ s/^js\.//;
//                                }
//                        }
//                }
//                if(!$data->{separator}){
//                        $data->{separator} = ',';
//                }
//
//                $extension = '';
//                my $commerceVars = {
//                        corder => '',
//                        ctotal => '',
//                        csubtotal => '',
//                        cship => '',
//                        cstore => 'web',
//                        ccurrency => '',
//                        ctax => '',
//                        ctype => '',
//                        cpromo => '',
//                        ccity => '',
//                        cstate => '',
//                        czip => '',
//                        ccountry => '',
//                        ccustid => ''
//                };
//
//                for my $key(sort keys %{$commerceVars}){
//                        if($outData{$key} ne 'none'){
//                                $extension .= "  b._$key=(typeof b['$outData{$key}']!='undefined')?b['$outData{$key}']:'$commerceVars->{$key}';\n";
//                        }else{
//                                $extension .= "  b._$key='';\n";
//                        }
//                }
//
//                for my $key(qw(cprod cprodname cbrand ccat ccat2 cquan cprice csku cpdisc)){
//                        if($outData{$key} ne 'none'){
//                                if($data->{listtype} eq 'array'){
//                                        $extension .= "  b._$key=(typeof b['$outData{$key}']!='undefined'&&b['$outData{$key}'].length>0)?b['$outData{$key}']:[];\n";
//                                }else{
//                                        $extension .= "  b._$key=(typeof b['$outData{$key}']!='undefined'&&b['$outData{$key}'].length>0)?b['$outData{$key}'].split('$data->{separator}'):[];\n";
//                                }
//                        }else{
//                                $extension .= "  b._$key=[];\n";
//                        }
//                }
//
//                if($data->{pricetype} eq 'line'){
//                        $extension .= "  for(c=0;c<b._cprice.length;c++){try{b._cprice[c]=(parseFloat(b._cprice[c])/parseInt(b._cquan[c])).toFixed(2)}catch(e){}}\n";
//                }
//
//                if($data->{totalcalculate} eq 'yes'){
//                        $extension .= "  b._ctotal=0;for(c=0;c<b._cprice.length;c++){try{b._ctotal+=parseFloat(b._cprice[c])*parseInt(b._cquan[c])}catch(e){};};b._ctotal=b._ctotal.toFixed(2);\n";
//                }
//                $extension .= "  if(b._cprod.length==0){b._cprod=b._csku.slice()};\n";
//                $extension .= "  if(b._cprodname.length==0){b._cprodname=b._csku.slice()};\n";
//                $extension .= "  function tf(a){if(a=='' || isNaN(parseFloat(a))){return a}else{return (parseFloat(a)).toFixed(2)}};\n";
//                $extension .= "  b._ctotal=tf(b._ctotal);b._csubtotal=tf(b._csubtotal);b._ctax=tf(b._ctax);b._cship=tf(b._cship);for(c=0;c<b._cprice.length;c++){b._cprice[c]=tf(b._cprice[c])};for(c=0;c<b._cpdisc.length;c++){b._cpdisc[c]=tf(b._cpdisc[c])};\n";
//                $extension = "function(a,b,c,d){\n" . $extension . "}";
//
//        #100006:Domain-Based Deployment
//        }elsif($id eq '100006'){
//
//                my @qaDomains;
//                my @devDomains;
//                for my $key(keys %{$data}){
//                        if($key =~ /_qadomain$/ && $data->{$key} ne ''){
//                                push @qaDomains, 'location.hostname=="'.$data->{$key}.'"';
//                        }elsif($key =~ /_devdomain$/ && $data->{$key} ne ''){
//                                push @devDomains, 'location.hostname=="'.$data->{$key}.'"';
//                        }
//                }
//
//                if((@devDomains || @qaDomains) && $configData->{config}->{target} eq 'prod'){
//                        my $code = "function utag_condloader(src,a,b){a=document;b=a.createElement('script');b.language='javascript';b.type='text/javascript';b.src=src;a.getElementsByTagName('head')[0].appendChild(b)};\n";
//                        $code .= "var utag_lh=location.hostname;\n";
//
//                        my $baseURI = $configData->{config}->{publish_url};
//                        $baseURI =~ s/^http\://;
//
//                        if(@qaDomains){
//                                if(exists $profileData->{publish}->{publish_qa} && length($profileData->{publish}->{publish_qa})>0){
//                                        $code .= "if(" . (join '||',@qaDomains) . "){utag_condloader('" . $profileData->{publish}->{publish_qa} . "utag.js');utag_condload=true}\n";
//                                }else{
//                                        $code .= "if(" . (join '||',@qaDomains) . "){utag_condloader('$baseURI/$configData->{config}->{account}/$configData->{config}->{profile}/qa/utag.js');utag_condload=true}\n";
//                                }
//                        }
//
//                        if(@devDomains){
//                                $code .= "else " if @qaDomains;
//                                if(exists $profileData->{publish}->{publish_dev} && length($profileData->{publish}->{publish_dev})>0){
//                                        $code .= "if(" . (join '||',@devDomains) . "){utag_condloader('" . $profileData->{publish}->{publish_dev} . "utag.js');utag_condload=true}\n";
//                                }else{
//                                        $code .= "if(" . (join '||',@devDomains) . "){utag_condloader('$baseURI/$configData->{config}->{account}/$configData->{config}->{profile}/dev/utag.js');utag_condload=true}\n";
//                                }
//                        }
//
//                        $extension = "function(a,b){\n" . $code . "}";
//
//                }else{
//                        $extension = ""
//                }
//
//        #100007: Channel Flow
//        }elsif($id eq '100007'){
//
//                #persistence may need persistence text
//                my %channelKeys;
//                for my $key(keys %$data){
//                        if($key =~ /^(\d+)_channelname$/){
//                                my $id = $1;
//                                $channelKeys{$id}->{channel} = $data->{$key};
//                                $channelKeys{$id}->{category} = $data->{$id.'_category'};
//                                $channelKeys{$id}->{filter}->{1}->{source} = $data->{$id.'_source'};
//                                $channelKeys{$id}->{filter}->{1}->{filtertype} = $data->{$id.'_filtertype'};
//                                $channelKeys{$id}->{filter}->{1}->{filter} = $data->{$id.'_filter'};
//                        }elsif($key =~ /^(\d+)_(\d+)_source$/){
//                                my $id = $1;
//                                my $filter = $2;
//                                $channelKeys{$id}->{filter}->{$filter}->{source} = $data->{$id.'_'.$filter.'_source'};
//                                $channelKeys{$id}->{filter}->{$filter}->{filtertype} = $data->{$id.'_'.$filter.'_filtertype'};
//                                $channelKeys{$id}->{filter}->{$filter}->{filter} = $data->{$id.'_'.$filter.'_filter'};
//                        }
//                }
//
//                my @clauses;
//                for my $key(sort { $a <=> $b } keys %channelKeys){
//                        my @andCondition;
//                        for my $filter(sort { $a <=> $b } keys %{$channelKeys{$key}->{filter}}){
//                                $channelKeys{$key}->{filter}->{$filter}->{source} =~ s/^js\.//;
//                                my $condition = generateConditionFilter({
//                                        input => "b['$channelKeys{$key}->{filter}->{$filter}->{source}']",
//                                        operator => $channelKeys{$key}->{filter}->{$filter}->{filtertype},
//                                        filter => $channelKeys{$key}->{filter}->{$filter}->{filter}
//                                });
//                                $log->debug("[100007: Channel Flow] $condition");
//                                if(length($condition)>0){
//                                        push @andCondition, $condition;
//                                }
//                        }
//                        push @clauses, "if(" . (join "&&", @andCondition) . "){o.channel='$channelKeys{$key}->{channel}';o.category='$channelKeys{$key}->{category}'}\n";
//                }
//
//                $extension = "";
//                if(@clauses){
//                        my $exp = '';
//                        my $expDays = 0;
//                        my $hour = 3600000;
//                        my $day = 86400000;
//                        if($data->{persistence} eq 'session'){
//                                $exp = '0';
//                        }elsif($data->{persistence} eq 'days'){
//                                my $offset = $hour;
//                                if($data->{persistencetext}>0){
//                                        $offset = $day*$data->{persistencetext};
//                                        $expDays = $data->{persistencetext};
//                                }
//                                $exp = 'new Date().getTime()+' . $offset;
//                        }else{
//                                $exp = 'new Date().getTime()+' . $day*365;
//                                $expDays = 365;
//                        }
//
//                        $extension  = "function(a,b,c,d,e,f,g,h,i,j,t,o){\n";
//                        $extension .= "  o={channel:'',category:'',exp:$expDays};\n";
//                        $extension .= "  if(a=='view'){\n";
//                        $extension .= "    " . (join "else ", @clauses) ."\n";
//                        $extension .= "    var dd = (isNaN(utag.cfg.domain.replace('.','')))?utag.cfg.domain:location.hostname; dd = ' domain='+dd+'; path=/;';\n"; #Build cookie domain and path string supporting site.com and ip addresses
//                        $extension .= "    if(o.channel!=''){\n";
//                        #update channelflow cookie
//                        $extension .= "     var exp = $exp;\n";
//                        $extension .= "     var expd = new Date($exp).toGMTString();\n";
//                        $extension .= "     if(typeof b['cp.channelflow']=='undefined'&&b['cp.channelflow']!=''){\n";
//                        $extension .= "      b['cp.channelflow']=o.channel+'|'+o.category+'|'+exp;\n";
//                        $extension .= "     }else{\n";
//                        $extension .= "       var ncf = [];\n"; # Array for new entries
//                        $extension .= "       var bcf = b['cp.channelflow'].split(',');\n";
//                        $extension .= "       for(var i=bcf.length-1;i>-1;i--){\n";
//                        $extension .= "         var chan = bcf[i].split('|');\n";
//                        $extension .= "         if(i == (bcf.length-1)&&chan[0]==o.channel&&chan[1]==o.category){\n";#update date on last item if the channels match
//                        $extension .= "           bcf[i]=o.channel+'|'+o.category+'|'+exp;\n";
//                        $extension .= "         }else if(i == (bcf.length-1)&&chan[0]!=o.channel){\n"; #append channel if not last entry
//                        $extension .= "           ncf.push(o.channel+'|'+o.category+'|'+exp);\n";
//                        unless ($data->{persistence} eq 'session') { # The session based cookie will disappear without the need to explicitly delete it.
//                            $extension .= "         }else if(parseInt(chan[2])<=(new Date().getTime()-$day*o.exp)){\n"; #remove expired channel entries
//                            $extension .= "           bcf.splice(i,1);\n";
//                        }
//                        $extension .= "         }\n";
//                        $extension .= "       }\n";
//                        $extension .= "       bcf = bcf.concat(ncf);\n";
//                        $extension .= "       b['cp.channelflow'] = bcf.join();\n";
//                        $extension .= "     }\n";
//                        $extension .= "      document.cookie='channelflow='+b['cp.channelflow']+';'" . ($exp eq '0'?'':"+' expires='+expd+';'") . '+dd;';
//
//                        #Update channeloriginator cookie if it's not set
//                        $extension .= "      if(typeof b['cp.channeloriginator']=='undefined'){";
//                        $extension .= "      b['cp.channeloriginator']=o.channel;";
//                        $extension .= "        document.cookie='channeloriginator='+o.channel+';'" . ($exp eq '0'?'':"+' expires='+expd+';'") . '+dd;';
//                        $extension .= "      }\n";
//
//                        #ALWAYS UPDATE channelcloser cookie
//                        $extension .= "      b['cp.channelcloser']=o.channel;";
//                        $extension .= "      document.cookie='channelcloser='+o.channel+';'" . ($exp eq '0'?'':"+' expires='+expd+';'") . '+dd;';
//                        $extension .= "    }\n";
//
//                        if($data->{populaterule} eq 'all'){
//                                $extension .= "    if(typeof b['cp.channelflow']!='undefined'){\n";
//                        }else{
//                                if($ALLLOADRULES->{$data->{populaterule}}->{init} == 0){
//                                        $extension .= "    $ALLLOADRULES->{$data->{populaterule}}->{condition}\n";
//                                }
//                                $extension .= "    if(typeof b['cp.channelflow']!='undefined'&&utag.cond[$data->{populaterule}]){\n";
//                        }
//
//                        my $influencer = "";
//                        if($data->{closerinfluencer} ne 'yes'){
//                                $influencer .= "e.pop();f.pop();\n";
//                        }
//                        if($data->{originatorinfluencer} ne 'yes'){
//                                $influencer .= "e.shift();f.shift();\n";
//                        }
//
//                        if($data->{repeatresponse} ne 'yes'){
//                                $influencer .= "t={};g=[];h=[];for(i=0;i<e.length;i++){if(t[e[i]+'|'+f[i]]!=1){g.push(e[i]);h.push(f[i])}else t[e[i]+'|'+f[i]]=1};e=g;f=h;\n";
//                        }
//                        $influencer .= "b['channel_influencer']=e.join(',');b['channel_category_influencer']=f.join(',');b['channel_influencer_length']=e.length;\n";
//
//                        if($data->{conversionvariable} =~ /^js\./){
//                                $data->{conversionvariable} =~ s/^js\.//;
//                        }
//
//                        $extension .= "c=b['cp.channelflow'].split(','),e=[],f=[];
//                for(d=0;d<c.length;d++){\ng=c[d].split('|');\nif(!g[2]||g[2]==0||parseInt(g[2])>=(new Date().getTime()-$day*o.exp)){\ne.push(g[0]);\nf.push(g[1])\n}};
//
//                if(e.length>0){
//                    b['channel_originator']=e[0];
//                    b['channel_category_originator']=f[0];
//                    b['channel_closer']=e[e.length-1];
//                    b['channel_category_closer']=f[f.length-1];
//                    b['channel_path']=e.join(',');
//                    b['channel_category_path']=f.join(',');
//                    if(e.length==1){
//                        b['channel_influencer']=e[0];
//                        b['channel_category_influencer']=f[0];
//                    }else{
//                        $influencer
//                    }
//                }else{
//                    b['channel_originator']='';
//                    b['channel_category_originator']='';
//                    b['channel_closer']='';
//                    b['channel_category_closer']='';
//                    b['channel_influencer']='';
//                    b['channel_category_influencer']='';
//                    b['channel_path']='';
//                    b['channel_category_path']='';
//                }
//                var c={o:'$data->{originatorcredit}',i:'$data->{influencercredit}',c:'$data->{closercredit}'},cc={o:0,i:0,c:0},cv=b['$data->{conversionvariable}'];
//                if(parseFloat(cv)>0){
//                    cc.o=cv*parseFloat(c.o/100);
//                    if(b['channel_influencer_length']<1)b['channel_influencer_length']=1;
//                    cc.i=parseFloat((cv*parseFloat(c.i/100))/b['channel_influencer_length']);
//                    cc.c=cv*parseFloat(c.c/100);
//                    for(i in utag.loader.GV(cc)){
//                        cc[i]=cc[i].toFixed(2);
//                    }
//                };\n";
//
//                                if($data->{originatorinfluencer} eq 'yes'){
//                                        $extension .= "cc.o=(parseFloat(cc.o)+parseFloat(cc.i)).toFixed(2);\n";
//                                };
//                                if($data->{closerinfluencer} eq 'yes'){
//                                        $extension .= "cc.c=(parseFloat(cc.c)+parseFloat(cc.i)).toFixed(2);\n";
//                                };
//                                $extension .= "b['channel_influencer_credit']=cc.i;b['channel_originator_credit']=cc.o;b['channel_closer_credit']=cc.c;\n";
//
//                        if(length $data->{code} > 0){
//                                $extension .= "try{var obj=b;$data->{code}\n}catch(e){};\n";
//                        }
//
//                        if($data->{clear} eq 'yes'){
//                              $extension .= "var cd=new Date(0).toGMTString();\n";
//                              $extension .= "document.cookie='channeloriginator=; expires='+cd+';'+dd;\n";
//                              $extension .= "document.cookie='channelflow=; expires='+cd+';'+dd;\n";
//                              $extension .= "document.cookie='channelcloser=; expires='+cd+';'+dd;\n";
//                        }
//                        $extension .= "    }\n";
//                        $extension .= "  }\n";
//                        $extension .= "}\n";
//                }
//
//        #100010:Report Pixel
//        }elsif($id eq '100010'){
//                if($data->{baseurl} && $data->{baseurl} ne ''){
//                        $extension = "function(a,b,c,d,e){utag.rpt.src='$data->{baseurl}'}";
//                }
//
//        #100011:Javascript Code - COMPLETE
//        }elsif($id eq '100011'){
//                $extension = "function(a,b){\n$data->{code}\n}";
//
//        #100012:SiteCatalyst Report Suite - COMPLETE
//        }elsif($id eq '100012'){
//                my $obj;
//                for my $key(keys %$data){
//                        if($key =~ /^(\d+)_source/){
//                                my $x = $1;
//                                $obj->{$x}->{source} = $data->{$key};
//                                $obj->{$x}->{source} =~ s/^js\.//;
//                                $obj->{$x}->{filter} = $data->{$x.'_filter'};
//                                $obj->{$x}->{filtertype} = $data->{$x.'_filtertype'};
//                                $obj->{$x}->{reportsuite} = $data->{$x.'_reportsuite'};
//                        }
//                }
//
//                my @filters;
//                for my $key(sort { $a <=> $b } keys %$obj){
//                        my $condition = generateConditionFilter({ input => "b['$obj->{$key}->{source}']", operator => $obj->{$key}->{filtertype}, filter => $obj->{$key}->{filter} });
//                        if(length($condition)>0){
//                                push @filters, "if(".$condition.")c='$obj->{$key}->{reportsuite}';";
//                        }
//                }
//
//                my $filterSize = @filters;
//                if($filterSize > 0){
//                        $extension = "function(a,b,c){\n";
//                        $extension .= "c='';\n";
//                        $extension .= (join "else ", @filters) . "\n";
//                        $extension .= "if(c.length>0)\n";
//                        $extension .= "s.sa(c);\n";
//                        $extension .= "}";
//                }
//
//        #100013:Previous Page
//        }elsif($id eq '100013'){
//                my $prev = $data->{prevpage};
//                if($prev =~ /^js\./){
//                        $prev =~ s/^js\.//;
//                }
//                $extension = 'function(a,b){b[\''.$data->{output}.'\']=b[\'cp.utag_main__prevpage\'];utag.loader.SC(\'utag_main\',{\'_prevpage\':b[\''.$prev.'\']+\';exp-1h\'})}';
//
//
//        #100014:Google Analytics Social Interaction
//        }elsif($id eq '100014'){
//                $extension = "function(a,b){}";
//
//        #100015:Link Tracking
//        }elsif($id eq '100015'){
//                $extension = "function(a,b){\n";
//                $extension .= "  if(typeof utag.linkHandler=='undefined'){\n";
//                $extension .= "    utag.linkHandler=function(a,b,c,d,e){\n";
//                $extension .= "      if(!a)a=window.event;\n";
//                $extension .= "      if(a.target)b=a.target;\n";
//                $extension .= "      else if(a.srcElement)b=a.srcElement;\n";
//                $extension .= "      if(b.nodeType==3)b=b.parentNode;\n";
//                $extension .= "      if(typeof b=='undefined'||typeof b.tagName=='undefined')return;\n";
//                $extension .= "      c=b.tagName.toLowerCase();\n";
//                $extension .= "      if(c=='body')return;\n";
//                $extension .= "      if(c!='a'){\n";
//                $extension .= "        for(d=0;d<5;d++){\n";
//                $extension .= "          if(typeof b!='undefined'&&b.parentNode)b=b.parentNode;\n";
//                $extension .= "          c=(b!=null&&b.tagName)?b.tagName.toLowerCase():'';\n";
//                $extension .= "          if(c=='a')break;\n";
//                $extension .= "          else if(c == 'body')return;\n";
//                $extension .= "        }\n";
//                $extension .= "      }\n";
//                $extension .= "      if(c!='a')return;\n";
//                $extension .= "      var lt=b.text ? b.text: b.innerText ? b.innerText : '';\n";
//                $extension .= "      if((lt=='' || /^\\s+\$/.test(lt)) && typeof b.innerHTML!='undefined'){\n";
//                $extension .= "        lt=b.innerHTML.toLowerCase();\n";
//                $extension .= "        if(lt.indexOf('<img ')>-1){\n";
//                $extension .= "          d=lt.indexOf('alt=\"');\n";
//                $extension .= "          if(d>-1){\n";
//                $extension .= "            e=lt.indexOf('\"', d + 5);\n";
//                $extension .= "            lt=lt.substring(d+5,e);\n";
//                $extension .= "          }else{\n";
//                $extension .= "            d=lt.indexOf('src=\"');\n";
//                $extension .= "            if(d>-1){\n";
//                $extension .= "              e=lt.indexOf('\"',d+5);\n";
//                $extension .= "              lt=lt.substring(d+5,e);\n";
//                $extension .= "            }\n";
//                $extension .= "          }\n";
//                $extension .= "        }\n";
//                $extension .= "      }\n";
//                $extension .= "      var hr=b.href,hrnq=(b.href.split('?'))[0];\n";
//                if($data->{keepqp} ne 'yes'){
//                        $extension .= "      var obj={link_obj:b,link_text:lt,link_url:hrnq,link_type:'exit link',event_name:'$data->{eventname}'};\n";
//                }else{
//                        $extension .= "      var obj={link_obj:b,link_text:lt,link_url:hr,link_type:'exit link',event_name:'$data->{eventname}'};\n";
//                }
//
//                if(length($data->{internalfilter}) > 0){
//                        $extension .= "c=[location.hostname].concat(('$data->{internalfilter}').split(','));\n";
//                        $extension .= "for(d=0;d<c.length;d++){if(hrnq.indexOf(c[d])>-1){obj.link_type='link';break;}};\n";
//                }
//                if(length($data->{downloadfilter}) > 0){
//                        $extension .= "c=('$data->{downloadfilter}').split(',');\n";
//                        $extension .= "for(d=0;d<c.length;d++){e=new RegExp(c[d]+'\$');if(e.test(hrnq)){obj.link_type='download link';break;}};\n";
//                }
//
//                if(length $data->{code} > 0){
//                        $extension .= "try{var link=b;$data->{code}\n}catch(e){};\n";
//                }
//
//                if($data->{all} ne 'yes'){
//                        my $conditionHash;
//                        for my $key(sort { $a <=> $b }keys %{$data}){
//                                if($key =~ /^source$/){
//                                        $conditionHash->{1}->{source} = $data->{$key};
//                                        $conditionHash->{1}->{filter} = $data->{"filter"};
//                                        $conditionHash->{1}->{filtertype} = $data->{"filtertype"};
//                                }elsif($key =~ /^(\d+)_source$/){
//                                        $conditionHash->{$1}->{source} = $data->{$key};
//                                        $conditionHash->{$1}->{filter} = $data->{$1."_filter"};
//                                        $conditionHash->{$1}->{filtertype} = $data->{$1."_filtertype"};
//                                }
//                        }
//
//                        #generate condition
//                        my @orCondition;
//                        for my $key(sort {$a <=> $b} keys %$conditionHash){
//                                my $source = $conditionHash->{$key}->{source};
//                                $source =~ s/^js\.//;
//                                my $condition = generateConditionFilter({ input => "obj['$source']", operator => $conditionHash->{$key}->{filtertype}, filter => $conditionHash->{$key}->{filter} });
//                                if(length($condition)>0){
//                                        push @orCondition, $condition;
//                                }
//                        }
//
//                        my $conditionSize = @orCondition;
//                        my $condition = 1;
//                        if($conditionSize > 0){
//                                $condition = join '||', @orCondition;
//                        }
//                        $extension .= "    if($condition)utag.link(obj);\n";
//                }else{
//                        $extension .= "    utag.link(obj)\n";
//                }
//                $extension .= "    }\n";
//                $extension .= "  utag.loader.EV(document,'mousedown',utag.linkHandler);\n";
//                $extension .= "  }\n";
//                $extension .= "}\n";
//
//        #100016:Content Modification
//         }elsif($id eq '100016'){
//
//                my %conditionHash;
//                my %modifyData;
//                for my $key(keys %$data){
//                        if($key =~ /^(\d+)_source$/){
//                                my $id = $1;
//                                $conditionHash{$id}->{1}->{source} = $data->{$id.'_source'};
//                                $conditionHash{$id}->{1}->{filtertype} = $data->{$id.'_filtertype'};
//                                $conditionHash{$id}->{1}->{filter} = $data->{$id.'_filter'};
//                        }elsif($key =~ /^(\d+)_(\d+)_source$/){
//                                my $id = $1;
//                                my $filter = $2;
//                                $conditionHash{$id}->{$filter}->{source} = $data->{$id.'_'.$filter.'_source'};
//                                $conditionHash{$id}->{$filter}->{filtertype} = $data->{$id.'_'.$filter.'_filtertype'};
//                                $conditionHash{$id}->{$filter}->{filter} = $data->{$id.'_'.$filter.'_filter'};
//                        }elsif($key =~ /^(\d+)_domid$/){
//                                my $id = $1;
//                                $modifyData{$id}->{id} = $data->{$id.'_domid'};
//                                $modifyData{$id}->{content} = $data->{$id.'_domcontent'};
//                                $modifyData{$id}->{position} = $data->{$id.'_position'};
//                                $modifyData{$id}->{type} = $data->{$id.'_type'};
//                        }
//                }
//
//                #generate condition
//                my @orCondition;
//                for my $key(sort {$a <=> $b} keys %conditionHash){
//                        my @andCondition;
//                        for my $clause(sort {$a <=> $b} keys %{$conditionHash{$key}}){
//                                my $source = $conditionHash{$key}->{$clause}->{source};
//                                $source =~ s/^js\.//;
//                                my $condition = generateConditionFilter({ input => "utag.data['$source']", operator => $conditionHash{$key}->{$clause}->{filtertype}, filter => $conditionHash{$key}->{$clause}->{filter} });
//                                if(length($condition)>0){
//                                        push @andCondition, $condition;
//                                }
//                        }
//
//                        my $conditionSize = @andCondition;
//                        if($conditionSize == 1){
//                                push @orCondition, $andCondition[0];
//                        }else{
//                                push @orCondition, '('.(join '&&', @andCondition).')';
//                        }
//                }
//
//                my $conditionSize = @orCondition;
//                my $condition = 1;
//                if($conditionSize > 0){
//                        $condition = join '||', @orCondition;
//                }
//
//                my $content = "var d,n;";
//                my $xpathFlag = 0;
//                for my $key(sort {$a <=> $b} keys %modifyData){
//                        $modifyData{$key}->{content} =~ s/\n//g;
//                        $content .= "try{n=document.createElement('div');n.innerHTML='$modifyData{$key}->{content}';";
//                        if($modifyData{$key}->{type} eq 'xpath'){
//                                $content .= "d=utag.ebx('$modifyData{$key}->{id}');d=d[0];";
//                                $xpathFlag = 1;
//                        }else{
//                                $content .= "d=document.getElementById('$modifyData{$key}->{id}');";
//                        }
//
//                        if($modifyData{$key}->{position} eq 'insertbefore'){
//                                $content .= "d.parentElement.insertBefore(n,d);";
//
//                        }elsif($modifyData{$key}->{position} eq 'insertafter'){
//                                $content .= "d.parentElement.insertBefore(n,d.nextSibling);";
//
//                        }elsif($modifyData{$key}->{position} eq 'insertfirst'){
//                                $content .= "d.insertBefore(n,d.firstChild);";
//
//                        }elsif($modifyData{$key}->{position} eq 'insertlast'){
//                                $content .= "d.appendChild(n);";
//
//                        }elsif($modifyData{$key}->{position} eq 'replace'){
//                                $content .= "d.innerHTML='$modifyData{$key}->{content}';";
//
//                        }elsif($modifyData{$key}->{position} eq 'replacenode'){
//                                $content .= "d.parentNode.replaceChild(n,d);";
//
//                        }
//                        $content .= "}catch(e){};";
//                }
//
//                if($xpathFlag){
//                        my $ebx = 'utag.ebx=function(p){function NB(a,i){var n="";if(a.length==0){return[document.body,"document.body"]}else if(a.length==1){n="document.body.childNodes["+a[0]+"]"}else{n="document.body.childNodes["+a[0]+"]";for(i=1;i<a.length;i++){n=n+".childNodes["+a[i]+"]"}}return[eval(n),n]}p=p.replace("/html/body/","").split("/");var d="",a=[],e="",n=0,c=0,x=0;for(x=0;x<p.length;x++){if(p[x].indexOf("[")>0){e=p[x].match(/[a-z]+/)[0];n=p[x].match(/[0-9]+/)[0]-1}else{e=p[x]}c=0;d=NB(a)[0].childNodes;for(i=0;i<d.length;i++){if(d[i].localName==e&&n>0){if(c==n){a.push(i);break}c++}else if(d[i].localName==e){a.push(i);break}}n=0}return NB(a)};';
//                        $content = $ebx . $content;
//                }
//
//                if($data->{scope} eq 'footer'){
//                        $extension = "(function(){if($condition){$content}})();";
//                }else{
//                        $extension = "function(a,b){if($condition){$content}}";
//                }
//
//        #100017:Conversion Event Naming
//        }elsif($id eq '100017'){
//                $extension = "function(a,b){}";
//
//        #100018:Segmenting Visitors (Split Segmentation)
//        }elsif($id eq '100018'){
//                my $setToVar = $data->{var};
//                $setToVar =~ s/^cp\.//;
//                my $is_preload=0;
//                if($data->{scope} eq 'preload'){
//                        $is_preload=1;
//                }
//                my $domain = $data->{domain};
//                if($domain eq ""){
//                        if($is_preload){
//                                $domain = '"+location.hostname+"';
//                        }else{
//                                $domain = '"+utag.cfg.domain+"'
//                        }
//                }
//                # reset code generate anonymous function when scope is preloader
//                if($is_preload){
//                        $extension = "(function() {";
//                        $extension .= "var c=' '+document.cookie;if(c.indexOf('" . $setToVar . "=')<0){";
//                }else{
//                        $extension = "function(a,b){";
//                        $extension .= "if(typeof b['cp." . $setToVar . "']=='undefined'||b['cp." . $setToVar . "']==''){";
//                }
//
//                $extension .= "var r=parseInt((Math.random()*100)+1);var s={";
//                my $segHash;
//                for my $key(sort { $a <=> $b }keys %{$data}){
//                        if($key =~ /^(\d+)_(seg.*)$/){
//                                $segHash->{$1}->{$2} = $data->{$key};
//                        }
//                }
//                my @segArray;
//                for my $key(sort keys %{$segHash}){
//                        push(@segArray, "'" . $segHash->{$key}->{"segname"} . "':" . $segHash->{$key}->{"segpercent"});
//                }
//                $extension .= join(",",@segArray);
//                $extension .= "};";
//                $extension .= "var g={},k=0,i;for(i in s){k++;g[i]={};g[i].min=k;k=k+s[i]-1;g[i].max=k;};for(i in g){if(r>=g[i].min&&r<=g[i].max){s=i;break;};};";
//                my $cookieVal="s";
//                if($data->{persistence} eq 'visitor'){
//                        #do not add anything
//                }elsif($data->{persistence} eq 'session'){
//                        $cookieVal .= '+\';exp-session\'';
//                }elsif($data->{persistence} eq 'hours'){
//                        $cookieVal .= '+\';exp-'.$data->{persistencetext}.'h\'';
//                }elsif($data->{persistence} eq 'days'){
//                        $cookieVal .= '+\';exp-'.$data->{persistencetext}.'d\'';
//                }
//                #SET COOKIE
//                if($setToVar =~ /^utag_main_/){
//                        my $x = $setToVar;
//                        $x =~ s/^utag_main_//;
//                        $extension .= 'utag.loader.SC(\'utag_main\',{\'' . $x . '\':' . $cookieVal . '});b[\'cp.' . $setToVar . '\']=s;';
//                }else{
//                        my $cookieExpire='Thu, 31 Dec 2099 00:00:00 GMT';
//                        if($data->{persistence} eq 'session'){
//                                $cookieExpire = '';
//                        }
//                        $extension .= 'document.cookie="' . $setToVar . '="+s+";path=/;domain=' . $domain . ';expires=' . $cookieExpire . '";';
//                        if(!$is_preload){
//                                $extension .= 'b[\'cp.' . $setToVar . '\']=s;';
//                        }
//                }
//                $extension .= "}}";
//                if($is_preload){
//                        $extension .= ")();";
//                }
//
//        #100019:Google WSO MVT
//        }elsif($id eq '100019'){
//                my $setHash;
//                my $conditionHash;
//                for my $key(sort { $a <=> $b }keys %{$data}){
//                        if($key =~ /^(\d+)_domid$/){
//                                $setHash->{$1}->{'domid'} = $data->{$key};
//                                $setHash->{$1}->{'utmxsection'} = $data->{$1 . '_utmxsection'};
//                        }elsif($key =~ /^(\d+)_source$/){
//                                $conditionHash->{$1}->{1}->{source} = $data->{$key};
//                                $conditionHash->{$1}->{1}->{filter} = $data->{$1."_filter"};
//                                $conditionHash->{$1}->{1}->{filtertype} = $data->{$1."_filtertype"};
//                        }elsif($key =~ /^(\d+)_(\d+)_source$/){
//                                $conditionHash->{$1}->{$2}->{source} = $data->{$key};
//                                $conditionHash->{$1}->{$2}->{filter} = $data->{$1."_".$2."_filter"};
//                                $conditionHash->{$1}->{$2}->{filtertype} = $data->{$1."_".$2."_filtertype"};
//                        }
//                }
//
//                #generate condition
//                my @orCondition;
//                for my $key(sort {$a <=> $b} keys %$conditionHash){
//                        my @andCondition;
//                        for my $clause(sort {$a <=> $b} keys %{$conditionHash->{$key}}){
//                                my $source = $conditionHash->{$key}->{$clause}->{source};
//                                $source =~ s/^js\.//;
//                                my $condition = generateConditionFilter({ input => "b['$source']", operator => $conditionHash->{$key}->{$clause}->{filtertype}, filter => $conditionHash->{$key}->{$clause}->{filter} });
//                                if(length($condition)>0){
//                                        push @andCondition, $condition;
//                                }
//                        }
//
//                        my $conditionSize = @andCondition;
//                        if($conditionSize == 1){
//                                push @orCondition, $andCondition[0];
//                        }else{
//                                push @orCondition, '('.(join '&&', @andCondition).')';
//                        }
//                }
//
//                my $conditionSize = @orCondition;
//                my $condition = 1;
//                if($conditionSize > 0){
//                        $condition = join '||', @orCondition;
//                }
//
//                #generate set
//                my @setContent;
//                for my $key(sort { $a <=> $b } keys %$setHash){
//                        push @setContent, "utag.sender[id].setUtmx('$setHash->{$key}->{domid}','$setHash->{$key}->{utmxsection}')";
//                }
//
//                my $setSize = @setContent;
//                my $clauseSize = @orCondition;
//                if($setSize > 0){
//                        $extension = "function(id){var b=utag.data;if($condition){".join(';',@setContent)."}}";
//                }
//
//        #100020:Lookup Table
//        }elsif($id eq '100020'){
//                my $setToVar = $data->{var};
//                my $lookupVar = $data->{varlookup};
//                $setToVar =~ s/^js\.//;
//                $lookupVar =~ s/^js\.//;
//                $extension = "function(a,b,c,d,e,f,g){";
//                $extension .= "d=b['" . $lookupVar . "'];";
//                $extension .= "if(typeof d=='undefined')return;";
//                $extension .= "c=[";
//                my $lookupHash;
//                for my $key(sort { $a <=> $b }keys %{$data}){
//                        if($key =~ /^(\d+)_(value.*)$/){
//                                $lookupHash->{$1}->{$2} = $data->{$key};
//                        }
//                        if($key =~ /^(\d+)_(name.*)$/){
//                                $lookupHash->{$1}->{$2} = $data->{$key};
//                        }
//                }
//                my @lookupArray;
//                for my $key(sort { $a <=> $b }keys %{$lookupHash}){
//                        my $name = $lookupHash->{$key}->{"name"};
//                        if($data->{filtertype} eq "regular_expression"){
//                                if(index($name,'/')==0){
//                                        $name =~ s/^\///;
//                                        $name =~ s/\/$//;
//                                }
//                        }
//                        push(@lookupArray, "{'" . $name . "':'" . $lookupHash->{$key}->{"value"} . "'}");
//                }
//                $extension .= join(",",@lookupArray);
//                $extension .= "];";
//                my $add = "";
//                if($data->{vartype} eq "array"){
//                        $extension .= "for(var h=0;h<d.length;h++){";
//                        $add = "[h]";
//                }
//                $extension .= "var m=false;";
//                $extension .= "for(e=0;e<c.length;e++){for(f in c[e]){";
//                if($data->{filtertype} eq "regular_expression"){
//                        $extension .= "g=new RegExp(f,'i');if(g.test(d" . $add . "))";
//                }elsif($data->{filtertype} eq "contains"){
//                        $extension .= "if(d" . $add .".toString().indexOf(f)>-1)";
//                }else{
//                        $extension .= "if(d" . $add ."==f)";
//                }
//                $extension .= "{b['" . $setToVar . "']" . $add ."=c[e][f];m=true};";
//                $extension .= "};if(m)break};";
//                if(($setToVar ne $lookupVar) or (($setToVar eq $lookupVar) and $data->{settotext} ne '')){
//                        $extension .= "if(!m)b['" . $setToVar . "']" . $add ."='" . $data->{settotext} ."';";
//                }
//                if($data->{vartype} eq "array"){
//                        $extension .= "};";
//                }
//
//                $extension .= "}";
//
//        #100021:JSON Flattener
//        }elsif($id eq '100021'){
//                my $jsonObj = $data->{jsonobject};
//                my $outputObj = $data->{outputobject};
//                if(length $jsonObj > 0 && length $outputObj > 0){
//                        $extension = "function(a,b){";
//                        $extension .= "var utag_jsonflatten=function(a,b,c,d){c={};d=function(a,b,e,f){for(f in a){e=a[f];if(e instanceof Array){for(var i=0;i<e.length;i++)d(e[i],f+i);c[f+'.array-size']=e.length}else if(typeof e=='object')d(e,f);else c[((b!='')?b+'.':'')+f]=e}};d(a,'');return c};";
//                        $extension .= "var utag_jsonflattenobj=utag_jsonflatten($jsonObj,'');";
//                        $extension .= "if(typeof $outputObj=='undefined')$outputObj=utag_jsonflattenobj;";
//                        $extension .= "else{for(var i in utag_jsonflattenobj){if(typeof utag_jsonflattenobj[i]!='function')$outputObj" . "[i]=utag_jsonflattenobj[i]}};";
//                        $extension .= "}";
//                }
//
//        #100022:OPT IN/OUT
//        }elsif($id eq '100022'){
//                $extension = "function(a,b){";
//                $extension .= "utag_trackingOptOut=function(){document.cookie='utag_optout=1;path=/;domain='+utag.cfg.domain+';expires=expires=Thu, 31 Dec 2099 00:00:00 GMT;'};";
//                $extension .= "utag_trackingOptIn=function(){document.cookie='utag_optout=0;path=/;domain='+utag.cfg.domain+';expires=expires=Thu, 31 Dec 2099 00:00:00 GMT;'};";
//                $extension .= "if(document.cookie.indexOf('utag_optout=1')>-1){if(typeof utag_cfg_ovrd=='undefined')utag_cfg_ovrd={};utag_cfg_ovrd['noload']=1}";
//                $extension .= "}";
//
//        #100023:TnT Content Modification
//        }elsif($id eq '100023'){
//            $log->debug("START #100023:TnT Content Modification : scope=$invocationScope; Status : $tntCfg->{status}");
//            if ($invocationScope =~ /sync|preload/) {
//                $log->debug("#100023:invocationScope =~ /sync|preload/");
//                if ($tntCfg->{status} ne 'init' ){
//                    $log->debug("#100023:status ne 'init'");
//                    unless ($tntCfg->{$invocationScope . 'Done'} || ($invocationScope eq 'preload' && $profileData->{publish}->{enable_sync_loader} eq 'yes')) {
//                        $log->debug("#100023: Generate initialization code");
//                        $tntCfg->{$invocationScope . 'Done'} = 1;
//                        $log->debug("START preload logic - build the hider");
//                        my $plSrc .= " ( function(bff,i) {"; # Use mmediately-Invoked Function Expression (IIFE) to eliminate variable pollution
//                        $plSrc .= "bff = { dl : [";
//                        my $cSep = "";
//                        for my $dvId (sort keys %{$tntCfg->{modDivs}}){
//                            $plSrc .= "$cSep\"$dvId\"";
//                            $cSep = ", ";
//                        }
//                        $plSrc .= " ], bn : function(n,s,d,h,t) {";
//                        $plSrc .= "s = document.createElement('style');";
//                        $plSrc .= "d = '#' + n + ' {visibility:hidden;}';";
//                        $plSrc .= "s.setAttribute(\"type\", \"text/css\");";
//                        $plSrc .= "s.setAttribute(\"id\", \"uffs_\"+n);";
//                        $plSrc .= "h = document.getElementsByTagName('head')[0];";
//                        $plSrc .= "h.appendChild(s);";
//                        $plSrc .= "if (s.styleSheet) {";
//                        $plSrc .= "s.styleSheet.cssText = d;"; # Handling for old IE quirks
//                        $plSrc .= "} else {";
//                        $plSrc .= "var t = document.createTextNode(d);"; # Normal logic
//                        $plSrc .= "s.appendChild(t);"; # Normal logic
//                        $plSrc .= "}}};";
//                        $plSrc .= "for (i=0; i<bff.dl.length; i++){";
//                        $plSrc .= "bff.bn(bff.dl[i]);";
//                        $plSrc .= "}";
//                        $plSrc .= "}());";
//                        
//                        $log->debug($plSrc);
//                        $extension = "$plSrc";
//                    }
//                }
//            } elsif ($invocationScope eq "domready"){ # Leave this section - this is a multiscope extension, so it needs to be here.
//                if  ($tntCfg->{status} ne "init")  {
//                    $log->debug("#100023: START domready logic");
//                }
//                # Reserved for future ....
//            } else {
//                $log->debug("#100023: START tag scoped logic - $invocationScope");
//                my %conditionHash;
//                my %modifyData;
//                for my $key(keys %$data){
//                    if($key =~ /^(\d+)_source$/){
//                        my $id = $1;
//                        $conditionHash{$id}->{1}->{source} = $data->{$id.'_source'};
//                        $conditionHash{$id}->{1}->{filtertype} = $data->{$id.'_filtertype'};
//                        $conditionHash{$id}->{1}->{filter} = $data->{$id.'_filter'};
//                    }elsif($key =~ /^(\d+)_(\d+)_source$/){
//                        my $id = $1;
//                        my $filter = $2;
//                        $conditionHash{$id}->{$filter}->{source} = $data->{$id.'_'.$filter.'_source'};
//                        $conditionHash{$id}->{$filter}->{filtertype} = $data->{$id.'_'.$filter.'_filtertype'};
//                        $conditionHash{$id}->{$filter}->{filter} = $data->{$id.'_'.$filter.'_filter'};
//                    }elsif($key =~ /^(\d+)_domid$/){
//                        my $id = $1;
//                        $modifyData{$id}->{id} = $data->{$id.'_domid'};
//                        $modifyData{$id}->{content} = '<div id="' . $data->{$id.'_mboxid'} . '">&nbsp;</div>';
//                        $modifyData{$id}->{type} = $data->{$id.'_type'};
//                        $modifyData{$id}->{mboxid} = $data->{$id.'_mboxid'};
//                        $modifyData{$id}->{params} = $data->{$id.'_mboxparams'};
//                        $modifyData{$id}->{position} = $data->{$id.'_position'};
//                        $modifyData{$id}->{flickerFree} = 'no';
//                        if (defined $data->{$id.'_flickerFree'}){
//                            $modifyData{$id}->{flickerFree} = $data->{$id.'_flickerFree'};
//                        }
//                    }
//                }
//
//                #generate condition
//                my @orCondition;
//                for my $key(sort {$a <=> $b} keys %conditionHash){
//                    my @andCondition;
//                    for my $clause(sort {$a <=> $b} keys %{$conditionHash{$key}}){
//                        my $source = $conditionHash{$key}->{$clause}->{source};
//                        $source =~ s/^js\.//;
//                        my $condition = generateConditionFilter({ input => "utag.data['$source']", operator => $conditionHash{$key}->{$clause}->{filtertype}, filter => $conditionHash{$key}->{$clause}->{filter} });
//                        if(length($condition)>0){
//                            push @andCondition, $condition;
//                        }
//                    }
//
//                    my $conditionSize = @andCondition;
//                    if($conditionSize == 1){
//                        push @orCondition, $andCondition[0];
//                    }else{
//                        push @orCondition, '('.(join '&&', @andCondition).')';
//                    }
//                }
//
//                my $conditionSize = @orCondition;
//                my $condition = 1;
//                if($conditionSize > 0){
//                    $condition = join '||', @orCondition;
//                }
//
//                my $content = "var d,n;";
//                my $zapHideCss = "";
//
//                for my $key(sort {$a <=> $b} keys %modifyData){
//                    $modifyData{$key}->{content} =~ s/\n//g;
//
//                    $content .= "try{n=document.createElement('div');n.innerHTML='$modifyData{$key}->{content}';";
//                    if($modifyData{$key}->{type} eq 'xpath'){
//                        $content .= "d=u.ebx('$modifyData{$key}->{id}');d=d[0];";
//                    }else{
//                        $content .= "d=document.getElementById('$modifyData{$key}->{id}');";
//                    }
//
//                    $log->debug("Check $key: p: $modifyData{$key}->{position} ; f: $modifyData{$key}->{flickerFree}");
//
//                    if ($modifyData{$key}->{position} =~ /^replace/ && $modifyData{$key}->{flickerFree} =~ /yes|on/){
//                        my $tid = $modifyData{$key}->{id};
//                        $tntCfg->{status} = "tagsFound";
//                        $tntCfg->{divStatus}->{$modifyData{$key}->{mboxid}} = "I";
//
//                        if ($invocationScope =~ /\d+/){
//                            push @{$tntCfg->{tagDivLkp}->{$invocationScope}} , $tid;
//                            push @{$tntCfg->{mbxLkp}->{$invocationScope}->{$modifyData{$key}->{mboxid}}} , "uffs_$tid";
//                        }
//                        # Build unhide code to reveal the div if it's condition is false.
//                        $tntCfg->{modDivs}->{$tid} = "try{ var e = document.getElementById('uffs_$tid'); e.parentNode.removeChild(e);}catch(x){}";
//                        $zapHideCss .= $tntCfg->{modDivs}->{$tid};
//                    }
//                    if($modifyData{$key}->{position} eq 'insertbefore'){
//                            $content .= "d.parentElement.insertBefore(n,d);";
//
//                    }elsif($modifyData{$key}->{position} eq 'insertafter'){
//                            $content .= "d.parentElement.insertBefore(n,d.nextSibling);";
//
//                    }elsif($modifyData{$key}->{position} eq 'insertfirst'){
//                            $content .= "d.insertBefore(n,d.firstChild);";
//
//                    }elsif($modifyData{$key}->{position} eq 'insertlast'){
//                            $content .= "d.appendChild(n);";
//
//                    }elsif($modifyData{$key}->{position} eq 'replace'){
//                            $content .= "d.innerHTML='$modifyData{$key}->{content}';";
//                    }elsif($modifyData{$key}->{position} eq 'replacenode'){
//                            $content .= "d.parentNode.replaceChild(n,d);";
//                    }elsif($modifyData{$key}->{position} eq 'replacekeepdefault'){
//                            my $newcontent = $modifyData{$key}->{content};
//                            $newcontent =~ s/\"\>\&nbsp\;\<\//\"\>\'\+l\+\'\<\//;
//                            $content .= "var l=d.innerHTML;d.innerHTML='$newcontent';";
//                    }
//                    my @params = split /&/, $modifyData{$key}->{params};
//                    #Look for the map values and add them to the param String
//                    $content .= "var p=['" . join("','", @params) . "'];";
//                    $content .= "for(var i in utag.loader.GV(u.map)){p.push(u.map[i]+'='+b[i])}";
//                    $content .= "mboxDefine.apply(this,['$modifyData{$key}->{mboxid}','$modifyData{$key}->{mboxid}'].concat(p));";
//                    $content .= "mboxUpdate.apply(this,['$modifyData{$key}->{mboxid}'].concat(p));";
//                    $content .= "}catch(e){};";
//                }
//
//                if ($zapHideCss){ # If flicker free is in use, then add logic to reveal the divs if this condition isn't met.
//                    $extension = "function(a,b,u){if($condition){$content} else { try{ (function(){ $zapHideCss }()) } catch(e) {} } }";    
//                } else {
//                    $extension = "function(a,b,u){if($condition){$content}}";    
//                }
//            }
//        #100024:DATA VALIDATION
//        }elsif($id eq '100024'){
//                my $conditionHash;
//                for my $key(sort { $a <=> $b }keys %{$data}){
//                        if($key =~ /^(\d+)_source$/){
//                                $conditionHash->{$1}->{source} = $data->{$key};
//                                $conditionHash->{$1}->{filter} = $data->{$1."_filter"};
//                                $conditionHash->{$1}->{filtertype} = $data->{$1."_filtertype"};
//                                $conditionHash->{$1}->{label} = $data->{$1."_label"};
//                        }
//                }
//
//                #generate condition
//                my $dataValidation;
//                for my $key(sort {$a <=> $b} keys %$conditionHash){
//                        my $source = $conditionHash->{$key}->{source};
//                        $source =~ s/^js\.//;
//                        my $condition = generateConditionFilter({ input => "b['$source']", operator => $conditionHash->{$key}->{filtertype}, filter => $conditionHash->{$key}->{filter} });
//                        if(length($condition)>0){
//                                $dataValidation->{$conditionHash->{$key}->{label}} = $condition;
//                        }
//                }
//
//                if(keys %{$dataValidation}){
//                        my $clause = "c='$data->{title}';";
//                        $clause .= "d={e:\'\',s:utag.cfg.path+\'utag.js\',l:0,t:\'dv\'};";
//                        for my $label(keys %{$dataValidation}){
//                                $clause .= 'if(' . $dataValidation->{$label} . '){d.e=c+\'/' . $label . '\';utag_err.push(utag.handler.C(d))};';
//                        }
//
//                        $extension = "function(a,b,c,d){";
//                        if($data->{rule} ne "all"){
//                                if($ALLLOADRULES->{$data->{rule}}->{init} == 0){
//                                        $extension .= "    $ALLLOADRULES->{$data->{rule}}->{condition}\n";
//                                }
//                                $extension .= "if(utag.cond[$data->{rule}]){$clause};";
//                        }else{
//                                $extension .= $clause;
//                        }
//                        $extension .= "}";
//                }
//
//        #100025:PATHNAME TOKENIZER
//        }elsif($id eq '100025'){
//                my $dataObj = "utag_data";
//                if($profileData->{publish}->{data_obj} ne ""){
//                        $dataObj = $profileData->{publish}->{data_obj};
//                }
//
//                my $is_preload = 0;
//                if($data->{scope} eq 'preload'){
//                        $is_preload=1;
//                }
//
//                $extension = "function(a,b,c){";
//                $extension .= "if(typeof $dataObj=='undefined')$dataObj={};";
//                #recommendation from Ty
//                #$extension .= "$dataObj=$dataObj||{}";
//                $extension .= "a=location.pathname.split('/');b=(a.length>9)?9:a.length;";
//                $extension .= "for(c=1;c<b;c++){" . $dataObj . "['_pathname'+c]=(typeof a[c]!='undefined')?a[c]:''}";
//                $extension .= "}";
//
//                if($is_preload){
//                        # if preloader wrap in anonymous function so the function is not stripped off
//                        $extension = "($extension)();";
//                }
//
//    # Multi-opt out processing.
//        }elsif($id eq '100026'){
//        if($invocationScope eq 'preload'){
//            my $definedTagsToAdd = "dt = {";
//            my $appendComma = "";
//            my %tagsAddedToO2 = ();
//            if ($optOutCfg{catOptOut}) {
//                for my $i(sort keys %{$optOutCfg{catNameIdMap}}){
//                    if (defined $optOutCfg{catTagsLkp}->{$i}){
//                        $definedTagsToAdd .= "$appendComma c$optOutCfg{catNameIdMap}->{$i}:$data->{optMethod}";
//                        $appendComma = ',';
//                    }
//                }
//            } else {
//                for my $i(sort keys %{$profileData->{manage}}){
//                    my $tagId = $profileData->{manage}->{$i}->{tag_id};
//                    unless ((defined $profileData->{manage}->{$i}->{advconfig_optout} && $profileData->{manage}->{$i}->{advconfig_optout} eq 'no')
//                            || ( ! isActiveTag($profileData->{manage}->{$i},$configData->{config}->{target}))
//                            || (exists $tagsAddedToO2{$tagId}))
//                        {
//                            $tagsAddedToO2{$tagId} = 1;
//                            $definedTagsToAdd .= "$appendComma $tagId:$data->{optMethod}";
//                            $appendComma = ',';
//                        }
//                }
//            }
//            if (defined $data->{singleCookieReq}){
//                $definedTagsToAdd .= "$appendComma 0:$data->{singleCookieReq}";
//            }
//
//            $definedTagsToAdd .= "};";
//            my $code .= '    (function (cv,dt,tl,i,cd,pt,sp) {';
//            $code .= $definedTagsToAdd;
//            $code .= '      if (("" + document.cookie).match("OPTOUTMULTI=([^\S;]*)")) {';
//            $code .= '          cv = unescape(RegExp.$1);';
//            $code .= '          tl = ("" + cv != "") ? (cv).split("|") : [];';
//            $code .= '          for (i = 0; i < tl.length; i += 1) {';
//            $code .= '              var pt = tl[i].split(":");';
//            $code .= '              if (pt.length > 1) {';
//            $code .= '                  dt[pt[0]] = pt[1] * 1;';
//            $code .= '                  if ((pt[0] * 1 == 0) && (pt[1] * 1 == 1)){';
//            $code .= '                      window.utag_cfg_ovrd = window.utag_cfg_ovrd || {};  ';
//            $code .= '                      window.utag_cfg_ovrd.nocookie=true;';
//            $code .= '                  }';
//            $code .= '              } else {';
//            $code .= '                  dt[pt[0]] = 1;';
//            $code .= '              }               ';
//            $code .= '          }';
//            $code .= '        }';
//            $code .= '        tl = [];';
//            $code .= '        for (i in dt) {';
//            $code .= '            if (dt.hasOwnProperty(i)) {';
//            $code .= '                tl.push(i + ":" + dt[i]);';
//            $code .= '            }';
//            $code .= '        }';
//            $code .= '        sp = "";';
//            $code .= '        cv = "";';
//            $code .= '        for (i in tl) {';
//            $code .= '            cv += sp + tl[i];';
//            $code .= '            sp = "|";';
//            $code .= '        }';
//            $code .= '        cd = new Date();';
//            $code .= '        cd.setDate(cd.getDate() + 90);';
//            $code .= '        dt = "" + location.hostname;';
//            $code .= '        tl = dt.split(".");';
//            $code .= '        pt = (/\.co\.|\.com\.|\.org\.|\.edu\.|\.net\.|\.asn\./.test(dt)) ? 3 : 2;';
//            $code .= '        sp = tl.splice(tl.length - pt, pt).join(".");';
//            $code .= '        document.cookie = "OPTOUTMULTI=" + encodeURI(cv) + ";domain=" + sp + ";path=/; expires=" + cd.toGMTString() + ";";';
//            $code .= '    })();';
//
//            $extension = "$code";
//
//        } elsif ($invocationScope eq 'domready') {
//            my %conditionHash;
//            my %modifyData;
//
//            my $baseURI = $configData->{config}->{publish_url};
//            $baseURI =~ s/^http\://;
//            $baseURI .= "/$configData->{config}->{account}/$configData->{config}->{profile}/$configData->{config}->{target}/utag.tagsOptOut.js";
//
//            my $optOutButtonCode = "<button class=\"tealiumMo2TriggerButton\" onclick=\"javascript: (function () {if (typeof __tealiumMo2Div == \\\'undefined\\\') {__tealiumMo2Div = document.createElement(\\\'SCRIPT\\\');__tealiumMo2Div.type = \\\'text/javascript\\\';__tealiumMo2Div.src = \\\'$baseURI?cb=\\\'+Math.random();document.getElementsByTagName(\\\'head\\\')[0].appendChild(__tealiumMo2Div);}else{__tealium.load();}})();\">Modify Privacy Options</button>";
//
//            for my $key(keys %$data){
//                if($key =~ /^(\d+)_source$/){
//                    my $id = $1;
//                    $conditionHash{$id}->{1}->{source} = $data->{$id.'_source'};
//                    $conditionHash{$id}->{1}->{filtertype} = $data->{$id.'_filtertype'};
//                    $conditionHash{$id}->{1}->{filter} = $data->{$id.'_filter'};
//                }elsif($key =~ /^(\d+)_(\d+)_source$/){
//                    my $id = $1;
//                    my $filter = $2;
//                    $conditionHash{$id}->{$filter}->{source} = $data->{$id.'_'.$filter.'_source'};
//                    $conditionHash{$id}->{$filter}->{filtertype} = $data->{$id.'_'.$filter.'_filtertype'};
//                    $conditionHash{$id}->{$filter}->{filter} = $data->{$id.'_'.$filter.'_filter'};
//                }elsif($key =~ /^(\d+)_domid$/){
//                    my $id = $1;
//                    $modifyData{$id}->{id} = $data->{$id.'_domid'};
//                    $modifyData{$id}->{content} = $optOutButtonCode;
//                    $modifyData{$id}->{position} = $data->{$id.'_position'};
//                    $modifyData{$id}->{type} = $data->{$id.'_type'};
//                }
//            }
//
//            #generate condition
//            my @orCondition;
//            for my $key(sort {$a <=> $b} keys %conditionHash){
//                my @andCondition;
//                for my $clause(sort {$a <=> $b} keys %{$conditionHash{$key}}){
//                    my $source = $conditionHash{$key}->{$clause}->{source};
//                    $source =~ s/^js\.//;
//                    my $condition = generateConditionFilter({ input => "utag.data['$source']", operator => $conditionHash{$key}->{$clause}->{filtertype}, filter => $conditionHash{$key}->{$clause}->{filter} });
//                    if(length($condition)>0){
//                        push @andCondition, $condition;
//                    }
//                }
//
//                my $conditionSize = @andCondition;
//                if($conditionSize == 1){
//                    push @orCondition, $andCondition[0];
//                }else{
//                    push @orCondition, '('.(join '&&', @andCondition).')';
//                }
//            }
//
//            my $conditionSize = @orCondition;
//            my $condition = 1;
//            if($conditionSize > 0){
//                $condition = join '||', @orCondition;
//            }
//
//            my $content = "var d,n;";
//            my $xpathFlag = 0;
//            for my $key(sort {$a <=> $b} keys %modifyData){
//                $modifyData{$key}->{content} =~ s/\n//g;
//                $content .= "try{n=document.createElement('div');n.innerHTML='$modifyData{$key}->{content}';";
//                if($modifyData{$key}->{type} eq 'xpath'){
//                    $content .= "d=utag.ebx('$modifyData{$key}->{id}');d=d[0];";
//                    $xpathFlag = 1;
//                }else{
//                    $content .= "d=document.getElementById('$modifyData{$key}->{id}');";
//                }
//
//                if($modifyData{$key}->{position} eq 'insertbefore'){
//                    $content .= "d.parentElement.insertBefore(n,d);";
//                }elsif($modifyData{$key}->{position} eq 'insertafter'){
//                    $content .= "d.parentElement.insertBefore(n,d.nextSibling);";
//
//                }elsif($modifyData{$key}->{position} eq 'insertfirst'){
//                    $content .= "d.insertBefore(n,d.firstChild);";
//
//                }elsif($modifyData{$key}->{position} eq 'insertlast'){
//                    $content .= "d.appendChild(n);";
//                }elsif($modifyData{$key}->{position} eq 'replace'){
//                    $content .= "d.innerHTML='$modifyData{$key}->{content}';";
//                }elsif($modifyData{$key}->{position} eq 'replacenode'){
//                    $content .= "d.parentNode.replaceChild(n,d);";
//                }
//                $content .= "}catch(e){};";
//            }
//
//            if($xpathFlag){
//                my $ebx = 'utag.ebx=function(p){function NB(a,i){var n="";if(a.length==0){return[document.body,"document.body"]}else if(a.length==1){n="document.body.childNodes["+a[0]+"]"}else{n="document.body.childNodes["+a[0]+"]";for(i=1;i<a.length;i++){n=n+".childNodes["+a[i]+"]"}}return[eval(n),n]}p=p.replace("/html/body/","").split("/");var d="",a=[],e="",n=0,c=0,x=0;for(x=0;x<p.length;x++){if(p[x].indexOf("[")>0){e=p[x].match(/[a-z]+/)[0];n=p[x].match(/[0-9]+/)[0]-1}else{e=p[x]}c=0;d=NB(a)[0].childNodes;for(i=0;i<d.length;i++){if(d[i].localName==e&&n>0){if(c==n){a.push(i);break}c++}else if(d[i].localName==e){a.push(i);break}}n=0}return NB(a)};';
//                $content = $ebx . $content;
//            }
//
//            if($data->{scope} eq 'footer'){
//                $extension = "(function(){if($condition){$content}})();";
//            }else{
//                $extension = "function(a,b){if($condition){$content}}";
//            }
//
//            # Build the js file that is called by the button.
//            my $outfile = "$configData->{config}->{revision_publish_dir}/utag.tagsOptOut.js";
//            my $template = getTemplate($configData, $profileData, "utag.tagsOptOut.js", "utag.tagsOptOut.js");
//
//            my $utuiCfgSvrData = {};
//            my $utuiCfgSvrFlNm = "$configData->{config}->{js_dir}/utui.config.server.json";
//            if(-e $utuiCfgSvrFlNm){
//                $utuiCfgSvrData = Tealium::UTUI::util::readJSONFile($utuiCfgSvrFlNm);
//            }
//
//            if(-e "$configData->{config}->{account_dir}/$configData->{config}->{account}/templates/$configData->{config}->{profile}/utui.config.server.json"){
//                my $customTemplateData = Tealium::UTUI::util::readJSONFile("$configData->{config}->{account_dir}/$configData->{config}->{account}/templates/$configData->{config}->{profile}/utui.config.server.json");
//                for my $i(keys %$customTemplateData){
//                    for my $j(keys %{$customTemplateData->{$i}}){
//                        $utuiCfgSvrData->{$i}->{$j} = $customTemplateData->{$i}->{$j};
//                    }
//                }
//            }
//
//            my $date = Tealium::UTUI::util::getDateId();
//            my $year = substr($date,0,4);
//
//            if(open IN, $template){
//                my @content = <IN>;
//                close IN;
//
//                for(my $i=0;$i<@content; $i++){
//                    if($content[$i] =~ /##(UT\w+?)##/){
//                        my $tag = $1;
//
//                        switch ($tag) {
//                            case "UTVERSION" {
//                                $content[$i] =~ s/##UTYEAR##/$year/;
//                                $content[$i] =~ s/##UTVERSION##/$date/;
//                            }
//
//                            case "UTTAGSOPTOUT" {
//                                my $utExtendContent = '{visitorSelectionType: ' . $data->{visitorSelectionType} . ', optMethod: ' . $data->{optMethod} . ', ';
//                                my $appendComma = "";
//                                unless ($data->{visitorSelectionType} eq "1") {
//                                    $utExtendContent .= 'tagList : [';
//                                    my %tagsAddedToO2 = ();
//                                    for my $i(sort keys %{$profileData->{manage}}){
//                                        my $tagId = $profileData->{manage}->{$i}->{tag_id};
//                                        unless ((defined $profileData->{manage}->{$i}->{advconfig_optout} && $profileData->{manage}->{$i}->{advconfig_optout} eq 'no')
//                                                || ( ! isActiveTag($profileData->{manage}->{$i},$configData->{config}->{target}))
//                                                || (exists $tagsAddedToO2{$tagId})) {
//                                                $tagsAddedToO2{$tagId} = 1;
//                                                my $group = {
//                                                    group_id => $utuiCfgSvrData->{$tagId}->{group},
//                                                    group => $utuiCfgSvrData->{'GROUP_' . $utuiCfgSvrData->{$tagId}->{group}}->{text},
//                                                    group_sort => $utuiCfgSvrData->{'GROUP_' . $utuiCfgSvrData->{$tagId}->{group}}->{sort}
//                                                };
//
//                                                $utExtendContent .= $appendComma . '{ id: "' . $tagId . '", name: "' . $profileData->{manage}->{$i}->{tag_name} .'", description: "' . $utuiCfgSvrData->{$tagId}->{description}
//                                                    . '", website : "' . $utuiCfgSvrData->{$tagId}->{website} . '", logo : "' . $utuiCfgSvrData->{$tagId}->{logo}
//                                                    . '", group_id : "' . $group->{group_id} . '", group : "' . $group->{group} . '", group_sort : "' . $group->{group_sort}
//                                                    . '" }';
//                                                $appendComma = ',';
//                                            }
//                                    }
//                                    $utExtendContent .= ']';
//                                } else {
//                                    my %activeCatIdMap;
//                                    for my $cn(keys %{$optOutCfg{catTagsLkp}}){
//                                        $activeCatIdMap{$cn} = $optOutCfg{catNameIdMap}->{$cn};
//                                    }
//                                    # To support foreign characters in category names and descriptions, use HTML encoding and create a new opt out hash
//                                    # Clean out any categories that are not in use, so they don't show up in the widget.
//                                    my $cleanHTMLCats = {};
//                                    for my $acn(keys %{$optOutCfg{catOptOutData}}){
//                                        if (defined $optOutCfg{catTagsLkp}->{$acn}){
//                                            $optOutCfg{catOptOutData}->{$acn}->{desc} = encode_entities($optOutCfg{catOptOutData}->{$acn}->{desc});
//                                            my $htmlEncKey = encode_entities($acn);
//                                            $cleanHTMLCats->{$htmlEncKey} = $optOutCfg{catOptOutData}->{$acn};
//                                        }
//                                    }
//                                    $utExtendContent .= "activeCatIdMap : ". to_json(\%activeCatIdMap, {canonical => 1}) . ", ";
//                                    $utExtendContent .= "categories : ". to_json($cleanHTMLCats, {canonical => 1});
//                                }
//                                $utExtendContent .= '}';
//                                $content[$i] =~ s/##UTTAGSOPTOUT##/$utExtendContent/g;
//                            }
//
//                        }
//                    }
//                }
//
//                if(open OUT, ">$outfile"){
//                    if(defined $profileData->{publish}->{minify} && $profileData->{publish}->{minify} eq "yes"){
//                        eval {
//                                my $minifiedContent = "//tealium universal tag - utag.$id ut4.0.$date, Copyright $year Tealium.com Inc. All Rights Reserved.\n";
//                                $minifiedContent .= minify( input => (join "", @content) );
//                                print OUT $minifiedContent;
//                        };
//                        if ($@){
//                                $minifyFail = "utag.tagsOptOut.js";
//                        }
//                    }else{
//                        print OUT (join "", @content);
//                    }
//                    close OUT;
//                    $EXTRA_PUBLISH_FILES{"utag.tagsOptOut.js"} = 1;
//                    my $templateFile = "$configData->{config}->{account_dir}/$configData->{config}->{account}/templates/$configData->{config}->{profile}/utag.tagsOptOut.js";
//                    #MOVE THIS FILE TO THE TEMPLATES DIRECTORY
//                    if(!-e "$templateFile"){
//                        copy("$configData->{config}->{template_dir}/utag.tagsOptOut.js", $templateFile);
//                    }
//
//                }else{
//                    $log->debug("createSender: cant write outfile: $outfile");
//                    return undef;
//                }
//
//            } else {
//                $log->debug("createSender: $template template not found.");
//                return undef;
//            }
//        }
//    }elsif($id eq '100027'){
//
//                my $setHash;
//
//                for my $key(sort { $a <=> $b }keys %{$data}){
//                        if($key =~ /^(\d+)_(set.*)$/){
//                                $setHash->{$1}->{$2} = $data->{$key};
//                        }
//                }
//
//                my @setContent;
//                for my $key(sort { $a <=> $b } keys %$setHash){
//                        $setHash->{$key}->{set} =~ s/^js\.//;
//                        if($setHash->{$key}->{setoption} eq 'var'){
//                                $setHash->{$key}->{settovar} =~ s/^js\.//;
//                                push @setContent, "$setHash->{$key}->{set}:utag.data['$setHash->{$key}->{settovar}']";
//                        }elsif($setHash->{$key}->{setoption} eq 'text'){
//                                $setHash->{$key}->{settotext} =~ s/[']/\\\'/g;
//                                push @setContent, "$setHash->{$key}->{set}:'$setHash->{$key}->{settotext}'";
//                        }elsif($setHash->{$key}->{setoption} eq 'code'){
//                                $setHash->{$key}->{settotext} =~ s/[;]//g;
//                                $setHash->{$key}->{settotext} =~ s/\/\/.*//g;
//                                push @setContent, "$setHash->{$key}->{set}:$setHash->{$key}->{settotext}";
//                        }
//                }
//
//
//                $extension = "function(a,b){";
//                if(!defined $GLOBAL_STATE{$id}->{runonce}){
//                        $extension .= 'if(typeof utag.runonce==\'undefined\')utag.runonce={};';
//                        $extension .= 'utag.jdh=function(h,i,j,k){h=utag.jdhc.length;if(h==0)window.clearInterval(utag.jdhi);else{for(i=0;i<h;i++){j=utag.jdhc[i];k=jQuery(j.i).is(":visible")?1:0;';
//                        $extension .= 'if(k!=j.s){if(j.e==(j.s=k))jQuery(j.i).trigger(j.e?"afterShow":"afterHide")}}}};utag.jdhi=window.setInterval(utag.jdh, 250);';
//                        $extension .= 'utag.jdhc=[];';
//                        $GLOBAL_STATE{$id}->{runonce} = 1;
//                }
//
//                my $handlerContent;
//                if($data->{handler} eq "custom"){
//                        $handlerContent = $data->{code};
//                }else{
//                        $handlerContent = join ",",@setContent;
//                        $handlerContent = "utag.$data->{handler}({ " . $handlerContent . " })";
//                }
//
//                if(length $handlerContent>0){
//                        $extension .= "\nif(typeof utag.runonce[$data->{_id}]=='undefined'){utag.runonce[$data->{_id}]=1;";
//            $data->{domid} =~ s/'/"/g;
//                        if($data->{event} eq "show"){
//                                $extension .= "utag.jdhc.push({i:'$data->{domid}',e:1});";
//                                $extension .= "jQuery('" . $data->{domid} ."').on('afterShow',function(e){if(jQuery('" . $data->{domid} . "').is(':visible')){";
//                                $extension .= $handlerContent;
//                                $extension .= "}})";
//                        }elsif($data->{event} eq "hide"){
//                                $extension .= "utag.jdhc.push({i:'$data->{domid}',e:0});";
//                                $extension .= "jQuery('" . $data->{domid} ."').on('afterHide',function(e){if(!jQuery('" . $data->{domid} . "').is(':visible')){";
//                                $extension .= $handlerContent;
//                                $extension .= "}})\n";
//                        }elsif($data->{event} eq "click"){
//                                $extension .= "jQuery('" . $data->{domid} ."').on('click',function(e){";
//                                $extension .= $handlerContent;
//                                $extension .= "})";
//                        }elsif($data->{event} eq "mousedown"){
//                                $extension .= "jQuery('" . $data->{domid} ."').on('mousedown',function(e){";
//                                $extension .= $handlerContent;
//                                $extension .= "});";
//                        }elsif($data->{event} eq "mouseup"){
//                                $extension .= "jQuery('" . $data->{domid} ."').on('mouseup',function(e){";
//                                $extension .= $handlerContent;
//                                $extension .= "});";
//                        }elsif($data->{event} eq "mouseover"){
//                                $extension .= "jQuery('" . $data->{domid} ."').on('mouseover',function(e){";
//                                $extension .= $handlerContent;
//                                $extension .= "});";
//                        }elsif($data->{event} eq "change"){
//                                $extension .= "jQuery('" . $data->{domid} ."').on('change',function(e){";
//                                $extension .= $handlerContent;
//                                $extension .= "});";
//                        }elsif($data->{event} eq "blur"){
//                                $extension .= "jQuery('" . $data->{domid} ."').on('blur',function(e){";
//                                $extension .= $handlerContent;
//                                $extension .= "});";
//                        }elsif($data->{event} eq "focus"){
//                                $extension .= "jQuery('" . $data->{domid} ."').on('focus',function(e){";
//                                $extension .= $handlerContent;
//                                $extension .= "});";
//                        }
//                        $extension .= "}\n";
//                }
//                $extension .= "}\n";
//        }elsif($id eq '100029'){
//
//                my $setHash;
//
//                for my $key(sort { $a <=> $b }keys %{$data}){
//                        if($key =~ /^(\d+)_(set.*)$/){
//                                $setHash->{$1}->{$2} = $data->{$key};
//                        }
//                }
//
//                my @setContent;
//                for my $key(sort { $a <=> $b } keys %$setHash){
//                        $setHash->{$key}->{set} =~ s/^js\.//;
//                        if($setHash->{$key}->{setoption} eq 'var'){
//                            $setHash->{$key}->{settovar} =~ s/^js\.//;
//                                push @setContent, "$setHash->{$key}->{set}:utag.data['$setHash->{$key}->{settovar}']";
//                        }elsif($setHash->{$key}->{setoption} eq 'text'){
//                                $setHash->{$key}->{settotext} =~ s/[']/\\\'/g;
//                                push @setContent, "$setHash->{$key}->{set}:'$setHash->{$key}->{settotext}'";
//                        }elsif($setHash->{$key}->{setoption} eq 'code'){
//                                $setHash->{$key}->{settotext} =~ s/[;]//g;
//                                $setHash->{$key}->{settotext} =~ s/\/\/.*//g;
//                                push @setContent, "$setHash->{$key}->{set}:$setHash->{$key}->{settotext}";
//                        }
//                }
//
//
//        $extension = "function(a,b){";
//        if(!defined $GLOBAL_STATE{$id}->{runonce}){
//            $extension .= 'if(typeof utag.runonce==\'undefined\')utag.runonce={};';
//            $GLOBAL_STATE{$id}->{runonce} = 1;
//        }
//
//        my $handlerContent;
//        if($data->{handler} eq "custom"){
//            $handlerContent = $data->{code};
//        }else{
//            $handlerContent = join ",",@setContent;
//            $handlerContent = "utag.$data->{handler}({ " . $handlerContent . " })";
//        }
//
//        if(length $handlerContent>0){
//            $data->{domid} =~ s/'/"/g;
//            $extension .= "\nif(typeof utag.runonce[$data->{_id}]=='undefined'){utag.runonce[$data->{_id}]=1;";
//                        if($data->{event} eq "click"){
//                                $extension .= "jQuery('" . $data->{domid} ."').bind('click',function(e){";
//                                $extension .= $handlerContent;
//                                $extension .= "})";
//                        }elsif($data->{event} eq "mousedown"){
//                                $extension .= "jQuery('" . $data->{domid} ."').bind('mousedown',function(e){";
//                                $extension .= $handlerContent;
//                                $extension .= "});";
//                        }elsif($data->{event} eq "mouseup"){
//                                $extension .= "jQuery('" . $data->{domid} ."').bind('mouseup',function(e){";
//                                $extension .= $handlerContent;
//                                $extension .= "});";
//                        }elsif($data->{event} eq "mouseover"){
//                                $extension .= "jQuery('" . $data->{domid} ."').bind('mouseover',function(e){";
//                                $extension .= $handlerContent;
//                                $extension .= "});";
//                        }elsif($data->{event} eq "change"){
//                                $extension .= "jQuery('" . $data->{domid} ."').bind('change',function(e){";
//                                $extension .= $handlerContent;
//                                $extension .= "});";
//                        }elsif($data->{event} eq "blur"){
//                                $extension .= "jQuery('" . $data->{domid} ."').bind('blur',function(e){";
//                                $extension .= $handlerContent;
//                                $extension .= "});";
//                        }elsif($data->{event} eq "focus"){
//                                $extension .= "jQuery('" . $data->{domid} ."').bind('focus',function(e){";
//                                $extension .= $handlerContent;
//                                $extension .= "});";
//                        }
//            $extension .= "}\n";
//        }
//        $extension .= "}\n";
//        }elsif($id eq '100030'){
//            $oldMobileExtVars = "; var nativeAppLiveHandlerData = " . to_json ($data, {canonical => 1});
//        }elsif($id eq '100031'){
//            my $currencyConvertHash;
//            for my $key(sort { $a <=> $b }keys %{$data}){
//                if($key =~ /^(\d+)_(\w+)$/){
//                    $currencyConvertHash->{$1}->{$2} = $data->{$key};
//                }
//            }
//            $extension = "function(a,b){\n";
//            for my $key(sort keys %$currencyConvertHash){
//                if($currencyConvertHash->{$key}->{loadrule} ne 'all'){
//                    $extension .= "  if(utag.cond[$currencyConvertHash->{$key}->{loadrule}]==1)";
//                }
//                my $var = $currencyConvertHash->{$key}->{currencyconvertvariable};
//                $var =~ s/^js\.//;
//                $extension .= "    b[\"$var\"] = tealiumiq_currency.convert(b[\"$var\"],\"$currencyConvertHash->{$key}->{fromcurrency}\",\"$currencyConvertHash->{$key}->{tocurrency}\");\n";
//            }
//            $extension .= "}\n";
//        }elsif($id eq '100032'){
//
//                my $setHash;
//
//                for my $key(sort { $a <=> $b }keys %{$data}){
//                        if($key =~ /^(\d+)_(set.*)$/){
//                                $setHash->{$1}->{$2} = $data->{$key};
//                        }
//                }
//
//                my @setContent;
//                for my $key(sort { $a <=> $b } keys %$setHash){
//                        $setHash->{$key}->{set} =~ s/^js\.//;
//                        if($setHash->{$key}->{setoption} eq 'var'){
//                            $setHash->{$key}->{settovar} =~ s/^js\.//;
//                                push @setContent, "$setHash->{$key}->{set}:utag.data['$setHash->{$key}->{settovar}']";
//                        }elsif($setHash->{$key}->{setoption} eq 'text'){
//                                $setHash->{$key}->{settotext} =~ s/[']/\\\'/g;
//                                push @setContent, "$setHash->{$key}->{set}:'$setHash->{$key}->{settotext}'";
//                        }elsif($setHash->{$key}->{setoption} eq 'code'){
//                                $setHash->{$key}->{settotext} =~ s/[;]//g;
//                                $setHash->{$key}->{settotext} =~ s/\/\/.*//g;
//                                push @setContent, "$setHash->{$key}->{set}:$setHash->{$key}->{settotext}";
//                        }
//                }
//
//
//        $extension = "function(a,b){";
//        if(!defined $GLOBAL_STATE{$id}->{runonce}){
//            $extension .= 'if(typeof utag.runonce==\'undefined\')utag.runonce={};';
//            $extension .= 'utag.jdh=function(h,i,j,k){h=utag.jdhc.length;if(h==0)window.clearInterval(utag.jdhi);else{for(i=0;i<h;i++){j=utag.jdhc[i];k=jQuery(j.i).is(":visible")?1:0;';
//                        $extension .= 'if(k!=j.s){if(j.e==(j.s=k))jQuery(j.i).trigger(j.e?"afterShow":"afterHide")}}}};utag.jdhi=window.setInterval(utag.jdh, 250);';
//            $extension .= 'utag.jdhc=[];';
//            $GLOBAL_STATE{$id}->{runonce} = 1;
//        }
//
//        my $handlerContent;
//        if($data->{handler} eq "custom"){
//            $handlerContent = $data->{code};
//        }else{
//            $handlerContent = join ",",@setContent;
//            $handlerContent = "utag.$data->{handler}({ " . $handlerContent . " })";
//        }
//
//        if(length $handlerContent>0){
//            $extension .= "\nif(typeof utag.runonce[$data->{_id}]=='undefined'){utag.runonce[$data->{_id}]=1;";
//            $data->{selector} =~ s/'/"/g;
//            my $primarySelector = "document.body";
//            my $selector = "'" . $data->{selector} . "'";
//            
//            if( $data->{primaryselector} ne "" ){
//                $primarySelector = "'" . $data->{primaryselector} . "'";
//            }
//            if($data->{event} eq "show"){
//                $extension .= "utag.jdhc.push({i:" . $selector . ",e:1});";
//                                $extension .= "jQuery(" . $primarySelector .").on('afterShow'," . $selector . ", function(e){if(jQuery(" . $selector . ").is(':visible')){";
//                                $extension .= $handlerContent;
//                                $extension .= "}})";
//                        }elsif($data->{event} eq "hide"){
//                                $extension .= "utag.jdhc.push({i:" . $selector . ",e:0});";
//                                $extension .= "jQuery(" . $primarySelector .").on('afterHide'," . $selector . ", function(e){if(!jQuery(" . $selector . ").is(':visible')){";
//                                $extension .= $handlerContent;
//                                $extension .= "}})\n";
//                        }elsif($data->{event} eq "click"){
//                                $extension .= "jQuery(" . $primarySelector .").on('click'," . $selector . ", function(e){";
//                                $extension .= $handlerContent;
//                                $extension .= "})";
//                        }elsif($data->{event} eq "mousedown"){
//                                $extension .= "jQuery(" . $primarySelector .").on('mousedown'," . $selector . ", function(e){";
//                                $extension .= $handlerContent;
//                                $extension .= "});";
//                        }elsif($data->{event} eq "mouseup"){
//                                $extension .= "jQuery(" . $primarySelector .").on('mouseup'," . $selector . ", function(e){";
//                                $extension .= $handlerContent;
//                                $extension .= "});";
//                        }elsif($data->{event} eq "mouseover"){
//                                $extension .= "jQuery(" . $primarySelector .").on('mouseover'," . $selector . ", function(e){";
//                                $extension .= $handlerContent;
//                                $extension .= "});";
//                        }elsif($data->{event} eq "change"){
//                                $extension .= "jQuery(" . $primarySelector .").on('change'," . $selector . ", function(e){";
//                                $extension .= $handlerContent;
//                                $extension .= "});";
//                        }elsif($data->{event} eq "blur"){
//                                $extension .= "jQuery(" . $primarySelector .").on('blur'," . $selector . ", function(e){";
//                                $extension .= $handlerContent;
//                                $extension .= "});";
//                        }elsif($data->{event} eq "focus"){
//                                $extension .= "jQuery(" . $primarySelector .").on('focus'," . $selector . ", function(e){";
//                                $extension .= $handlerContent;
//                                $extension .= "});";
//                        }
//            $extension .= "}\n";
//        }
//        $extension .= "}\n";
//        }elsif($id eq '100033'){
//           
//            $extension = "function(a,b){";
//                $extension .="/** A JavaScript implementation of the RSA Data Security, Inc. MD5 Message\n";
//                $extension .=" * Digest Algorithm, as defined in RFC 1321.\n";
//                $extension .=" * Version 2.2 Copyright (C) Paul Johnston 1999 - 2009\n";
//                $extension .=" * Other contributors: Greg Holt, Andrew Kepert, Ydnar, Lostinet\n";
//                $extension .=" * Distributed under the BSD License\n";
//                $extension .=" * See http://pajhome.org.uk/crypt/md5 for more info.*/\n";
//                $extension .='var hexcase=0;var b64pad="";function hex_md5(a){return rstr2hex(rstr_md5(str2rstr_utf8(a)))}function rstr_md5(a){return binl2rstr(binl_md5(rstr2binl(a),a.length*8))}function rstr2hex(c){try{hexcase}catch(g){hexcase=0}var f=hexcase?"0123456789ABCDEF":"0123456789abcdef";';
//                $extension .='var b="";var a;for(var d=0;d<c.length;d++){a=c.charCodeAt(d);b+=f.charAt((a>>>4)&15)+f.charAt(a&15)}return b}function str2rstr_utf8(c){var b="";var d=-1;var a,e;while(++d<c.length){a=c.charCodeAt(d);e=d+1<c.length?c.charCodeAt(d+1):0;';
//                $extension .='if(55296<=a&&a<=56319&&56320<=e&&e<=57343){a=65536+((a&1023)<<10)+(e&1023);d++}if(a<=127){b+=String.fromCharCode(a)}else{if(a<=2047){b+=String.fromCharCode(192|((a>>>6)&31),128|(a&63))}else{if(a<=65535){b+=String.fromCharCode(224|((a>>>12)&15),128|((a>>>6)&63),128|(a&63))}';
//                $extension .='else{if(a<=2097151){b+=String.fromCharCode(240|((a>>>18)&7),128|((a>>>12)&63),128|((a>>>6)&63),128|(a&63))}}}}}return b}function rstr2binl(b){var a=Array(b.length>>2);for(var c=0;c<a.length;c++){a[c]=0}for(var c=0;c<b.length*8;';
//                $extension .='c+=8){a[c>>5]|=(b.charCodeAt(c/8)&255)<<(c%32)}return a}function binl2rstr(b){var a="";for(var c=0;c<b.length*32;c+=8){a+=String.fromCharCode((b[c>>5]>>>(c%32))&255)}return a}function binl_md5(p,k){p[k>>5]|=128<<((k)%32);p[(((k+64)>>>9)<<4)+14]=k;';
//                $extension .='var o=1732584193;var n=-271733879;var m=-1732584194;var l=271733878;for(var g=0;g<p.length;g+=16){var j=o;var h=n;var f=m;var e=l;o=md5_ff(o,n,m,l,p[g+0],7,-680876936);l=md5_ff(l,o,n,m,p[g+1],12,-389564586);m=md5_ff(m,l,o,n,p[g+2],17,606105819);';
//                $extension .='n=md5_ff(n,m,l,o,p[g+3],22,-1044525330);o=md5_ff(o,n,m,l,p[g+4],7,-176418897);l=md5_ff(l,o,n,m,p[g+5],12,1200080426);m=md5_ff(m,l,o,n,p[g+6],17,-1473231341);n=md5_ff(n,m,l,o,p[g+7],22,-45705983);o=md5_ff(o,n,m,l,p[g+8],7,1770035416);l=md5_ff(l,o,n,m,p[g+9],12,-1958414417);';
//                $extension .='m=md5_ff(m,l,o,n,p[g+10],17,-42063);n=md5_ff(n,m,l,o,p[g+11],22,-1990404162);o=md5_ff(o,n,m,l,p[g+12],7,1804603682);l=md5_ff(l,o,n,m,p[g+13],12,-40341101);m=md5_ff(m,l,o,n,p[g+14],17,-1502002290);n=md5_ff(n,m,l,o,p[g+15],22,1236535329);o=md5_gg(o,n,m,l,p[g+1],5,-165796510);';
//                $extension .='l=md5_gg(l,o,n,m,p[g+6],9,-1069501632);m=md5_gg(m,l,o,n,p[g+11],14,643717713);n=md5_gg(n,m,l,o,p[g+0],20,-373897302);o=md5_gg(o,n,m,l,p[g+5],5,-701558691);l=md5_gg(l,o,n,m,p[g+10],9,38016083);m=md5_gg(m,l,o,n,p[g+15],14,-660478335);n=md5_gg(n,m,l,o,p[g+4],20,-405537848);';
//                $extension .='o=md5_gg(o,n,m,l,p[g+9],5,568446438);l=md5_gg(l,o,n,m,p[g+14],9,-1019803690);m=md5_gg(m,l,o,n,p[g+3],14,-187363961);n=md5_gg(n,m,l,o,p[g+8],20,1163531501);o=md5_gg(o,n,m,l,p[g+13],5,-1444681467);l=md5_gg(l,o,n,m,p[g+2],9,-51403784);m=md5_gg(m,l,o,n,p[g+7],14,1735328473);';
//                $extension .='n=md5_gg(n,m,l,o,p[g+12],20,-1926607734);o=md5_hh(o,n,m,l,p[g+5],4,-378558);l=md5_hh(l,o,n,m,p[g+8],11,-2022574463);m=md5_hh(m,l,o,n,p[g+11],16,1839030562);n=md5_hh(n,m,l,o,p[g+14],23,-35309556);o=md5_hh(o,n,m,l,p[g+1],4,-1530992060);l=md5_hh(l,o,n,m,p[g+4],11,1272893353);';
//                $extension .='m=md5_hh(m,l,o,n,p[g+7],16,-155497632);n=md5_hh(n,m,l,o,p[g+10],23,-1094730640);o=md5_hh(o,n,m,l,p[g+13],4,681279174);l=md5_hh(l,o,n,m,p[g+0],11,-358537222);m=md5_hh(m,l,o,n,p[g+3],16,-722521979);n=md5_hh(n,m,l,o,p[g+6],23,76029189);o=md5_hh(o,n,m,l,p[g+9],4,-640364487);';
//                $extension .='l=md5_hh(l,o,n,m,p[g+12],11,-421815835);m=md5_hh(m,l,o,n,p[g+15],16,530742520);n=md5_hh(n,m,l,o,p[g+2],23,-995338651);o=md5_ii(o,n,m,l,p[g+0],6,-198630844);l=md5_ii(l,o,n,m,p[g+7],10,1126891415);m=md5_ii(m,l,o,n,p[g+14],15,-1416354905);n=md5_ii(n,m,l,o,p[g+5],21,-57434055);';
//                $extension .='o=md5_ii(o,n,m,l,p[g+12],6,1700485571);l=md5_ii(l,o,n,m,p[g+3],10,-1894986606);m=md5_ii(m,l,o,n,p[g+10],15,-1051523);n=md5_ii(n,m,l,o,p[g+1],21,-2054922799);o=md5_ii(o,n,m,l,p[g+8],6,1873313359);l=md5_ii(l,o,n,m,p[g+15],10,-30611744);m=md5_ii(m,l,o,n,p[g+6],15,-1560198380);';
//                $extension .='n=md5_ii(n,m,l,o,p[g+13],21,1309151649);o=md5_ii(o,n,m,l,p[g+4],6,-145523070);l=md5_ii(l,o,n,m,p[g+11],10,-1120210379);m=md5_ii(m,l,o,n,p[g+2],15,718787259);n=md5_ii(n,m,l,o,p[g+9],21,-343485551);o=safe_add(o,j);n=safe_add(n,h);m=safe_add(m,f);l=safe_add(l,e)}';
//                $extension .='return Array(o,n,m,l)}function md5_cmn(h,e,d,c,g,f){return safe_add(bit_rol(safe_add(safe_add(e,h),safe_add(c,f)),g),d)}function md5_ff(g,f,k,j,e,i,h){return md5_cmn((f&k)|((~f)&j),g,f,e,i,h)}function md5_gg(g,f,k,j,e,i,h){return md5_cmn((f&j)|(k&(~j)),g,f,e,i,h)}';
//                $extension .='function md5_hh(g,f,k,j,e,i,h){return md5_cmn(f^k^j,g,f,e,i,h)}function md5_ii(g,f,k,j,e,i,h){return md5_cmn(k^(f|(~j)),g,f,e,i,h)}function safe_add(a,d){var c=(a&65535)+(d&65535);var b=(a>>16)+(d>>16)+(c>>16);return(b<<16)|(c&65535)}function bit_rol(a,b){return(a<<b)|(a>>>(32-b))};';
//
//                # Generate hashing
//                my @encryptArr = ();
//                my $extHash = "";
//                if($data->{hash} eq "1"){
//                    $extHash = "hex_md5";
//                }
//                
//                for my $key(keys %{$data}){
//                        if($key =~ /^(\d+)_(source.*)$/){
//                                my ($id) = $key =~ /^(\d+)_/;
//                                my $extSource = $data->{$id."_source"};
//                                $extSource =~ s/^js\.//g;
//                                
//                                push @encryptArr, "try{b['".$extSource."']=".$extHash."(b['".$extSource."'])}catch(e){}";
//                        }
//                }
//
//                my $setSize = @encryptArr;
//                if($setSize > 0){
//                        $extension .= join(';',@encryptArr);
//                }
//                
//            $extension .= "}\n";
//        }elsif($id eq '100034'){
//            my $utid = $configData->{config}->{account} . '/' . $configData->{config}->{profile} . '/' . $configData->{config}->{revision};
//            my $dataObj = "utag_data";
//            if($profileData->{publish}->{data_object} ne ""){
//                $dataObj = $profileData->{publish}->{data_object};
//            }
//
//            $extension = 'function(a,b){';
//            $extension .= 'window.' . $dataObj . ' = window.' . $dataObj . ' || {};' . $dataObj . '.do_not_track = "";';
//            if($data->{donotload} eq 'true'){
//                $extension .= 'if( navigator.doNotTrack == "yes" || navigator.doNotTrack == "1" || navigator.msDoNotTrack == "1" ){';
//                $extension .= 'window.utag={};utag.data={};utag.handler={};utag.handler.trigger=function(){};utag.track=function(){};utag.view=function(){};utag.link=function(){};utag.cfg={};utag.data.do_not_track="yes";utag.cfg.utid="' . $utid . '";';
//                $extension .= '}';
//            }else{  
//                $extension .= 'if( navigator.doNotTrack == "yes" || navigator.doNotTrack == "1" || navigator.msDoNotTrack == "1" ){' . $dataObj . '.do_not_track = "yes";}';
//            }
//            $extension .= '}';
//        } elsif ($id eq '100035'){ # Modal offer extension
//            my %conditionHash;
//
//            for my $key(keys %$data){
//                if($key =~ /^(\d+)_source$/){
//                    my $id = $1;
//                    $conditionHash{$id}->{1}->{source} = $data->{$id.'_source'};
//                    $conditionHash{$id}->{1}->{filtertype} = $data->{$id.'_filtertype'};
//                    $conditionHash{$id}->{1}->{filter} = $data->{$id.'_filter'};
//                }elsif($key =~ /^(\d+)_(\d+)_source$/){
//                    my $id = $1;
//                    my $filter = $2;
//                    $conditionHash{$id}->{$filter}->{source} = $data->{$id.'_'.$filter.'_source'};
//                    $conditionHash{$id}->{$filter}->{filtertype} = $data->{$id.'_'.$filter.'_filtertype'};
//                    $conditionHash{$id}->{$filter}->{filter} = $data->{$id.'_'.$filter.'_filter'};
//                }
//            }
//
//            #generate condition
//            my @orCondition;
//            for my $key(sort {$a <=> $b} keys %conditionHash){
//                my @andCondition;
//                for my $clause(sort {$a <=> $b} keys %{$conditionHash{$key}}){
//                    my $source = $conditionHash{$key}->{$clause}->{source};
//                    $source =~ s/^js\.//;
//                    my $condition = generateConditionFilter({ input => "utag.data['$source']", operator => $conditionHash{$key}->{$clause}->{filtertype}, filter => $conditionHash{$key}->{$clause}->{filter} });
//                    if(length($condition)>0){
//                            push @andCondition, $condition;
//                    }
//                }
//
//                my $conditionSize = @andCondition;
//                if($conditionSize == 1){
//                    push @orCondition, $andCondition[0];
//                }else{
//                    push @orCondition, '('.(join '&&', @andCondition).')';
//                }
//            }
//
//            my $conditionSize = @orCondition;
//            my $condition = 1;
//            if($conditionSize > 0){
//                $condition = join '||', @orCondition;
//            }
//
//            my $outfile = "$configData->{config}->{revision_publish_dir}/utag.modalExt_$data->{_id}.js";
//            my $template = getTemplate($configData, $profileData, "utag.modalExt.js", "utag.modalExt.js");
//            my $date = Tealium::UTUI::util::getDateId();
//            my $year = substr($date,0,4);
//
//            if(open IN, $template){
//                my @content = <IN>;
//                close IN;
//
//                my $p2jLkp = { # Use a look up to eliminate some repetitive code
//                    UTMDLEXTID => '_id',
//                    UTMDLISSTD => 'mdlIsStandardModal',
//                    UTMDLHEADERTXT => 'mdlHeaderTxt', 
//                    UTMDLBODYTXT => 'mdlBodyTxt',
//                    UTMDLFOOTERTXT => 'mdlFooterTxt', 
//                    UTMDLDLGHEIGHT => 'mdlDlgHeight',
//                    UTMDLDLGWIDTH => 'mdlDlgWidth',
//                    UTMDLCSSCODE => 'mdlCssCode',
//                    UTMDLHTMLCODE => 'mdlHtmlCode',
//                    UTMDLBASECSS => 'mdlBaseCss',
//                    UTMDLBASEHTML => 'mdlBaseHtml'
//                };
//
//                for(my $i=0;$i<@content; $i++){
//                    if($content[$i] =~ /##(UTMDL\w+?)##/){
//                        my $tgt = $1;
//                        if ($p2jLkp->{$tgt}){ # Simple substitution
//                            my $clnVal;
//                            unless ($tgt =~ /BASECSS|BASEHTML|CSSCODE|HTMLCODE/){
//                                $clnVal = encode_entities($data->{$p2jLkp->{$tgt}}); # Deal with weird encoded characters etc. 
//                            } else {
//                                $clnVal = $data->{$p2jLkp->{$tgt}};
//                                if ($tgt =~ /CSSCODE|HTMLCODE/){
//                                    $clnVal =~ s/\n//g;
//                                }
//                            }
//                            $clnVal =~ s/\n/<br>/g;
//                            $content[$i] =~ s/##$tgt##/$clnVal/g;                                
//                        }
//                    } elsif ($content[$i] =~ /##UTVERSION##/) {
//                        $content[$i] =~ s/##UTYEAR##/$year/;
//                        $content[$i] =~ s/##UTVERSION##/$date/;
//                    }
//
//                }
//
//                if(open OUT, ">", $outfile){
//                    if(defined $profileData->{publish}->{minify} && $profileData->{publish}->{minify} eq "yes"){
//                        eval {
//                                my $minifiedContent = "//tealium universal tag - utag.modalExt_$data->{_id} ut4.0.$date, Copyright $year Tealium.com Inc. All Rights Reserved.\n";
//                                $minifiedContent .= minify( input => (join "", @content) );
//                                print OUT $minifiedContent;
//                        };
//                        if ($@){
//                                $minifyFail = "utag.utag.modalExt_$data->{_id}.js";
//                        }
//                    }else{
//                        print OUT @content;
//                    }
//                    close OUT;
//                    $EXTRA_PUBLISH_FILES{"utag.modalExt_$data->{_id}.js"} = 1;
//
//                    my $templateFile = "$configData->{config}->{account_dir}/$configData->{config}->{account}/templates/$configData->{config}->{profile}/utag.modalExt.js";
//                    if(!-e "$templateFile"){
//                        copy("$configData->{config}->{template_dir}/utag.modalExt.js", $templateFile);
//                    }
//
//                }else{
//                    $log->debug("createSender: cant write outfile: $outfile");
//                    return undef;
//                }
//
//            } else {
//                $log->debug("createSender: $template template not found.");
//                return undef;
//            }
//
//            my $content = "utag.ut.loader({src: utag.cfg.path + 'utag.modalExt_$data->{_id}.js?utv=' + utag.cfg.v,cb:function() {utag.extn.mdlW.load();}});";
//
//            $extension = "function(a,b){if($condition){$content}}";
//
//        }
//
//        $log->debug("EXTENSIONS: $id: $extension");
//
//        return (length $extension > 0) ? $extension : undef;
//}
//*/
