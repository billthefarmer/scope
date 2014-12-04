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
import android.os.Handler;
import android.app.ActionBar;
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
import android.widget.TextView;

public class SpectrumActivity extends Activity
{
    private Spectrum spectrum;
    private FreqScale scale;
    private TextView text;
    private Unit unit;

    private Audio audio;

    // On create

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.activity_spectrum);

	spectrum = (Spectrum)findViewById(R.id.spectrum);
	scale = (FreqScale)findViewById(R.id.freqscale);
	unit = (Unit)findViewById(R.id.specunit);

	if (unit != null)
	    unit.scale = 0;

	// Enable back navigation on action bar

	ActionBar actionBar = getActionBar();
	actionBar.setDisplayHomeAsUpEnabled(true);
	actionBar.setTitle(R.string.spectrum);

	actionBar.setCustomView(R.layout.text);
	actionBar.setDisplayShowCustomEnabled(true);
	text = (TextView)actionBar.getCustomView();

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
	return true;
    }

    // On options item selected

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
	// Get id

	int id = item.getItemId();
	switch (id)
	{
	    // Home

	case android.R.id.home:
	    onBackPressed();
	    break;

	    // Settings

	case R.id.action_settings:
	    return onSettingsClick(item);

	default:
	    return false;
	}

	return true;
    }

    // On back pressed

    @Override
    public void onBackPressed()
    {
	Intent intent = new Intent(this, MainActivity.class);
	intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	startActivity(intent);
    }

    // On settings click

    private boolean onSettingsClick(MenuItem item)
    {
	Intent intent = new Intent(this, SettingsActivity.class);
	intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	startActivity(intent);

	return true;
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
	protected int input;
	protected int sample;

	// Data

	protected double frequency;
	protected double fps;

	private AudioRecord audioRecord;

	private static final int OVERSAMPLE = 16;
	private static final int SAMPLES = 16384;
	private static final int RANGE = SAMPLES * 7 / 16;
	private static final int STEP = SAMPLES / OVERSAMPLE;
	private static final int SIZE = 4096;

	private static final double MIN = 0.5;
	private static final double expect = 2.0 * Math.PI * STEP / SAMPLES;

	private Thread thread;
	private short data[];
	private double buffer[];

	private Complex x;

	protected double xa[];

	private double xp[];
	private double xf[];
	private double dx[];

	// Constructor

	protected Audio()
	{
	    data = new short[STEP];
	    buffer = new double[SAMPLES];
	    
	    x = new Complex(SAMPLES);

	    xa = new double[RANGE];
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

	    // Calculate fps

	    fps = (double)sample / SAMPLES;

	    // Start recording

	    audioRecord.startRecording();

	    // Max data

	    double dmax = 0.0;

	    // Continue until the thread is stopped

	    while (thread != null)
	    {
		// Read a buffer of data

		size = audioRecord.read(data, 0, STEP);

		// Stop the thread if no data

		if (size == 0)
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

		    x.r[i] = buffer[i] / norm * window;
		}

		// do FFT

		fftr(x);

		// Process FFT output

		for (int i = 1; i < RANGE; i++)
		{
		    double real = x.r[i];
		    double imag = x.i[i];

		    xa[i] = Math.hypot(real, imag);

		    // Do frequency calculation

		    double p = Math.atan2(imag, real);
		    double dp = xp[i] - p;

		    xp[i] = p;

		    // Calculate phase difference

		    dp -= i * expect;

		    int qpd = (int)(dp / Math.PI);

		    if (qpd >= 0)
			qpd += qpd & 1;

		    else
			qpd -= qpd & 1;

		    dp -=  Math.PI * qpd;

		    // Calculate frequency difference

		    double df = OVERSAMPLE * dp / (2.0 * Math.PI);

		    // Calculate actual frequency from slot frequency plus
		    // frequency difference and correction value

		    xf[i] = i * fps + df * fps;
		}

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

		if (max > MIN)
		{
		    final String s = String.format("%1.1fHz", frequency);
		    Handler handler = text.getHandler();
		    handler.post(new Runnable()
			{
			    @Override
			    public void run()
			    {
				text.setText(s);
			    }
			});
		}

		else
		{
		    frequency = 0.0;
		    Handler handler = text.getHandler();
		    handler.post(new Runnable()
			{
			    @Override
			    public void run()
			    {
				text.setText("");
			    }
			});
		}

		spectrum.postInvalidate();
	    }

	    // Stop and release the audio recorder

	    if (audioRecord != null)
	    {
		audioRecord.stop();
		audioRecord.release();
	    }
	}

	// Real to complex FFT, ignores imaginary values in input array

	private void fftr(Complex a)
	{
	    final int n = a.r.length;
	    final double norm = Math.sqrt(1.0 / n);

	    for (int i = 0, j = 0; i < n; i++)
	    {
		if (j >= i)
		{
		    double tr = a.r[j] * norm;

		    a.r[j] = a.r[i] * norm;
		    a.i[j] = 0.0;

		    a.r[i] = tr;
		    a.i[i] = 0.0;
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
		double delta = (Math.PI / mmax);
		for (int m = 0; m < mmax; m++)
		{
		    double w = m * delta;
		    double wr = Math.cos(w);
		    double wi = Math.sin(w);

		    for (int i = m; i < n; i += istep)
		    {
			int j = i + mmax;
			double tr = wr * a.r[j] - wi * a.i[j];
			double ti = wr * a.i[j] + wi * a.r[j];
			a.r[j] = a.r[i] - tr;
			a.i[j] = a.i[i] - ti;
			a.r[i] += tr;
			a.i[i] += ti;
		    }
		}
	    }
	}
    }

    // This object replaces arrays of structs in the C version because
    // initialising arrays of objects in Java is, IMHO, barmy

    // Complex

    private class Complex
    {
	double r[];
	double i[];

	private Complex(int l)
	{
	    r = new double[l];
	    i = new double[l];
	}
    }
}
