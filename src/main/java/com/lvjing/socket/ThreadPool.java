package com.lvjing.socket;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * @Author:HuangHua
 * @Descirption: TCP服务线程池
 * @Date: Created by huanghua on 2018/6/25.
 * @Modified By:
 */
@SuppressWarnings("ALL")
@Component
public class ThreadPool{

    /**全局静态线程池**/
    private static ThreadPoolExecutor executor;

    /**核心线程池大小**/
    private static final int CORE_POOL_SIZE = 200;


    /**最大线程池大小**/
    private static final int MAXIMUM_POOL_SIZE = 1000;


    private static final Logger logger = LoggerFactory.getLogger(ThreadPool.class);

    @PostConstruct
    public  void startServer() throws IOException {
        try {
            ServerSocket serverSocket = new ServerSocket(8888);
            logger.info("the server is started...");

            //**创建线程池**//
            executor = new ThreadPoolExecutor(CORE_POOL_SIZE,
                    MAXIMUM_POOL_SIZE,
                    30,
                    TimeUnit.SECONDS,
                    new ArrayBlockingQueue<Runnable>(10));
            executor.allowCoreThreadTimeOut(true);
            while (true) {
                //**客户端接入**//
                Socket s = serverSocket.accept();
                logger.info("the connection,IP address is:" + s.getInetAddress());
                Thread t = new Thread(new ServerThread(s));
                executor.execute(t);
                logger.info("the number of thread in pool is：" + executor.getPoolSize() + " ,the number of thread in queue：" +
                        executor.getQueue().size() + " ,the number of thread completed：" + executor.getCompletedTaskCount());

                Thread.sleep(1000);
            //executor.shutdown();
            }
        }catch (Exception e){
            logger.error(e.getMessage());
        }
    }
}