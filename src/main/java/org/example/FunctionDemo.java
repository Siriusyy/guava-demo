package org.example;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import org.junit.Test;

import java.util.List;

public class FunctionDemo {
    public class Person {
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }

        private String name;
        private int age;

        public Person(String name, int age) {
            this.name = name;
            this.age = age;
        }
    }

    @Test
    public void test(){
        List<Person> people = Lists.newArrayList(new Person("bowen", 27),
                new Person("bob", 20),
                new Person("Katy", 18),
                new Person("Lim", 66),
                new Person("Toby", 78),
                new Person("Logon", 24));

        List<Person> oldPeople = Lists.newArrayList(Collections2.filter(people, new Predicate<Person>() {
            public boolean apply(Person person) {
                return person.getAge() >= 60;
            }
        }));

        for (Person p : oldPeople) {
            System.out.println(p.getName());
        }
    }
}
