package com.treeapps.syllablestrainer.editor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import com.google.gson.reflect.TypeToken;
import com.treeapps.syllablestrainer.R;
import com.treeapps.syllablestrainer.utils.clsColoredStringBuilder;
import com.treeapps.syllablestrainer.utils.clsUtils;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class ActivityEditor extends ListActivity {
	
	public static class clsEditorWord {
		public boolean boolIsEnabled;
		public ArrayList<clsEditorSyllable> objSyllables = new ArrayList<clsEditorSyllable>();


		public CharSequence GetSyllable(int intSyllableIndex) {
			if (intSyllableIndex + 1 > objSyllables.size()) {
				return "";
			} else {
				return objSyllables.get(intSyllableIndex).strValue;
			}
		}
		
		@Override
		public String toString() {
			// TODO Auto-generated method stub
			String strResult = "";
			for (clsEditorSyllable objEditorSyllable: this.objSyllables) {
				strResult += objEditorSyllable.strValue;
			}
			return strResult;
		}
	}
	
	public static class clsEditorSyllable {
		public clsEditorSyllable(String strValue) {
			this.strValue = strValue;
		}

		public String strValue;
	}
	
	public static class clsInputData {
		public ArrayList<clsEditorWord> objWords = new ArrayList<clsEditorWord>();
	}
	
	public static class clsOutputData {
		public ArrayList<clsEditorWord> objWords = new ArrayList<clsEditorWord>();
	}
	
	public class clsListItem {
		public boolean boolIsSelected;
		public clsEditorWord objWord;
	}

	public static final String INPUT_DATA = "com.treeapps.syllablestrainer.editor.input_data";
	public static final String OUTPUT_DATA = "com.treeapps.syllablestrainer.editor.output_data";
	public static final String INPUT_DATA_TYPE = "com.treeapps.syllablestrainer.editor.input_data_type";
	
	public static final int INPUT_DATA_TYPE_REAL_WORDS = 0;
	public static final int INPUT_DATA_TYPE_SYLLABLES = 1;
	
	// Persistence
	ArrayList<clsListItem> objListItems = new ArrayList<clsListItem>();
	clsEditorArrayAdapter objEditorArrayAdapter;
	int enumInputDataType;
	
	// End of persistence	

	clsInputData objInputData;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_editor);
		

		// Session management
		if (savedInstanceState == null) {
			// Get input data
			Bundle objBundle = getIntent().getExtras();  
			String strInputData = objBundle.getString(INPUT_DATA);
			objInputData = clsUtils.DeSerializeFromString(strInputData, clsInputData.class);
			enumInputDataType = objBundle.getInt(INPUT_DATA_TYPE);
			
			
			// Fill list
			PopulateListItemsFromInputData();
			objEditorArrayAdapter =  new clsEditorArrayAdapter(this, R.layout.editor_list_item, objListItems);
			setListAdapter(objEditorArrayAdapter);
			
			SaveState();
		} else {
			LoadState();
		}
	}

	void SaveState() {
    	clsUtils.SerializeToSharedPreferences("ActivityEditor", "objListItems", this, objListItems);
    	clsUtils.SerializeToSharedPreferences("ActivityEditor", "enumInputDataType", this, enumInputDataType);
    	
    }
    
    void LoadState() {
    	java.lang.reflect.Type collectionType = new TypeToken<ArrayList<clsListItem>>(){}.getType();
    	objListItems = clsUtils.DeSerializeFromSharedPreferences("ActivityEditor", "objListItems", this, collectionType);
    	objEditorArrayAdapter =  new clsEditorArrayAdapter(this, R.layout.editor_list_item, objListItems);
    	enumInputDataType = clsUtils.DeSerializeFromSharedPreferences("ActivityEditor", "enumInputDataType", this, int.class);
    	setListAdapter(objEditorArrayAdapter);
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

	private void PopulateListItemsFromInputData() {
		if (objInputData != null) {
			for (clsEditorWord objEditorWord : objInputData.objWords) {
				clsListItem objListItem = new clsListItem();
				objListItem.boolIsSelected = false;
				objListItem.objWord = objEditorWord;
				objListItems.add(objListItem);
			}
		} else {
			objListItems = new ArrayList<clsListItem>();
		}
	}
	
	private clsOutputData PopulateOutputDataFromListItems() {
		clsOutputData objOutputData = new clsOutputData();
		if (objListItems != null) {
			for (clsListItem objListItem : objListItems) {
				objOutputData.objWords.add(objListItem.objWord);
			}
		} 
		return objOutputData;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_editor, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		final ArrayList<clsListItem> objSelectedListItems = GetSelectedListItems();
		
		int id = item.getItemId();
		switch (id) {
		case R.id.action_editor_new:
			// Action
			clsEditorWord objNewObjWord =  new clsEditorWord();
			AddEditItem(objNewObjWord, new OnAddEditItemCompleteListener() {
				
				@Override
				public void OnComplete(boolean boolIsSaveNeeded, clsEditorWord objUpdatedWord) {
					if (boolIsSaveNeeded) {
						clsListItem objListItem =  new clsListItem();
						objListItem.objWord = objUpdatedWord;
						objListItem.boolIsSelected = false; 
						objListItems.add(objListItem);
						Collections.sort(objListItems, new ListComparator());
						objEditorArrayAdapter.notifyDataSetChanged();
					}
				}
			});
			break;
		case R.id.action_editor_edit:
			// Ensure single item is selected
			if (objSelectedListItems.size() == 0) {
				clsUtils.MessageBox(this, "Notice", false, "No item is selected", true, null);
				return true;
			} else if (objSelectedListItems.size() > 1) {
				clsUtils.MessageBox(this, "Notice", false, "More than one item is selected", true, null);
				return true;
			}
			// Action
			AddEditItem(objSelectedListItems.get(0).objWord, new OnAddEditItemCompleteListener() {
				
				@Override
				public void OnComplete(boolean boolIsSaveNeeded, clsEditorWord objUpdatedWord) {
					if (boolIsSaveNeeded) {
						objSelectedListItems.get(0).objWord = objUpdatedWord;
						Collections.sort(objListItems, new ListComparator());
						objEditorArrayAdapter.notifyDataSetChanged();
					}
				}
			});			
			break;	
		case R.id.action_editor_delete:
			// Ensure single item is selected
			if (objSelectedListItems.size() == 0) {
				clsUtils.MessageBox(this, "Notice", false, "No item is selected", true, null);
				return true;
			} 
			clsUtils.OkCancelDialog(this, "Are you sure you want to delete?", new clsUtils.OnOkCancelDialogCompleteListener() {			
				@Override
				public void OnComplete(boolean boolIsOk) {
					if (boolIsOk) {
						for (clsListItem objListItem: objSelectedListItems) {
							objListItems.remove(objListItem);
						}
						Collections.sort(objListItems, new ListComparator());
						objEditorArrayAdapter.notifyDataSetChanged();
					}					
				}
			});
			break;
		case R.id.action_editor_selection_clear:
			clsUtils.OkCancelDialog(this, "Are you sure you want to clear all selections?", new clsUtils.OnOkCancelDialogCompleteListener() {
				
				@Override
				public void OnComplete(boolean boolIsOk) {
					if (boolIsOk) {
						for (clsListItem objListItem : objListItems) {
							objListItem.boolIsSelected = false;
						}
						objEditorArrayAdapter.notifyDataSetChanged();
					}
				}
			});
			
			break;
		case R.id.action_editor_selection_select_all:
			clsUtils.OkCancelDialog(this, "Are you sure you want to select all?", new clsUtils.OnOkCancelDialogCompleteListener() {
				
				@Override
				public void OnComplete(boolean boolIsOk) {
					if (boolIsOk) {
						for (clsListItem objListItem : objListItems) {
							objListItem.boolIsSelected = true;
						}
						objEditorArrayAdapter.notifyDataSetChanged();
					}
				}
			});
			
			break;
		case R.id.action_editor_selection_enable_all:
			clsUtils.OkCancelDialog(this, "Are you sure you want to enable all?", new clsUtils.OnOkCancelDialogCompleteListener() {
				
				@Override
				public void OnComplete(boolean boolIsOk) {
					if (boolIsOk) {
						for (clsListItem objListItem : objListItems) {
							objListItem.objWord.boolIsEnabled = true;
						}
						objEditorArrayAdapter.notifyDataSetChanged();
					}
				}
			});
			break;
		case R.id.action_editor_selection_disable_all:
			clsUtils.OkCancelDialog(this, "Are you sure you want to disable all?", new clsUtils.OnOkCancelDialogCompleteListener() {
				
				@Override
				public void OnComplete(boolean boolIsOk) {
					if (boolIsOk) {
						for (clsListItem objListItem : objListItems) {
							objListItem.objWord.boolIsEnabled = false;
						}
						objEditorArrayAdapter.notifyDataSetChanged();
					}
				}
			});
			
			break;
		}
				
		return true;
	}
	
	public interface OnAddEditItemCompleteListener {
		public void OnComplete(boolean boolIsSaveNeeded, clsEditorWord objUpdatedWord);
	}
	
	public void AddEditItem (clsEditorWord objWord, final OnAddEditItemCompleteListener OnAddEditItemCompleteListener) {
		LayoutInflater factory = LayoutInflater.from(this);

		//Text_entry is an Layout XML file containing two text field to display in alert dialog
		final View textEntryView = factory.inflate(R.layout.add_edit_item, null);

		final EditText objSyll1EditText = (EditText) textEntryView.findViewById(R.id.add_edit_syll_1);
		final EditText objSyll2EditText = (EditText) textEntryView.findViewById(R.id.add_edit_syll_2);
		final EditText objSyll3EditText = (EditText) textEntryView.findViewById(R.id.add_edit_syll_3);
		final EditText objSyll4EditText = (EditText) textEntryView.findViewById(R.id.add_edit_syll_4);

		objSyll1EditText.setText(objWord.GetSyllable(0), TextView.BufferType.EDITABLE);
		objSyll1EditText.setInputType(InputType.TYPE_CLASS_TEXT);
		objSyll2EditText.setText(objWord.GetSyllable(1), TextView.BufferType.EDITABLE);
		objSyll2EditText.setInputType(InputType.TYPE_CLASS_TEXT);
		objSyll3EditText.setText(objWord.GetSyllable(2), TextView.BufferType.EDITABLE);
		objSyll3EditText.setInputType(InputType.TYPE_CLASS_TEXT);
		objSyll4EditText.setText(objWord.GetSyllable(3), TextView.BufferType.EDITABLE);
		objSyll4EditText.setInputType(InputType.TYPE_CLASS_TEXT);
		
		// Only display as many syllables as per item type
		if (enumInputDataType == INPUT_DATA_TYPE_SYLLABLES) {
			objSyll2EditText.setVisibility(View.GONE);
			objSyll3EditText.setVisibility(View.GONE);
			objSyll4EditText.setVisibility(View.GONE);
		}

		final AlertDialog.Builder alert = new AlertDialog.Builder(this);
		alert.setTitle("Edit syllables:").setView(textEntryView).setPositiveButton("Save", new DialogInterface.OnClickListener() {
			   public void onClick(DialogInterface dialog, int whichButton) {
				   clsEditorWord objUpdatedWord = new clsEditorWord();
				   
				   if (!TextUtils.isEmpty(objSyll1EditText.getText().toString())) {
					   objUpdatedWord.objSyllables.add(new clsEditorSyllable(objSyll1EditText.getText().toString()));
				   }
				   
				   if (!TextUtils.isEmpty(objSyll2EditText.getText().toString())) {
					   objUpdatedWord.objSyllables.add(new clsEditorSyllable(objSyll2EditText.getText().toString()));
				   }
				   
				   if (!TextUtils.isEmpty(objSyll3EditText.getText().toString())) {
					   objUpdatedWord.objSyllables.add(new clsEditorSyllable(objSyll3EditText.getText().toString()));
				   }
				   
				   if (!TextUtils.isEmpty(objSyll4EditText.getText().toString())) {
					   objUpdatedWord.objSyllables.add(new clsEditorSyllable(objSyll4EditText.getText().toString()));
				   }
				   
				   OnAddEditItemCompleteListener.OnComplete(true, objUpdatedWord);
			   	}
		  }).setNegativeButton("Cancel",  new DialogInterface.OnClickListener() {
			   public void onClick(DialogInterface dialog, int whichButton) {
				   OnAddEditItemCompleteListener.OnComplete(false, null);
			   }
		  });
		alert.show();
	}
	
	private ArrayList<clsListItem> GetSelectedListItems() {
		ArrayList<clsListItem> objSelectedListItems = new ArrayList<clsListItem>();
		for (clsListItem objListItem: objListItems) {
			if (objListItem.boolIsSelected) {
				objSelectedListItems.add(objListItem);
			}
		}
		return objSelectedListItems;
	}
	
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Do you want to accept the changes before exiting?");
		builder.setPositiveButton("Accept", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				clsOutputData objOutputData = PopulateOutputDataFromListItems();
				String strOutputData = clsUtils.SerializeToString(objOutputData);
				Intent objIntent = getIntent();
				objIntent.putExtra(OUTPUT_DATA, strOutputData);
				setResult(RESULT_OK, getIntent());    	
				ActivityEditor.this.finish();
			}
			
			
		});
		builder.setNeutralButton("Exit", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				dialog.cancel();
				// Return to caller
				setResult(RESULT_CANCELED, getIntent());    	
				ActivityEditor.this.finish();
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
			String strLhsWord = lhs.objWord.toString();
			String strRhsWord = rhs.objWord.toString();
			return strLhsWord.compareToIgnoreCase(strRhsWord);
		}			
	}

	public class clsEditorArrayAdapter extends ArrayAdapter<clsListItem>  {

		Context objContext;
		int intResource;
		List<clsListItem> objListItems;
		
		public clsEditorArrayAdapter(Context objContext, int intResource, List<clsListItem> objListItems) {
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
			TextView objWordItem = (TextView) todoView.findViewById(R.id.word_item);
			clsColoredStringBuilder objColoredStringBuilder = new clsColoredStringBuilder();
			int intIndexCount = 0;
			for (clsEditorSyllable objEditorSyllable: objListItem.objWord.objSyllables) {
				switch (intIndexCount) {
				case 0:
					objColoredStringBuilder.AddSnippetToString(objEditorSyllable.strValue, Color.rgb(255, 0, 0));
					intIndexCount+= 1;
					break;
				case 1:
					objColoredStringBuilder.AddSnippetToString(objEditorSyllable.strValue, Color.rgb(45, 110, 100));
					intIndexCount+= 1;
					break;
				case 2:
					objColoredStringBuilder.AddSnippetToString(objEditorSyllable.strValue, Color.rgb(0, 0, 255));
					intIndexCount+= 1;
					break;
				case 3:
					objColoredStringBuilder.AddSnippetToString(objEditorSyllable.strValue, Color.rgb(180, 160, 40));
					intIndexCount+= 1;
					break;
				}					
			}
			// Display
			objWordItem.setText(objColoredStringBuilder.GetString());
			
			
			// Checkbox - enabled
			CheckBox objEnabledCheckBox = (CheckBox) todoView.findViewById(R.id.word_enabled_checkbox);
			objEnabledCheckBox.setChecked(objListItem.objWord.boolIsEnabled);
			objEnabledCheckBox.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					objListItem.objWord.boolIsEnabled = ((CheckBox)v).isChecked();					
				}
			});
			
			return todoView;
		}
		 
	}
}
