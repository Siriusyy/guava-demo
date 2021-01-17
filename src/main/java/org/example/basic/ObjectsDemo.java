package org.example.basic;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.collect.ComparisonChain;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;

public class ObjectsDemo {
    @Test
    /**
     * 可以判断null等
     */
    public void testEqual() {
        Assert.assertFalse((Objects.equal(null, "hello")));
        Assert.assertTrue((Objects.equal(null, null)));
        Assert.assertTrue((Objects.equal("hello", "hello")));
    }

    private class A {
        String str;
        int[] ints;
        HashMap map;

        public A(String str, int[] ints, HashMap map) {
            this.str = str;
            this.ints = ints;
            this.map = map;
        }

        public String getStr() {
            return str;
        }

        public int[] getInts() {
            return ints;
        }

        public HashMap getMap() {
            return map;
        }


        /**
         * toStringHelper已经移到MoreObjects中
         *
         * @return
         */
        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("str", getStr())
                    .add("array", getInts())
                    .add("map", getMap())
                    .toString();
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(ints, map, str);
        }

        public int compareTo(A b) {
            return ComparisonChain.start()
                    .compare(this.str, b.str)
                    .compare(this.ints.length, b.ints.length)
                    .compare(this.map.size(), b.map.size())
                    .result();
        }

    }

    @Test
    public void testHash() {
        HashMap<Object, Object> map = new HashMap<>();
        map.put("1", 1);
        A a = new A("hello", new int[]{1, 2, 3}, map);
        System.out.println(a.hashCode());

    }

    @Test
    public void testToStringHelper() {
        A a = new A("hello", new int[]{1, 2, 3}, new HashMap());
        a.getMap().put("1", 1);

        System.out.println(a.toString());
        System.out.println(a.getInts());
    }

    @Test
    public void testComparison() {
        HashMap<Object, Object> map = new HashMap<>();
        map.put("1", 1);
        A a = new A("aaa", new int[]{1, 2, 3}, map);
        A b = new A("hello", new int[]{1, 2, 3}, map);
        System.out.println(a.compareTo(b));
    }

}
