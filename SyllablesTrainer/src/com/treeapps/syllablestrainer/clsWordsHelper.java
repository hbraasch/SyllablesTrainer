package com.treeapps.syllablestrainer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.UUID;

import com.treeapps.syllablestrainer.utils.clsRobustProgressDialog;
import com.treeapps.syllablestrainer.utils.clsUtils;
import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;


public class clsWordsHelper {
	
	Context objContext;
	public clsRepository objRepository = new clsRepository();
	public ArrayList<clsSyllable> objSyllables1 = new ArrayList<clsSyllable>();
	public ArrayList<clsSyllable> objSyllables2 = new ArrayList<clsSyllable>();
	public ArrayList<clsSyllable> objSyllables3 = new ArrayList<clsSyllable>();
	public ArrayList<clsSyllable> objSyllables4 = new ArrayList<clsSyllable>();
	
	private final static int DEFAULT_MAX_SYLLABLES_AMOUNT = 4; 
	private int intMaxSyllableAmount = DEFAULT_MAX_SYLLABLES_AMOUNT;
	private ArrayList<clsWord> objFilteredWords = new ArrayList<clsWord>();
	public ArrayList<clsSyllable> objFilteredPreSyllables = new ArrayList<clsSyllable>();
	public ArrayList<clsSyllable> objFilteredRootSyllables = new ArrayList<clsSyllable>();
	public ArrayList<clsSyllable> objFilteredPostSyllables = new ArrayList<clsSyllable>();
	
	
	public class clsRepository {
		public ArrayList<clsWord> objWords = new ArrayList<clsWord>();
		public ArrayList<clsSyllable> objPreSyllables = new ArrayList<clsSyllable>();
		public ArrayList<clsSyllable> objRootSyllables = new ArrayList<clsSyllable>();
		public ArrayList<clsSyllable> objPostSyllables = new ArrayList<clsSyllable>();
	}


	
	public void LoadRepository(Context objContext) throws Exception {
	
		// Load word data
		File objFile = clsUtils.GetRepositoryFile(objContext);
		try {
			objRepository = clsUtils.DeSerializeFromFile(objFile, clsRepository.class);
			GenerateSyllableCollections();
			UpdatePreSyllableEnableds();
			UpdateRootSyllableEnableds();
			UpdatePostSyllableEnableds();
		} catch (Exception e) {
			throw new Exception("Could not load repository. " + e.getMessage());
		}		
	}

	public void GenerateSyllableCollections() {
		// Generate syllable collections
		GenerateSyllableCollection(objSyllables1, 0);
		GenerateSyllableCollection(objSyllables2, 1);
		GenerateSyllableCollection(objSyllables3, 2);
		GenerateSyllableCollection(objSyllables4, 3);
	}
	
	public void UpdateWords (ArrayList<clsWord> objWords) {
		objRepository.objWords = objWords;
		UpdateWordSyllableRestrictedAndEnables(this.intMaxSyllableAmount);
		GenerateSyllableCollections();
	}
	
	public void UpdateWordSyllableRestrictedAndEnables(int intMaxSyllableAmount) {
		this.intMaxSyllableAmount = intMaxSyllableAmount;
		objFilteredWords.clear();
		for (clsWord objWord: objRepository.objWords) {
			if (objWord.objSyllables.size()<= intMaxSyllableAmount) {
				if (objWord.boolIsEnabled) {
					objFilteredWords.add(objWord);
				}
			}
		}
	}
	
	public void UpdatePreSyllables (ArrayList<clsSyllable> objSyllables) {
		objRepository.objPreSyllables = objSyllables;
		UpdatePreSyllableEnableds();
	}
	
	
	public void UpdateRootSyllables (ArrayList<clsSyllable> objSyllables) {
		objRepository.objRootSyllables = objSyllables;
		UpdateRootSyllableEnableds();
	}
	
	public void UpdatePostSyllables (ArrayList<clsSyllable> objSyllables) {
		objRepository.objPostSyllables = objSyllables;
		UpdatePostSyllableEnableds();
	}
	
	public void UpdatePreSyllableEnableds() {
		objFilteredPreSyllables.clear();
		for (clsSyllable objSyllable: objRepository.objPreSyllables) {
				if (objSyllable.boolIsEnabled) {
					objFilteredPreSyllables.add(objSyllable);
				}
		}
	}

	public void UpdateRootSyllableEnableds() {
		objFilteredRootSyllables.clear();
		for (clsSyllable objSyllable: objRepository.objRootSyllables) {
				if (objSyllable.boolIsEnabled) {
					objFilteredRootSyllables.add(objSyllable);
				}
		}
	}
	
	public void UpdatePostSyllableEnableds() {
		objFilteredPostSyllables.clear();
		for (clsSyllable objSyllable: objRepository.objPostSyllables) {
				if (objSyllable.boolIsEnabled) {
					objFilteredPostSyllables.add(objSyllable);
				}
		}
	}
	
	public void SaveRepository(Context objContext) throws Exception {
		
		File objFile = clsUtils.GetRepositoryFile(objContext);
		try {
			clsUtils.SerializeToFile(objFile, objRepository);
		} catch (Exception e) {
			throw new Exception("Could not save repository. " + e.getMessage());
		}	
	}
	
	
	
	private void GenerateSyllableCollection(ArrayList<clsSyllable> objSyllables, int intSyllableIndex) {
		objSyllables.clear();
		for (clsWord objWord: objRepository.objWords) {
			String strValue = objWord.GetSyllable(intSyllableIndex);
			if (!TextUtils.isEmpty(strValue)) {
				if(!objSyllables.contains(strValue)) {
					objSyllables.add(new clsSyllable(strValue, objWord.boolIsEnabled));
				}				
			}
		}
	}

	public String GetRandomWordId() {
		
		if (objRepository.objWords.size() == 0) {
			return "";
		}
		
		if (objFilteredWords.size() == 0) {
			UpdateWordSyllableRestrictedAndEnables(intMaxSyllableAmount);
			if (objFilteredWords.size() == 0) {
				return "";
			}
		} 
		
		Random r = new Random();
		int intMaxVal = objFilteredWords.size() - 1;
		int intMinVal = 0;
		int intIndex;
		if (intMaxVal != 0) {
			intIndex = r.nextInt((intMaxVal - intMinVal) + 1) + intMinVal;
		} else {
			intIndex = 0;
		}
		return objFilteredWords.get(intIndex).strId;

	}
	
	public static String GetRandomSyllable(ArrayList<clsSyllable> objSyllables) {
		if (objSyllables.size() == 0) {
			return "";
		} else if (objSyllables.size() == 1) {
			return objSyllables.get(0).strValue;			
		} else {			
			Random r = new Random();
			int intMaxVal = objSyllables.size() - 1;
			int intMinVal = 0;
			int intIndex;
			if (intMaxVal != 0) {
				intIndex = r.nextInt((intMaxVal - intMinVal) + 1) + intMinVal;
			} else {
				intIndex = 0;
			}
			return objSyllables.get(intIndex).strValue;
		}
	}

	public clsWord GetWordFromId(String strWordId) {
		for (clsWord objWord: objRepository.objWords) {
			if (strWordId.equals(objWord.strId)) {
				return objWord;
			}
		}
		return null;
	}
	
	public static class clsWord {
		public String strId;
		public boolean boolIsEnabled;
		public List<clsSyllable> objSyllables = new ArrayList<clsSyllable>();
		
		public clsWord(String strSyllable1, String strSyllable2, String strSyllable3, String strSyllable4,  boolean boolIsEnabled) {
			strId = UUID.randomUUID().toString();
			this.boolIsEnabled = boolIsEnabled;
			if (!TextUtils.isEmpty(strSyllable1)) {
				objSyllables.add(new clsSyllable(strSyllable1, boolIsEnabled));
			} else {
				return;
			}
			if (!TextUtils.isEmpty(strSyllable2)) {
				objSyllables.add(new clsSyllable(strSyllable2, boolIsEnabled));
			} else {
				return;
			}
			if (!TextUtils.isEmpty(strSyllable3)) {
				objSyllables.add(new clsSyllable(strSyllable3, boolIsEnabled));
			} else {
				return;
			}
			if (!TextUtils.isEmpty(strSyllable4)) {
				objSyllables.add(new clsSyllable(strSyllable4, boolIsEnabled));
			} else {
				return;
			}
			
			
		}
		
		@Override
		public boolean equals(Object o) {
			// TODO Auto-generated method stub
			if(o instanceof clsWord){				
				if (((clsWord)o).toString().equals(this.toString())) {
					return true;
				}
			}
			return false;
		}
		
		@Override
		public String toString() {
			String strWord = "";
			for (clsSyllable objSyllable: objSyllables) {
				strWord += objSyllable.strValue;
			}
			return strWord;
		}

		public String GetSyllable(int intSyllableIndex) {
			if (intSyllableIndex + 1 > objSyllables.size()) {
				return "";
			} else {
				return objSyllables.get(intSyllableIndex).strValue;
			}
		}
	}
	
	public static class clsSyllable {
		public String strValue;
		public boolean boolIsEnabled;
		public clsSyllable (String strSyllable, boolean boolIsEnabled) {
			strValue = strSyllable;
			this.boolIsEnabled = boolIsEnabled;
		}
		
		@Override
		public boolean equals(Object o) {
			// TODO Auto-generated method stub
			if(o instanceof clsSyllable){				
				if (((clsSyllable)o).strValue.equals(this.strValue)) {
					return true;
				}
			}
			return false;
		}
		
	}
	
	public interface OnImportDataFromCsvFileCompleteListener <T> {
		public void OnComplete (boolean boolIsSuccess, String strErrorMessage, ArrayList<T> objResult);
	}
	
	public interface ImportDataFromCsvFileMethod<T> {
		public ArrayList<T> Execute(File objDefaultCsvFile) throws Exception;
	}
	
	public ImportDataFromCsvFileMethod<clsWord> ImportWordDataFromCsvFileMethod =  new ImportDataFromCsvFileMethod<clsWord>() {

		@Override
		public ArrayList<clsWord> Execute(File objDefaultCsvFile) throws Exception {
			// TODO Auto-generated method stub
			return ImportWordDataFromCsvFile(objDefaultCsvFile);
		}
		
	};
	
	public ImportDataFromCsvFileMethod<clsSyllable> ImportSyllableDataFromCsvFileMethod =  new ImportDataFromCsvFileMethod<clsSyllable>() {

		@Override
		public ArrayList<clsSyllable> Execute(File objDefaultCsvFile) throws Exception {
			// TODO Auto-generated method stub
			return ImportSyllableDataFromCsvFile(objDefaultCsvFile);
		}
		
	};
	
	/**
	 * This async task gets passed one of two generic methods, one that does WORD import, the other that does SYLLABLES import
	 * See ImportDataFromCsvFileMethod
	 * @author HeinrichWork
	 *
	 * @param <T>
	 */
	public static class ImportDataFromCsvFileAsyncTask <T> extends AsyncTask<Void, String, ArrayList<T>> {
		
		File objDefaultCsvFile;
		Exception ex;
		OnImportDataFromCsvFileCompleteListener<T> OnImportDataFromCsvFileCompleteListener;
		clsRobustProgressDialog objProgressDialog;
		public static String strProgressMessage;
		ImportDataFromCsvFileMethod<T> ImportDataFromCsvFileMethod; 
		
		public ImportDataFromCsvFileAsyncTask(Context objContext, File objDefaultCsvFile, 
				OnImportDataFromCsvFileCompleteListener<T> OnImportDataFromCsvFileCompleteListener,
				String strInitialProgressMessage, ImportDataFromCsvFileMethod<T> ImportDataFromCsvFileMethod) {
			this.objDefaultCsvFile = objDefaultCsvFile;
			this.OnImportDataFromCsvFileCompleteListener = OnImportDataFromCsvFileCompleteListener;
			objProgressDialog = new clsRobustProgressDialog(objContext);
			ImportDataFromCsvFileAsyncTask.strProgressMessage = strInitialProgressMessage;
			this.ImportDataFromCsvFileMethod = ImportDataFromCsvFileMethod;
		}

		
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			try {
				objProgressDialog.setMessage(strProgressMessage);
				if (!objProgressDialog.isShowing()) {
				    	objProgressDialog.show();
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		@Override
		protected ArrayList<T> doInBackground(Void... params) {
			try {
				publishProgress(strProgressMessage);
				return (ArrayList<T>) ImportDataFromCsvFileMethod.Execute(objDefaultCsvFile);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				ex = e;
			}
			return null;
		}		
		
		@Override
    	protected void onProgressUpdate(String... values) {
        	try {
				if (objProgressDialog.isShowing()) {
				    objProgressDialog.setMessage(values[0]);
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
		
		@Override
		protected void onPostExecute(ArrayList<T> result) {
			super.onPostExecute(result);
			try {
				if (objProgressDialog.isShowing()) {
					objProgressDialog.dismiss();
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (ex == null) {
				OnImportDataFromCsvFileCompleteListener.OnComplete(true, "", result);
			} else {
				OnImportDataFromCsvFileCompleteListener.OnComplete(false, ex.getMessage(), null);
			}
		}
		
		@Override
		protected void onCancelled() {
			super.onCancelled();

			if (objProgressDialog.isShowing()) {
				objProgressDialog.dismiss();
			}
							
		}
		
		public void dismiss() {
	        if ( objProgressDialog != null ) {
	        	objProgressDialog.dismiss();
	        }
	    }
	}
	
	

	private static ArrayList<clsWord> ImportWordDataFromCsvFile(File objDefaultCsvFile) throws Exception {
		ArrayList<clsWord> objNewWords = new ArrayList<clsWord>();
		InputStream instream = null;
		try {
			// open the file for reading
			instream = new FileInputStream(objDefaultCsvFile.getAbsolutePath());

			// if file the available for reading
			if (instream != null) {
				  // prepare the file for reading
				  InputStreamReader inputreader = new InputStreamReader(instream);
				  BufferedReader buffreader = new BufferedReader(inputreader);
	
				  String line;
	
				  // read every line of the file into the line-variable, on line at the time
				  line = buffreader.readLine();
				  while (line != null) {				     
				    // Parse line
				     StringTokenizer tokens = new StringTokenizer(line, ",");
				     String strSyl1 = "", strSyl2 = "", strSyl3 = "", strSyl4 = "";
				     if (tokens.hasMoreTokens()) {
				    	 strSyl1 = tokens.nextToken().trim();
				     }
				     if (tokens.hasMoreTokens()) {
				    	 strSyl2 = tokens.nextToken().trim();
				     }
				     if (tokens.hasMoreTokens()) {
				    	 strSyl3 = tokens.nextToken().trim();
				     }
				     if (tokens.hasMoreTokens()) {
				    	 strSyl4 = tokens.nextToken().trim();
				     }
				     clsWord objWord = new clsWord(strSyl1, strSyl2, strSyl3, strSyl4, true);
				     objNewWords.add(objWord);
				     line = buffreader.readLine();
				  }
				  buffreader.close();
			}
		} catch (Exception ex) {
		    throw ex;
		} finally {
			instream.close();
		}
		return objNewWords;
	}
	
	public static ArrayList<clsSyllable> ImportSyllableDataFromCsvFile(File objDefaultCsvFile) throws Exception {
		ArrayList<clsSyllable> objSyllables = new ArrayList<clsSyllable>();
		InputStream instream = null;
		try {
			// open the file for reading
			instream = new FileInputStream(objDefaultCsvFile.getAbsolutePath());

			// if file the available for reading
			if (instream != null) {
				  // prepare the file for reading
				  InputStreamReader inputreader = new InputStreamReader(instream);
				  BufferedReader buffreader = new BufferedReader(inputreader);
	
				  String line;
	
				  // read every line of the file into the line-variable, on line at the time
				  line = buffreader.readLine().trim();
				  while (line != null) {				     				     
					 clsSyllable objSyllable = new clsSyllable(line, true);
				     if(!objSyllables.contains(objSyllable)) {
				    	 objSyllables.add(objSyllable);	
				     }				     			     
				     line = buffreader.readLine();
				  }
				  buffreader.close();
			}
		} catch (Exception ex) {
		    throw ex;
		} finally {
			instream.close();
		}
		return objSyllables;
	}



	

}
