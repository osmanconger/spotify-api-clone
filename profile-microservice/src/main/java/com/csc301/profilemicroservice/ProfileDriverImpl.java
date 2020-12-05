package com.csc301.profilemicroservice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;

import org.springframework.stereotype.Repository;
import org.neo4j.driver.v1.Transaction;
import org.neo4j.driver.v1.exceptions.ClientException;

@Repository
public class ProfileDriverImpl implements ProfileDriver {

	Driver driver = ProfileMicroserviceApplication.driver;

	public static void InitProfileDb() {
		String queryStr;

		try (Session session = ProfileMicroserviceApplication.driver.session()) {
			try (Transaction trans = session.beginTransaction()) {
				queryStr = "CREATE CONSTRAINT ON (nProfile:profile) ASSERT exists(nProfile.userName)";
				trans.run(queryStr);

				queryStr = "CREATE CONSTRAINT ON (nProfile:profile) ASSERT exists(nProfile.password)";
				trans.run(queryStr);

				queryStr = "CREATE CONSTRAINT ON (nProfile:profile) ASSERT nProfile.userName IS UNIQUE";
				trans.run(queryStr);

				trans.success();
			}
			session.close();
		}
	}
	
	@Override
	public DbQueryStatus createUserProfile(String userName, String fullName, String password) {
		String queryStr;
		DbQueryStatus status;

		try (Session session = ProfileMicroserviceApplication.driver.session()) {
			try (Transaction trans = session.beginTransaction()) {
				Map<String, Object> parameters = new HashMap<String, Object>();
				parameters.put("userName", userName);
				parameters.put("fullName", fullName);
				parameters.put("password", password);
				parameters.put("plName", userName+"-favorites");
				
				queryStr = "CREATE (:profile{userName:{userName}, fullName:{fullName}," +
						"password:{password}})-[:created]->(:playlist {plName:{plName}})";
				StatementResult result = trans.run(queryStr, parameters);	
				trans.success();
				
				status = new DbQueryStatus(result.consume().toString(), DbQueryExecResult.QUERY_OK);
			} catch(ClientException e) {
				status = new DbQueryStatus(e.getMessage(), DbQueryExecResult.QUERY_ERROR_GENERIC);
			} catch(Exception e) {
				status = new DbQueryStatus(e.getMessage(), DbQueryExecResult.QUERY_ERROR_GENERIC);
			}
			session.close();
		}
		return status;
	}

	@Override
	public DbQueryStatus followFriend(String userName, String frndUserName) {
		String queryStr;
		DbQueryStatus status;

		try (Session session = ProfileMicroserviceApplication.driver.session()) {
			try (Transaction trans = session.beginTransaction()) {
				Map<String, Object> parameters = new HashMap<String, Object>();
				parameters.put("userName", userName);
				parameters.put("frndUserName", frndUserName);
				
				queryStr = "MATCH (a:profile{userName:{userName}}),(b:profile{userName:{frndUserName}})\r\n"
						+ "MERGE (a)-[r:follows]->(b)\r\n"
						+ "RETURN r";
				StatementResult result = trans.run(queryStr, parameters);	
				trans.success();
				if(result.hasNext())
					status = new DbQueryStatus(result.consume().toString(), DbQueryExecResult.QUERY_OK);
				else
					status = new DbQueryStatus(result.consume().toString(), DbQueryExecResult.QUERY_ERROR_NOT_FOUND);
			} catch(Exception e) {
				status = new DbQueryStatus(e.getMessage(), DbQueryExecResult.QUERY_ERROR_GENERIC);
			}
			session.close();
		}
		return status;
	}

	@Override
	public DbQueryStatus unfollowFriend(String userName, String frndUserName) {
		String queryStr;
		DbQueryStatus status;

		try (Session session = ProfileMicroserviceApplication.driver.session()) {
			try (Transaction trans = session.beginTransaction()) {
				Map<String, Object> parameters = new HashMap<String, Object>();
				parameters.put("userName", userName);
				parameters.put("frndUserName", frndUserName);
				
				queryStr = "MATCH (a:profile{userName:{userName}})-[r:follows]->(b:profile{userName:{frndUserName}})\r\n"
						+ "WITH r, count(*)  AS r2\r\n"
						+ "DELETE r\r\n"
						+ "RETURN r2";
				StatementResult result = trans.run(queryStr, parameters);	
				trans.success();
				if(result.hasNext())
					status = new DbQueryStatus(result.consume().toString(), DbQueryExecResult.QUERY_OK);
				else
					status = new DbQueryStatus(result.consume().toString(), DbQueryExecResult.QUERY_ERROR_NOT_FOUND);
			} catch(Exception e) {
				status = new DbQueryStatus(e.getMessage(), DbQueryExecResult.QUERY_ERROR_GENERIC);
			}
			session.close();
		}
		return status;
	}
	
	public ArrayList<String> getAllFriends(String userName) {
		ArrayList<String> friends = new ArrayList<>();
		List<Record> friendsRecords;
		String queryStr;

		try (Session session = ProfileMicroserviceApplication.driver.session()) {
			try (Transaction trans = session.beginTransaction()) {
				Map<String, Object> parameters = new HashMap<String, Object>();
				parameters.put("userName", userName);
				
				queryStr = "MATCH (:profile{userName:{userName}})-[f:follows]->(u:profile)\r\n"
						+ "RETURN u.userName";
				StatementResult result = trans.run(queryStr, parameters);	
				friendsRecords = result.list();
		        for (int i = 0; i < friendsRecords.size(); i++) {
		        	friends.add(friendsRecords.get(i).get("u.userName").toString());
		        }
								
				trans.success();
			}
			session.close();
		}
		
		return friends;
	}

	public ArrayList<String> getAllLikedSongs(String userName) {
		ArrayList<String> songs = new ArrayList<>();
		List<Record> songsRecords;
		String queryStr;

		try (Session session = ProfileMicroserviceApplication.driver.session()) {
			try (Transaction trans = session.beginTransaction()) {
				Map<String, Object> parameters = new HashMap<String, Object>();
				parameters.put("plName", userName+"-favorites");
				
				queryStr = "MATCH (:playlist{plName:{plName}})-[i:includes]->(s:song)\r\n"
						+ "RETURN s.songId";
				StatementResult result = trans.run(queryStr, parameters);	
				songsRecords = result.list();
		        for (int i = 0; i < songsRecords.size(); i++) {
		        	songs.add(songsRecords.get(i).get("s.songId").toString());
		        }
								
				trans.success();
			}
			session.close();
		}
		
		return songs;
	}

	@Override
	public DbQueryStatus getAllSongFriendsLike(String userName) {
		ArrayList<String> friends = getAllFriends(userName);
		return null;
	}
}
