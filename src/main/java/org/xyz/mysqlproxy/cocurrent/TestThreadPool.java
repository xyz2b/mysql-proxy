package org.xyz.mysqlproxy.cocurrent;

import org.apache.tomcat.jni.Time;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class TestThreadPool {
    private static class CustomThreadFactory implements ThreadFactory {
        /**
         * 目前系统中的线程池数量，作为线程名称一部分
         * */
        private static final AtomicInteger threadPoolNumber = new AtomicInteger(1);
        /**
         * 线程所属组
         * */
        private final ThreadGroup threadGroup;

        /**
         * 该线程池中线程的数量，作为线程名称一部分
         * */
        private static final AtomicInteger threadNumber = new AtomicInteger(1);

        /**
         * 线程标识，作为线程名称一部分
         * */
        private final String threadTag;

        CustomThreadFactory(String threadTag) {
            SecurityManager s = System.getSecurityManager();
            threadGroup = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
            this.threadTag = "apppool-" + threadPoolNumber.getAndIncrement() + "-" + threadTag + "-";
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(threadGroup, r, threadTag + threadNumber, 0);

            if (t.isDaemon()) {
                t.setDaemon(false);
            }

            if (t.getPriority() != Thread.NORM_PRIORITY) {
                t.setPriority(Thread.NORM_PRIORITY);
            }

            return t;
        }
    }

    public static void main(String[] args) {
        System.currentTimeMillis();


        ThreadPoolExecutor jPool = new ThreadPoolExecutor(2,
                10,
                3,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue(20),
                new CustomThreadFactory("mixed"));

        jPool.allowCoreThreadTimeOut(true);

        jPool.submit(new Runnable() {
            @Override
            public void run() {
                Time.sleep(1);
            }
        });

        jPool.submit(new Runnable() {
            @Override
            public void run() {
                Time.sleep(1);
            }
        });

        jPool.submit(new Runnable() {
            @Override
            public void run() {
                Time.sleep(1);
            }
        });

        jPool.submit(new Runnable() {
            @Override
            public void run() {
                Time.sleep(1);
            }
        });

        jPool.submit(new Runnable() {
            @Override
            public void run() {
                Time.sleep(1);
            }
        });

    }
}
