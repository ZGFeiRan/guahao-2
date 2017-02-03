package com.tianxing.userapps.guahao5;

import android.os.Handler;

import org.apache.http.HttpEntity;

/**
 * Created by tao.li on 2015/8/15.
 */
public class DepGetter extends ListGetter{
    DepGetter(String hpid, Handler messageHandler)
    {
        super(null, messageHandler);
        //mUrl = String.format("http://m.bjguahao.gov.cn/Home/Hospital/index/hpid/%s", hpid);
        //mReferer = HTTPSessionStatus.URL_INDEX_MOBILE;
        mUrl = String.format(HTTPSessionStatus.URL_MOBILE_BASE + "hp/appoint/%s.htm", hpid);
        mReferer = HTTPSessionStatus.URL_INDEX_MOBILE;
        mHTTPHeaders.put("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:39.0) Gecko/20100101 Firefox/39.0");
    }
    @Override
    protected HttpEntity httpVisit()
    {
        return HTTPClientWrapper.getInstance().doGet(mUrl, null, mHTTPHeaders);
    }
}
