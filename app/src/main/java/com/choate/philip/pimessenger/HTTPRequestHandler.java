package com.choate.philip.pimessenger;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLStreamHandler;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

//import org.json.simple.JSONObject;

import javax.net.ssl.HttpsURLConnection;
/**
 * Created by Philip on 9/28/2015.
 */
public class HTTPRequestHandler {
    public static void main(String[] args) throws Exception {

        HTTPRequestHandler http = new HTTPRequestHandler();
        System.out.println("\nTesting - Send Http POST request");
        JSONObject cred = new JSONObject();
        cred.put("name", "ian2");
        cred.put("email", "ian2@thing.com");
        cred.put("password", "ian2");
        http.sendPost("http://piemessengerbackend.herokuapp.com/users/register", cred);

    }
    // HTTP POST request
    private void sendPost(String url, JSONObject postObj) throws Exception {
        URL obj=new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        //add reuqest header
        con.setDoOutput(true);
        con.setDoInput(true);
        con.setRequestProperty("Content-Type", "application/json");
        con.setRequestProperty("Accept", "application/json");
        con.setRequestMethod("POST");

        // Send post request
        OutputStreamWriter wr = new OutputStreamWriter(con.getOutputStream());
        wr.write(postObj.toString());
        wr.flush();

        StringBuilder sb = new StringBuilder();
        int HttpResult = con.getResponseCode();
        if(HttpResult == HttpURLConnection.HTTP_OK){
            BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(),"utf-8"));
            String line = null;
            while ((line = br.readLine()) != null) {
                System.out.println(line);
                sb.append("\"" + line + "\"");
            }

            br.close();
            String jsonString = sb.toString();
            JSONObject js = new JSONObject("{\"success\":false, \"errorMessage\":\"Application with appId : [randomAppId] not registered\", \"errorCode\":102}");
            System.out.println(sb.toString());

        }else{
            System.out.println(con.getResponseMessage());
        }
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
