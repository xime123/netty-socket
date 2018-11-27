package ifreecomm.nettyserver.nio;

import android.util.Log;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.Iterator;

/**
 * NIO TCP服务端
 */
public class TCPServer {
    private static final String TAG = TCPServer.class.getSimpleName();
    //缓冲区大小
    private static final int BUFFER_SIZE = 1024;
    //设置超时时间
    private static final int TIME_OUT = 10000;
    //设置本地监听端口
    private static final int LISTEN_PORT = 1990;

    public static void startServer() {

        try {
            //创建选择器
            Selector selector = Selector.open();
            //打开监听信道
            ServerSocketChannel listenerChannel = ServerSocketChannel.open();
            //监听信道与本地端口绑定
            listenerChannel.socket().bind(new InetSocketAddress(LISTEN_PORT));
            //设置为非阻塞模式
            listenerChannel.configureBlocking(false);
            //将选择器绑定到监听信道，只有非阻塞信道才可以这册选择器，并在注册过程中指出该信道可以进行accept操作
            listenerChannel.register(selector, SelectionKey.OP_ACCEPT);
            //创建一个处理协议的实现累，由他来具体操作
            TCPProtocol protocol = new TCPProtocolIMpl(BUFFER_SIZE);
            //反复循环等待io
            while (true) {
                //等到某个信道就绪（或者超时）
                if (selector.select(TIME_OUT) == 0) {
                    Log.i(TAG, "独自等待");
                    continue;
                }
                //取迭代器selectKey()中包含来每个准备好某一I/O操作的信道的SelectionKey
                Iterator<SelectionKey> keyIterator = selector.selectedKeys().iterator();
                while (keyIterator.hasNext()) {
                    SelectionKey key = keyIterator.next();
                    try {
                        if (key.isAcceptable()) {
                            //有客户端连接请求时
                            protocol.handleAccept(key);
                        }

                        if (key.isReadable()) {
                            //从客户端读取数据
                            protocol.handleRead(key);
                        }

                        if (key.isValid() && key.isWritable()) {
                            //客户端可写时
                            protocol.handleWrite(key);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        //出现IO异常（如客户端断开连接）时移除处理过的key
                        keyIterator.remove();
                        continue;
                    }
                    //移除处理过的key
                    keyIterator.remove();
                    continue;
                }

            }
        } catch (IOException e) {
            Log.e(TAG, "tcp 服务启动失败  e=" + e.getMessage());
            e.printStackTrace();
        }

    }

}
