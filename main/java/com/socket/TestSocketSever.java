package com.socket;

import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class TestSocketSever {
    public static void main(String[] args) throws Exception {
        //首先创建服务端口Socket，端口号为8416；
        ServerSocket serverSocket=new ServerSocket(8416);
        System.out.println("等待客户端连接。。。");
        //等待客户端连接，有客户端链接后返回客户端的Socket对象，否则线程一直阻塞
        Socket client=serverSocket.accept();
        System.out.println("有新客户连接，端口号为"+client.getPort());
        //获取客户短的输入输出流。
        Scanner clientInput=new Scanner(client.getInputStream());
        clientInput.useDelimiter("\n");
        PrintStream clientout=new PrintStream(client.getOutputStream(),
                true,"UTF-8");
        //读取客户端的输入
        if(clientInput.hasNext()){
            System.out.println(client.getInetAddress()+"说"+clientInput.next());
        }
        //向客户端输出
       clientout.println("I am Server");
        //关闭输入输出流
        clientInput.close();
        clientout.close();
        serverSocket.close();


    }

}
