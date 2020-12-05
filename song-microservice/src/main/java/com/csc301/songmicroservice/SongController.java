package com.csc301.songmicroservice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/")
public class SongController {
	
	@Bean
	public RestTemplate getRestTemplate() {
		return new RestTemplate();
	}
	
	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private final SongDal songDal;

	private OkHttpClient client = new OkHttpClient();

	
	public SongController(SongDal songDal) {
		this.songDal = songDal;
	}

	
	@RequestMapping(value = "/getSongById/{songId}", method = RequestMethod.GET)
	public @ResponseBody Map<String, Object> getSongById(@PathVariable("songId") String songId,
			HttpServletRequest request) {

		Map<String, Object> response = new HashMap<String, Object>();
		response.put("path", String.format("GET %s", Utils.getUrl(request)));
		System.out.println("its working 1");

		DbQueryStatus status = songDal.findSongById(songId);;
		
		response.put("message", status.getMessage());
		response = Utils.setResponseStatus(response, status.getdbQueryExecResult(), status.getData());

		return response;
	}

	
	@RequestMapping(value = "/getSongTitleById/{songId}", method = RequestMethod.GET)
	public @ResponseBody Map<String, Object> getSongTitleById(@PathVariable("songId") String songId,
			HttpServletRequest request) {

		Map<String, Object> response = new HashMap<String, Object>();
		response.put("path", String.format("GET %s", Utils.getUrl(request)));

		DbQueryStatus status = songDal.getSongTitleById(songId);;
		
		response.put("message", status.getMessage());
		response = Utils.setResponseStatus(response, status.getdbQueryExecResult(), status.getData());

		return response;
	}

	
	@RequestMapping(value = "/deleteSongById/{songId}", method = RequestMethod.DELETE)
	public @ResponseBody Map<String, Object> deleteSongById(@PathVariable("songId") String songId,
			HttpServletRequest request) {

		Map<String, Object> response = new HashMap<String, Object>();
		response.put("path", String.format("DELETE %s", Utils.getUrl(request)));
		
		DbQueryStatus status = songDal.deleteSongById(songId);
		
		if(status.getdbQueryExecResult() == DbQueryExecResult.QUERY_OK) {
			restTemplate.exchange("http://localhost:3002/deleteAllSongsFromDb/" + songId, HttpMethod.PUT, null, String.class);
		} 
		

		
		response.put("message", status.getMessage());
		response = Utils.setResponseStatus(response, status.getdbQueryExecResult(), status.getData());
		
		return response;
	}

	
	@RequestMapping(value = "/addSong", method = RequestMethod.POST)
	public @ResponseBody Map<String, Object> addSong(@RequestParam Map<String, String> params,
			HttpServletRequest request) {
		
 		Map<String, Object> response = new HashMap<String, Object>();
		response.put("path", String.format("POST %s", Utils.getUrl(request)));
		
		DbQueryStatus status;
		
		if(params.get(Song.KEY_SONG_NAME) != null && params.get(Song.KEY_SONG_ARTIST_FULL_NAME) != null && params.get(Song.KEY_SONG_ALBUM) != null) {
			Song _song = new Song(params.get(Song.KEY_SONG_NAME), params.get(Song.KEY_SONG_ARTIST_FULL_NAME), params.get(Song.KEY_SONG_ALBUM));
			status = songDal.addSong(_song);
			if(status.getdbQueryExecResult() == DbQueryExecResult.QUERY_OK) {
				restTemplate.exchange("http://localhost:3002/addSongToDB/" + _song.getId(), HttpMethod.PUT, null, String.class);
			} 
		}
		else {
			status = new DbQueryStatus("missing paramater", DbQueryExecResult.QUERY_ERROR_NOT_FOUND);
		}
		
		response.put("message", status.getMessage());
		response = Utils.setResponseStatus(response, status.getdbQueryExecResult(), status.getData());
		
		return response;
	}

	
	@RequestMapping(value = "/updateSongFavouritesCount/{songId}", method = RequestMethod.PUT)
	public @ResponseBody Map<String, Object> updateFavouritesCount(@PathVariable("songId") String songId,
			@RequestParam("shouldDecrement") String shouldDecrement, HttpServletRequest request) {

		Map<String, Object> response = new HashMap<String, Object>();
		response.put("data", String.format("PUT %s", Utils.getUrl(request)));

		DbQueryStatus status;
		
		if(shouldDecrement.equals("true") || shouldDecrement.equals("false")) {
			status = songDal.updateSongFavouritesCount(songId, Boolean.parseBoolean(shouldDecrement));
		}
		else {
			status = new DbQueryStatus("missing paramater or contains a parameter with unperimitted value", DbQueryExecResult.QUERY_ERROR_NOT_FOUND);
		}
		
		response.put("message", status.getMessage());
		response = Utils.setResponseStatus(response, status.getdbQueryExecResult(), status.getData());
		
		return response;
	}
}