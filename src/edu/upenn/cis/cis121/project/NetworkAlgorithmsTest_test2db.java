package edu.upenn.cis.cis121.project;

import static org.junit.Assert.*;

import java.util.LinkedList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class NetworkAlgorithmsTest_test2db {
	NetworkAlgorithms na;

	@Before
	public void setUp() throws Exception {
		na = new NetworkAlgorithms("user_id", "password","CIS", "fling.seas.upenn.edu", 1521);
	}

	
	// use pop_test_2.sql db
	@Test
	public void testRecFriends() {
		List<Integer> recs = na.recommendFriends(1, 4);
		assertTrue(recs.size() == 4);
		System.out.println(recs.get(0));
		System.out.println(recs.get(1));
		System.out.println(recs.get(2));
		System.out.println(recs.get(3));
		//should have 4,5,8,9
		for (int i : recs) {
			assertTrue(i == 4 || i == 5 || i == 8 || i == 9 );
		}

		
	}
	
	@Test
	public void testRecFriends7() {
		List<Integer> recs = na.recommendFriends(1, 5);
		assertTrue(recs.size() == 5);
		for (int i : recs) {
			assertTrue(i == 4 || i == 5 || i == 8 || i == 9 || i == 11);
			
		}
		
		System.out.println(recs.get(0));
		System.out.println(recs.get(1));
		System.out.println(recs.get(2));
		System.out.println(recs.get(3));
		System.out.println(recs.get(4));
		
		assertTrue(!recs.contains(6));
		
		//should have 4,5,8,9,11

		
	}
	
	@Test
	public void testRecFriendsMax() {
		List<Integer> recs = na.recommendFriends(1, 10);
		assertTrue(recs.size() == 8);
		
	}
	
	@Test
	public void testBacon() {
		assertTrue(na.distance(1, 1) == 0);
		assertTrue(na.distance(1, 2) == 1);
		assertTrue(na.distance(1, 3) == 2);
		assertTrue(na.distance(1, 6) == 3);
		assertTrue(na.distance(6, 11) == 6);
	}
	
	@Test
	public void testRecommendPlaces() {
		List<Integer> recs = na.recommendPlaces(7, 2);
		for (int rec : recs) {
			System.out.println(rec); // expect 5, 4, or 3
		}

	}
	

}
