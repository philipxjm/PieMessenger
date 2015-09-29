package com.choate.philip.pimessenger;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLStreamHandler;

import org.json.simple.JSONObject;

import javax.net.ssl.HttpsURLConnection;
/**
 * Created by Philip on 9/28/2015.
 */
public class HTTPRequestHandler {
    private final String USER_AGENT = "Mozilla/5.0";

    public static void main(String[] args) throws Exception {

        HTTPRequestHandler http = new HTTPRequestHandler();
        System.out.println("\nTesting - Send Http POST request");
        http.sendPost();

    }
    // HTTP POST request
    private void sendPost() throws Exception {

        String url = "http://piemessengerbackend.herokuapp.com/users/register";
        URLStreamHandler handler = getURLStreamHandler("http");
        URL obj = new URL(null, url, handler);
        HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();

        //add reuqest header
        con.setDoOutput(true);
        con.setDoInput(true);
        con.setRequestProperty("Content-Type", "application/json");
        con.setRequestProperty("Accept", "application/json");
        con.setRequestMethod("POST");


        // Send post request
        con.setDoOutput(true);
        DataOutputStream wr = new DataOutputStream(con.getOutputStream());
        wr.flush();
        wr.close();

        int responseCode = con.getResponseCode();
        System.out.println("\nSending 'POST' request to URL : " + url);
        System.out.println("Response Code : " + responseCode);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        //print result
        System.out.println(response.toString());

    }

    public static URLStreamHandler getURLStreamHandler(String protocol) {
        try {
            Method method = URL.class.getDeclaredMethod("getURLStreamHandler", String.class);
            method.setAccessible(true);
            return (URLStreamHandler) method.invoke(null, protocol);
        } catch (Exception e) {
            return null;
        }
    }
}
