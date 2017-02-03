package com.tianxing.userapps.guahao5;

import android.app.Activity;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;

import org.apache.http.HttpEntity;
import org.apache.http.cookie.Cookie;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnSubscribeFragmentListener} interface
 * to handle interaction events.
 * Use the {@link SubscribeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SubscribeFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    String mHpid;
    String mKeid;
    String mDate1;
    String mKeyword;
    String mSubscribeURL;
    String mDatid;
    String mdxCodeGetParam;
    Integer mCheckCanSubscribeTimes = 0;
    private Map<String, String> mSubscribeInputKV = new HashMap<String, String>();
    WebView mWebView = null;

    private Handler mCheckCanSubscribeMsgHandler = null;
    private Handler mGetSubscribePageMsgHandler = null;
    private Handler mGetdxCodeMsgHandler = null;
    private Handler mSubscribeMsgHandler = null;
    private Timer mTimer = null;

    private TimerTask mCurTimerTask = null;

    private OnSubscribeFragmentListener mListener = null;

    private EditText mEditTextDebugMsgName;
    private EditText mEditTextDebugMsg;
    private EditText mEditTextdxCode;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SubscribeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SubscribeFragment newInstance(String param1, String param2) {
        SubscribeFragment fragment = new SubscribeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public SubscribeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_subscribe, container, false);
        CookieSyncManager.createInstance(getActivity().getApplicationContext());
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        //cookieManager.setAcceptThirdPartyCookies(mWebView, true);
        CookieSyncManager.getInstance().sync();
        String cookie = HTTPClientWrapper.getInstance().getCookie();
        cookieManager.removeAllCookie();
        cookieManager.setCookie(HTTPSessionStatus.URL_BASE, cookie);//cookies是在HttpClient中获得的cookie
        CookieSyncManager.getInstance().sync();

        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                if (url.equals(HTTPSessionStatus.URL_BASE + mSubscribeURL))
                {
                    mWebView.loadUrl("javascript:window.htmlCallback.getHTML('<html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>', 'GetSubscribePage');");
                }
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }


            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, String url)
            {
                return super.shouldInterceptRequest(view, url);
            }
        });

        MainActivity activity = (MainActivity)getActivity();
        mHpid = activity.mCurHospitalID;
        mKeid = activity.mCurDepID;
        mDate1 = activity.mCurDate;
        mKeyword = activity.mDoctorKeyword;
        mTimer = new Timer(true);

        Looper looper = Looper.myLooper();
        mCheckCanSubscribeMsgHandler = new CheckCanSubscribeMsgHandler(looper);
        mGetSubscribePageMsgHandler = new GetSubscribePageMsgHandler(looper);
        mGetdxCodeMsgHandler = new GetdxCodeMsgHandler(looper);
        mSubscribeMsgHandler = new SubscribeMsgHandler(looper);

        final Button mSubscribeButton = (Button) v.findViewById(R.id.buttonSubscribe);
        mSubscribeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onSubscribeClick();
            }
        });
        mEditTextDebugMsgName = (EditText)v.findViewById(R.id.editTextDebugMsgName);
        mEditTextDebugMsg = (EditText)v.findViewById(R.id.editTextDebugMsg);
        mEditTextdxCode = (EditText)v.findViewById(R.id.editTextdxCode);

        mEditTextDebugMsgName.setText("CheckCanSubscribe");
        mEditTextDebugMsg.setText("");

        mCheckCanSubscribeTimes = 0;
        ScheduleTask(new CheckCanSubscribeTimerTask(), 2000);
        return v;
    }
    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onSubscribeSuccess();
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnSubscribeFragmentListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnSubscribeFragmentListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnSubscribeFragmentListener {
        public void onSubscribeSuccess();
    }
    class HTMLCallback
    {
        @JavascriptInterface
        public void getHTML(String html, String url)
        {
            if (url.equals("GetSubscribePage"))
            {
                Message message = Message.obtain();
                message.obj = html;
                mGetSubscribePageMsgHandler.sendMessage(message);
            }
            else if(url.equals("GetdxCode"))
            {
                Message message = Message.obtain();
                message.obj = html;
                mGetdxCodeMsgHandler.sendMessage(message);
            }
            else if (url.equals("Subscribe")&&html.length() > 0)
            {
                Message message = Message.obtain();
                message.obj = html;
                mSubscribeMsgHandler.sendMessage(message);
            }
        }
    }
    private void ScheduleTask(TimerTask task, int ms)
    {
        if (mTimer != null)
        {
            if (mCurTimerTask != null)
            {
                mCurTimerTask.cancel();
            }
            //mTimer.cancel();
            mTimer.purge();
        }
        mCurTimerTask = task;
        if (mCurTimerTask != null)
        {
            mTimer.schedule(mCurTimerTask, 0, ms);
        }
    }
    public class  CheckCanSubscribeTimerTask extends TimerTask {
        @Override
        public void run() {
            String canSubscribeRet = checkCanSubscribe();
            if (!canSubscribeRet.isEmpty()) {
                Message message = Message.obtain();
                message.obj = canSubscribeRet;
                mCheckCanSubscribeMsgHandler.sendMessage(message);
            }

        }
    }
    public class GetSubscribePageTimerTask extends TimerTask
    {
            @Override
            public void run() {
            String canSubscribeRet = GetSubscribePage();
            if (!canSubscribeRet.isEmpty()) {
                Message message = Message.obtain();
                message.obj = canSubscribeRet;
                mGetSubscribePageMsgHandler.sendMessage(message);
            }
        }
    }

    public class GetdxCodeTimerTask extends TimerTask
    {
        @Override
        public void run() {
            String canSubscribeRet = GetdxCode();
            if (!canSubscribeRet.isEmpty()) {
                Message message = Message.obtain();
                message.obj = canSubscribeRet;
                mGetdxCodeMsgHandler.sendMessage(message);
            }
        }
    }

    public class SubscribeTimerTask extends TimerTask
    {
        @Override
        public void run() {
            String subscribeRet = Subscribe();
            if (!subscribeRet.isEmpty()) {
                Message message = Message.obtain();
                message.obj = subscribeRet;
                mSubscribeMsgHandler.sendMessage(message);
            }
        }
    }
    public void onSubscribeClick()
    {
        String dxCode = mEditTextdxCode.getText().toString();
        if (dxCode.isEmpty())
        {
            mEditTextdxCode.setError(getString(R.string.hello_world));
            mEditTextdxCode.requestFocus();
            return;
        }
        mSubscribeInputKV.put("dxcode", dxCode);
        //ScheduleTask(new SubscribeTimerTask(), 100);
        Subscribe();
    }
    protected  String SubscribeByWebview(String orderURL, Map headers)
    {
        //document.body.innerHTML
        //String js = "testt();var int=window.setInterval(function(){clearInterval(int);window.htmlCallback.getHTML(document, 'Subscribe');},500);";
        String dxCode = mEditTextdxCode.getText().toString();
        //String url = String.format("javascript:$(\"#dxcode1\").val(\"%s\");testt();var int=window.setInterval(function(){clearInterval(int);window.htmlCallback.getHTML(document.body.innerHTML, 'Subscribe');},1000);", dxCode);
        String url = String.format("javascript:$(\"#dxcode1\").val(\"%s\");testt();", dxCode);
        //refer http://www.bjguahao.gov.cn/comm/xiehe/guahao.php?hpid=1&ksid=1220010&datid=385718&jiuz=&ybkh=&hzname=&hzsfz=
        mWebView.loadUrl(url, headers);//TODO
        return "";
    }
    protected  String SubscribeByHTTPClientWrapper(String orderURL, Map headers)
    {
        HttpEntity entity = HTTPClientWrapper.getInstance().doPost(orderURL, mSubscribeInputKV, headers, "gb2312");
        try {
            return EntityUtils.toString(entity);
        } catch (Exception e) {
            //strResult = e.getMessage().toString();
            e.printStackTrace();
        }
        return "";
    }
    protected String Subscribe()
    {
        Map headers = new HashMap();
        String subscribeURL = HTTPSessionStatus.URL_BASE + mSubscribeURL;
        headers.put("Referer", subscribeURL);
        headers.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        headers.put("Accept-Language", "zh-CN,zh;q=0.8,en-US;q=0.5,en;q=0.3");
        //headers.put("Accept-Encoding", "gzip, deflate");
        String orderURL = subscribeURL.substring(0, subscribeURL.lastIndexOf('/')+1) + "ghdown.php";
        return SubscribeByWebview(orderURL, headers);
        //return SubscribeByHTTPClientWrapper(orderURL, headers);
    }
    class SubscribeMsgHandler extends Handler {
        public SubscribeMsgHandler(Looper looper) {
            super(looper);
        }
        @Override
        public void handleMessage(Message msg) {
            String httpGetRet = (String) msg.obj;
            Log.d("SubscribeMsg", httpGetRet);
            String prefix = httpGetRet.substring(0, 3);
            if (!prefix.equals("err"))
            {
                ScheduleTask(null, 0);
                mEditTextDebugMsgName.setText("Subscribe");
                mEditTextDebugMsg.setText("Success");
            }
        }
    }

    protected String checkCanSubscribe() {
        Map headers = new HashMap();
        headers.put("Referer", HTTPSessionStatus.URL_INDEX);
        //hpid=1&keid=1400116&date1='
        String url = String.format("%s?hpid=%s&keid=%s&date1=%s", HTTPSessionStatus.URL_SEARCH, mHpid, mKeid, mDate1);
        HttpEntity entity = HTTPClientWrapper.getInstance().doGet(url, new HashMap(), headers);
        if (entity == null)
        {
            return "";
        }
        try {
            return EntityUtils.toString(entity);
        } catch (Exception e) {
            //strResult = e.getMessage().toString();
            e.printStackTrace();
        }
        return "";
    }
    protected String GetSubscribePage() {
        Map headers = new HashMap();
        headers.put("Referer", HTTPSessionStatus.URL_INDEX);
        //http://www.bjguahao.gov.cn/comm/xiehe/guahao.php?hpid=1&ksid=1220011&datid=366172
        String url = HTTPSessionStatus.URL_BASE + mSubscribeURL;
        return GetSubscribePageByWebview(url, headers);
    }
    protected String GetSubscribePageByWebview(String url, Map headers)
    {
        mWebView.setVisibility(View.VISIBLE);
        mWebView.loadUrl(url, headers);
        //TODO get and pass HTML page to check if login
        return "success";
    }
    protected String GetSubscribePageByHTTPClientWrapper(String url, Map headers)
    {
        HttpEntity entity = HTTPClientWrapper.getInstance().doGet(url, new HashMap(), headers);
        try {
            return EntityUtils.toString(entity);
        } catch (Exception e) {
            //strResult = e.getMessage().toString();
            e.printStackTrace();
        }
        return "";
    }
    protected String GetdxCode()
    {
        //return GetdxCodeByHTTPClientWrapper();
        return GetdxCodeByWebview();
    }
    protected String GetdxCodeByWebview()
    {
        String js = "$(\".guahao input:button\").click();var int=window.setInterval(function(){if($(\"#tian\").html().trim().length>0){clearInterval(int);window.htmlCallback.getHTML(document.getElementById('tian').innerHTML, 'GetdxCode');}},500);";
        //mWebView.loadUrl("javascript:"+js);//TODO
        //mWebView.loadUrl("javascript:window.htmlCallback.getHTML(document.getElementById('tian').innerHTML, 'GetdxCode');");
        return "";
    }
    protected String GetdxCodeByHTTPClientWrapper() {
        Map headers = new HashMap();
        headers.put("Referer",  HTTPSessionStatus.URL_BASE + mSubscribeURL);
        headers.put("Accept", "*/*");
        //headers.put("Accept-Encoding", "gzip, deflate");
        headers.put("Accept-Language", "zh-CN,zh;q=0.8,en-US;q=0.5,en;q=0.3");
        headers.put("Connection", "keep-alive");
        headers.put("X-Requested-With", "XMLHttpRequest");
        HTTPClientWrapper.getInstance().setCookie("Hm_lpvt_65f844e6a6e140ab52d02690ed38a38b", "1437122394");
        HTTPClientWrapper.getInstance().setCookie("Hm_lvt_65f844e6a6e140ab52d02690ed38a38b", "1436480663,1436506581,1436704503,1437014225");
        String url = String.format("%s?ksid=%s&hpid=%s&datid=%s&jiuz=&ybkh=&baoxiao=0%s",
                HTTPSessionStatus.URL_GETCODEOFPHONE, mKeid, mHpid, mDatid, mdxCodeGetParam);
        Log.d("GetdxCode url", url);
        HttpEntity entity = HTTPClientWrapper.getInstance().doGet(url, new HashMap(), headers);

        String ret="";
        try {
            ret =  EntityUtils.toString(entity);
        } catch (Exception e) {
            //strResult = e.getMessage().toString();
            e.printStackTrace();
        }
        return ret;
    }

    public String GetDatidFromSubscribeURL()
    {
        if (mSubscribeURL.isEmpty())
        {
            return "";
        }
        String patternStr = ".+datid=([0-9]+)";
        Pattern pattern = Pattern.compile(patternStr);
        Matcher matcher;
        matcher = pattern.matcher(mSubscribeURL);
        if (matcher.matches()){
            return matcher.group(1);
        }
        return "";
    }
    class CheckCanSubscribeMsgHandler extends Handler {
        public CheckCanSubscribeMsgHandler(Looper looper) {
            super(looper);
        }
        @Override
        public void handleMessage(Message msg) {
            String canSubscribeRet = (String) msg.obj;
            //Log.d("msg", canSubscribeRet);
            //find keyword
            String anchor = getAnchorByKeywordFromHTML(canSubscribeRet);
            if (!anchor.isEmpty())
            {
                mSubscribeURL = anchor.substring(2);//remove './'
                mDatid = GetDatidFromSubscribeURL();
                mEditTextDebugMsg.setText("");
                mEditTextDebugMsgName.setText("GetSubscribePage");
                //ScheduleTask(new GetSubscribePageTimerTask(), 10000);//TODO time
                ScheduleTask(null, 0);
                GetSubscribePage();
                return;
            }
            mEditTextDebugMsg.setText("count " + ++mCheckCanSubscribeTimes);
        }
        private String getAnchorByKeywordFromHTML(String html)
        {
            /*
            <td>下午</td>
            <td>骨科门诊</td> <td>骨科</td>
            <td>主治医师</td>
            <td>主治医师</td>
            <td>5.00</td>
            <td></td>
            <td>7</td><td>1</td><td><a href='./xiehe/guahao.php?hpid=1&ksid=1220010&datid=381627' onclick='return tgai()' target='_balnk' >预约挂号</a></td>
            </tr><tr>
             */
            String patternStr = String.format("<td>.*%s.*</td>[\\s\\S]*?<td><a .*href\\s*=\\s*'(.+?)'.*>.*</a>", mKeyword);
            //String mPatternStr = String.format("<a .*href\\s*=\\s*'(.+?)'.*>.*</a>", mKeyword);
            Pattern p = Pattern.compile(patternStr);
            Matcher m = p.matcher(html);

            Log.d("mPattern", p.pattern());
            if (m.find())
            {
                Log.d("anchor",m.group(1));
                return m.group(1);
                //use the temp string for display
            }
            return "";
        }
    }

    class GetSubscribePageMsgHandler extends Handler {
        public GetSubscribePageMsgHandler(Looper looper) {
            super(looper);
        }
        @Override
        public void handleMessage(Message msg) {
            String canSubscribeRet = (String) msg.obj;
            if (canSubscribeRet.contains("<script>alert('请先登录 未注册用户请先注册 点击确定进入登陆界面');"))
            {
                mEditTextDebugMsg.setText("请先登录");
                ScheduleTask(null, 0);
                return;
            }
            String dxCodeGetLink = getdxCodeLinkFromHTML(canSubscribeRet);
            if (!dxCodeGetLink.isEmpty())
            {
                mEditTextDebugMsg.setText("Success");
                mdxCodeGetParam = dxCodeGetLink;
                //ScheduleTask(new GetdxCodeTimerTask(), 30000);
                GetdxCode();
                //loop to get sms for dx code
                //set dx code textview and submit
            }
        }
        private String getdxCodeLinkFromHTML(String orghtml)
        {
            Log.d("getdxCodeLinkFromHTML",orghtml);
            String ret="";
            String html = orghtml.replaceAll("\t","").replaceAll("\r","").replaceAll(" ","");
            String[] tmps = html.split("\n");
            String patternStr = "^(\\$\\.get\\(\"\\.\\./shortmsg/dx_code\\.php\\?hpid=\"\\+hpid\\+\")(\\&.*)(\"\\+\"\\&ksid=\"\\+ksid\\+\"\\&datid=\"\\+datid\\+\"\\&jiuz=\"\\+jiuz\\+\"\\&ybkh=\"\\+ybkh\\+\"\\&baoxiao=\"\\+baoxiao,null,callback\\);)$";
            Pattern pattern = Pattern.compile(patternStr);
            Matcher matcher;
            for (String tmp : tmps) {
                matcher = pattern.matcher(tmp);
                if (matcher.matches()){
                    ret = matcher.group(2);
                    Log.d("param", ret);
                    break;
                }
            }
            mSubscribeInputKV.clear();
            patternStr = "<input .*?name=['\"]{0,1}(.*?)['\"]{0,1} .*?value=['\"]{0,1}(.*?)['\"]{0,1}[ >]";
            pattern = Pattern.compile(patternStr);
            matcher = pattern.matcher(orghtml);
            while (matcher.find())
            {
                /*
                String name = matcher.group(1);
                if (name.contains("\""))
                {
                    name = name.substring(1, name.length()-2);
                }
                String value = matcher.group(2);
                if (value.contains("\""))
                {
                    value = name.substring(1, value.length()-2);
                }
                */
                mSubscribeInputKV.put(matcher.group(1),matcher.group(2));
            }
            /*
            mPatternStr = "<input .*?value=['\"]{0,1}(.*?)['\"]{0,1} [^<>]*?name=['\"]{0,1}(.*?)['\"]{0,1}[ >]";
            mPattern = Pattern.compile(mPatternStr);
            matcher = mPattern.matcher(orghtml);
            while (matcher.find())
            {
                mSubscribeInputKV.put(matcher.group(2),matcher.group(1));
            }
            */
            //mPatternStr = "<form name=\"ti\" method=\"post\" action=\"ghdown.php\">";
            return ret;
        }
    }

    class GetdxCodeMsgHandler extends Handler {
        public GetdxCodeMsgHandler(Looper looper) {
            super(looper);
        }
        @Override
        public void handleMessage(Message msg) {
            String httpGetRet = (String) msg.obj;
            String dxCodeGetRet= getdxCodeRet(httpGetRet);
            if (dxCodeGetRet.contains("已发送短信验证码"))
            {
                mEditTextDebugMsgName.setText("GetdxCode");
                mEditTextDebugMsg.setText("Success");
                ScheduleTask(null, 0);
                //loop to get sms for dx code
                //set dx code textview and submit
                //onSubscribeClick();
            }
        }
        private String getdxCodeRet(String html)
        {
            Log.d("getdxCodeRet",html);
            return html;
        }
    }
}
