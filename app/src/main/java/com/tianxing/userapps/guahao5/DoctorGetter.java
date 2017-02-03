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
 * Created by tao.li on 2015/8/15.
 */
public class DoctorGetter extends ListGetter{
    DoctorGetter(String date, String dutyCode, String hpid, String keid, Handler messageHandler)
    {
        super(null, messageHandler);
        mReferer = String.format(HTTPSessionStatus.URL_DATELIST_MOBILE, hpid, keid);
        // $.ajax({type:"POST",
        // dataType:"json",
        // url:basePath+"dpt/partduty.htm",
        // data:{hospitalId:"12",departmentId:a,dutyCode:b,dutyDate:c,isAjax:!0},
        mUrl = HTTPSessionStatus.URL_DOCTOR_DUTY;
        mPostKVs.put("dutyDate", date);
        mPostKVs.put("hospitalId", hpid);
        mPostKVs.put("dutyCode", dutyCode);
        mPostKVs.put("departmentId", keid);
        mPostKVs.put("isAjax", "true");
    }
}
