package de.uni_potsdam.hpi.bpt.bp2014.jcore.rest;

import de.uni_potsdam.hpi.bpt.bp2014.jcore.ExecutionService;
import de.uni_potsdam.hpi.bpt.bp2014.jcore.controlnodes.AbstractControlNodeInstance;
import de.uni_potsdam.hpi.bpt.bp2014.jcore.controlnodes.ActivityInstance;
import de.uni_potsdam.hpi.bpt.bp2014.jcore.rest.TransportationBeans.ActivityJaxBean;
import de.uni_potsdam.hpi.bpt.bp2014.jcore.rest.TransportationBeans.DataAttributeUpdateJaxBean;
import de.uni_potsdam.hpi.bpt.bp2014.jcore.rest.TransportationBeans.DataObjectSetsJaxBean;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.annotation.XmlRootElement;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

/**
 *
 */
@Path("interface/v2/scenario/{scenarioId}/instance/{instanceId}/activity")
public class ActivityRestService {
    private static Logger log = Logger.getLogger(RestInterface.class);

    /**
     * Returns a JSON-Object containing information about all activity
     * instances of a specified scenario instance.
     * The JSON-Object will group the activities regarding their state.
     * If the scenario instance does not exist, the response code will
     * specify the error which occurred.
     *
     * @param uriInfo      The context object. It provides information
     *                     the server context.
     * @param scenarioID   The id of the scenario
     * @param instanceID   The id of the instance.
     * @param filterString Defines a search strings. Only activities
     *                     with a label containing this String will be
     *                     shown.
     * @param state		   The state of the instance.
     * @return A Response with the status and content of the request.
     * A 200 (OK) implies that the instance was found and the
     * result contains the JSON-Object.
     * If only the scenario ID is incorrect a 301 (REDIRECT)
     * will point to the correct URL.
     * If the instance ID is incorrect a 404 (NOT_FOUND) will
     * be returned.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON) public Response getActivitiesOfInstance(
            @Context UriInfo uriInfo,
            @PathParam("scenarioId") int scenarioID,
            @PathParam("instanceId") int instanceID,
            @QueryParam("filter") String filterString,
            @QueryParam("state") String state) {
        ExecutionService executionService = ExecutionService.getInstance(scenarioID);
        if (!executionService.existScenarioInstance(instanceID)) {
            return Response.status(Response.Status.NOT_FOUND)
                    .type(MediaType.APPLICATION_JSON)
                    .entity("{\"message\":\"There is no instance "
                            + "with id " + instanceID + "\"}")
                    .build();
        } else if (!executionService.existScenario(scenarioID)) {
            try {
                return Response.seeOther(new URI("interface/v2/scenario/"
                        + executionService
                        .getScenarioIDForScenarioInstance(
                                instanceID)
                        + "/instance/" + instanceID + "/activity")).build();
            } catch (URISyntaxException e) {
                log.error("Error:", e);
            }
        }
        if ((filterString == null || filterString.isEmpty()) && (state == null || state
                .isEmpty())) {
            return getAllActivitiesOfInstance(scenarioID, instanceID, uriInfo);
        } else if ((filterString == null || filterString.isEmpty())) {
            return getAllActivitiesOfInstanceWithState(
                    scenarioID, instanceID, state, uriInfo);
        } else if ((state == null || state.isEmpty())) {
            return getAllActivitiesOfInstanceWithFilter(
                    scenarioID, instanceID, filterString,
                    uriInfo);
        } else {
            return getAllActivitiesWithFilterAndState(
                    scenarioID, instanceID, filterString, state,
                    uriInfo);
        }
    }

    /**
     * Returns a Response object.
     * The Object will be either a 200 with the activities in an JSON-Object
     * or an 400 with an error message if the state is invalid
     *
     * @param instanceID   The id of the scenario instance
     * @param filterString the filter string to be applied
     * @param state        the state of the activity
     * @return The Response object as described above.
     */
    private Response getAllActivitiesWithFilterAndState(int scenarioID, int instanceID,
                                                        String filterString, String state, UriInfo uriInfo) {
        ExecutionService executionService = ExecutionService.getInstance(scenarioID);
        executionService.openExistingScenarioInstance(scenarioID, instanceID);
        Collection<ActivityInstance> instances;
        switch (state) {
            case "ready":
                instances = executionService.getEnabledActivities(instanceID);
                break;
            case "terminated":
                instances = executionService.getTerminatedActivities(instanceID);
                break;
            case "running":
                instances = executionService.getRunningActivities(instanceID);
                break;
            default:
                return Response.status(Response.Status.NOT_FOUND)
                        .type(MediaType.APPLICATION_JSON)
                        .entity("{\"error\":\"The state is not allowed "
                                + state + "\"}")
                        .build();
        }
        Collection<ActivityInstance> selection = new LinkedList<>();
        for (ActivityInstance instance : instances) {
            if (instance.getLabel().contains(filterString)) {
                selection.add(instance);
            }
        }
        JSONObject result = buildJSONObjectForActivities(selection, state, uriInfo);
        return Response.ok(result.toString(), MediaType.APPLICATION_JSON).build();
    }

    /**
     * Returns a Response Object.
     * The Response Object will be a 200 with JSON content.
     * The Content will be a JSON Object, containing information about activities.
     * The Label of the activities mus correspond to the filter String and be
     * part of the scenario instance specified by the instanceID.
     *
     * @param instanceID   The id of the scenario instance.
     * @param filterString The string which will be the filter condition for the activity ids.
     * @return The created Response object with a 200 and a JSON.
     */
    private Response getAllActivitiesOfInstanceWithFilter(int scenarioID, int instanceID,
                                                          String filterString, UriInfo uriInfo) {
        ExecutionService executionService = ExecutionService.getInstance(scenarioID);
        executionService.openExistingScenarioInstance(scenarioID, instanceID);
        Map<String, Collection<ActivityInstance>> instances = new HashMap<>();
        instances.put("ready", executionService.getEnabledActivities(instanceID));
        instances.put("running", executionService.getRunningActivities(instanceID));
        instances.put("terminated", executionService.getTerminatedActivities(instanceID));
        JSONArray ids = new JSONArray();
        JSONObject activities = new JSONObject();
        for (Map.Entry<String, Collection<ActivityInstance>> entry : instances.entrySet()) {
            for (ActivityInstance instance : entry.getValue()) {
                if (instance.getLabel().contains(filterString)) {
                    ids.put(instance.getControlNodeInstanceId());
                    JSONObject activityJSON = new JSONObject();
                    activityJSON.put("id",
                            instance.getControlNodeInstanceId());
                    activityJSON.put("label", instance.getLabel());
                    activityJSON.put("state", entry.getKey());
                    activityJSON.put("link", uriInfo.getAbsolutePath() + "/"
                            + instance.getControlNodeInstanceId());
                    activities.put(""
                                    + instance.getControlNodeInstanceId(),
                            activityJSON);
                }
            }
        }
        JSONObject result = new JSONObject();
        result.put("ids", ids);
        result.put("activities", activities);
        return Response.ok(result.toString(), MediaType.APPLICATION_JSON).build();
    }
    /**
     * This method creates a Response object for all specified activities.
     * The activities are specified by an scenario instance and a state.
     * In addition UriInfo object is needed in order to create the links
     * to the activity instances.
     *
     * @param scenarioID The ID of the scenario (model).
     * @param instanceID The ID of the scenario instance.
     * @param state      A String identifying the state.
     * @param uriInfo    A UriInfo object, which holds the server context.
     * @return A Response object, which is either a 404 if the state is invalid,
     * or a 200 if with json content.
     */
    private Response getAllActivitiesOfInstanceWithState(int scenarioID, int instanceID,
                                                         String state, UriInfo uriInfo) {
        ExecutionService executionService = ExecutionService.getInstance(scenarioID);
        executionService.openExistingScenarioInstance(scenarioID, instanceID);
        Collection<ActivityInstance> instances;
        switch (state) {
            case "ready":
                instances = executionService.getEnabledActivities(instanceID);
                break;
            case "terminated":
                instances = executionService.getTerminatedActivities(instanceID);
                break;
            case "running":
                instances = executionService.getRunningActivities(instanceID);
                break;
            case "ready(ControlFlow)":
                instances = executionService.getControlFlowEnabledActivities(instanceID);
                break;
            case "ready(Data)":
                instances = executionService.getDataEnabledActivities(instanceID);
                break;
            default:
                return Response.status(Response.Status.NOT_FOUND)
                        .type(MediaType.APPLICATION_JSON)
                        .entity("{\"error\":\"The state "
                                + "is not allowed "	+ state + "\"}")
                        .build();
        }
        JSONObject result = buildJSONObjectForActivities(instances, state, uriInfo);
        return Response.ok(result.toString(), MediaType.APPLICATION_JSON).build();
    }

    /**
     * Returns a Response Object for all activities with the instance Id.
     * We assume that the instanceId is correct.
     * The Response will be a 200 with json content.
     * The Content will be a json object with information about each activity.
     *
     * @param instanceID the instance id of the scenario instance.
     * @return The Response Object, with 200 and JSON Content.
     */
    private Response getAllActivitiesOfInstance(
            int scenarioID, int instanceID, UriInfo uriInfo) {
        ExecutionService executionService = ExecutionService.getInstance(scenarioID);
        executionService.openExistingScenarioInstance(scenarioID, instanceID);
        Map<String, Collection<ActivityInstance>> instances = new HashMap<>();
        instances.put("ready", executionService.getEnabledActivities(instanceID));
        instances.put("running", executionService.getRunningActivities(instanceID));
        instances.put("terminated", executionService.getTerminatedActivities(instanceID));
        JSONArray ids = new JSONArray();
        JSONObject activities = new JSONObject();
        for (Map.Entry<String, Collection<ActivityInstance>> entry : instances.entrySet()) {
            for (ActivityInstance instance : entry.getValue()) {
                ids.put(instance.getControlNodeInstanceId());
                JSONObject activityJSON = new JSONObject();
                activityJSON.put("id", instance.getControlNodeInstanceId());
                activityJSON.put("label", instance.getLabel());
                activityJSON.put("state", entry.getKey());
                activityJSON.put("link", uriInfo.getAbsolutePath() + "/"
                        + instance.getControlNodeInstanceId());
                activities.put(""
                                + instance.getControlNodeInstanceId(),
                        activityJSON);
            }
        }
        JSONObject result = new JSONObject();
        result.put("ids", ids);
        result.put("activities", activities);
        return Response.ok(result.toString(), MediaType.APPLICATION_JSON).build();
    }


    /**
     * Builds a JSON Object for a Map with data
     * corresponding to a set of activities.
     *
     * @param instances The Map containing information about the activity instances.
     *                  We Assume that the key is a the id and the value is a Map
     *                  from String to Object with the properties of the instance.
     * @return The newly created JSON Object with the activity data.
     */
    private JSONObject buildJSONObjectForActivities(Collection<ActivityInstance> instances,
                                                    String state, UriInfo uriInfo) {
        List<Integer> ids = new ArrayList<>(instances.size());
        JSONArray activities = new JSONArray();
        for (ActivityInstance instance : instances) {
            JSONObject activityJSON = new JSONObject();
            ids.add(instance.getControlNodeInstanceId());
            activityJSON.put("id", instance.getControlNodeInstanceId());
            activityJSON.put("label", instance.getLabel());
            activityJSON.put("state", state);
            activityJSON.put("link", uriInfo.getAbsolutePath() + "/"
                    + instance.getControlNodeInstanceId());
            activities.put(activityJSON);
        }
        JSONObject result = new JSONObject();
        result.put("ids", new JSONArray(ids));
        result.put("activities", activities);
        return result;
    }


    /**
     * This method is used to get all the information for an activity.
     * This means the label, id and a link for the input-/outputSets.
     *
     * @param uriInfo            A UriInfo object, which holds the server context.
     * @param scenarioID         The databaseID of a scenario.
     * @param scenarioInstanceID The databaseID of a scenarioInstance.
     * @param activityID         The databaseID of an activityInstance.
     * @return a response Object with the status code:
     * 200 if everything was correct and holds the information about the activityInstance.
     * A 404 Not Found is returned if the scenario/scenarioInstance/activityInstanceID is wrong.
     */
    @GET
    @Path("{activityId}")
    public Response getActivity(
            @Context UriInfo uriInfo,
            @PathParam("scenarioId") int scenarioID,
            @PathParam("instanceId") int scenarioInstanceID,
            @PathParam("activityId") int activityID) {

        ExecutionService executionService = ExecutionService.getInstance(scenarioID);
        if (!executionService
                .openExistingScenarioInstance(scenarioID, scenarioInstanceID)) {
            return Response.status(Response.Status.NOT_FOUND)
                    .type(MediaType.APPLICATION_JSON)
                    .entity("{\"error\":\"There is no such "
                            + "scenario instance.\"}")
                    .build();
        }
        if (!executionService.testActivityInstanceExists(activityID)) {
            return Response.status(Response.Status.NOT_FOUND)
                    .type(MediaType.APPLICATION_JSON)
                    .entity("{\"error\":\"There is no such "
                            + "activity instance.\"}")
                    .build();
        }
        ActivityJaxBean activity = new ActivityJaxBean();
        activity.setId(activityID);
        LinkedList<AbstractControlNodeInstance> controlNodeInstances =
                executionService.getScenarioInstance(
                        scenarioInstanceID).getControlNodeInstances();
        for (AbstractControlNodeInstance controlNodeInstance : controlNodeInstances) {
            if (controlNodeInstance.getControlNodeInstanceId() == activityID) {
                activity.setLabel(executionService
                        .getLabelForControlNodeID(
                                controlNodeInstance.
                                        getControlNodeId()
                        ));
            }
        }
        activity.setInputSetLink(uriInfo.getAbsolutePath() + "/input");
        activity.setOutputSetLink(uriInfo.getAbsolutePath() + "/output");
        return Response.ok(activity, MediaType.APPLICATION_JSON).build();

    }

    /**
     * This method implements the REST call for retrieving.
     * reference information for a specific activity
     *
     * @param uriInfo            A UriInfo object, which holds the server context.
     * @param scenarioID         The databaseID of a scenario.
     * @param scenarioInstanceID The databaseID of a scenarioInstance.
     * @param activityID         The databaseID of an activityInstance.
     * @return a json object containing the referenced activities
     */
    @GET
    @Path("{activityId}/references")
    public Response getReferencesForActivity(
            @Context UriInfo uriInfo,
            @PathParam("scenarioId") int scenarioID,
            @PathParam("instanceId") int scenarioInstanceID,
            @PathParam("activityId") int activityID) {
        ExecutionService executionService = ExecutionService.getInstance(scenarioID);
        executionService.openExistingScenarioInstance(scenarioID, scenarioInstanceID);

        Collection<ActivityInstance> referencedActivities = executionService
                .getReferentialEnabledActivities(scenarioInstanceID, activityID);

        JSONObject result =
                buildJSONObjectForReferencedActivities(
                        referencedActivities, uriInfo);
        return Response.ok(result.toString(), MediaType.APPLICATION_JSON).build();
    }

    /**
     * @param instances The Map containing information about the activity instances.
     *                  We Assume that the key is a the id and the value is a Map
     *                  from String to Object with the properties of the instance.
     * @param uriInfo   Specifies the context. For example the uri
     *                  of the request.
     * @return			JSON Object containing activities and their references.
     */
    private JSONObject buildJSONObjectForReferencedActivities(
            Collection<ActivityInstance> instances, UriInfo uriInfo) {
        List<Integer> ids = new ArrayList<>(instances.size());
        JSONArray activities = new JSONArray();
        for (ActivityInstance instance : instances) {
            JSONObject activityJSON = new JSONObject();
            ids.add(instance.getControlNodeInstanceId());
            activityJSON.put("id", instance.getControlNodeInstanceId());
            activityJSON.put("label", instance.getLabel());
            activityJSON.put("link", uriInfo.getAbsolutePath() + "/"
                    + instance.getControlNodeInstanceId());
            activities.put(activityJSON);
        }
        JSONObject result = new JSONObject();
        result.put("ids", new JSONArray(ids));
        result.put("activities", activities);
        return result;
    }

    /**
     * This method updates the data attributes of a specific activity
     * defined via its activityID.
     *
     * @param scenarioID         The id of a scenario model.
     * @param scenarioInstanceID the id of an scenario instance.
     * @param activityID         the control node id of the activity.
     * @param input				 data input.
     * @return Status code with regard to its success / failure
     */
    @PUT
    @Path("{activityId}")
    public Response setDataAttribute(
            @PathParam("scenarioId") int scenarioID,
            @PathParam("instanceId") int scenarioInstanceID,
            @PathParam("activityId") int activityID,
            final DataAttributeUpdateJaxBean input) {
        ExecutionService executionService = ExecutionService.getInstance(scenarioID);
        executionService.openExistingScenarioInstance(scenarioID, scenarioInstanceID);

        Map<Integer, String> values = new HashMap<>();
        if (input != null) {
            values.put(input.getId(), input.getValue());
        }

        if (executionService
                .setDataAttributeValues(scenarioInstanceID, activityID, values)) {
            return Response.status(Response.Status.ACCEPTED)
                    .type(MediaType.APPLICATION_JSON)
                    .entity("{\"message\":\"attribute value was "
                            + "changed successfully.\"}")
                    .build();
        } else {
            return Response.status(Response.Status.BAD_REQUEST)
                    .type(MediaType.APPLICATION_JSON)
                    .entity("{\"error\":\"error within the "
                            + "update of attributes\"}")
                    .build();
        }
    }

    /**
     * Updates the state of an activity instance.
     * The state will be changed to the specified one.
     * The activity Instance is specified by:
     *
     * @param scenarioID         The id of a scenario model.
     * @param scenarioInstanceID the id of an scenario instance.
     * @param activityID         the control node id of the activity.
     * @param state              the new state of the activity.
     * @param outputset			 the outputset of the activity.
     * @return Returns a Response, the response code implies the
     * outcome of the PUT-Request.
     * A 202 (ACCEPTED) means that the POST was successful.
     * A 400 (BAD_REQUEST) if the transition was not allowed.
     */
    @POST
    @Path("{activityId}")
    public Response updateActivityState(
            @PathParam("scenarioId") int scenarioID,
            @PathParam("instanceId") int scenarioInstanceID,
            @PathParam("activityId") int activityID,
            @QueryParam("state") String state,
            @DefaultValue("-1") @QueryParam("outputset") int outputset) {

        boolean result;
        if (state == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .type(MediaType.APPLICATION_JSON)
                    .entity("{\"error\":\"The state is not set\"}")
                    .build();
        }
        ExecutionService executionService = ExecutionService.getInstance(scenarioID);
        executionService.openExistingScenarioInstance(scenarioID, scenarioInstanceID);
        switch (state) {
            case "begin":
                result = executionService
                        .beginActivityInstance(scenarioInstanceID, activityID);
                break;
            case "terminate":
                if (outputset != -1) {
                    result = executionService
                            .terminateActivityInstance(
                                    scenarioInstanceID,
                                    activityID,
                                    outputset);
                } else {
                    result = executionService.terminateActivityInstance(
                            scenarioInstanceID, activityID);
                }
                break;
            default:
                return Response.status(Response.Status.BAD_REQUEST)
                        .type(MediaType.APPLICATION_JSON)
                        .entity("{\"error\":\"The state transition "
                                + state + " is unknown\"}")
                        .build();
        }
        if (result) {
            return Response.status(Response.Status.ACCEPTED)
                    .type(MediaType.APPLICATION_JSON)
                    .entity("{\"message\":\"activity state changed.\"}")
                    .build();
        } else {
            executionService
                    .reloadScenarioInstanceFromDatabase(
                            scenarioID, scenarioInstanceID);
            if ("begin".equals(state)) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .type(MediaType.APPLICATION_JSON)
                        .entity("{\"error\":\"impossible to "
                                + "start"
                                + " activity with id "
                                + activityID + "\"}")
                        .build();
            } else {
                return Response.status(Response.Status.BAD_REQUEST)
                        .type(MediaType.APPLICATION_JSON)
                        .entity("{\"error\":\"impossible to "
                                + "terminate"
                                + " activity with id "
                                + activityID + "\"}")
                        .build();
            }

        }
    }

    /**
     * This method responds to a GET request by returning an array of inputSets.
     * Each contains the inputSetDatabaseID, the name of the dataObject and their state
     * as a Map & a link to get the dataObjectInstances with their dataAttributesInstances.
     * The result is determined by:
     *
     * @param uriInfo            A UriInfo object that holds the server context
     *                              used for the link.
     * @param scenarioID         The databaseID of the scenario.
     * @param scenarioInstanceID The databaseID of the scenarioInstance belonging to the
     *                              aforementioned scenario.
     * @param activityID         The databaseID of the activityInstance belonging to this
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
    @Path("{activityId}/input")
    public Response getInputDataObjects(
            @Context UriInfo uriInfo,
            @PathParam("scenarioId") int scenarioID,
            @PathParam("instanceId") int scenarioInstanceID,
            @PathParam("activityId") int activityID) {

        ExecutionService executionService = ExecutionService.getInstance(scenarioID);
        if (!executionService.
                openExistingScenarioInstance(scenarioID, scenarioInstanceID)) {
            return Response.status(Response.Status.NOT_FOUND)
                    .type(MediaType.APPLICATION_JSON)
                    .entity("{\"error\":\"There is no such "
                            + "scenario instance.\"}")
                    .build();
        }
        if (!executionService.testActivityInstanceExists(activityID)) {
            return Response.status(Response.Status.NOT_FOUND)
                    .type(MediaType.APPLICATION_JSON)
                    .entity("{\"error\":\"There is no such "
                            + "activity instance.\"}")
                    .build();
        }
        if (executionService.getInputSetsForActivityInstance(activityID) == null
                || executionService
                .getInputSetsForActivityInstance(activityID).size() == 0) {
            return Response.status(Response.Status.NOT_FOUND)
                    .type(MediaType.APPLICATION_JSON)
                    .entity("{\"error\":\"There is no inputSet for this "
                            + "activity instance.\"}")
                    .build();
        }
        Map<Integer, Map<String, String>> inputSetMap = executionService
                .getInputSetsForActivityInstance(activityID);
        int j = 0;
        DataObjectSetsJaxBean[] inputSets =
                new DataObjectSetsJaxBean[inputSetMap.keySet().size()];
        for (Integer i : inputSetMap.keySet()) {
            DataObjectSetsJaxBean inputSet = new DataObjectSetsJaxBean();
            inputSet.setId(i);
            inputSet.setDataObjects(inputSetMap.get(i));
            String[] path = uriInfo.getAbsolutePath().toString().split("/");
            inputSet.setLinkDataObject("");
            for (int k = 0; k < path.length - 3; k++) {
                inputSet.setLinkDataObject(
                        inputSet.getLinkDataObject() + path[k] + "/");
            }
            inputSet.setLinkDataObject(
                    inputSet.getLinkDataObject() + "inputset/" + inputSet.getId()
            );
            inputSets[j] = inputSet;
            j++;
        }
        return Response.ok(inputSets, MediaType.APPLICATION_JSON).build();
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
     * @param activityID         The databaseID of the activityInstance belonging to this
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
    @Path("{activityId}/output")
    public Response getOutputDataObjects(
            @Context UriInfo uriInfo,
            @PathParam("scenarioId") int scenarioID,
            @PathParam("instanceId") int scenarioInstanceID,
            @PathParam("activityId") int activityID) {

        ExecutionService executionService = ExecutionService.getInstance(scenarioID);
        if (!executionService
                .openExistingScenarioInstance(scenarioID, scenarioInstanceID)) {
            return Response.status(Response.Status.NOT_FOUND)
                    .type(MediaType.APPLICATION_JSON)
                    .entity("{\"error\":\"There is no such "
                            + "scenario instance.\"}")
                    .build();
        }
        if (!executionService.testActivityInstanceExists(activityID)) {
            return Response.status(Response.Status.NOT_FOUND)
                    .type(MediaType.APPLICATION_JSON)
                    .entity("{\"error\":\"There is no such "
                            + "activity instance.\"}")
                    .build();
        }
        if (executionService.getOutputSetsForActivityInstance(activityID) == null
                || executionService.
                getOutputSetsForActivityInstance(activityID).size() == 0) {
            return Response.status(Response.Status.NOT_FOUND)
                    .type(MediaType.APPLICATION_JSON)
                    .entity("{\"error\":\"There is no outputSet for this "
                            + "activity instance.\"}")
                    .build();
        }
        Map<Integer, Map<String, String>> outputSetMap = executionService
                .getOutputSetsForActivityInstance(activityID);
        int j = 0;
        DataObjectSetsJaxBean[] outputSets = new DataObjectSetsJaxBean[outputSetMap.keySet()
                .size()];
        for (Integer i : outputSetMap.keySet()) {
            DataObjectSetsJaxBean outputSet = new DataObjectSetsJaxBean();
            outputSet.setId(i);
            outputSet.setDataObjects(outputSetMap.get(i));
            String[] path = uriInfo.getAbsolutePath().toString().split("/");
            outputSet.setLinkDataObject("");
            for (int k = 0; k < path.length - 3; k++) {
                outputSet.setLinkDataObject(
                        outputSet.getLinkDataObject()+ path[k] + "/");
            }
            outputSet.setLinkDataObject(
                    outputSet.getLinkDataObject() + "outputset/" + outputSet.getId());
            outputSets[j] = outputSet;
            j++;
        }
        return Response.ok(outputSets, MediaType.APPLICATION_JSON).build();
    }

}