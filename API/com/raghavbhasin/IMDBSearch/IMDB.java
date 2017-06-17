package com.raghavbhasin.IMDBSearch;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;


public class IMDB {
	private URL host, host_search;
	private URLConnection connection, connection_search;
	private BufferedReader reader = null,reader_search;
	private String address = null;
	

	//HTML TAGS
	private final String IMDB_SEARCH = "http://www.imdb.com/find?ref_=nv_sr_fn&q=";
	private final String SEARCH_PARAM = "name=\"tt\"";
	private final String SEARCH_TAG = "/title/";
	private final String PREFIX_URL = "http://www.imdb.com";
	private final String LANG = "<";
	private final String RANG = ">";
	private final String NAME_TAG = "itemprop=\"name\"";
	private final String GENRE_TAG = "itemprop=\"genre\"";
	private final String DIRECTOR_TAG = "itemprop=\"director\"";
	private final String TITLE_TAG = "property=\'og:title\' content=\"";
	private final String TYPE_TAG = "content=\"video.movie\"";
	private final String DATE_TAG = "itemprop=\"datePublished\" content=\"";
	private final String DURATION_TAG = "itemprop=\"duration\"";
	private final String KEYWORD_TAG = "itemprop=\"keywords\"";
	private final String DESCRIPTION_TAG = "class=\"summary_text\"";
	private final String QUOTE = "\"";
	private final String CONTENT = "content=\"";
	private final String TIME_TAG = "\"PT";
	private final String RATING_TAG = "itemprop=\"ratingValue\"";
	
	private boolean do_actor = false;
	private boolean should_extract = false;
	private int number_of_actors = 0;
	private Movie movie = new Movie();
	
	
	public IMDB(String name, boolean is_first_param_movie_name) 
	{	
		String url = name;
		if(is_first_param_movie_name)
		{
		String target = create_search_target(name);
		url = IMDB_SEARCH + target + "&s=all";
		} else
		{
			
			movie.setImdb_url(name);
		}
		try {
			host_search = new URL(url);
			connection_search = host_search.openConnection();
			reader_search = new BufferedReader(new InputStreamReader
			(connection_search.getInputStream()));
		} catch (IOException e) {
			
		}
		try {
		   scrape_web();
		} catch (Exception e) {
		
		}
	}
	
	
	private  String scrape(String line)
	{
		int start = line.indexOf(SEARCH_TAG);
		String intm = line.substring(start);
		int end = line.indexOf(RANG) - 1;
		return intm.substring(0, end);
	}
	
	
	private String scrape_web() throws Exception
	{
		String line = null;
		String data = null;
		
		
		try {
			while ((line = reader_search.readLine()) != null) {
				
				if(line.contains(SEARCH_PARAM))
					 should_extract = true;
				
				if(should_extract && line.contains(SEARCH_TAG)) {
					String link = PREFIX_URL + scrape(line);
					data = link;
					break;
				}
				
			}
		} 
		
		catch (IOException e) {
			
		}
		if(data != null) {
			address = data;
			reinit(data);
		}
		return address;
	}
	
	private void reinit(String url) {
		movie.setImdb_url(url);
		try {
			host = new URL(url);
			connection = host.openConnection();
			reader = new BufferedReader(new InputStreamReader
			(connection.getInputStream()));
		} catch (IOException e) {
			
		}
	}


	private String create_search_target(String name)
	{
		String[] list = name.split(" ");
		String res = "";
		for(String part:list)
		{
			res += "+" + part.toLowerCase();
		}
		return res.substring(1);
	}

	private String get_time(String line)
	{
		int start = line.indexOf(TIME_TAG) + TIME_TAG.length();
		String im = line.substring(start);
		int end = im.indexOf(QUOTE) - 1;
		return im.substring(0,end);
	}
	
	private String get_released(String line)
	{
		int start = line.indexOf(CONTENT);
		String intermediate_string = line.substring(start);
		start = intermediate_string.indexOf(QUOTE) + 1;
		intermediate_string = intermediate_string.substring(start);
		int end = intermediate_string.indexOf(QUOTE);
		return intermediate_string.substring(0, end);
	}
	
	private String get_title(String line)
	{
		int start = line.indexOf(TITLE_TAG);
		String im = line.substring(start);
		start = im.indexOf(QUOTE) + 1;
		im = im.substring(start);
		int end = im.indexOf(QUOTE);
		return im.substring(0, end);
	}
	

	public boolean isMovie()
	{ 
		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				
				if(line.contains(TYPE_TAG)) {
					reinit(address);
					return true;
				}
			}
		} 
		
		catch (IOException e) {
			
		}
		reinit(address);
		return false;
	}
	private String extract_from_tag(String line, String tag)
	{
		int start = line.indexOf(tag);
		String im = line.substring(start);
		start = im.indexOf(RANG) + 1;
		int end = im.indexOf(LANG);
		return im.substring(start, end);
	}
	
	
	
	public Movie getMovie()
	{
		String line = null;
		
		try {
			while ((line = reader.readLine()) != null) {
				if(line.contains(DESCRIPTION_TAG))
				{ String des = reader.readLine();
				  movie.setDescription(des);
				}
				else if (line.contains(KEYWORD_TAG))
				{	
					try{
					String key = extract_from_tag(line,KEYWORD_TAG);
					movie.setKeyword(key);
				  }catch(Exception e) {}
				}
				else if (line.contains(DURATION_TAG))
				{ 
					try{
						String time = get_time(line);
						movie.setRuntime(time);
					}catch(Exception e) {}
				}
				else if(line.contains(DATE_TAG))
				{
					try{
						String rel = get_released(line);
						movie.setReleased(rel);
					}catch(Exception e) {}
				}
				else if(line.contains(TITLE_TAG))
				{
					try{
						String title = get_title(line);
						movie.setTitle(title);
					}catch(Exception e) {}
				}
				else if(line.contains(DIRECTOR_TAG))
				{
					reader.readLine();
					line = reader.readLine();
					if(line.contains(NAME_TAG))
					{
						try{
							String dir = extract_from_tag(line,NAME_TAG);
							movie.setDirector(dir);
							do_actor = true;
						}catch (Exception e) {}
					}
				}
				else if (do_actor) 
				{
				 if (line.contains(NAME_TAG) )
				{
					 try{
							String actor = extract_from_tag(line,NAME_TAG);
							movie.setActors(actor);
							number_of_actors++;
							if(number_of_actors > 8) do_actor = false;
							
						}catch (Exception e) {}
					}
				}
				else if(line.contains(GENRE_TAG))
				{
					try{
						String genre = extract_from_tag(line,GENRE_TAG);
						movie.setGenre(genre);
					}catch (Exception e) {}
				}
				else if(line.contains(RATING_TAG))
				{	
					try{
						String ratings = extract_from_tag(line,RATING_TAG);
						movie.setRating(ratings);
					}catch (Exception e) {}
				}
			}
		} 
		
		catch (IOException e) {
			
		}
		reinit(movie.getImdb_url());
		return movie;
	}
	
	public String getURL()
	{
		return address;
		
	}
	
	public Movie getExtractedMovie()
	{
		return movie;
	}
	
	public boolean nextSearch()
	{  
		String org = address;
		if(org == null)
			return false;
	
		try {
			scrape_web(); 
		} catch (Exception e) {
		}
		String next = address;

		if(org.equals(next) || next == null)
			return false;
		
	   return true;
	}
}
