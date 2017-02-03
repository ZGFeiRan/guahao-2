package com.tianxing.userapps.guahao5;

import android.os.Handler;
import android.os.Message;

import org.apache.http.HttpEntity;
import org.apache.http.util.EntityUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by tao.li on 2015/8/18.
 */
public class ListGetter {
    private Handler mMessageHandler = null;
    private Timer mTimer = null;
    private TimerTask mCurTimerTask = null;
    protected Map mHTTPHeaders = new HashMap();
    protected Map mPostKVs = new HashMap();
    protected String mReferer;
    protected String mUrl;
    ListGetter(Timer timer, Handler messageHandler)
    {
        mTimer = timer;
        if (timer == null)
        {
            mTimer = new Timer(true);
        }
        mMessageHandler = messageHandler;
        mHTTPHeaders.put("Accept", "application/json, text/javascript, */*; q=0.01");
        mHTTPHeaders.put("Accept-Language", "zh-CN,zh;q=0.8,en;q=0.6");
    }

    public void start()
    {
        stop();
        mCurTimerTask = new GetTimerTask();
        mTimer.schedule(mCurTimerTask, 0);
    }
    public void stop()
    {
        if (mTimer != null) {
            if (mCurTimerTask != null) {
                mCurTimerTask.cancel();
            }
            //mTimer.cancel();
            mTimer.purge();
        }
    }
    public class GetTimerTask extends TimerTask {
        @Override
        public void run() {
            String canSubscribeRet = get();
            Message message = Message.obtain();
            message.obj = canSubscribeRet;
            mMessageHandler.sendMessage(message);
        }
    }
    protected HttpEntity httpVisit()
    {
        mHTTPHeaders.put("X-Requested-With", "XMLHttpRequest");
        return HTTPClientWrapper.getInstance().doPost(mUrl, mPostKVs, mHTTPHeaders, "UTF-8");
    }

    protected String get() {
        mHTTPHeaders.put("Referer", mReferer);
        HttpEntity entity = httpVisit();
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
}
