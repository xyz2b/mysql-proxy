package org.xyz.mysqlproxy.cocurrent;

public interface Task<R> {
    /**
     * 执行任务
     * @return 任务执行结果
     * */
    R execute() throws Exception;
}
