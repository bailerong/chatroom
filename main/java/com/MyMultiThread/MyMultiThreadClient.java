package com.MyMultiThread;

import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Scanner;

import static java.lang.System.out;
import static sun.java2d.cmm.ColorTransform.Out;

class ReadFromServer implements Runnable{
    private Socket client;
    public ReadFromServer(Socket client){
        this.client=client;
    }
    /*
        读取服务器端信息发来的线程
    */
    @Override
    public void run() {
        Scanner in= null;
        try {
            in = new Scanner(client.getInputStream());
            in.useDelimiter("\n");
            while(true){
                if(in.hasNextLine()){
                    out.println("从服务器发来的消息为："+in.next());
                }
                if(client.isClosed()){
                    out.println("客户端已经关闭");
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            in.close();
            try {
                client.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}
class WirteToServer implements Runnable{
    private Socket client;
    public WirteToServer(Socket client){
        this.client=client;
    }
    @Override
    public void run() {
        Scanner in=new Scanner(System.in);
        in.useDelimiter("\n");
        PrintStream out= null;
        try {
            out = new PrintStream(client.getOutputStream());
            while(true){
                out.println("请输入要发送的内容");
                String strToServer;
                if(in.hasNextLine()){
                    strToServer=in.nextLine().trim();
                    out.println(strToServer);
                    if(strToServer=="byebye"){
                        System.out.println("客户端关闭");
                        client.close();
                        in.close();
                        out.close();
                        break;
                    }
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
public class MyMultiThreadClient {
    public static void main(String[] args)throws Exception {

            Socket socket=new Socket("127.0.0.1",6666);
            Thread readMessage=new Thread(new ReadFromServer(socket));
            Thread wirte=new Thread(new WirteToServer(socket));
            readMessage.start();
            wirte.start();


    }
}
