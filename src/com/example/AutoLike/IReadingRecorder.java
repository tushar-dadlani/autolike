package com.example.AutoLike;

public interface IReadingRecorder {
	void recordReading(int type, int value, long imageId) throws Exception;
	boolean analyzeLike() throws Exception;
	void reset() throws Exception;
}
