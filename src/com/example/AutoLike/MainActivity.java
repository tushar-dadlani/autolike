package com.example.AutoLike;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.Toast;

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.model.GraphUser;
import com.neurosky.thinkgear.TGDevice;

public class MainActivity extends FragmentActivity {
	private MainFragment mainFragment; 

	TGDevice tgDevice;
	BluetoothAdapter bluetoothAdapter;

	final boolean rawEnabled = false;
	
    public static Button b;
    
    String[] urls = {
    		"http://www.iwallscreen.com/stock/beach-wallpaper.jpg",
    		"http://www.iwallscreen.com/stock/2010-alice-in-wonderland-cheshire-cat-hd-desktop.jpg",
    		"http://www.iwallscreen.com/stock/2012-mazda-5-grand-touring-photo-gallery-of-short-take-road-test.jpg",
    		"https://scontent-b.xx.fbcdn.net/hphotos-prn2/t31/1266918_694201383942880_1664167396_o.jpg",
    		"http://www.wallsave.com/wallpapers/1024x768/jessica-alba/218629/jessica-alba-best-photography-of-218629.jpg",
    		"https://scontent-a.xx.fbcdn.net/hphotos-frc1/t31/883866_10152665161785720_597803406_o.jpg",
    		"https://scontent-a.xx.fbcdn.net/hphotos-ash2/t1/377386_10151148385395720_1670205248_n.jpg",
    		"https://scontent-a.xx.fbcdn.net/hphotos-prn2/t1/1384155_10202021326722234_1837204170_n.jpg",
    		"https://scontent-b.xx.fbcdn.net/hphotos-ash4/t1/1455005_10202021325762210_1219889289_n.jpg",
    		"http://www.wallcoo.com/2560x1600/2560x1600_WideScreen_Wallpapers_beach/images/%5Bwallcoo.com%5D_2560x1600_Widescreen_Beach_wallpaper_1EP022.jpg",
    		"https://scontent-b.xx.fbcdn.net/hphotos-prn2/t1/1185504_10151684778037483_152349835_n.jpg"
	};
    
    int image_index = 0;
	
    WebView wv;

	double target;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		 
		wv = (WebView) findViewById(R.id.webview);
		
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


		if (savedInstanceState == null) {
	        // Add the fragment on initial activity setup
	        mainFragment = new MainFragment();
	        getSupportFragmentManager()
	        .beginTransaction()
	        .add(android.R.id.content, mainFragment)
	        .commit();
	    } else {
	        // Or set the fragment from restored state info
	        mainFragment = (MainFragment) getSupportFragmentManager()
	        .findFragmentById(android.R.id.content);
	    }

		
		Session.openActiveSession(this, true, new Session.StatusCallback() {

		      // callback when session changes state
		      @Override
		      public void call(Session session, SessionState state, Exception exception) {
		        if (session.isOpened()) {

		          // make request to the /me API
		          Request.executeMeRequestAsync(session, new Request.GraphUserCallback() {

		            // callback after Graph API response with user object
		            @Override
		            public void onCompleted(GraphUser user, Response response) {
		              if (user != null) {
		           //     TextView welcome = (TextView) findViewById(R.id.welcome);
		          //      welcome.setText("Hello " + user.getName() + "!");
		            	  Log.v("Logged In", user.getName());
		              }
		            }
		          });
		        }
		      }
		    });
		
		
		
		
		
	}
	

	
	public void updateWebView(String url)
	{
				
	        	this.wv.loadUrl(url);
	        	this.wv.reload();
	        	
	        //	this.setContentView(wv);
	}
	
	  @Override
	  public void onActivityResult(int requestCode, int resultCode, Intent data) {
	      super.onActivityResult(requestCode, resultCode, data);
	      Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
	
	  }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	@Override
	public void onDestroy() {
		tgDevice.close();
		super.onDestroy();
	}

	final Handler handler = new Handler() {
		@SuppressLint("NewApi") @Override
		public void handleMessage(Message msg) {
			IReadingRecorder recorder = ReadingRecorder.getInstance();
			try{
				switch (msg.what) {
				case TGDevice.MSG_MEDITATION:
					recorder.recordReading(IReadingRecorder.MEDITATION, msg.arg1, 1);
					Log.v("Meditation",Integer.toString(msg.arg1));
					break;
				case TGDevice.MSG_ATTENTION:
					recorder.recordReading(IReadingRecorder.ATTENTION, msg.arg1, 1);
					Log.v("Attention",Integer.toString(msg.arg1));
					target = msg.arg1;
					break;
				case TGDevice.MSG_BLINK:
					boolean like = recorder.analyzeLike();
					recorder.reset();
					
					if(wv != null)
					{
						updateWebView(urls[image_index++]);
						if(image_index >= urls.length) {
							image_index = 0;
						}
						// Show popup to show a toast
						String toastMessage = "Image ";
						if(image_index %3 == 0) {
							toastMessage += " Liked!!";
						} else {
							toastMessage += " Ignored!!";
							
						}
						Toast.makeText(getApplicationContext(), toastMessage,
								   Toast.LENGTH_LONG).show();
						
					}
					else
					{
						System.out.println("WebView Null");
					}
					
					if(like) {
						Log.i("***************", "Yes");
					} else {
						Log.i("---------------", "No");
					}

					
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
					//Log.v("Control", "PoorSignal: " + msg.arg1); 
					break;
				case TGDevice.MSG_RAW_DATA:
					//int rawValue = msg.arg1;
					//Log.v("RAW",Integer.toString(rawValue));
					break;
				case TGDevice.MSG_EEG_POWER:
					//int ep = msg.arg1;
					//Log.v("HelloEEG", "Delta: " + Integer.toString(ep));
					break;

				default:
					break; 
				}
			} catch(Exception e) {
				Log.e("Error in handleMessage", "Error details are:: ", e);
			}
		}
	};
	
	


}
