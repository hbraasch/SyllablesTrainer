package com.treeapps.syllablestrainer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.treeapps.android.in_app_billing.util.IabHelper;
import com.treeapps.android.in_app_billing.util.IabHelper.OnConsumeFinishedListener;
import com.treeapps.android.in_app_billing.util.IabHelper.OnIabPurchaseFinishedListener;
import com.treeapps.android.in_app_billing.util.IabHelper.QueryInventoryFinishedListener;
import com.treeapps.android.in_app_billing.util.IabResult;
import com.treeapps.android.in_app_billing.util.Inventory;
import com.treeapps.android.in_app_billing.util.Purchase;
import com.treeapps.android.in_app_billing.util.SkuDetails;
import com.treeapps.syllablestrainer.clsWordsHelper.ImportDataFromCsvFileAsyncTask;
import com.treeapps.syllablestrainer.clsWordsHelper.OnImportDataFromCsvFileCompleteListener;
import com.treeapps.syllablestrainer.clsWordsHelper.clsSyllable;
import com.treeapps.syllablestrainer.clsWordsHelper.clsWord;
import com.treeapps.syllablestrainer.editor.ActivityEditor;
import com.treeapps.syllablestrainer.editor.ActivityFileChooser;
import com.treeapps.syllablestrainer.editor.ActivityEditor.clsEditorSyllable;
import com.treeapps.syllablestrainer.editor.ActivityEditor.clsEditorWord;
import com.treeapps.syllablestrainer.editor.ActivityEditor.clsInputData;
import com.treeapps.syllablestrainer.editor.ActivityEditor.clsOutputData;
import com.treeapps.syllablestrainer.multidivtrainer.ActivityMultiDivTrainer;
import com.treeapps.syllablestrainer.utils.clsColoredStringBuilder;
import com.treeapps.syllablestrainer.utils.clsMCrypt;
import com.treeapps.syllablestrainer.utils.clsRobustProgressDialog;
import com.treeapps.syllablestrainer.utils.clsUtils;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

public class ActivityStartup extends Activity {
	
	private static final String TAG = "Trainer";

	enum enumWordType {
		REAL, NONSENSE
	}

	class clsSession {
		public String strDisplaySyllable1;
		public String strDisplaySyllable2;
		public String strDisplaySyllable3;
		public String strDisplaySyllable4;
		public boolean boolIsSyllable1Checked;
		public boolean boolIsSyllable2Checked;
		public boolean boolIsSyllable3Checked;
		public boolean boolIsSyllable4Checked;
		public enumWordType enumWordTypes;
		public boolean boolIsSyllablesDisplayColoured;
		public int intAmountOfSyllablesToDisplay;
		public int intPromptTextSizeDp;
		public int intTestAmount;
	}

	private static final int EDIT_REAL_WORDS = 1;
	private static final int EDIT_PRE_SYLLABLES = 2;
	private static final int EDIT_ROOT_SYLLABLES = 3;
	private static final int EDIT_POST_SYLLABLES = 4;
	private static final int FILE_CHOOSER = 5;
	
	private static final int PROMPT_MAX_TEXT_SIZE = 100;
	private static final int PROMPT_MIN_TEXT_SIZE = 0;
	private static final int PROMPT_DEFAULT_TEXT_SIZE = 20;
	
	private static final int SYLLABLES_DEFAULT_DISPLAY_AMOUNT = 3;
	
	// In app billing
	public static class clsIabLocalData {
		public boolean boolIsAdsDisabled;
		public float fltDiskQuota;
	}
	static final String base64EncodedPublicKeyPart = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAjfMlu5NTHfIqk5+gQzSCCubC7r7MBmhktUKpK7Kefa7yxE9FeY/zNvcoe35maC+jrBx4dNkK54gdjoEgr7g7gPm8YH0HJCrHiWnWOqOeI8QoDQHLKv7UVFdPNKWzEZf6ILoaGXirpgyUoiVrTrk1MuMLANFZKIYkO7VJJrj9XdwyqO0ATegysZVCFf9xG01BdP2iWzCpirCu1VKFZ/yLvhd9FrOIS6x3umEZStE4rIv//+zTp7r7MiwIHd5YtU/VzAg7QVlGLbY1P9BuhiSs7wlKJiP7LuJGh1cURif90lWm447nIBBI8kLlGrLnr+D900TiJWxLQlc0MlZkuz+E+QIDAQAB";
	public static final String TAG_IAB = "IAB";
	public static final String SKU_UPGRADE_TO_PRO = "treeapps.treenote.upgrade_to_pro";
																							// OR
																							// android.test.purchased
																							// OR
																							// android.test.canceled
																							// OR
																							// android.test.refunded
																							// android.test.item_unavailable
	static final int RC_REQUEST = 10001; // (arbitrary) request code for the purchase flow
																				

	
	IabHelper mHelper;
	static clsRobustProgressDialog	objIabProgressDialog; 
	static String objMaxSyllablesAmountSpinnerRealWordsArray[] = {"1","2","3","4"};
	static String objMaxSyllablesAmountSpinnerNonsenseWordsArray[] = {"1","2","3"};

	// End of In app billing
	
	AdView adView;
	
	// Persist
	Context objContext;
	clsSession objSession = new clsSession();
	clsWordsHelper objWordsHelper = new clsWordsHelper();
	
	// End of persist

	RetainedFragment objRetainedFragment; // Used to persist objects that
											// implement interfaces (callback's)
											// used during orientation changes
	
	ImportDataFromCsvFileAsyncTask<clsWord> objImportWordDataFromCsvFileAsyncTask;
	ImportDataFromCsvFileAsyncTask<clsSyllable> objImportSyllableDataFromCsvFileAsyncTask;
	
	boolean boolIsPromptTextSizeSeekbarOnProgressChangedEventEnabled = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_startup);

		objContext = this;

		// Setup persistent storage to persistent event handlers
		FragmentManager fragmentManager = getFragmentManager();
		objRetainedFragment = (RetainedFragment) fragmentManager.findFragmentByTag("DATA");
		if (objRetainedFragment == null) {
			objRetainedFragment = new RetainedFragment();
			fragmentManager.beginTransaction().add(objRetainedFragment, "DATA").commit();
		}

		// Special for Samsung phones
		EnableOverflowMenu();
		
		// Setup display properties that does not require app data
		
		// Display click listener
		final TextView objDisplayView = (TextView) this.findViewById(R.id.display);
		objDisplayView.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				int intWordWidth = v.getWidth();
				float fltTouchWidth = event.getX();
				int intTouchedSyllable = CalculateTouchedSyllable(intWordWidth, fltTouchWidth);
				HiliteSingleSyllable(objDisplayView, intTouchedSyllable);
				return false;
			}
		});

		// OnClick listeners
		Button objSyllable1Button = (Button) findViewById(R.id.syllable1_button_next);
		objSyllable1Button.setOnClickListener(new MyOnClickListener());
		Button objSyllable2Button = (Button) findViewById(R.id.syllable2_button_next);
		objSyllable2Button.setOnClickListener(new MyOnClickListener());
		Button objSyllable3Button = (Button) findViewById(R.id.syllable3_button_next);
		objSyllable3Button.setOnClickListener(new MyOnClickListener());
		Button objSyllable4Button = (Button) findViewById(R.id.syllable4_button_next);
		objSyllable4Button.setOnClickListener(new MyOnClickListener());

		Button objNextWordButton = (Button) findViewById(R.id.button_next_word);
		objNextWordButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String strWordId = objWordsHelper.GetRandomWordId();
				UpdateSessionDataWithNewRealWord(strWordId);
				objSession.intTestAmount += 1;
				SaveState();
				UpdateGui(objSession);
			}
		});

		RadioButton objRealWordRadioButton = (android.widget.RadioButton) this.findViewById(R.id.radio_real_words);
		RadioButton objNonsenseWordRadioButton = (android.widget.RadioButton) this
				.findViewById(R.id.radio_nonsense_words);
		objRealWordRadioButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (((RadioButton) v).isChecked()) {
					objSession.enumWordTypes = enumWordType.REAL;
					String strWordId = objWordsHelper.GetRandomWordId();
					UpdateSessionDataWithNewRealWord(strWordId);
					UpdateGui(objSession);
				} else {
					objSession.enumWordTypes = enumWordType.NONSENSE;
					UpdateSessionDataWithNewNonsenseWord();
					UpdateGui(objSession);
				}
				SaveState();
				UpdateGui(objSession);
			}
		});
		


		objNonsenseWordRadioButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (((RadioButton) v).isChecked()) {
					objSession.enumWordTypes = enumWordType.NONSENSE;
					UpdateSessionDataWithNewNonsenseWord();					
					UpdateGui(objSession);
				} else {
					objSession.enumWordTypes = enumWordType.REAL;
					String strWordId = objWordsHelper.GetRandomWordId();
					UpdateSessionDataWithNewRealWord(strWordId);
					UpdateGui(objSession);
				}
				SaveState();
				UpdateGui(objSession);
			}
		});

		RelativeLayout objTestAmountLayout = (RelativeLayout) this.findViewById(R.id.layout_test_amount);
		objTestAmountLayout.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				clsUtils.OkCancelDialog(objContext, "Are you sure you want to clear the counter?", new clsUtils.OnOkCancelDialogCompleteListener() {				
					@Override
					public void OnComplete(boolean boolIsOk) {
						if (boolIsOk) {
							objSession.intTestAmount = 0;
							SaveState();
							UpdateGui(objSession);
						}						
					}
				});				
			}
		});
		
		CheckBox objShowColoursCheckboxView = (CheckBox) this.findViewById(R.id.checkbox_show_colours);
		objShowColoursCheckboxView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				objSession.boolIsSyllablesDisplayColoured = ((CheckBox) v).isChecked();
				SaveState();
				UpdateGui(objSession);
			}
		});

		Spinner objMaxSyllablesAmountSpinner = (Spinner) findViewById(R.id.spinner_syllables_amount);
		ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, android.R.id.text1);
		spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		objMaxSyllablesAmountSpinner.setAdapter(spinnerAdapter);
				
		objMaxSyllablesAmountSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				if (objSession != null) {
					// Change selection
					objSession.intAmountOfSyllablesToDisplay = position + 1;
					objWordsHelper.UpdateWordSyllableRestrictedAndEnables(objSession.intAmountOfSyllablesToDisplay);
					SaveState();
					UpdateGui(objSession);
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				// Do nothing
			}
		});
		
		SeekBar objPromptTextSizeSeekbar = (SeekBar) findViewById(R.id.seekbar_prompt_text_size);
		objPromptTextSizeSeekbar.setMax(PROMPT_MAX_TEXT_SIZE);
		objPromptTextSizeSeekbar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// Do nothing			
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// Do nothing				
			}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				if (boolIsPromptTextSizeSeekbarOnProgressChangedEventEnabled) {
					// Change text size				
					float floatNextTextSize = GetTextSizeFromSeekbarProgress(progress);
					objSession.intPromptTextSizeDp = (int) floatNextTextSize;
					SaveState();
					UpdateGui(objSession);
				}
			}

		});
		
		// AdMob, only when advert removal has not been purchased
		CheckIfUpgradedToPro(objContext, new OnCheckIfUpgradedToProCompletedListener() {
			
			@Override
			public void OnComplete(boolean boolIsSuccess, String strErrorMessage, boolean boolIsUpgraded) {
				if (boolIsSuccess) {
					if (boolIsUpgraded) {
						UnDisplayAd();
					} else {
						DisplayAd();
					}					
				} else {
					DisplayAd();
					clsUtils.MessageBox(objContext, "Info", true, "Could not determine inventory status. " + strErrorMessage,
							false, null);
				}				
			}
		});
		
		
		
		// Prepare app data and update display
		if (savedInstanceState == null) {
			// Running from launcher
			try {
				// Action: Make sure all data file resources are in place
				SetupResources(new OnSetupResourcesCompleteListener() {
					
					@Override
					public void OnComplete(boolean boolIsSuccess, String strErrorMessage) {
						if (boolIsSuccess) {
							// Action: Load application data
							LoadState();
							if (objSession == null) {
								// Running for first time ever								
								// Load repository
								try {
									objWordsHelper.LoadRepository(objContext);
								} catch (Exception e) {
									clsUtils.MessageBox(objContext, "Error", true, e.getMessage(), false, null);
								}
								
								// Initialize state
								objSession = new clsSession();
								objSession.intAmountOfSyllablesToDisplay = SYLLABLES_DEFAULT_DISPLAY_AMOUNT;
								objSession.boolIsSyllable1Checked = true;
								objSession.boolIsSyllable2Checked = true;
								objSession.boolIsSyllable3Checked = true;
								objSession.boolIsSyllable4Checked = false;
								objSession.enumWordTypes = enumWordType.REAL;
								objSession.boolIsSyllablesDisplayColoured = true;
								objSession.intPromptTextSizeDp = PROMPT_DEFAULT_TEXT_SIZE;
								objSession.intTestAmount = 0;
								String strWordId = objWordsHelper.GetRandomWordId();
								UpdateSessionDataWithNewRealWord(strWordId);
								
								// Save State
								SaveState();
							}
							// Setup display
							UpdateGui(objSession);
						} else {
							clsUtils.MessageBox(objContext, "Error", true, strErrorMessage, false, null);
						}						
					}
				});	
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				clsUtils.MessageBox(this, "Error", true, "Could not load database at startup. " + e.getMessage(),
						false, null);
			}
		} else {
			LoadState();
			UpdateGui(objSession);
		}
			


	}
	
	interface OnCheckIfUpgradedToProCompletedListener {
		public void OnComplete (boolean boolIsSuccess, String strErrorMessage, boolean boolIsUpgraded);
	}

	public static class clsPurchaseState {
		public boolean boolIsUpgradedToPro;
	}
	private void CheckIfUpgradedToPro(final Context objContext, final OnCheckIfUpgradedToProCompletedListener OnCheckIfUpgradedToProCompletedListener) { 
		// Action: Get result from local preference
		clsPurchaseState objPurchaseState = CheckIfUpgradedToProFromLocalPreference(objContext);
		if (objPurchaseState != null) {
			OnCheckIfUpgradedToProCompletedListener.OnComplete(true, "", objPurchaseState.boolIsUpgradedToPro);
		} else {
			// Action: Does not exists locally, so check inventory online
			if (mHelper == null) {
				mHelper = new IabHelper(this, base64EncodedPublicKeyPart);
				// Action: Ensure helper exists
				mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
					public void onIabSetupFinished(IabResult result) {
						if (result.isFailure()) {
							OnCheckIfUpgradedToProCompletedListener.OnComplete(false, result.toString(),false);
							return;
						} else {
							// Action: Query inventory
							mHelper.queryInventoryAsync(new QueryInventoryFinishedListener() {
								
								@Override
								public void onQueryInventoryFinished(IabResult result, Inventory inv) {
									if (result.isSuccess()){
										if (inv.hasPurchase(ActivityStartup.SKU_UPGRADE_TO_PRO)) {
											// Action: Return result and ensure there is a local copy
											SetUpgradedToProInLocalPreference(objContext, true);
											OnCheckIfUpgradedToProCompletedListener.OnComplete(true, "",true);
										}	else {
											SetUpgradedToProInLocalPreference(objContext, false);
											OnCheckIfUpgradedToProCompletedListener.OnComplete(true, "",false);
										}
									} else {
										SetUpgradedToProInLocalPreference(objContext, false);
										OnCheckIfUpgradedToProCompletedListener.OnComplete(false, result.toString(),false);
										return;
									}								
								}
							});
						}
					}
				});
			}
			
		}	
	}



	public static clsPurchaseState CheckIfUpgradedToProFromLocalPreference(Context objContext) {
		try {
			String strPurchaseStateEncrypted = clsUtils.DeSerializeFromSharedPreferences("ActivityStartup", "Inventory", objContext, String.class);
			if (TextUtils.isEmpty(strPurchaseStateEncrypted)) {
				return null;
			}
			String strPurchaseStateDecrypted = Decrypt(strPurchaseStateEncrypted);
			clsPurchaseState obPurchaseState = clsUtils.DeSerializeFromString(strPurchaseStateDecrypted, clsPurchaseState.class);
			return obPurchaseState;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	

	private  void SetUpgradedToProInLocalPreference(Context objContext, boolean boolIsUpgraded) {
		clsPurchaseState obPurchaseState = new clsPurchaseState();
		obPurchaseState.boolIsUpgradedToPro = boolIsUpgraded;
		String strPurchaseStateDecrypted = clsUtils.SerializeToString(obPurchaseState);
		String strPurchaseStateEncrypted = Encrypt(strPurchaseStateDecrypted);
		clsUtils.SerializeToSharedPreferences("ActivityStartup", "Inventory", objContext, strPurchaseStateEncrypted);
	}
	
	private  void ClearUpgradedToProInLocalPreference(Context objContext) {
		clsUtils.SerializeToSharedPreferences("ActivityStartup", "Inventory", objContext, null);
	}


	private static String Decrypt(String strPurchaseStateEncrypted) {
		clsMCrypt objMCrypt = new clsMCrypt();
		String strEncrypted;
		try {
			strEncrypted = new String( objMCrypt.decrypt( strPurchaseStateEncrypted ));
			return strEncrypted;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	private static String Encrypt(String strPurchaseStateDecrypted) {
		clsMCrypt objMCrypt = new clsMCrypt();
		try {
			String strEncryptedResult = clsMCrypt.bytesToHex(objMCrypt.encrypt(strPurchaseStateDecrypted) );
			return strEncryptedResult;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	public void UnDisplayAd() {
		RelativeLayout objRelativeLayout = (RelativeLayout) findViewById(R.id.layout_startup);
		View objAddView = (View) findViewById(R.id.google_ad_view);
		objRelativeLayout.removeView(objAddView);
	}

	public void DisplayAd() {
		// Look up the AdView as a resource and load a request.
		adView = (AdView) this.findViewById(R.id.google_ad_view);
		AdRequest adRequest = new AdRequest.Builder().addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
				.addTestDevice("803D489BC46137FD0761EC7EBFBBFB09")
				.addTestDevice("C1B978D9FE1B0A6A8A58F1F44F653BE3")
				.addTestDevice("FB01AE66402BBCBA23816BED978B7D93")
				.addTestDevice("893617FB0C72A211F504BCEDEA9D3EEA")
				.addTestDevice("84144DEC93984DE4D04B37D684E93832")
				.addTestDevice("A947D095EE036142160FD3D2D4D5034C").build();
		adView.loadAd(adRequest);
	}


	public float GetTextSizeFromSeekbarProgress(int intProgress) {
		float fltResult = (float) (((PROMPT_MAX_TEXT_SIZE - PROMPT_MIN_TEXT_SIZE)/((float)PROMPT_MAX_TEXT_SIZE) * intProgress) + PROMPT_MIN_TEXT_SIZE);
		return fltResult;
	}
	
	public int CalcSeekbarProgressFromTextSize(int intTextSizeInDp) {
		if (intTextSizeInDp == 0) {
			intTextSizeInDp = PROMPT_MIN_TEXT_SIZE;
		}
		float fltResult = (intTextSizeInDp  - PROMPT_MIN_TEXT_SIZE)/((float)((PROMPT_MAX_TEXT_SIZE - PROMPT_MIN_TEXT_SIZE)));
		return (int) (PROMPT_MAX_TEXT_SIZE * fltResult);
	}

	private void EnableOverflowMenu() {
		// Hack to get to overflow action to display if there is a physical menu
		// key
		try {
			ViewConfiguration config = ViewConfiguration.get(this);
			Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
			if (menuKeyField != null) {
				menuKeyField.setAccessible(true);
				menuKeyField.setBoolean(config, false);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void UpdateSessionDataWithNewRealWord(String strWordId) {
		if (!TextUtils.isEmpty(strWordId)) {
			clsWord objWord = objWordsHelper.GetWordFromId(strWordId);
			if (objWord != null) {
				objSession.strDisplaySyllable1 = objWord.GetSyllable(0);
				objSession.strDisplaySyllable2 = objWord.GetSyllable(1);
				objSession.strDisplaySyllable3 = objWord.GetSyllable(2);
				objSession.strDisplaySyllable4 = objWord.GetSyllable(3);
			}

		}
		SaveState();
	}
	
	public void UpdateSessionDataWithNewNonsenseWord() {
		objSession.strDisplaySyllable4 = "";
		objSession.strDisplaySyllable3 = clsWordsHelper.GetRandomSyllable(objWordsHelper.objFilteredPostSyllables);
		objSession.strDisplaySyllable2 = clsWordsHelper.GetRandomSyllable(objWordsHelper.objFilteredRootSyllables);
		objSession.strDisplaySyllable1 = clsWordsHelper.GetRandomSyllable(objWordsHelper.objFilteredPreSyllables);	
		// Ensure there is only max of 3 syllables when doing nonsense words
		if (objSession.intAmountOfSyllablesToDisplay == 4) {
			objSession.intAmountOfSyllablesToDisplay = 3;
			SaveState();
		}
		SaveState();
	}
	
	public void UpdateSessionDataWithEmptyWord() {
		objSession.strDisplaySyllable1 = "";
		objSession.strDisplaySyllable2 = "";
		objSession.strDisplaySyllable3 = "";
		objSession.strDisplaySyllable4 = "";
		SaveState();
	}
	
	public interface OnSetupResourcesCompleteListener {
		public void OnComplete(boolean boolIsSuccess, String strErrorMessage);
	}

	private void SetupResources(final OnSetupResourcesCompleteListener OnSetupResourcesCompleteListener) {
		try {
			// Place default word list in app folder
			File objImportWordFile = clsUtils.GetImportWordFile(this);
			if (!objImportWordFile.exists()) {
				ExtractRawFile(objImportWordFile, R.raw.words);
			}
			// Place default syllable lists in app folder
			final File objPreSyllablesFile = clsUtils.GetImportPreSyllableFile(this);
			if (!objPreSyllablesFile.exists()) {
				ExtractRawFile(objPreSyllablesFile, R.raw.presylls);
			}

			// Place default syllable lists in app folder
			final File objRootSyllablesFile = clsUtils.GetImportRootSyllableFile(this);
			if (!objRootSyllablesFile.exists()) {
				ExtractRawFile(objRootSyllablesFile, R.raw.rootsylls);
			}

			// Place default syllable lists in app folder
			final File objPostSyllablesFile = clsUtils.GetImportPostSyllableFile(this);
			if (!objPostSyllablesFile.exists()) {
				ExtractRawFile(objPostSyllablesFile, R.raw.postsylls);
			}

			// If no repository folder, create prefilled one
			File objRepositoryFile = clsUtils.GetRepositoryFile(this);
			if (!objRepositoryFile.exists()) {
				objWordsHelper.objRepository = objWordsHelper.new clsRepository();
				objImportWordDataFromCsvFileAsyncTask = new ImportDataFromCsvFileAsyncTask<clsWord>(objContext,
						objImportWordFile, new OnImportDataFromCsvFileCompleteListener<clsWord>() {

							@Override
							public void OnComplete(boolean boolIsSuccess, String strErrorMessage,
									ArrayList<clsWord> objResult) {
								if (boolIsSuccess) {
									try {
										// Update
										objWordsHelper.objRepository.objWords = objResult;
										
										// Import smaller files, no need for async operation
										objWordsHelper.objRepository.objPreSyllables = (ArrayList<clsSyllable>) clsWordsHelper
												.ImportSyllableDataFromCsvFile(objPreSyllablesFile);
										objWordsHelper.objRepository.objRootSyllables = (ArrayList<clsSyllable>) clsWordsHelper
												.ImportSyllableDataFromCsvFile(objRootSyllablesFile);
										objWordsHelper.objRepository.objPostSyllables = (ArrayList<clsSyllable>) clsWordsHelper
												.ImportSyllableDataFromCsvFile(objPostSyllablesFile);
										objWordsHelper.SaveRepository(objContext);
										OnSetupResourcesCompleteListener.OnComplete(true,"");
									} catch (Exception e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
										OnSetupResourcesCompleteListener.OnComplete(false, "Could not import all syllable files. " + e.getMessage());
									}
								} else {
									OnSetupResourcesCompleteListener.OnComplete(false, "Could not import words file. " + strErrorMessage);
								}
							}
						}, "Loading words...", objWordsHelper.ImportWordDataFromCsvFileMethod);
				objImportWordDataFromCsvFileAsyncTask.execute(null, null, null);
			} else {
				OnSetupResourcesCompleteListener.OnComplete(true,"");
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			OnSetupResourcesCompleteListener.OnComplete(false, "Unable to setup resources. " + e.getMessage());
		}

	}

	public void ExtractRawFile(File objDestFile, int intRawResourceId) throws Exception {
		// Write InputStream to file
		InputStream objImportWordListInputStream = getResources().openRawResource(intRawResourceId);
		final OutputStream output = new FileOutputStream(objDestFile);
		try {

			final byte[] buffer = new byte[1024];
			int read;

			while ((read = objImportWordListInputStream.read(buffer)) != -1)
				output.write(buffer, 0, read);
			output.flush();
		} finally {
			output.close();
			objImportWordListInputStream.close();
		}
	}

	void UpdateGui(clsSession objSession) {
		
		TextView objDisplayView = (TextView) this.findViewById(R.id.display);

		RelativeLayout objSyllableButtonsView = (RelativeLayout) this.findViewById(R.id.layout_syllable_buttons);
		RelativeLayout objSyllable1ButtonView = (RelativeLayout) this.findViewById(R.id.layout_syllable1_button);
		RelativeLayout objSyllable2ButtonView = (RelativeLayout) this.findViewById(R.id.layout_syllable2_button);
		RelativeLayout objSyllable3ButtonView = (RelativeLayout) this.findViewById(R.id.layout_syllable3_button);
		RelativeLayout objSyllable4ButtonView = (RelativeLayout) this.findViewById(R.id.layout_syllable4_button);

		Button objNextWordButton = (Button) this.findViewById(R.id.button_next_word);
		RadioButton objRealWordRadioButton = (android.widget.RadioButton) this.findViewById(R.id.radio_real_words);
		RadioButton objNonsenseWordRadioButton = (android.widget.RadioButton) this
				.findViewById(R.id.radio_nonsense_words);
		CheckBox objShowColoursCheckboxView = (CheckBox) this.findViewById(R.id.checkbox_show_colours);
		Spinner objMaxSyllablesAmountSpinner = (Spinner) findViewById(R.id.spinner_syllables_amount);

		// Syllables check boxes - makes use of switch fall-through principle
		objSyllableButtonsView.setVisibility(View.GONE);
		objSyllable1ButtonView.setVisibility(View.GONE);
		objSyllable2ButtonView.setVisibility(View.GONE);
		objSyllable3ButtonView.setVisibility(View.GONE);
		objSyllable4ButtonView.setVisibility(View.GONE);
		switch (objSession.intAmountOfSyllablesToDisplay) {
		case 4:
			objSyllable4ButtonView.setVisibility(View.VISIBLE);
		case 3:
			objSyllable3ButtonView.setVisibility(View.VISIBLE);
		case 2:
			objSyllable2ButtonView.setVisibility(View.VISIBLE);
		case 1:
			objSyllable1ButtonView.setVisibility(View.VISIBLE);
		}

		// Colors checkbox
		objShowColoursCheckboxView.setChecked(objSession.boolIsSyllablesDisplayColoured);

		// Syllables amount
		if (objSession.enumWordTypes == enumWordType.REAL) {
			@SuppressWarnings("unchecked")
			ArrayAdapter<String> adapter  = (ArrayAdapter<String>) objMaxSyllablesAmountSpinner.getAdapter();
			adapter.clear();
			adapter.addAll(objMaxSyllablesAmountSpinnerRealWordsArray);
		} else {
			@SuppressWarnings("unchecked")
			ArrayAdapter<String> adapter  = (ArrayAdapter<String>) objMaxSyllablesAmountSpinner.getAdapter();
			adapter.clear();
			adapter.addAll(objMaxSyllablesAmountSpinnerNonsenseWordsArray);
		}		
		objMaxSyllablesAmountSpinner.setSelection(objSession.intAmountOfSyllablesToDisplay - 1);

		if (objSession.enumWordTypes == enumWordType.REAL) {
			// Radio buttons
			objRealWordRadioButton.setChecked(true);
			objNonsenseWordRadioButton.setChecked(false);
			// Next word buttons
			objNextWordButton.setVisibility(View.VISIBLE);
			objSyllableButtonsView.setVisibility(View.VISIBLE);
		} else {
			// Radio buttons
			objRealWordRadioButton.setChecked(false);
			objNonsenseWordRadioButton.setChecked(true);
			// Next word buttons
			objNextWordButton.setVisibility(View.INVISIBLE);
			objSyllableButtonsView.setVisibility(View.VISIBLE);
		}

		if (objSession.boolIsSyllablesDisplayColoured) {
			// Colored
			DisplayAllColoured(objDisplayView);

		} else {
			DisplayAllNonColoured(objDisplayView);
		}
		
		// Prompt sizing to always fit
		objDisplayView.setTextSize(objSession.intPromptTextSizeDp);		
		if (IsPromptWrapping(objDisplayView, GetDisplayString())) {
			int intMaxTextSizeDp = GetBestFitTextSizeInDp(objDisplayView, GetDisplayString());
			Log.d(TAG,"Limit reached: objSession.intPromptTextSizeDp = " + objSession.intPromptTextSizeDp + ", intMaxTextSizeDp = " +intMaxTextSizeDp);
			objSession.intPromptTextSizeDp = intMaxTextSizeDp;
			SaveState();
			objDisplayView.setTextSize(objSession.intPromptTextSizeDp);
		}
				
		boolIsPromptTextSizeSeekbarOnProgressChangedEventEnabled = false;
		SeekBar objPromptTextSizeSeekbar = (SeekBar) findViewById(R.id.seekbar_prompt_text_size);
		int intProgress = CalcSeekbarProgressFromTextSize(objSession.intPromptTextSizeDp);
		objPromptTextSizeSeekbar.setProgress(intProgress);
		boolIsPromptTextSizeSeekbarOnProgressChangedEventEnabled = true;
		
		UpdateTestAmountDisplay(objSession);
				
		invalidateOptionsMenu();
	}



	public void UpdateTestAmountDisplay(clsSession objSession) {
		TextView objTestAmountTextView = (TextView) this.findViewById(R.id.label_test_amount);
		clsColoredStringBuilder objColoredStringBuilder = new clsColoredStringBuilder();
		objColoredStringBuilder.AddSnippetToString("Count: ", Color.BLUE);
		objColoredStringBuilder.AddSnippetToString(Integer.toString(objSession.intTestAmount), Color.BLACK);
		objTestAmountTextView.setText(objColoredStringBuilder.GetString());
	}
		

	public boolean IsPromptWrapping(TextView objDisplayView, String strPrompt) {
		// Determine maximum allowable width
		RelativeLayout objParentView = (RelativeLayout) objDisplayView.getParent();
		int intMaxWidthPx = objParentView.getWidth();
		
		Paint textPaint = objDisplayView.getPaint();
		int intPromptWidthPx =  (int) textPaint.measureText(strPrompt, 0, strPrompt.length());
		
		Log.d(TAG, "intMaxWidthPx = " + intMaxWidthPx + " , intPromptWidthPx = " + intPromptWidthPx);
		if (intPromptWidthPx >= intMaxWidthPx)  return true;
		return false;
	}
	
	private int GetBestFitTextSizeInDp(TextView objDisplayView, String strPrompt) {
		int intOriginalTextSizeDp = clsUtils.PxToDp(objContext, (int) objDisplayView.getTextSize());
		int intFittedTextSizeDp = intOriginalTextSizeDp;
		
		// Determine maximum allowable width
		RelativeLayout objParentView = (RelativeLayout) objDisplayView.getParent();
		int intMaxWidthPx = objParentView.getWidth();
		
		if (intMaxWidthPx == 0) return objSession.intPromptTextSizeDp;
		
		// Iterate until fitted	
		Paint textPaint = objDisplayView.getPaint();
		objDisplayView.setTextSize(intFittedTextSizeDp);
		int intPromptWidthPx = (int) textPaint.measureText(strPrompt, 0, strPrompt.length());
		while (intPromptWidthPx > intMaxWidthPx)
	    {
			intFittedTextSizeDp--;
			objDisplayView.setTextSize(intFittedTextSizeDp);
			intPromptWidthPx = (int) textPaint.measureText(strPrompt, 0, strPrompt.length());
	    }
		objDisplayView.setTextSize(intOriginalTextSizeDp);
		return intFittedTextSizeDp - 1;		
	}
	

	protected String GetDisplayString() { 
		String strDisplay = "";
		int intSyllableCount = 1;
		if ((!TextUtils.isEmpty(objSession.strDisplaySyllable1)) && (intSyllableCount <= objSession.intAmountOfSyllablesToDisplay)) {
			strDisplay += objSession.strDisplaySyllable1;
		} else {
			return strDisplay;
		}
		intSyllableCount += 1;
		if ((!TextUtils.isEmpty(objSession.strDisplaySyllable2)) && (intSyllableCount <= objSession.intAmountOfSyllablesToDisplay)) {
			strDisplay += objSession.strDisplaySyllable2;
		} else {
			return strDisplay;
		}
		intSyllableCount += 1;
		if ((!TextUtils.isEmpty(objSession.strDisplaySyllable3)) && (intSyllableCount <= objSession.intAmountOfSyllablesToDisplay)) {
			strDisplay += objSession.strDisplaySyllable3;
		} else {
			return strDisplay;
		}
		intSyllableCount += 1;
		if ((!TextUtils.isEmpty(objSession.strDisplaySyllable4)) && (intSyllableCount <= objSession.intAmountOfSyllablesToDisplay)) {
			strDisplay += objSession.strDisplaySyllable4;
		} else {
			return strDisplay;
		}
		return strDisplay;
	}
	
	private void DisplayAllNonColoured(TextView objDisplayView) {
		String strDisplay = GetDisplayString();
		objDisplayView.setText(strDisplay);
	}

	private SpannableStringBuilder DisplayAllColoured(TextView objDisplayView) {
		clsColoredStringBuilder objColoredStringBuilder = new clsColoredStringBuilder();
		int intSyllableCount = 1;
		if ((!TextUtils.isEmpty(objSession.strDisplaySyllable1)) && (intSyllableCount <= objSession.intAmountOfSyllablesToDisplay)) {
			objColoredStringBuilder.AddSnippetToString(objSession.strDisplaySyllable1, Color.rgb(255, 0, 0));
		} else {
			objDisplayView.setText(objColoredStringBuilder.GetString());
			return objColoredStringBuilder.GetString();
		}
		intSyllableCount += 1;
		if ((!TextUtils.isEmpty(objSession.strDisplaySyllable2)) && (intSyllableCount <= objSession.intAmountOfSyllablesToDisplay)) {
			objColoredStringBuilder.AddSnippetToString(objSession.strDisplaySyllable2, Color.rgb(45, 110, 100));
		} else {
			objDisplayView.setText(objColoredStringBuilder.GetString());
			return objColoredStringBuilder.GetString();
		}
		intSyllableCount += 1;
		if ((!TextUtils.isEmpty(objSession.strDisplaySyllable3)) && (intSyllableCount <= objSession.intAmountOfSyllablesToDisplay)) {
			objColoredStringBuilder.AddSnippetToString(objSession.strDisplaySyllable3, Color.rgb(0, 0, 255));
		} else {
			objDisplayView.setText(objColoredStringBuilder.GetString());
			return objColoredStringBuilder.GetString();
		}
		intSyllableCount += 1;
		if ((!TextUtils.isEmpty(objSession.strDisplaySyllable4)) && (intSyllableCount <= objSession.intAmountOfSyllablesToDisplay)) {
			objColoredStringBuilder.AddSnippetToString(objSession.strDisplaySyllable4, Color.rgb(180, 160, 40));
		} else {
			objDisplayView.setText(objColoredStringBuilder.GetString());
			return objColoredStringBuilder.GetString();
		}
		objDisplayView.setText(objColoredStringBuilder.GetString());
		return objColoredStringBuilder.GetString();
	}

	protected void HiliteSingleSyllable(TextView objDisplayView, int intTouchedSyllable) {
		clsColoredStringBuilder objColoredStringBuilder = new clsColoredStringBuilder();
		int intSyllableCount = 1;
		if ((!TextUtils.isEmpty(objSession.strDisplaySyllable1)) && (intSyllableCount <= objSession.intAmountOfSyllablesToDisplay)) {
			if (intTouchedSyllable == 0) {
				objColoredStringBuilder.AddSnippetToString(objSession.strDisplaySyllable1, Color.RED);
			} else {
				objColoredStringBuilder.AddSnippetToString(objSession.strDisplaySyllable1, Color.BLACK);
			}
		} else {
			objDisplayView.setText(objColoredStringBuilder.GetString());
			return;
		}
		intSyllableCount += 1;
		if ((!TextUtils.isEmpty(objSession.strDisplaySyllable2)) && (intSyllableCount <= objSession.intAmountOfSyllablesToDisplay)) {
			if (intTouchedSyllable == 1) {
				objColoredStringBuilder.AddSnippetToString(objSession.strDisplaySyllable2, Color.RED);
			} else {
				objColoredStringBuilder.AddSnippetToString(objSession.strDisplaySyllable2, Color.BLACK);
			}
		} else {
			objDisplayView.setText(objColoredStringBuilder.GetString());
			return;
		}
		intSyllableCount += 1;
		if ((!TextUtils.isEmpty(objSession.strDisplaySyllable3)) && (intSyllableCount <= objSession.intAmountOfSyllablesToDisplay)) {
			if (intTouchedSyllable == 2) {
				objColoredStringBuilder.AddSnippetToString(objSession.strDisplaySyllable3, Color.RED);
			} else {
				objColoredStringBuilder.AddSnippetToString(objSession.strDisplaySyllable3, Color.BLACK);
			}
		} else {
			objDisplayView.setText(objColoredStringBuilder.GetString());
			return;
		}
		intSyllableCount += 1;
		if ((!TextUtils.isEmpty(objSession.strDisplaySyllable4)) && (intSyllableCount <= objSession.intAmountOfSyllablesToDisplay)) {
			if (intTouchedSyllable == 3) {
				objColoredStringBuilder.AddSnippetToString(objSession.strDisplaySyllable4, Color.RED);
			} else {
				objColoredStringBuilder.AddSnippetToString(objSession.strDisplaySyllable4, Color.BLACK);
			}
		} else {
			objDisplayView.setText(objColoredStringBuilder.GetString());
			return;
		}
		objDisplayView.setText(objColoredStringBuilder.GetString());

	}

	protected int CalculateTouchedSyllable(int intWordWidth, float fltTouchWidth) {
		// Determine length of displayed word
		int intCharacterCount = 0;
		if (!TextUtils.isEmpty(objSession.strDisplaySyllable1)) {
			intCharacterCount += objSession.strDisplaySyllable1.length();

			if (!TextUtils.isEmpty(objSession.strDisplaySyllable2)) {
				intCharacterCount += objSession.strDisplaySyllable2.length();

				if (!TextUtils.isEmpty(objSession.strDisplaySyllable3)) {
					intCharacterCount += objSession.strDisplaySyllable3.length();

					if (!TextUtils.isEmpty(objSession.strDisplaySyllable4)) {
						intCharacterCount += objSession.strDisplaySyllable4.length();
					}
				}
			}
		}
		// Determine which syllable spans the touched point
		int intCurSyllStartPos = 0;
		int intCurSyllEndPos = 0;
		int intCharWidth = intWordWidth / intCharacterCount;

		if (!TextUtils.isEmpty(objSession.strDisplaySyllable1)) {
			intCurSyllStartPos = intCurSyllEndPos;
			intCurSyllEndPos = objSession.strDisplaySyllable1.length() * intCharWidth + intCurSyllStartPos;
			if (fltTouchWidth < intCurSyllEndPos) {
				return 0;
			}

			if (!TextUtils.isEmpty(objSession.strDisplaySyllable2)) {
				intCurSyllStartPos = intCurSyllEndPos;
				intCurSyllEndPos = objSession.strDisplaySyllable2.length() * intCharWidth + intCurSyllStartPos;
				if (fltTouchWidth < intCurSyllEndPos) {
					return 1;
				}

				if (!TextUtils.isEmpty(objSession.strDisplaySyllable3)) {
					intCurSyllStartPos = intCurSyllEndPos;
					intCurSyllEndPos = objSession.strDisplaySyllable3.length() * intCharWidth + intCurSyllStartPos;
					if (fltTouchWidth < intCurSyllEndPos) {
						return 2;
					}

					if (!TextUtils.isEmpty(objSession.strDisplaySyllable4)) {
						intCurSyllStartPos = intCurSyllEndPos;
						intCurSyllEndPos = objSession.strDisplaySyllable4.length() * intCharWidth + intCurSyllStartPos;
						if (fltTouchWidth < intCurSyllEndPos) {
							return 3;
						}
					}
				}
			}
		}
		return 0;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_startup, menu);
		
		// Get handles of dynamic menu items
		MenuItem objEdit = menu.findItem(R.id.action_startup_edit);
		MenuItem objImport = menu.findItem(R.id.action_startup_import);
		MenuItem objExport = menu.findItem(R.id.action_startup_export);
		MenuItem objBuyPro = menu.findItem(R.id.action_buy_pro_version);
		
		boolean boolIsUpgraded = false;
		clsPurchaseState objPurchaseState = CheckIfUpgradedToProFromLocalPreference(objContext);
		if (objPurchaseState != null) {
			if (objPurchaseState.boolIsUpgradedToPro) {
				boolIsUpgraded = true;
			}
		}
		if (boolIsUpgraded) {
			clsUtils.SetMenuItemEnabled(objEdit, true);
			clsUtils.SetMenuItemEnabled(objImport, true);
			clsUtils.SetMenuItemEnabled(objExport, true);
			objBuyPro.setVisible(false);
		} else {
			clsUtils.SetMenuItemEnabled(objEdit, false);
			clsUtils.SetMenuItemEnabled(objImport, false);
			clsUtils.SetMenuItemEnabled(objExport, false);
			objBuyPro.setVisible(true);
		}		
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		Intent objIntent;
		final int id = item.getItemId();

		switch (id) {
		case R.id.action_startup_import_real_words:

			ChooseFile(clsUtils.GetWordsImportFileType(), new OnChooseFileCompleteListener() {

				@Override
				public void OnComplete(boolean boolIsFileChosen, String strFilePath) {
					if (boolIsFileChosen) {
						File objImportFile = new File(strFilePath);
						if (objImportFile.exists()) {
							try {
								objImportWordDataFromCsvFileAsyncTask = new ImportDataFromCsvFileAsyncTask<clsWord>(
										objContext, new File(strFilePath),
										new OnImportDataFromCsvFileCompleteListener<clsWord>() {

											@Override
											public void OnComplete(boolean boolIsSuccess, String strErrorMessage,
													ArrayList<clsWord> objResult) {
												if (boolIsSuccess) {
													try {
														objWordsHelper.UpdateWords(objResult);
														objWordsHelper.SaveRepository(objContext);
														clsUtils.MessageBox(objContext, "Notice", false,
																"File successfully imported", true, null);
													} catch (Exception e) {
														// TODO Auto-generated
														// catch block
														e.printStackTrace();
														clsUtils.MessageBox(objContext, "Error", false,
																"Could not import file. " + e.getMessage(), false, null);
													}
												} else {
													clsUtils.MessageBox(objContext, "Error", false,
															"Could not import file. " + strErrorMessage, false, null);
												}
											}
										}, "Waiting ...", objWordsHelper.ImportWordDataFromCsvFileMethod);
								objImportWordDataFromCsvFileAsyncTask.execute(null, null, null);
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
								clsUtils.MessageBox(objContext, "Error", false,
										"Could not import file. " + e.getMessage(), false, null);
							}

						} else {
							clsUtils.MessageBox(objContext, "Notice", false, "Import file '" + objImportFile.getName()
									+ "' does not exist", false, null);
						}

					}

				}
			});

			break;
		case R.id.action_startup_import_presylls:
		case R.id.action_startup_import_rootsylls:
		case R.id.action_startup_import_postsylls:
			String strFiletype = "";
			switch (id) {
			case R.id.action_startup_import_presylls:
				strFiletype = clsUtils.GetPreSyllImportFileType();
				break;
			case R.id.action_startup_import_rootsylls:
				strFiletype = clsUtils.GetRootSyllImportFileType();
				break;
			case R.id.action_startup_import_postsylls:
				strFiletype = clsUtils.GetPostSyllImportFileType();
				break;
			}
			ChooseFile(strFiletype, new OnChooseFileCompleteListener() {

				@Override
				public void OnComplete(boolean boolIsFileChosen, String strFilePath) {
					if (boolIsFileChosen) {
						File objImportFile = new File(strFilePath);
						if (objImportFile.exists()) {
							try {
								objImportSyllableDataFromCsvFileAsyncTask = new ImportDataFromCsvFileAsyncTask<clsSyllable>(
										objContext, new File(strFilePath),
										new OnImportDataFromCsvFileCompleteListener<clsSyllable>() {

											@Override
											public void OnComplete(boolean boolIsSuccess, String strErrorMessage,
													ArrayList<clsSyllable> objResult) {
												if (boolIsSuccess) {
													try {
														switch (id) {
														case R.id.action_startup_import_presylls:
															objWordsHelper.UpdatePreSyllables(objResult);
															break;
														case R.id.action_startup_import_rootsylls:
															objWordsHelper.UpdateRootSyllables(objResult);
															break;
														case R.id.action_startup_import_postsylls:
															objWordsHelper.UpdatePostSyllables(objResult);
															break;
														}														
														objWordsHelper.SaveRepository(objContext);
														clsUtils.MessageBox(objContext, "Notice", false,
																"File successfully imported", true, null);
													} catch (Exception e) {
														// TODO Auto-generated
														// catch block
														e.printStackTrace();
														clsUtils.MessageBox(objContext, "Error", false,
																"Could not import file. " + e.getMessage(), false, null);
													}
												} else {
													clsUtils.MessageBox(objContext, "Error", false,
															"Could not import file. " + strErrorMessage, false, null);
												}
											}
										}, "Waiting ...", objWordsHelper.ImportSyllableDataFromCsvFileMethod);
								objImportSyllableDataFromCsvFileAsyncTask.execute(null, null, null);
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
								clsUtils.MessageBox(objContext, "Error", false,
										"Could not import file. " + e.getMessage(), false, null);
							}

						} else {
							clsUtils.MessageBox(objContext, "Notice", false, "Import file '" + objImportFile.getName()
									+ "' does not exist", false, null);
						}

					}

				}
			});

			break;
		
		case R.id.action_startup_edit_real_words:
			objIntent = new Intent(this, ActivityEditor.class);
			// Generate input data
			clsInputData objInputData = new clsInputData();
			for (clsWord objWord : objWordsHelper.objRepository.objWords) {
				clsEditorWord objEditorWord = new clsEditorWord();
				for (clsSyllable objSyllable : objWord.objSyllables) {
					clsEditorSyllable objEditorSyllable = new clsEditorSyllable(objSyllable.strValue);
					objEditorWord.objSyllables.add(objEditorSyllable);
				}
				objEditorWord.boolIsEnabled = objWord.boolIsEnabled;
				objInputData.objWords.add(objEditorWord);
			}
			String strInputData = clsUtils.SerializeToString(objInputData);
			objIntent.putExtra(ActivityEditor.INPUT_DATA, strInputData);
			objIntent.putExtra(ActivityEditor.INPUT_DATA_TYPE, ActivityEditor.INPUT_DATA_TYPE_REAL_WORDS);
			startActivityForResult(objIntent, EDIT_REAL_WORDS);
			break;
		case R.id.action_startup_edit_nonsense_words_presylls:
		case R.id.action_startup_edit_nonsense_words_rootsylls:
		case R.id.action_startup_edit_nonsense_words_postsylls:
			int intSharedResultCode = 0;
			ArrayList<clsSyllable> objSharedSyllables = new ArrayList<clsSyllable>();
			switch (id) {
			case R.id.action_startup_edit_nonsense_words_presylls:
				intSharedResultCode = EDIT_PRE_SYLLABLES;
				objSharedSyllables = objWordsHelper.objRepository.objPreSyllables;
				break;
			case R.id.action_startup_edit_nonsense_words_rootsylls:
				intSharedResultCode = EDIT_ROOT_SYLLABLES;
				objSharedSyllables = objWordsHelper.objRepository.objRootSyllables;
				break;
			case R.id.action_startup_edit_nonsense_words_postsylls:
				intSharedResultCode = EDIT_POST_SYLLABLES;
				objSharedSyllables = objWordsHelper.objRepository.objPostSyllables;
				break;
			}

			// Generate input data
			objInputData = new clsInputData();
			for (clsSyllable objSyllable : objSharedSyllables) {
				clsEditorWord objEditorWord = new clsEditorWord();
				clsEditorSyllable objEditorSyllable = new clsEditorSyllable(objSyllable.strValue);
				objEditorWord.objSyllables.add(objEditorSyllable);
				objEditorWord.boolIsEnabled = objSyllable.boolIsEnabled;
				objInputData.objWords.add(objEditorWord);
			}
			strInputData = clsUtils.SerializeToString(objInputData);
			objIntent = new Intent(this, ActivityEditor.class);
			objIntent.putExtra(ActivityEditor.INPUT_DATA, strInputData);
			objIntent.putExtra(ActivityEditor.INPUT_DATA_TYPE, ActivityEditor.INPUT_DATA_TYPE_SYLLABLES);
			startActivityForResult(objIntent, intSharedResultCode);
			break;
		case R.id.action_buy_pro_version:
			try {
				// Action: Prepare Iab
				mHelper = new IabHelper(this, base64EncodedPublicKeyPart);
				setWaitScreen(objContext, true);
				mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
					public void onIabSetupFinished(IabResult result) {

						if (!result.isSuccess()) {
							// There was a problem.
							setWaitScreen(objContext, false);
							clsUtils.MessageBox(objContext, "Error", true,"Problem setting up in-app billing: " + result, true, null);
							return;
						}

						// Have we been disposed of in the meantime? If so, quit.
						if (mHelper == null) {
							setWaitScreen(objContext, false);
							return;
						}
						// IAB is fully set up. Now, let's get an inventory of stuff user owns.
						//Action: Get the items that can be purchased and prompt
						ArrayList<String> skuList = new ArrayList<String> ();
						skuList.add(ActivityStartup.SKU_UPGRADE_TO_PRO);
						mHelper.queryInventoryAsync(true, skuList, new QueryInventoryFinishedListener() {
							
							@Override
							public void onQueryInventoryFinished(IabResult result, Inventory inv) {
								if (result.isSuccess()) {
									SkuDetails objSkuDetails = inv.getSkuDetails(ActivityStartup.SKU_UPGRADE_TO_PRO);
									if (objSkuDetails != null) {
										String strName = objSkuDetails.getTitle();
										String strPrice = objSkuDetails.getPrice();
										// Action: Prompt user if he wants to buy
										setWaitScreen(objContext, false);
										AlertDialog.Builder objBuilder = new AlertDialog.Builder(objContext);
										objBuilder.setTitle("Item: " + strName + " costs " + strPrice + " to enable. Do you want to continue?");
										objBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
											
											@Override
											public void onClick(DialogInterface dialog, int which) {
												// Action: User confirms, carry on with purchase
												setWaitScreen(objContext, true);
												mHelper.launchPurchaseFlow((Activity)objContext, ActivityStartup.SKU_UPGRADE_TO_PRO,
														ActivityStartup.RC_REQUEST, new OnIabPurchaseFinishedListener() {															
															@Override
															public void onIabPurchaseFinished(IabResult result, Purchase info) {
																if (result.isSuccess()) {
																	// Action: Purchase is success, double check results
																	 if (VerifyDeveloperPayload(info)) {
																		 if (info.getSku().equals(ActivityStartup.SKU_UPGRADE_TO_PRO)) {
																            	// Action: Update local data
																			 	SetUpgradedToProInLocalPreference(objContext, true);
																			 	UnDisplayAd();
																			 	UpdateGui(objSession);
																	            setWaitScreen(objContext, false);
																         } else {
																				setWaitScreen(objContext, false);
																            	clsUtils.MessageBox(objContext, "Error", true, "Error purchasing. Incorrect item purchased.",false, null);
																                return;
																         }
																	 } else {
																		setWaitScreen(objContext, false);
														            	clsUtils.MessageBox(objContext, "Error", true, "Error purchasing. Authenticity verification failed.",false, null);
														                return;
														             }	
																} else {
																	setWaitScreen(objContext, false);
																	clsUtils.MessageBox(objContext, "Error", true,"Problem purchasing. " + result.getMessage(), false, null);
																	return;
																}
																
															}
														});
												
											}
										});
										objBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
											
											@Override
											public void onClick(DialogInterface dialog, int which) {
												// Do nothing if purchase request cancelled
												setWaitScreen(objContext, false);
												return;
											}
										});
										objBuilder.show();
									} else {
										setWaitScreen(objContext, false);
										clsUtils.MessageBox(objContext, "Error", true,"Problem retieving in-app billing products. Could not find sales item", false, null);
										return;
									}
									
								} else {
									setWaitScreen(objContext, false);
									clsUtils.MessageBox(objContext, "Error", true,"Problem retieving in-app billing products. " + result.getMessage(), false, null);
									return;
								}							
							}
						});
					}
				});
	
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			break;
		case R.id.action_developer_unpurchase:
			try {
				// Action: Prepare Iab
				mHelper = new IabHelper(this, base64EncodedPublicKeyPart);
				setWaitScreen(objContext, true);
				mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
					public void onIabSetupFinished(IabResult result) {

						if (!result.isSuccess()) {
							// There was a problem.
							setWaitScreen(objContext, false);
							clsUtils.MessageBox(objContext, "Error", true,"Problem setting up in-app billing. " + result, false, null);
							return;
						}

						// Have we been disposed of in the meantime? If so, quit.
						if (mHelper == null) {
							setWaitScreen(objContext, false);
							return;
						}
						// Action: IAB is fully set up. Get inventory and un-purchase.
						mHelper.queryInventoryAsync(new QueryInventoryFinishedListener() {
							
							@Override
							public void onQueryInventoryFinished(IabResult result, Inventory inv) {
								if (result.isSuccess()){
									if (inv.hasPurchase(ActivityStartup.SKU_UPGRADE_TO_PRO)) {
										// Action: Un-purchase
										mHelper.consumeAsync(inv.getPurchase(ActivityStartup.SKU_UPGRADE_TO_PRO), new OnConsumeFinishedListener() {
											
											@Override
											public void onConsumeFinished(Purchase purchase, IabResult result) {
												if (result.isSuccess()) {
													// Consume successful
													ClearUpgradedToProInLocalPreference(objContext);
													setWaitScreen(objContext, false);
													clsUtils.MessageBox(objContext, "Info", false,"Item successfully consumed", true, null);
													return;													
												} else {
													// Failure
													setWaitScreen(objContext, false);
													clsUtils.MessageBox(objContext, "Error", true,"Expected item appears not to be purchased", false, null);
													return;
												}												
											}
										});
									} else {
										setWaitScreen(objContext, false);
										clsUtils.MessageBox(objContext, "Error", true,"Expected item appears not to be purchased", false, null);
										return;
									}
									
								} else {
									setWaitScreen(objContext, false);
									clsUtils.MessageBox(objContext, "Error", true,"Problem getting inventory. " + result, false, null);
									return;
								}								
							}
						});
					}
				});	
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}		
			break;
		case R.id.action_tools_multiplication_trainer:
			objIntent = new Intent(this,ActivityMultiDivTrainer.class);
			objIntent.putExtra(ActivityMultiDivTrainer.TEST_TYPE, ActivityMultiDivTrainer.enumTestTypes.MULTIPLY);
			startActivity(objIntent);
			break;
		case R.id.action_tools_divide_trainer:
			objIntent = new Intent(this,ActivityMultiDivTrainer.class);
			objIntent.putExtra(ActivityMultiDivTrainer.TEST_TYPE, ActivityMultiDivTrainer.enumTestTypes.DIVIDE);
			startActivity(objIntent);
			break;
		case R.id.action_developer_clear_all:
			// Delete application folder
			clsUtils.RemoveAppFolder(this);
			
			// Delete persistent state
			ClearState();
			ClearUpgradedToProInLocalPreference(this);
			
			// Exit
			finish();
			break;
		case R.id.action_let_friends_know:
			try {
				clsUtils.SendLetYourFriendsKnowEmail(this);
			} catch (Exception e) {
				e.printStackTrace();
				clsUtils.MessageBox(this, "Error", true, "Unable to send message. " + e.getMessage(), false, null);
			}
			break;
		case R.id.action_help:
			CreateHelpMessage(this).show();
			break;
		case R.id.action_about:
			clsUtils.MessageBox(this, "About", false, "SyllablesTrainer Version: " + clsUtils.getAppVersionName(this) + "." + clsUtils.getAppVersionCode(this), false, null);
			break;
		case R.id.action_startup_settings:
			objIntent = new Intent(this, ActivityStartupSettings.class);
			startActivity(objIntent);
			break;
		}
		return true;

	}
	
	 public static AlertDialog CreateHelpMessage(Context context) {
	  final TextView message = new TextView(context);
	  final SpannableString s = new SpannableString("Please see the intro video at \nhttps://www.youtube.com/watch?v=TT2N7TuQaoU&feature=youtu.be");
	  Linkify.addLinks(s, Linkify.WEB_URLS);
	  message.setText(s);
	  message.setMovementMethod(LinkMovementMethod.getInstance());

	  return new AlertDialog.Builder(context)
	   .setTitle("Help")
	   .setCancelable(true)
	   .setIcon(android.R.drawable.ic_dialog_info)
	   .setPositiveButton("Dismiss", null)
	   .setView(message)
	   .create();
	 }
	


	private boolean VerifyDeveloperPayload(Purchase info) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		
		// Pass on the activity result to the helper for handling
	    if (!mHelper.handleActivityResult(requestCode, resultCode, data)) {
	        // Not handled, so handle it ourselves (here's where you'd
	        // perform any handling of activity results not related to in-app
	        // billing...
	        super.onActivityResult(requestCode, resultCode, data);
	        
	        if (resultCode == RESULT_OK) {
				Bundle objBundle = data.getExtras();
				switch (requestCode) {
				case EDIT_REAL_WORDS:
					String strOutputData = objBundle.getString(ActivityEditor.OUTPUT_DATA);
					clsOutputData objOutputData = clsUtils.DeSerializeFromString(strOutputData, clsOutputData.class);
					ArrayList<clsWord> objUpdatedWords = new ArrayList<clsWord>();
					for (clsEditorWord objEditorWord : objOutputData.objWords) {
						String strSyll1 = "", strSyll2 = "", strSyll3 = "", strSyll4 = "";
						switch (objEditorWord.objSyllables.size()) {
						case 4:
							strSyll4 = objEditorWord.objSyllables.get(3).strValue;
						case 3:
							strSyll3 = objEditorWord.objSyllables.get(2).strValue;
						case 2:
							strSyll2 = objEditorWord.objSyllables.get(1).strValue;
						case 1:
							strSyll1 = objEditorWord.objSyllables.get(0).strValue;
						}
						clsWord objNewWord = new clsWord(strSyll1, strSyll2, strSyll3, strSyll4, objEditorWord.boolIsEnabled);
						objUpdatedWords.add(objNewWord);
					}
					objWordsHelper.UpdateWords(objUpdatedWords);
					try {
						objWordsHelper.SaveRepository(objContext);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						clsUtils.MessageBox(objContext, "Error", true, "Could not update word list. " + e.getMessage(),
								false, null);
					}
					break;
				case EDIT_PRE_SYLLABLES:
				case EDIT_ROOT_SYLLABLES:
				case EDIT_POST_SYLLABLES:
					strOutputData = objBundle.getString(ActivityEditor.OUTPUT_DATA);
					objOutputData = clsUtils.DeSerializeFromString(strOutputData, clsOutputData.class);
					ArrayList<clsSyllable> objUpdatedSyllables = new ArrayList<clsSyllable>();
					for (clsEditorWord objEditorWord : objOutputData.objWords) {
						clsSyllable objNewSyllable = new clsSyllable(objEditorWord.objSyllables.get(0).strValue, objEditorWord.boolIsEnabled);
						objUpdatedSyllables.add(objNewSyllable);
					}
					switch (requestCode) {
					case EDIT_PRE_SYLLABLES:
						objWordsHelper.UpdatePreSyllables(objUpdatedSyllables);
						break;
					case EDIT_ROOT_SYLLABLES:
						objWordsHelper.UpdateRootSyllables(objUpdatedSyllables);
						break;
					case EDIT_POST_SYLLABLES:
						objWordsHelper.UpdatePostSyllables(objUpdatedSyllables);
						break;
					}

					try {
						objWordsHelper.SaveRepository(objContext);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						clsUtils.MessageBox(objContext, "Error", true, "Could not update word list. " + e.getMessage(),
								false, null);
					}
					break;
				case FILE_CHOOSER:
					OnChooseFileCompleteListener OnChooseFileCompleteListener = objRetainedFragment.getData();
					if (OnChooseFileCompleteListener != null) {
						final String strSelectedFilePath = data.getStringExtra("SelectedFilePath");
						OnChooseFileCompleteListener.OnComplete(true, strSelectedFilePath);
					} else {
						clsUtils.MessageBox(objContext, "Error", true, "Could not return filename to calling activity",
								false, null);
					}
					break;
				}
			} else {
				// Do basically nothing
				switch (requestCode) {
				case FILE_CHOOSER:
					// Flag to calling that no file selected
					OnChooseFileCompleteListener OnChooseFileCompleteListener = objRetainedFragment.getData();
					if (OnChooseFileCompleteListener != null) {
						OnChooseFileCompleteListener.OnComplete(false, "");
					} else {
						clsUtils.MessageBox(objContext, "Error", true, "Could not return filename to calling activity",
								false, null);
					}
					break;
				}
			}
	    }
	    else {
	        Log.d(ActivityStartup.TAG_IAB, "onActivityResult handled by IABUtil.");
	    }
	
	}

	interface OnChooseFileCompleteListener {
		public void OnComplete(boolean boolIsFileChosen, String strFilePath);
	}

	public class RetainedFragment extends Fragment {

		// data object we want to retain
		private OnChooseFileCompleteListener data;

		// this method is only called once for this fragment
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			// retain this fragment
			setRetainInstance(true);
		}

		public void setData(OnChooseFileCompleteListener data) {
			this.data = data;
		}

		public OnChooseFileCompleteListener getData() {
			return data;
		}
	}

	private void ChooseFile(String strFilterExtension, OnChooseFileCompleteListener OnChooseFileResultReceiver) {
		objRetainedFragment.setData(OnChooseFileResultReceiver);
		SaveState();
		LoadState();
		Intent intent = new Intent(this, ActivityFileChooser.class);
		intent.putExtra(ActivityFileChooser.PATH, clsUtils.GetAppDirectoryName(this));
		intent.putExtra(ActivityFileChooser.NEW_FILE_PROMPT, false);
		intent.putExtra(ActivityFileChooser.FILTER_EXTENSION, strFilterExtension);
		startActivityForResult(intent, FILE_CHOOSER);
	}

	void SaveState() {
		clsUtils.SerializeToSharedPreferences("ActivityStartup", "objSession", this, objSession);
	}
	
	private void ClearState() {
		objSession = null;
		SaveState();		
	}

	void LoadState() {
		objContext = this;
		
		objSession = clsUtils.DeSerializeFromSharedPreferences("ActivityStartup", "objSession", this, clsSession.class);
		if (objSession != null) {
			try {
				objWordsHelper.LoadRepository(this);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		FragmentManager fragmentManager = getFragmentManager();
		objRetainedFragment = (RetainedFragment) fragmentManager.findFragmentByTag("DATA");
		if (objRetainedFragment == null) {
			objRetainedFragment = new RetainedFragment();
			fragmentManager.beginTransaction().add(objRetainedFragment, "DATA").commit();
		}
		
		if (mHelper == null) {
			mHelper = new IabHelper(this, base64EncodedPublicKeyPart);
			mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
				public void onIabSetupFinished(IabResult result) {
					if (result.isFailure()) {
						// There was a problem.
						clsUtils.MessageBox(objContext, "Error", true,"Problem setting up in-app billing: " + result, true, null);
						return;
					} 
				}
			});
		}
		
		if (objIabProgressDialog == null) {
			objIabProgressDialog =  new clsRobustProgressDialog(objContext);
		}
		

	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		SaveState();
		if (objImportWordDataFromCsvFileAsyncTask != null) {
			objImportWordDataFromCsvFileAsyncTask.dismiss();
		}
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if (mHelper != null) {
			mHelper.dispose();
	    } 
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		LoadState();
	}

	class MyOnClickListener implements View.OnClickListener {

		@Override
		public void onClick(View v) {
			if (objSession.enumWordTypes == enumWordType.REAL) {
				switch (v.getId()) {
				case R.id.syllable1_button_next:
					objSession.strDisplaySyllable1 = clsWordsHelper.GetRandomSyllable(objWordsHelper.objSyllables1);
					break;
				case R.id.syllable2_button_next:
					objSession.strDisplaySyllable2 = clsWordsHelper.GetRandomSyllable(objWordsHelper.objSyllables2);
					break;
				case R.id.syllable3_button_next:
					objSession.strDisplaySyllable3 = clsWordsHelper.GetRandomSyllable(objWordsHelper.objSyllables3);
					break;
				case R.id.syllable4_button_next:
					objSession.strDisplaySyllable4 = clsWordsHelper.GetRandomSyllable(objWordsHelper.objSyllables4);
					break;
				}
			} else {
				switch (v.getId()) {
				case R.id.syllable1_button_next:
					objSession.strDisplaySyllable1 = clsWordsHelper.GetRandomSyllable(objWordsHelper.objFilteredPreSyllables);
					break;
				case R.id.syllable2_button_next:
					objSession.strDisplaySyllable2 = clsWordsHelper.GetRandomSyllable(objWordsHelper.objFilteredRootSyllables);
					break;
				case R.id.syllable3_button_next:
					objSession.strDisplaySyllable3 = clsWordsHelper.GetRandomSyllable(objWordsHelper.objFilteredPostSyllables);
					break;
				case R.id.syllable4_button_next:
					objSession.strDisplaySyllable4 = "";
					break;
				}
			}
			objSession.intTestAmount += 1;
			SaveState();
			UpdateGui(objSession);
		}

	}
	
	
	// Enables or disables the "please wait" screen.
	static void setWaitScreen(Context objContext, boolean set) {
		if (set == true) { 
			objIabProgressDialog = null;
		}
		if (objIabProgressDialog == null) {
			objIabProgressDialog = new clsRobustProgressDialog(objContext);
			objIabProgressDialog.setMessage("Processing..., please wait.");
		}
		if (set) {
			if (!objIabProgressDialog.isShowing()) {
				objIabProgressDialog.show();
			}
		} else {
			if (objIabProgressDialog.isShowing()) {
				objIabProgressDialog.dismiss();
			}
		}
	}
	
	

}
