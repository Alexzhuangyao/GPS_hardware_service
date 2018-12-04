package com.lvjing.socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.EOFException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executor;

/**
 * @Author:HuangHua
 * @Descirption:
 * @Date: Created by huanghua on 2018/6/15.
 * @Modified By:
 */

//@Component
public class ServerPool {

    /**连接池大小**/
    private static final int THREADPOOLSIZE = 200;

    private static final Logger logger = LoggerFactory.getLogger(ServerPool.class);

    //@PostConstruct
    //以下注释忽略代码中死循环的报错；
    @SuppressWarnings("InfiniteLoopStatement")
    public  void startServer() throws IOException {
        int count = 1;
        try {
            /**
             * 创建一个ServerSocket对象，并给它制定一个端口号，
             * 通过这个端口号来监听客户端的连接，服务端接受客户端连接的请求是
             * 不间断地接受的，所以服务端的编程一般都永无休止的运行
             */
            ServerSocket ss = new ServerSocket(8888);

            logger.info("the server is started...");
            while (true) {
                /**
                 * 在服务端调用accept()方法接受客户端的连接对象，accept()方法是
                 * 一个阻塞式的方法，一直傻傻地等待着是否有客户端申请连接
                 */

                Socket s = ss.accept();
                logger.info("the " + count + "th connection,IP address is:"
                        + s.getInetAddress());
                count++;
                /**
                 * 服务端使用多线程方便多客户端的连接
                 * 这里将服务端的socket传给内部类，方便每个客户端都创建一个线程
                 */
                Thread t = new Thread(new ServerThread(s));
                t.start();
            }
        } catch (IOException e) {
            logger.error(e.getMessage());
        }

    }
}
