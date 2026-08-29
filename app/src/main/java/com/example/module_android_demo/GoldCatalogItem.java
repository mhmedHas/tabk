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

    public GoldCatalogItem(String epc) {
        this.epc = epc;
    }
}
