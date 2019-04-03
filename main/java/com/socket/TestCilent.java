package com.socket;

import com.sun.deploy.util.SessionState;
import com.sun.security.ntlm.Client;

import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Scanner;

public class TestCilent {
    public static void main(String[] args) {
        String serverName="127.0.0.1";
        Integer port=8416;
        //创建socket连接服务器
        try {
            Socket cilent=new Socket(serverName,port);
            System.out.println("连接上服务器，服务器地址为"+cilent.getInetAddress());
            //获取输入输出流
            PrintStream out=new PrintStream(cilent.getOutputStream(),
                    true,"UTF-8");
            Scanner in =new Scanner(cilent.getInputStream());
            in.useDelimiter("\n");
            //向服务器输出内容
            out.println("i am Cilent");
            //读取服务器输入
            if(in.hasNext()){
                System.out.println("向服务器发送消息为"+in.next());
                in.close();
                out.close();
                cilent.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}
