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
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import java.util.Arrays;
import java.util.Locale;

// SpectrumActivity
@SuppressWarnings("deprecation")
public class SpectrumActivity extends Activity
    implements View.OnClickListener, PopupMenu.OnMenuItemClickListener

{
    private static final String PREF_INPUT = "pref_input";
    private static final String PREF_FILL = "pref_fill";
    private static final String PREF_HOLD = "pref_hold";
    private static final String PREF_SCREEN = "pref_screen";
    private static final String PREF_THEME = "pref_theme";

    private static final int REQUEST_PERMISSIONS = 1;

    private Spectrum spectrum;
    private Toolbar toolbar;
    private TextView text;
    private Toast toast;

    private MenuItem lockItem;

    private Audio audio;

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
        case Main.LIGHT:
            setTheme(R.style.AppTheme);
            break;

        case Main.DARK:
            setTheme(R.style.AppDarkTheme);
            break;

        case Main.SYSTEM:
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

        setContentView(R.layout.activity_spectrum);

        spectrum = findViewById(R.id.spectrum);
        FreqScale scale = findViewById(R.id.freqscale);
        Unit unit = findViewById(R.id.specunit);

        if (unit != null)
            unit.scale = 0;

        if (spectrum != null)
            spectrum.setOnClickListener(this);

        setTitle(R.string.spectrum);

        // Show custom view
        ActionBar actionBar = getActionBar();
        actionBar.setCustomView(R.layout.text);
        actionBar.setDisplayShowCustomEnabled(true);
        text = (TextView) actionBar.getCustomView();

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

        audio = new Audio();

        if (spectrum != null)
            spectrum.audio = audio;

        if (scale != null)
            scale.audio = audio;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.spectrum, menu);

        lockItem = menu.findItem(R.id.action_lock);

        return true;
    }

    // On options item selected
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        Intent intent;

        // Get id
        int id = item.getItemId();
        switch (id)
        {
        // Home
        case android.R.id.home:
            finish();
            break;

        // Lock
        case R.id.action_lock:
            audio.lock = !audio.lock;
            item.setIcon(audio.lock ? R.drawable.lock_checked :
                         R.drawable.action_lock);
            showToast(audio.lock ? R.string.lock_on : R.string.lock_off);
            break;

        // Help
        case R.id.action_help:
            intent = new Intent(this, HelpActivity.class);
            startActivity(intent);
            break;

        // Settings
        case R.id.action_settings:
            intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            break;

        default:
            return false;
        }

        return true;
    }

    // On click
    @Override
    public void onClick(View v)
    {
        // Get id
        int id = v.getId();
        switch (id)
        {
        // Spectrum
        case R.id.spectrum:
            audio.lock = !audio.lock;
            if (lockItem != null)
                lockItem.setIcon(audio.lock ?
                                 R.drawable.lock_checked :
                                 R.drawable.action_lock);
            showToast(audio.lock ? R.string.lock_on : R.string.lock_off);
            break;
        }
    }

    // onMenuItemClick
    @Override
    public boolean onMenuItemClick(MenuItem item)
    {
        Intent intent;

        // Get id
        int id = item.getItemId();
        switch (id)
        {
        // Scope
        case R.id.action_scope:
            finish();
            return true;

        // Help
        case R.id.action_help:
            intent = new Intent(this, HelpActivity.class);
            startActivity(intent);
            return true;

            // Settings
        case R.id.action_settings:
            intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;

        default:
            return false;
        }
    }

    // Show toast.
    void showToast(int key)
    {
        String text = getString(key);

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
        // Fix for android 13
        View view = toast.getView();
        if (view != null && Build.VERSION.SDK_INT > Main.VERSION_CODE_S_V2)
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

        if (last != theme && Build.VERSION.SDK_INT != Build.VERSION_CODES.M)
            recreate();

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

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions,
                                           int[] grantResults)
    {
        if (requestCode == REQUEST_PERMISSIONS)
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
            audio.fill = preferences.getBoolean(PREF_FILL, true);
            audio.hold = preferences.getBoolean(PREF_HOLD, true);
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

    // Audio
    protected class Audio implements Runnable
    {
        private static final String TAG = "Spectrum";

        protected int input;
        protected int sample;
        protected boolean lock;
        protected boolean fill;
        protected boolean hold;

        // Data
        protected double frequency;
        protected double fps;

        private AudioRecord audioRecord;

        private static final int OVERSAMPLE = 4;
        private static final int SAMPLES = 4096;
        private static final int RANGE = SAMPLES / 2;
        private static final int STEP = SAMPLES / OVERSAMPLE;

        private static final int N = 8;
        private static final int M = 32;

        private static final double MIN = 0.5;
        private static final double expect = 2.0 * Math.PI * STEP / SAMPLES;

        private long counter;

        private Thread thread;
        private short data[];
        private double buffer[];

        private double xr[];
        private double xi[];

        protected double xa[];
        protected double xm[];

        private double xp[];
        private double xf[];


        // Constructor
        protected Audio()
        {
            data = new short[STEP];
            buffer = new double[SAMPLES];

            xr = new double[SAMPLES];
            xi = new double[SAMPLES];

            xa = new double[RANGE];
            xm = new double[RANGE];
            xp = new double[RANGE];
            xf = new double[RANGE];
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

            // Exception
            catch (Exception e)
            {
                runOnUiThread(() -> showAlert(R.string.app_name,
                                              R.string.error_init));
                thread = null;
                return;
            }

            // Get sample rate
            sample = audioRecord.getSampleRate();

            // Calculate fps
            fps = (double) sample / SAMPLES;

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

            // Max data
            double dmax = 0.0;

            // Max spectrum
            Arrays.fill(xm, 0.0);

            // Continue until the thread is stopped
            while (thread != null)
            {
                // Read a buffer of data
                int size = audioRecord.read(data, 0, STEP);

                // Stop the thread if no data or error state
                if (size <= 0)
                {
                    thread = null;
                    break;
                }

                // Move the main data buffer up
                System.arraycopy(buffer, STEP, buffer, 0, SAMPLES - STEP);

                for (int i = 0; i < STEP; i++)
                    buffer[(SAMPLES - STEP) + i] = data[i];

                // Maximum value
                if (dmax < 4096.0)
                    dmax = 4096.0;

                // Calculate normalising value
                double norm = dmax;

                dmax = 0.0;

                // Copy data to FFT input arrays
                for (int i = 0; i < SAMPLES; i++)
                {
                    // Find the magnitude
                    if (dmax < Math.abs(buffer[i]))
                        dmax = Math.abs(buffer[i]);

                    // Calculate the window
                    double window =
                        0.5 - 0.5 * Math.cos(2.0 * Math.PI *
                                             i / SAMPLES);

                    // Normalise and window the input data
                    xr[i] = buffer[i] / norm * window;
                }

                // do FFT
                fftr(xr, xi);

                // Process FFT output
                for (int i = 1; i < RANGE; i++)
                {
                    double real = xr[i];
                    double imag = xi[i];

                    // Get the magnitude
                    xa[i] = Math.hypot(real, imag);

                    // Do max spectrum calculation
                    if (xm[i] < xa[i])
                        xm[i] = xa[i];

                    else
                        xm[i] = ((xm[i] * 49.0) + xa[i]) / 50.0;

                    // Do frequency calculation
                    double p = Math.atan2(imag, real);
                    double dp = xp[i] - p;

                    xp[i] = p;

                    // Calculate phase difference
                    dp -= i * expect;

                    int qpd = (int) (dp / Math.PI);

                    if (qpd >= 0)
                        qpd += qpd & 1;

                    else
                        qpd -= qpd & 1;

                    dp -= Math.PI * qpd;

                    // Calculate frequency difference
                    double df = OVERSAMPLE * dp / (2.0 * Math.PI);

                    // Calculate actual frequency from slot frequency plus
                    // frequency difference and correction value
                    xf[i] = i * fps + df * fps;
                }

                // Do a full process run every N
                if (++counter % N != 0)
                    continue;

                // Check display lock
                if (lock)
                    continue;

                // Update spectrum
                spectrum.postInvalidate();

                // Update frequency and dB every M
                if (counter % M != 0)
                    continue;

                // Maximum FFT output
                double max = 0.0;

                // Find maximum value
                for (int i = 1; i < RANGE; i++)
                {
                    if (xa[i] > max)
                    {
                        max = xa[i];
                        frequency = xf[i];
                    }
                }

                // Sum of harmonics
                double sumh = 0.0;
                // Sum of fundamental
                double sumf = 0.0;
                for (int i = 1; i < RANGE; i++)
                    // Sum the fundamental
                    if (Math.abs(xf[i] - frequency) < fps)
                        sumf += xa[i] * xa[i];

                    // Sum the harmonics
                    else if (Math.IEEEremainder(xf[i], frequency) <
                             Math.round(xf[i] / frequency) * fps)
                        sumh += xa[i] * xa[i];

                // Total harmonic distortion
                double thd = Math.sqrt(sumh / sumf) * 100.0;

                // Level
                double level = 0.0;

                for (int i = 0; i < STEP; i++)
                    level += ((double) data[i] / 32768.0) *
                             ((double) data[i] / 32768.0);

                level = Math.sqrt(level / STEP) * 2.0;

                double dB = Math.log10(level) * 20.0;

                if (dB < -80.0)
                    dB = -80.0;

                // Update frequency and dB display
                if (max > MIN)
                {
                    final String s = String.format(Locale.getDefault(),
                                                   "%1.0f%% %1.1fHz  %1.1fdB",
                                                   thd, frequency, dB);
                    text.post(() -> text.setText(s));
                }
                else
                {
                    frequency = 0.0;
                    final String s = String.format(Locale.getDefault(),
                                                   "%1.1fdB", dB);
                    text.post(() -> text.setText(s));
                }
            }

            // Stop and release the audio recorder
            cleanUpAudioRecord();
        }

        // Real to complex FFT, ignores imaginary values in input array
        private void fftr(double ar[], double ai[])
        {
            final int n = ar.length;
            final double norm = Math.sqrt(1.0 / n);

            for (int i = 0, j = 0; i < n; i++)
            {
                if (j >= i)
                {
                    double tr = ar[j] * norm;

                    ar[j] = ar[i] * norm;
                    ai[j] = 0.0;

                    ar[i] = tr;
                    ai[i] = 0.0;
                }

                int m = n / 2;
                while (m >= 1 && j >= m)
                {
                    j -= m;
                    m /= 2;
                }
                j += m;
            }

            for (int mmax = 1, istep = 2 * mmax; mmax < n;
                    mmax = istep, istep = 2 * mmax)
            {
                double delta = Math.PI / mmax;
                for (int m = 0; m < mmax; m++)
                {
                    double w = m * delta;
                    double wr = Math.cos(w);
                    double wi = Math.sin(w);

                    for (int i = m; i < n; i += istep)
                    {
                        int j = i + mmax;
                        double tr = wr * ar[j] - wi * ai[j];
                        double ti = wr * ai[j] + wi * ar[j];
                        ar[j] = ar[i] - tr;
                        ai[j] = ai[i] - ti;
                        ar[i] += tr;
                        ai[i] += ti;
                    }
                }
            }
        }
    }
}
