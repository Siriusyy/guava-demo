package org.example.basic;

import com.google.common.base.Function;
import com.google.common.base.MoreObjects;
import com.google.common.collect.Ordering;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * 比较和排序
 * 先行定义比较的规则，之后就可以根据这个规则来进行排序
 */
public class OrderingDemo {
    private static List<People> list;

    private static class People {
        int age;
        String name;
        int gender;
        String idnumber;

        public People(int age, String name, int gender, String idnumber) {
            this.age = age;
            this.name = name;
            this.gender = gender;
            this.idnumber = idnumber;
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this).add("name",name).add("age",age)
                    .add("gender", new java.util.function.Function<Integer,String>() {
                        @Override
                        public String apply(Integer integer) {
                            if (gender == 0){
                                return "男";
                            }
                            return "女";
                        }
                    }.apply(age))
                    .add("idnumber",idnumber)
                    .toString();
        }
    }

    @BeforeClass
    public static void init() {
        list = new ArrayList();
        list.add(new People(3, "a", 1, "000002"));
        list.add(new People(2, "b", 1, "000001"));
        list.add(new People(1, "c", 1, "000003"));
    }

    @Test
    public void testCompare() {
        int compare = Ordering.natural().compare(1, 2);
        Assert.assertEquals(compare, -1);
        compare = Ordering.natural().reverse().compare(1, 2);
        Assert.assertEquals(compare, 1);
        compare = Ordering.natural().reverse().nullsFirst().compare(1, null);
        Assert.assertEquals(compare, 1);
    }


    @Test
    public void testSortByAge() {
        List<People> copy = Ordering.natural().onResultOf(new Function<People, Comparable>() {
            @Override
            public @Nullable Comparable apply(@Nullable People people) {
                return people.age;
            }
        }).sortedCopy(list);

        System.out.println(copy);
        Assert.assertTrue(Ordering.natural().onResultOf(new Function<People, Comparable>() {
            @Override
            public @Nullable Comparable apply(@Nullable People people) {
                return people.age;
            }
        }).isOrdered(copy));
    }

    @Test
    public void testSortByName() {
        Function<People, Comparable> function = new Function<People, Comparable>() {
            @Override
            public @Nullable Comparable apply(@Nullable People people) {
                return people.name;
            }
        };
        List<People> copy = Ordering.usingToString().onResultOf(function).sortedCopy(list);

        System.out.println(copy);
        Assert.assertTrue(Ordering.natural().onResultOf(function).isOrdered(copy));

    }
}
