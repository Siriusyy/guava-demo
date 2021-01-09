package org.example.set;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import org.junit.Test;

import java.util.HashSet;

public class ImmutableSetDemo {
    @Test
    public void test() {
        //of方法构建
        ImmutableSet<String> COLOR_NAMES = ImmutableSet.of(
                "red",
                "orange",
                "yellow",
                "green",
                "blue",
                "purple");
        System.out.println(COLOR_NAMES);
        //已有的集合copy
        HashSet<String> set = new HashSet<>();
        set.add("red");
        set.add("orange");
        set.add("yellow");
        set.add("green");
        set.add("blue");
        set.add("purple");
        ImmutableSet<String> iset = ImmutableSet.copyOf(set);
        System.out.println(iset);
        //使用builder方法
        ImmutableSet<String> build = ImmutableSet.<String>builder()
                .add("red")
                .add("orange")
                .add("yellow")
                .add("green")
                .add("blue")
                .add("purple")
                .build();
        System.out.println(build);
        //Sorted在构建的时候就会排序。
        ImmutableSortedSet<Integer> nums = ImmutableSortedSet.of(1, 2, 5, 3, 7, 6);
        System.out.println(nums);
    }
}
