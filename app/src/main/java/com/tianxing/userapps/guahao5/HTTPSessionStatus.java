package com.tianxing.userapps.guahao5;

/**
 * Created by tao.li on 2015/6/28.
 */
public class HTTPSessionStatus {
    //index
    public static final String URL_INDEX = "http://www.bjguahao.gov.cn/comm/index.html";
    public static final String URL_MOBILE_BASE = "http://m.bjguahao.gov.cn/";
    public static final String URL_INDEX_MOBILE = "http://m.bjguahao.gov.cn/index.htm";

    //base url
    public static final String URL_BASE = URL_MOBILE_BASE;
    //login
    public static final String URL_LOGIN="http://www.bjguahao.gov.cn/comm/logon.php";
    public static final String URL_LOGIN_MOBILE=URL_MOBILE_BASE + "quicklogin.htm";
    //public static final String URL_DATELIST_MOBILE=URL_INDEX_MOBILE +"/Home/guahao/index/hpid/%s/ksid/%s";
    public static final String URL_DATELIST_MOBILE=URL_MOBILE_BASE +"dpt/appoint/%s-%s.htm";
    public static final String URL_SEARCH="http://www.bjguahao.gov.cn/comm/ghao.php";
    public static final String URL_GETLOGINCODE="http://www.bjguahao.gov.cn/comm/code.php";
    public static final String URL_GETLOGINCODE_MOBILE="http://m.bjguahao.gov.cn/quicklogin.htm";

    // doctor DUTY URL
    public static final String URL_DOCTOR_DUTY = URL_BASE + "dpt/partduty.htm";

    // /order/confirm/" + b[a].hospitalId + "-" + b[a].departmentId + "-" + b[a].doctorId + "-" + b[a].dutySourceId + ".htm'
    public static final String  URL_ORDER_CONFIRM = URL_BASE + "order/confirm/%s-%s-%s-%s.htm";

    public static final String URL_ORDER = URL_BASE + "order/confirm.htm";

    ///shortmsg/dx_code.php?hpid="+hpid+"&hpid390628=390155&ksid390628=390155&n390628=390155&uOn390628=390155&crodlszma390628=390155&ascrodlszma=390155"+"&ksid="+ksid+"&datid="+datid+"&jiuz="+jiuz+"&ybkh="+ybkh+"&baoxiao="+baoxiao
    public static final String URL_GETCODEOFPHONE="http://www.bjguahao.gov.cn/comm/shortmsg/dx_code.php";

    //public static final String URL_ORDER = "http://www.bjguahao.gov.cn/comm/TG/ghdown.php";

    //private static final String PATH_SAVE_IMG = ConfigureUtil.login_code_img_save_path;
}
