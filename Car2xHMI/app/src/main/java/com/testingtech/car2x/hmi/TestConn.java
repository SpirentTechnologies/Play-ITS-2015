package com.testingtech.car2x.hmi;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class TestConn implements  Runnable{

    public void run(){
        try{

            Socket socket = new Socket(Globals.serverIp, 20000);

            System.out.println("my addr " + socket.getLocalAddress());
            System.out.println("my port " + socket.getLocalPort());
            System.out.println("my soc addr " + socket.getLocalSocketAddress());


            OutputStream os = socket.getOutputStream();
            OutputStreamWriter osw = new OutputStreamWriter(os);

            System.out.println("start");
            String s = "hello iam the greatest ever (phil)";
            osw.write(s);
            osw.flush();

            InputStream is = socket.getInputStream();
            byte[] buffer = new byte[1000];
            is.read(buffer);

            String received = new String(buffer);
            System.out.println("RECEIVED: " + received);

            socket.close();

            /*
            ServerSocket ss = new ServerSocket(30000);
            System.out.println("Waiting for conn");
            Socket s = ss.accept();
            System.out.println("Connected");
            InputStream is = s.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            char[] buffer = new char[1000];
            isr.read(buffer);

            String input = new String(buffer);
            System.out.println("RECEIVED: "+ input);
            */

        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
