package edu.hope.jdood.ssn;

import org.json.JSONObject;

public class Post {
	public final String author;
	public final String content;

	public Post(String author, String content) {
		this.author = author;
		this.content = content;
	}

	public JSONObject toJSON() {
		JSONObject json = new JSONObject();
		json.put("author", author);
		json.put("content", content);
		return json;
	}
}
