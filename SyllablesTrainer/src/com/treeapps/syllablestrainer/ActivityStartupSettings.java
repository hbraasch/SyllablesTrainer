package com.treeapps.syllablestrainer;

import com.treeapps.syllablestrainer.multidivtrainer.ActivitySelectTables.clsSelectTablesArrayAdapter;
import com.treeapps.syllablestrainer.multidivtrainer.ActivitySelectTables.clsSession;
import com.treeapps.syllablestrainer.utils.clsUtils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;

public class ActivityStartupSettings extends PreferenceActivity {
	
	class clsSession {
		
	}
	
	// Persist
	public clsSession objSession = new clsSession();
	public Context objContext;
	// End of persist

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		objContext = this;
		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction()
	        .replace(android.R.id.content, new StartupSettingsFragment()).commit();
			
			SaveState();
		} else {
			LoadState();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_startup_settings, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class StartupSettingsFragment extends PreferenceFragment {
		@Override
		public void onCreate(Bundle savedInstanceState) {
			// TODO Auto-generated method stub
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.layout.activity_startup_settings);
			
			Preference prefHelp = findPreference("label_help");	
			prefHelp.setOnPreferenceClickListener(new OnPreferenceClickListener() {
				
				@Override
				public boolean onPreferenceClick(Preference preference) {
					String url = getActivity().getResources().getString(R.string.url_help);
					Intent i = new Intent(Intent.ACTION_VIEW);
					i.setData(Uri.parse(url));
					startActivity(i);
					return false;
				}
			});
			
			
			Preference prefAbout = findPreference("label_about");
			prefAbout.setTitle("SyllablesTrainer Version: " + clsUtils.getAppVersionName(getActivity()) + "." + clsUtils.getAppVersionCode(getActivity()));
			
		}
	}
	
	
	
	void SaveState() {
    	clsUtils.SerializeToSharedPreferences("ActivitySelectTables", "objSession", this, objSession);   	
    }
    
    void LoadState() {
    	objSession = clsUtils.DeSerializeFromSharedPreferences("ActivitySelectTables", "objSession", this, clsSession.class);
    	objContext = this;
    }
    
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		LoadState();
	}
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		SaveState();
		super.onPause();
	}
}
