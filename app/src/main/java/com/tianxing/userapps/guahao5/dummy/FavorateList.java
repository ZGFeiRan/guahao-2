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
public class FavorateList {

    /**
     * An array of sample (dummy) items.
     */
    public static List< Item> ITEMS = new ArrayList< Item>();

    /**
     * A map of sample (dummy) items, by ID.
     */
    public static Map<String, Item> ITEM_MAP = new HashMap<String,  Item>();

    static {
        // Add sample items.
        addItem(new  Item("1", "协和", "1400116", "特需妇科门诊1"));
    }

    public static boolean addItem(Item item) {
        if (ITEM_MAP.containsKey(item.hpid+item.ksid))
        {
            return false;
        }
        ITEMS.add(item);
        ITEM_MAP.put(item.hpid+item.ksid, item);
        return true;
    }

    /**
     * A dummy item representing a piece of content.
     */
    public static class  Item {
        public String hpid;
        public String hpName;
        public String ksid;
        public String ksName;

        public  Item(String hpid, String hpName, String ksid, String ksName) {
            this.hpid = hpid;
            this.hpName = hpName;
            this.ksid = ksid;
            this.ksName = ksName;
        }

        @Override
        public String toString() {
            return hpName + " " + ksName;
        }
    }
}
