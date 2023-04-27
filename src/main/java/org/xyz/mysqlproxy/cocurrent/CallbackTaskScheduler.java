package org.xyz.mysqlproxy.cocurrent;

import com.google.common.util.concurrent.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;


/**
 * 带有回调的任务执行器
 * */
public class CallbackTaskScheduler {
    /**
     * google的guava线程池
     * */
    static ListeningExecutorService gPool = null;

    static {
        ExecutorService jPool = ThreadPoolFactory.getMixedTargetThreadPool();
        gPool = MoreExecutors.listeningDecorator(jPool);
    }

    private CallbackTaskScheduler() {
    }

    /**
     * 添加任务
     *
     * @param callbackTask 带回调的任务
     * */
    public static <R> void add(CallbackTask<R> callbackTask) {
        // 提交任务
        ListenableFuture<R> future = gPool.submit(new Callable<R>() {
            public R call() throws Exception {
                R r = callbackTask.execute();
                return r;
            }
        });

        // 添加回调方法
        Futures.addCallback(future, new FutureCallback<R>() {
            public void onSuccess(R r) {
                callbackTask.onSuccess(r);
            }

            public void onFailure(Throwable t) {
                callbackTask.onFailure(t);
            }
        }, gPool);  // 第三个参数传入线程池，会从线程池中捞一个线程来执行callback中的操作
    }
}
