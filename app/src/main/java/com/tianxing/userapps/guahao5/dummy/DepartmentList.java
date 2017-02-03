package com.tianxing.userapps.guahao5.dummy;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 * <p/>
 * TODO: Replace all uses of this class before publishing your app.
 */
public class DepartmentList {
    private static Comparator cmp = Collator.getInstance(java.util.Locale.CHINA);
    /**
     * An array of sample (dummy) items.
     */
    public static List<DepartmentItem> ITEMS = new ArrayList<DepartmentItem>();
    public static Set<String> ITEMS_SET = new HashSet<String>();

    /*
    static {
        // Add 3 sample items.
        addItem(new DepartmentItem("1400116", "特需妇科"));
        addItem(new DepartmentItem("1400216", "特需产科门诊"));
        addItem(new DepartmentItem("1400210", "产科门诊"));
        addItem(new DepartmentItem("1400110", "妇科门诊"));
        addItem(new DepartmentItem("1220010", "骨科"));
    }
    */
    public static void clear()
    {
        ITEMS.clear();
        ITEMS_SET.clear();
    }
    public static void addItem(DepartmentItem item) {
        if (!ITEMS_SET.contains(item.id)) {
            ITEMS.add(item);
            ITEMS_SET.add(item.id);
        }
    }

    /**
     * A dummy item representing a piece of content.
     */
    public static class DepartmentItem implements Comparable<DepartmentItem>{
        public String id;
        public String content;

        public DepartmentItem(String id, String content) {
            this.id = id;
            this.content = content;
        }

        @Override
        public String toString() {
            return content;
        }

        @Override
        public int compareTo(DepartmentItem arg1) {
            return cmp.compare(this.content, arg1.content);
        }
    }
}
