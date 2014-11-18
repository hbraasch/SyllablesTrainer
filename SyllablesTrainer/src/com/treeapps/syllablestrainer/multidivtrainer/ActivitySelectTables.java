package com.treeapps.syllablestrainer.multidivtrainer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.google.gson.reflect.TypeToken;
import com.treeapps.syllablestrainer.R;
import com.treeapps.syllablestrainer.editor.ActivityEditor;
import com.treeapps.syllablestrainer.editor.ActivityEditor.clsListItem;
import com.treeapps.syllablestrainer.editor.ActivityEditor.clsOutputData;
import com.treeapps.syllablestrainer.utils.clsUtils;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class ActivitySelectTables extends ListActivity {
	
	public static class clsListItem {
		public boolean boolIsSelected;
		public int intTable;
	}
	
	public class clsSession {
		public ArrayList<clsListItem> objListItems = new ArrayList<clsListItem>();
	}

	public static final String LIST_ITEMS = "com.treeapps.syllablestrainer.multidivtrainer.list_items";
	
	// Persistent
	clsSession objSession = new clsSession();
	Context objContext;
	clsSelectTablesArrayAdapter objSelectTablesArrayAdapter;
	
	// End of persistent

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_select_tables);
		
		if (savedInstanceState == null) {
			String strListItems = getIntent().getExtras().getString(LIST_ITEMS);
			java.lang.reflect.Type collectionType = new TypeToken<ArrayList<clsListItem>>(){}.getType();
			objSession.objListItems = clsUtils.DeSerializeFromString(strListItems, collectionType);
			
			objSelectTablesArrayAdapter =  new clsSelectTablesArrayAdapter(this, R.layout.select_table_list_item, objSession.objListItems);
			setListAdapter(objSelectTablesArrayAdapter);
			
			SaveState();
		} else {
			LoadState();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_select_tables, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		switch (id) {
		case R.id.action_table_selector_selection_clear:
			clsUtils.OkCancelDialog(this, "Are you sure you want to clear all selections?", new clsUtils.OnOkCancelDialogCompleteListener() {
				
				@Override
				public void OnComplete(boolean boolIsOk) {
					if (boolIsOk) {
						for (clsListItem objListItem : objSession.objListItems) {
							objListItem.boolIsSelected = false;
						}
						objSelectTablesArrayAdapter.notifyDataSetChanged();
					}
				}
			});
			
			break;
		case R.id.action_table_selector_selection_select_all:
			clsUtils.OkCancelDialog(this, "Are you sure you want to select all?", new clsUtils.OnOkCancelDialogCompleteListener() {
				
				@Override
				public void OnComplete(boolean boolIsOk) {
					if (boolIsOk) {
						for (clsListItem objListItem : objSession.objListItems) {
							objListItem.boolIsSelected = true;
						}
						objSelectTablesArrayAdapter.notifyDataSetChanged();
					}
				}
			});
			
			break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	private boolean IsAtLeastOneTableEnabled() {
		for (clsListItem objListItem: objSession.objListItems) {
			if (objListItem.boolIsSelected) return true;
		}
		return false;
	}
	
	void SaveState() {
    	clsUtils.SerializeToSharedPreferences("ActivitySelectTables", "objSession", this, objSession);   	
    }
    
    void LoadState() {
    	objSession = clsUtils.DeSerializeFromSharedPreferences("ActivitySelectTables", "objSession", this, clsSession.class);
    	objSelectTablesArrayAdapter =  new clsSelectTablesArrayAdapter(this, R.layout.select_table_list_item, objSession.objListItems);
		setListAdapter(objSelectTablesArrayAdapter);
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
	
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		
		if (!IsAtLeastOneTableEnabled()) {
			clsUtils.MessageBox(this,  "Notice", false, "No tables are selected. Please select at least one table", false, null);
			return;
		}
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Do you want to accept the changes before exiting?");
		builder.setPositiveButton("Accept", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub

				Intent objIntent = getIntent();
				String strListItems = clsUtils.SerializeToString(objSession.objListItems);
				objIntent.putExtra(LIST_ITEMS, strListItems);
				setResult(RESULT_OK, getIntent());    	
				ActivitySelectTables.this.finish();
			}
			
			
		});
		builder.setNeutralButton("Exit", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				dialog.cancel();
				// Return to caller
				setResult(RESULT_CANCELED, getIntent());    	
				ActivitySelectTables.this.finish();
			}
		});
		builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// Do nothing				
			}
		});
		builder.show();
		
	}
	


	class ListComparator implements Comparator<clsListItem> {

		@Override
		public int compare(clsListItem lhs, clsListItem rhs) {
			Integer intLhs = lhs.intTable;
			Integer intRhs = rhs.intTable;
			return intLhs.compareTo(intRhs);
		}			
	}
	
	public class clsSelectTablesArrayAdapter extends ArrayAdapter<clsListItem>  {

		Context objContext;
		int intResource;
		List<clsListItem> objListItems;
		
		public clsSelectTablesArrayAdapter(Context objContext, int intResource, List<clsListItem> objListItems) {
			super(objContext, intResource, objListItems);			
			this.objContext = objContext;
			this.intResource = intResource;
			this.objListItems = objListItems;
			Collections.sort(this.objListItems, new ListComparator());
		}
		
		
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			final clsListItem objListItem = getItem(position);
			RelativeLayout todoView;
			if (convertView == null) {
				todoView = new RelativeLayout(getContext());
				String inflater = Context.LAYOUT_INFLATER_SERVICE;
				LayoutInflater li;
				li = (LayoutInflater) getContext().getSystemService(inflater);
				li.inflate(intResource, todoView, true);
			} else {
				todoView = (RelativeLayout) convertView;
			}
			
			// Checkbox -select
			CheckBox objCheckBox = (CheckBox) todoView.findViewById(R.id.word_checkbox);
			objCheckBox.setChecked(objListItem.boolIsSelected);
			objCheckBox.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					objListItem.boolIsSelected = ((CheckBox)v).isChecked();					
				}
			});
			
			// Text item
			TextView objTableItem = (TextView) todoView.findViewById(R.id.table_item);
			objTableItem.setText(Integer.toString(objListItem.intTable));		
			return todoView;
		}
		 
	}
}
