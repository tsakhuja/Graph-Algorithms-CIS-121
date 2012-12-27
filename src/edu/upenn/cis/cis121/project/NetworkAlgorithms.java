package edu.upenn.cis.cis121.project;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.TreeMap;

/**
 * Class that makes recommendations, determines statistics based on a user's network of friends
 * @author tsakhuja
 *
 */
public class NetworkAlgorithms {
	
	private DBWrapper db;
	private double _numLikesMean;
	private double _numLikesStDev;
	private double _numLikesTypesMean;
	private double _numLikesTypesStDev;
	private double _distancesMean;
	private double _distancesStDev;
	
	
	/**
	 * Constructor
	 * @param dbUser
	 * @param dbPass
	 * @param dbSID
	 * @param dbHost
	 * @param port
	 */
	public NetworkAlgorithms(String dbUser, String dbPass, String dbSID, String dbHost, int port) {
		db = new DBWrapper(dbUser, dbPass, dbSID, dbHost, port);
	}
	
	/**
	 * Close the database connection if not already closed.
	 */
	public void closeDBConnection() {
		db.closeDBConnection();
	}
	
	/**
	 * Computes Bacon distance between two users. If either user does not exist in DB, throws
	 * exception
	 * @param user_id1
	 * @param user_id2
	 * @return
	 * @throws IllegalArgumentException if either user does not exist in DB
	 */
	public int distance(int userId1, int userId2) throws IllegalArgumentException {
		// check if db contains both users
		if (!db.hasUser(userId1) || !db.hasUser(userId2)) {
			throw new IllegalArgumentException();
		} else if (userId1 == userId2) {
			return 0; // Bacon distance between user and itself is 0
		} else{
			return baconBFS(userId1, userId2);
		}

	}
	
	/**
	 * Method that computes bacon distance using a DFS
	 * @param user1
	 * @param user2
	 * @return
	 */
	private int baconBFS(int user1, int user2) {
		HashSet<Integer> marked = new HashSet<Integer>(); // keep Set of marked vertices
		HashMap<Integer, Integer> edgeTo = new HashMap<Integer, Integer>(); // keep Set of back edges
		LinkedList<Integer> queue = new LinkedList<Integer>();
		int start = user1; // starting point of traversal
		
		marked.add(start); // mark first node as seen
		queue.add(start); // add first node to queue
		
		while (!queue.isEmpty()) {
			int v = queue.remove(); // remove next vertex from queue
			for (int i : db.getFriends(v)) {
				if (!marked.contains(i)) { // for each unmarked adjacent vertex
					marked.add(i); // mark it as seen
					queue.add(i); // add it to queue
					edgeTo.put(i, v); // mark back edge in the form of prior vertex id
					if (i == user2) {
						// follow discovery edges from pt B to A to compute path length
						int cursor = user2;
						int length = 0;
						while(cursor != user1) {
							cursor = edgeTo.get(cursor);
							length++;
						}
						return length;
					}
				}
			}
		}
		return -1; // return -1 if no path found.		
	}
	
	/**
	 * Returns the specified number of friend recommendations for the specified user computed
	 * using Dijkstra's algorithm.
	 * @param user_id the user for whom to make recommendations
	 * @param numRec the maximum number of desired friend recommendations
	 * @return a list of recommended friends, specified by their user_ids
	 * @throws IllegalArgumentException if user_id is not in database or numRec < 1
	 */
	public List<Integer> recommendFriends(int user_id, int numRec) throws IllegalArgumentException {
		
		if (!db.hasUser(user_id) || numRec < 1) {
			throw new IllegalArgumentException();
		}
		
		return dijkstraSP(user_id, numRec);
	}
	
	/**
	 * Computes edge weight between two friends used in recommendFriends
	 * @param user_id1
	 * @param user_id2
	 * @return
	 */
	private double edgeWeight(int user_id1, int user_id2) {
		if (user_id1 == user_id2) {
			return 0.0;
		}
		
		int numPlaces = this.numPlacesInCommon(user_id1, user_id2); // number of places in common
		
		// compute number of place types in common
		HashMap<Integer, Integer> user1Types = db.getTypes(user_id1); // get map of user's type_id likes
		HashMap<Integer, Integer> user2Types = db.getTypes(user_id2);
		int numTypes = 0;
		// iterate over user1's place types...if user2 has any shared place types, increment numTypes by
		// the min of user1 and user2's count for that type.
		for (int type_id : user1Types.keySet()) {
			if (user2Types.get(type_id) != null) {
				numTypes += Math.min(user1Types.get(type_id), user2Types.get(type_id));
			}
		}
		
		// return edge weight based on formula
		return 1 / (numPlaces + 0.1 * numTypes + 0.01);
	}
	
	/**
	 * Determines the number of places in common between two users
	 * @param user_id1
	 * @param user_id2
	 * @return
	 */
	private int numPlacesInCommon(int user_id1, int user_id2) {
		if (user_id1 == user_id2) {
			return 0;
		}
		int[] user1Likes = db.getLikes(user_id1);
		int[] user2Likes = db.getLikes(user_id2);
		// compute number of places in common
		HashSet<Integer> u1Places = new HashSet<Integer>();
		HashSet<Integer> placesInCommon = new HashSet<Integer>();
		for (int i = 0; i < user1Likes.length; i++) {
			u1Places.add(user1Likes[i]);
		}
		for (int place_id : user2Likes) {
			if (u1Places.contains(place_id)) {
				placesInCommon.add(place_id);
			}
		}
		return placesInCommon.size(); 
	}
	
	/**
	 * Dijktra's shortest path algorithm
	 * @param user_id
	 * @param numRec
	 * @return
	 */
	private List<Integer> dijkstraSP(int user_id, int numRec) {
		PriorityQueue<Vertex> closestUsers = new PriorityQueue<Vertex>(); // Priority queue to keep sorted list of known edge lengths
		
		int[] users = db.allUsers();
		HashSet<Integer> usersFriends = new HashSet<Integer>(); // get set of user's friends
		for (int u : db.getFriends(user_id)) {
			usersFriends.add(u);
		}

		int maxRecs;
		LinkedList<Integer> sortedRecs = new LinkedList<Integer>();
		
		if (numRec > db.allNonFriends(user_id).length) {
			maxRecs = db.allNonFriends(user_id).length;
		} else {
			maxRecs = numRec;
		}
		
		PriorityQueue<Vertex> pq = new PriorityQueue<Vertex>(); // Priority queue for Dijkstra's
		HashMap<Integer, Double> distTo = new HashMap<Integer, Double>(); // Store of distances
		HashMap<Integer, Vertex> vertexMap = new HashMap<Integer, Vertex>(); // Store of vertexes
		
		for (int user : users) {
			if (user == user_id) {
				distTo.put(user_id, 0.0);  // mark starting point with distance of 0
			} else {
				distTo.put(user, Double.POSITIVE_INFINITY); // mark all nodes as far away
			}
			Vertex v = new Vertex(user, distTo.get(user)); 
			vertexMap.put(user, v); // store map of vertices
			pq.add(v); // Add vertex (which contains its distance) to queue
		}
		while (!pq.isEmpty() && sortedRecs.size() < maxRecs) { // while queue not empty, relax!
			Vertex startV = pq.poll();
			int[] friends = db.getFriends(startV._label);
			for (int adj : friends) {
				// if new distance is smaller than current distance, update
				double distance = this.edgeWeight(startV._label, adj);
				if (distTo.get(adj) > (distTo.get(startV._label) + distance)) { 
					distTo.put(adj, distTo.get(startV._label) + distance); // update distance
					// if the pq has this vertex, update the path length
					if (pq.contains(vertexMap.get(adj))) {
						Vertex removedVertex = vertexMap.get(adj);
						pq.remove(removedVertex);
						removedVertex._pathLength = distTo.get(startV._label) + distance;
						pq.add(removedVertex);
					} else {
						pq.add(new Vertex(adj, distance));
					}
				}
				if (!usersFriends.contains(adj) && !closestUsers.contains(vertexMap.get(adj)) && adj != user_id) { // if not a friend and not already suggested, add this friend to recommended list
					closestUsers.add(vertexMap.get(adj));
				}
			}
		}
		
		// return maxRecs number of closest matches
		for (int i = 0; i < maxRecs; i++) {
			Vertex v = closestUsers.poll();
			if (v != null) {
				sortedRecs.add(v._label);
			}
		}
		return sortedRecs;
	}
	
	/**
	 * Private class representing a vertex; holds a label for the vertex and the path length to it.
	 * @author tsakhuja
	 *
	 */
	private class Vertex implements Comparable<Vertex> {
		private int _label;
		private double _pathLength;
		
		public Vertex(int label, double pathLength) {
			_label = label;
			_pathLength = pathLength;
		}

		@Override
		public int compareTo(Vertex v) {
			if (this._pathLength == v._pathLength) {
				return 0;
			} else if (this._pathLength < v._pathLength) {
				return -1;
			} else {
				return 1;
			}
		}	
		
	}
	
	/**
	 * Returns JSON formatted string of specified number of geographically closest friends
	 * and most suitable places for them to visit together.
	 * @param user_id
	 * @param maxFriends
	 * @param maxPlaces
	 * @return
	 * @throws IllegalArgumentException
	 */
	public String recommendActivities(int user_id, int maxFriends, int maxPlaces)
			throws IllegalArgumentException {
		// check for valid arguments
		if (user_id < 0 || !db.hasUser(user_id) || maxFriends < 1 || maxPlaces < 1) {
			throw new IllegalArgumentException();
		}
		
		User user = db.getUser(user_id);
		// create array of maxFriends closest friends, ordered by closeness
		TreeMap<Double, Integer> friendDistances = new TreeMap<Double, Integer>();
		// keep track of how many likes each place has among close friends
		TreeMap<Integer, Integer> placeLikes = new TreeMap<Integer, Integer>(); 
		
		int[] friendIds = db.getFriends(user_id);
		int[] closestFriends = new int[maxFriends < friendIds.length ? maxFriends : friendIds.length];
		for (int friendId : friendIds) {
			friendDistances.put(this.userDistance(user_id, friendId), friendId);
		}
		for (int i = 0; i < closestFriends.length; i++) {
			int friendId = friendDistances.pollFirstEntry().getValue();
			closestFriends[i] = friendId;
			int [] likes = db.getLikes(friendId);
			for (int l : likes) { // keep count of how many likes each place has
				if (placeLikes.containsKey(l)) {
					placeLikes.put(l, placeLikes.get(l) + 1);
				} else {
					placeLikes.put(l, 1);
				}
			}
		}
		// now +1 each place that the user likes
		for (int placeLike : placeLikes.keySet()) {
			if (user.likes(placeLike)) {
				placeLikes.put(placeLike, placeLikes.get(placeLike) + 1);
			}
		}
		
		// determine center of friends
		double[] center = new double[2];
		for (int i : closestFriends) {
			double[] location = db.getUserLocation(i);
			center[0] += location[0]/(closestFriends.length + 1);
			center[1] += location[1]/(closestFriends.length + 1);
		} 
		center[0] += user.getLat()/(closestFriends.length + 1); // add user's location
		center[1] += user.getLon()/(closestFriends.length + 1);
		
		// determine maxPlaces number of recommended places
		int[] allPlaces = db.allPlaces();
		TreeMap<Double, Integer> placeMap = new TreeMap<Double, Integer>();
		for (int p : allPlaces) {
			int numFriendLikes = placeLikes.get(p) != null ? placeLikes.get(p) : 0; // check for null
			placeMap.put(this.suitability(p, numFriendLikes	, center), p); 
		}
		
		ArrayList<Integer> topPlaces = new ArrayList<Integer>();
		for (int i = 0; i < maxPlaces && !placeMap.isEmpty(); i++) {
			topPlaces.add(placeMap.pollLastEntry().getValue()); // populate topPlaces
		}
		
		
		return this.formatJSON(user_id, closestFriends, topPlaces); // return JSON formatted results
	}
	
	/**
	 * Helper method to compute geographical distance between two users
	 * @param user_id1
	 * @param user_id2
	 * @return
	 */
	private double userDistance(int user_id1, int user_id2) {
		double[] user1Location = db.getUserLocation(user_id1);
		double lat1 = user1Location[0];
		double lon1 = user1Location[1];
		
		double[] user2Location = db.getUserLocation(user_id2);
		double lat2 = user2Location[0];
		double lon2 = user2Location[1];
		
		return Math.sqrt( Math.pow(lat1-lat2, 2) + Math.pow(lon1-lon2, 2) );
	}
	
	/**
	 * Computes the suitability score for a given place
	 * @param place_id
	 * @param numLikes
	 * @param center
	 * @return
	 */
	private double suitability(int place_id, int numLikes, double[] center) {
		double[] location = db.getLocation(place_id);
		double dist = Math.sqrt( Math.pow(location[0]-center[0], 2) + Math.pow(location[1]-center[1], 2) );
		return numLikes / (dist + 0.01);
	}
	
	/**
	 * 
	 * @return
	 */
	private String formatJSON(int user_id, int[] friend_ids, ArrayList<Integer> place_ids) {
		StringBuffer buf = new StringBuffer("{ \n");
		
		// add user information
		User user = db.getUser(user_id);
		buf.append("\"user\": {\"user_id\":" + user_id + ",\n"); 
		buf.append("\"first_name\":\"" + user.getFirstName() + "\",\n");
		buf.append("\"last_name\":\"" + user.getLastName() + "\",\n");
		buf.append("\"latitude\":" + user.getLat() + ",\n");
		buf.append("\"longitude\":" + user.getLon() + "\n");
		buf.append("},\n");
		
		// add friends' information
		buf.append("\"friends\": {\n");
		for (int i = 0; i < friend_ids.length; i++) {
			User friend = db.getUser(friend_ids[i]);
			buf.append("\"" + i + "\": {\"user_id\":" + friend_ids[i] + ",\n"); 
			buf.append("\"first_name\":\"" + friend.getFirstName() + "\",\n");
			buf.append("\"last_name\":\"" + friend.getLastName() + "\",\n");
			buf.append("\"latitude\":" + friend.getLat() + ",\n");
			buf.append("\"longitude\":" + friend.getLon() + "}");
			if (i < friend_ids.length - 1) { // last friend has no comma
				buf.append(",\n");
			} else {
				buf.append("\n");
			}
		}
		buf.append("},\n");
		
		// add place information
		buf.append("\"places\": {\n");
		for (int i = 0; i < place_ids.size(); i++) {
			Place place = db.getPlace(place_ids.get(i));
			buf.append("\"" + i + "\": {\"place_id\":" + place_ids.get(i) + ",\n"); 
			buf.append("\"place_name\":\"" + place.getName() + "\",\n");
			buf.append("\"description\":\"" + db.getPlaceTypeDescription(place.getTypeId()) + "\",\n");
			buf.append("\"latitude\":" + place.getLat() + ",\n");
			buf.append("\"longitude\":" + place.getLon() + "}");
			if (i < place_ids.size() - 1) { // last place has no comma
				buf.append(",\n");
			} else {
				buf.append("\n");
			}
		}
		buf.append("}\n");
		
		buf.append("}\n");
		
		return buf.toString();		
		
	}
	
	/**
	 * Recommends places for user
	 * @param user_id
	 * @param numRec
	 * @return
	 * @throws IllegalArgumentException
	 */
	public List<Integer> recommendPlaces(int user_id, int numRec) throws IllegalArgumentException {
		if (!db.hasUser(user_id) || numRec < 1) {
			throw new IllegalArgumentException();
		}
		
		HashMap<Integer, Double> numLikesDist = new HashMap<Integer, Double>(); // distribution of number of likes by friends, weighted by user similarity
		HashMap<Integer, Double> numLikesTypesDist = new HashMap<Integer, Double>(); // distribution of number of likes of place of same type by user
		HashMap<Integer, Double> distancesDist = new HashMap<Integer, Double>(); // distribution of distances of places from user
		
		int[] places = db.allPlaces();
		User user = db.getUser(user_id);
		for (int p : places) {
			// if user doesn't already like the place, compute the score of its parameters
			if (!user.likes(p)) {
				double[] params = this.computePlaceParameters(p, user_id);
				numLikesDist.put(p, params[0]);
				numLikesTypesDist.put(p, params[1]);
				distancesDist.put(p, params[2]);
			}
		}
		
		// compute means and stdevs of each parameter
		_numLikesMean = this.mean(numLikesDist.values());
		_numLikesStDev = this.stdev(numLikesDist.values(), _numLikesMean);
		
		_numLikesTypesMean = this.mean(numLikesTypesDist.values());
		_numLikesTypesStDev = this.stdev(numLikesTypesDist.values(), _numLikesTypesMean);
		
		_distancesMean = this.mean(distancesDist.values());
		_distancesStDev = this.stdev(distancesDist.values(), _distancesMean);
		
		// Compute suitabilities
		TreeMap<Double, Integer> suitabilities = new TreeMap<Double, Integer>();
		for (int p : numLikesDist.keySet()) {
			suitabilities.put(this.computeSuitability(numLikesDist.get(p), numLikesTypesDist.get(p), distancesDist.get(p)), p);
		}
		
		// get matches
		ArrayList<Integer> matches = new ArrayList<Integer>();
		for (int i = 0; i < numRec && suitabilities.size() > 0; i ++) {
			matches.add(suitabilities.pollLastEntry().getValue());
		}
		
		return matches;
	}
	
	/**
	 * Computes average
	 * @param vals
	 * @return
	 */
	private double mean(Collection<Double> vals) {
		double mean = 0;
		for (double i : vals) {
			mean += i / vals.size();
		}
		return mean;
	}
	
	/**
	 * Computes standard deviation
	 * @param vals
	 * @return
	 */
	private double stdev(Collection<Double> vals, double mean) {
		double variance = 0;
		for (double i : vals) {
			variance += Math.pow(i - mean, 2);
		}
		return Math.sqrt(variance / vals.size());
	}
	
	/**
	 * Computes the factors that enter into the suitability score for a given place 
	 * in the recommendations algorithm in the form of [numLikes, numLikesSameType, distance]
	 * @param place_id
	 * @param user_id
	 * @return
	 */
	private double[] computePlaceParameters(int place_id, int user_id) {
		int placeType = db.getPlaceType(place_id);
		Place[] likes = db.getLikesAsPlaces(user_id);
		User[] friends = db.getFriendsAsUsers(user_id);
		
		int numLikes = 0;
		for (User friend : friends) {
			if (friend.likes(place_id)) {
				numLikes += this.numPlacesInCommon(user_id, friend.getId()); // weight common like by compute number of places in common
			}
		}
		
		int numLikesSameType = 0; // number of likes the user has of the same type as place_id
		for (Place p : likes) {
			if (placeType == p.getTypeId()) {
				numLikesSameType++;
			}
		}
		
		double[] placeLoc = db.getLocation(place_id);
		double[] userLoc = db.getUserLocation(user_id);
		double distance = Math.sqrt(Math.pow(placeLoc[0] - userLoc[0], 2) + Math.pow(placeLoc[1] - userLoc[1], 2));
		
		return new double[] {numLikes, numLikesSameType, distance};
	}
	
	/**
	 * Computes suitability based on my formula
	 * @param val
	 * @param mean
	 * @param stdev
	 * @return
	 */
	private double computeSuitability(double numLikes, double numLikesTypes, double distance) {
		return (numLikes - _numLikesMean) / _numLikesStDev + 
					(numLikesTypes - _numLikesTypesMean) / _numLikesTypesStDev + _distancesStDev / (distance - _distancesMean);
	}
	
	/**
	 * Method used to evaluate the effectiveness of the place rec's algorithm
	 * @param user_id
	 * @param place_id
	 * @return
	 */
	public int numLikesAmongFriends(int user_id, int place_id){
		User[] friends = db.getFriendsAsUsers(user_id);
		int numLikes = 0;
		for (User friend : friends) {
			if (friend.likes(place_id)) {
				numLikes += this.numPlacesInCommon(user_id, friend.getId());
			}
		}
		System.out.println("Mean num likes: " + _numLikesMean);
		return numLikes;
	}
	
}
