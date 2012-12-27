package edu.upenn.cis.cis121.project;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Class representing a place object
 * @author tsakhuja
 *
 */
public class Place {
	private int _id;
	private String _name;
	private int _typeId;
	private double _lat;
	private double _lon;
	
	/**
	 * Constructor
	 * @param rs the SQL query result set used to define the place
	 * @throws SQLException
	 */
	public Place(ResultSet rs) throws SQLException {
		_id = rs.getInt("place_id");
		_name = rs.getString("place_name");
		_typeId = rs.getInt("type_id");
		_lat = rs.getDouble("latitude");
		_lon = rs.getDouble("longitude");
	}
	
	// Accessors 
	/**
	 * Returns the the id of the place
	 * @return
	 */
	public int getId() {
		return _id;
	}
	
	/**
	 * Returns the type id of the place
	 * @return
	 */
	public int getTypeId() {
		return _typeId;
	}
	
	/**
	 * Returns the name of the place
	 * @return
	 */
	public String getName() {
		return _name;
	}
	
	/**
	 * Returns the latitude of the place
	 * @return
	 */
	public double getLat() {
		return _lat;
	}
	
	/**
	 * Returns the longitude of the place
	 * @return
	 */
	public double getLon() {
		return _lon;
	}
	
}
