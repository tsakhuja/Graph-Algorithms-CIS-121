package edu.upenn.cis.cis121.project;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class NetworkAlgorithmsTest {
	NetworkAlgorithms na;

	@Before
	public void setUp() throws Exception {
		na = new NetworkAlgorithms("user_id", "password","CIS", "fling.seas.upenn.edu", 1521);
	}

//	@Test
//	public void testDistance() {
//		assertTrue(na.distance(0, 0) == 0);
//		//assertEquals(na.distance(-1, -1), null);
//		//assertEquals(na.distance(0, -1), null);
//		assertTrue(na.distance(0, 58) == 1);
//		assertTrue(na.distance(58, 0) == 1);
//		assertTrue(na.distance(0, 1) == 2);
//		//assertTrue(na.distance(58, 1) == -1);
//	}
	
//	@Test
//	public void testRecommendFriends() {
//		List<Integer> recs = na.recommendFriends(2, 2);
//		assertTrue(recs.size() == 2);
//		assertTrue(recs.get(0) == 5);
//		assertTrue(recs.get(1) == 4);
//		
//		List<Integer> recs1 = na.recommendFriends(5, 2);
//		assertTrue(recs1.size() == 2);
//		assertTrue(recs1.get(0) == 4);
//		
//		List<Integer> recs2 = na.recommendFriends(3, 2);
//		assertTrue(recs2.size() == 0);
//		
//		List<Integer> recs3 = na.recommendFriends(0, 3);
//		assertTrue(recs3.size() == 3);
//		assertTrue(recs3.get(0) == 4);
//		assertTrue(recs3.get(1) == 2);
//	}
	

	
	@Test
	public void testRecommendedActivities() {
		String s = na.recommendActivities(197, 2, 4);
		System.out.println(s);
	}
	
//	@Test
//	public void testRecommendPlaces() {
//		List<Integer> recs = na.recommendPlaces(197, 5);
//		for (int rec : recs) {
//			System.out.println(rec); // expect 5, 4, or 3
//			System.out.println("number of likes by friends: " + na.numLikesAmongFriends(197, rec));
//		}
//
//	}
	

}
