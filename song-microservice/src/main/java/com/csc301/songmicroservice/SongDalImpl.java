package com.csc301.songmicroservice;

import javax.websocket.Session;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

@Repository
public class SongDalImpl implements SongDal {

	private final MongoTemplate db;

	@Autowired
	public SongDalImpl(MongoTemplate mongoTemplate) {
		this.db = mongoTemplate;
	}

	@Override
	public DbQueryStatus addSong(Song songToAdd) {
		
		DbQueryStatus status;

		try {
			db.insert(songToAdd);
			status = new DbQueryStatus("song inserted succesfully", DbQueryExecResult.QUERY_OK);
			status.setData(songToAdd);
		} catch(Exception e) {
			status = new DbQueryStatus(e.getMessage(), DbQueryExecResult.QUERY_ERROR_GENERIC);
		}
		
		return status;
	}

	@Override
	public DbQueryStatus findSongById(String songId) {
		
		DbQueryStatus status;
		

		try {
			Song song = db.findById(songId, Song.class);
			if(song != null) {
				status = new DbQueryStatus("song retrieved succesfully", DbQueryExecResult.QUERY_OK);
				status.setData(song);
			} else {
				status = new DbQueryStatus("song doesn't exist", DbQueryExecResult.QUERY_ERROR_NOT_FOUND);

			}
		} catch(Exception e) {
			status = new DbQueryStatus(e.getMessage(), DbQueryExecResult.QUERY_ERROR_GENERIC);
		}
		
		return status;
		
	}

	@Override
	public DbQueryStatus getSongTitleById(String songId) {
		
		DbQueryStatus status;

		try {
			Song song = db.findById(songId, Song.class);
			if(song != null) {
				status = new DbQueryStatus("song retrieved succesfully", DbQueryExecResult.QUERY_OK);
				status.setData(song.getSongName());
			} else {
				status = new DbQueryStatus("song doesn't exist", DbQueryExecResult.QUERY_ERROR_NOT_FOUND);

			}
		} catch(Exception e) {
			status = new DbQueryStatus(e.getMessage(), DbQueryExecResult.QUERY_ERROR_GENERIC);
		}
		
		return status;
		
	}

	@Override
	public DbQueryStatus deleteSongById(String songId) {
		
		DbQueryStatus status;

		try {
			Song song = db.findById(songId, Song.class);
			if(song != null) {
				db.remove(song);
				status = new DbQueryStatus("song deleted succesfully", DbQueryExecResult.QUERY_OK);
			} else {
				status = new DbQueryStatus("song doesn't exist", DbQueryExecResult.QUERY_ERROR_NOT_FOUND);

			}
		} catch(Exception e) {
			status = new DbQueryStatus(e.getMessage(), DbQueryExecResult.QUERY_ERROR_GENERIC);
		}
		
		return status;
	}

	@Override
	public DbQueryStatus updateSongFavouritesCount(String songId, boolean shouldDecrement) {
		
		DbQueryStatus status;

		try {
			Song song = db.findById(songId, Song.class);
			if(song != null) {
				Query query = new Query(Criteria.where("_id").is(song.getId()));
				long newFavCount = song.getSongAmountFavourites() - ((shouldDecrement) ? 1 : -1);
				Update update = new Update().set("songAmountFavourites", newFavCount);
				db.updateFirst(query, update, Song.class);
				status = new DbQueryStatus("song updated succesfully", DbQueryExecResult.QUERY_OK);
			} else {
				status = new DbQueryStatus("song doesn't exist", DbQueryExecResult.QUERY_ERROR_NOT_FOUND);

			}
		} catch(Exception e) {
			status = new DbQueryStatus(e.getMessage(), DbQueryExecResult.QUERY_ERROR_GENERIC);
		}
		
		return status;
	}
}