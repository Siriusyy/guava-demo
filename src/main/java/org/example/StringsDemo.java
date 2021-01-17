package org.example;

import com.google.common.base.*;
import com.google.common.collect.ImmutableMap;
import org.junit.Test;

import java.util.List;
import java.util.Map;

public class StringsDemo {

    @Test
    public void testJoiner() {
        //字符串连接，skipNulls去掉null
        String s1 = Joiner.on(",").skipNulls().join("one", null, "tow");
        System.out.println(s1);
        //字符串连接，useForNull对null使用默认值代替
        String s2 = Joiner.on(",").useForNull("default").join("one", null, "tow");
        System.out.println(s2);
        //map转为字符串连接，on为entry间分隔符，withKeyValueSeparator为kev和value间分隔符
        String s3 = Joiner.on("&").withKeyValueSeparator("=").join(ImmutableMap.of("one", "1", "two", "2"));
        System.out.println(s3);
    }

    @Test
    public void testSpliter() {
        String rawStr = "好好学习  study ,hard 123 2323 ，<b> </b>!  sf!!！桂\t林S\nSDF\rSSD  、DF   alter(test)<sef8989。，奔啊 lalsdl";
        //单个分隔符（可单字或多字）分隔
        List<String> list1 = Splitter.on("。").trimResults().omitEmptyStrings().splitToList(rawStr);
        System.out.println(list1);
        //正则分隔
        List<String> list2 = Splitter.onPattern("\\s").trimResults().omitEmptyStrings().splitToList(rawStr);
        System.out.println(list2);
        //多个分隔符分隔
        List<String> list3 = Splitter.on(CharMatcher.anyOf(",，")).trimResults().omitEmptyStrings().splitToList(rawStr);
        System.out.println(list3);
        //fixedLength固定长度切分，limit限制最大分隔量
        List<String> list4 = Splitter.fixedLength(8).trimResults().omitEmptyStrings().limit(6).splitToList(rawStr);
        System.out.println(list4);
        //字符串转为map,on切分字符串，withKeyValueSeparator分隔key和value
        Map<String, String> map1 = Splitter.on(",").withKeyValueSeparator("=").split("one=1,two=2,three=3,four=4");
        System.out.println(map1);
    }

    @Test
    public void testCharMatcher() {
        String rawStr = "好好学习  study hard 123 2323 <b> </b>!  sf!!！桂\t林S\nSDF\rSSD  DF   alter(test)<sef8989。，奔啊 lalsdl";
        //在原始串中保留指定区间内的字符
        String result = CharMatcher.inRange('0', '9').retainFrom(rawStr);
        System.out.println(result);
        //去掉空白或指定字符
        result = CharMatcher.whitespace().or(CharMatcher.anyOf("<>!/，。")).removeFrom(rawStr);
        System.out.println(result);
        //替换非\t\n\r的字符
        result = CharMatcher.javaIsoControl().negate().replaceFrom(rawStr, "default");
        System.out.println(result);
        //去掉首尾ascii码字符
        result = CharMatcher.ascii().trimFrom(rawStr);
        System.out.println(result);
        //统计ascii码字符
        int cnt = CharMatcher.ascii().countIn(rawStr);
        System.out.println(cnt);
        //判断ascii码字符
        boolean flag = CharMatcher.ascii().matchesAnyOf(rawStr);
        System.out.println(flag);
        //去掉连续指定字符并用指定字符替换
        result = CharMatcher.whitespace().trimAndCollapseFrom(rawStr, ' ');
        System.out.println(result);
    }

    @Test
    public void testCaseFormat(){
        //小下划线转为小写驼峰，返回值为stuName
        String result = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, "stu_name");
        System.out.println(result);
        //小写驼峰转为下划线，返回值为stu_name
        result = CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, "stuName");
        System.out.println(result);
        //小驼峰转为小写中划线，返回值为stu-name
        result = CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_HYPHEN, "stuName");
        System.out.println(result);
        //小写驼峰转为大写驼峰，返回值为StuName
        result = CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, "stuName");
        System.out.println(result);
        //小写驼峰转为大写下划线，返回值为STU_NAME
        result = CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_UNDERSCORE, "stuName");
        System.out.println(result);
    }

    @Test
    public void testStrings(){
        //获取相同前缀
        String result = Strings.commonPrefix("aacdd22", "aacex22");
        System.out.println(result);
        //获取相同后缀
        result = Strings.commonSuffix("aacdd22","aacex22");
        System.out.println(result);
        //开头字符填充
        result = Strings.padStart("aa",8,'x');
        System.out.println(result);
        //结尾字符填充
        result = Strings.padEnd("aa",8,'x');
        System.out.println(result);
        //结尾字符填充
        result = Strings.repeat("ax",3);
        System.out.println(result);
        //格式化字符串
        result = Strings.lenientFormat("%s+%s=%s", 1,2,3);
        System.out.println(result);
    }

}
