package de.hpi.bpt.chimera.jcore.rest;

import de.hpi.bpt.chimera.database.DbScenarioInstance;
import de.hpi.bpt.chimera.jcore.ExecutionService;
import de.hpi.bpt.chimera.jcore.ScenarioInstance;
import de.hpi.bpt.chimera.jcore.rest.TransportationBeans.NamedJaxBean;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.List;
import java.util.Map;

/**
 * This class implements the REST interface for scenario instances.
 */
@Path("interface/v2")
public class ScenarioInstanceRestService {
	private static Logger log = Logger.getLogger(RestInterface.class);

	/**
	 * Creates a new instance of a specified scenario.
	 * This method assumes that the name of then new instance will be the same
	 * as the name of the scenario.
	 * Hence no additional information should be transmitted.
	 * The response will imply if the post was successful.
	 *
	 * @param uri        a context, which holds information about the server
	 * @param scenarioId the id of the scenario.
	 * @return The Response of the POST. The Response code will be
	 * either a 201 (CREATED) if the post was successful or 400 (BAD_REQUEST)
	 * if the scenarioID was invalid.
	 * The content of the Response will be a JSON-Object containing information
	 * about the new instance.
	 */
	@POST
	@Path("scenario/{scenarioId}/instance")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response startNewInstance(@Context UriInfo uri, @PathParam("scenarioId") int scenarioId) {
		return initializeNewInstance(uri, scenarioId, "");
	}

	/**
	 * Creates a new instance of a specified scenario.
	 * This method assumes that the new instance will be named.
	 * The name will be received as a JSON-Object inside the request
	 * Body.
	 * The JSON should have the format
	 * {@code {"name": <nameOfInstance>}}.
	 * The response will imply if the post was successful.
	 *
	 * @param uriInfo    The context of the server, used to receive the url.
	 * @param scenarioId the id of the scenario.
	 * @param name       The name, which will be used for the new instance.
	 * @return The Response of the PUT. The Response code will be
	 * either a 201 (CREATED) if the post was successful or 400 (BAD_REQUEST)
	 * if the scenarioID was invalid.
	 * The content of the Response will be a JSON-Object containing information
	 * about the new instance.
	 */
	@PUT
	@Path("scenario/{scenarioId}/instance")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response startNewNamedInstance(@Context UriInfo uriInfo, @PathParam("scenarioId") int scenarioId, NamedJaxBean name) {
		if (name == null) {
			return initializeNewInstance(uriInfo, scenarioId, "");
		} else {
			return initializeNewInstance(uriInfo, scenarioId, name.getName());
		}

	}

	private Response initializeNewInstance(UriInfo uriInfo, int scenarioId, String name) {
		ExecutionService executionService = ExecutionService.getInstance(scenarioId);
		int instanceId;
		if (name.isEmpty()) {
			instanceId = executionService.startNewScenarioInstance();
		} else {
			instanceId = executionService.startNewScenarioInstance(name);
		}
		return Response.status(Response.Status.CREATED).type(MediaType.APPLICATION_JSON).entity("{\"id\":" + instanceId + ",\"link\":\"" + uriInfo.getAbsolutePath() + "/" + instanceId + "\"}").build();
	}


	/**
	 * This method provides detailed information about a scenario instance.
	 * The information will contain the id, name, parent scenario and the
	 * number of activities in the different states.
	 * The Response is JSON-Object.
	 *
	 * @param uriInfo    Contains the context information, is used to build
	 *                   links to other resources.
	 * @param scenarioId The ID of the scenario.
	 * @param instanceId The ID of the instance.
	 * @return Will return a Response with a JSON-Object body, containing
	 * the information about the instance.
	 * If the instance ID or both are incorrect 404 (NOT_FOUND) will be
	 * returned.
	 * If the scenario ID is wrong but the instance ID is correct a 301
	 * (REDIRECT) will be returned.
	 * If both IDs are correct a 200 (OK) with the expected JSON-Content
	 * will be returned.
	 */
	@GET
	@Path("scenario/{scenarioId}/instance/{instanceId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getScenarioInstance(@Context UriInfo uriInfo, @PathParam("scenarioId") int scenarioId, @PathParam("instanceId") int instanceId) {
		DbScenarioInstance instance = new DbScenarioInstance();
		JSONObject result = new JSONObject(instance.getInstanceMap(instanceId));
		result.put("activities", uriInfo.getAbsolutePath() + "/activity");
		return Response.ok(result.toString(), MediaType.APPLICATION_JSON).build();
	}

	/**
	 * This method provides a list of registered events for the instance.
	 *
	 * @param uriInfo    Contains the context information, is used to build
	 *                   links to other resources.
	 * @param scenarioId The ID of the scenario.
	 * @param instanceId The ID of the instance.
	 * @return A list of events for the specified scenario instance.
	 */
	@GET
	@Path("scenario/{scenarioId}/instance/{instanceId}/events")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getEvents(@Context UriInfo uriInfo, @PathParam("scenarioId") int scenarioId, @PathParam("instanceId") int instanceId) {
		ScenarioInstance scenarioInstance = new ScenarioInstance(scenarioId, instanceId);
		List<String> eventKeys = scenarioInstance.getRegisteredEventKeys();
		return Response.ok(MediaType.APPLICATION_JSON).entity(eventKeys).build();
	}

	/**
	 * This get is used to determine whether a given scenario instance can terminate,
	 * meaning whether one of its termination conditions is fulfilled.
	 *
	 * @param scenarioId The Id of the scenario
	 * @param instanceId The Id of the instance
	 * @return A Response: 200 if termination conditions are fulfilled,
	 * 400 if none is fulfilled, 404 if the scenario instance is not found.
	 */
	@GET
	@Path("scenario/{scenarioId}/instance/{instanceId}/canTerminate")
	@Produces(MediaType.TEXT_PLAIN)
	public Response checkTermination(@PathParam("scenarioId") int scenarioId, @PathParam("instanceId") int instanceId) {
		ScenarioInstance instance = new ScenarioInstance(scenarioId, instanceId);
		if (instance.canTerminate()) {
			return Response.status(Response.Status.OK).type(MediaType.TEXT_PLAIN).entity("Instance can be deleted").build();
		} else {
			return Response.status(Response.Status.BAD_REQUEST).type(MediaType.TEXT_PLAIN).entity("Termination condition is not fulfilled").build();
		}
	}

	/**
	 * This method is used to terminate a scenario instance.
	 *
	 * @param scenarioId The Id of the scenario
	 * @param instanceId The Id of the instance
	 * @return A Response: 200 if termination conditions are fulfilled and it
	 * has been terminated, 400 if the termination conditions are not fulfilled,
	 * 404 if the scenario instance is not found.
	 */
	@POST
	@Path("scenario/{scenarioId}/instance/{instanceId}/terminate")
	@Produces(MediaType.TEXT_PLAIN)
	public Response terminateScenarioInstance(@PathParam("scenarioId") int scenarioId, @PathParam("instanceId") int instanceId) {
		ScenarioInstance instance = new ScenarioInstance(scenarioId, instanceId);
		if (instance.canTerminate()) {
			instance.terminate();
			return Response.status(Response.Status.OK).type(MediaType.TEXT_PLAIN).entity("Instance has been terminated").build();
		} else {
			return Response.status(Response.Status.BAD_REQUEST).type(MediaType.TEXT_PLAIN).entity("Termination condition is not fulfilled").build();
		}
	}

	/**
	 * This method provides information about all instances of one scenario.
	 * The scenario is specified by an given id.
	 * If there is no scenario with the specific id a 404 response with a meaningful
	 * error message will be returned.
	 * If the Scenario exists a JSON-Array containing JSON-Objects with
	 * important information about an instance of the scenario will be returned.
	 *
	 * @param uri          Request URI.
	 * @param scenarioId   The id of the scenario which instances should be returned.
	 * @param filterString Specifies a search. Only scenarios which
	 *                     name contain the specified string will be
	 *                     returned.
	 * @return A JSON-Object with an array of information about all instances of
	 * one specified scenario. The information contains the id and name.
	 */
	@GET
	@Path("scenario/{scenarioId}/instance")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getScenarioInstances(@Context UriInfo uri, @PathParam("scenarioId") int scenarioId, @QueryParam("filter") String filterString) {
		DbScenarioInstance instance = new DbScenarioInstance();
		JSONObject result = new JSONObject();
		Map<Integer, String> data = instance.getScenarioInstancesLike(scenarioId, filterString);
		JSONObject links = new JSONObject();
		for (int id : data.keySet()) {
			links.put(String.valueOf(id), uri.getAbsolutePath() + "/" + id);
		}
		result.put("ids", new JSONArray(data.keySet()));
		result.put("labels", new JSONObject(data));
		result.put("links", links);
		return Response.ok(result.toString(), MediaType.APPLICATION_JSON).build();
	}
}