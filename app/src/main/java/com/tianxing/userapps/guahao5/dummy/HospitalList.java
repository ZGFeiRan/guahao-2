package com.tianxing.userapps.guahao5.dummy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 * <p/>
 * TODO: Replace all uses of this class before publishing your app.
 */
public class HospitalList {

    /**
     * An array of sample (dummy) items.
     */
    public static List< HospitalItem> ITEMS = new ArrayList< HospitalItem>();

    /**
     * A map of sample (dummy) items, by ID.
     */
    public static Map<String, HospitalItem> ITEM_MAP = new HashMap<String,  HospitalItem>();

    static {
        // Add sample items.
        addItem(new  HospitalItem("1", "协和 8:30 7"));
        addItem(new  HospitalItem("12", "北京大学第一医院 8:45 7"));
        addItem(new  HospitalItem("142", "北京大学第三医院 09:30 7"));
        addItem(new  HospitalItem("4", "中国医学科学院阜外医院 9:00 7"));
        addItem(new  HospitalItem("4", "中国医学科学院阜外医院特需 9:00 7"));
        addItem(new  HospitalItem("34", "首都医科大学附属北京儿童医院 14:00 7"));
        addItem(new  HospitalItem("102","首都儿科研究所附属儿童医院 14:00 7"));
        addItem(new  HospitalItem("104", "首都医科大学附属北京妇产医院北京妇幼保健院东院 9:15 56"));
        addItem(new  HospitalItem("118", "首都医科大学附属北京妇产医院北京妇幼保健院西院 9:15 42"));
        addItem(new  HospitalItem("105", "首都医科大学附属北京同仁医院 8:45 7"));
        addItem(new  HospitalItem("123", "北京同仁医院南院(亦庄院区) 8:45 7"));
        addItem(new  HospitalItem("122", "中国中医科学院广安门医院 9:15 91"));
        addItem(new  HospitalItem("10", "中国中医科学院广安门医院南区 0:00 91"));
        addItem(new  HospitalItem("135", "北京中医药大学东直门医院 09:30 28"));
        addItem(new  HospitalItem("175", "北京中医药大学东直门医院东区（原北京市通州区中医医院） 10:00 91"));
    }

    public static void addItem( HospitalItem item) {
        if (!ITEM_MAP.containsKey(item.id)) {
            ITEMS.add(item);
            ITEM_MAP.put(item.id, item);
        }
    }

    /**
     * A dummy item representing a piece of content.
     */
    public static class  HospitalItem {
        public String id;
        public String content;

        public  HospitalItem(String id, String content) {
            this.id = id;
            this.content = content;
        }

        @Override
        public String toString() {
            return content;
        }
    }
}
