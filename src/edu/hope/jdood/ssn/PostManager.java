package edu.hope.jdood.ssn;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class PostManager implements Iterable<Post> {
	private Connection con;

	private PostManager(String url) throws SQLException {
		con = DriverManager.getConnection(url);
		try {
			con.createStatement().execute(
					"CREATE TABLE post (id int, author varchar(50), content varchar(255))");
		} catch (SQLException e) {
			if (!e.getSQLState().equals("X0Y32")) {
				throw e;
			}
		}
	}

	/**
	 * gets the number of posts in the database
	 * 
	 * @return the number of posts
	 */
	public synchronized int size() {
		int n = 0;
		try {
			ResultSet rs = con.createStatement()
					.executeQuery("SELECT COUNT(*) as count FROM post");
			// if there are not any results, throw an exception
			if (!rs.next())
				throw new RuntimeException("Couldn't get count");
			// store the count in a variable
			n = rs.getInt("count");

		} catch (SQLException e) {
			throw new RuntimeException(
					"Could't retrieve count from table count", e);
		}
		return n;
	}

	/**
	 * Gets a numbered post
	 * 
	 * @param index offset into the list of posts
	 * @return Post at the given index
	 * @throws IndexOutOfBoundsException if the index doesn't match any posts
	 * @throws AssertionError if two posts have the same index
	 * @throws RuntimeException if there is some unknown SQL failure
	 */
	public synchronized Post get(int index) {
		Post post = null;
		try {
			PreparedStatement ps = con.prepareStatement("SELECT author,content FROM post WHERE id=?");
			ps.setInt(1, index);
			ResultSet rs = ps.executeQuery();
			if(!rs.next())
				throw new IndexOutOfBoundsException(index + " is not a valid post index");
			String author = rs.getString("author");
			String content = rs.getString("content");
			post = new Post(author,content);
			if(rs.next())
				throw new AssertionError("there should only be one entry with id " + index);
		}
		catch(SQLException e) {
			throw new RuntimeException("Unknown failure while getting post # " + index, e);
		}
		return post;
	}

	/**
	 * Add a new post
	 * 
	 * @param post to be added
	 * @throws RuntimeException if there was a failure adding the post
	 */
	public synchronized void add(Post post) {
		try {
			//get last id, this will be the id for the new post
			int n = size();
			
			//create the statement to add the post
			PreparedStatement ps = con.prepareStatement("INSERT INTO post (id, author, content) VALUES (?, ?, ?) ");
			ps.setInt(1, n);
			ps.setString(2, post.author);
			ps.setString(3, post.content);
			
			// execute the statement, if the number of posted added was not exactly 1, throw an exception
			if(ps.executeUpdate()!=1) {
				throw new RuntimeException("Post was not added " + post.content + " by " + post.author);
			}
		}
		catch(SQLException e) {
			throw new RuntimeException("Could not add post " + post.content + " by " + post.author, e);
		}
	}

	@Override
	public synchronized Iterator<Post> iterator() {
		List<Post> posts = new ArrayList<>();
		try {
			ResultSet rs = con.createStatement().executeQuery("SELECT author,content FROM post");
			while(rs.next()) {
				String author = rs.getString("author");
				String content = rs.getString("content");
				posts.add(new Post(author, content));
			}
		} catch (SQLException e) {
			throw new RuntimeException("Could not get list of posts", e);
		}
		return posts.iterator();
	}
	// Change this at a per-computer level
	public static final String DEFAULT_URL = "jdbc:derby:c:/Users/John6/workspace/SimpleSocialNetwork/ssnDB;create=true";
	public static final String DRIVER_CLASS = "org.apache.derby.jdbc.EmbeddedDriver";
	static {
		try {
			Class.forName(DRIVER_CLASS);
		}
		catch(ClassNotFoundException e) {
			throw new RuntimeException("Could not load derby embedded driver", e);
		}
	}
	
	
	private static final Map<String, PostManager> postManagers = new HashMap<>();
	
	/**
	 * returns a post manager connected to the default database
	 * 
	 * @return default PostManager 
	 */
	public static PostManager connect() {
		return connect(DEFAULT_URL);
	}

	/**
	 * Connects to the database with the given URL
	 * 
	 * @param url database URL to connect to
	 * @return PostManager interfacing with that database
	 */
	public static PostManager connect(String url) {
		try {
			synchronized (postManagers) {
				PostManager pm = postManagers.get(url);
				if (pm == null) {
					pm = new PostManager(url);
					postManagers.put(url, pm);
				}
				return pm;
			}
		} catch (SQLException e) {
			throw new RuntimeException("could not connect to database " + url, e);
		}
	}

}
