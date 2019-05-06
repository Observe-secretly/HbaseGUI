package com.lm.hbase.util.network;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.SecureRandom;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

public class HttpURLConnectionFactory {

    public static int DEFAULT_CONN_TIMEOUR = 5000;

    public static HttpURLConnection getConn(String url) throws Throwable {
        HttpURLConnection conn = null;
        URL http = new URL(url);
        if (url.startsWith("https:")) {
            HttpsURLConnection httpsConn = null;
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(new KeyManager[0], new TrustManager[] { new MyX509TrustManager() }, new SecureRandom());
            SSLSocketFactory ssf = sslContext.getSocketFactory();
            httpsConn = (HttpsURLConnection) http.openConnection();
            httpsConn.setSSLSocketFactory(ssf);
            httpsConn.setHostnameVerifier(new HostnameVerifier() {

                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });
            conn = httpsConn;
        } else {
            conn = (HttpURLConnection) http.openConnection();
        }

        return conn;
    }

    public static void downloadFile(HttpURLConnection con, String descDir, String fileName) {

        InputStream is = null;
        OutputStream os = null;
        try {
            File descFile = new File(descDir + System.getProperty("file.separator") + fileName);
            addConnProp(con, "GET", true);
            is = con.getInputStream();
            os = new FileOutputStream(descFile);
            int size = 0;
            byte[] buf = new byte[1024];
            while ((size = is.read(buf)) != -1) {
                os.write(buf, 0, size);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (os != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    public static String sendGet(HttpURLConnection con) throws Throwable {
        addConnProp(con, "GET", true);
        BufferedReader br = null;
        StringBuffer resultBuffer = new StringBuffer();
        try {
            br = new BufferedReader(new InputStreamReader(con.getInputStream(), "utf-8"));
            String temp;
            while ((temp = br.readLine()) != null) {
                resultBuffer.append(temp);
            }
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    br = null;
                    throw new RuntimeException(e);
                } finally {
                    if (con != null) {
                        con.disconnect();
                        con = null;
                    }
                }
            }
        }
        return resultBuffer.toString();
    }

    public static String sendPost(HttpURLConnection con, String body) throws Throwable {
        addConnProp(con, "POST", true);
        OutputStream outStream = null;
        BufferedReader responseReader = null;
        StringBuffer sb = new StringBuffer();
        if (body != null) {
            outStream = con.getOutputStream();
            byte[] data = body.getBytes();
            outStream.write(data);
            outStream.flush();
            outStream.close();
        }

        int resultCode = con.getResponseCode();

        if (HttpURLConnection.HTTP_OK == resultCode) {
            String readLine = new String();
            responseReader = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"));
            while ((readLine = responseReader.readLine()) != null) {
                sb.append(readLine).append("\n");
            }
            responseReader.close();
        }
        return sb.toString();
    }

    private static void addConnProp(HttpURLConnection conn, String method, boolean flag) throws Throwable {

        conn.setRequestMethod(method);
        conn.setConnectTimeout(DEFAULT_CONN_TIMEOUR);
        conn.setRequestProperty("accept", "*/*");
        // conn.setRequestProperty("Content-Type",
        // "application/x-www-form-urlencoded");
        conn.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
        conn.setUseCaches(false);
        if (flag) {
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setRequestProperty("connection", "Keep-Alive");
        }
    }
}
