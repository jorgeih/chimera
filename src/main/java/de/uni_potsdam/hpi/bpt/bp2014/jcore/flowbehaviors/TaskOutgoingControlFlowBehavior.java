package de.uni_potsdam.hpi.bpt.bp2014.jcore.flowbehaviors;

import de.uni_potsdam.hpi.bpt.bp2014.database.DbSelectedDataObjects;
import de.uni_potsdam.hpi.bpt.bp2014.database.data.*;
import de.uni_potsdam.hpi.bpt.bp2014.jcore.data.DataManager;
import de.uni_potsdam.hpi.bpt.bp2014.jcore.controlnodes.ActivityInstance;
import de.uni_potsdam.hpi.bpt.bp2014.jcore.data.DataObject;
import de.uni_potsdam.hpi.bpt.bp2014.jcore.ScenarioInstance;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Handles the behavior of a terminating activity instance.
 */
public class TaskOutgoingControlFlowBehavior extends AbstractParallelOutgoingBehavior {
	private final ActivityInstance activityInstance;
    /**
	 * Initializes the TaskOutgoingControlFlowBehavior.
	 *
	 * @param activityId         This is the database id from the activity instance.
	 * @param scenarioInstance    This is an instance from the class ScenarioInstance.
	 * @param fragmentInstanceId This is the database id from the fragment instance.
	 * @param activityInstance 	This is an AbstractControlNodeInstance.
	 */
	public TaskOutgoingControlFlowBehavior(int activityId, ScenarioInstance scenarioInstance,
			int fragmentInstanceId, ActivityInstance activityInstance) {
		this.setControlNodeId(activityId);
		this.setScenarioInstance(scenarioInstance);
		this.setFragmentInstanceId(fragmentInstanceId);
		this.activityInstance = activityInstance;
	}

	@Override public void terminate() {
		this.terminate(new HashMap<>());
	}

	/**
	 * Terminates the activity.
	 *
	 * @param dataClassNameToStateName Map from name of the data class to new state
	 */
	public void terminate(Map<String, String> dataClassNameToStateName) {
        List<DataObject> usedDataObjects = getUsedDataobjects();
        Map<Integer, Integer> dataClassIdToStateId = translate(
                usedDataObjects, dataClassNameToStateName);

        for (Map.Entry<Integer, Integer> entry : dataClassIdToStateId.entrySet()) {
            this.changeDataObjectInstanceState(entry.getKey(), entry.getValue());
        }

        ScenarioInstance scenarioInstance = this.getScenarioInstance();
        scenarioInstance.updateDataFlow();
        scenarioInstance.checkXorGatewaysForTermination(this.getControlNodeId());
		this.enableFollowing();
		this.runAutomaticTasks();
	}

    /**
     * Converts map from data class name to state name to it's respective database
     * ids.
     * @param dataObjects
     * @param dataClassNameToStateName
     * @return
     */
    private Map<Integer, Integer> translate(List<DataObject> dataObjects,
            Map<String, String> dataClassNameToStateName) {
        DbDataObject dbDataObject = new DbDataObject();
        DbState dbState = new DbState();
        Map<Integer, Integer> dataclassIdToStateId = new HashMap<>();
        for (DataObject dataObject : dataObjects) {
            String name = dbDataObject.getName(dataObject.getId());
            String stateToSet = dataClassNameToStateName.get(name);
            Integer stateId = dbState.getStateId(dataObject.getDataClassId(), stateToSet);
            dataclassIdToStateId.put(dataObject.getDataClassId(), stateId);
        }
        return dataclassIdToStateId;
    }
    /**
     * @return List of the data objects on which the activity works
     */
    private List<DataObject> getUsedDataobjects() {
        DbSelectedDataObjects dbSelectedDataObjects = new DbSelectedDataObjects();
        ScenarioInstance instance = this.getScenarioInstance();
        Set<Integer> workItems =  new HashSet<>(dbSelectedDataObjects.getDataObjectSelection(
                instance.getScenarioInstanceId(), this.getControlNodeId()));
        List<DataObject> dataObjects = instance.getDataManager().getDataObjects();
        return dataObjects.stream().filter(x -> workItems.contains(x.getId())).collect(
                Collectors.toList());
    }

    /**
     * Since at the moment each output set can only contain the same data objects with different
     * states it is enough to look only at one output set and free all data objects in this.
     */
    public void cancel() {
        DbSelectedDataObjects dbSelectedDataObjects = new DbSelectedDataObjects();
        List<Integer> usedDataObjects = dbSelectedDataObjects.getDataObjectSelection(
                this.getScenarioInstance().getScenarioInstanceId(), this.getControlNodeId());
        usedDataObjects.forEach(this::unlockDataObject);
    }


	/**
	 * Change the state of the given data object.
	 *
	 * @param dataObjectId This is the database id from the data object.
	 * @param stateId      This is the database id from the state.
	 * @return true if the data object state could been changed. false if not
	 */
	public Boolean changeDataObjectInstanceState(int dataObjectId, int stateId) {
        DataManager dataManager = this.getScenarioInstance().getDataManager();
        return dataManager.changeDataObjectInstanceState(dataObjectId, stateId,
                activityInstance.getControlNodeInstanceId());
	}

	/**
	 * Sets the data object to not on change.
	 * @param dataObjectId This is the database id from the data object.
	 */
	public void unlockDataObject(int dataObjectId) {
        DataManager dataManager = this.getScenarioInstance().getDataManager();
        Optional<DataObject> dataObjectInstance =
                dataManager.getDataobjectForId(dataObjectId);
        assert dataObjectInstance.isPresent(): "invalid data object id";
        dataObjectInstance.get().unlock();
	}
}
