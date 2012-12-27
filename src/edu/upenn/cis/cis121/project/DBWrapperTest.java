package edu.upenn.cis.cis121.project;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class DBWrapperTest {
	DBWrapper db;
	@Before
	public void setUp() throws Exception {
		db = new DBWrapper("tsakhuja", "chotu6718","CIS", "fling.seas.upenn.edu", 1521);
	}

	@Test
	public void getFriendsTest() {
		int[] friends = db.getFriends(0);
		assertTrue(friends.length == 12);
		assertTrue(friends[0] == 58);
		assertTrue(friends[11] == 571);
	}
	
	@Test
	public void getLikesTest() {
		int[] likes = db.getLikes(0);
		assertTrue(likes.length == 11);
		assertTrue(likes[0] == 2);
		assertTrue(likes[10] == 34);
		int[] like2 = db.getLikes(6);
	}
	
	@Test
	public void getLocationTest() {
		double[] location = db.getLocation(0);
		assertTrue(location[0] == 73);
		assertTrue(location[1] == 263);
	}

}
