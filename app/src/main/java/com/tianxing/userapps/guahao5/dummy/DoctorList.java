package com.tianxing.userapps.guahao5.dummy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 * <p/>
 * TODO: Replace all uses of this class before publishing your app.
 */
public class DoctorList {

    /**
     * An array of sample (dummy) items.
     */
    public List<DoctorItem> ITEMS = new ArrayList<DoctorItem>();

    /**
     * A map of sample (dummy) items, by ID.
     */
    private Map<String, DoctorItem> ITEM_MAP = new HashMap<String, DoctorItem>();

    public void addItem(DoctorItem item) {
        ITEMS.add(item);
        Collections.sort(ITEMS, item);
        ITEM_MAP.put(item.dutySourceId, item);
    }
    public void clear()
    {
        ITEM_MAP.clear();
        ITEMS.clear();
    }

    /**
     * A dummy item representing a piece of content.
     */
    public static class DoctorItem implements Comparator<DoctorItem> {
        public String dutySourceId = "";
        public String doctorId = "";
        public String doctorName = "";
        public String doctorTitleName = "";
        public String totalFee = "";
        public String skill = "";
        public String remainAvailableNumber = "";

        public DoctorItem(String name)
        {
            this.doctorName = name;
        }
        public DoctorItem(String dutySourceId,
                          String doctorId,
                          String doctorName,
                          String doctorTitleName,
                          String totalFee,
                          String skill,
                          String remainAvailableNumber) {
            this.dutySourceId = dutySourceId == null ? "" : dutySourceId;
            this.doctorId = doctorId == null ? "" : doctorId;
            this.doctorName = doctorName == null ? "" : doctorName;
            this.doctorTitleName = doctorTitleName == null ? "" : doctorTitleName;
            this.totalFee = totalFee == null ? "" : totalFee;
            this.skill = skill == null ? "" : skill;
            this.remainAvailableNumber = remainAvailableNumber == null ? "" : remainAvailableNumber;
        }
        public int compare(DoctorItem p1, DoctorItem p2) {
            Integer p1RemainNum = 0;
            Integer p2RemainNum = 0;
            try {
                p1RemainNum = Integer.parseInt(p1.remainAvailableNumber);
                p2RemainNum = Integer.parseInt(p2.remainAvailableNumber);
            } catch (Exception e){
            }
            if (p1RemainNum> p2RemainNum) {
                return -1;
            } else if (p1RemainNum < p2RemainNum) {
                return 1;
            }
            return 0;
        }
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(doctorName);
            sb.append(" ");
            sb.append(doctorTitleName);
            sb.append(" ");
            sb.append(skill);
            sb.append(" ");
            sb.append(totalFee);
            sb.append(" 可挂 ");
            sb.append(remainAvailableNumber);
            return sb.toString();
        }
    }
}
