package com.example.OpenWave;

import java.util.Timer;
import java.util.TimerTask;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.neurosky.thinkgear.TGDevice;

public class LightswitchActivity extends Activity {
	TGDevice tgDevice;
	BluetoothAdapter bluetoothAdapter;
	ImageView shine0;
	ImageView shine1;
	SeekBar seekbar;
	final boolean rawEnabled = false;
	TextView status;
	Timer timer;
	double delta;
	double target;

	@SuppressLint("NewApi") @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.lightswitch);

		View v = findViewById(R.id.lightbulboff);
		View root = v.getRootView();
		root.setBackgroundColor(Color.BLACK);

		
		ImageView lightbulb = (ImageView) findViewById(R.id.LightBulbon);
		lightbulb.setImageResource(R.drawable.lightbulbon);
		
		final ImageView on = (ImageView) findViewById(R.id.LightBulbon);

		status = (TextView) findViewById(R.id.status);
		Typeface font = Typeface.createFromAsset(getAssets(), "BebasNeue.otf");
		status.setTypeface(font);
		status.setBackgroundColor(Color.BLACK);
		status.setTextColor(Color.WHITE);
		
		// Connect the device Via Bluetooth
		bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if(bluetoothAdapter == null) {
			Toast.makeText(this, "Bluetooth not available", Toast.LENGTH_LONG).show();
			finish();
			return;
		}else {
			tgDevice = new TGDevice(bluetoothAdapter, handler);
		}          

		if(tgDevice.getState() != TGDevice.STATE_CONNECTING && tgDevice.getState() != TGDevice.STATE_CONNECTED){
			tgDevice.connect(rawEnabled);   
		}	

		//Set seekbar changeListener
		seekbar = (SeekBar) findViewById(R.id.seekBar1);
		seekbar.setBackgroundColor(Color.BLACK);
		seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

			@SuppressLint("NewApi") 
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				on.setAlpha((float) progress/100);
			}
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {}
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {}
		});
		
		timer = new Timer();
		timer.schedule(new TimerTask(){
			public void run() {
				int current = seekbar.getProgress();
				delta = Math.pow((Math.abs(target - current)),.25);
				if(target < current) delta *= -1;
				seekbar.setProgress(seekbar.getProgress() + (int) delta);
			}
		}, 10, 30);
	}

	@Override
	public void onDestroy() {
		tgDevice.close();
		super.onDestroy();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.lightswitch, menu);
		return true;
	}

	final Handler handler = new Handler() {
		@SuppressLint("NewApi") @Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case TGDevice.MSG_ATTENTION:	
				target = msg.arg1;
				break;
			case TGDevice.MSG_BLINK:
				System.out.println("Blink: " + msg.arg1);

				//Blink.setText("Blink: " + msg.arg1 + "");
				break;
			case TGDevice.MSG_STATE_CHANGE:
				switch (msg.arg1) {
				case TGDevice.STATE_IDLE:
					status.setText("Status: Idle");
					break;
				case TGDevice.STATE_CONNECTING:		                	
					status.setText("Status: Connecting...");
					break;		                    
				case TGDevice.STATE_CONNECTED:
					status.setText("Status: Connected.");
					tgDevice.start();
					break;
				case TGDevice.STATE_NOT_FOUND:
					status.setText("Status: Can't find");
					break;
				case TGDevice.STATE_NOT_PAIRED:
					status.setText("Status: not paired");
					break;
				case TGDevice.STATE_DISCONNECTED:
					status.setText("Status: Disconnected mang");
				}
				break;
			default:
				break;
			}
		}
	};
}
