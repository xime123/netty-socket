package ifreecomm.nettydemo.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

/**
 * NIO TCP客户端
 */
public class TCPClient {
    //信道选择器
    private Selector selector;
    //与服务通信的信道
    private SocketChannel socketChannel;

    private String hostIP;
    private int port;

    public TCPClient(String hostIP, int port) throws IOException {
        this.port = port;
        this.hostIP = hostIP;
        initialize();
    }

    /**
     * 初始化
     */
    private void initialize() throws IOException {
        //打开监听信道并设置为非阻塞模式
        socketChannel = SocketChannel.open(new InetSocketAddress(hostIP, port));
        socketChannel.configureBlocking(false);

        //打开并注册选择器到信道
        selector = Selector.open();
        socketChannel.register(selector, SelectionKey.OP_READ);
        //启动读线程
        new TCPClientReadThread(selector);
    }

    public void sendMsg(String msg) throws IOException {
        ByteBuffer writeBuffer = ByteBuffer.wrap(msg.getBytes("UTF-8"));
        socketChannel.write(writeBuffer);
    }
}
