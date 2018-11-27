package ifreecomm.nettydemo;

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
import java.net.Socket;

public class SocketClientActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_socket_client);
    }

    public void doConnect(View view) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    //1、创建客户端Socket，指定服务器地址和端口
                    Socket socket=new Socket(Const.HOST,10086);
                    //2、获取输出流，向服务器端发送信息
                    OutputStream os=socket.getOutputStream();
                    PrintWriter printWriter=new PrintWriter(os);
                    printWriter.write("用户名：zhangsan;密码:'123'");
                    printWriter.flush();
                    socket.shutdownOutput();
                    //3、获取输入流，并读取服务器端的响应信息
                    InputStream is=socket.getInputStream();
                    InputStreamReader isr=new InputStreamReader(is);
                    BufferedReader bfr=new BufferedReader(isr);
                    String info;
                    while ((info=bfr.readLine())!=null){
                        Log.e("SocketServerActivity","我说客户端，服务器说："+info);
                    }
                    //关闭资源
                    bfr.close();
                    isr.close();
                    is.close();
                    printWriter.close();
                    os.close();
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }

    public void doSend(View view) {
    }
}
