package org.wjx.pool;

import org.springframework.scheduling.concurrent.DefaultManagedAwareThreadFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author xiu
 * @create 2023-12-06 18:57
 */
@Component
public class CustomThreadPool {
       public static ThreadPoolExecutor poolExecutor =new ThreadPoolExecutor(
               5,
               10,
               10,
               TimeUnit.MINUTES,
               new ArrayBlockingQueue<>(30),
               new DefaultManagedAwareThreadFactory());
}
