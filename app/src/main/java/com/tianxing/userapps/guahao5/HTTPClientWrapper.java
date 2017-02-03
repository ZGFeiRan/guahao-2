package com.tianxing.userapps.guahao5;

/**
 * Created by tao.li on 2015/6/28.
 */
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.ProtocolVersion;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.cookie.ClientCookie;
import org.apache.http.cookie.Cookie;
import org.apache.http.cookie.CookieSpecRegistry;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.params.HttpParams;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.client.HttpClient;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.DefaultedHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.util.EntityUtils;
import org.apache.http.protocol.HTTP;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.widget.EditText;
public class HTTPClientWrapper {
    private HttpParams httpParams;
    private DefaultHttpClient httpClient;
    private HttpContext localContext;

    private static final HTTPClientWrapper INSTANCE = new HTTPClientWrapper();

    private HTTPClientWrapper(){
        Logger.getLogger("org.apache.http.impl.client.DefaultHttpClient").setLevel(Level.FINEST);
        Logger.getLogger("org.apache.http.headers").setLevel(Level.FINEST);
        Logger.getLogger("org.apache.http.client.protocol.RequestAddCookies").setLevel(Level.FINEST);
        Logger.getLogger("org.apache.http.impl.conn.DefaultClientConnection").setLevel(Level.FINEST);
        Logger.getLogger("org.apache.commons.httpclient").setLevel(Level.FINEST);
        Logger.getLogger("httpclient").setLevel(Level.FINEST);
        getHttpClient();
    }
    public static HTTPClientWrapper getInstance(){
        return INSTANCE;
    }
    public HttpEntity doGet(String url, Map params, Map headers) {
        //Map params2 = new HashMap();
        //params2.put("hl", "zh-CN");
        //getHttpClient();
        // editText.setText(doGet(url2, params2));
        String paramStr = "";
        if (params != null) {
            Iterator iter = params.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry entry = (Map.Entry) iter.next();
                Object key = entry.getKey();
                Object val = entry.getValue();
                paramStr += paramStr = "&" + key + "=" + val;
            }
        }
        if (!paramStr.equals("")) {
            paramStr = paramStr.replaceFirst("&", "?");
            url += paramStr;
        }
        HttpGet httpRequest = new HttpGet(url);
        Iterator iter = headers.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            Object key = entry.getKey();
            Object val = entry.getValue();
            httpRequest.setHeader(key.toString(), val.toString());
        }
        //httpRequest.setHeader("Host","www.bjguahao.gov.cn");
        String host = getHostFromURL(url);
        httpRequest.setHeader("Host",host);
        //httpRequest.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:39.0) Gecko/20100101 Firefox/39.0");

        String errRes = "";
        HttpEntity result = null;
        try {
            HttpResponse httpResponse = httpClient.execute(httpRequest, localContext);
            if (httpResponse.getStatusLine().getStatusCode() == 200) {
                //errRes = EntityUtils.toString(httpResponse.getEntity());
                result = httpResponse.getEntity();
            } else {
                errRes = "Error Response: "
                        + httpResponse.getStatusLine().toString();
            }
        } catch (ClientProtocolException e) {
            errRes = e.getMessage().toString();
            e.printStackTrace();
        } catch (IOException e) {
            errRes = e.getMessage().toString();
            e.printStackTrace();
        } catch (Exception e) {
            //errRes = e.getMessage().toString();
            e.printStackTrace();
        }/*finally {
            get.releaseConnection();
        }*/
        if (!errRes.isEmpty()) {
            Log.v("errRes", errRes);
        }
        return result;
    }
    public HttpEntity doPost(String url, Map<String, String> params,  Map headers, String encode) {
        List<NameValuePair> paramsList = new ArrayList<NameValuePair>();
        Iterator<Map.Entry<String, String>> iter = params.entrySet().iterator();
        while (iter.hasNext())
        {
            Map.Entry entry = (Map.Entry) iter.next();
            String key = (String)entry.getKey();
            String val = (String)entry.getValue();
            paramsList.add(new BasicNameValuePair(key, val));
        }
        return doPost(url, paramsList, headers, encode);
    }

    public HttpEntity doPost(String url, List<NameValuePair> params,  Map headers, String encode) {
        HttpPost httpRequest = new HttpPost(url);
        Iterator iter = headers.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            Object key = entry.getKey();
            Object val = entry.getValue();
            httpRequest.setHeader(key.toString(), val.toString());
        }
        String host = getHostFromURL(url);
        httpRequest.setHeader("Host",host);
        httpRequest.setHeader("User-Agent",
            "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/55.0.2883.87 Safari/537.36");
        httpRequest.setHeader("Accept", "application/json, text/javascript, */*;q=0.01");
        httpRequest.setHeader("Origin", "http://m.bjguahao.gov.cn");
        httpRequest.setHeader("X-Requested-With", "XMLHttpRequest");

        String errRes = "";
        HttpEntity result = null;
        try {
            UrlEncodedFormEntity entity = new UrlEncodedFormEntity(params, "UTF-8");
            httpRequest.setEntity(entity);
            //entity.setContentEncoding("UTF-8");
            Log.d("content-type", entity.getContentType().toString());
            //Log.d("content charset", EntityUtils.getContentCharSet(entity));
            HttpResponse httpResponse = httpClient.execute(httpRequest, localContext);
            if (httpResponse.getStatusLine().getStatusCode() == 200) {
                result = httpResponse.getEntity();
            } else {
                errRes = "Error Response: "
                        + httpResponse.getStatusLine().toString();
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!errRes.isEmpty())
        {
            Log.v("errRes", errRes);
        }
        return result;
    }
    private String getHostFromURL(String url)
    {
        String host;
        String prefix = "http://";
        int pos1 = url.indexOf(prefix);
        if (pos1 < 0)
        {
            return "m.bjguahao.gov.cn";
        }
        pos1 += prefix.length();
        int pos2 = url.indexOf('/', pos1);
        host = url.substring(pos1, pos2);
        return host;
    }
    public HttpClient getHttpClient() {
        StrictMode.ThreadPolicy policy=new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        this.httpParams = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParams, 20 * 1000);
        HttpConnectionParams.setSoTimeout(httpParams, 20 * 1000);
        HttpConnectionParams.setSocketBufferSize(httpParams, 8192);
        HttpClientParams.setRedirecting(httpParams, true);
        //String userAgent = "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:39.0) Gecko/20100101 Firefox/39.0";
        //String userAgent = "Mozilla/5.0 (Linux; U; Android 4.2.2; en-us; sdk Build/JB_MR1.1) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30";
        String userAgent = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/49.0.2623.112 Safari/537.36";
        HttpProtocolParams.setUserAgent(httpParams, userAgent);
        HttpProtocolParams.setVersion(httpParams, new ProtocolVersion("HTTP", 1, 1));
        httpClient = new DefaultHttpClient(httpParams);
        httpClient.getParams().setParameter(ClientPNames.COOKIE_POLICY,
                CookiePolicy.BEST_MATCH);
        httpClient.getParams().setParameter(ClientPNames.COOKIE_POLICY, CookiePolicy.RFC_2109);
        httpClient.getParams().setParameter(ClientPNames.COOKIE_POLICY, CookiePolicy.BROWSER_COMPATIBILITY);
        CookieStore cookieStore = new BasicCookieStore();
        localContext = new BasicHttpContext();
        localContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);

        return httpClient;
    }
    public void setCookie(String k, String v)
    {
        CookieStore cookieStore = (CookieStore)localContext.getAttribute(ClientContext.COOKIE_STORE);
        BasicClientCookie cookie = new BasicClientCookie(k, v);
        Cookie c = cookieStore.getCookies().get(0);
        cookie.setDomain(c.getDomain());
        cookieStore.addCookie(cookie);
    }
    public String getCookie()
    {
        CookieStore cookieStore = (CookieStore)localContext.getAttribute(ClientContext.COOKIE_STORE);
        List<Cookie> cookies = cookieStore.getCookies();
        String cookieString = "";
        for (Cookie cookie : cookies) {
            cookieString += cookie.getName() + "=" + cookie.getValue() + "; domain=" + cookie.getDomain();
            return cookieString;
        }
        return cookieString;
    }
}
