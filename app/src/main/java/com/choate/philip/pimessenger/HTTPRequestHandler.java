package com.choate.philip.pimessenger;
import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import java.io.InputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import javax.net.ssl.HttpsURLConnection;
/**
 * Created by Philip on 9/28/2015.
 */
public class HTTPRequestHandler {
    final String TAG = "HTTPRequestHandler.java";
    static InputStream is = null;
    static JSONObject jObj = null;
    static String json = "";

    public JSONArray getJSONArrayFromURL(String url) throws JSONException {
        JSONArray jArray;
        jArray = new JSONArray("[]");
        // make HTTP Get request
        try {
            DefaultHttpClient httpClient = new DefaultHttpClient();

            HttpGet httpGet = new HttpGet(url);

            HttpResponse httpResponse = httpClient.execute(httpGet);
            HttpEntity httpEntity = httpResponse.getEntity();
            is = httpEntity.getContent();

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {

            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "iso-8859-1"), 8);
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
            is.close();
            json = sb.toString();

        } catch (Exception e) {
            Log.e(TAG, "Error converting result " + e.toString());
        }

        System.out.println("JSON String: " + json);
        // try parse the string to a JSON object
        try {
            jArray = new JSONArray(json);
        } catch (JSONException e) {
            Log.e(TAG, "Error parsing data " + e.toString());
        }

        // return JSON String
        return jArray;
    }

    public JSONObject getJSONFromUrl(String url, JSONObject obj) {

        // make HTTP request
        try {

            DefaultHttpClient httpClient = new DefaultHttpClient();


            String json=obj.toString();
            HttpPost httpPost = new HttpPost(url);
            httpPost.setHeader("Content-type", "application/json");
            StringEntity se = new StringEntity(obj.toString());
            se.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
            httpPost.setEntity(se);

            HttpResponse httpResponse = httpClient.execute(httpPost);
            HttpEntity httpEntity = httpResponse.getEntity();
            is = httpEntity.getContent();

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {

            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "iso-8859-1"), 8);
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
            is.close();
            json = sb.toString();

        } catch (Exception e) {
            Log.e(TAG, "Error converting result " + e.toString());
        }

        System.out.println("JSON String: " + json);
        // try parse the string to a JSON object
        try {
            jObj = new JSONObject(json);
        } catch (JSONException e) {
            Log.e(TAG, "Error parsing data " + e.toString());
        }

        // return JSON String
        return jObj;
    }
}
