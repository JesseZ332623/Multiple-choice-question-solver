package com.jesse.examination.config.asyncconfig;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
public class AsyncConfig
{
    /**
     * 对于发送邮件这样的耗时操作（执行时间 3s+），令其异步的执行更能提升效率（非阻塞，时间小于 100 ms）。
     * 因此为这个服务单独配置线程池，在并发场景下，邮件任务会提交到这个线程池执行。
     *
     * @return 配置好的服务专用线程池，被 Spring 扫描然后注入到需要的地方
     */
    @Bean
    public Executor emailSendExecutor()
    {
        /*
         * 关于 Spring 线程池的执行策略，这里我有必要做一些说明：
         *
         * 1. 核心线程优先：
         *      当外部提交任务时，若核心线程有空闲，则立即交由其执行。
         *
         * 2. 队列缓冲：
         *      默认使用有界队列 LinkedBlockingQueue<>，当核心线程都在忙时，
         *      把任务先暂存到该队列等待。
         *
         * 3. 线程扩展：
         *      当队列已满，则新建超出核心线程数的新线程去执行任务，
         *      但数量（核心线程数 + 新建线程数）不会超过 setMaxPoolSize() 所设的值。
         *
         * 4. 拒绝策略：
         *      满足线程数到达最大值且队列已满这两个条件时，
         *      对后续提交的任务则执行预设好地拒绝策略。
         */
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        executor.setCorePoolSize(5);                     // 常驻线程数量
        executor.setMaxPoolSize(20);                      // 最大线程数量
        executor.setQueueCapacity(50);                  // 最大队列数量

        /*
         * 当本线程池中的所有线程空闲超过 30 秒后，
         * 则回收一个线程临时线程，若下一次扫描依旧空闲，则再回收一个临时线程。
         * 这个过程会持续到池中只剩下 setCorePoolSize() 方法设置的常驻线程数量。
         */
        executor.setKeepAliveSeconds(30);

        /*
         * 倘若本线程池满载时，外部还在源源不断的提交任务，
         * 线程池就得想办法拒绝（reject）这些任务。
         * ThreadPoolTaskExecutor 默认有 4 种执行策略，按负责程度排序如下：
         *
         * 1. AbortPolicy           直接抛出 RejectedExecutionException
         *
         * 2. DiscardPolicy         默认丢弃被拒绝的任务
         *
         * 3. DiscardOldestPolicy   丢弃线程队列种最老的任务，
         *                          然后尝试让新提交的任务入队
         *
         * 4. CallerRunsPolicy      不丢弃队列中的任何任务，
         *                          直接把任务返还给提交任务的线程自己去执行
         */
        executor.setRejectedExecutionHandler(
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
        executor.setThreadNamePrefix("Email-Thread-");  // 线程名前缀
        executor.initialize();                          // 执行初始化

        return executor;
    }
}
