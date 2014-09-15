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
    private static final double values[] =
    {0.1, 0.2, 0.5, 1.0,
     2.0, 5.0, 10.0, 20.0,
     50.0, 100.0, 200.0, 500.0};
	
    private static final String strings[] =
    {"0.1 ms", "0.2 ms", "0.5 ms",
     "1.0 ms", "2.0 ms", "5.0 ms",
     "10 ms", "20 ms", "50 ms",
     "0.1 sec", "0.2 sec", "0.5 sec"};

    private static final int counts[] =
    {128, 256, 512, 1024,
     2048, 4096, 8192, 16384,
     32768, 65536, 131072, 262144};

    protected int timebase;

    private Scope scope;
    private XScale xscale;
    private Unit unit;

    private Audio audio;
    private Toast toast;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.activity_main);

	scope = (Scope)findViewById(R.id.scope);
	xscale = (XScale)findViewById(R.id.xscale);
	unit = (Unit)findViewById(R.id.unit);

	// Create audio

	audio = new Audio();

	if (scope != null)
	{
	    scope.main = this;
	    scope.audio = audio;
	}

	// Set up the click listeners

	setClickListeners();

	// Set timebase index

	timebase = 3;
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
	    // Bright line

	case R.id.bright:
	    audio.bright = !audio.bright;
	    item.setIcon(audio.bright? R.drawable.ic_action_bright_line_checked:
			 R.drawable.ic_action_bright_line);
	    showToast(audio.bright? R.string.bright_on: R.string.bright_off);
	    break;

	    // Single shot

	case R.id.single:
	    audio.single = !audio.single;
	    item.setIcon(audio.single? R.drawable.ic_action_single_shot_checked:
			 R.drawable.ic_action_single_shot);
	    showToast(audio.single? R.string.single_on: R.string.single_off);
	    break;

	    // Trigger

	case R.id.trigger:
	    if (audio.single)
		audio.trigger = true;
	    break;

	    // Sync polarity

	case R.id.polarity:
	    audio.polarity = !audio.polarity;
	    item.setIcon(audio.polarity? R.drawable.ic_action_polarity_checked:
			 R.drawable.ic_action_polarity);
	    showToast(audio.polarity? R.string.sync_pos: R.string.sync_neg);
	    break;

	    // Zoom in

	case R.id.zoom_in:
	    timebase--;
	    if (timebase < 0)
		timebase = 0;
	    showTimebase(timebase);
	    break;

	    // Zoom out

	case R.id.zoom_out:
	    timebase++;
	    if (timebase >= strings.length)
		timebase = strings.length - 1;
	    showTimebase(timebase);
	    break;

	    // Storage

	case R.id.storage:
	    if (scope != null)
	    {
		scope.storage = !scope.storage;
		item.setIcon(scope.storage?
			     R.drawable.ic_action_storage_checked:
			     R.drawable.ic_action_storage);
		showToast(scope.storage?
			  R.string.storage_on: R.string.storage_off);
	    }
	    break;

	    // Clear

	case R.id.clear:
	    if ((scope != null) && scope.storage)
		scope.clear = true;
	    break;

	    // Settings

	case R.id.action_settings:
	    return onSettingsClick(item);

	default:
	    return false;
	}

	return true;
    }

    // On settings click

    private boolean onSettingsClick(MenuItem item)
    {
	Intent intent = new Intent(this, SettingsActivity.class);
	startActivity(intent);

	return true;
    }

    // Show timebase

    void showTimebase(int timebase)
    {
	String text = "Timebase: " + strings[timebase];

	showToast(text);
    }

    // Show toast.

    void showToast(int key)
    {
	Resources resources = getResources();
	String text = resources.getString(key);

	showToast(text);
    }

    void showToast(String text)
    {
	// Cancel the last one

	if (toast != null)
	    toast.cancel();

	// Make a new one

	toast = Toast.makeText(this, text, Toast.LENGTH_SHORT);
	toast.setGravity(Gravity.CENTER, 0, 0);
	toast.show();
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

	PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

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

	protected boolean bright;
	protected boolean single;
	protected boolean trigger;
	protected boolean polarity;

	protected int input;
	protected int sample;

	// Data

	protected Thread thread;
	protected short data[];
	protected long length;

	// Private data

	private static final int SAMPLES = 262144;
	private static final int FRAMES = 4096;

	private static final int INIT  = 0;
	private static final int FIRST = 1;
	private static final int NEXT  = 2;
	private static final int LAST  = 3;

	private AudioRecord audioRecord;
	private short buffer[];

	// Constructor

	protected Audio()
	{
	    buffer = new short[FRAMES];
	    data = new short[SAMPLES];
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
		AudioRecord.getMinBufferSize(sample,
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
			    showAlert(R.string.app_name,
				      R.string.error_buffer);
			}
		    });

		thread = null;
		return;
	    }

	    // Create the AudioRecord object

	    audioRecord =
		new AudioRecord(input, sample,
				AudioFormat.CHANNEL_IN_MONO,
				AudioFormat.ENCODING_PCM_16BIT,
				size);

	    // Check audiorecord

	    if (audioRecord == null)
	    {
		runOnUiThread(new Runnable()
		    {
			@Override
			public void run()
			{
			    showAlert(R.string.app_name,
				      R.string.error_init);
			}
		    });

		thread = null;
		return;
	    }

	    // Check state

	    int state = audioRecord.getState(); 

	    if (state != AudioRecord.STATE_INITIALIZED)
	    {
		runOnUiThread(new Runnable()
		    {
			@Override
			public void run()
			{
			    showAlert(R.string.app_name,
				      R.string.error_init);
			}
		    });

		audioRecord.release();
		thread = null;
		return;
	    }

	    // Start recording

	    audioRecord.startRecording();

	    int index = 0;
	    int count = 0;

	    state = INIT;
	    short last = 0;

	    // Continue until the thread is stopped

	    while (thread != null)
	    {
		// Read a buffer of data

		size = audioRecord.read(buffer, 0, FRAMES);

		// Stop the thread if no data

		if (size == 0)
		{
		    thread = null;
		    break;
		}

		// State machine for sync and copying data to display buffer

		switch (state)
		{
		    // INIT: waiting for sync

		case INIT:

		    index = 0;

		    if (bright)
			state++;

		    else
		    {
			if (single && !trigger)
			    break;

			// Initialise sync

			int dx = 0;

			// Sync polarity

			if (polarity)
			{
			    for (int i = 0; i < size; i++)
			    {
				dx = buffer[i] - last;

				if (dx < 0 && last > 0 && buffer[i] < 0)
				{
				    index = i;
				    state++;
				    break;
				}

				last = buffer[i];
			    }
			}

			else
			{
			    for (int i = 0; i < size; i++)
			    {
				dx = buffer[i] - last;

				if (dx > 0 && last < 0 && buffer[i] > 0)
				{
				    index = i;
				    state++;
				    break;
				}

				last = buffer[i];
			    }
			}
		    }

		    // No sync, try next time

		    if (state == INIT)
			break;

		    // Reset trigger

		    if (single && trigger)
			trigger = false;

		    // FIRST: First chunk of data

		case FIRST:

		    // Update count

		    count = counts[timebase];
		    length = count;

		    // Copy data

		    System.arraycopy(buffer, index, data, 0, size - index);
		    index = size - index;

		    // If done, wait for sync again

		    if (index >= count)
			state = INIT;

		    // Else get some more data next time

		    else
			state++;
		    break;

		    // NEXT: Subsequent chunks of data

		case NEXT:

		    // Copy data

		    System.arraycopy(buffer, 0, data, index, size);
		    index += size;

		    // Done, wait for sync again

		    if (index >= count)
			state = INIT;

		    // Else if last but one chunk, get last chunk next time

		    else if (index + size >= count)
			state++;
		    break;

		    // LAST: Last chunk of data

		case LAST:

		    // Copy data

		    System.arraycopy(buffer, 0, data, index, count - index);

		    // Wait for sync next time

		    state = INIT;
		    break;
		}

		if (scope.scale != values[timebase])
		{
		    // Set up scale

		    scope.scale = (float)values[timebase];
		    xscale.scale = scope.scale;
		    xscale.step = 500 * xscale.scale;
		    unit.scale = scope.scale;

		    // Reset start

		    scope.start = 0;
		    xscale.start = 0;

		    // Update display

		    xscale.postInvalidate();
		    unit.postInvalidate();
		}

		// Update display

		scope.postInvalidate();
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
