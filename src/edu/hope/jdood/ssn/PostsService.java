package edu.hope.jdood.ssn;

import java.net.URI;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.json.JSONArray;
import org.json.JSONObject;

@Path("/posts")
public class PostsService {

	@GET
	@Produces({ "application/json", "text/json" })
	public Response listAllPosts() {

		JSONArray result = new JSONArray();
		for (Post post : PostManager.connect()) {
			result.put(post.toJSON());
		}
		return Response.ok().entity(result.toString()).build();
	}

	@GET
	@Produces({ "application/json", "text/json" })
	@Path("{n}")
	public Response getSinglePost(@PathParam("n") int n) {
		PostManager pm = PostManager.connect();
		if (n >= 0 && n < pm.size()) {
			return Response.ok(pm.get(n).toJSON().toString()).build();
		}
		return Response.status(Response.Status.NOT_FOUND).build();
	}

	@POST
	@Consumes({ "application/json", "text/json" })
	public Response createPost(@Context UriInfo uriInfo, String postText) {
		JSONObject postJSON = new JSONObject(postText);
		String author = postJSON.getString("author");
		String content = postJSON.getString("content");
		Post post = new Post(author, content);
		PostManager pm = PostManager.connect();
		pm.add(post);

		URI path = URI.create(String.format("%s/%d", uriInfo.getAbsolutePath(),
				pm.size() - 1));
		return Response.created(path).build();
	}
}
