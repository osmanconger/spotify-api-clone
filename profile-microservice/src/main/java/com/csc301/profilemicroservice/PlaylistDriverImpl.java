package com.csc301.profilemicroservice;

import java.util.HashMap;
import java.util.Map;

import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.springframework.stereotype.Repository;
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
				Map<String, Object> parameters = new HashMap<String, Object>();
				parameters.put("plName", userName+"-favorites");
				parameters.put("songId", songId);
				
				queryStr = "MATCH (a:playlist{plName:{plName}}), (s:song{songId:{songId}})\r\n"
						+ "MERGE (a)-[i:includes]->(s)\r\n"
						+ "RETURN i";
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
	public DbQueryStatus unlikeSong(String userName, String songId) {
		String queryStr;
		DbQueryStatus status;

		try (Session session = ProfileMicroserviceApplication.driver.session()) {
			try (Transaction trans = session.beginTransaction()) {
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
