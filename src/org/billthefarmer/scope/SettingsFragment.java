////////////////////////////////////////////////////////////////////////////////
//
//  Scope - An Android scope written in Java.
//
//  Copyright (C) 2014	Bill Farmer
//
//  This program is free software; you can redistribute it and/or modify
//  it under the terms of the GNU General Public License as published by
//  the Free Software Foundation; either version 3 of the License, or
//  (at your option) any later version.
//
//  This program is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//  GNU General Public License for more details.
//
//  You should have received a copy of the GNU General Public License
//  along with this program.  If not, see <http://www.gnu.org/licenses/>.
//
//  Bill Farmer	 william j farmer [at] yahoo [dot] co [dot] uk.
//
///////////////////////////////////////////////////////////////////////////////

package org.billthefarmer.scope;

import android.app.ActionBar;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;

public class SettingsFragment extends PreferenceFragment
{
    private static final String KEY_PREF_ABOUT = "pref_about";

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
	super.onCreate(savedInstanceState);

	// Load the preferences from an XML resource

	addPreferencesFromResource(R.xml.preferences);

	SharedPreferences preferences =
	    PreferenceManager.getDefaultSharedPreferences(getActivity());

	// Get about summary

	Preference about = findPreference(KEY_PREF_ABOUT);
	String sum = (String) about.getSummary();

	// Get context and package manager

	Context context = getActivity();
	PackageManager manager = context.getPackageManager();

	// Get info

	PackageInfo info = null;
	try
	{
	    info = manager.getPackageInfo("org.billthefarmer.scope", 0);
	}
	
	catch (NameNotFoundException e)
	{
	    e.printStackTrace();
	}

	// Set version in text view

	if (info != null)
	{
	    String s = String.format(sum, info.versionName);
	    about.setSummary(s);
	}
    }

    // On preference tree click

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
					 Preference preference)
    {
    	boolean result =
	    super.onPreferenceTreeClick(preferenceScreen, preference);

    	if (preference instanceof PreferenceScreen)
    	{
	    Dialog dialog = ((PreferenceScreen)preference).getDialog();
	    ActionBar actionBar = dialog.getActionBar();
	    actionBar.setDisplayHomeAsUpEnabled(false);
    	}

    	return result;
    }
}
