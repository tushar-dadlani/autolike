package com.example.AutoLike;

public interface IReadingRecorder {
	int MEDITATION = 1;
	int ATTENTION = 2;
	
	void recordReading(int type, int value, long imageId) throws Exception;
	boolean analyzeLike() throws Exception;
	void reset() throws Exception;
}
