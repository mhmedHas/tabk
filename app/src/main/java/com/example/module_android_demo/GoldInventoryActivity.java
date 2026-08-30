package com.example.module_android_demo;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.uhf.api.cls.Reader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * شاشة صحن الذهب.
 *
 * المنطق الأساسي:
 * 1) القارئ يقرأ EPC من الشريحة.
 * 2) الـ EPC هو المفتاح الوحيد للبحث عن القطعة في Firebase للمستخدم الحالي.
 * 3) لا يتم تحميل/عرض كل قطع Firebase مسبقاً.
 * 4) عند العثور على القطعة، يتم جلب الاسم والنوع والوزن واللون والصورة وعرضها.
 * 5) الأخضر = القارئ يراها الآن.
 * 6) الأحمر = كانت موجودة ثم اختفت من قراءات القارئ لمدة 1.6 ثانية.
 */
public class GoldInventoryActivity extends Activity {

    private static final long PRESENCE_WINDOW_MS = 1600L;

    private final Map<String, GoldCatalogItem> itemCache = new HashMap<>();
    private final Set<String> displayedEpcs = new HashSet<>();
    private final Set<String> pendingEpcs = new HashSet<>();
    private final Map<String, Long> lastSeen = new HashMap<>();
    private final List<GoldCatalogItem> displayedItems = new ArrayList<>();

    private GoldItemAdapter adapter;
    private TextView tvStatTotal;
    private TextView tvStatPresent;
    private TextView tvStatMissing;
    private TextView tvLastUpdate;
    private TextView tvEmpty;
    private TextView tvReaderState;
    private SwipeRefreshLayout swipeRefresh;

    private final Handler uiHandler = new Handler(Looper.getMainLooper());
    private long lastUpdateMs = 0L;

    private final Runnable presenceTicker = new Runnable() {
        @Override
        public void run() {
            refreshPresenceUi();
            uiHandler.postDelayed(this, 250L);
        }
    };

    private BroadcastReceiver tagReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_gold_inventory);

        tvStatTotal = findViewById(R.id.tv_stat_total);
        tvStatPresent = findViewById(R.id.tv_stat_present);
// tvStatMissing = findViewById(R.id.tv_stat_missing);
// مؤقتاً: استخدم TextView موجود أو أنشئ واحداً في الكود
        tvStatMissing = new TextView(this);
        tvStatMissing.setText("0");        tvLastUpdate = findViewById(R.id.tv_last_update);
        tvEmpty = findViewById(R.id.tv_empty);
        tvReaderState = findViewById(R.id.tv_reader_state);
        swipeRefresh = findViewById(R.id.swipe_refresh);

        RecyclerView rvItems = findViewById(R.id.rv_items);
        rvItems.setLayoutManager(new GridLayoutManager(this, 3));

        String uid = auth.getCurrentUser().getUid();
        adapter = new GoldItemAdapter(this, displayedItems, uid, this::isEpcPresent);
        rvItems.setAdapter(adapter);

        Button btnRefresh = findViewById(R.id.btn_refresh_catalog);
        btnRefresh.setOnClickListener(v -> refreshFirebaseDataForDisplayedItems());

        swipeRefresh.setOnRefreshListener(this::refreshFirebaseDataForDisplayedItems);

        updateReaderState();
        registerTagReceiver();
        refreshPresenceUi();
    }

    private void registerTagReceiver() {
        tagReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent == null || !MainActivity.BROADCAST_ACTION1.equals(intent.getAction())) {
                    return;
                }

                byte[] epcBytes = intent.getByteArrayExtra("EPC");
                if (epcBytes == null || epcBytes.length == 0) return;

                String epc = Reader.bytes_Hexstr(epcBytes);
                handleEpcRead(epc);
            }
        };

        IntentFilter filter = new IntentFilter(MainActivity.BROADCAST_ACTION1);
        ContextCompat.registerReceiver(this, tagReceiver, filter,
                ContextCompat.RECEIVER_NOT_EXPORTED);
    }

    private void handleEpcRead(String rawEpc) {
        String epc = normalizeEpc(rawEpc);
        if (epc.isEmpty()) return;

        lastSeen.put(epc, System.currentTimeMillis());
        lastUpdateMs = System.currentTimeMillis();
        updateReaderState();

        // القطعة اتعرفت قبل كده؛ لا تعمل طلب Firebase في كل قراءة RFID.
        if (displayedEpcs.contains(epc)) {
            refreshPresenceUi();
            return;
        }

        // لو الطلب شغال بالفعل لا نكرره.
        if (pendingEpcs.contains(epc)) {
            refreshPresenceUi();
            return;
        }

        GoldCatalogItem cached = itemCache.get(epc);
        if (cached != null) {
            addItemToSession(cached);
            return;
        }

        pendingEpcs.add(epc);
        findItemByEpc(epc);
    }

    /**
     * بحث مباشر باستخدام EPC المقروء، بدون تحميل catalog كامل من Firebase.
     */
    private void findItemByEpc(final String epc) {
        final FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            pendingEpcs.remove(epc);
            return;
        }

        String uid = auth.getCurrentUser().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users").document(uid).collection("items")
                .whereEqualTo("epcHex", epc)
                .limit(1)
                .get()
                .addOnSuccessListener(this, snapshot -> {
                    if (!snapshot.isEmpty()) {
                        handleFirebaseItem(epc, snapshot.getDocuments().get(0));
                        return;
                    }

                    // توافق مع البيانات القديمة التي يكون فيها EPC داخل payload.qrCode.
                    db.collection("users").document(uid).collection("items")
                            .whereEqualTo("payload.qrCode", epc)
                            .limit(1)
                            .get()
                            .addOnSuccessListener(this, payloadSnapshot -> {
                                if (!payloadSnapshot.isEmpty()) {
                                    handleFirebaseItem(epc, payloadSnapshot.getDocuments().get(0));
                                } else {
                                    findInBalances(epc, uid);
                                }
                            })
                            .addOnFailureListener(this, e -> findInBalances(epc, uid));
                })
                .addOnFailureListener(this, e -> findInBalances(epc, uid));
    }

    private void findInBalances(final String epc, final String uid) {
        FirebaseFirestore.getInstance()
                .collection("users").document(uid).collection("balances")
                .whereEqualTo("epcHex", epc)
                .limit(1)
                .get()
                .addOnSuccessListener(this, snapshot -> {
                    if (!snapshot.isEmpty()) {
                        handleFirebaseItem(epc, snapshot.getDocuments().get(0));
                    } else {
                        // الشريحة اتقرأت فعليًا لكن مالهاش أي سجل في Firebase.
                        // نعرضها كـ "غير مسجلة" بدل ما نتجاهلها تمامًا.
                        markEpcUnregistered(epc);
                    }
                })
                .addOnFailureListener(this, e -> {
                    pendingEpcs.remove(epc);
                    refreshPresenceUi();
                    Toast.makeText(this, "تعذر قراءة بيانات القطعة من Firebase", Toast.LENGTH_SHORT).show();
                });
    }

    /** يعرض كارت "غير مسجلة" لشريحة مقروءة لا يوجد لها أي سجل مطابق في Firebase. */
    @RequiresApi(api = Build.VERSION_CODES.N)
    private void markEpcUnregistered(String epc) {
        pendingEpcs.remove(epc);
        // لا نضيفها لـ itemCache: لو اتسجلت لاحقًا على Firebase، زر التحديث (سحب للأسفل)
        // هيحدّثها تلقائيًا لأنها موجودة في displayedItems.
        addItemToSession(GoldCatalogItem.unregistered(epc));
    }

    private void handleFirebaseItem(String epc, DocumentSnapshot doc) {
        Map<String, Object> data = doc.getData();
        if (data == null) {
            pendingEpcs.remove(epc);
            return;
        }

        Map<String, Object> payload = asMap(data.get("payload"));

        GoldCatalogItem item = new GoldCatalogItem(epc);
        item.name = firstNonEmpty(
                str(data.get("name")),
                str(payload.get("name")),
                str(payload.get("kind")),
                str(payload.get("type")),
                "قطعة ذهب");
        item.type = firstNonEmpty(
                str(data.get("type")),
                str(data.get("category")),
                str(payload.get("type")),
                str(payload.get("kind")),
                "ذهب");
        item.weight = firstNonEmpty(
                str(data.get("weight")),
                str(payload.get("weight")),
                "-");
        item.color = firstNonEmpty(
                str(data.get("color")),
                str(payload.get("color")),
                "ذهبي");
        item.imageUrl = firstNonEmpty(
                str(data.get("imageUrl")),
                str(data.get("image")),
                str(data.get("photoUrl")),
                str(payload.get("imageUrl")),
                str(payload.get("image")),
                str(payload.get("photoUrl")),
                "");

        itemCache.put(epc, item);
        pendingEpcs.remove(epc);
        addItemToSession(item);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void addItemToSession(GoldCatalogItem item) {
        String epc = normalizeEpc(item.epc);
        if (epc.isEmpty() || displayedEpcs.contains(epc)) return;

        displayedEpcs.add(epc);
        displayedItems.add(item);
        displayedItems.sort((a, b) -> a.name.compareToIgnoreCase(b.name));
        refreshPresenceUi();
    }

    private void refreshFirebaseDataForDisplayedItems() {
        if (displayedItems.isEmpty()) {
            swipeRefresh.setRefreshing(false);
            return;
        }

        swipeRefresh.setRefreshing(true);
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            swipeRefresh.setRefreshing(false);
            return;
        }

        String uid = auth.getCurrentUser().getUid();
        final int[] remaining = {displayedItems.size()};

        for (GoldCatalogItem oldItem : new ArrayList<>(displayedItems)) {
            String epc = oldItem.epc;
            // نفس منطق البحث الثلاثي (items -> payload.qrCode -> balances) المستخدم عند أول قراءة،
            // عشان قطعة اتعرضت "غير مسجلة" تقدر تتحدّث تلقائيًا لو اتضافت لـ Firebase بعدين.
            lookupDocByEpc(epc, uid, doc -> {
                if (doc != null) {
                    updateCachedItem(epc, doc);
                } else {
                    markCachedItemUnregistered(epc);
                }
                remaining[0]--;
                if (remaining[0] == 0) finishRefresh();
            });
        }
    }

    private interface DocLookupCallback {
        void onResult(DocumentSnapshot doc);
    }

    /** بحث ثلاثي الطبقات عن EPC معين: items.epcHex -> items.payload.qrCode -> balances.epcHex. */
    private void lookupDocByEpc(final String epc, final String uid, final DocLookupCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(uid).collection("items")
                .whereEqualTo("epcHex", epc)
                .limit(1)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (!snapshot.isEmpty()) {
                        callback.onResult(snapshot.getDocuments().get(0));
                        return;
                    }
                    db.collection("users").document(uid).collection("items")
                            .whereEqualTo("payload.qrCode", epc)
                            .limit(1)
                            .get()
                            .addOnSuccessListener(payloadSnapshot -> {
                                if (!payloadSnapshot.isEmpty()) {
                                    callback.onResult(payloadSnapshot.getDocuments().get(0));
                                    return;
                                }
                                db.collection("users").document(uid).collection("balances")
                                        .whereEqualTo("epcHex", epc)
                                        .limit(1)
                                        .get()
                                        .addOnSuccessListener(balancesSnapshot -> callback.onResult(
                                                balancesSnapshot.isEmpty() ? null : balancesSnapshot.getDocuments().get(0)))
                                        .addOnFailureListener(e -> callback.onResult(null));
                            })
                            .addOnFailureListener(e -> callback.onResult(null));
                })
                .addOnFailureListener(e -> callback.onResult(null));
    }

    private void updateCachedItem(String epc, DocumentSnapshot doc) {
        Map<String, Object> data = doc.getData();
        if (data == null) return;
        Map<String, Object> payload = asMap(data.get("payload"));
        GoldCatalogItem item = itemCache.get(epc);
        if (item == null) item = new GoldCatalogItem(epc);

        item.registered = true;
        item.name = firstNonEmpty(str(data.get("name")), str(payload.get("name")), str(payload.get("kind")), item.name);
        item.type = firstNonEmpty(str(data.get("type")), str(data.get("category")), str(payload.get("type")), str(payload.get("kind")), item.type);
        item.weight = firstNonEmpty(str(data.get("weight")), str(payload.get("weight")), item.weight);
        item.color = firstNonEmpty(str(data.get("color")), str(payload.get("color")), item.color);
        item.imageUrl = firstNonEmpty(str(data.get("imageUrl")), str(data.get("image")), str(data.get("photoUrl")), str(payload.get("imageUrl")), str(payload.get("image")), item.imageUrl);

        itemCache.put(epc, item);
        for (int i = 0; i < displayedItems.size(); i++) {
            if (epc.equals(displayedItems.get(i).epc)) {
                displayedItems.set(i, item);
                break;
            }
        }
    }

    /** لو التحديث لم يجد سجل في Firebase، اتأكد إن القطعة المعروضة لسه واضحة إنها "غير مسجلة". */
    private void markCachedItemUnregistered(String epc) {
        for (int i = 0; i < displayedItems.size(); i++) {
            GoldCatalogItem existing = displayedItems.get(i);
            if (epc.equals(existing.epc) && existing.registered) {
                // كان معروض كمسجل وبقى غير موجود في Firebase (اتحذف مثلاً) - رجّعه "غير مسجلة".
                GoldCatalogItem unregistered = GoldCatalogItem.unregistered(epc);
                itemCache.remove(epc);
                displayedItems.set(i, unregistered);
                break;
            }
        }
    }

    private void finishRefresh() {
        swipeRefresh.setRefreshing(false);
        adapter.notifyDataSetChanged();
        refreshPresenceUi();
    }

    private boolean isEpcPresent(String epc) {
        Long seen = lastSeen.get(epc);
        return seen != null && System.currentTimeMillis() - seen < PRESENCE_WINDOW_MS;
    }

    private void refreshPresenceUi() {
        int present = 0;
        for (GoldCatalogItem item : displayedItems) {
            if (isEpcPresent(item.epc)) present++;
        }

        int total = displayedItems.size();
        int missing = total - present;

        tvStatTotal.setText(String.valueOf(total));
        tvStatPresent.setText(String.valueOf(present));
        tvStatMissing.setText(String.valueOf(missing));
        tvEmpty.setVisibility(total == 0 ? View.VISIBLE : View.GONE);

        if (lastUpdateMs > 0) {
            tvLastUpdate.setText(formatTime(lastUpdateMs));
        }

        updateReaderState();
        adapter.notifyDataSetChanged();
    }

    private void updateReaderState() {
        // وجود MainActivity في الخلفية يعني أن محرك القارئ هو الذي يرسل Broadcastات الـ EPC.
        boolean active = MainActivityActiveHolder.isActive;
        tvReaderState.setText(active ? "متصل" : "جاهز لقراءة RFID");
        tvReaderState.setTextColor(active ? Color.rgb(50, 235, 80) : Color.LTGRAY);
    }

    /** حالة بسيطة يتم ضبطها من lifecycle الخاص بـ MainActivity في النسخة الحالية. */
    static class MainActivityActiveHolder {
        static boolean isActive = false;
    }

    private static String normalizeEpc(String value) {
        if (value == null) return "";
        return value.replace(" ", "").replace("\n", "").replace("\r", "").trim().toUpperCase();
    }

    private static Map<String, Object> asMap(Object value) {
        if (value instanceof Map) {
            return (Map<String, Object>) value;
        }
        return new HashMap<>();
    }

    private static String str(Object value) {
        return value == null ? "" : String.valueOf(value).trim();
    }

    private static String firstNonEmpty(String... values) {
        for (String value : values) {
            if (value != null && !value.trim().isEmpty()) return value.trim();
        }
        return "";
    }

    private static String formatTime(long timeMs) {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("hh:mm:ss a", java.util.Locale.US);
        return sdf.format(new java.util.Date(timeMs));
    }

    @Override
    protected void onResume() {
        super.onResume();
        MainActivityActiveHolder.isActive = true;
        uiHandler.post(presenceTicker);
    }

    @Override
    protected void onPause() {
        super.onPause();
        uiHandler.removeCallbacks(presenceTicker);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MainActivityActiveHolder.isActive = false;
        if (tagReceiver != null) {
            try {
                unregisterReceiver(tagReceiver);
            } catch (Exception ignored) {
            }
        }
    }
}
