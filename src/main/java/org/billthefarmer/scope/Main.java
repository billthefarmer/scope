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

import android.Manifest;
import android.animation.ValueAnimator;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.widget.PopupMenu;
import android.widget.Toast;
import android.widget.Toolbar;

// Main
@SuppressWarnings("deprecation")
public class Main extends Activity
    implements PopupMenu.OnMenuItemClickListener
{
    public static final String PREF_ABOUT = "pref_about";
    public static final String PREF_THEME = "pref_theme";
    public static final String PREF_INPUT = "pref_input";
    public static final String PREF_SCREEN = "pref_screen";
    public static final String PREF_BRIGHT = "pref_bright";
    public static final String PREF_SINGLE = "pref_single";
    public static final String PREF_STORAGE = "pref_storage";
    public static final String PREF_TIMEBASE = "pref_timebase";

    private static final String TAG = "Scope";

    private static final String BRIGHT = "bright";
    private static final String SINGLE = "single";
    private static final String STORAGE = "storage";
    private static final String TIMEBASE = "timebase";

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

    private static final int REQUEST_PERMISSIONS = 1;

    public static final int VERSION_CODE_S_V2 = 32;

    public static final int LIGHT  = 0;
    public static final int DARK   = 1;
    public static final int SYSTEM = 2;

    protected static final int SIZE = 20;
    protected static final int DEFAULT_TIMEBASE = 3;
    protected static final float SMALL_SCALE = 200;
    protected static final float LARGE_SCALE = 200000;

    protected int timebase;

    private GestureDetector gestureDetector;
    private ScaleGestureDetector scaleDetector;

    private Toolbar toolbar;
    private Scope scope;
    private XScale xscale;
    private YScale yscale;
    private Unit unit;

    private Audio audio;
    private Toast toast;

    // private boolean dark;
    private int theme;

    // On create
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // Get preferences
        getPreferences();

        Configuration config = getResources().getConfiguration();
        int night = config.uiMode & Configuration.UI_MODE_NIGHT_MASK;

        switch (theme)
        {
        case LIGHT:
            setTheme(R.style.AppTheme);
            break;

        case DARK:
            setTheme(R.style.AppDarkTheme);
            break;

        case SYSTEM:
            switch (night)
            {
            case Configuration.UI_MODE_NIGHT_NO:
                setTheme(R.style.AppTheme);
                break;

            case Configuration.UI_MODE_NIGHT_YES:
                setTheme(R.style.AppDarkTheme);
                break;
            }
            break;
        }

        setContentView(R.layout.activity_main);

        scope = findViewById(R.id.scope);
        xscale = findViewById(R.id.xscale);
        yscale = findViewById(R.id.yscale);
        unit = findViewById(R.id.unit);

        // Set short title
        if (config.orientation == Configuration.ORIENTATION_PORTRAIT)
            setTitle(R.string.short_name);

        // Find toolbar
        toolbar = findViewById(getResources().getIdentifier("action_bar",
                                                            "id", "android"));
        // Set up navigation
        toolbar.setNavigationIcon(R.drawable.ic_menu_white_24dp);
        toolbar.setNavigationOnClickListener((v) ->
        {
            PopupMenu popup = new PopupMenu(this, v);
            popup.inflate(R.menu.navigation);
            popup.setOnMenuItemClickListener(this);
            popup.show();
        });

        // Create audio
        audio = new Audio();

        if (scope != null)
        {
            scope.audio = audio;
        }

        // Set timebase index
        timebase = DEFAULT_TIMEBASE;

        // Set up scale
        if (scope != null && xscale != null && unit != null)
        {
            scope.xscale = values[timebase];
            xscale.scale = scope.xscale;
            xscale.step = 1000 * xscale.scale;
            unit.scale = scope.xscale;
        }

        // Set up gesture detectors
        gestureDetector =
            new GestureDetector(this, new GestureListener());
        scaleDetector =
            new ScaleGestureDetector(this, new ScaleListener());

        if (scope != null)
            scope.setOnTouchListener((v, event) ->
            {
                scaleDetector.onTouchEvent(event);
                gestureDetector.onTouchEvent(event);
                return true;
            });
    }

    // onCreateOptionsMenu
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it
        // is present.
        getMenuInflater().inflate(R.menu.main, menu);

        return true;
    }

    // onPrepareOptionsMenu
    @Override
    public boolean onPrepareOptionsMenu(Menu menu)
    {
        // Bright
        menu.findItem(R.id.bright).setIcon(audio.bright?
                                           R.drawable.bright_checked :
                                           R.drawable.action_bright);

        // Single
        menu.findItem(R.id.single).setIcon(audio.single?
                                           R.drawable.single_checked :
                                           R.drawable.action_single);

        // Timebase
        if (timebase != DEFAULT_TIMEBASE)
        {
            MenuItem item = menu.findItem(R.id.timebase);
            if (item.hasSubMenu())
            {
                SubMenu submenu = item.getSubMenu();
                item = submenu.getItem(timebase);
                if (item != null)
                    item.setChecked(true);
            }
        }

        // Storage
        menu.findItem(R.id.storage).setIcon(scope.storage ?
                                            R.drawable.storage_checked :
                                            R.drawable.action_storage);
        menu.findItem(R.id.storage).setChecked(scope.storage);

        return true;
    }

    // Restore state
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState)
    {
        super.onRestoreInstanceState(savedInstanceState);

        // Bright
        audio.bright = savedInstanceState.getBoolean(BRIGHT, false);

        // Single
        audio.single = savedInstanceState.getBoolean(SINGLE, false);

        // Timebase
        timebase = savedInstanceState.getInt(TIMEBASE, DEFAULT_TIMEBASE);
        setTimebase(timebase, false);

        // Storage
        scope.storage = savedInstanceState.getBoolean(STORAGE, false);

        // Start
        scope.start = savedInstanceState.getFloat(START, 0);
        xscale.start = scope.start;
        xscale.invalidate();

        // Index
        scope.index = savedInstanceState.getFloat(INDEX, 0);

        // Level
        yscale.index = savedInstanceState.getFloat(LEVEL, 0);
        yscale.invalidate();

        invalidateOptionsMenu();
    }

    // Save state
    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);

        // Bright
        outState.putBoolean(BRIGHT, audio.bright);

        // Single
        outState.putBoolean(SINGLE, audio.single);

        // Timebase
        outState.putInt(TIMEBASE, timebase);

        // Storage
        outState.putBoolean(STORAGE, scope.storage);

        // Start
        outState.putFloat(START, scope.start);

        // Index
        outState.putFloat(INDEX, scope.index);

        // Level
        outState.putFloat(LEVEL, yscale.index);
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
                xscale.invalidate();
            }
            break;

        // Right
        case R.id.right:
            if (scope != null && xscale != null)
            {
                scope.start += xscale.step;
                if (scope.start > audio.length)
                    scope.start = audio.length - (xscale.step / 4);

                xscale.start = scope.start;
                xscale.invalidate();
            }
            break;

        // Start
        case R.id.start:
            if (scope != null && xscale != null)
            {
                scope.start = 0;
                scope.index = 0;
                xscale.start = 0;
                xscale.invalidate();
                yscale.index = 0;
                yscale.invalidate();
                setTimebase(timebase, false);
            }
            break;

        // End
        case R.id.end:
            if (scope != null && xscale != null)
            {
                scope.start = audio.length - (xscale.step / 4);
                xscale.start = scope.start;
                xscale.invalidate();
            }
            break;

        // Spectrum
        case R.id.action_spectrum:
            return onSpectrumClick(item);

        // Help
        case R.id.action_help:
            return onHelpClick(item);

        // Settings
        case R.id.action_settings:
            return onSettingsClick(item);

        default:
            return false;
        }

        return true;
    }

    // onMenuItemClick
    @Override
    public boolean onMenuItemClick(MenuItem item)
    {
        // Get id
        int id = item.getItemId();
        switch (id)
        {
        // Spectrum
        case R.id.action_spectrum:
            return onSpectrumClick(item);

        // Help
        case R.id.action_help:
            return onHelpClick(item);

        // Settings
        case R.id.action_settings:
            return onSettingsClick(item);

        default:
            return false;
        }
    }

    // On settings click
    private boolean onSettingsClick(MenuItem item)
    {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);

        return true;
    }

    // On help click
    private boolean onHelpClick(MenuItem item)
    {
        Intent intent = new Intent(this, HelpActivity.class);
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
            scope.xscale = values[timebase];
            xscale.scale = scope.xscale;
            xscale.step = 1000 * xscale.scale;
            unit.scale = scope.xscale;

            // Set up scope points
            scope.points = timebase == 0;

            // Reset start
            scope.start = 0;
            xscale.start = 0;

            // Update display
            xscale.invalidate();
            unit.invalidate();
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
        String text = getString(key);

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
        // Fix for android 13
        View view = toast.getView();
        if (view != null && Build.VERSION.SDK_INT > VERSION_CODE_S_V2)
            view.setBackgroundResource(R.drawable.toast_frame);
        toast.show();
    }

    // On Resume
    @Override
    protected void onResume()
    {
        super.onResume();

        int last = theme;

        // Get preferences
        getPreferences();

        if (last != theme)
        {
            if (Build.VERSION.SDK_INT != Build.VERSION_CODES.M)
                recreate();
        }

        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED)
        {
            requestPermissions(new String[]
            {Manifest.permission.RECORD_AUDIO}, REQUEST_PERMISSIONS);
            return;
        }

        // Start the audio thread
        audio.start();
    }

    // onRequestPermissionsResult
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions,
                                           int[] grantResults)
    {
        if (requestCode == REQUEST_PERMISSIONS)
        {
            for (int i = 0; i < grantResults.length; i++)
                if (permissions[i].equals(Manifest.permission.RECORD_AUDIO) &&
                    grantResults[i] == PackageManager.PERMISSION_GRANTED)
                {
                    // Granted, recreate or start audio thread
                    if (Build.VERSION.SDK_INT != Build.VERSION_CODES.M)
                        recreate();

                    else
                        audio.start();
                }
        }
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
        SharedPreferences preferences =
            PreferenceManager.getDefaultSharedPreferences(this);

        // Set preferences
        if (audio != null)
        {
            audio.input =
                Integer.parseInt(preferences.getString(PREF_INPUT, "0"));
            audio.bright = preferences.getBoolean(PREF_BRIGHT, false);
            audio.single = preferences.getBoolean(PREF_SINGLE, false);
        }

        if (scope != null)
        {
            scope.storage = preferences.getBoolean(PREF_STORAGE, false);
            timebase = preferences.getInt(PREF_TIMEBASE, DEFAULT_TIMEBASE);
            setTimebase(timebase, false);
        }

        boolean screen = preferences.getBoolean(PREF_SCREEN, false);

        // Check screen
        Window window = getWindow();
        if (screen)
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        else
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        theme = Integer.parseInt(preferences.getString(PREF_THEME, "0"));
    }

    // Save preferences
    void savePreferences()
    {
        SharedPreferences preferences =
            PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putBoolean(PREF_BRIGHT, audio.bright);
        editor.putBoolean(PREF_SINGLE, audio.single);
        editor.putBoolean(PREF_STORAGE, scope.storage);
        editor.putInt(PREF_TIMEBASE, timebase);
        editor.apply();
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
                                 (dialog, which) ->
        {
            // Dismiss dialog
            dialog.dismiss();
        });

        // Create the dialog
        AlertDialog dialog = builder.create();

        // Show it
        dialog.show();
    }

    // GestureListener
    private class GestureListener
        extends GestureDetector.SimpleOnGestureListener
    {
        // onDown
        @Override
        public boolean onDown(MotionEvent e)
        {
            return true;
        }

        // onFling
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2,
                               float velocityX, float velocityY)
        {
            float scale = (float) (2.0 / ((audio.sample / 100000.0) *
                                          scope.xscale));
            // Calculate target value for animator
            float target = scope.start - velocityX / scale / 4;

            // Start the animation
            ValueAnimator animator =
                ValueAnimator.ofFloat(scope.start, target);
            animator.setInterpolator(new DecelerateInterpolator());
            animator.addUpdateListener((animation) ->
            {
                scope.start = (float) animation.getAnimatedValue();

                if (scope.start < 0)
                {
                    animation.cancel();
                    scope.start = 0;
                }

                if (scope.start > audio.length)
                {
                    animation.cancel();
                    scope.start = audio.length - (xscale.step / 4);
                }

                xscale.start = scope.start;
                xscale.invalidate();
            });

            animator.start();
            return true;
        }

        // onScroll
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2,
                                float distanceX, float distanceY)
        {
            float scale = (float) (2.0 / ((audio.sample / 100000.0) *
                                          scope.xscale));
            scope.start += distanceX / scale;
            if (scope.start < 0)
                scope.start = 0;

            if (scope.start > audio.length)
                scope.start = audio.length - (xscale.step / 4);

            xscale.start = scope.start;
            xscale.invalidate();

            return true;
        }

        // onSingleTapUp
        @Override
        public boolean onSingleTapUp(MotionEvent e)
        {
            scope.index = e.getX();
            return true;
        }
    }

    // ScaleListener
    private class ScaleListener
        extends ScaleGestureDetector.SimpleOnScaleGestureListener
    {
        // onScale
        @Override
        public boolean onScale(ScaleGestureDetector detector)
        {
            scope.xscale /= detector.getScaleFactor();
            xscale.scale = scope.xscale;
            xscale.invalidate();

            return true;
        }
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

        private static final int INIT = 0;
        private static final int FIRST = 1;
        private static final int NEXT = 2;
        private static final int LAST = 3;

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

            try
            {
                // Wait for the thread to exit
                if (t != null && t.isAlive())
                    t.join();
            }

            catch (Exception e) {}
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
            try
            {
                // Create the AudioRecord object
                audioRecord = new AudioRecord.Builder()
                    .setAudioSource(input)
                    .setAudioFormat
                    (new AudioFormat.Builder()
                     .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                     .setChannelMask(AudioFormat.CHANNEL_IN_MONO)
                     .build())
                    .build();
            }

            catch (Exception e)
            {
                runOnUiThread(() -> showAlert(R.string.app_name,
                                              R.string.error_init));
                thread = null;
                return;
            }

            // Get sample rate
            sample = audioRecord.getSampleRate();

            // Check audiorecord
            // Check state
            int state = audioRecord.getState();

            if (state != AudioRecord.STATE_INITIALIZED)
            {
                runOnUiThread(() -> showAlert(R.string.app_name,
                                              R.string.error_init));

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
                int size = audioRecord.read(buffer, 0, FRAMES);

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
                        int dx;

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
