package com.example.AutoLike;



import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.widget.Toast;

import com.neurosky.thinkgear.TGDevice;

public class LightswitchActivity extends Activity {
	TGDevice tgDevice;
	BluetoothAdapter bluetoothAdapter;

	final boolean rawEnabled = false;


	double target;

	@SuppressLint("NewApi") @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);



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


	}

	@Override
	public void onDestroy() {
		tgDevice.close();
		super.onDestroy();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return true;
	}

	final Handler handler = new Handler() {
		@SuppressLint("NewApi") @Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case TGDevice.MSG_MEDITATION:
				Log.v("Meditation",Integer.toString(msg.arg1));
				break;
			case TGDevice.MSG_ATTENTION:
				Log.v("Attention",Integer.toString(msg.arg1));
				target = msg.arg1;
				break;
			case TGDevice.MSG_BLINK:
				Log.v("Blink", Integer.toString(msg.arg1));
				break;
			case TGDevice.MSG_STATE_CHANGE:
				switch (msg.arg1) {
				case TGDevice.STATE_IDLE:
					Log.v("Status", "Idle");
					break;
				case TGDevice.STATE_CONNECTING:		                	
					Log.v("Status", "Connecting...");
					break;		                    
				case TGDevice.STATE_CONNECTED:
					Log.v("Status", "Connected.");
					tgDevice.start();
					break;
				case TGDevice.STATE_NOT_FOUND:
					Log.v("Status", "Can't find");
					break;
				case TGDevice.STATE_NOT_PAIRED:
					Log.v("Status", "not paired");
					break;
				case TGDevice.STATE_DISCONNECTED:
					Log.v("Status", "Disconnected mang");
				}
				break;


			case TGDevice.MSG_POOR_SIGNAL:
				Log.v("Control", "PoorSignal: " + msg.arg1); 
				break;
			case TGDevice.MSG_RAW_DATA:
				int rawValue = msg.arg1;
				Log.v("RAW",Integer.toString(rawValue));
				break;
			case TGDevice.MSG_EEG_POWER:
				int ep = msg.arg1;
				Log.v("HelloEEG", "Delta: " + Integer.toString(ep));
				break;

			default:
				break; 
			}




		}
	};
}
