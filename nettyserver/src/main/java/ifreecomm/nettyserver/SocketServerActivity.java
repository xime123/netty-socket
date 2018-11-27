package ifreecomm.nettyserver;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * 基于TCP协议的socket通信，实现用户登录，服务端
 */
public class SocketServerActivity extends AppCompatActivity {
    private final static String TAG = SocketServerActivity.class.getSimpleName();
    private String IP = "";
    private int PORT = 10086;
    ServerSocket serverSocket;
    Socket socket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_socket_client);
    }


    public void doSend(View view) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                //获取输出流，响应客户端的请求
                OutputStream outputStream = null;
                try {
                    outputStream = socket.getOutputStream();
                    PrintWriter writer = new PrintWriter(outputStream);
                    writer.write("欢迎您");
                    writer.flush();
                    writer.close();
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }

    private void close() {

    }

    public void startServer(View view) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    //创建一个服务器端的socket，指定绑定端口号
                    serverSocket = new ServerSocket(PORT);
                    //调用accept方法开始监听，等待客户端连接
                    socket = serverSocket.accept();
                    //获取客户端的信息
                    InetAddress address = socket.getInetAddress();
                    printLogI("客户端地址=" + address.getHostName());
                    //获取输入流
                    InputStream inputStream = socket.getInputStream();
                    InputStreamReader reader = new InputStreamReader(inputStream);
                    BufferedReader br = new BufferedReader(reader);
                    String info;
                    while ((info = br.readLine()) != null) {
                        printLogI("我是服务器，客户端说：" + info);
                    }
                    socket.shutdownInput();//关闭输入流
                    //4、获取输出流，响应客户端的请求
                    OutputStream outputStream = socket.getOutputStream();
                    PrintWriter writer = new PrintWriter(outputStream);
                    writer.write("欢迎您");
                    writer.flush();
                    br.close();
                    reader.close();
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e(TAG, "服务器创建失败 e=" + e.getMessage());
                }
            }
        }).start();


    }

    private void printLogI(String msg) {
        Log.e(TAG, "msg="+msg);
    }
}
