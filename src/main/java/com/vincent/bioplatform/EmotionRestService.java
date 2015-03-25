package com.vincent.bioplatform;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import com.vincent.bioplatform.ecghandler.AWSSimpleQueueServiceUtil;

@Path("/emotion")
public class EmotionRestService {

	@GET
	@Path("/{param}")
	public Response printMessage(@PathParam("param") String msg) {
		String result;
		if (msg==""){
			return Response.status(200).entity("This is a test?").build();
		}

		if ((result = AWSSimpleQueueServiceUtil.getInstance()
				.getMessagesFromQueue(msg)) != null) {
			return Response.status(200).entity(result).build();
		} else
			return Response.status(400).build();

	}

}
