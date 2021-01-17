package org.example;

import com.google.common.cache.*;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class CacheDemo {
    @Test
    public void test() {
        Cache<String, String> cache = CacheBuilder.newBuilder().build();
        cache.put("word", "Hello Guava Cache");
        System.out.println(cache.getIfPresent("word"));
    }

    @Test
    public void testLoadingCache() {
        LoadingCache<String, String> cache = CacheBuilder.newBuilder()
                .build(new CacheLoader<String, String>() {
                    public String load(String key) throws RuntimeException {
                        Random random = new Random();
                        return key + random.nextInt(100);
                    }
                });
        try {
            String v = cache.get("key");
            System.out.println(v);
        } catch (RuntimeException | ExecutionException e) {
            throw new RuntimeException(e.getCause());
        }
    }

    @Test
    public void testMaxNum() {
        Cache<String, String> cache = CacheBuilder.newBuilder()
                .maximumSize(2)
                .build();
        cache.put("key1", "value1");
        cache.put("key2", "value2");
        cache.put("key3", "value3");
        System.out.println("第一个值：" + cache.getIfPresent("key1"));
        System.out.println("第二个值：" + cache.getIfPresent("key2"));
        System.out.println("第三个值：" + cache.getIfPresent("key3"));

    }

    @Test
    public void testExpire() throws InterruptedException {
        Cache<String, String> cache = CacheBuilder.newBuilder()
                .maximumSize(2)
                .expireAfterWrite(3, TimeUnit.SECONDS)
                .build();
        cache.put("key1", "value1");
        int time = 1;
        while (true) {
            System.out.println("第" + time++ + "次取到key1的值为：" + cache.getIfPresent("key1"));
            Thread.sleep(1000);
            if (time > 5) {
                break;
            }
        }

        cache = CacheBuilder.newBuilder()
                .maximumSize(2)
                .expireAfterAccess(3, TimeUnit.SECONDS)
                .build();
        cache.put("key1", "value1");
        time = 1;
        while (true) {
            Thread.sleep(time * 1000);
            System.out.println("睡眠" + time++ + "秒后取到key1的值为：" + cache.getIfPresent("key1"));
            if (time > 5) {
                break;
            }
        }
    }

    /**
     * 可以通过weakKeys和weakValues方法指定Cache只保存对缓存记录key和value的弱引用。
     * 这样当没有其他强引用指向key和value时，key和value对象就会被垃圾回收器回收。
     */
    @Test
    public void testWeakValue() {
        Cache<String, Object> cache = CacheBuilder.newBuilder()
                .maximumSize(2)
                .weakValues()
                .build();
        Object value = new Object();
        cache.put("key1", value);

        value = new Object();//原对象不再有强引用
        System.gc();
        System.out.println(cache.getIfPresent("key1"));
    }

    @Test
    public void testInvalidate() {
        Cache<String, String> cache = CacheBuilder.newBuilder().build();
        Object value = new Object();
        cache.put("key1", "value1");
        cache.put("key2", "value2");
        cache.put("key3", "value3");

        List<String> list = new ArrayList<String>();
        list.add("key1");
        list.add("key2");

        cache.invalidateAll(list);//批量清除list中全部key对应的记录
        System.out.println(cache.getIfPresent("key1"));
        System.out.println(cache.getIfPresent("key2"));
        System.out.println(cache.getIfPresent("key3"));
    }

    @Test
    public void testRemovalListener() {
        RemovalListener<String, String> listener = new RemovalListener<String, String>() {
            public void onRemoval(RemovalNotification<String, String> notification) {
                System.out.println("[" + notification.getKey() + ":" + notification.getValue() + "] is removed!");
            }
        };
        Cache<String, String> cache = CacheBuilder.newBuilder()
                .maximumSize(3)
                .removalListener(listener)
                .build();
        Object value = new Object();
        cache.put("key1", "value1");
        cache.put("key2", "value2");
        cache.put("key3", "value3");
        cache.put("key4", "value3");
        cache.put("key5", "value3");
        cache.put("key6", "value3");
        cache.put("key7", "value3");
        cache.put("key8", "value3");
    }

    @Test
    public void testGet() throws InterruptedException {
        Cache<String, String> cache = CacheBuilder.newBuilder()
                .maximumSize(3)
                .build();

        new Thread(new Runnable() {
            public void run() {
                System.out.println("thread1");
                try {
                    String value = cache.get("key", new Callable<String>() {
                        public String call() throws Exception {
                            System.out.println("load1"); //加载数据线程执行标志
                            Thread.sleep(1000); //模拟加载时间
                            return "auto load by Callable";
                        }
                    });
                    System.out.println("thread1 " + value);
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        new Thread(new Runnable() {
            public void run() {
                System.out.println("thread2");
                try {
                    String value = cache.get("key", new Callable<String>() {
                        public String call() throws Exception {
                            System.out.println("load2"); //加载数据线程执行标志
                            Thread.sleep(1000); //模拟加载时间
                            return "auto load by Callable";
                        }
                    });
                    System.out.println("thread2 " + value);
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        Thread.sleep(3000);

    }
}