package com.MyMultiThread;

import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//服务器端
public class MyMultiThreadServer {
    //我们需要一个我们的我们的Map结构来保存我们的K\V所有的客户的值，我们需要一边读取一遍来进行我们的
    //写入，所以我们的程序是一个高并发状态的程序，所以我们在实现的时候，用的是我们的ConcurrentHashMap
    private static Map<String, Socket> clientmap = new ConcurrentHashMap<String, Socket>();

    //具体处理与每个客户端之间关系的内部类
    public static class ExcuteClient implements Runnable {
        private Socket client;
        public ExcuteClient(Socket client) {
            this.client = client;
        }

        @Override
        public void run() {
            try {
                //获取客户端的输入流；
                Scanner in = new Scanner(client.getInputStream());
                //用来接收我们的客户端发来的消息。
                String strFromClient;
                while (true) {
                    strFromClient = in.nextLine();
                    //windows下的默认换行符为/r/n;所以我们为了能够在Linux环境和Window环境下都能够
                    //顺利的执行，我们将其/r换成我们的空字符
                    Pattern pattern = Pattern.compile("\r");
                    Matcher matcher = pattern.matcher(strFromClient);
                    strFromClient = matcher.replaceAll("");
                    //注册流程
                    if (strFromClient.startsWith("username")) {
                        String username = strFromClient.split("\\:")[1];
                        registerUser(username, client);
                        continue;
                    }
                    //群聊流程
                    if (strFromClient.startsWith("G")) {
                        String msg = strFromClient.split("\\:")[1];
                        groupChat(msg);
                        continue;
                    }
                    //私聊流程
                    if (strFromClient.startsWith("P")) {
                        String msg = strFromClient.split("\\:")[1].split("-")[1];
                        String username = strFromClient.split("\\:")[1].split("-")[0];
                        privateChat(username, msg);
                    }
                    //注销流程
                    if (strFromClient.contains("byebye")) {
                        String username = null;
                        //找到用户名称
                        //我们中的KaySet返回的是我们clientmap中key的镜像，
                        for (String keyname : clientmap.keySet()) {
                            if (clientmap.get(keyname).equals(client)) {
                                username = keyname;
                            }
                        }
                        System.out.println("用户：" + username + "下线了");
                        clientmap.remove(username);
                        continue;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        //私聊流程
        public void privateChat(String username, String msg) {
            Socket privateSocket = clientmap.get(username);
            try {
                //我们的屏幕也是一个缓冲区，我们设置自动刷新它，然后将我们的对象内容打印到屏幕上
                PrintStream out = new PrintStream(client.getOutputStream(),
                        true, "UTF-8");
                out.println("私聊信息为：" + msg);
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        //用户注册
        public void registerUser(String username, Socket client) {
            System.out.println(username + "注册成功了");
            System.out.println("用户" + username + "注册成功了");
            System.out.println("当前群的人数为：" + (clientmap.size() + 1) + "人");
            //将用户信息保存到map中
            clientmap.put(username, client);
            try {
                PrintStream out = new PrintStream(client.getOutputStream(), true, "UTF-8");
                out.println("用户注册成功");
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        //群聊流程
        public void groupChat(String msg) {
            Set<Map.Entry<String, Socket>> clientSet = clientmap.entrySet();
            for (Map.Entry<String, Socket> entry : clientSet) {
                Socket socket = entry.getValue();
                try {
                    PrintStream out = new PrintStream(socket.getOutputStream(), true, "UTF-8");
                    out.println("群聊信息：" + msg);
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
        public static void main(String[] args) {
            ExecutorService excuteClient = Executors.newFixedThreadPool(20);
            try {
                ServerSocket serverSocket=new ServerSocket(6666);
                for(int i=0;i<20;i++){
                    System.out.println("等待客户端连接：");
                    Socket client=serverSocket.accept();
                    System.out.println("等待客户端连接，端口号为："+client.getPort());
                    excuteClient.submit(new ExcuteClient(client));
                }
                excuteClient.shutdown();
                serverSocket.close();

            } catch (IOException e) {
                e.printStackTrace();
            }

        }

  }
