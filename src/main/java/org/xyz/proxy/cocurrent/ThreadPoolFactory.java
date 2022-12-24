package org.xyz.proxy.cocurrent;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 线程池
 * */
@Slf4j
public class ThreadPoolFactory {

    /**
     * CPU核数
     * */
    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();


    /**
     * 定制线程工厂，用于创建自定义线程
     * */
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

    /**
     * JVM关闭时调用钩子函数执行的任务
     * */
    static class ShutdownHookThread extends Thread {
        private volatile boolean hasShutdown = false;
        private static AtomicInteger shutdownTimes = new AtomicInteger(0);
        private final Callable callback;

        public ShutdownHookThread(String name, Callable callback) {
            super("JVM退出钩子(" + name + ")");

            this.callback = callback;
        }

        @Override
        public void run() {
            synchronized (this) {
                log.info(getName() + " starting.... ");
                if (!this.hasShutdown) {
                    this.hasShutdown = true;
                    long beginTime = System.currentTimeMillis();
                    try {
                        this.callback.call();
                    } catch (Exception e) {
                        log.error(getName() + " error: " + e.getMessage());
                    }
                    long consumingTimeTotal = System.currentTimeMillis() - beginTime;
                    log.info(getName() + "  耗时(ms): " + consumingTimeTotal);
                }
            }
        }
    }

    /**
     * 优雅关闭线程池
     * */
    public static void shutdownThreadPoolGracefully(ExecutorService threadPool) {
        if (!(threadPool instanceof ExecutorService) || threadPool.isTerminated()) {
            return;
        }
        try {
            threadPool.shutdown();   //拒绝接受新任务
        } catch (SecurityException e) {
            return;
        } catch (NullPointerException e) {
            return;
        }
        try {
            // 等待 60 s，等待线程池中的任务完成执行
            if (!threadPool.awaitTermination(60, TimeUnit.SECONDS)) {
                // 调用 shutdownNow 取消正在执行的任务
                threadPool.shutdownNow();
                // 再次等待 60 s，如果还未结束，可以再次尝试，或则直接放弃
                if (!threadPool.awaitTermination(60, TimeUnit.SECONDS)) {
                    log.info("线程池任务未正常执行结束");
                }
            }
        } catch (InterruptedException ie) {
            // 捕获异常，重新调用 shutdownNow
            threadPool.shutdownNow();
        }
        // 仍然没有关闭，循环关闭1000次，每次等待10毫秒
        if (!threadPool.isTerminated()) {
            try {
                for (int i = 0; i < 1000; i++) {
                    if (threadPool.awaitTermination(10, TimeUnit.MILLISECONDS)) {
                        break;
                    }
                    threadPool.shutdownNow();
                }
            } catch (InterruptedException e) {
                log.error(e.getMessage());
            } catch (Throwable e) {
                log.error(e.getMessage());
            }
        }
    }

    /**
     * 空闲保活时限，单位秒
     */
    private static final int KEEP_ALIVE_SECONDS = 30;
    /**
     * 有界队列size
     */
    private static final int QUEUE_SIZE = 10000;


    /**
     * CPU线程池核心线程数
     */
    private static final int CORE_POOL_SIZE = 0;
    /**
     * CPU线程池最大线程数
     * */
    private static final int MAXIMUM_POOL_SIZE = CPU_COUNT;

    /**
     * 懒汉式单例创建线程池：用于执行CPU密集型任务
     * */
    private static class CpuIntenseTargetThreadPoolLazyHolder {
        /**
         * 线程池： 用于执行CPU密集型任务
         * */
        private static final ThreadPoolExecutor EXECUTOR = new ThreadPoolExecutor(
                MAXIMUM_POOL_SIZE,
                MAXIMUM_POOL_SIZE,
                KEEP_ALIVE_SECONDS,
                TimeUnit.SECONDS,
                new LinkedBlockingDeque<>(QUEUE_SIZE),
                new CustomThreadFactory("cpu")
        );

        static {
            // 允许核心线程超时销毁
            EXECUTOR.allowCoreThreadTimeOut(true);
            // JVM关闭时的钩子函数，关闭线程池
            Runtime.getRuntime().addShutdownHook(
                new ShutdownHookThread("CPU密集型任务线程池", new Callable<Void>() {
                    @Override
                    public Void call() throws Exception {
                        //优雅关闭线程池
                        shutdownThreadPoolGracefully(EXECUTOR);
                        return null;
                    }
                })
            );
        }
    }

    /**
     * 获取执行CPU密集型任务的线程池
     *
     * @return CPU密集型任务的线程池
     */
    public static ThreadPoolExecutor getCpuIntenseTargetThreadPool() {
        return CpuIntenseTargetThreadPoolLazyHolder.EXECUTOR;
    }

    /**
     * IO线程池最大线程数
     */
    private static final int IO_MAX = Math.max(2, CPU_COUNT * 2);
    /**
     * IO线程池核心线程数
     */
    private static final int IO_CORE = 0;

    /**
     * 懒汉式单例创建线程池：用于执行IO密集型任务
     * */
    private static class IoIntenseTargetThreadPoolLazyHolder {
        /**
         * 线程池： 用于执行IO密集型任务
         * */
        private static final ThreadPoolExecutor EXECUTOR = new ThreadPoolExecutor(
                IO_MAX,
                IO_MAX,
                KEEP_ALIVE_SECONDS,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue(QUEUE_SIZE),
                new CustomThreadFactory("io"));

        static {
            EXECUTOR.allowCoreThreadTimeOut(true);
            //JVM关闭时的钩子函数
            Runtime.getRuntime().addShutdownHook(
                new ShutdownHookThread("IO密集型任务线程池", new Callable<Void>() {
                    @Override
                    public Void call() throws Exception {
                        //优雅关闭线程池
                        shutdownThreadPoolGracefully(EXECUTOR);
                        return null;
                    }
                })
            );
        }
    }

    /**
     * 获取执行IO密集型任务的线程池
     *
     * @return 执行IO密集型任务的线程池
     */
    public static ThreadPoolExecutor getIoIntenseTargetThreadPool() {
        return IoIntenseTargetThreadPoolLazyHolder.EXECUTOR;
    }

    /**
     * 混合线程池核心线程数
     * */
    private static final int MIXED_CORE = 0;
    /**
     * 混合线程池最大线程数
     * */
    private static final int MIXED_MAX = Math.max(2, (int) Math.ceil(CPU_COUNT * 1.5));
    private static final String MIXED_THREAD_AMOUNT = "mixed.thread.amount";

    /**
     * 懒汉式单例创建线程池：用于执行混合型任务
     * */
    private static class MixedTargetThreadPoolLazyHolder {
        // 首先从环境变量 mixed.thread.amount 中获取预先配置的线程数
        // 如果没有对 mixed.thread.amount 做配置，则使用常量 MIXED_MAX 作为线程数
        private static final int max = (null != System.getProperty(MIXED_THREAD_AMOUNT)) ?
                Integer.parseInt(System.getProperty(MIXED_THREAD_AMOUNT)) : MIXED_MAX;
        // 线程池： 用于混合型任务
        private static final ThreadPoolExecutor EXECUTOR = new ThreadPoolExecutor(
                max,
                max,
                KEEP_ALIVE_SECONDS,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue(QUEUE_SIZE),
                new CustomThreadFactory("mixed"));

        static {
            EXECUTOR.allowCoreThreadTimeOut(true);
            //JVM关闭时的钩子函数
            Runtime.getRuntime().addShutdownHook(new ShutdownHookThread("混合型任务线程池", new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    //优雅关闭线程池
                    shutdownThreadPoolGracefully(EXECUTOR);
                    return null;
                }
            }));
        }
    }

    /**
     * 获取执行混合型任务的线程池
     *
     * @return 执行混合型任务的线程池
     */
    public static ThreadPoolExecutor getMixedTargetThreadPool() {
        return MixedTargetThreadPoolLazyHolder.EXECUTOR;
    }
}
