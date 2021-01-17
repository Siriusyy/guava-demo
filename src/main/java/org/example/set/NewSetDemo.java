package org.example.set;

import com.google.common.collect.*;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class NewSetDemo {

    /**
     * 维基百科从数学角度这样定义 Multiset：”集合[set]概念的延伸，它的元素可以重复出现…与集合[set]相同
     * 而与元组[tuple]相反的是，Multiset 元素的顺序是无关紧要的：Multiset {a, a, b}和{a, b, a}是相等的”。
     * 这里所说的集合[set]是数学上的概念，Multiset继承自 JDK 中的 Collection 接口，而不是 Set 接口，
     * 所以包含重复元素并没有违反原有的接口契约。
     */
    @Test
    public void testMultiSet() {
        HashMultiset<Integer> mset = HashMultiset.<Integer>create();
        mset.add(1);
        mset.add(2);
        mset.add(1);
        mset.add(3);
        mset.add(3);
        mset.add(3);
        mset.add(3);

        System.out.println(mset);
        Assert.assertEquals(4, mset.count(3));

        Set<Integer> set = mset.elementSet();
        //System.out.println(set);
        Assert.assertEquals(set.size(), 3);

        //功能类似于分组
        Set<Multiset.Entry<Integer>> entries = mset.entrySet();
        for (Multiset.Entry<Integer> entry : entries) {
            System.out.println(entry.getElement() + "--" + entry.getCount());
        }

        mset.setCount(2,10);
        System.out.println(mset);
    }

    /**
     * 可以用两种方式思考 Multimap 的概念：”键-单个值映射”的集合：
     * a -> 1 a -> 2 a ->4 b -> 3 c -> 5
     * 或者”键-值集合映射”的映射：
     * a -> [1, 2, 4] b -> 3 c -> 5
     */
    @Test
    public void testMultiMap() {
        HashMultimap<Integer, String> multimap = HashMultimap.create();
        multimap.put(1, "apple");
        multimap.put(1, "banana");
        multimap.put(1, "peach");
        multimap.put(1, "watermelon");
        multimap.put(2, "tomato");
        multimap.put(2, "cabbage");
        multimap.put(2, "spinach");
        HashSet<String> set = new HashSet<>();
        set.add("chicken");
        set.add("fish");
        set.add("pork");
        multimap.putAll(3, set);

        System.out.println(multimap);
        multimap.asMap().get(1).add("strawberry");
        System.out.println(multimap);
        //视图无法添加
        //multimap.asMap().put(1,"peal");
        //所有单个键值对
        Set<Map.Entry<Integer, String>> entries = multimap.entries();
        for (Map.Entry<Integer, String> entry : entries) {
            System.out.println(entry.getKey() + "--" + entry.getValue());
        }

    }

    //BiMap键值对双向映射
    @Test
    public void testBiMap(){
        HashBiMap<Integer, String> biMap = HashBiMap.create();
        biMap.put(1,"1");
        biMap.put(2,"2");
        biMap.put(3,"3");
        biMap.put(4,"4");
        biMap.put(5,"5");
        biMap.put(6,"6");

        BiMap<String, Integer> inverse = biMap.inverse();
        System.out.println(inverse.get("1"));

        biMap.put(7,"7");//同步更新inverse
        System.out.println(inverse.get("7"));
    }

    /**
     * 两个键映射一个值
     */
    @Test
    public void testTable(){
        HashBasedTable<Integer, Integer, String> table = HashBasedTable.create();
        table.put(1,1,"(1,1)");
        table.put(1,4,"(1,4)");
        table.put(5,8,"(5,8)");
        //返回本质实现
        Map<Integer, Map<Integer, String>> rowMap = table.rowMap();
        String s = rowMap.get(1).get(1);
        System.out.println(s);

        Map<Integer, String> row = table.row(1);
        System.out.println(row);

        Map<Integer, Map<Integer, String>> colMap = table.columnMap();
        System.out.println(colMap.get(4).get(1));

        Set<Table.Cell<Integer, Integer, String>> cells = table.cellSet();
        System.out.println("============");
        for (Table.Cell<Integer, Integer, String> cell : cells) {
            System.out.println(cell);
        }
    }

    /**
     * 它的键是类型，而值是符合键所指类型的对象。
     */
    @Test
    public void testClassToInstanceMap(){
        MutableClassToInstanceMap<Number> map = MutableClassToInstanceMap.create();
        map.put(Integer.class,10);
        map.put(Float.class,5.0f);
        map.put(Long.class,1000000000l);
        System.out.println(map.get(Integer.class));
        System.out.println(map.get(Float.class));
    }
    @Test
    public void testRangeSet(){
        RangeSet<Integer> rangeSet = TreeRangeSet.create();
        rangeSet.add(Range.closed(1, 10));
        // {[1,10]}
        System.out.println(rangeSet);
        rangeSet.add(Range.closedOpen(11, 15));
        //不相连区间:{[1,10], [11,15)}
        System.out.println(rangeSet);
        rangeSet.add(Range.closedOpen(15, 20));
        //相连区间; {[1,10], [11,20)}
        System.out.println(rangeSet);
        rangeSet.add(Range.openClosed(0, 0));
        //空区间; {[1,10], [11,20)}
        System.out.println(rangeSet);
        rangeSet.remove(Range.open(5, 10));
        //分割[1, 10]; {[1,5], [10,10], [11,20)}
        System.out.println(rangeSet);
    }
}
