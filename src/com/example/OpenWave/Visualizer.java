package com.example.OpenWave;
/*
 * Name: Visualizer.java
 * Author: Alex Evanczuk
 * Date: February 16, 2013
 * Purpose: Interface with NeuroSky wireless EEG device, MindBand
 * 			to create simple visualization software for PennHacks hackathon
 * 
 * Description:
 * This is the main visualization activity of my program. 
 * It creates a graph (using AChartEngine graph library) to display EEG data, updating constantly with new data.
 * It also displays the proprietary eSense levels of meditation, attention, as well as eye blinks.
 * 
 * Other Notes:
 * To Add:
 * - CubeLineGraph smoothing adjust feature
 * - Upload to facebook button
 * */


// Imports
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.BarChart;
import org.achartengine.chart.TimeChart;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.SimpleSeriesRenderer;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.neurosky.thinkgear.TGDevice;
import com.neurosky.thinkgear.TGEegPower;

public class Visualizer extends Activity {

	// Widgets
	private TextView State;
	private String connection;
	private int signal;
	private int timeDelta = 10;
	
	// Objects for Graphing
	
	// GRAPH ****************************
	TimeChart chart;
	private GraphicalView mChart;
	private XYMultipleSeriesDataset mDataset = new XYMultipleSeriesDataset();
	private XYMultipleSeriesRenderer mRenderer = new XYMultipleSeriesRenderer();
	private XYSeries mAlphaData;
	private XYSeries mBetaData;
	private XYSeries mThetaData;
	
	private XYSeriesRenderer mAlphaRenderer;
	private XYSeriesRenderer mBetaRenderer;
	private XYSeriesRenderer mThetaRenderer;
	
	private double xminCurrent;
	private double xmaxCurrent;

	// CHART ****************************
	private BarChart metrics;
	private GraphicalView bChart;
	private XYMultipleSeriesDataset bDataset = new XYMultipleSeriesDataset();
	private XYMultipleSeriesRenderer bRenderer = new XYMultipleSeriesRenderer();

	private XYSeriesRenderer bAttentionRenderer;
	private XYSeriesRenderer bMeditationRenderer;
	private XYSeriesRenderer bBlinkRenderer;

	private XYSeries bAttentionData;
	private XYSeries bMeditationData;
	private XYSeries bBlinkData;
	
	private int attentionTarget;
	private int meditationTarget;
	private int blinkTarget;
	
	private double attentionCurrent;
	private double meditationCurrent;
	private double blinkCurrent;

	// Data to be graphed
	List<TGEegPower> bands;
	long startTime = System.currentTimeMillis();

	// Objects to interface with hardware
	private BluetoothAdapter bluetoothAdapter;
	private TGDevice tgDevice;
	private TGEegPower band;
	final boolean rawEnabled = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Interface with device
		bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if(bluetoothAdapter == null) {
			// Alert user that Bluetooth is not available
			Toast.makeText(this, "Bluetooth not available", Toast.LENGTH_LONG).show();
			finish();
			return;
		} else {
			// Create the TGDevice
			tgDevice = new TGDevice(bluetoothAdapter, handler);
		}  

		// Initialize layout and widgets
		setContentView(R.layout.visualizer);
		onResume();
		
		State = (TextView) findViewById(R.id.State);
		State.setTextColor(Color.WHITE);
		Typeface font2 = Typeface.createFromAsset(getAssets(), "Vegur-Light.otf");
		State.setTypeface(font2);
		TextView connect = (TextView) findViewById(R.id.button1);
		Typeface font = Typeface.createFromAsset(getAssets(), "BebasNeue.otf");
		connect.setTypeface(font);
		connect.setBackgroundColor(Color.BLACK);
		connect.setTextColor(Color.WHITE);
		SeekBar seekbar = (SeekBar) findViewById(R.id.seekBar1);
		seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,boolean fromUser) {
				timeDelta = progress + 10;
			}
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {}
			@Override
			public void onStopTrackingTouch(SeekBar arg0) {}});

		// Set Background Color
		View v = findViewById(R.id.State);
		View root = v.getRootView();
		root.setBackgroundColor(Color.BLACK);
		
		// Initialize graphing and data storage
		bands = new ArrayList<TGEegPower>();
		
		// Create update timer
		Timer timer = new Timer();
		timer.schedule(new TimerTask(){
			public void run() {
				long time = System.currentTimeMillis();
				double x = (time - startTime) / 1000;
				
				mRenderer.setXAxisMax(x);
				mRenderer.setXAxisMin(x-timeDelta);

				/*
				System.out.println("x: " + x);
				
				double deltamin = Math.pow((Math.abs(x - xminCurrent)), .5);
				if(x < xminCurrent) deltamin *= -1;
				mRenderer.setXAxisMin(Math.max(0, xminCurrent + deltamin - timeDelta));
				xminCurrent = xminCurrent + deltamin - timeDelta;
			
				double deltamax = Math.pow((Math.abs(x - xmaxCurrent)), .5);
				if(x < xmaxCurrent) deltamin *= -1;
				mRenderer.setXAxisMax(Math.max(0, xmaxCurrent + deltamax));
				xmaxCurrent = xmaxCurrent + deltamax;
				
				System.out.println("Delta: " + deltamax);
				System.out.println("Diff: " + (xmaxCurrent - xminCurrent));
				*/

				double attentiondelta = Math.pow((Math.abs(attentionTarget - attentionCurrent)),.25);
				if(attentionTarget < attentionCurrent) attentiondelta *= -1;
				bAttentionData.add(1, attentionCurrent + (int) attentiondelta);
				attentionCurrent = attentionCurrent + (int) attentiondelta;
				
				double meditationdelta = Math.pow((Math.abs(meditationTarget - meditationCurrent)),.25);
				if(meditationTarget < meditationCurrent) meditationdelta *= -1;
				bMeditationData.add(2, meditationCurrent + (int) meditationdelta);
				meditationCurrent = meditationCurrent + (int) meditationdelta;
				
				double blinkdelta = Math.pow((Math.abs(blinkTarget - blinkCurrent)),.9);
				if(blinkTarget < blinkCurrent) blinkdelta *= -1;
				bBlinkData.add(3, blinkCurrent + (int) blinkdelta);
				blinkCurrent = blinkCurrent + (int) blinkdelta;
				
				bChart.repaint();
			}
		}, 0, 100);
		
	}

	@Override
	public void onDestroy() {
		tgDevice.close();
		super.onDestroy();
	}

	/*
	 * **************************************************************
	 * INTERFACING METHODS
	 * **************************************************************
	 */

	// Handle messages from TGDevice
	final Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case TGDevice.MSG_STATE_CHANGE:
				switch (msg.arg1) {
				case TGDevice.STATE_IDLE:
					connection = "Device is idle";
					break;
				case TGDevice.STATE_CONNECTING:		                	
					connection = "Device is connecting";
					break;		                    
				case TGDevice.STATE_CONNECTED:
					connection = "Device is connected";
					tgDevice.start();
					break;
				case TGDevice.STATE_NOT_FOUND:
					connection = "Device can't be found";
					break;
				case TGDevice.STATE_NOT_PAIRED:
					connection = "Device is not paired";
					break;
				case TGDevice.STATE_DISCONNECTED:
					connection = "Device is disconnected";
				}

				break;
			case TGDevice.MSG_POOR_SIGNAL:
				signal = msg.arg1;
				break;
			case TGDevice.MSG_ATTENTION:
				addMetric("Attention",msg.arg1);
				break;
			case TGDevice.MSG_MEDITATION:
				addMetric("Meditation",msg.arg1);
				break;
			case TGDevice.MSG_BLINK:
				addMetric("Blink",msg.arg1);
				break;
			case TGDevice.MSG_LOW_BATTERY:
				Toast.makeText(getApplicationContext(), "Low battery!", Toast.LENGTH_SHORT).show();
				break;
				// This is the case where data is to be collected
			case TGDevice.MSG_EEG_POWER:
				band = (TGEegPower) msg.obj;
				bands.add(band);
				addPoint(band);
				break;
			default:
				addMetric("Blink",0);
				break;
			}

			String sig;
			if(signal < 100){
				sig = "good.";
			} else {
				sig = "bad.";
			}
			State.setText(connection + " and signal quality is " + sig);
			mChart.repaint();

		}
	};

	public void connectDevice(View view) {
		if(tgDevice.getState() != TGDevice.STATE_CONNECTING && tgDevice.getState() != TGDevice.STATE_CONNECTED)
			tgDevice.connect(rawEnabled);   
	}

	/*
	 * **************************************************************
	 * GRAPHING METHODS
	 * **************************************************************
	 */
	

	private double rawtoVoltage(double raw){
		return raw * (1.8/4096) / 2000;
	}

	private void initChart() {
		mAlphaData = new XYSeries("Alpha");
		mBetaData = new XYSeries("Beta");
		mThetaData = new XYSeries("Theta");

		mDataset.addSeries(mAlphaData);
		mDataset.addSeries(mBetaData);
		mDataset.addSeries(mThetaData);

		mAlphaRenderer = new XYSeriesRenderer();
		mThetaRenderer = new XYSeriesRenderer();
		mBetaRenderer = new XYSeriesRenderer();

		mAlphaRenderer.setColor(Color.GREEN);
		mThetaRenderer.setColor(Color.MAGENTA);
		mBetaRenderer.setColor(Color.RED);
		mBetaRenderer.setLineWidth(2);
		mAlphaRenderer.setLineWidth(2);
		mThetaRenderer.setLineWidth(2);


		//mRenderer.setAntialiasing(true);
		mRenderer.setLabelsTextSize(22);
		mRenderer.addSeriesRenderer(mThetaRenderer);
		mRenderer.addSeriesRenderer(mAlphaRenderer);
		mRenderer.addSeriesRenderer(mBetaRenderer);
		
		mRenderer.setYAxisMax(.005);
		mRenderer.setYAxisMin(-.001);

	}

	private void initBarchart() {
		bAttentionData = new XYSeries("Attention");
		bMeditationData = new XYSeries("Meditation");
		bBlinkData = new XYSeries("Blink");
		bDataset.addSeries(bAttentionData);
		bDataset.addSeries(bMeditationData);
		bDataset.addSeries(bBlinkData);

		bAttentionRenderer = new XYSeriesRenderer();
		bMeditationRenderer = new XYSeriesRenderer();
		bBlinkRenderer = new XYSeriesRenderer();

		bRenderer.setXLabels(1);
		bRenderer.addXTextLabel(1, "Attention");
		bRenderer.addXTextLabel(2, "Meditation");
		bRenderer.addXTextLabel(3, "Blink");

		bRenderer.setLabelsTextSize(12);
		bRenderer.addSeriesRenderer(bAttentionRenderer);
		bRenderer.addSeriesRenderer(bMeditationRenderer);
		bRenderer.addSeriesRenderer(bBlinkRenderer);
		bRenderer.setAntialiasing(true);
		bRenderer.setBarSpacing(-0.01);

		SimpleSeriesRenderer r = new SimpleSeriesRenderer();
		r.setDisplayChartValues(true);

		bRenderer.setXAxisMax(3.5);
		bRenderer.setXAxisMin(.5);
		bRenderer.setYAxisMax(100);
		bRenderer.setYAxisMin(0);
		
		bRenderer.setShowAxes(false);

		double[] range = {0, 4, -10, 100};
		bRenderer.setInitialRange(range);
		bRenderer.setLabelsColor(Color.BLUE);
		bRenderer.setZoomEnabled(false);
		bRenderer.setExternalZoomEnabled(false);
		bRenderer.setShowLegend(false);
		bRenderer.setLabelsColor(Color.WHITE);
		bRenderer.setPanEnabled(false,false);

		bRenderer.setPanEnabled(false);
		bRenderer.setLabelsTextSize(20);
	}

	// Add to graph
	private void addPoint(TGEegPower point){
		double alphaVoltage = rawtoVoltage((point.lowAlpha+point.highAlpha)/2);
		double betaVoltage = rawtoVoltage((point.lowBeta + point.highBeta)/2);
		double thetaVoltage = rawtoVoltage(point.theta);
		long time = System.currentTimeMillis();
		double x = (time - startTime) / 1000;
		if(alphaVoltage < 1) mAlphaData.add(x, alphaVoltage);
		if(betaVoltage < 1) mBetaData.add(x, betaVoltage);
		if(thetaVoltage < 1) mThetaData.add(x, thetaVoltage);
	}

	// Add to bar chart
	private void addMetric(String type, int i){
		if(type.equals("Attention")){
			attentionTarget = i;
		} else if(type.equals("Meditation")){
			meditationTarget = i;
		} else if(type.equals("Blink")){
			blinkTarget = i;
		}
	}

	protected void onResume() {
		super.onResume();
		// Bar Chart
		LinearLayout barlayout = (LinearLayout) findViewById(R.id.barchart);
		if (bChart == null) {
			initBarchart();
			bChart = ChartFactory.getBarChartView(this, bDataset, bRenderer, BarChart.Type.DEFAULT);
			barlayout.addView(bChart);
		} else {
			bChart.repaint();
		}

		// Graph
		LinearLayout layout = (LinearLayout) findViewById(R.id.chart);
		if (mChart == null) {
			initChart();
			mChart = ChartFactory.getCubeLineChartView(this, mDataset, mRenderer, 0.25f);
			layout.addView(mChart);
		} else {
			mChart.repaint();
		}
	}
}