package com.treeapps.syllablestrainer.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Environment;
import android.text.Html;
import android.util.DisplayMetrics;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.gson.Gson;

public class clsUtils {

	public interface MessageBoxCompleted {
		public void Complete(boolean boolIsError);	
	}
	
	public static void MessageBox(Context context, String strTitle, final boolean boolIsError, String strMessage,
			boolean boolDisplayAsToast, final MessageBoxCompleted objMessageBoxCompleted)  {
		if (!boolDisplayAsToast) {
			AlertDialog.Builder builder = new AlertDialog.Builder(context);
			if (!strTitle.isEmpty()) {
				clsColoredStringBuilder objBuilder = new clsColoredStringBuilder();
				if (boolIsError) {
					objBuilder.AddSnippetToString(strTitle, Color.RED);
				} else {
					objBuilder.AddSnippetToString(strTitle, Color.BLUE);
				}				
				builder.setTitle(objBuilder.GetString());
			}
			builder.setMessage(strMessage);
			builder.setCancelable(true);
			builder.setPositiveButton("Close", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					if (objMessageBoxCompleted != null) {
						objMessageBoxCompleted.Complete(boolIsError);
					} 
				}
			});
			AlertDialog dialog = builder.create();
			dialog.show();
		} else {
			Toast.makeText(context, strMessage, Toast.LENGTH_SHORT).show();
		}
		
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T DeSerializeFromSharedPreferences(String strPrimaryKey, String strSecondaryKey,
			Context _context, Type objType) {

		SharedPreferences sharedPref = _context.getSharedPreferences(strPrimaryKey, Context.MODE_PRIVATE);
		String strSerialize = sharedPref.getString(strSecondaryKey, "");
		if (!strSerialize.isEmpty()) {
			Gson gson = new Gson();
			return (T) gson.fromJson(strSerialize, objType);
		}
		return null;
	}

	public static <T> void SerializeToSharedPreferences(String strPrimaryKey, String strSecondaryKey, Context _context,
			T obj) {
		Gson gson = new Gson();
		String strSerialized = gson.toJson(obj);
		SharedPreferences sharedPref = _context.getSharedPreferences(strPrimaryKey, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPref.edit();
		editor.putString(strSecondaryKey, strSerialized);
		editor.commit();
		sharedPref = null;
	}

	@SuppressWarnings("unchecked")
	// Note: Here is example of getting type of ArrayList:
	// java.lang.reflect.Type collectionType = new TypeToken<ArrayList<clsListViewState>>(){}.getType();
	public static <T> T DeSerializeFromString(String strSerialize, Type objType) {
		Gson gson = new Gson();
		return (T) gson.fromJson(strSerialize, objType);
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T DeSerializeFromFile(File objFile,  Type objType) throws Exception {
		String strData = "";
		if (objFile.exists()) {
			try {
				FileReader fr = new FileReader(objFile);
				BufferedReader br = new BufferedReader(fr);
				String s;
				while ((s = br.readLine()) != null) {
					strData += s;
				}
				br.close();
				fr.close();
			} catch (Exception e) {
				throw new Exception("Could not deserialize file. " + e.getMessage());
			} 			
		} else {
			throw new Exception("Could not deserialize file. File does not exist");
		}
		return (T) clsUtils.DeSerializeFromString(strData, objType);
	}
	
	public static <T> void SerializeToFile(File objFile, T obj) {
		try {
			FileWriter fw = new FileWriter(objFile);
			String strData = clsUtils.SerializeToString(obj);
			fw.write(strData);
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	
	public static <T> String SerializeToString(T obj) {
		Gson gson = new Gson();
		return gson.toJson(obj);
	}

	public static String GetAppDirectoryName(Context objContext) {
		String strAppDirectoryName = Environment.getExternalStorageDirectory().toString() + "/SyllablesTrainer";
		File objFolder = new File(strAppDirectoryName);
		if (!objFolder.exists()) {
			objFolder.mkdirs();
		}
		return strAppDirectoryName;
	}

	public static String GetWordsFilename() {
		// TODO Auto-generated method stub
		return "words.txt";
	}


	public static File GetRepositoryFile(Context objContext) {
		String strRepositoryFilename = "data.syllablestrainer";
		File objFile = new File(GetAppDirectoryName(objContext), strRepositoryFilename);
		return objFile;
	}

	public static File GetImportWordFile(Context objContext) {
		String strRepositoryFilename = "data" + GetWordsImportFileType();
		File objFile = new File(GetAppDirectoryName(objContext), strRepositoryFilename);
		return objFile;
	}

	public static File GetImportPreSyllableFile(Context objContext) {
		String strRepositoryFilename = "data" + GetPreSyllImportFileType();
		File objFile = new File(GetAppDirectoryName(objContext), strRepositoryFilename);
		return objFile;
	}

	public static File GetImportRootSyllableFile(Context objContext) {
		String strRepositoryFilename = "data" + GetRootSyllImportFileType();
		File objFile = new File(GetAppDirectoryName(objContext), strRepositoryFilename);
		return objFile;
	}

	public static File GetImportPostSyllableFile(Context objContext) {
		String strRepositoryFilename = "data" + GetPostSyllImportFileType();
		File objFile = new File(GetAppDirectoryName(objContext), strRepositoryFilename);
		return objFile;
	}

	public static String GetWordsImportFileType() {
		// TODO Auto-generated method stub
		return ".words";
	}
	
	public static String GetPreSyllImportFileType() {
		// TODO Auto-generated method stub
		return ".presyll";
	}
	
	public static String GetRootSyllImportFileType() {
		// TODO Auto-generated method stub
		return ".rootsyll";
	}
	
	public static String GetPostSyllImportFileType() {
		// TODO Auto-generated method stub
		return ".postsyll";
	}

	public static void RemoveAppFolder (Context objContext) {
		File objFolder = new File(GetAppDirectoryName(objContext));
		if (objFolder.exists()) {
			DeleteFolder(objFolder);
		}
	}
	
	public static void DeleteFolder(File file) { 
        if (file.isDirectory())
            for (String child : file.list())
            	DeleteFolder(new File(file, child));
        file.delete();  // delete child file or empty directory
    }
	
	public static void SetMenuItemEnabled(MenuItem objMenuItem, boolean IsEnabled) {
		if (IsEnabled) {
			objMenuItem.setEnabled(true);
		} else {
			objMenuItem.setEnabled(false);
		}
	}
	public static interface OnOkCancelDialogCompleteListener {
		public void OnComplete(boolean boolIsOk);
	}
	
	public static void OkCancelDialog(Context objContext, String strPrompt, final OnOkCancelDialogCompleteListener OnOkCancelDialogCompleteListener) {
		AlertDialog.Builder objBuilder = new AlertDialog.Builder(objContext);
		objBuilder.setTitle(strPrompt);
		objBuilder.setPositiveButton("OK", new OnClickListener() {			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				OnOkCancelDialogCompleteListener.OnComplete(true);				
			}
		});
		objBuilder.setNegativeButton("Cancel", new OnClickListener() {			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				OnOkCancelDialogCompleteListener.OnComplete(false);				
			}
		});
		objBuilder.show();
	}
	
	public static int DpToPx(Context context, int dp) {
		DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
		int px = Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
		return px;
	}

	public static int PxToDp(Context context, int px) {
		DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
		int dp = Math.round(px / (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
		return dp;
	}
	
	public static int getAppVersionCode(Context context) {
		try {
			PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
			return packageInfo.versionCode;
		} catch (NameNotFoundException e) {
			// should never happen
			throw new RuntimeException("Could not get package name: " + e);
		}
	}
	
	public static String getAppVersionName(Context context) {
		try {
			PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
			return packageInfo.versionName;
		} catch (NameNotFoundException e) {
			// should never happen
			throw new RuntimeException("Could not get package name: " + e);
		}
	}
	
	public static void SendLetYourFriendsKnowEmail(Activity objActivity) {

		final Intent intent = new Intent(Intent.ACTION_SEND);
		intent.setType("text/html");
		String strSubject = "Check out SyllablesTrainer";
		intent.putExtra(Intent.EXTRA_SUBJECT, strSubject);
		String strMessage = "Here is the link: https://play.google.com/store/apps/details?id=com.treeapps.syllablestrainer<br><br>Regards";
		intent.putExtra(Intent.EXTRA_TEXT, Html.fromHtml(strMessage));
		try {
			objActivity.startActivity(intent);
		} catch (ActivityNotFoundException ex) {
			throw ex;
		}
	}
}
