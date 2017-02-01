package edu.hope.jdood.ssn;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

@Path("/test")
public class TestService {
	
	@GET
	@Produces("text/html")
	public Response returnHelloWorld() {
		String result = "<html>\n<head>\n\t<title>Test Page</title>\n</head><body>\n\t<h1>My Test Page!</h1>\n</body>\n</html>";
		return Response.status(200).entity(result).build();
	}
	
}
