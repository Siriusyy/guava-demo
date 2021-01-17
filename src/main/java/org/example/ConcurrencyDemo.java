package org.example;

import com.google.common.base.Function;
import com.google.common.util.concurrent.*;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.Test;

import java.util.concurrent.*;


public class ConcurrencyDemo {

    @Test
    public void testThreadFactoryBuilder() {
        ThreadPoolExecutor executor = new ThreadPoolExecutor(Runtime.getRuntime().availableProcessors() * 2, 15,
                60, TimeUnit.SECONDS, new LinkedBlockingDeque<Runnable>(),
                new ThreadFactoryBuilder()
                        .setNameFormat("thread--xx--xx")
                        .setDaemon(true).build());
    }


    class MyTask implements Callable<Integer> {
        String str;

        public MyTask(String str) {
            this.str = str;
        }

        @Override
        public Integer call() throws Exception {
            System.out.println("call excute...." + str);
            return 8;
        }
    }

    @Test
    public void testListenableFeature() throws ExecutionException, InterruptedException {
        ListeningExecutorService executorService = MoreExecutors.listeningDecorator(
                MoreExecutors.getExitingExecutorService(new ThreadPoolExecutor(10, 100,
                        3000, TimeUnit.MILLISECONDS,
                        new LinkedBlockingQueue<Runnable>())));
        ListenableFuture<Integer> future = executorService.submit(new MyTask("test"));
        System.out.println("future:" + future.get());

        Futures.addCallback(future, new FutureCallback<Integer>() {
            @Override
            public void onSuccess(Integer result) {
                System.out.println("result" + result);
            }

            @Override
            public void onFailure(Throwable t) {
                System.err.println("error");
            }
        }, executorService);

    }

    @Test
    public void testSettable() throws InterruptedException, ExecutionException, TimeoutException {
        SettableFuture<Object> sf = SettableFuture.create();

        ListeningExecutorService executorService = MoreExecutors.listeningDecorator(
                MoreExecutors.getExitingExecutorService(new ThreadPoolExecutor(10, 100,
                        3000, TimeUnit.MILLISECONDS,
                        new LinkedBlockingQueue<Runnable>())));
        ListenableFuture<String> future = executorService.submit(new Callable<String>() {
            @Override
            public String call() throws Exception {
                TimeUnit.SECONDS.sleep(2);
                //TimeUnit.SECONDS.sleep(5);
                String ret = "hello";
                sf.set(ret);
                return ret;
            }
        });

        System.out.println(sf.get(5, TimeUnit.SECONDS));
    }


    @Test
    public void testAsyncFunction() throws ExecutionException, InterruptedException {

        ListeningExecutorService listeningExecutorService = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(2));

        ListenableFuture<String> lis = listeningExecutorService.submit(new Callable<String>() {
            @Override
            public String call() {
                return "张三";
            }
        });

        ListenableFuture<String> lf = Futures.transform(lis, new Function<String, String>() {
            @Override
            public @Nullable String apply(@Nullable String input) {
                try {
                    TimeUnit.SECONDS.sleep(3);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return "hello" + input;
            }
        }, listeningExecutorService);

        AsyncFunction<String, String> asyncFunction = new AsyncFunction<String, String>() {
            @Override
            public ListenableFuture<String> apply(final String input) throws Exception {
                return listeningExecutorService.submit(new Callable<String>() {
                    @Override
                    public String call() throws Exception {
                        TimeUnit.SECONDS.sleep(3);
                        return "hello" + input;
                    }
                });
            }

        };

        ListenableFuture<String> lfas = Futures.transformAsync(lis, asyncFunction, listeningExecutorService);



        long start = System.currentTimeMillis();
        //todo ??
        System.out.println(lfas.get());
        System.out.println(System.currentTimeMillis() - start);
        System.out.println(lf.get());
        System.out.println(System.currentTimeMillis() - start);

        listeningExecutorService.shutdown();
    }

}
