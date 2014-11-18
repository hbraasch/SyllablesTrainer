package com.treeapps.syllablestrainer.utils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;

public class clsRobustProgressDialog extends ProgressDialog {
	private Context mContext;

	public clsRobustProgressDialog(Context context) {
		super(context);
		mContext = context;
	}

	public clsRobustProgressDialog(Context context, int theme) {
		super(context, theme);
		mContext = context;
	}

	public void show() {
		if (mContext.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
			((Activity) mContext).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		else
			((Activity) mContext).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		super.show();
	}

	public void dismiss() {
		super.dismiss();
		((Activity) mContext).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
	}

}
