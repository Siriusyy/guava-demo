package org.example;

import com.google.common.base.Optional;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Set;


public class OptinalDemo {
    @Test
    public void testOf() {
        //如果Optional包含非null的引用（引用存在），返回true
        Optional<Integer> var = Optional.of(5);
        //如果Optional包含非null的引用（引用存在），返回true
        Assert.assertTrue(var.isPresent());
        Assert.assertEquals(5, (int) var.get());
    }


    @Test(expected = NullPointerException.class)
    public void testOf2() {
        HashMap<String, Integer> map = new HashMap<>();
        //直接抛异常
        Optional.of(map.get("1"));
    }

    @Test(expected = IllegalStateException.class)
    public void testAbsent() {
        //创建引用缺失的Optional实例
        Optional<Object> absent = Optional.absent();
        Assert.assertFalse(absent.isPresent());
        //抛异常，absent不允许get
        absent.get();
    }

    @Test
    public void testOr() {
        Optional<Integer> absent = Optional.absent();
        //返回Optional所包含的引用，若引用缺失，返回指定的值
        Assert.assertEquals(5, (int) absent.or(5));
        //返回Optional所包含的引用，若引用缺失，返回null
        System.out.println(absent.orNull());
    }

    /**
     * asSet返回Optional所包含引用的单例不可变集
     * 如果引用存在，返回一个只有单一元素的集合
     * 如果引用缺失，返回一个空集合。
     */
    @Test
    public void testAsSet() {
        Optional<Integer> absent = Optional.absent();
        Assert.assertEquals(0,absent.asSet().size());
        Optional<Integer> var = Optional.of(3);
        Set<Integer> set = var.asSet();
        Assert.assertEquals(1,set.size());
        for (int i : set) {
            Assert.assertEquals(3,i);
        }
    }

}
