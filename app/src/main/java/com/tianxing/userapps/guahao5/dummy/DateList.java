package com.tianxing.userapps.guahao5.dummy;

import android.app.Fragment;
import android.content.Context;
import android.content.res.Resources;
import android.text.style.TtsSpan;
import android.util.Log;

import com.tianxing.userapps.guahao5.MyApp;
import com.tianxing.userapps.guahao5.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 * <p/>
 * TODO: Replace all uses of this class before publishing your app.
 */
public class DateList {

    /**
     * An array of sample (dummy) items.
     */
    public List<DateItem> ITEMS = new ArrayList<>();

    /**
     * A map of sample (dummy) items, by ID.
     */

    private Calendar c = Calendar.getInstance();
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    /*
    static {
        // Add sample items.
        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        for (Integer i=0;i<7;i++)
        {
            c.add(Calendar.DAY_OF_MONTH, 1);
            Integer weekDay = c.get(Calendar.DAY_OF_WEEK)-1;
            if (weekDay == 6 || weekDay == 0)
            {
                continue;
            }
            String cStr = sdf.format(c.getTime());
            addItem(new DateItem(i, cStr, "星期"+weekDay));
        }
    }
    */

    private void addItem(DateItem item) {
        ITEMS.add(item);
        Collections.sort(ITEMS, item);
    }
    public void clear()
    {
        ITEMS.clear();
    }
    public void addItem(Map<String, Integer> date2cnt, String isAvailable)
    {
        Iterator iter = date2cnt.keySet().iterator();
        while(iter.hasNext())
        {
            String dateStr = iter.next().toString();
            String formatDate = dateStr.split("_")[1];
            String dutyCode = dateStr.split("_")[0];
            Integer cnt = date2cnt.get(dateStr);
            try
            {
                Date date = sdf.parse(formatDate);
                c.setTime(date);
            }
            catch (ParseException e)
            {
                Log.d("addItem", e.getMessage());
                continue;
            }

            Integer weekDay = c.get(Calendar.DAY_OF_WEEK)-1;
            addItem(new DateItem(formatDate, dutyCode, "星期" + weekDay + " " +cnt+" "+isAvailable));
        }
    }
    public void addGrabItem()
    {
        String recentDate = "";
        for(DateItem item : ITEMS)
        {

            if (item.date.compareTo(recentDate) > 0)
            {
                recentDate = item.date;
            }
        }
        if (recentDate.isEmpty())
        {
            c.setTime(new Date());
            c.add(Calendar.DAY_OF_MONTH, 7);
        }
        else {
            try {
                Date date = sdf.parse(recentDate);
                c.setTime(date);
                c.add(Calendar.DAY_OF_MONTH, 1);
            } catch (ParseException e) {
                Log.d("addGrabItem", e.getMessage());
                return;
            }
        }
        Integer weekDay = c.get(Calendar.DAY_OF_WEEK)-1;
        String date4Grab = sdf.format(c.getTime());
        addItem(new DateItem(date4Grab, "-1", "星期" + weekDay + " 抢号"));
    }
    /**
     * A dummy item representing a piece of content.
     */
}
