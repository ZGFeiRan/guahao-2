package com.tianxing.userapps.guahao5.dummy;

import android.content.res.Resources;

import com.tianxing.userapps.guahao5.MyApp;
import com.tianxing.userapps.guahao5.R;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by litao on 2017/2/4.
 */

public class DateItem implements Comparator<DateItem> {
    public String date;
    public String dutyCode;
    public String weekDay;

    public static Map<String, String> dutyCode2Name = new HashMap<String, String>() {
        {
            Resources res = MyApp.getInstance().getResources();
            put("-1", res.getString(R.string.whole_day));
            put("1", res.getString(R.string.morning));
            put("2", res.getString(R.string.afternoon));
            put("4", res.getString(R.string.night));
        }
    };

    public DateItem(String date, String dutyCode, String weekDay) {
        this.date = date;
        this.dutyCode = dutyCode;
        this.weekDay = weekDay;
    }
    public int compare(DateItem p1, DateItem p2) {
        return p1.date.compareTo(p2.date);
    }
    @Override
    public String toString() {
        return date + " " + dutyCode2Name.get(dutyCode) + " " + weekDay;
    }
}

