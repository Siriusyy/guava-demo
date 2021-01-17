package org.example;

import com.google.common.eventbus.*;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EventBusDemo {
    private static class Event1 {
        @Override
        public String toString() {
            return "事件1";
        }
    }

    private static class Event2 {
        @Override
        public String toString() {
            return "事件2";
        }
    }

    private static class EventX {
        @Override
        public String toString() {
            return "事件X";
        }
    }

    private static class EventListener {
        @Subscribe
        @AllowConcurrentEvents
        public void onEvent(Event1 event1) throws InterruptedException {
            String name = Thread.currentThread().getName();
            System.out.println(name + " sleep 一会儿");
            Thread.sleep(2000);
            System.out.println(name + "===订阅事件1,接收到:" + event1);
        }

        @Subscribe
        public void onEvent(Event2 event2) throws InterruptedException {
            String name = Thread.currentThread().getName();
            System.out.println(name + " sleep 一会儿");
            Thread.sleep(1000);
            System.out.println(name + "===订阅事件2,接收到:" + event2);
        }

        @Subscribe
        public void onEvent(DeadEvent deadEvent) throws InterruptedException {
            String name = Thread.currentThread().getName();
            Thread.sleep(3000);
            System.out.println(name + "===订阅错误的事件,接收到:" + deadEvent);
        }
    }

    public static void main(String[] args) throws InterruptedException {
        String name = Thread.currentThread().getName();
        EventBus eb = new EventBus();
        eb.register(new EventListener());
        System.out.println(name + "----------发送事件X---------");
        eb.post(new EventX());
        System.out.println(name + "----------发送事件1----并行接收-----");
        ExecutorService threadPool = Executors.newCachedThreadPool();
        eb = new AsyncEventBus(threadPool);
        eb.register(new EventListener());
        for (int i = 0; i < 10; i++) {
            eb.post(new Event1());
        }
        Thread.sleep(2000);
        System.out.println(name + "----------发送事件2----串行接收-----");
        for (int i = 0; i < 10; i++) {
            eb.post(new Event2());
        }
        Thread.sleep(2000);
        threadPool.shutdown();
    }
}
