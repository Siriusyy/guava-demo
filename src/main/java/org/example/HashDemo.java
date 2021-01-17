package org.example;

import com.google.common.base.Charsets;
import com.google.common.hash.*;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Random;

public class HashDemo {
    class Person {
        int id;
        String firstName;
        String lastName;
        int birthYear;

        public Person(int id, String firstName, String lastName, int birthYear) {
            this.id = id;
            this.firstName = firstName;
            this.lastName = lastName;
            this.birthYear = birthYear;
        }
    }

    @Test
    public void testHash() {
        Funnel<Person> personFunnel = new Funnel<Person>() {
            @Override
            public void funnel(Person person, PrimitiveSink into) {
                into.putInt(person.id)
                        .putString(person.firstName, Charsets.UTF_8)
                        .putString(person.lastName, Charsets.UTF_8)
                        .putInt(person.birthYear);
            }
        };

        HashFunction hf = Hashing.md5();
        HashCode hc = hf.newHasher()
                .putLong(12345)
                .putString("Tom", Charsets.UTF_8)
                .putObject(new Person(12345, "Tim", "Yang", 1998), personFunnel)
                .hash();
        System.out.println(hc);
    }

    @Test
    public void testBloomFilter() {
        ArrayList<Person> friendsList = new ArrayList<>();
        Random random = new Random();

        for (int i = 0; i < 10; i++) {
            friendsList.add(new Person(i, String.valueOf(random.nextInt(999)), "Yang" + i, 1998));
        }
        Person dude = new Person(12345, "Tim", "Yang", 1998);
        friendsList.add(dude);
        Funnel<Person> personFunnel = new Funnel<Person>() {
            @Override
            public void funnel(Person person, PrimitiveSink into) {
                into.putInt(person.id)
                        .putString(person.firstName, Charsets.UTF_8)
                        .putString(person.lastName, Charsets.UTF_8)
                        .putInt(person.birthYear);
            }
        };
        BloomFilter<Person> friends = BloomFilter.create(personFunnel, 500, 0.01);
        for (Person friend : friendsList) {
            friends.put(friend);
        }

        // 很久以后
        if (friends.mightContain(dude)) {
            //dude不是朋友还运行到这里的概率为1%
            //在这儿，我们可以在做进一步精确检查的同时触发一些异步加载
        }
    }
}
