package com.csc301.profilemicroservice;

import java.util.HashMap;
import java.util.Map;

import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestTemplate;
import org.neo4j.driver.v1.Transaction;

@Repository
public class PlaylistDriverImpl implements PlaylistDriver {

	Driver driver = ProfileMicroserviceApplication.driver;

	public static void InitPlaylistDb() {
		String queryStr;

		try (Session session = ProfileMicroserviceApplication.driver.session()) {
			try (Transaction trans = session.beginTransaction()) {
				queryStr = "CREATE CONSTRAINT ON (nPlaylist:playlist) ASSERT exists(nPlaylist.plName)";
				trans.run(queryStr);
				trans.success();
			}
			session.close();
		}
	}

	@Override
	public DbQueryStatus likeSong(String userName, String songId) {
		String queryStr;
		DbQueryStatus status;

		try (Session session = ProfileMicroserviceApplication.driver.session()) {
			try (Transaction trans = session.beginTransaction()) {
				RestTemplate restTemplate = new RestTemplate();
				Map<String, Object> parameters = new HashMap<String, Object>();
				parameters.put("plName", userName+"-favorites");
				parameters.put("songId", songId);
				
				queryStr = "MATCH (a:playlist{plName:{plName}}), (s:song{songId:{songId}})\r\n"
						+ "MATCH (a)-[i0:includes]->(s)\r\n"
						+ "RETURN i0";
				StatementResult result0 = trans.run(queryStr, parameters);
				if(!result0.hasNext()) {
				    String uri = "http://localhost:3001/updateSongFavouritesCount/"+songId+"?shouldDecrement=false";
				    restTemplate.exchange(uri, HttpMethod.PUT, null, String.class);
				}
				
				queryStr = "MATCH (a:playlist{plName:{plName}}), (s:song{songId:{songId}})\r\n"
						+ "MERGE (a)-[i:includes]->(s)\r\n"
						+ "RETURN i";
				StatementResult result = trans.run(queryStr, parameters);	
				trans.success();
				
				if(result.hasNext()) {
					status = new DbQueryStatus(result.consume().toString(), DbQueryExecResult.QUERY_OK);
				}
				else {
					status = new DbQueryStatus(result.consume().toString(), DbQueryExecResult.QUERY_ERROR_NOT_FOUND);
				}
			} catch(Exception e) {
				System.out.println(e);
				status = new DbQueryStatus(e.getMessage(), DbQueryExecResult.QUERY_ERROR_GENERIC);
			}
			session.close();
		}
		return status;
	}

	@Override
	public DbQueryStatus unlikeSong(String userName, String songId) {
		String queryStr;
		DbQueryStatus status;

		try (Session session = ProfileMicroserviceApplication.driver.session()) {
			try (Transaction trans = session.beginTransaction()) {
				RestTemplate restTemplate = new RestTemplate();
				Map<String, Object> parameters = new HashMap<String, Object>();
				parameters.put("plName", userName+"-favorites");
				parameters.put("songId", songId);
				
				queryStr = "MATCH (a:playlist{plName:{plName}}), (s:song{songId:{songId}})\r\n"
						+ "MATCH (a)-[i:includes]->(s)\r\n"
						+ "WITH i, count(*) AS i2\r\n"
						+ "DELETE i\r\n"
						+ "RETURN i2";
				StatementResult result = trans.run(queryStr, parameters);	
				trans.success();
				
				if(result.hasNext()) {
					status = new DbQueryStatus(result.consume().toString(), DbQueryExecResult.QUERY_OK);
				    String uri = "http://localhost:3001/updateSongFavouritesCount/"+songId+"?shouldDecrement=true";
				    restTemplate.exchange(uri, HttpMethod.PUT, null, String.class);
				}
				else {
					status = new DbQueryStatus(result.consume().toString(), DbQueryExecResult.QUERY_ERROR_NOT_FOUND);
				}
			} catch(Exception e) {
				status = new DbQueryStatus(e.getMessage(), DbQueryExecResult.QUERY_ERROR_GENERIC);
			}
			session.close();
		}
		return status;
	}

	@Override
	public DbQueryStatus deleteSongFromDb(String songId) {
		String queryStr;
		DbQueryStatus status;

		try (Session session = ProfileMicroserviceApplication.driver.session()) {
			try (Transaction trans = session.beginTransaction()) {
				Map<String, Object> parameters = new HashMap<String, Object>();
				parameters.put("songId", songId);
				
				queryStr = "MATCH (s:song{songId:{songId}})\r\n"
						+ "DELETE s";
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

	public DbQueryStatus addSongToDB(String songId) {
		String queryStr;
		DbQueryStatus status;

		try (Session session = ProfileMicroserviceApplication.driver.session()) {
			try (Transaction trans = session.beginTransaction()) {
				Map<String, Object> parameters = new HashMap<String, Object>();
				parameters.put("songId", songId);
				
				queryStr = "MERGE (s:song{songId:{songId}}) RETURN s";
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

}
