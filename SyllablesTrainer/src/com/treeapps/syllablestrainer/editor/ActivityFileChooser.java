package com.treeapps.syllablestrainer.editor;

import java.io.File;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.text.DateFormat;

import com.treeapps.syllablestrainer.R;
import com.treeapps.syllablestrainer.R.id;
import com.treeapps.syllablestrainer.R.layout;
import com.treeapps.syllablestrainer.R.string;
import com.treeapps.syllablestrainer.utils.clsUtils;

import android.os.Bundle;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class ActivityFileChooser extends ListActivity {

	public static final String PATH = "com.treeapps.syllablestrainer.path";
	public static final String NEW_FILE_PROMPT = "com.treeapps.syllablestrainer.new_file_prompt";
	public static final String FILTER_EXTENSION = "com.treeapps.syllablestrainer.filter_extension";

	/** Includes the "." */

	class clsSession {
		public String strStartDir;
		public File fileCurrentDir;
		public boolean boolAddNewFilePrompt;
		public String strFilterExtension;
		public List<clsFileChooserItem> objDirs = new ArrayList<clsFileChooserItem>();
		public List<clsFileChooserItem> objFiles = new ArrayList<clsFileChooserItem>();
	}

	// Persistent
	clsSession objSession = new clsSession();
	private clsFileChooserArrayAdapter objFileChooserAdapter;

	// End of persistent

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (savedInstanceState == null) {
			Bundle objBundle = getIntent().getExtras();
			objSession.strStartDir = objBundle.getString(PATH);
			objSession.boolAddNewFilePrompt = objBundle.getBoolean(NEW_FILE_PROMPT);
			objSession.strFilterExtension = objBundle.getString(FILTER_EXTENSION);
			objSession.fileCurrentDir = new File(objSession.strStartDir);
			UpdateUi(objSession.fileCurrentDir);
			SaveState();
		} else {
			LoadState();
			UpdateUi(objSession.fileCurrentDir);
		}
	}

	private void LoadState() {
		objSession = clsUtils.DeSerializeFromSharedPreferences("ActivityFileChooser", "objSession", this,
				clsSession.class);
		objFileChooserAdapter = new clsFileChooserArrayAdapter(ActivityFileChooser.this,
				R.layout.file_chooser_list_item, objSession.objDirs);
		this.setListAdapter(objFileChooserAdapter);
	}

	private void SaveState() {
		// TODO Auto-generated method stub
		clsUtils.SerializeToSharedPreferences("ActivityFileChooser", "objSession", this, objSession);
	}

	@Override
	protected void onPause() {
		super.onPause();
		SaveState();
	}

	@Override
	protected void onResume() {
		LoadState();
		super.onResume();
	}

	private boolean IsAppFileType(String filename, String strFilterExtension) {
		if (filename.lastIndexOf('.') > 0) {
			// get last index for '.' char
			int lastIndex = filename.lastIndexOf('.');

			// get extension
			String str = filename.substring(lastIndex);

			// match path name extension
			if (str.equals(strFilterExtension)) {
				return true;
			}
		}
		return false;
	}

	private void UpdateUi(File fileInitialPath) {
		objSession.objDirs.clear();
		objSession.objFiles.clear();
		File[] objFileItems = fileInitialPath.listFiles();
		this.setTitle("Current Dir: " + fileInitialPath.getName());

		try {
			for (File objFileItem : objFileItems) {
				Date lastModDate = new Date(objFileItem.lastModified());
				DateFormat formater = DateFormat.getDateTimeInstance();
				String date_modify = formater.format(lastModDate);
				if (objFileItem.isDirectory()) {

					File[] fbuf = objFileItem.listFiles();
					int buf = 0;
					if (fbuf != null) {
						buf = fbuf.length;
					} else
						buf = 0;
					String num_item = String.valueOf(buf);
					if (buf == 0)
						num_item = num_item + " item";
					else
						num_item = num_item + " items";

					// String formated = lastModDate.toString();
					objSession.objDirs.add(new clsFileChooserItem(objFileItem.getName(), num_item, date_modify,
							objFileItem.getAbsolutePath(), "directory_icon"));

				} else {
					String strFilename = objFileItem.getName();
					if (IsAppFileType(strFilename, objSession.strFilterExtension)) {
						objSession.objFiles.add(new clsFileChooserItem(strFilename, objFileItem.length() + " Bytes",
								date_modify, objFileItem.getAbsolutePath(), "file_icon"));
					}
				}
			}
		} catch (Exception e) {

		}
		Collections.sort(objSession.objDirs);
		if (objSession.boolAddNewFilePrompt) {
			objSession.objDirs.add(0, new clsFileChooserItem("<" + getResources().getString(R.string.new_file_prompt)
					+ ">", "", "", "", "file_icon"));
		}
		Collections.sort(objSession.objFiles);

		objSession.objDirs.addAll(objSession.objFiles);
		if (!fileInitialPath.getName().equalsIgnoreCase("sdcard"))
			objSession.objDirs.add(0, new clsFileChooserItem("..", "Parent Directory", "", fileInitialPath.getParent(),
					"directory_up"));
		objFileChooserAdapter = new clsFileChooserArrayAdapter(ActivityFileChooser.this,
				R.layout.file_chooser_list_item, objSession.objDirs);
		this.setListAdapter(objFileChooserAdapter);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		// TODO Auto-generated method stub
		super.onListItemClick(l, v, position, id);
		clsFileChooserItem objFileChooserItem = objFileChooserAdapter.getItem(position);
		if (objFileChooserItem.getImage().equalsIgnoreCase("directory_icon")
				|| objFileChooserItem.getImage().equalsIgnoreCase("directory_up")) {
			objSession.fileCurrentDir = new File(objFileChooserItem.getPath());
			UpdateUi(objSession.fileCurrentDir);
		} else {
			onFileClick(objFileChooserItem);
		}
	}

	private void onFileClick(clsFileChooserItem objFileChooserItem) {
		// Toast.makeText(this, "Folder Clicked: "+ currentDir,
		// Toast.LENGTH_SHORT).show();
		Intent intent = new Intent();
		intent.putExtra("SelectedPath", objSession.fileCurrentDir.toString());
		intent.putExtra("SelectedFileName", objFileChooserItem.getName());
		intent.putExtra("SelectedFilePath", objFileChooserItem.getPath());
		setResult(RESULT_OK, intent);
		finish();
	}

	public class clsFileChooserItem implements Comparable<clsFileChooserItem> {
		private String name;
		private String data;
		private String date;
		private String path;
		private String image;

		public clsFileChooserItem(String n, String d, String dt, String p, String img) {
			name = n;
			data = d;
			date = dt;
			path = p;
			image = img;

		}

		public String getName() {
			return name;
		}

		public String getData() {
			return data;
		}

		public String getDate() {
			return date;
		}

		public String getPath() {
			return path;
		}

		public String getImage() {
			return image;
		}

		public int compareTo(clsFileChooserItem o) {
			if (this.name != null)
				return this.name.toLowerCase().compareTo(o.getName().toLowerCase());
			else
				throw new IllegalArgumentException();
		}
	}

	public class clsFileChooserArrayAdapter extends ArrayAdapter<clsFileChooserItem> {

		private Context c;
		private int id;
		private List<clsFileChooserItem> items;

		public clsFileChooserArrayAdapter(Context context, int textViewResourceId, List<clsFileChooserItem> objects) {
			super(context, textViewResourceId, objects);
			c = context;
			id = textViewResourceId;
			items = objects;

		}

		public clsFileChooserItem getItem(int i) {
			return items.get(i);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;
			if (v == null) {
				LayoutInflater vi = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = vi.inflate(id, null);
			}

			/* create a new view of my layout and inflate it in the row */
			// convertView = ( RelativeLayout ) inflater.inflate( resource, null
			// );

			final clsFileChooserItem o = items.get(position);
			if (o != null) {
				TextView t1 = (TextView) v.findViewById(R.id.label_subscriptions);
				TextView t2 = (TextView) v.findViewById(R.id.TextView02);
				TextView t3 = (TextView) v.findViewById(R.id.TextViewDate);
				/* Take the ImageView from layout and set the city's image */
				ImageView imageCity = (ImageView) v.findViewById(R.id.fd_Icon1);
				String uri = "drawable/" + o.getImage();
				int imageResource = c.getResources().getIdentifier(uri, null, c.getPackageName());
				Drawable image = c.getResources().getDrawable(imageResource);
				imageCity.setImageDrawable(image);

				if (t1 != null)
					t1.setText(o.getName());
				if (t2 != null)
					t2.setText(o.getData());
				if (t3 != null)
					t3.setText(o.getDate());

			}
			return v;
		}

	}

}
