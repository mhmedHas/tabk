package com.example.module_android_demo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.os.Looper;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** بطاقة قطعة الذهب: أخضر = موجودة، أحمر = مرفوعة. */
public class GoldItemAdapter extends RecyclerView.Adapter<GoldItemAdapter.ViewHolder> {

    public interface PresenceChecker {
        boolean isPresent(String epc);
    }

    private static final int MAX_IMAGE_DIM = 500;
    private static final LruCache<String, Bitmap> imageCache =
            new LruCache<String, Bitmap>(24 * 1024 * 1024) {
                @Override
                protected int sizeOf(String key, Bitmap bmp) {
                    return bmp.getByteCount();
                }
            };

    private static final Set<String> imageLoadStarted = new HashSet<>();
    private static final ExecutorService bgExecutor = Executors.newFixedThreadPool(3);

    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final Context context;
    private final List<GoldCatalogItem> items;
    private final PresenceChecker presenceChecker;
    private final String storageUid;

    public GoldItemAdapter(Context context, List<GoldCatalogItem> items,
                           String storageUid, PresenceChecker presenceChecker) {
        this.context = context;
        this.items = items;
        this.presenceChecker = presenceChecker;
        this.storageUid = storageUid;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context)
                .inflate(R.layout.item_gold_card, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        GoldCatalogItem item = items.get(position);
        boolean present = presenceChecker.isPresent(item.epc);

        if (!item.registered) {
            bindUnregistered(holder, item, present);
            return;
        }

        int stateColor = Color.parseColor(present ? "#18E957" : "#FF3B3B");

        holder.tvName.setText(item.name);
        holder.tvType.setText("النوع: " + item.type);
        holder.tvWeight.setText("الوزن: " + item.weight + " جم");
        holder.tvColor.setText("اللون: " + item.color);
        holder.tvEpc.setText("EPC: " + item.epc);
        holder.tvStatus.setText(present ? "موجود" : "مرفوعة");
        holder.tvStatus.setTextColor(stateColor);
        holder.tvStatusIcon.setText(present ? "●" : "▲");
        holder.tvStatusIcon.setTextColor(stateColor);

        GradientDrawable border = new GradientDrawable();
        border.setColor(Color.parseColor(present ? "#0D2D22" : "#301A1D"));
        border.setStroke(dp(1), Color.parseColor(present ? "#1B9E59" : "#A83C43"));
        border.setCornerRadius(dp(12));
        holder.card.setBackground(border);

        holder.imgView.setTag(item.epc);
        holder.imgView.setImageResource(android.R.drawable.ic_menu_gallery);
        loadImage(item, holder.imgView);
    }

    /** كارت لشريحة اتقرأت لكن مالهاش سجل في Firebase: تحذير برتقالي بدل الصورة والبيانات. */
    private void bindUnregistered(ViewHolder holder, GoldCatalogItem item, boolean present) {
        holder.tvName.setText("قطعة غير مسجلة");
        holder.tvType.setText("النوع: غير معروف");
        holder.tvWeight.setText("الوزن: غير معروف");
        holder.tvColor.setText("اللون: غير معروف");
        holder.tvEpc.setText("EPC: " + item.epc);
        holder.tvStatus.setText(present ? "غير مسجلة - موجودة" : "غير مسجلة - مرفوعة");

        int warnColor = Color.parseColor("#F5A623");
        holder.tvStatus.setTextColor(warnColor);
        holder.tvStatusIcon.setText("!");
        holder.tvStatusIcon.setTextColor(warnColor);

        GradientDrawable border = new GradientDrawable();
        border.setColor(Color.parseColor("#332B12"));
        border.setStroke(dp(1), warnColor);
        border.setCornerRadius(dp(12));
        holder.card.setBackground(border);

        // مفيش أي بحث عن صورة على الشبكة لقطعة غير مسجلة أصلاً.
        holder.imgView.setTag(item.epc);
        holder.imgView.setImageResource(android.R.drawable.ic_dialog_alert);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private void loadImage(final GoldCatalogItem item, final ImageView target) {
        final String epc = item.epc;
        Bitmap cached = imageCache.get(epc);
        if (cached != null) {
            target.setImageBitmap(cached);
            return;
        }

        synchronized (imageLoadStarted) {
            if (imageLoadStarted.contains(epc)) return;
            imageLoadStarted.add(epc);
        }

        if (item.imageUrl != null &&
                (item.imageUrl.startsWith("http://") || item.imageUrl.startsWith("https://"))) {
            loadHttpImage(item.imageUrl, epc, target);
            return;
        }

        if (storageUid == null || storageUid.isEmpty()) return;

        StorageReference ref = FirebaseStorage.getInstance().getReference()
                .child("images").child("users").child(storageUid).child(epc.toUpperCase());

        ref.listAll()
                .addOnSuccessListener(result -> {
                    if (!result.getItems().isEmpty()) {
                        downloadStorageImage(result.getItems().get(0), epc, target);
                    }
                })
                .addOnFailureListener(e -> { });
    }

    private void downloadStorageImage(StorageReference ref, final String epc, final ImageView target) {
        ref.getBytes(4L * 1024 * 1024)
                .addOnSuccessListener(bytes -> decodeAndSet(bytes, epc, target))
                .addOnFailureListener(e -> { });
    }

    private void loadHttpImage(final String url, final String epc, final ImageView target) {
        bgExecutor.execute(() -> {
            HttpURLConnection connection = null;
            try {
                connection = (HttpURLConnection) new URL(url).openConnection();
                connection.setConnectTimeout(7000);
                connection.setReadTimeout(10000);
                connection.setInstanceFollowRedirects(true);

                InputStream input = connection.getInputStream();
                ByteArrayOutputStream output = new ByteArrayOutputStream();
                byte[] buffer = new byte[8192];
                int count;
                while ((count = input.read(buffer)) != -1) {
                    output.write(buffer, 0, count);
                    if (output.size() > 4 * 1024 * 1024) break;
                }
                input.close();
                decodeAndSet(output.toByteArray(), epc, target);
            } catch (Exception ignored) {
            } finally {
                if (connection != null) connection.disconnect();
            }
        });
    }

    private void decodeAndSet(byte[] bytes, final String epc, final ImageView target) {
        bgExecutor.execute(() -> {
            Bitmap bmp = decodeSampled(bytes);
            if (bmp == null) return;
            imageCache.put(epc, bmp);
            mainHandler.post(() -> {
                if (epc.equals(target.getTag())) target.setImageBitmap(bmp);
            });
        });
    }

    private Bitmap decodeSampled(byte[] bytes) {
        try {
            BitmapFactory.Options bounds = new BitmapFactory.Options();
            bounds.inJustDecodeBounds = true;
            BitmapFactory.decodeByteArray(bytes, 0, bytes.length, bounds);
            int sample = 1;
            while ((bounds.outWidth / sample) > MAX_IMAGE_DIM ||
                    (bounds.outHeight / sample) > MAX_IMAGE_DIM) {
                sample *= 2;
            }
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inSampleSize = sample;
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length, opts);
        } catch (Exception e) {
            return null;
        }
    }

    private int dp(int value) {
        return (int) (value * context.getResources().getDisplayMetrics().density + 0.5f);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        View card;
        ImageView imgView;
        TextView tvName, tvType, tvWeight, tvColor, tvEpc, tvStatus, tvStatusIcon;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            card = itemView.findViewById(R.id.card_root);
            imgView = itemView.findViewById(R.id.img_item);
            tvName = itemView.findViewById(R.id.tv_name);
            tvType = itemView.findViewById(R.id.tv_type);
            tvWeight = itemView.findViewById(R.id.tv_weight);
            tvColor = itemView.findViewById(R.id.tv_color);
            tvEpc = itemView.findViewById(R.id.tv_epc);
            tvStatus = itemView.findViewById(R.id.tv_status);
            tvStatusIcon = itemView.findViewById(R.id.tv_status_icon);
        }
    }
}
