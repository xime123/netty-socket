package ifreecomm.nettydemo;

import android.os.Handler;
import android.os.Looper;

import java.net.InetSocketAddress;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.CharsetUtil;

/**
 * TCP 客户端
 *
 * @author hyde
 */
public class NettyClient {

    String SERVER_IP = "192.168.0.242";
    int SERVER_PORT = 20001;
    private static final int MAX_RECONNECT_COUNT = 5;
    public static final int RECONNECT_SECONDS = 30;
    public static final int RECONNECT_SECONDS_DELAY = 60;
    public static final int READ_TIMEOUT_SECONDS = 60;
    public static final int WRITE_TIMEOUT_SECONDS = 60;
    public static final int BOTH_TIMEOUT_SECONDS = 60;

    public int connect_time = 0;
    //这样定义是为了实现多线程共享内存
    private volatile int reconnectCount = 0;
    private volatile int reconnectTime = 0;
    // 线程池
    ScheduledExecutorService executor;
    NioEventLoopGroup workGroup, workGroup02;
    Bootstrap bootstrap, bootstrap02;

    public static Channel channel, channel02;

    private static NettyClient INSTANCE;
    //初始化一个 连接的异步线程
    private ConnectThread mConnectThread;
    private ConnectThread02 mConnectThread02;
    private Handler mainHandler = new Handler(Looper.getMainLooper());
    private boolean hashConnected = false;

    /**
     * 初始化客户端配置
     */
    public NettyClient() {
        workGroup = new NioEventLoopGroup(Runtime.getRuntime().availableProcessors() * 2);
        workGroup02 = new NioEventLoopGroup(Runtime.getRuntime().availableProcessors() * 2);
        executor = Executors.newScheduledThreadPool(1);
        bootstrap = new Bootstrap();
        bootstrap02 = new Bootstrap();
        initBootstrap();

    }

    public static NettyClient getInstance() {
        if (INSTANCE == null) {
            synchronized (NettyClient.class) {
                if (INSTANCE == null)
                    INSTANCE = new NettyClient();
            }
        }
        return INSTANCE;
    }


    /**
     * 开始连接
     */
    public synchronized void start(String tcpIp, int tcpPort) {

        SERVER_IP = tcpIp;
        SERVER_PORT = tcpPort;
        mConnectThread = new ConnectThread();
        mConnectThread.start();
    }

    /**
     * 开始连接
     */
    public synchronized void start02(String tcpIp, int tcpPort) {

        SERVER_IP = tcpIp;
        SERVER_PORT = tcpPort;
        mConnectThread02 = new ConnectThread02();
        mConnectThread02.start();
    }


    private void initBootstrap() {

        bootstrap
                .group(workGroup)   //这种属于 单线程Reactor模式；boss与 work 共用一个线程池；
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    protected void initChannel(SocketChannel socketChannel)
                            throws Exception {
                        ChannelPipeline p = socketChannel.pipeline();
                        //配置超时时间
                        p.addLast(
                                //增加连接超时时间定义
                                new IdleStateHandler(
                                        READ_TIMEOUT_SECONDS, WRITE_TIMEOUT_SECONDS,
                                        BOTH_TIMEOUT_SECONDS));
                        p.addLast("ping", new IdleStateHandler(20, 50, 0, TimeUnit.SECONDS));//5s未发送数据，回调userEventTriggered

                        //  ByteBuf delimiter = Upooled.copiedBuffer("$/%@^#/").getBytes();

                        //  p.addLast(new LineBasedFrameDecoder(1024));
                        //  p.addLast(new StringDecoder());
                        //  ByteBuf delimiter = Unpooled.copiedBuffer("n/".getBytes());
                        // socketChannel.pipeline().addLast(new DelimiterBasedFrameDecoder(1024, delimiter));

                        p.addLast(new StringEncoder(CharsetUtil.UTF_8));
                        p.addLast(new LineBasedFrameDecoder(1024));//黏包处理
                        p.addLast(new StringDecoder(CharsetUtil.UTF_8));

                        p.addLast(new NettyClientHandler());
                    }
                });
        bootstrap.option(ChannelOption.SO_RCVBUF, 1024);
        bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
        bootstrap.option(ChannelOption.TCP_NODELAY, true);

        bootstrap02
                .group(workGroup02)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    protected void initChannel(SocketChannel socketChannel)
                            throws Exception {
                        ChannelPipeline p = socketChannel.pipeline();
                        //配置超时时间
                        p.addLast(
                                //增加连接超时时间定义
                                new IdleStateHandler(
                                        READ_TIMEOUT_SECONDS, WRITE_TIMEOUT_SECONDS,
                                        BOTH_TIMEOUT_SECONDS));
                        p.addLast("ping", new IdleStateHandler(20, 50, 0, TimeUnit.SECONDS));//5s未发送数据，回调userEventTriggered
                        p.addLast(new StringEncoder(CharsetUtil.UTF_8));
                        p.addLast(new LineBasedFrameDecoder(1024));//黏包处理
                        p.addLast(new StringDecoder(CharsetUtil.UTF_8));
                        p.addLast(new NettyClientHandler());
                    }
                });
        bootstrap02.option(ChannelOption.SO_RCVBUF, 1024);
        bootstrap02.option(ChannelOption.SO_KEEPALIVE, true);
        bootstrap02.option(ChannelOption.TCP_NODELAY, true);


    }

    public void stop() {
        if (mConnectThread != null) {
            mConnectThread.interrupt();
            executor.shutdownNow();
            mConnectThread = null;
        }
        if (channel != null && channel.isActive()) {
            channel.close();
            channel = null;
        }
//        if (workGroup != null) {
//            try {
//                workGroup.shutdownGracefully().sync();
//            } catch (InterruptedException e) {
//                L.e(e.getMessage());
//            }
//        }

    }

    public boolean isAlive() {
        if (channel != null) {
            return channel.isActive();
        } else {
            return false;
        }
    }

    /**
     * 10s发起重连；
     */
    void doConnect(final String ip, final int port) {

        try {
            // 连接服务端
            ChannelFuture future = bootstrap.connect(new InetSocketAddress(ip, port)).sync();

            channel = future.sync().channel();
            printServerInformation();
            hashConnected = true;
            channel.closeFuture().sync();
        } catch (Exception e) {
            hashConnected = false;
            e.printStackTrace();
        } finally {

        }
    }

    void doConnect02(final String ip, final int port) {
        SERVER_IP = ip;
        SERVER_PORT = port;
        try {
            // 连接服务端
            ChannelFuture future = bootstrap.connect(new InetSocketAddress(ip, port)).sync();

            channel02 = future.sync().channel();
            printServerInformation();
            // channel02.closeFuture().sync();
        } catch (Exception e) {

            e.printStackTrace();
        } finally {
            //  doConnect02(ip,port);
        }
    }


    private void printServerInformation() {
        L.e("=====================================");
        L.e("client connect ip:" + SERVER_IP);
        L.e("client connect port:" + SERVER_PORT);
        L.e("client start");
        L.e("=====================================");
    }

    public boolean getConnectStatus() {
        return hashConnected;
    }

    public void sendMsgToServer(String msg, ChannelFutureListener channelFutureListener) {
        ChannelFuture future = channel.write(msg);
        future.addListener(channelFutureListener);
    }

    public class ConnectThread extends Thread {
        public void run() {
            super.run();
            if (this.isInterrupted()) {
                L.e("the thread is stop");
                return;
            }

            doConnect(SERVER_IP, SERVER_PORT);
        }
    }

    public class ConnectThread02 extends Thread {
        public void run() {
            super.run();
            if (this.isInterrupted()) {
                L.e("the thread is stop");
                return;
            }

            doConnect02(SERVER_IP, SERVER_PORT);
        }
    }

    public interface SendDataListener {
        void onSuccess();

        void onFailure();
    }


}
