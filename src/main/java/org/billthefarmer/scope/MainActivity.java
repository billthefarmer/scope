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
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

// MainActivity
public class MainActivity extends Activity
{
    private static final String PREF_INPUT = "pref_input";
    private static final String PREF_SCREEN = "pref_screen";

    private static final String TAG = "Scope";

    private static final String STATE = "state";

    private static final String BRIGHT = "bright";
    private static final String SINGLE = "single";
    private static final String TIMEBASE = "timebase";
    private static final String STORAGE = "storage";

    private static final String START = "start";
    private static final String INDEX = "index";
    private static final String LEVEL = "level";

    private static final float values[] =
    {
        0.1f, 0.2f, 0.5f, 1.0f,
        2.0f, 5.0f, 10.0f, 20.0f,
        50.0f, 100.0f, 200.0f, 500.0f
    };

    private static final String strings[] =
    {
        "0.1 ms", "0.2 ms", "0.5 ms",
        "1.0 ms", "2.0 ms", "5.0 ms",
        "10 ms", "20 ms", "50 ms",
        "0.1 sec", "0.2 sec", "0.5 sec"
    };

    private static final int counts[] =
    {
        256, 512, 1024, 2048,
        4096, 8192, 16384, 32768,
        65536, 131072, 262144, 524288
    };

    protected static final int SIZE = 20;
    protected static final int DEFAULT_TIMEBASE = 3;
    protected static final float SMALL_SCALE = 200;
    protected static final float LARGE_SCALE = 200000;

    protected int timebase;

    private Scope scope;
    private XScale xscale;
    private YScale yscale;
    private Unit unit;

    private Audio audio;
    private Toast toast;
    private SubMenu submenu;

    private boolean screen;

    // On create
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        scope = (Scope)findViewById(R.id.scope);
        xscale = (XScale)findViewById(R.id.xscale);
        yscale = (YScale)findViewById(R.id.yscale);
        unit = (Unit)findViewById(R.id.unit);

        // Get action bar
        ActionBar actionBar = getActionBar();

        // Set short title
        if (actionBar != null)
            actionBar.setTitle(R.string.short_name);

        // Create audio
        audio = new Audio();

        if (scope != null)
            scope.audio = audio;

        // Set timebase index
        timebase = DEFAULT_TIMEBASE;

        // Set up scale
        if (scope != null && xscale != null && unit != null)
        {
            scope.scale = values[timebase];
            xscale.scale = scope.scale;
            xscale.step = 1000 * xscale.scale;
            unit.scale = scope.scale;
        }
    }

    // onCreateOptionsMenu
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuItem item;

        // Inflate the menu; this adds items to the action bar if it
        // is present.
        getMenuInflater().inflate(R.menu.main, menu);

        // Set menu state from restored state

        // Bright
        item = menu.findItem(R.id.bright);
        item.setIcon(audio.bright ? R.drawable.bright_checked :
                     R.drawable.action_bright);

        // Single
        item = menu.findItem(R.id.single);
        item.setIcon(audio.single ? R.drawable.single_checked :
                     R.drawable.action_single);

        // Timebase
        item = menu.findItem(R.id.timebase);
        if (timebase != DEFAULT_TIMEBASE)
        {
            if (item.hasSubMenu())
            {
                submenu = item.getSubMenu();
                item = submenu.getItem(timebase);
                if (item != null)
                    item.setChecked(true);
            }
        }

        // Storage
        item = menu.findItem(R.id.storage);
        item.setIcon(scope.storage ?
                     R.drawable.storage_checked :
                     R.drawable.action_storage);

        return true;
    }

    // Restore state
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState)
    {
        super.onRestoreInstanceState(savedInstanceState);

        // Get saved state bundle
        Bundle bundle = savedInstanceState.getBundle(STATE);

        // Bright
        audio.bright = bundle.getBoolean(BRIGHT, false);

        // Single
        audio.single = bundle.getBoolean(SINGLE, false);

        // Timebase
        timebase = bundle.getInt(TIMEBASE, DEFAULT_TIMEBASE);
        setTimebase(timebase, false);

        // Storage
        scope.storage = bundle.getBoolean(STORAGE, false);

        // Start
        scope.start = bundle.getFloat(START, 0);
        xscale.start = scope.start;
        xscale.postInvalidate();

        // Index
        scope.index = bundle.getFloat(INDEX, 0);

        // Level
        yscale.index = bundle.getFloat(LEVEL, 0);
        yscale.postInvalidate();
    }

    // Save state
    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);

        // State bundle
        Bundle bundle = new Bundle();

        // Bright
        bundle.putBoolean(BRIGHT, audio.bright);

        // Single
        bundle.putBoolean(SINGLE, audio.single);

        // Timebase
        bundle.putInt(TIMEBASE, timebase);

        // Storage
        bundle.putBoolean(STORAGE, scope.storage);

        // Start
        bundle.putFloat(START, scope.start);

        // Index
        bundle.putFloat(INDEX, scope.index);

        // Level
        bundle.putFloat(LEVEL, yscale.index);

        // Save bundle
        outState.putBundle(STATE, bundle);
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
            item.setIcon(audio.bright ?
                         R.drawable.bright_checked :
                         R.drawable.action_bright);
            showToast(audio.bright ? R.string.bright_on : R.string.bright_off);
            break;

        // Single shot
        case R.id.single:
            audio.single = !audio.single;
            item.setIcon(audio.single ?
                         R.drawable.single_checked :
                         R.drawable.action_single);
            showToast(audio.single ? R.string.single_on : R.string.single_off);
            break;

        // Trigger
        case R.id.trigger:
            if (audio.single)
                audio.trigger = true;
            break;

        // Timebase
        case R.id.timebase:
            if (item.hasSubMenu())
                submenu = item.getSubMenu();
            break;

        // 0.1 ms
        case R.id.tb0_1ms:
            timebase = 0;
            item.setChecked(true);
            setTimebase(timebase, true);
            break;

        // 0.2 ms
        case R.id.tb0_2ms:
            timebase = 1;
            item.setChecked(true);
            setTimebase(timebase, true);
            break;

        // 0.5 ms
        case R.id.tb0_5ms:
            timebase = 2;
            item.setChecked(true);
            setTimebase(timebase, true);
            break;

        // 1.0 ms
        case R.id.tb1_0ms:
            timebase = 3;
            item.setChecked(true);
            setTimebase(timebase, true);
            break;

        // 2.0 ms
        case R.id.tb2_0ms:
            timebase = 4;
            item.setChecked(true);
            setTimebase(timebase, true);
            break;

        // 5.0 ms
        case R.id.tb5_0ms:
            timebase = 5;
            item.setChecked(true);
            setTimebase(timebase, true);
            break;

        // 10 ms
        case R.id.tb10ms:
            timebase = 6;
            item.setChecked(true);
            setTimebase(timebase, true);
            break;

        // 20 ms
        case R.id.tb20ms:
            timebase = 7;
            item.setChecked(true);
            setTimebase(timebase, true);
            break;

        // 50 ms

        case R.id.tb50ms:
            timebase = 8;
            item.setChecked(true);
            setTimebase(timebase, true);
            break;

        // 0.1 sec
        case R.id.tb0_1sec:
            timebase = 9;
            item.setChecked(true);
            setTimebase(timebase, true);
            break;

        // 0.2 sec
        case R.id.tb0_2sec:
            timebase = 10;
            item.setChecked(true);
            setTimebase(timebase, true);
            break;

        // 0.5 sec
        case R.id.tb0_5sec:
            timebase = 11;
            item.setChecked(true);
            setTimebase(timebase, true);
            break;

        // Storage
        case R.id.storage:
            if (scope != null)
            {
                scope.storage = !scope.storage;
                item.setIcon(scope.storage ?
                             R.drawable.storage_checked :
                             R.drawable.action_storage);
                showToast(scope.storage ?
                          R.string.storage_on : R.string.storage_off);
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
                yscale.index = 0;
                yscale.postInvalidate();
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

        // Spectrum
        case R.id.action_spectrum:
            return onSpectrumClick(item);

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

    // On spectrum click
    private boolean onSpectrumClick(MenuItem item)
    {
        Intent intent = new Intent(this, SpectrumActivity.class);
        startActivity(intent);

        return true;
    }

    // Set timebase
    void setTimebase(int timebase, boolean show)
    {
        if (scope != null && xscale != null && unit != null)
        {
            // Set up scale
            scope.scale = values[timebase];
            xscale.scale = scope.scale;
            xscale.step = 1000 * xscale.scale;
            unit.scale = scope.scale;

            // Set up scope points
            if (timebase == 0)
                scope.points = true;

            else
                scope.points = false;

            // Reset start
            scope.start = 0;
            xscale.start = 0;

            // Update display
            xscale.postInvalidate();
            unit.postInvalidate();
        }

        // Show timebase
        if (show)
            showTimebase(timebase);
    }

    // Show timebase
    void showTimebase(int timebase)
    {
        String text = "Timebase: " + strings[timebase];

        showToast(text);
    }

    // Show toast
    void showToast(int key)
    {
        Resources resources = getResources();
        String text = resources.getString(key);

        showToast(text);
    }

    // Show toast
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

    // On pause
    @Override
    protected void onPause()
    {
        super.onPause();

        // Save preferences
        savePreferences();

        // Stop audio thread
        audio.stop();
    }

    // Get preferences
    void getPreferences()
    {
        // Load preferences
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        SharedPreferences preferences =
            PreferenceManager.getDefaultSharedPreferences(this);

        // Set preferences
        if (audio != null)
        {
            audio.input =
                Integer.parseInt(preferences.getString(PREF_INPUT, "0"));
        }

        screen = preferences.getBoolean(PREF_SCREEN, false);

        // Check screen
        Window window = getWindow();
        if (screen)
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        else
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    // Save preferences
    void savePreferences()
    {
        SharedPreferences preferences =
            PreferenceManager.getDefaultSharedPreferences(this);

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
            // Stop and release the audio recorder
            cleanUpAudioRecord();

            Thread t = thread;
            thread = null;

            // Wait for the thread to exit
            while (t != null && t.isAlive())
                Thread.yield();
        }

        // Stop and release the audio recorder
        private void cleanUpAudioRecord()
        {
            if (audioRecord != null &&
                    audioRecord.getState() == AudioRecord.STATE_INITIALIZED)
            {
                try
                {
                    if (audioRecord.getRecordingState() ==
                        AudioRecord.RECORDSTATE_RECORDING)
                        audioRecord.stop();

                    audioRecord.release();
                }

                catch (Exception e) {}
            }
        }

        // Process Audio
        protected void processAudio()
        {
            // Assume the output sample rate will work on the input as
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
            try
            {
                audioRecord =
                    new AudioRecord(input, sample,
                                    AudioFormat.CHANNEL_IN_MONO,
                                    AudioFormat.ENCODING_PCM_16BIT,
                                    size);
            }

            // Exception
            catch (Exception e)
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

            // Continue until he thread is stopped
            while (thread != null)
            {
                // Read a buffer of data
                size = audioRecord.read(buffer, 0, FRAMES);

                // Stop the thread if no data or error state
                if (size <= 0)
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

                        // Calculate sync level
                        float level = -yscale.index * scope.yscale;

                        // Initialise sync
                        int dx = 0;

                        // Sync polarity
                        if (level < 0)
                        {
                            for (int i = 0; i < size; i++)
                            {
                                dx = buffer[i] - last;

                                if (dx < 0 && last > level && buffer[i] < level)
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

                                if (dx > 0 && last < level && buffer[i] > level)
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
            cleanUpAudioRecord();
        }
    }
}
