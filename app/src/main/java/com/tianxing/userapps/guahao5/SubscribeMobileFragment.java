package com.tianxing.userapps.guahao5;

import android.app.Activity;
import android.app.Fragment;
import android.content.ContentResolver;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
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
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Method;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Calendar;
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
 * Use the {@link SubscribeMobileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SubscribeMobileFragment extends Fragment {
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
    String mDoctorId;
    String mPatientId;
    String mKeyword;
    String mSubscribeURL;
    String mDutySourceId;
    String mDutyCode;
    private Map<String, String> mSubscribeInputKV = new HashMap<String, String>();
    WebView mWebView = null;

    private Handler mCheckCanSubscribeMsgHandler = null;
    private Handler mGetSubscribePageMsgHandler = null;
    private Handler mGetdxCodeMsgHandler = null;
    private Handler mGetSMS4dxCodeMsgHandler = null;
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
    public static SubscribeMobileFragment newInstance(String param1, String param2) {
        SubscribeMobileFragment fragment = new SubscribeMobileFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public SubscribeMobileFragment() {
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
        v.setFocusable(true);//这个和下面的这个命令必须要设置了，才能监听back事件
        v.setFocusableInTouchMode(true);
        v.setOnKeyListener(backListener);

        CookieSyncManager.createInstance(getActivity().getApplicationContext());
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        //cookieManager.setAcceptThirdPartyCookies(mWebView, true);
        CookieSyncManager.getInstance().sync();
        String cookie = HTTPClientWrapper.getInstance().getCookie();
        cookieManager.removeAllCookie();
        cookieManager.setCookie(HTTPSessionStatus.URL_BASE, cookie);//cookies是在HttpClient中获得的cookie
        CookieSyncManager.getInstance().sync();

        MainActivity activity = (MainActivity) getActivity();
        mHpid = activity.mCurHospitalID;
        mKeid = activity.mCurDepID;
        mDate1 = activity.mCurDate;

        mDutySourceId = activity.mDoctorItem.dutySourceId;
        mDoctorId = activity.mDoctorItem.doctorId;
        mKeyword = activity.mDoctorKeyword;
        mDutyCode = activity.mDateItem.dutyCode;
        mTimer = new Timer(true);

        Looper looper = Looper.myLooper();
        mCheckCanSubscribeMsgHandler = new CheckCanSubscribeMsgHandler(looper);
        mGetSubscribePageMsgHandler = new GetSubscribePageMsgHandler(looper);
        mGetdxCodeMsgHandler = new GetdxCodeMsgHandler(looper);
        mGetSMS4dxCodeMsgHandler = new GetSMS4dxCodeMsgHandler(looper);
        mSubscribeMsgHandler = new SubscribeMsgHandler(looper);

        final Button mSubscribeButton = (Button) v.findViewById(R.id.buttonSubscribe);
        mSubscribeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onSubscribeClick();
            }
        });
        mEditTextDebugMsgName = (EditText) v.findViewById(R.id.editTextDebugMsgName);
        mEditTextDebugMsg = (EditText) v.findViewById(R.id.editTextDebugMsg);
        mEditTextdxCode = (EditText) v.findViewById(R.id.editTextdxCode);

        if (mDutySourceId.isEmpty()) {
            mEditTextDebugMsgName.setText("CheckCanSubscribe");
            mEditTextDebugMsg.setText("");
            ScheduleTask(new CheckCanSubscribeTimerTask(), 2000);
        } else {
            mEditTextDebugMsgName.setText("GetSubscribePage");
            mEditTextDebugMsg.setText("");
            ScheduleTask(new GetSubscribePageTimerTask(), 2000);
        }
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

    @Override
    public void onHiddenChanged(boolean hidden) {
        if (hidden) {
            ScheduleTask(null, 0);
        }
    }

    private View.OnKeyListener backListener = new View.OnKeyListener() {
        @Override
        public boolean onKey(View view, int i, KeyEvent keyEvent) {
            if (keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
                if (i == KeyEvent.KEYCODE_BACK) {
                    onHiddenChanged(true);
                    return false;
                }
            }
            return false;
        }
    };
    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnSubscribeFragmentListener {
        public void onSubscribeSuccess();
    }

    class HTMLCallback {
        @JavascriptInterface
        public void getHTML(String html, String url) {
            if (url.equals("GetSubscribePage")) {
                Message message = Message.obtain();
                message.obj = html;
                mGetSubscribePageMsgHandler.sendMessage(message);
            } else if (url.equals("GetdxCode")) {
                Message message = Message.obtain();
                message.obj = html;
                mGetdxCodeMsgHandler.sendMessage(message);
            } else if (url.equals("Subscribe") && html.length() > 0) {
                Message message = Message.obtain();
                message.obj = html;
                mSubscribeMsgHandler.sendMessage(message);
            }
        }
    }

    private void ScheduleTask(TimerTask task, int ms) {
        if (mTimer != null) {
            if (mCurTimerTask != null) {
                mCurTimerTask.cancel();
            }
            //mTimer.cancel();
            mTimer.purge();
        }
        mCurTimerTask = task;
        if (mCurTimerTask != null) {
            mTimer.schedule(mCurTimerTask, 0, ms);
        }
    }

    protected String GetSubscribePage() {
        Map headers = new HashMap();
        // /order/confirm/"+b[a].hospitalId+"-"+b[a].departmentId+"-"+b[a].doctorId+"-"+b[a].dutySourceId+".htm
        String referer = String.format(
                HTTPSessionStatus.URL_DATELIST_MOBILE,
                mHpid, mKeid);
        headers.put("Referer", referer);
        headers.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");

        mSubscribeURL = String.format(HTTPSessionStatus.URL_ORDER_CONFIRM,
                mHpid,mKeid,mDoctorId,mDutySourceId);
        //return GetSubscribePageByWebview(mSubscribeURL, headers);
        return GetSubscribePageByHTTPClientWrapper(mSubscribeURL, headers);
    }

    protected String GetSubscribePageByWebview(String url, Map headers) {
        mWebView.setVisibility(View.VISIBLE);
        mWebView.loadUrl(url, headers);
        //TODO get and pass HTML page to check if login
        return "success";
    }

    protected String GetSubscribePageByHTTPClientWrapper(String url, Map headers) {
        HttpEntity entity = HTTPClientWrapper.getInstance().doGet(url, new HashMap(), headers);
        try {
            return EntityUtils.toString(entity);
        } catch (Exception e) {
            //strResult = e.getMessage().toString();
            e.printStackTrace();
        }
        return "";
    }

    public class GetSubscribePageTimerTask extends TimerTask {
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

    class GetSubscribePageMsgHandler extends Handler {
        public GetSubscribePageMsgHandler(Looper looper) {
            super(looper);
        }
        private String patternPatientIdStr =
                "class=\"Rese_db_dl\"[\\s\\S]*?<input .*?name=\"hzr\" value=\"([0-9]+)\" checked=\"checked\"";
        private Pattern patternPatientId = Pattern.compile(patternPatientIdStr);
        @Override
        public void handleMessage(Message msg) {
            String canSubscribeRet = (String) msg.obj;
            if (canSubscribeRet.contains("<script>alert('请先登录 未注册用户请先注册 点击确定进入登陆界面');")) {
                mEditTextDebugMsg.setText("请先登录");
                ScheduleTask(null, 0);
                return;
            }
            mPatientId = GetPatientId(canSubscribeRet);
            if (mPatientId.isEmpty()) {
                mEditTextDebugMsg.setText("PatientId empty");
                ScheduleTask(null, 0);
                return;
            }
            mEditTextDebugMsg.setText("Success");
            ScheduleTask(new GetdxCodeTimerTask(), 65000);
            //GetdxCode();
            //loop to get sms for dx code
        }
        private String GetPatientId(String msg) {
            Matcher matcher = patternPatientId.matcher(msg);
            if (matcher.find()) {
                String patientId = matcher.group(1);
                return patientId;
            }
            return "";
        }
    }


    public class SubscribeTimerTask extends TimerTask {
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

    public void onSubscribeClick() {
        String dxCode = mEditTextdxCode.getText().toString();
        if (dxCode.isEmpty()) {
            mEditTextdxCode.setError(getString(R.string.require_prompt));
            mEditTextdxCode.requestFocus();
            return;
        }
        mSubscribeInputKV.put("dutySourceId", mDutySourceId);
        mSubscribeInputKV.put("hospitalId", mHpid);
        mSubscribeInputKV.put("departmentId", mKeid);
        mSubscribeInputKV.put("doctorId", mDoctorId);
        mSubscribeInputKV.put("patientId", mPatientId);
        mSubscribeInputKV.put("smsVerifyCode", dxCode);
        mSubscribeInputKV.put("isAjax","true");
        mEditTextDebugMsgName.setText("Subscribe");
        ScheduleTask(new SubscribeTimerTask(), 2000);
        //Subscribe();
    }

    protected String SubscribeByWebview(String orderURL, Map headers) {
        //document.body.innerHTML
        //String js = "testt();var int=window.setInterval(function(){clearInterval(int);window.htmlCallback.getHTML(document, 'Subscribe');},500);";
        String dxCode = mEditTextdxCode.getText().toString();
        //String url = String.format("javascript:$(\"#dxcode1\").val(\"%s\");testt();var int=window.setInterval(function(){clearInterval(int);window.htmlCallback.getHTML(document.body.innerHTML, 'Subscribe');},1000);", dxCode);
        String url = String.format("javascript:$(\"#dxcode1\").val(\"%s\");testt();", dxCode);
        //refer http://www.bjguahao.gov.cn/comm/xiehe/guahao.php?hpid=1&ksid=1220010&datid=385718&jiuz=&ybkh=&hzname=&hzsfz=
        mWebView.loadUrl(url, headers);//TODO
        return "";
    }

    protected String SubscribeByHTTPClientWrapper(String orderURL, Map headers) {
        HttpEntity entity = HTTPClientWrapper.getInstance().doPost(orderURL, mSubscribeInputKV, headers, "UTF-8");
        try {
            return EntityUtils.toString(entity);
        } catch (Exception e) {
            //strResult = e.getMessage().toString();
            e.printStackTrace();
        }
        return "";
    }

    protected String Subscribe() {
        Map headers = new HashMap();
        headers.put("Referer", mSubscribeURL);
        headers.put("Accept", "application/json, text/javascript, */*; q=0.01");
        headers.put("Accept-Language", "zh-CN,zh;q=0.8,en-US;q=0.5,en;q=0.3");
        headers.put("X-Requested-With", "XMLHttpRequest");
        //headers.put("Accept-Encoding", "gzip, deflate");
        String orderURL = HTTPSessionStatus.URL_ORDER;
        //return SubscribeByWebview(orderURL, headers);
        return SubscribeByHTTPClientWrapper(orderURL, headers);
    }

    class SubscribeMsgHandler extends Handler {
        int mCheckSubscribeTimes = 0;

        public SubscribeMsgHandler(Looper looper) {
            super(looper);
        }

        public String ascii2native(String ascii) {
            int n = ascii.length() / 6;
            StringBuilder sb = new StringBuilder(n);
            for (int i = 0, j = 2; i < n; i++, j += 6) {
                String code = ascii.substring(j, j + 4);
                char ch = (char) Integer.parseInt(code, 16);
                sb.append(ch);
            }
            return sb.toString();
        }
        private String getAscii(String str)
        {
            final String patternStr = "(\\\\u[0-9a-z]{4})+";
            final Pattern p = Pattern.compile(patternStr);
            Matcher m = p.matcher(str);
            if (m.find())
            {
                return m.group(0);
            }
            return "";
        }
        @Override
        public void handleMessage(Message msg) {
            String httpGetRet = (String) msg.obj;
            Log.d("SubscribeMsg", httpGetRet);
            Integer codeStatus = -1;
            boolean hasError = false;
            String offerTime = "";
            String numericalSequence = "";
            try {
                JSONObject jsonObject = new JSONObject(httpGetRet);
                codeStatus = jsonObject.getInt("code");
                hasError = jsonObject.getBoolean("hasError");
                JSONArray data = jsonObject.getJSONArray("data");
                if (data.length() > 0) {
                    JSONObject data0 = data.getJSONObject(0);
                    offerTime = data0.getString("offerTime");
                    numericalSequence = data0.getString("numericalSequence");
                }
            } catch (JSONException e) {
                Log.e("JSONException", e.toString());
            }
            if (codeStatus == 200 && !hasError)
            {
                ScheduleTask(null, 0);
                mEditTextDebugMsg.setText("No." + numericalSequence + "\n" + offerTime);
                mCheckSubscribeTimes = 0;
                Log.i("Subscribe CodeStatus", "200");
                return;
            }
            String realInfo = getAscii(httpGetRet);
            if (!realInfo.isEmpty())
            {
                realInfo = ascii2native(realInfo);
            }
            ++mCheckSubscribeTimes;
            //TODO if yuyue full,schedule null
            if (mCheckSubscribeTimes > 5)
            {
                ScheduleTask(null, 0);
                mEditTextDebugMsg.setText("stop count=" + mCheckSubscribeTimes);
                mCheckSubscribeTimes = 0;
                return;
            }
            mEditTextDebugMsg.setText(realInfo + " count " +mCheckSubscribeTimes);
            //\u7cfb\u7edf\u7e41\u5fd9\uff0c\u8bf7\u7a0d\u540e\u518d\u8bd5\uff01//系统繁忙，请稍后再试！
            //\\u9a8c\\u8bc1\\u7801\\u4e0d\\u6b63\\u786e"//验证码不正确！
        }
    }

    public class CheckCanSubscribeTimerTask extends TimerTask {
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

    protected String checkCanSubscribe() {
        Map headers = new HashMap();
        String referer = String.format(
                "http://m.bjguahao.gov.cn/dpt/appoint/%s-%s.htm",
                mHpid, mKeid);
        headers.put("Referer", referer);
        headers.put("X-Requested-With", "XMLHttpRequest");
        headers.put("Accept", "application/json, text/javascript, */*; q=0.01");
        headers.put("Accept-Language", "zh-CN,zh;q=0.8,en-US;q=0.5,en;q=0.3");
        Map mPostKVs = new HashMap();
        mPostKVs.put("dutyDate", mDate1);
        mPostKVs.put("hospitalId", mHpid);
        mPostKVs.put("dutyCode", mDutyCode);
        mPostKVs.put("departmentId", mKeid);
        mPostKVs.put("isAjax", "true");
        String url = HTTPSessionStatus.URL_DOCTOR_DUTY;
        HttpEntity entity = HTTPClientWrapper.getInstance().doPost(url, mPostKVs, headers, "UTF-8");
        if (entity == null) {
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

    class CheckCanSubscribeMsgHandler extends Handler {
        private Integer mCheckCanSubscribeTimes = 0;
        public CheckCanSubscribeMsgHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            String canSubscribeRet = (String) msg.obj;
            Log.d("msg", canSubscribeRet);
            mEditTextDebugMsgName.setText("CheckCanSubscribe");
            //find keyword
            boolean ret = getDoctorAttrByKeywordFromJSON(canSubscribeRet);
            if (ret) {
                mSubscribeURL = String.format(HTTPSessionStatus.URL_ORDER_CONFIRM,
                        mHpid,mKeid,mDoctorId,mDutySourceId);
                mEditTextDebugMsg.setText("");
                mEditTextDebugMsgName.setText("GetSubscribePage");
                ScheduleTask(new GetSubscribePageTimerTask(), 2000);//TODO time
                //ScheduleTask(null, 0);
                //GetSubscribePage();
                mCheckCanSubscribeTimes = 0;
                return;
            }
            mEditTextDebugMsg.setText("count " + ++mCheckCanSubscribeTimes);
        }

        private boolean getDoctorAttrByKeywordFromJSON(String jsonStr) {
            try {
                JSONObject jsonObj = new JSONObject(jsonStr);
                JSONArray datids = jsonObj.getJSONArray("data");
                Pattern p = Pattern.compile(mKeyword);
                for (int i = 0; i < datids.length(); i++) {
                    JSONObject datid = datids.getJSONObject(i);
                    String doctorName = datid.getString("doctorName");
                    String skill = datid.getString("skill");
                    String remainAvailableNumber = datid.getString("remainAvailableNumber");

                    if ((p.matcher(doctorName).find()|| p.matcher(skill).find())
                            && !remainAvailableNumber.equals("0")) {
                        Log.d("getDoctorAttrFromJSON",
                                "doctorName:" + doctorName +
                                        ",skill:" + skill +
                                        ",remainNum:" + remainAvailableNumber +
                                        ",keyword:" + mKeyword);
                        mDutySourceId = datid.getString("dutySourceId");
                        mDoctorId = datid.getString("doctorId");
                        return true;
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return false;
        }
    }

    protected String GetdxCode() {
        return GetdxCodeByHTTPClientWrapper();
        //return GetdxCodeByWebview();
    }

    protected String GetdxCodeByWebview() {
        String js = "$(\".guahao input:button\").click();var int=window.setInterval(function(){if($(\"#tian\").html().trim().length>0){clearInterval(int);window.htmlCallback.getHTML(document.getElementById('tian').innerHTML, 'GetdxCode');}},500);";
        //mWebView.loadUrl("javascript:"+js);//TODO
        //mWebView.loadUrl("javascript:window.htmlCallback.getHTML(document.getElementById('tian').innerHTML, 'GetdxCode');");
        return "";
    }

    protected String GetdxCodeByHTTPClientWrapper() {
        Map headers = new HashMap();
        headers.put("Referer", mSubscribeURL);
        headers.put("Accept", "application/json, text/javascript, */*; q=0.01");
        headers.put("Accept-Encoding", "gzip, deflate");
        headers.put("Accept-Language", "zh-CN,zh;q=0.8,en;q=0.6");
        headers.put("Connection", "keep-alive");
        headers.put("X-Requested-With", "XMLHttpRequest");
        //HTTPClientWrapper.getInstance().setCookie("Hm_lpvt_65f844e6a6e140ab52d02690ed38a38b", "1437122394");
        String url = HTTPSessionStatus.URL_MOBILE_BASE + "v/sendorder.htm";
        Log.d("GetdxCode url", url);
        Map kvs = new HashMap();
        HttpEntity entity = HTTPClientWrapper.getInstance().doPost(url, kvs, headers, "UTF-8");

        String ret = "";
        try {
            ret = EntityUtils.toString(entity);
        } catch (Exception e) {
            //strResult = e.getMessage().toString();
            e.printStackTrace();
        }
        return ret;
    }

    public class GetdxCodeTimerTask extends TimerTask {
        @Override
        public void run() {
            String canSubscribeRet = GetdxCode();
            Message message = Message.obtain();
            message.obj = canSubscribeRet;
            mGetdxCodeMsgHandler.sendMessage(message);
        }
    }

    class GetdxCodeMsgHandler extends Handler {
        int cnt = 0;
        public GetdxCodeMsgHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            String httpGetRet = (String) msg.obj;
            String dxCodeGetRet = getdxCodeRet(httpGetRet);
            mEditTextDebugMsgName.setText("GetdxCode");
            mEditTextDebugMsg.setText("count " + ++cnt);
            Integer codeStatus = -1;
            try {
                JSONObject jsonObj = new JSONObject(dxCodeGetRet);
                codeStatus = jsonObj.getInt("code");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if (codeStatus == 200)
            {
                ScheduleTask(new GetSMS4dxCodeTimerTask(), 2000);
                mEditTextDebugMsg.setText("Success");
                cnt = 0;
                //loop to get sms for dx code
                //set dx code textview and submit
                //onSubscribeClick();
            }
        }

        private String getdxCodeRet(String html) {
            Log.d("getdxCodeRet", html);
            return html;
        }
    }

    public class GetSMS4dxCodeTimerTask extends TimerTask {
        private Date startTime;
        private ContentResolver cr;
        GetSMS4dxCodeTimerTask()
        {
            startTime = new Date(System.currentTimeMillis());
            this.cr = SubscribeMobileFragment.this.getActivity().getContentResolver();
        }
        @Override
        public void run() {
            String sms4dxCode = GetSMS4dxCode();
            Message message = Message.obtain();
            message.obj = sms4dxCode;
            mGetSMS4dxCodeMsgHandler.sendMessage(message);
        }

        protected String GetSMS4dxCode() {
            final String SMS_URI_INBOX = "content://sms/inbox";
            final long DIFF_BIG = 2*60*1000;
            final long DIFF_SMALL = -5*60*1000;
            try {
                String[] projection = new String[]{"_id", "address", "person",
                        "body", "date", "type"};
                Uri uri = Uri.parse(SMS_URI_INBOX);
                Cursor cur = cr.query(uri, projection, null, null, "date desc");
                Pattern p = Pattern.compile("([0-9]{6})");
                if (cur.moveToFirst()) {//start loop with the newest sms
                    int phoneNumberColumn = cur.getColumnIndex("address");
                    int smsbodyColumn = cur.getColumnIndex("body");
                    int dateColumn = cur.getColumnIndex("date");
                    do {
                        String phoneNumber = cur.getString(phoneNumberColumn);
                        Date smsDate = new Date(Long.parseLong(cur.getString(dateColumn)));
                        long dateDiff = startTime.getTime() - smsDate.getTime();//minutes
                        if (dateDiff > DIFF_BIG || dateDiff < DIFF_SMALL) {
                            cur.close();
                            return "";
                        }
                        if (!phoneNumber.startsWith("010")) {
                            continue;
                        }
                        String smsbody = cur.getString(smsbodyColumn);
                        //parse dxcode

                        Matcher m = p.matcher(smsbody);
                        if (m.find()) {
                            cur.close();
                            return m.group(1);
                        }
                    } while (cur.moveToNext());
                }
                cur.close();
            }
            catch(SQLiteException ex){
                Log.d("in getSmsInPhone", ex.getMessage());
            }
            return "";
        }
    }
    class GetSMS4dxCodeMsgHandler extends Handler {
        public GetSMS4dxCodeMsgHandler(Looper looper) {
            super(looper);
        }
        private int cnt = 0;
        @Override
        public void handleMessage(Message msg) {
            String smsContent = (String) msg.obj;
            cnt++;
            mEditTextDebugMsgName.setText("GetSMS4dxCode");
            mEditTextDebugMsg.setText("count " + cnt);
            if (!smsContent.isEmpty()) {
                cnt = 0;
                mEditTextdxCode.setText(smsContent);
                ScheduleTask(null, 0);
                onSubscribeClick();
            }
            if (cnt > 15)
            {
                mEditTextDebugMsg.setText("stop");
                cnt = 0;
                ScheduleTask(null, 0);
            }
        }
    }

}