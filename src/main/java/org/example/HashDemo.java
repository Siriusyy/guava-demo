package org.example;

import com.google.common.base.Charsets;
import com.google.common.hash.*;
import org.junit.Test;

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
    public void test() {
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
                .putObject(new Person(12345,"Tim","Yang",1998), personFunnel)
                .hash();
        System.out.println(hc);
    }
}
