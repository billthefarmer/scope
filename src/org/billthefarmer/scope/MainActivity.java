////////////////////////////////////////////////////////////////////////////////
//
//  Scope - An Android scope written in Java.
//
//  Copyright (C) 2013	Bill Farmer
//
//  This program is free software: you can redistribute it and/or modify
//  it under the terms of the GNU General Public License as published by
//  the Free Software Foundation, either version 3 of the License, or
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

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

public class MainActivity extends Activity
    implements OnClickListener
{
    private Scope scope;

    private Audio audio;
    private Toast toast;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.activity_main);

	scope = (Scope)findViewById(R.id.scope);

	// Create audio

	audio = new Audio();

	// Connect views to audio

	if (scope != null)
	    scope.audio = audio;

	// Set up the click listeners

	setClickListeners();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
	// Inflate the menu; this adds items to the action bar if it is present.
	getMenuInflater().inflate(R.menu.main, menu);
	return true;
    }

    // Set click listeners

    void setClickListeners()
    {
	// Scope

	if (scope != null)
	    scope.setOnClickListener(this);
    }

    // On click

    @Override
    public void onClick(View v)
    {
	// Get id

	int id = v.getId();
	switch (id)
	{
	    // Scope

	case R.id.scope:
	    // TODO
	    break;
	}
    }

    // On options item

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
	// Get id

	int id = item.getItemId();
	switch (id)
	{
	    // Settings

//	case R.id.settings:
//	    return onSettingsClick(item);

	default:
	    return false;
	}
    }

    // On settings click

    private boolean onSettingsClick(MenuItem item)
    {
//	Intent intent = new Intent(this, SettingsActivity.class);
//	startActivity(intent);

	return true;
    }

    // Show toast.

    void showToast(int key)
    {
	Resources resources = getResources();
	String text = resources.getString(key);

	// Cancel the last one

	if (toast != null)
	    toast.cancel();

	// Make a new one

	toast = Toast.makeText(this, text, Toast.LENGTH_SHORT);
	toast.setGravity(Gravity.CENTER, 0, 0);
	toast.show();

	// Update status

//	if (status != null)
//	    status.invalidate();
    }

    // On start

    @Override
    protected void onStart()
    {
	super.onStart();
    }

    // On Resume

    @Override
    protected void onResume()
    {
	super.onResume();

	// Get preferences

	getPreferences();

	// Update status

//	if (status != null)
//	    status.invalidate();

	// Start the audio thread

	audio.start();
    }

    @Override
    protected void onPause()
    {
	super.onPause();

	// Save preferences

	savePreferences();

	// Stop audio thread

	audio.stop();
    }

    // On stop

    @Override
    protected void onStop()
    {
	super.onStop();
    }

    // Save preferences

    void savePreferences()
    {
	SharedPreferences preferences =
	    PreferenceManager.getDefaultSharedPreferences(this);

	Editor editor = preferences.edit();
	// TODO
	editor.commit();
    }

    // Get preferences

    void getPreferences()
    {
	// Load preferences

//	PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

	SharedPreferences preferences =
	    PreferenceManager.getDefaultSharedPreferences(this);

	// Set preferences

	// TODO
    }

    // Show alert

    void showAlert(int appName, int errorBuffer)
    {
	// Create an alert dialog builder

	AlertDialog.Builder builder =
	    new AlertDialog.Builder(this);

	// Set the title, message and button

	builder.setTitle(appName);
	builder.setMessage(errorBuffer);
	builder.setNeutralButton(android.R.string.ok,
				 new DialogInterface.OnClickListener()
				 {				
				     @Override
				     public void onClick(DialogInterface dialog,
							 int which)
				     {
					 // Dismiss dialog

					 dialog.dismiss();	
				     }
				 });
	// Create the dialog

	AlertDialog dialog = builder.create();

	// Show it

	dialog.show();
    }

    // Audio

    protected class Audio implements Runnable
    {
	// Preferences

	protected int input;
	protected double sample;

	// Data

	protected Thread thread;
	protected double buffer[];
	protected short data[];

	// Private data

	private AudioRecord audioRecord;

	// Constructor

	protected Audio()
	{
//	    buffer = new double[SAMPLES];
//	    data = new short[STEP];
	}

	// Start audio

	protected void start()
	{
	    // Start the thread

	    thread = new Thread(this, "Audio");
	    thread.start();
	}

	// Run

	@Override
	public void run()
	{
	    processAudio();
	}

	// Stop

	protected void stop()
	{
	    Thread t = thread;
	    thread = null;

	    // Wait for the thread to exit

	    while (t != null && t.isAlive())
		Thread.yield();
	}

	// Process Audio

	protected void processAudio()
	{
	    // Assume the output sample will work on the input as
	    // there isn't a AudioRecord.getNativeInputSampleRate()

	    sample =
		AudioTrack.getNativeOutputSampleRate(AudioManager.STREAM_MUSIC);

	    // Get buffer size

	    int size =
		AudioRecord.getMinBufferSize((int)sample,
					     AudioFormat.CHANNEL_IN_MONO,
					     AudioFormat.ENCODING_PCM_16BIT);
	    // Give up if it doesn't work

	    if (size == AudioRecord.ERROR_BAD_VALUE ||
		size == AudioRecord.ERROR ||
		size <= 0)
	    {
		runOnUiThread(new Runnable()
		    {
			@Override
			public void run()
			{
//			    showAlert(R.string.app_name,
//				      R.string.error_buffer);
			}
		    });

		thread = null;
		return;
	    }

	    // Create the AudioRecord object

	    audioRecord =
		new AudioRecord(input, (int)sample,
				AudioFormat.CHANNEL_IN_MONO,
				AudioFormat.ENCODING_PCM_16BIT,
				size);
	    // Check state

	    int state = audioRecord.getState(); 

	    if (state != AudioRecord.STATE_INITIALIZED)
	    {
		runOnUiThread(new Runnable()
		    {
			@Override
			public void run()
			{
//			    showAlert(R.string.app_name, R.string.error_init);
			}
		    });

		audioRecord.release();
		thread = null;
		return;
	    }

	    // Start recording

	    audioRecord.startRecording();

	    // Max data

	    double dmax = 0.0;

	    // Continue until the thread is stopped

	    while (thread != null)
	    {
		// Read a buffer of data

		size = audioRecord.read(data, 0, size);

		// Stop the thread if no data

		if (size == 0)
		{
		    thread = null;
		    break;
		}

		// If display not locked update scope

//		if (scope != null && !lock)
//		    scope.postInvalidate();

		// Move the main data buffer up

//		System.arraycopy(buffer, STEP, buffer, 0, SAMPLES - STEP);

		// Max signal

		double rm = 0;

		// Signal value

//		rm /= STEP;
//		signal = (float)Math.sqrt(rm);

		// Maximum value

		if (dmax < 4096.0)
		    dmax = 4096.0;

		// Calculate normalising value

		double norm = dmax;

		dmax = 0.0;
	    }

	    // Stop and release the audio recorder

	    if (audioRecord != null)
	    {
		audioRecord.stop();
		audioRecord.release();
	    }
	}
    }
}
