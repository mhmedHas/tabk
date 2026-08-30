package com.example.module_android_demo;

/**
 * بيانات قطعة واحدة تم جلبها من Firestore بعد قراءة الـ EPC.
 */
public class GoldCatalogItem {
    public String epc;
    public String name = "قطعة ذهب";
    public String type = "ذهب";
    public String weight = "-";
    public String color = "ذهبي";
    public String imageUrl = "";

    /** false = الشريحة اتقرأت لكن EPC مش موجود في Firebase لهذا المستخدم. */
    public boolean registered = true;

    public GoldCatalogItem(String epc) {
        this.epc = epc;
    }

    /** يبني كارت لشريحة اتقرأت لكن غير مسجلة على Firebase. */
    public static GoldCatalogItem unregistered(String epc) {
        GoldCatalogItem item = new GoldCatalogItem(epc);
        item.registered = false;
        item.name = "قطعة غير مسجلة";
        item.type = "-";
        item.weight = "-";
        item.color = "-";
        item.imageUrl = "";
        return item;
    }
}
