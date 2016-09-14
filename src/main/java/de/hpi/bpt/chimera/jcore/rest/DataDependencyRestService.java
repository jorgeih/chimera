package de.hpi.bpt.chimera.jcore.rest;

import de.hpi.bpt.chimera.database.controlnodes.DbControlNodeInstance;
import de.hpi.bpt.chimera.jcore.ExecutionService;
import de.hpi.bpt.chimera.jcore.ScenarioInstance;
import de.hpi.bpt.chimera.database.data.DbDataConditions;
import de.hpi.bpt.chimera.jcore.data.DataObject;
import de.hpi.bpt.chimera.jcore.rest.TransportationBeans.DataObjectJaxBean;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This class implements the REST interface for data based elements.
 * It allows to generate and retrieve information about inputs/outputs.
 * Note that direct interaction with data objects is handled by {@link DataObjectRestService}
 */
@Path("interface/v2")
public class DataDependencyRestService extends AbstractRestService {

    /**
     * This method responds to a GET request by returning an array of inputSets.
     * Each contains the inputSetDatabaseID, the name of the dataObject and their state
     * as a Map & a link to get the dataObjectInstances with their dataAttributesInstances.
     * The result is determined by:
     *
     * @param uriInfo            A UriInfo object that holds the server context
     *                              used for the link.
     * @param scenarioId         The databaseID of the scenario.
     * @param instanceId The databaseID of the scenarioInstance belonging to the
     *                              aforementioned scenario.
     * @param activityInstanceId         The databaseID of the activityInstance belonging to this
     *                              scenarioInstance.
     * @return a response consisting of:
     * array of inputSets containing the inputSetDatabaseID, the name of the dataObject
     * and their state as a Map & a link to get the dataObjectInstances with their
     * dataAttributesInstances.
     * a response status code:
     * <p/>
     * A 200 if everything was correct.
     * A 404 Not Found is returned if the scenario/scenarioInstance/activityInstance
     * is non-existing or if the activity has no inputSet & with an error message
     * instead of the array.
     */
    @GET
    @Path("scenario/{scenarioId}/instance/{instanceId}/activityinstance/{activityInstanceId}/input")
    public Response getInputDataObjects(
            @Context UriInfo uriInfo,
            @PathParam("scenarioId") int scenarioId,
            @PathParam("instanceId") int instanceId,
            @PathParam("activityInstanceId") int activityInstanceId) {

        int activityId = new DbControlNodeInstance().getControlNodeId(activityInstanceId);
        Map<String, Set<String>> inputSets = new DbDataConditions().loadInputSets(activityId);
        if (inputSets.size() == 0) {
            String errorMsg = "{\"error\":\"There is no input set for activity instance %d\"}";
            errorMsg = String.format(errorMsg, activityInstanceId);
            return Response.status(Response.Status.NOT_FOUND).type(MediaType.APPLICATION_JSON)
                    .entity(errorMsg).build();
        }
        return Response.ok(buildIOJson(inputSets), MediaType.APPLICATION_JSON).build();
    }


    /**
     * This method responds to a GET request by returning an array of outputSets.
     * Each contains the outputSetDatabaseID, the name of the dataObject and their
     * state as a Map & a link to get the dataObjectInstances with their
     * dataAttributesInstances.
     * The result is determined by:
     *
     * @param uriInfo            A UriInfo object, which holds the server context used
     *                              for the link.
     * @param scenarioID         The databaseID of the scenario.
     * @param scenarioInstanceID The databaseID of the scenarioInstance belonging to the
     *                              aforementioned scenario.
     * @param activityInstanceId         The databaseID of the activityInstance belonging to this
     *                              scenarioInstance.
     * @return a response consisting of:
     * array of outputSets containing the outputSetDatabaseID, the name of the dataObject
     * and their state as a Map & a link to get the dataObjectInstances
     * with their dataAttributesInstances.
     * a response status code:
     * <p/>
     * A 200 if everything was correct.
     * A 404 Not Found is returned if the scenario/scenarioInstance/activityInstance
     * is non-existing or if the activity has no outputSet & with an error message
     * instead of the array.
     */
    @GET
    @Path("scenario/{scenarioId}/instance/{instanceId}/activityinstance/{activityInstanceId}/output")
    public Response getOutputDataObjects(
            @Context UriInfo uriInfo,
            @PathParam("scenarioId") int scenarioID,
            @PathParam("instanceId") int scenarioInstanceID,
            @PathParam("activityInstanceId") int activityInstanceId) {

        int activityId = new DbControlNodeInstance().getControlNodeId(activityInstanceId);
        Map<String, Set<String>> outputSets = new DbDataConditions()
                .loadOutputSets(activityId);
        if (outputSets.size() == 0) {
            String errorMsg = "{\"error\":\"There is no output set for activity instance %d\"}";
            errorMsg = String.format(errorMsg, activityInstanceId);
            return Response.status(Response.Status.NOT_FOUND).type(MediaType.APPLICATION_JSON)
                    .entity(errorMsg).build();
        }
        return Response.ok(buildIOJson(outputSets), MediaType.APPLICATION_JSON).build();
    }

    /**
     *
     * @param scenarioId         The databaseID of the scenario.
     * @param scenarioInstanceId The databaseID of the scenarioInstance belonging
     *                           to the aforementioned scenario.
     * @param activityId         The databaseID of the activity in this scenarioInstance.
     * @return an array with the available inputs
     */
    @GET
    @Path("scenario/{scenarioId}/instance/{instanceId}/activity/{activityId}/availableInput")
    public Response getAvailableInput(@PathParam("scenarioId") int scenarioId,
                                      @PathParam("instanceId") int scenarioInstanceId,
                                      @PathParam("activityId") int activityId) {
        ExecutionService executionService = ExecutionService.getInstance(scenarioId);
        executionService.openExistingScenarioInstance(scenarioId, scenarioInstanceId);
        ScenarioInstance scenarioInstance = executionService.getScenarioInstance(
                scenarioInstanceId);
        List<DataObject> possibleInputs = scenarioInstance.getDataManager()
                .getAvailableInput(activityId);
        List<DataObjectJaxBean> outputBeans = possibleInputs.stream()
                .map(x -> buildDataObjectJaxBean(x, executionService))
                .collect(Collectors.toList());
        JSONArray array = new JSONArray(outputBeans);
        return Response.status(Response.Status.ACCEPTED).type(MediaType.APPLICATION_JSON)
                .entity(array.toString()).build();
    }

    private DataObjectJaxBean buildDataObjectJaxBean(
            DataObject dataObjectInstance, ExecutionService executionService) {
        DataObjectJaxBean dataObject = new DataObjectJaxBean();
        dataObject.setId(dataObjectInstance.getId());
        dataObject.setLabel(dataObjectInstance.getName());
        dataObject.setState(dataObjectInstance.getStateName());
        dataObject.setAttributeConfiguration(executionService
                .getDataAttributesForDataObjectInstance(dataObjectInstance));
        return dataObject;
    }

    private String buildIOJson(Map<String, Set<String>> ioSets) {
        JSONObject object = new JSONObject();
        for (Map.Entry<String, Set<String>> entry : ioSets.entrySet()) {
            object.put(entry.getKey(), new JSONArray(entry.getValue()));
        }
        return object.toString();
    }
}