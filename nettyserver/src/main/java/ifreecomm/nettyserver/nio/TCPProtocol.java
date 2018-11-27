package ifreecomm.nettyserver.nio;

import java.io.IOException;
import java.nio.channels.SelectionKey;

public interface TCPProtocol {
    /**
     * 接收一个SocketChannel
     *
     * @param key
     * @throws IOException
     */
    void handleAccept(SelectionKey key) throws IOException;


    /**
     * 从一个SocketChannel读取数据处理
     *
     * @param key
     * @throws IOException
     */
    void handleRead(SelectionKey key) throws IOException;

    /**
     * 向一个socketchannel写如数据
     *
     * @param key
     * @throws IOException
     */
    void handleWrite(SelectionKey key) throws IOException;
}
