package com.example.module_android_demo;

import com.function.commfun;
import com.uhf.api.cls.Reader.CustomCmdType;
import com.uhf.api.cls.Reader.IMPINJM4QtPara;
import com.uhf.api.cls.Reader.IMPINJM4QtResult;
import com.uhf.api.cls.Reader.Mtr_Param;
import com.uhf.api.cls.Reader.READER_ERR;
import com.uhf.api.cls.Reader.TagFilter_ST;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

/**
 * 特殊指令QT
 * 
 * @author Administrator
 * 
 */
public class SubQTActivity extends Activity {

	MyApplication myapp;
	Spinner spinner_opbank_qt;
	RadioGroup gr_opant_qt, gr_match_qt, gr_enablefil_qt, gr_qt_cmd, gr_qt_vw,
			gr_qt_dic, gr_qt_sta;
	CheckBox cb_pwd_qt;
	TagFilter_ST g2tf_qt = null;
	Button button_qt;
	ArrayAdapter<String> arradp_fbank;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.subqtlayout);
		Application app = getApplication();
		myapp = (MyApplication) app;

		button_qt = (Button) findViewById(R.id.button_qtset);
		cb_pwd_qt = (CheckBox) findViewById(R.id.checkBox_opacepwd_qt);
		gr_opant_qt = (RadioGroup) findViewById(R.id.radioGroup_opant_qt);
		gr_match_qt = (RadioGroup) findViewById(R.id.radioGroup_opmatch_qt);
		gr_enablefil_qt = (RadioGroup) findViewById(R.id.radioGroup_enableopfil_qt);
		gr_qt_cmd = (RadioGroup) findViewById(R.id.radioGroup_cmdtype);
		gr_qt_vw = (RadioGroup) findViewById(R.id.radioGroup_view);
		gr_qt_dic = (RadioGroup) findViewById(R.id.radioGroup_dic);
		gr_qt_sta = (RadioGroup) findViewById(R.id.radioGroup_sta);
		spinner_opbank_qt = (Spinner) findViewById(R.id.spinner_opfbank_qt);
		arradp_fbank = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, MyApplication.spifbank);
		spinner_opbank_qt.setAdapter(arradp_fbank);

		button_qt.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				try {
					SetOpant();
					SetPassword();
					SetFiler();

					byte[] rpaswd = new byte[4];
					if (!myapp.Rparams.password.equals("")) {
						myapp.Mreader.Str2Hex(myapp.Rparams.password,
								myapp.Rparams.password.length(), rpaswd);
					}

					// m4 qt
					IMPINJM4QtPara CustomPara = myapp.Mreader.new IMPINJM4QtPara();
					CustomPara.TimeOut = 1000;
					CustomPara.CmdType = commfun.SortGroup(gr_qt_cmd);
					if (CustomPara.CmdType == 1) {
						CustomPara.MemType = commfun.SortGroup(gr_qt_vw) == 0 ? 1
								: 0;
						CustomPara.PersistType = commfun.SortGroup(gr_qt_sta) == 0 ? 1
								: 0;
						CustomPara.RangeType = commfun.SortGroup(gr_qt_dic) == 0 ? 1
								: 0;
					}
					CustomPara.AccessPwd = rpaswd;

					IMPINJM4QtResult CustomRet = myapp.Mreader.new IMPINJM4QtResult();
					myapp.Mreader.CustomCmd(myapp.Rparams.opant,
							CustomCmdType.IMPINJ_M4_Qt, CustomPara, CustomRet);

				} catch (Exception e) {
					// TODO Auto-generated catch block
					Toast.makeText(SubQTActivity.this,
							MyApplication.Constr_excep + e.getMessage(),
							Toast.LENGTH_SHORT).show();
				}

			}

		});
	}

	private void SetOpant() {
		myapp.Rparams.opant = commfun.SortGroup(gr_opant_qt) + 1;
	}

	private void SetFiler() {
		if (commfun.SortGroup(gr_enablefil_qt) == 1) {
			EditText et = (EditText) findViewById(R.id.editText_opfilterdata_qt);
			int ln = et.getText().toString().length();
			if (ln == 1 || ln % 2 == 1)
				ln++;
			byte[] fdata = new byte[ln / 2];
			myapp.Mreader.Str2Hex(et.getText().toString(), et.getText()
					.toString().length(), fdata);

			g2tf_qt = myapp.Mreader.new TagFilter_ST();
			g2tf_qt.fdata = fdata;
			g2tf_qt.flen = fdata.length * 8;
			int ma = commfun.SortGroup(gr_match_qt);
			if (ma == 1)
				g2tf_qt.isInvert = 1;
			else
				g2tf_qt.isInvert = 0;

			g2tf_qt.bank = spinner_opbank_qt.getSelectedItemPosition() + 1;

			EditText etadr = (EditText) findViewById(R.id.editText_opfilsadr_qt);
			g2tf_qt.startaddr = Integer.valueOf(etadr.getText().toString());

			READER_ERR er = myapp.Mreader.ParamSet(
					Mtr_Param.MTR_PARAM_TAG_FILTER, g2tf_qt);
			if (er == READER_ERR.MT_OK_ERR)
				Toast.makeText(SubQTActivity.this,
						MyApplication.Constr_ok + er.toString(),
						Toast.LENGTH_SHORT).show();
			else
				Toast.makeText(SubQTActivity.this,
						MyApplication.Constr_failed + er.toString(),
						Toast.LENGTH_SHORT).show();
		} else {
			g2tf_qt = null;
			myapp.Mreader.ParamSet(Mtr_Param.MTR_PARAM_TAG_FILTER, g2tf_qt);
		}
	}

	private void SetPassword() {
		myapp.Rparams.password = "";
		if (cb_pwd_qt.isChecked()) {
			EditText et = (EditText) findViewById(R.id.editText_password_qt);
			myapp.Rparams.password = et.getText().toString();
		}
	}
}
