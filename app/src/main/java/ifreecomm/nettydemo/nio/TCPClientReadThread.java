package ifreecomm.nettydemo.nio;

import android.util.Log;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;

public class TCPClientReadThread implements Runnable {
    private Selector selector;

    public TCPClientReadThread(Selector selector) {
        this.selector = selector;
        new Thread(this).start();
    }


    @Override
    public void run() {
        try {
            while (selector.select() > 0) {
                //遍历每个有可用IO操作Channel对应的SelectionKey
                for (SelectionKey key : selector.keys()) {
                    //如果该SelectionKey对应的channel中有可读的数据
                    if (key.isReadable()) {
                        //使用NIO读取Channel中的数据
                        SocketChannel channel = (SocketChannel) key.channel();
                        ByteBuffer buffer = ByteBuffer.allocate(1024);
                        channel.read(buffer);
                        buffer.flip();
                        //将字节转化为UTF-16的字符串
                        String receiveString = Charset.forName("UTF-8").newDecoder().decode(buffer).toString();
                        Log.i("TCPClientReadThread", "客户端接到到来自服务器的数据:" + channel.socket().getRemoteSocketAddress() + ":::" + receiveString);
                        //为下一次读取做准备
                        key.interestOps(SelectionKey.OP_READ);
                        //移除已经处理过的key
                        selector.selectedKeys().remove(key);
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
