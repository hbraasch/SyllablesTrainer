package com.treeapps.syllablestrainer.utils;

import java.util.ArrayList;
import java.util.List;

import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;

public class clsColoredStringBuilder {
	
	private List<clsSnippet> objSnippets = new ArrayList<clsSnippet>();
	
	class clsSnippet {
		public String strValue;
		public int intColour;
		
		public clsSnippet(String strSnippet, int intColour) {
			this.strValue = strSnippet;
			this.intColour = intColour;
		}
	}

	public void AddSnippetToString(String strSnippet, int intColour) {
		objSnippets.add(new clsSnippet(strSnippet, intColour));
	}
	
	public void Clear() {
		objSnippets.clear();
	}
	
	public SpannableStringBuilder GetString() {
		SpannableStringBuilder builder = new SpannableStringBuilder();
		// Add word to sentence, calculating positions on the fly
		boolean boolIsFirst = true;
		for (clsSnippet objSnippet: objSnippets ){			
			if (boolIsFirst) {
				boolIsFirst = false;
				SpannableString spanLine = new SpannableString(objSnippet.strValue);
				spanLine.setSpan(new ForegroundColorSpan(objSnippet.intColour), 0, objSnippet.strValue.length(), 0);
				builder.append(spanLine);
			} else {
				SpannableString spanLine = new SpannableString(objSnippet.strValue);
				spanLine.setSpan(new ForegroundColorSpan(objSnippet.intColour), 0, objSnippet.strValue.length(), 0);
				builder.append(spanLine);
			}
		}
		return builder;
	}

	public boolean isEmpty() {
		if (objSnippets.size() == 0) return true;
		return false;
	}
	
}
