package com.function;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import java.util.ArrayList;
import java.util.HashMap;

import com.example.module_android_demo.R;
import android.view.LayoutInflater;
import android.widget.BaseAdapter;

/**
 * 带勾选自定义list适配器
 * 
 * @author Administrator
 * 
 */
public class MyAdapter_fre extends BaseAdapter {
	// 填充数据的list
	private ArrayList<String> list;
	// 用来控制CheckBox的选中状况
	private static HashMap<Integer, Boolean> isSelected;
	// 用来导入布局
	private LayoutInflater inflater = null;

	// 构造器
	@SuppressLint("UseSparseArrays")
	public MyAdapter_fre(ArrayList<String> list, Context context) {
		this.list = list;
		inflater = LayoutInflater.from(context);
		isSelected = new HashMap<Integer, Boolean>();
		// 初始化数据
		initDate();
	}

	// 初始化isSelected的数据
	private void initDate() {
		for (int i = 0; i < list.size(); i++) {
			getIsSelected().put(i, false);
		}
	}

	@Override
	public int getCount() {
		return list.size();
	}

	@Override
	public Object getItem(int position) {
		return list.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder_fre holder = null;
		if (convertView == null) {
			// 获得ViewHolder对象
			holder = new ViewHolder_fre();
			// 导入布局并赋值给convertview
			convertView = inflater.inflate(R.layout.listitemview_fre, null);

			holder.cb = (CheckBox) convertView.findViewById(R.id.item_cb);
			// Log.d("SLB4.0", (String)getItem(position));

			// 为view设置标签
			convertView.setTag(holder);
		} else {
			// 取出holder
			holder = (ViewHolder_fre) convertView.getTag();
		}
		holder.cb.setText((String) getItem(position));
		// 根据isSelected来设置checkbox的选中状况
		holder.cb.setChecked(getIsSelected().get(position));
		return convertView;
	}

	public static HashMap<Integer, Boolean> getIsSelected() {
		return isSelected;
	}

	public static void setIsSelected(HashMap<Integer, Boolean> isSelected) {
		MyAdapter_fre.isSelected = isSelected;
	}

	public static class ViewHolder_fre {
		public CheckBox cb;
	}
}