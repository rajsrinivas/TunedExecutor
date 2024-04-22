package com.rajsrinivas.common.concurrent;



import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.atomic.AtomicLong;

public class BasicHttpClient implements Runnable {

    private static final String SERVER_URL_STRING = "any server here";
    private static HttpURLConnection con;
    private static byte[] postData = getPostData();

    private static byte[] getPostData() {
        InputStream ins = BasicHttpClient.class.getResourceAsStream("");
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            toByteArray(ins,os);
            return os.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static final int timesToRun = 1000;
    private static final Logger logger = LoggerFactory.getLogger(BasicHttpClient.class);
    private static final AtomicLong counter = new AtomicLong(1);

    public static void main(String[] args) {
        int parallelRuns = 100;
        for(int i = 0; i < parallelRuns ; i++){
            new Thread(new BasicHttpClient()).start();
        }
    }

    public void run(){
        for( int i = 0 ; i < timesToRun ; i++){
            try {
                long count = counter.incrementAndGet();
                logger.info("Started request: " + count);
                URL gateway = new URL(SERVER_URL_STRING);
                con = (HttpURLConnection) gateway.openConnection();
                con.setDoOutput(true);
                con.setRequestProperty("User-Agent", "BasicHttpClient - Java");
                con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                try(DataOutputStream wr = new DataOutputStream(con.getOutputStream())){
                    wr.write(postData);
                    wr.flush();
                }
                InputStream inputStream = con.getInputStream();
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                toByteArray(inputStream, os);

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void toByteArray(InputStream in, ByteArrayOutputStream os) throws IOException {
        byte[] buffer = new byte[1024];
        int len;
        while((len= in.read(buffer)) != -1){
            os.write(buffer,0,len);
        }
    }
}
