package com.example.AutoLike;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;

import android.util.Log;

public class ReadingRecorder implements IReadingRecorder {
	private static ReadingRecorder rr;
	Connection dbConn;
	public static final int MEDITATION = 1;
	public static final int ATTENTION = 2;

	public static ReadingRecorder getInstance() {
		if (rr == null) {
			rr = new ReadingRecorder();
		}

		return rr;
	}

	public ReadingRecorder() {
		try {
			Class.forName("org.h2.Driver");
			this.dbConn = DriverManager.getConnection("jdbc:h2:mem:recordings");
			// Create the underlying table
			createTable();
			Log.i("Table Creation", "Successful !!");
		} catch (SQLException e) {
			Log.e("Oops!! SQLException!! Look below for details: ", Log.getStackTraceString(e));
		} catch (ClassNotFoundException e1) {
			Log.e("Oops!! ClassNotFoundException!! Look below for details: ", Log.getStackTraceString(e1));
		}
	}
	
	private void createTable() throws SQLException {
		Statement stmt = this.dbConn.createStatement();
		String createTable = "create table readings(type INTEGER, value INTEGER, "
				+ "recorded_time NUMBER, image_id NUMBER)";
		int status = stmt.executeUpdate(createTable);

		Log.v("Create table status: ", status + "");
	}

	@Override
	public void recordReading(int type, int value, long imageId) throws SQLException {
		if(this.dbConn == null) {
			Log.e("ERROR", "Connection object is empty. Cannot save recording");
			return;
		}

		String insStmt = "insert into readings(type, value, recorded_time, image_id) values (?, ?, ?, ?)";
		PreparedStatement stmt = this.dbConn.prepareStatement(insStmt);
		
		stmt.setInt(1, type);
		stmt.setInt(2, value);
		stmt.setLong(3, new Date().getTime());
		stmt.setLong(4, imageId);
		
		int count = stmt.executeUpdate();
		if(count != 1) {
			Log.e("Error when trying to insert the readings: ", "[" + type + ", " + value + ", " + imageId + "]");
		}
	}
	
	@Override
	public boolean analyzeLike() throws SQLException {
		if(this.dbConn == null) {
			Log.e("ERROR", "Connection object is empty. Cannot analyze.");
			return false;
		}
		boolean isLike = false;
		String selStmt = "select meditation.*, attention.* from "
				+ "(select avg(value) from readings where type = " + MEDITATION + ") meditation, "
				+ "(select avg(value) from readings where type = " + ATTENTION + ") attention";
		PreparedStatement stmt = this.dbConn.prepareStatement(selStmt);
		
		ResultSet rs = stmt.executeQuery();
		int meditation = 0, attention = 0;
		while(rs.next()) {
			meditation = rs.getInt(1);
			attention = rs.getInt(2);
			Log.d("Meditation average : ", meditation + "");
			Log.d("Attention average : ", attention + "");
		}
		
		if( (meditation >= 50) && (attention >= 60) ) {
			isLike = true;
		}
		
		return isLike;
	}
	
	@Override
	public void reset() throws SQLException {
		if(this.dbConn == null) {
			Log.e("ERROR", "Connection object is empty. Cannot reset.");
			return;
		}
		
		Statement stmt = this.dbConn.createStatement();
		String truncTable = "delete from readings";
		int status = stmt.executeUpdate(truncTable);

		Log.v("Truncate table status: ",  status + "");
	}
}
