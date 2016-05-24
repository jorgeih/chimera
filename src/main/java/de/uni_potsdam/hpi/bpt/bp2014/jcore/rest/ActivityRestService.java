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
import java.lang.annotation.ElementType;
import java.util.*;
import java.util.stream.Collectors;

/**
 *
 */
@Path("interface/v2")
public class ActivityRestService extends AbstractRestService {
    private static Logger log = Logger.getLogger(RestInterface.class);

    private static final String READY = "ready";
    private static final String READY_DATA = "ready(Data)";
    private static final String READY_CF = "ready(ControlFlow)";
    private static final String RUNNING = "running";
    private static final String TERMINATED = "terminated";

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
    @Path("scenario/{scenarioId}/instance/{instanceId}/activity")
    @Produces(MediaType.APPLICATION_JSON) public Response getActivitiesOfInstance(
            @Context UriInfo uriInfo,
            @PathParam("scenarioId") int scenarioID,
            @PathParam("instanceId") int instanceID,
            @QueryParam("filter") String filterString,
            @QueryParam("state") String state) {
        if ((filterString == null || filterString.isEmpty()) && (state == null || state
                .isEmpty())) {
            return getAllActivitiesOfInstance(scenarioID, instanceID, uriInfo);
        } else if (filterString == null || filterString.isEmpty())
            return getAllActivitiesOfInstanceWithState(
                    scenarioID, instanceID, state, uriInfo);
        else if (state == null || state.isEmpty()) {
            return getAllActivitiesOfInstanceWithFilter(
                    scenarioID, instanceID, filterString,
                    uriInfo);
        } else {
            return getAllActivitiesWithFilterAndState(
                    scenarioID, instanceID, filterString, state, uriInfo);
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
        Collection<ActivityInstance> instances = getActivitiesOfState(state, scenarioID, instanceID);
        if (!isLegalState(state)) {
            this.buildNotFoundResponse("{\"error\":\"The state is not allowed "
                    + state + "\"}");
        }
        Collection<ActivityInstance> selection = instances.stream()
                .filter(instance -> instance.getLabel().contains(filterString))
                .collect(Collectors.toList());
        JSONObject result = buildJSONObjectForActivities(selection, state, uriInfo);
        return Response.ok(result.toString(), MediaType.APPLICATION_JSON).build();
    }

    private boolean isLegalState(String state) {
        List<String> allowedStates = Arrays.asList(
                READY, READY_DATA, READY_CF, TERMINATED, RUNNING);
        return allowedStates.contains(state);
    }

    private Collection<ActivityInstance> getActivitiesOfState(
            String state, int scenarioId, int scenarioInstanceId) {
        ExecutionService executionService = ExecutionService.getInstance(scenarioId);
        executionService.openExistingScenarioInstance(scenarioId, scenarioInstanceId);
        if (READY.equals(state)) {
            return executionService.getEnabledActivities(scenarioInstanceId);
        } else if(TERMINATED.equals(state)) {
            return executionService.getTerminatedActivities(scenarioInstanceId);
        } else if (RUNNING.equals(state)) {
            return executionService.getRunningActivities(scenarioInstanceId);
        } else if (READY_DATA.equals(state)) {
            return executionService.getDataEnabledActivities(scenarioInstanceId);
        } else if (READY_CF.equals(state)) {
            return executionService.getControlFlowEnabledActivities(scenarioInstanceId);
        }
        throw new IllegalArgumentException("State has to be one of ready, terminated or running");
    }


    /**
     * Returns a Response Object.
     * The Response Object will be a 200 with JSON content.
     * The Content will be a JSON Object, containing information about activities.
     * The Label of the activities mus correspond to the filter String and be
     * part of the scenario instance specified by the instanceId.
     *
     * @param instanceId   The id of the scenario instance.
     * @param filterString The string which will be the filter condition for the activity ids.
     * @return The created Response object with a 200 and a JSON.
     */
    private Response getAllActivitiesOfInstanceWithFilter(int scenarioId, int instanceId,
                                                          String filterString, UriInfo uriInfo) {
        ExecutionService executionService = ExecutionService.getInstance(scenarioId);
        executionService.openExistingScenarioInstance(scenarioId, instanceId);
        Map<String, Collection<ActivityInstance>> stateToActivities = new HashMap<>();
        stateToActivities.put(READY, executionService.getEnabledActivities(instanceId));
        stateToActivities.put(RUNNING, executionService.getRunningActivities(instanceId));
        stateToActivities.put(TERMINATED, executionService.getTerminatedActivities(instanceId));
        stateToActivities.put(READY_DATA, executionService.getDataEnabledActivities(instanceId));
        stateToActivities.put(READY_CF, executionService.getControlFlowEnabledActivities(instanceId));

        JSONArray ids = new JSONArray();
        JSONObject activities = new JSONObject();
        for (Map.Entry<String, Collection<ActivityInstance>> entry : stateToActivities.entrySet()) {
            String state = entry.getKey();
            for (ActivityInstance instance : entry.getValue()) {
                if (instance.getLabel().contains(filterString)) {
                    ids.put(instance.getControlNodeInstanceId());
                    JSONObject activityJson = buildActivityJson(state, instance, uriInfo);
                    activities.put(String.valueOf(
                            instance.getControlNodeInstanceId()), activityJson);
                }
            }
        }
        JSONObject result = new JSONObject();
        result.put("ids", ids);
        result.put("activities", activities);
        return Response.ok(result.toString(), MediaType.APPLICATION_JSON).build();
    }

    private JSONObject buildActivityJson(String state, ActivityInstance instance, UriInfo uriInfo) {
        JSONObject activityJSON = new JSONObject();
        activityJSON.put("id",
                instance.getControlNodeInstanceId());
        activityJSON.put("label", instance.getLabel());
        activityJSON.put("state", state);
        activityJSON.put("link", uriInfo.getAbsolutePath() + "/"
                + instance.getControlNodeInstanceId());
        return activityJSON;
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
            case READY:
                instances = executionService.getEnabledActivities(instanceID);
                break;
            case TERMINATED:
                instances = executionService.getTerminatedActivities(instanceID);
                break;
            case RUNNING:
                instances = executionService.getRunningActivities(instanceID);
                break;
            case READY_CF:
                instances = executionService.getControlFlowEnabledActivities(instanceID);
                break;
            case READY_DATA:
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
        instances.put(READY, executionService.getEnabledActivities(instanceID));
        instances.put(RUNNING, executionService.getRunningActivities(instanceID));
        instances.put(TERMINATED, executionService.getTerminatedActivities(instanceID));
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
     * @param scenarioId         The databaseID of a scenario.
     * @param scenarioInstanceId The databaseID of a scenarioInstance.
     * @param activityID         The databaseID of an activityInstance.
     * @return a response Object with the status code:
     * 200 if everything was correct and holds the information about the activityInstance.
     * A 404 Not Found is returned if the scenario/scenarioInstance/activityInstanceID is wrong.
     */
    @GET
    @Path("scenario/{scenarioId}/instance/{instanceId}/activity/{activityId}")
    public Response getActivity(
            @Context UriInfo uriInfo,
            @PathParam("scenarioId") int scenarioId,
            @PathParam("instanceId") int scenarioInstanceId,
            @PathParam("activityId") int activityID) {

        ExecutionService executionService = ExecutionService.getInstance(scenarioId);
        if (!executionService.testActivityInstanceExists(activityID)) {
            return Response.status(Response.Status.NOT_FOUND)
                    .type(MediaType.APPLICATION_JSON)
                    .entity("{\"error\":\"There is no such "
                            + "activity instance.\"}")
                    .build();
        }
        ActivityJaxBean activity = new ActivityJaxBean();
        activity.setId(activityID);
        ExecutionService.getInstance(scenarioId).openExistingScenarioInstance(
                scenarioId, scenarioInstanceId);
        List<AbstractControlNodeInstance> controlNodeInstances =
                executionService.getScenarioInstance(
                        scenarioInstanceId).getControlNodeInstances();
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
    @Path("scenario/{scenarioId}/instance/{instanceId}/activity/{activityId}")
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

        if (executionService.setDataAttributeValues(scenarioInstanceID, activityID, values)) {
            return this.buildAcceptedResponse("{\"message\":\"attribute value was "
                    + "changed successfully.\"}");
        } else {
            return this.buildBadRequestResponse("{\"error\":\"error within the "
                    + "update of attributes\"}");
        }
    }

    /**
     * Updates the state of an activity instance.
     * The state will be changed to the specified one.
     * The activity Instance is specified by:
     *
     * @param scenarioId         The id of a scenario model.
     * @param scenarioInstanceId the id of an scenario instance.
     * @param activityId         the control node id of the activity.
     * @param postBody           Json Object containing the target state and the
     *                           data object id's if needed
     * @return Returns a Response, the response code implies the
     * outcome of the PUT-Request.
     * A 202 (ACCEPTED) means that the POST was successful.
     * A 400 (BAD_REQUEST) if the transition was not allowed.
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("scenario/{scenarioId}/instance/{instanceId}/activity/{activityId}/begin")
    public Response updateActivityState(
            @PathParam("scenarioId") int scenarioId,
            @PathParam("instanceId") int scenarioInstanceId,
            @PathParam("activityId") int activityId,
            JSONObject postBody) {
        boolean succesful;
        if (!postBody.has("state")) {
            return this.buildBadRequestResponse("{\"error\":\"The state is not set\"}");
        }
        ExecutionService executionService = ExecutionService.getInstance(scenarioId);
        executionService.openExistingScenarioInstance(scenarioId, scenarioInstanceId);
        List<Integer> usedDataObjects = new ArrayList<>();
        if (!postBody.has("dataobjects")) {
            JSONArray dataobjectsJson = postBody.getJSONArray("dataobjects");
            for (int i = 0; i < dataobjectsJson.length(); i++) {
                usedDataObjects.add(dataobjectsJson.getInt(i));
            }
        }
        succesful = executionService.beginActivityInstance(
                scenarioInstanceId, activityId, usedDataObjects);
        if (succesful) {
            return Response.status(Response.Status.ACCEPTED)
                    .type(MediaType.APPLICATION_JSON)
                    .entity("{\"message\":\"activity state changed.\"}")
                    .build();
        } else {
                return this.buildBadRequestResponse("{\"error\":\"impossible to "
                        + "start activity with id " + activityId + "\"}");
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("scenario/{scenarioId}/instance/{instanceId}/activity/{activityId}/terminate")
    public Response terminateActivity(@PathParam("scenarioId") int scenarioId,
                                  @PathParam("instanceId") int scenarioInstanceId,
                                  @PathParam("activityId") int activityId,
                                  JSONObject postBody) {
        ExecutionService executionService = ExecutionService.getInstance(scenarioId);
        executionService.openExistingScenarioInstance(scenarioId, scenarioInstanceId);
        boolean succesful;
        if (postBody.length() == 0) {
//            executionService.terminateActivityInstance(scenarioInstanceId,
//                    activityId, outputset);
//            scenarioInstanceId, activityId);
        }
        return this.buildBadRequestResponse("{\"error\":\"impossible to "
                + "terminate activity with id " + activityId + "\"}");

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
}
