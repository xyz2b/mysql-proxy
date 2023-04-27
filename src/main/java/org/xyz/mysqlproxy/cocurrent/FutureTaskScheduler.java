package org.xyz.mysqlproxy.cocurrent;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 不带回调的任务执行器，普通任务执行器
 * */
public class FutureTaskScheduler {
    static ThreadPoolExecutor mixPool = null;

    static {
        mixPool = ThreadPoolFactory.getMixedTargetThreadPool();
    }

    private FutureTaskScheduler()
    {

    }

    /**
     * 添加任务
     *
     * @param task 普通任务
     * @return future
     */
    public static <R> Future<R> add(Task<R> task) {
        return mixPool.submit(
            new Callable<R>() {
                @Override
                public R call() throws Exception {
                    return task.execute();
                }
            }
        );
    }
}
