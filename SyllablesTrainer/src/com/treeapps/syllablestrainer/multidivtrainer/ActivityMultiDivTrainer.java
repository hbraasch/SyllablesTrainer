package com.treeapps.syllablestrainer.multidivtrainer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.gson.reflect.TypeToken;
import com.treeapps.syllablestrainer.ActivityStartup;
import com.treeapps.syllablestrainer.R;
import com.treeapps.syllablestrainer.ActivityStartup.clsPurchaseState;
import com.treeapps.syllablestrainer.utils.clsColoredStringBuilder;
import com.treeapps.syllablestrainer.utils.clsUtils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class ActivityMultiDivTrainer extends Activity {
	
	public static final int MAX_TABLES_AMOUNT = 12;
	
	public static final String TEST_TYPE = "com.treeapps.syllablestrainer.multidivtrainer.test_type";

	private static final int SELECT_TABLES = 1;
	
	public enum enumTestTypes {
		MULTIPLY, DIVIDE
	}
	
	class clsSession {
		public clsNumerator objCurrentQuestionNumerator;
		public clsDenominator objCurrentQuestionDenominator;
		public int intCurrentAnswer = 0;
		public boolean boolIsAnswerEmpty = true;
		public clsData objData = new clsData();
		public int intTestAmount;
		public int intCorrectCount;
		public enumTestTypes enumTestType = enumTestTypes.MULTIPLY;
		public int intCurrentMultiplyTermLeft;
		public int intCurrentMultiplyTermRight;
	}
	
	class clsData {
		public ArrayList<clsNumerator> objNumerators = new ArrayList<clsNumerator>();
		public ArrayList<clsNumerator> objFilteredNumerators = new ArrayList<clsNumerator>();
		public ArrayList<Integer> objEnabledTables = new ArrayList<Integer>();
	}
	
	class clsNumerator {
		public int intValue;
		public ArrayList<clsDenominator> objDenominators = new ArrayList<clsDenominator>();
		public clsNumerator(int intValue) {
			super();
			this.intValue = intValue;
		}
		public void GenerateOwnDenominators() {
			objDenominators.clear();
			for (int i = 1; i <= MAX_TABLES_AMOUNT; i++ ) {
				int quotient = intValue/i;
				int remainder = intValue % i;
				if (quotient >= 1) {
					if (remainder == 0) {
						if (intValue/i <= 12) {
							objDenominators.add(new clsDenominator(i));
						}
					}
				}
			}			
		}
		
		private clsDenominator FindDenominatorFromValue(int intSelectedDenominatorValue) {
			for (clsDenominator objDenominator: objDenominators ) {
				if (objDenominator.intValue == intSelectedDenominatorValue) {
					return objDenominator;
				}
			}
			return null;
		}
		
	
	}
	
	class clsDenominator {
		public int intValue;

		public clsDenominator(int intValue) {
			super();
			this.intValue = intValue;
		}
		
	}
	
	// Persist
	clsSession objSession;
	Context objContext;
	
	// End of persist
	
	AdView adView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_divide_trainer);
			
		TextView objKey0 = (TextView) findViewById(R.id.keypad_key_0);
		TextView objKey1 = (TextView) findViewById(R.id.keypad_key_1);
		TextView objKey2 = (TextView) findViewById(R.id.keypad_key_2);
		TextView objKey3 = (TextView) findViewById(R.id.keypad_key_3);
		TextView objKey4 = (TextView) findViewById(R.id.keypad_key_4);
		TextView objKey5 = (TextView) findViewById(R.id.keypad_key_5);
		TextView objKey6 = (TextView) findViewById(R.id.keypad_key_6);
		TextView objKey7 = (TextView) findViewById(R.id.keypad_key_7);
		TextView objKey8 = (TextView) findViewById(R.id.keypad_key_8);
		TextView objKey9 = (TextView) findViewById(R.id.keypad_key_9);		
		TextView objKeyClear = (TextView) findViewById(R.id.keypad_key_clear);
		TextView objKeyEnter = (TextView) findViewById(R.id.keypad_key_enter);
		
		objKey0.setOnClickListener(new MyOnClickListener());
		objKey1.setOnClickListener(new MyOnClickListener());
		objKey2.setOnClickListener(new MyOnClickListener());
		objKey3.setOnClickListener(new MyOnClickListener());
		objKey4.setOnClickListener(new MyOnClickListener());
		objKey5.setOnClickListener(new MyOnClickListener());
		objKey6.setOnClickListener(new MyOnClickListener());
		objKey7.setOnClickListener(new MyOnClickListener());
		objKey8.setOnClickListener(new MyOnClickListener());
		objKey9.setOnClickListener(new MyOnClickListener());
		objKeyClear.setOnClickListener(new MyOnClickListener());
		objKeyEnter.setOnClickListener(new MyOnClickListener());
		
		ImageView objSuccessImageView = (ImageView) this.findViewById(R.id.prompt_success_icon);
		objSuccessImageView.setVisibility(View.INVISIBLE);
		
		RadioButton objTestTypeMultiplyRadioButton = (android.widget.RadioButton) this.findViewById(R.id.radio_test_type_multiply);
		RadioButton objTestTypeDivideRadioButton = (android.widget.RadioButton) this
				.findViewById(R.id.radio_test_type_divide);
		objTestTypeMultiplyRadioButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (((RadioButton) v).isChecked()) {
					objSession.enumTestType = enumTestTypes.MULTIPLY;
				} else {
					objSession.enumTestType = enumTestTypes.DIVIDE;
				}
				objSession.intCurrentAnswer = 0;
				objSession.boolIsAnswerEmpty = true;
				GetRandomQuestion();
				SaveState();
				UpdateGui();
			}
		});
		
		objTestTypeDivideRadioButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (((RadioButton) v).isChecked()) {
					objSession.enumTestType = enumTestTypes.DIVIDE;
				} else {
					objSession.enumTestType = enumTestTypes.MULTIPLY;
				}
				objSession.intCurrentAnswer = 0;
				objSession.boolIsAnswerEmpty = true;
				GetRandomQuestion();
				SaveState();
				UpdateGui();
			}
		});
		
		ImageView objTestAmountImageView =  (ImageView) this.findViewById(R.id.clear_image_test_amount);
		objTestAmountImageView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				clsUtils.OkCancelDialog(objContext, "Are you sure you want to clear the counter?", new clsUtils.OnOkCancelDialogCompleteListener() {				
					@Override
					public void OnComplete(boolean boolIsOk) {
						if (boolIsOk) {
							ClearTestCounter();
							SaveState();
							UpdateGui();
						}						
					}
				});				
			}
		});
		
		ImageView objSuggestionImageView =  (ImageView) this.findViewById(R.id.suggestion_image);
		objSuggestionImageView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Toast.makeText(objContext, "The answer is: " + GetAnswer(), Toast.LENGTH_SHORT).show();			
			}
		});
		
		// AdMob, only when advert removal has not been purchased
		boolean boolIsUpgraded = false;
		clsPurchaseState objPurchaseState = ActivityStartup.CheckIfUpgradedToProFromLocalPreference(this);
		if (objPurchaseState != null) {
			if (objPurchaseState.boolIsUpgradedToPro) {
				boolIsUpgraded = true;
			}
		}
		if(boolIsUpgraded) {
			UnDisplayAd();		
		} else {
			DisplayAd();
		}
		
		if(savedInstanceState == null) {
			objSession = clsUtils.DeSerializeFromSharedPreferences("ActivityDivideTrainer", "objSession", this, clsSession.class);
			if (objSession == null) {
				objSession = new clsSession();
				CreateDefaultSourceData();
			} 
			objSession.enumTestType = (enumTestTypes) getIntent().getSerializableExtra(TEST_TYPE);
			
			// Setup new state
			GetRandomQuestion();
			ClearTestCounter();
			
			// Save state
			SaveState();
		} else {
			LoadState();
		}
		
		UpdateGui();
				
	}

	public void UnDisplayAd() {
		RelativeLayout objRelativeLayout = (RelativeLayout) findViewById(R.id.layout_multidiv_trainer);
		View objAddView = (View) findViewById(R.id.multidiv_trainer_google_ad_view);
		objRelativeLayout.removeView(objAddView);
	}

	public void DisplayAd() {
		// Look up the AdView as a resource and load a request.
		adView = (AdView) this.findViewById(R.id.multidiv_trainer_google_ad_view);
		AdRequest adRequest = new AdRequest.Builder().addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
				.addTestDevice("803D489BC46137FD0761EC7EBFBBFB09")
				.addTestDevice("C1B978D9FE1B0A6A8A58F1F44F653BE3")
				.addTestDevice("FB01AE66402BBCBA23816BED978B7D93")
				.addTestDevice("893617FB0C72A211F504BCEDEA9D3EEA")
				.addTestDevice("84144DEC93984DE4D04B37D684E93832")
				.addTestDevice("A947D095EE036142160FD3D2D4D5034C").build();
		adView.loadAd(adRequest);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_divide_trainer, menu);
		
		// Get handles of dynamic menu items
		MenuItem objSelectTables = menu.findItem(R.id.action_multidiv_trainer_select_tables);
		
		boolean boolIsUpgraded = false;
		clsPurchaseState objPurchaseState = ActivityStartup.CheckIfUpgradedToProFromLocalPreference(objContext);
		if (objPurchaseState != null) {
			if (objPurchaseState.boolIsUpgradedToPro) {
				boolIsUpgraded = true;
			}
		}
		if (boolIsUpgraded) {
			clsUtils.SetMenuItemEnabled(objSelectTables, true);
		} else {
			clsUtils.SetMenuItemEnabled(objSelectTables, false);
		}		

		return true;
	}

	private void UpdateGui() {
		String strQuestion = GetQuestionString();
		TextView objQuestion = (TextView) findViewById(R.id.prompt_question);
		objQuestion.setText(strQuestion);
		
		TextView objAnswer = (TextView) findViewById(R.id.prompt_answer);
		if (objSession.boolIsAnswerEmpty) {
			objAnswer.setText("?");
		} else {
			objAnswer.setText(Integer.toString(objSession.intCurrentAnswer));
		}
		
		RadioButton objTestTypeMultiplyRadioButton = (android.widget.RadioButton) this.findViewById(R.id.radio_test_type_multiply);
		RadioButton objTestTypeDivideRadioButton = (android.widget.RadioButton) this.findViewById(R.id.radio_test_type_divide);
		if (objSession.enumTestType == enumTestTypes.MULTIPLY) {
			objTestTypeMultiplyRadioButton.setChecked(true);
			objTestTypeDivideRadioButton.setChecked(false);
		} else {
			objTestTypeMultiplyRadioButton.setChecked(false);
			objTestTypeDivideRadioButton.setChecked(true);		
		}
		
		UpdateTestCounterDisplay();
	}



	public String GetQuestionString() {
		String strQuestion;
		if (objSession.enumTestType == enumTestTypes.DIVIDE) {
			strQuestion = objSession.objCurrentQuestionNumerator.intValue + " \u00F7 "
					+ objSession.objCurrentQuestionDenominator.intValue + " = ";
		} else {
			strQuestion = objSession.intCurrentMultiplyTermLeft + " x "
					+ objSession.intCurrentMultiplyTermRight + " = ";
		}
		return strQuestion;
	}
	
	public int GetAnswer() {
		if (objSession.enumTestType == enumTestTypes.DIVIDE) {
			return objSession.objCurrentQuestionNumerator.intValue / objSession.objCurrentQuestionDenominator.intValue;
		} else {
			return objSession.intCurrentMultiplyTermLeft * objSession.intCurrentMultiplyTermRight;
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		
		Intent objIntent;
		final int id = item.getItemId();

		switch (id) {
		case R.id.action_multidiv_trainer_select_tables:
			objIntent = new Intent(this, ActivitySelectTables.class);
			ArrayList<ActivitySelectTables.clsListItem> objListItems = new ArrayList<ActivitySelectTables.clsListItem>();
			for (int intTable = 1; intTable <= MAX_TABLES_AMOUNT; intTable ++) {
				ActivitySelectTables.clsListItem objListItem = new ActivitySelectTables.clsListItem();
				objListItem.intTable = intTable;
				objListItem.boolIsSelected = false;
				objListItems.add(objListItem);
				for (int intEnabledTable: objSession.objData.objEnabledTables) {
					if (intEnabledTable == intTable) {
						objListItem.boolIsSelected = true;
						break;
					}
				}
			}
			String strListItems = clsUtils.SerializeToString(objListItems);
			objIntent.putExtra(ActivitySelectTables.LIST_ITEMS, strListItems);
			startActivityForResult(objIntent, SELECT_TABLES);
			break;
		}
		
		return super.onOptionsItemSelected(item);
	}
	
	class MyOnClickListener implements View.OnClickListener {
		boolean boolEnterPressed = false;
		@Override
		public void onClick(View v) {
			objSession.boolIsAnswerEmpty = false;
			switch (v.getId()) {
			case R.id.keypad_key_0:
				objSession.intCurrentAnswer = (objSession.intCurrentAnswer * 10) + 0;
				break;
			case R.id.keypad_key_1:
				objSession.intCurrentAnswer = (objSession.intCurrentAnswer * 10) + 1;
				break;
			case R.id.keypad_key_2:
				objSession.intCurrentAnswer = (objSession.intCurrentAnswer * 10) + 2;
				break;
			case R.id.keypad_key_3:
				objSession.intCurrentAnswer = (objSession.intCurrentAnswer * 10) + 3;
				break;
			case R.id.keypad_key_4:
				objSession.intCurrentAnswer = (objSession.intCurrentAnswer * 10) + 4;
				break;
			case R.id.keypad_key_5:
				objSession.intCurrentAnswer = (objSession.intCurrentAnswer * 10) + 5;
				break;
			case R.id.keypad_key_6:
				objSession.intCurrentAnswer = (objSession.intCurrentAnswer * 10) + 6;
				break;
			case R.id.keypad_key_7:
				objSession.intCurrentAnswer = (objSession.intCurrentAnswer * 10) + 7;
				break;
			case R.id.keypad_key_8:
				objSession.intCurrentAnswer = (objSession.intCurrentAnswer * 10) + 8;
				break;
			case R.id.keypad_key_9:
				objSession.intCurrentAnswer = (objSession.intCurrentAnswer * 10) + 9;
				break;
			case R.id.keypad_key_clear:
				objSession.intCurrentAnswer = 0;
				objSession.boolIsAnswerEmpty = true;
				SaveState();
				UpdateGui();
				return;
			case R.id.keypad_key_enter:
				boolEnterPressed = true;				
				break;
			}
			
			if (IsEnoughKeysPressed()) {
				boolEnterPressed = true;
			}
			
			if (boolEnterPressed || IsAnswerCorrect()) {
				boolEnterPressed = false;
				if (IsAnswerCorrect()) {
					// Pass
					DisplaySuccessImage(true, 1000, new OnDisplaySuccessCompleteListener() {
						@Override
						public void OnComplete() {
							IncrementTestCounter(true);
							objSession.intCurrentAnswer = 0;
							objSession.boolIsAnswerEmpty = true;
							GetRandomQuestion();
							SaveState();
							UpdateGui();
						}						
					});
				} else {
					// Fail
					IncrementTestCounter(false);
					objSession.intCurrentAnswer = 0;
					objSession.boolIsAnswerEmpty = true;
					DisplaySuccessImage(false, 2000, null);
				}
			}
			SaveState();
			UpdateGui();		
		}



		public boolean IsAnswerCorrect() {
			return GetAnswer() == objSession.intCurrentAnswer;
		}
		
		private boolean IsEnoughKeysPressed() {
			int intActualAnswer = GetAnswer();
			String strActualAnswer = Integer.toString(intActualAnswer);
			String strCurrentAnswer = Integer.toString(objSession.intCurrentAnswer);
			if (strActualAnswer.length() <= strCurrentAnswer.length()) return true;
			return false;
		}

	}
	
	public interface OnDisplaySuccessCompleteListener {
		public void OnComplete();
	}
	
	private void DisplaySuccessImage(boolean boolIsSuccess, int intDurationMs, final OnDisplaySuccessCompleteListener OnDisplaySuccessCompleteListener) {
		final ImageView objImageView = (ImageView) ((Activity) objContext).findViewById(R.id.prompt_success_icon);
        if (boolIsSuccess) {
			objImageView.setBackground(objContext.getResources().getDrawable(R.drawable.correct_icon));
        } else {
			objImageView.setBackground(objContext.getResources().getDrawable(R.drawable.wrong_icon));
        }
		objImageView.setVisibility(View.VISIBLE);
		CountDownTimer blinkTimer = new CountDownTimer(intDurationMs, intDurationMs) {
			
			@Override
			public void onTick(long millisUntilFinished) {
				
			}

			@Override
			public void onFinish() {
				objImageView.setVisibility(View.INVISIBLE);
				if (OnDisplaySuccessCompleteListener != null) {
					OnDisplaySuccessCompleteListener.OnComplete();
				}				
			}
			
		};
		blinkTimer.start();
	}
	
	class clsSuccessImageWithDisplayDurationRunnable implements Runnable {

		int intDurationMs;
		boolean boolIsSuccess;
		
		public clsSuccessImageWithDisplayDurationRunnable(boolean boolIsSuccess, int intDurationMs) {
			this.boolIsSuccess = boolIsSuccess;
			this.intDurationMs = intDurationMs;			
		}

		@Override
		public void run() {
			try {
				ImageView objImageView = (ImageView) ((Activity) objContext).findViewById(R.id.prompt_success_icon);
	            if (boolIsSuccess) {
					objImageView.setBackground(objContext.getResources().getDrawable(R.drawable.correct_icon));
	            } else {
					objImageView.setBackground(objContext.getResources().getDrawable(R.drawable.wrong_icon));
	            }
				objImageView.setVisibility(View.VISIBLE);
				Thread.sleep(intDurationMs);
				objImageView.setVisibility(View.INVISIBLE);
           } catch (Exception e) {
               e.printStackTrace();
           }
		}
		
	}
	
	
	private void CreateDefaultSourceData() {
		// Update numerators
		CreateDefaultNumerators();
		CreateDefaultEnabledTables();
	}
	
	
	private void CreateDefaultNumerators() {
		// TODO Auto-generated method stub
		objSession.objData.objNumerators.clear();
		ArrayList<Integer> intNumerators = new ArrayList<Integer>(Arrays.asList(
				1,2,4,6,8,9,10,12,14,15,16,18,20,21,22,24,25,27,28,30,32,33,35,36,40,44,45,48,50,55,56,60,66,70,72,77,80,81,82,88,90,96,99,100,108,110,120,121,132,144));
		for (int i = 0; i < intNumerators.size(); i++) {
			clsNumerator objNumerator = new clsNumerator(intNumerators.get(i));
			objNumerator.GenerateOwnDenominators();
			objSession.objData.objNumerators.add(objNumerator);
		}
	}
	
	private void CreateDefaultEnabledTables() {
		objSession.objData.objEnabledTables.clear();
		for (int i = 1; i <= MAX_TABLES_AMOUNT; i++) {
			objSession.objData.objEnabledTables.add(i);
		}		
	}
	
	private void UpdateFilteredNumerators(int intSelectedTable) {
		objSession.objData.objFilteredNumerators.clear();
		for (clsNumerator objNumerator: objSession.objData.objNumerators) {

			for(clsDenominator objDenominator: objNumerator.objDenominators) {
				if (objDenominator.intValue == intSelectedTable ) {
					// Yes, it contains table
					// See if already added
					boolean boolAlreadyExists = false;
					for (clsNumerator objNumerator2: objSession.objData.objFilteredNumerators) {
						if (objNumerator2.intValue == objNumerator.intValue) {
							boolAlreadyExists = true;
							break;
						}
					}
					if (!boolAlreadyExists) {
						objSession.objData.objFilteredNumerators.add(objNumerator);
					}						
				}
			}
		}
	}
	

	
	public void GetRandomQuestion() {
		if (objSession.enumTestType == enumTestTypes.DIVIDE) {
			
			Random r = new Random();
			// Get Denominator
			int intMaxVal = objSession.objData.objEnabledTables.size() - 1;
			int intMinVal = 0;
			int intIndex;
			if (intMaxVal != 0) {
				intIndex = r.nextInt((intMaxVal - intMinVal) + 1) + intMinVal;
			} else {
				intIndex = 0;
			}
			int intSelectedDenominator = objSession.objData.objEnabledTables.get(intIndex);
			
			UpdateFilteredNumerators(intSelectedDenominator);
			
			// Get Numerator

			intMaxVal = objSession.objData.objFilteredNumerators.size() - 1;
			intMinVal = 0;

			if (intMaxVal != 0) {
				intIndex = r.nextInt((intMaxVal - intMinVal) + 1) + intMinVal;
			} else {
				intIndex = 0;
			}
			clsNumerator objSelectedNumerator = objSession.objData.objFilteredNumerators.get(intIndex);
			
			// Save to session data
			objSession.objCurrentQuestionNumerator = objSelectedNumerator;
			objSession.objCurrentQuestionDenominator = objSelectedNumerator.FindDenominatorFromValue(intSelectedDenominator);
			objSession.intCurrentAnswer = 0;
			objSession.boolIsAnswerEmpty = true;
		} else if (objSession.enumTestType == enumTestTypes.MULTIPLY) {
			Random r = new Random();
			// Left term
			int intMaxVal = MAX_TABLES_AMOUNT;
			int intMinVal = 1;
			int intTermLeftIndex;
			intTermLeftIndex = r.nextInt((intMaxVal - intMinVal) + 1) + intMinVal;

			
			// Right term
			intMaxVal = objSession.objData.objEnabledTables.size() - 1;
			intMinVal = 0;
			int intTermRightIndex;
			if (intMaxVal != 0) {
				intTermRightIndex = r.nextInt((intMaxVal - intMinVal) + 1) + intMinVal;
			} else {
				intTermRightIndex = 0;
			}
			
			objSession.intCurrentMultiplyTermLeft  = intTermLeftIndex;
			objSession.intCurrentMultiplyTermRight  = objSession.objData.objEnabledTables.get(intTermRightIndex);
		}
		SaveState();
	}
	
	



	public void ClearTestCounter() {
		objSession.intTestAmount = 0;
		objSession.intCorrectCount = 0;
	}
	
	public void IncrementTestCounter(boolean boolIsCorrect) {
		if (boolIsCorrect) {
			objSession.intCorrectCount += 1;			
		}
		objSession.intTestAmount += 1;		
	}
		
	public void UpdateTestCounterDisplay() {
		TextView objTestAmountTextView = (TextView) this.findViewById(R.id.label_test_amount);
		clsColoredStringBuilder objColoredStringBuilder = new clsColoredStringBuilder();
		objColoredStringBuilder.AddSnippetToString("Count: ", Color.BLUE);
		objColoredStringBuilder.AddSnippetToString(Integer.toString(objSession.intTestAmount), Color.BLACK);
		objColoredStringBuilder.AddSnippetToString(" Pass: ", Color.BLUE);
		objColoredStringBuilder.AddSnippetToString(Integer.toString(objSession.intCorrectCount), Color.BLUE);
		objColoredStringBuilder.AddSnippetToString(" Fail: ", Color.BLUE);
		int intFailCount = objSession.intTestAmount - objSession.intCorrectCount;
		objColoredStringBuilder.AddSnippetToString(Integer.toString(intFailCount),(intFailCount == 0) ? Color.BLACK: Color.RED);
		objTestAmountTextView.setText(objColoredStringBuilder.GetString());
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
			case SELECT_TABLES:
				String strListItems = data.getExtras().getString(ActivitySelectTables.LIST_ITEMS);
				java.lang.reflect.Type collectionType = new TypeToken<ArrayList<ActivitySelectTables.clsListItem>>(){}.getType();
				ArrayList<ActivitySelectTables.clsListItem> objListItems = clsUtils.DeSerializeFromString(strListItems, collectionType);
				objSession.objData.objEnabledTables.clear();
				for (ActivitySelectTables.clsListItem objListItem:objListItems) {
					if (objListItem.boolIsSelected) {
						objSession.objData.objEnabledTables.add(objListItem.intTable);
					}
				}
				GetRandomQuestion();
				SaveState();
				UpdateGui();
				break;
			}
		}
	}


	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		SaveState();

	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		LoadState();
	}
	
	void SaveState() {
		clsUtils.SerializeToSharedPreferences("ActivityDivideTrainer", "objSession", this, objSession);
	}
	

	void LoadState() {
		objContext = this;		
		objSession = clsUtils.DeSerializeFromSharedPreferences("ActivityDivideTrainer", "objSession", this, clsSession.class);
		
	}
}
