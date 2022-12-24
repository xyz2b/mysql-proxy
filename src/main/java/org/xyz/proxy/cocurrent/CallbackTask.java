package org.xyz.proxy.cocurrent;

/**
 * 带有回调的任务接口
 * */
public interface CallbackTask<R> extends Task<R> {
    /**
     * 任务执行完成回调函数（成功或失败）
     * @param r 任务执行结果
     * */
    void onSuccess(R r);
    /**
     * 任务执行异常回调函数（抛出异常）
     * @param t 异常信息
     * */
    void onFailure(Throwable t);
}
