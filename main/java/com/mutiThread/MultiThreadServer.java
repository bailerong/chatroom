package com.mutiThread;

import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MultiThreadServer {
    //存储所有注册的客户端
    private static Map<String,Socket> ClientMap=new ConcurrentHashMap<String,Socket>();
    //具体处理与每个客户端之间通信的内部类
    private static class ExecuteClient implements Runnable{
        private Socket client;
        public ExecuteClient(Socket client){
            this.client=client;
        }

        @Override
        public void run() {
            try {
                //获取客户端输入流
                Scanner in =new Scanner(client.getInputStream());
                String strFromClient;
                while(true){
                    if(in.hasNextLine()){
                        strFromClient=in.nextLine();
                        //windows下将默认换行/r/n中的/r替换为空字符串
                        Pattern pattern=Pattern.compile("\r");
                        Matcher matcher=pattern.matcher(strFromClient);
                        strFromClient=matcher.replaceAll("");
                        //注册流程
                        if(strFromClient.startsWith("username")){
                            String username=strFromClient.split("\\:")[1];
                            registerUser(username,client);
                            continue;
                        }
                        //群聊流程
                        if(strFromClient.startsWith("G")){

                            String msg=strFromClient.split("\\:")[1];
                            groupChat(msg);
                            continue;
                        }
                        //私聊流程
                        if(strFromClient.startsWith("P")){
                            String userName=strFromClient.split("\\:")[1].split("-")[0];
                            String msg=strFromClient.split("\\:")[1].split("-")[1];
                            privateChat(userName,msg);
                        }
                        //用户退出
                        if(strFromClient.startsWith("bye")){
                            String userName=null;
                            //根据socket找到serName
                             for(String keyName:ClientMap.keySet()){
                                 if(ClientMap.get(keyName).equals(client)){
                                     userName=keyName;
                                 }
                             }
                            System.out.println("用户"+userName+"下线了！");
                             ClientMap.remove(userName);
                             continue;
                        }

                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        //群聊流程
        private void groupChat(String msg) {
            //取出clientMap中的所有Entry遍历发送群聊信息
            Set<Map.Entry<String,Socket>> ClientSet=ClientMap.entrySet();
            for(Map.Entry<String,Socket>entry:ClientSet){
                try {
                    Socket socket=entry.getValue();
                    //取每个客户端的输出流
                    PrintStream out=new PrintStream(socket.getOutputStream(),true,"UTF-8");
                    out.println("群聊信息为："+msg);

                } catch (IOException e) {
                    System.out.println("群聊异常，错误为：" + e);
                }
            }
        }
        private void privateChat(String useName,String msg){
            try {
                Socket privateSocket=ClientMap.get(useName);
                //打印输出流
                PrintStream out=new PrintStream(privateSocket.getOutputStream(),true,"UTF-8");
                out.println("私聊信息为："+msg);
            } catch (IOException e) {
                System.out.println("私聊异常，错误为："+e);
            }

        }

        private void registerUser(String userName,Socket client) {
            System.out.println("用户姓名为："+userName);
            System.out.println("用户"+userName+"上线了！");
            System.out.println("当前群聊人数为："+(ClientMap.size()+1)+"人");
            //将用户信息保存到map中
            ClientMap.put(userName,client);
            try{
                PrintStream out=new PrintStream(client.getOutputStream(),true,"UTF-8");
                //告知用户注册成功
                out.println("用户注册成功");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args)throws Exception {
        ExecutorService executorService= Executors.newFixedThreadPool(20);
        ServerSocket serverSocket=new ServerSocket(8416);
        for(int i=0;i<20;i++){
            System.out.println("等待客户端连接");
            Socket client=serverSocket.accept();
            System.out.println("有新客户端连接，端口号为："+client.getPort());
            executorService.submit(new ExecuteClient(client));

        }
        executorService.shutdown();
        serverSocket.close();
    }

}
