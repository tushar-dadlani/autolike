package com.example.OpenWave;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity {

	Button visualize;
	Button control;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		visualize = (Button) findViewById(R.id.Visualize);
		control = (Button) findViewById(R.id.Control);
		Typeface font = Typeface.createFromAsset(getAssets(), "BebasNeue.otf");
		visualize.setTypeface(font);
		control.setTypeface(font);
		control.setTextSize(75);
		visualize.setTextSize(75);
		
		control.setBackgroundColor(Color.BLACK);
		control.setTextColor(Color.WHITE);
		visualize.setBackgroundColor(Color.LTGRAY);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}
	
	public void switchtoControl(View v){
		Intent controlIntent = new Intent(v.getContext(),LightswitchActivity.class);
		startActivity(controlIntent);
	}

	public void switchtoVisualize(View v){
		Intent visualizeIntent = new Intent(v.getContext(),Visualizer.class);
		startActivity(visualizeIntent);
	}
}
