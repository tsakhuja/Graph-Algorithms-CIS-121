package edu.upenn.cis.cis121.project;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Class representing a user object.
 * @author tsakhuja
 *
 */
public class User {
	private int _id;
	private String _firstName;
	private String _lastName;
	private double _lat;
	private double _lon;
	private int[] _friends;
	private int[] _likes;
	

	/**
	 * Constructor
	 * @param rs
	 * @throws SQLException
	 */
	public User(ResultSet rs) throws SQLException {
		_id = rs.getInt("user_id");
		_firstName = rs.getString("first_name");
		_lastName = rs.getString("last_name");
		_lat = rs.getDouble("latitude");
		_lon = rs.getDouble("longitude");
		
	}
	
	/**
	 * Set the user's friends
	 * @param friends
	 */
	public void setFriends(int[] friends) {
		_friends = friends;
	}
	
	/**
	 * Set user's likes
	 * @param likes
	 */
	public void setLikes(int[] likes) {
		_likes = likes;
	}
	
	
	// Accessors 
	/**
	 * Returns the id of the user
	 * @return
	 */
	public int getId() {
		return _id;
	}
	
	/**
	 * Returns the user's first name
	 * @return
	 */
	public String getFirstName() {
		return _firstName;
	}
	
	/**
	 * Returns the user's last name
	 * @return
	 */
	public String getLastName() {
		return _lastName;
	}
	
	/**
	 * Returns the user's latitude
	 * @return
	 */
	public double getLat() {
		return _lat;
	}
	
	/**
	 * Returns the user's longitude
	 * @return
	 */
	public double getLon() {
		return _lon;
	}
	
	/**
	 * Returns the user's friends as an array of user_ids
	 * @return
	 */
	public int[] getFriends() {
		return _friends;
	}
	
	/**
	 * Returns the user's likes as an array of place_ids
	 * @return
	 */
	public int[] getLikes() {
		return _likes;
	}
	
	/**
	 * Returns whether or not the user likes a given place
	 * @param place_id
	 * @return
	 */
	public boolean likes(int place_id) {
		for (int i : _likes) {
			if (i == place_id) {
				return true;
			}
		}
		return false;
	}
	
}
