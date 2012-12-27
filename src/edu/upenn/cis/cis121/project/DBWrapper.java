package edu.upenn.cis.cis121.project;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;


public class DBWrapper {
	
	private HashMap<Integer, User> _users; // Store queried objects locally
	private HashMap<Integer, Place> _places;
	private HashMap<Integer, String> _types; // Store of place types and descriptions
	
	private Connection _conn;
	private String _dbUser;
	private String _dbPass;
	private String _dbSID;
	private String _dbHost;
	private int _port;
	
	/**
	 * Default constructor
	 * the parameters here are the same as for the PopulateDB constructor
	 */ 
	public DBWrapper(String dbUser, String dbPass, String dbSID, String dbHost, int port) {
		_users = new HashMap<Integer, User>();
		_places = new HashMap<Integer, Place>();
		_types = new HashMap<Integer, String>();
		
		_dbUser = dbUser;
		_dbPass = dbPass;
		_dbSID = dbSID;
		_dbHost = dbHost;
		_port = port;
		
		populateUsers();
		populatePlaces();
		populatePlaceTypes();
	}
	
	
	/**
	 * Helper method that scrapes User table rows and stores in a HashMap
	 */
	private void populateUsers() {
		String query = "select * from Users";
		
		try {
			String res = this.openDBConnection();
			System.out.println(res);
			Statement st = _conn.createStatement();
			ResultSet rs = st.executeQuery(query);
			// store all users in table locally
			while (rs.next()) {
				User user = new User(rs);
				int[] friends = this.getFriendsFromDB(user.getId());
				user.setFriends(friends); // get and set user's friends
				user.setLikes(this.getLikesFromDB(user.getId())); // get and set user's likes
				_users.put(user.getId(), user);
			}

			rs.close();
			st.close();
		} catch (SQLException s) {
			System.err.println("Registrar encountered an exception: " + s.toString());
		} catch (ClassNotFoundException c) {
			System.err.println("Registrar encountered an exception: " + c.toString());
		} finally {
			this.closeDBConnection();
		}
	}
	
	/**
	 * Helper method that returns the specified user's friends as an int[].
	 * This method is designed to be called while the db connection is open.
	 * @param user_id
	 * @return
	 * @throws SQLException 
	 */
	private int[] getFriendsFromDB(int user_id) throws SQLException {
		String query = "select * from Friends where user_id1 = " 
				+ user_id + " or user_id2 = " + user_id; // since friendships are mutual, check both foreign keys.
		
		// Prepare and execute query
		Statement st = _conn.createStatement();
		ResultSet rs = st.executeQuery(query);
		ArrayList<Integer> friends = new ArrayList<Integer>(); // keep array list of user's friends
		while (rs.next()) {
			int friendId1 = rs.getInt("user_id1");
			int friendId2 = rs.getInt("user_id2");
			friends.add(friendId1 == user_id ? friendId2 : friendId1); // A friend is that who is not self in a friendship
		} 

		rs.close();
		st.close();
		
		// convert array list to int[] and return
		int[] userFriends = new int[friends.size()];
		for (int i = 0; i < friends.size(); i++) {
			userFriends[i] = friends.get(i);
		}

		return userFriends;
	}
	
	/**
	 * Helper method that returns the specified user's likes as an int[].
	 * Method must be called while db connection is open.
	 * @param user_id
	 * @return
	 * @throws SQLException 
	 */
	private int[] getLikesFromDB(int user_id) throws SQLException {
		String query = "select * from Likes where user_id = " + user_id;
		
		Statement st = _conn.createStatement();
		ResultSet rs = st.executeQuery(query);
		ArrayList<Integer> likes = new ArrayList<Integer>(); // create array list of user's likes
		while (rs.next()) {
			int placeId = rs.getInt("place_id");
			likes.add(placeId);
		}

		rs.close();
		st.close();
		
		// convert array list to int[]
		int[] userLikes = new int[likes.size()];
		for (int i = 0; i < likes.size(); i++) {
			userLikes[i] = likes.get(i);
		}

		return userLikes;
	}
	
	/**
	 * Helper method that scrapes User table rows and stores in a HashMap
	 */
	private void populatePlaces() {
		String query = "select * from Places";

		try {
			String res = this.openDBConnection();
			System.out.println(res);
			Statement st = _conn.createStatement();
			ResultSet rs = st.executeQuery(query);
			rs.setFetchSize(st.getMaxRows()); // set result set size to max to improve speed
			while (rs.next()) {
				Place place = new Place(rs);
				_places.put(place.getId(), place);
				}

			rs.close();
			st.close();
		} catch (SQLException s) {
			System.err.println("Registrar encountered an exception: " + s.toString());
		} catch (ClassNotFoundException c) {
			System.err.println("Registrar encountered an exception: " + c.toString());
		} finally {
			this.closeDBConnection();
		}
	}
	
	/**
	 * Helper to populate placeTypes
	 */
	private void populatePlaceTypes() {
		String query = "select * from Place_Types";

		try {
			String res = this.openDBConnection();
			System.out.println(res);
			Statement st = _conn.createStatement();
			ResultSet rs = st.executeQuery(query);
			rs.setFetchSize(st.getMaxRows()); // set result set size to 100 to improve speed
			while (rs.next()) {
				String desc = rs.getString("description");
				int id = rs.getInt("type_id");
				_types.put(id, desc);
				}

			rs.close();
			st.close();
		} catch (SQLException s) {
			System.err.println("Registrar encountered an exception: " + s.toString());
		} catch (ClassNotFoundException c) {
			System.err.println("Registrar encountered an exception: " + c.toString());
		} finally {
			this.closeDBConnection();
		}
	}
	
	/**
	 * Open the database connection.
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
	 * Close the database connection if not already closed.
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
	 * returns the user_ids of all friends of the user with the input user_id, 
	 * null if no friends or user not in db
	 * @param user_id
	 * @return
	 */
	public int[] getFriends(int user_id) {
		User user = _users.get(user_id);
		if (user == null) {
			return null;
		}
		
		return user.getFriends();
	}

	
	/**
	 * returns the place_ids of all the places liked by the user with user_id
	 * @param user_id
	 * @return
	 */
	public int[] getLikes(int user_id) {
		
		User user = _users.get(user_id);
		if (user == null) {
			return null;
		}
		
		return user.getLikes();
		
	}

	
	/**
	 * returns an array of the form [lat,lon] representing the location
	 * of the place with place_id
	 * @param place_id
	 * @return
	 */
	public double[] getLocation(int place_id) {
		if (_places.containsKey(place_id)) {
			Place p = _places.get(place_id);
			double lat = p.getLat();
			double lon = p.getLon();
			double[] output = {lat, lon};
			return output;
		} else {
			return null;
		}
	}
	
	/**
	 * Returns true if the database contains a user with the specified userId
	 * @param userId the user's id
	 * @return
	 */
	public boolean hasUser(int userId) {
		return _users.containsKey(userId);
	}
	
	/**
	 * Returns a HashMap of the frequencies placeTypes of given user's likes. 
	 * @param placeId
	 * @return
	 */
	public HashMap<Integer, Integer> getTypes(int user_id) {
		HashMap<Integer, Integer> types = new HashMap<Integer, Integer>();
		int[] placeIds = _users.get(user_id).getLikes();
		for (int placeId : placeIds) {
			int type_id = _places.get(placeId).getTypeId();
			if (types.get(type_id) != null) {
				types.put(type_id, types.get(type_id) + 1);
			} else {
				types.put(type_id, 1);
			}
		}
		return types;

	}
	
	/**
	 * returns an array of the user_ids of all users in the db who are not friends with the specified user
	 * @return
	 */
	public int[] allNonFriends(int user_id) {
		int[] friends = this.getFriends(user_id);
		HashSet<Integer> friendSet = new HashSet<Integer>();
		for (int f : friends) {
			friendSet.add(f);
		}
		Set<Integer> keys = _users.keySet();
		Set<Integer> nonFriends = new HashSet<Integer>(); // make copy of keyset so we don't modify it
		for (Integer k : keys) {
			nonFriends.add(k);
		}
		nonFriends.removeAll(friendSet); // remove friends from the set of all users
		
		int[] ids = new int[nonFriends.size()];
		int i = 0;
		for (int u : nonFriends) {
			ids[i] = u; 
			i++;
		}
		return ids;
	}
	
	/**
	 * 
	 * @return
	 */
	public int[] allUsers() {
		int[] ids = new int[_users.keySet().size()];
		int i = 0;
		for (int u : _users.keySet()) {
			ids[i] = u; 
			i++;
		}
		return ids;
	}
	
	/**
	 * Getter for all places
	 * @return
	 */
	public int[] allPlaces() {
		int[] ids = new int[_places.keySet().size()];
		int i = 0;
		for (int u : _places.keySet()) {
			ids[i] = u; 
			i++;
		}
		return ids;
	}
	
	/**
	 * Returns the location of a user as a lat, lon array.
	 * @param user_id
	 * @return
	 */
	public double[] getUserLocation(int user_id) {
		double lat = _users.get(user_id).getLat();
		double lon = _users.get(user_id).getLon();
		double [] output = {lat, lon};
		return output;
	}
	
	/**
	 * Returns the user with the specified id or null if not in DB
	 * @param user_id
	 * @return
	 */
	public User getUser(int user_id) {
		return _users.get(user_id);
	}
	
	/**
	 * Returns the place with the specified id or null if not in DB
	 * @param place_id
	 * @return
	 */
	public Place getPlace(int place_id) {
		return _places.get(place_id);
	}
	
	/**
	 * Returns the description of the specified place type.
	 * @param type_id
	 * @return
	 */
	public String getPlaceTypeDescription(int type_id) {
		return _types.get(type_id);
	}
	
	/**
	 * Returns an array of place objects the user likes.
	 * @param user_id
	 * @return
	 */
	public Place[] getLikesAsPlaces(int user_id) {
		User user = _users.get(user_id);
		if (user == null) {
			return null;
		}
		
		int[] place_ids = user.getLikes();
		Place[] places = new Place[place_ids.length];
		for (int i = 0; i < place_ids.length; i++) {
			places[i] = _places.get(place_ids[i]);
		}
		
		return places;
	}
	
	/**
	 * Returns an array of User objects with whom the user is friends with
	 * @param user_id
	 * @return
	 */
	public User[] getFriendsAsUsers(int user_id) {
		User user = _users.get(user_id);
		if (user == null) {
			return null;
		}
		
		int[] friend_ids = user.getFriends();
		User[] friends = new User[friend_ids.length];
		for (int i = 0; i < friend_ids.length; i++) {
			friends[i] = _users.get(friend_ids[i]);
		}
		
		return friends;
	}
	
	/**
	 * Returns place_type_id of given place
	 * @param place_id
	 * @return
	 */
	public int getPlaceType(int place_id) {
		return _places.get(place_id).getTypeId();
	}

	
}
