package com.example.AutoLike;

import java.util.ArrayList;
import java.util.List;

import android.util.Log;

public class ReadingRecorder implements IReadingRecorder {
	private static ReadingRecorder rr = null;
	List<Integer> meditation;
	List<Integer> attention;

	public static final ReadingRecorder getInstance() {
		if (rr == null) {
			rr = new ReadingRecorder();
		}

		return rr;
	}

	public ReadingRecorder() {
		meditation = new ArrayList<Integer>();
		attention = new ArrayList<Integer>();
	}

	@Override
	public void recordReading(int type, int value, long imageId)
			throws Exception {
		if (type == MEDITATION) {
			meditation.add(value);
		} else {
			attention.add(value);
		}
	}

	@Override
	public boolean analyzeLike() throws Exception {
		boolean like = false;
		
		int cntM = 0, cntA = 0;
		for(int m : meditation) {
			if(m > 50) {
				cntM++;
			}
		}
		for(int a : attention) {
			if(a > 50) {
				cntA++;
			}
		}
		
		Log.d("Count of Meditation: ",  cntM + "");
		Log.d("Count of Attention: ",  cntA + "");
		
		if(cntM >= 3 && cntA >= 4) {
			like = true;
		}
		
		return like;
	}

	@Override
	public void reset() throws Exception {
		meditation.clear();
		attention.clear();
	}

}
