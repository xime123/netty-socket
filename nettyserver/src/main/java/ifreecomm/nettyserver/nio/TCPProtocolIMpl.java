package ifreecomm.nettyserver.nio;

import android.util.Log;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Date;

public class TCPProtocolIMpl implements TCPProtocol {
    private static final String TAG = TCPProtocolIMpl.class.getSimpleName();
    private int bufferSize;

    public TCPProtocolIMpl(int bufferSize) {
        this.bufferSize = bufferSize;
    }

    @Override
    public void handleAccept(SelectionKey key) throws IOException {
        SocketChannel clientChannel = ((ServerSocketChannel) key.channel()).accept();
        clientChannel.configureBlocking(false);
        clientChannel.register(key.selector(), SelectionKey.OP_READ, ByteBuffer.allocate(bufferSize));
    }

    @Override
    public void handleRead(SelectionKey key) throws IOException {
        //获取与客户端通信渠道
        SocketChannel clientChannel = (SocketChannel) key.channel();
        //得到并清空缓冲区
        ByteBuffer buffer = (ByteBuffer) key.attachment();
        buffer.clear();
        long bytesRead = clientChannel.read(buffer);
        if (bytesRead == -1) {
            //没有读到数据的情况下 关闭
            clientChannel.close();
        } else {
            //将字节转发为UTF-16的字符串
            String receivedString = Charset.forName("UTF-8").newDecoder().decode(buffer).toString();
            Log.e(TAG, "接收到来自" + clientChannel.socket().getRemoteSocketAddress() + "的信息" + receivedString);
            //准备发送信息
            String sendString = "您好，客户端@" + new Date().toString() + ",已经接收到你的信息" + receivedString;
            buffer = ByteBuffer.wrap(sendString.getBytes("UTF-8"));
            clientChannel.write(buffer);
            //设置为下一次读取或者写入准备
            key.interestOps(SelectionKey.OP_READ|SelectionKey.OP_WRITE);

        }
    }

    @Override
    public void handleWrite(SelectionKey key) throws IOException {

    }
}
