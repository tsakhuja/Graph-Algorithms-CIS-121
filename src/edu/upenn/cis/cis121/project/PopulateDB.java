package edu.upenn.cis.cis121.project;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;



/**
 * Class that populates places in an SQL db from csv files.
 * @author tsakhuja
 *
 */
public class PopulateDB {
	

	private Connection _conn;
	private String _dbUser;
	private String _dbPass;
	private String _dbSID;
	private String _dbHost;
	private int _port;
	
	/**
	 * Default constructor
	 */
	public PopulateDB(String dbUser, String dbPass, String dbSID, String dbHost, int port) {
		_dbUser = dbUser;
		_dbPass = dbPass;
		_dbSID = dbSID;
		_dbHost = dbHost;
		_port = port;
 
	}
	
	/**
	 * Opens the database connection
	 * 
	 * @param dbUser
	 * @param dbPass
	 * @param dbSID
	 * @param dbHost
	 * @param port
	 * @return status (string)
	 * @throws SQLException
	 * @throws ClassNotFoundException
	 */
	private String openDBConnection() throws SQLException, ClassNotFoundException{
		
		_conn = DBUtils.openDBConnection(_dbUser, _dbPass, _dbSID, _dbHost, _port);
		String res = DBUtils.testConnection();
	
		return res;
	}
	
	/**
	 * Close the database connection if open
	 * @throws SQLException
	 */
	public void closeDBConnection() {
		try {
			if (!_conn.isClosed()) {
				DBUtils.closeDBConnection();
			}
		} catch (SQLException sqle) {
			sqle.printStackTrace(System.err);
		}
	}
	
	/**
	 * Helper method that reads in entries from a csv into an ArrayList
	 * @param inFile
	 * @return
	 */
	private ArrayList<String[]> readData(String inFile) {
		ArrayList<String[]> entries = new ArrayList<String[]>();
		
		try {
			BufferedReader reader = new BufferedReader(new FileReader(inFile));
			String nextLine = reader.readLine();
			
			while (nextLine != null) {
				// process each line of csv file by splitting parameters around comma.
				String[] entry = nextLine.split(",");
				// add each row of data into the output ArrayList
				entries.add(entry);
				
				nextLine = reader.readLine();
			}
			reader.close(); 
		} catch (FileNotFoundException e) {
			System.out.println("File " + inFile + " not found.");
		} catch (IOException i) {
			System.out.println("Error reading file");
		} 
		return entries;
	}
	
	/**
	 * Populates Places table
	 * @param filename
	 */
	public void populatePlaces(String filename) throws SQLException {
		ArrayList<String[]> entries = readData(filename);
		ArrayList<String> inserted = new ArrayList<String>(); // keep track of inserted rows

		try {
			String response = this.openDBConnection(); // open DB
			System.out.println(response);
			for (String[] entry : entries) {
				String place_id = entry[0];
				String place_name = "'" + entry[1].replace("'", "''") + "'";
				String type_id = entry[2];
				String latitude = entry[3];
				String longitude = entry[4];
				String query = "insert into Places (place_id, place_name, type_id, latitude, longitude) values (" +
						place_id + ", " + place_name + ", " + type_id + ", " + latitude + ", " + longitude + ")"; 
				if (!inserted.contains(query)) { // only insert entry if it's not already in table
					DBUtils.executeUpdate(query); // insert into DB
					inserted.add(entry.toString());
				}
			}
		} catch (ClassNotFoundException ce) {
			System.err.println("Registrar encountered an exception: " + ce.toString());
		} finally {
			this.closeDBConnection();
		}
	}
	
	/**
	 * Populates Likes table
	 * @param filename
	 * @throws SQLException
	 */
	public void populateLikes(String filename) throws SQLException {
		ArrayList<String[]> entries = readData(filename);
		ArrayList<String> inserted = new ArrayList<String>(); // keep track of inserted rows
		
		try {
			String response = this.openDBConnection(); // open DB
			System.out.println(response);
			for (String[] entry : entries) {
				String user_id = entry[0];
				String place_id = entry[1];
				String query = "insert into Likes (user_id, place_id) values (" +
						user_id + ", " + place_id + ")"; 
				if (!inserted.contains(query)) { // only insert entry if it's not already in table
					DBUtils.executeUpdate(query);
					inserted.add(query);
				}
			}
		} catch (ClassNotFoundException ce) {
			System.err.println("Registrar encountered an exception: " + ce.toString());
		} finally {
			this.closeDBConnection();
		}
	}

	public static void main (String args[]) throws SQLException {
		
		PopulateDB pop = new PopulateDB("tsakhuja", "chotu6718", "CIS", "fling.seas.upenn.edu", 1521);
		
		pop.populatePlaces("places_data.csv"); // populate places
		System.out.println("Places added to DB");
		pop.populateLikes("likes_data.csv"); // populate likes
		System.out.println("Likes added to DB");

	}
}
