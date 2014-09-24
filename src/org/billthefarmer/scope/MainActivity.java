////////////////////////////////////////////////////////////////////////////////
//
//  Scope - An Android scope written in Java.
//
//  Copyright (C) 2014	Bill Farmer
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
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.widget.Toast;

public class MainActivity extends Activity
{
    private static final float values[] =
    {0.1f, 0.2f, 0.5f, 1.0f,
     2.0f, 5.0f, 10.0f, 20.0f,
     50.0f, 100.0f, 200.0f, 500.0f};
	
    private static final String strings[] =
    {"0.1 ms", "0.2 ms", "0.5 ms",
     "1.0 ms", "2.0 ms", "5.0 ms",
     "10 ms", "20 ms", "50 ms",
     "0.1 sec", "0.2 sec", "0.5 sec"};

    private static final int counts[] =
    {256, 512, 1024, 2048,
     4096, 8192, 16384, 32768,
     65536, 131072, 262144, 524288};

    protected static final int SIZE = 20;
    protected static final float SMALL_SCALE = 200;
    protected static final float LARGE_SCALE = 200000;

    protected int timebase;

    private Scope scope;
    private XScale xscale;
    private Unit unit;

    private Audio audio;
    private Toast toast;
    private SubMenu submenu;

    // On create

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

	// Set timebase index

	timebase = 3;

	// Set up scale

	if (scope != null && xscale != null && unit != null)
	{
	    scope.scale = values[timebase];
	    xscale.scale = scope.scale;
	    xscale.step = 1000 * xscale.scale;
	    unit.scale = scope.scale;
	}
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
	// Inflate the menu; this adds items to the action bar if it is present.
	getMenuInflater().inflate(R.menu.main, menu);
	return true;
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
	    showToast(!audio.polarity? R.string.sync_pos: R.string.sync_neg);
	    break;

	    // Timebase

	case R.id.timebase:
	    if (item.hasSubMenu())
		submenu = item.getSubMenu();
	    break;

	    // 0.1 ms

	case R.id.tb0_1ms:
	    clearLast(submenu, timebase);
	    timebase = 0;
	    item.setChecked(true);
	    setTimebase(timebase);
	    break;

	    // 0.2 ms

	case R.id.tb0_2ms:
	    clearLast(submenu, timebase);
	    timebase = 1;
	    item.setChecked(true);
	    setTimebase(timebase);
	    break;

	    // 0.5 ms

	case R.id.tb0_5ms:
	    clearLast(submenu, timebase);
	    timebase = 2;
	    item.setChecked(true);
	    setTimebase(timebase);
	    break;

	    // 1.0 ms

	case R.id.tb1_0ms:
	    clearLast(submenu, timebase);
	    timebase = 3;
	    item.setChecked(true);
	    setTimebase(timebase);
	    break;

	    // 2.0 ms

	case R.id.tb2_0ms:
	    clearLast(submenu, timebase);
	    timebase = 4;
	    item.setChecked(true);
	    setTimebase(timebase);
	    break;

	    // 5.0 ms

	case R.id.tb5_0ms:
	    clearLast(submenu, timebase);
	    timebase = 5;
	    item.setChecked(true);
	    setTimebase(timebase);
	    break;

	    // 10 ms

	case R.id.tb10ms:
	    clearLast(submenu, timebase);
	    timebase = 6;
	    item.setChecked(true);
	    setTimebase(timebase);
	    break;

	    // 20 ms

	case R.id.tb20ms:
	    clearLast(submenu, timebase);
	    timebase = 7;
	    item.setChecked(true);
	    setTimebase(timebase);
	    break;

	    // 50 ms

	case R.id.tb50ms:
	    clearLast(submenu, timebase);
	    timebase = 8;
	    item.setChecked(true);
	    setTimebase(timebase);
	    break;

	    // 0.1 sec

	case R.id.tb0_1sec:
	    clearLast(submenu, timebase);
	    timebase = 9;
	    item.setChecked(true);
	    setTimebase(timebase);
	    break;

	    // 0.2 sec

	case R.id.tb0_2sec:
	    clearLast(submenu, timebase);
	    timebase = 10;
	    item.setChecked(true);
	    setTimebase(timebase);
	    break;

	    // 0.5 sec

	case R.id.tb0_5sec:
	    clearLast(submenu, timebase);
	    timebase = 11;
	    item.setChecked(true);
	    setTimebase(timebase);
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

	    // Left

	case R.id.left:
	    if (scope != null && xscale != null)
	    {
		scope.start -= xscale.step;
		if (scope.start < 0)
		    scope.start = 0;

		xscale.start = scope.start;
		xscale.postInvalidate();
	    }
	    break;

	    // Right

	case R.id.right:
	    if (scope != null && xscale != null)
	    {
		scope.start += xscale.step;
		if (scope.start >= audio.length)
		    scope.start -= xscale.step;

		xscale.start = scope.start;
		xscale.postInvalidate();
	    }
	    break;

	    // Start

	case R.id.start:
	    if (scope != null && xscale != null)
	    {
		scope.start = 0;
		scope.index = 0;
		xscale.start = 0;
		xscale.postInvalidate();
	    }
	    break;

	    // End

	case R.id.end:
	    if (scope != null && xscale != null)
	    {
		while (scope.start < audio.length)
		    scope.start += xscale.step;
		scope.start -= xscale.step;
		xscale.start = scope.start;
		xscale.postInvalidate();
	    }
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

    // Clear last

    void clearLast(SubMenu submenu, int timebase)
    {
	// Clear last submenu item tickbox

	if (submenu != null)
	{
	    MenuItem last =  submenu.getItem(timebase);

	    if (last != null)
		last.setChecked(false);
	}
    }

    // Set timebase

    void setTimebase(int timebase)
    {
	// Set up scale

	scope.scale = values[timebase];
	xscale.scale = scope.scale;
	xscale.step = 1000 * xscale.scale;
	unit.scale = scope.scale;

	// Reset start

	scope.start = 0;
	xscale.start = 0;

	// Update display

	xscale.postInvalidate();
	unit.postInvalidate();

	// Show timebase

	showTimebase(timebase);
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

	// Start the audio thread

	audio.start();
    }

    @Override
    protected void onPause()
    {
	super.onPause();

	// Stop audio thread

	audio.stop();
    }

    // On stop

    @Override
    protected void onStop()
    {
	super.onStop();
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

	private static final int SAMPLES = 524288;
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
	    // there isn't an AudioRecord.getNativeInputSampleRate()

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
