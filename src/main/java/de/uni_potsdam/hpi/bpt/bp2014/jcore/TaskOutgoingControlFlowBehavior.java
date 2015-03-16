package de.uni_potsdam.hpi.bpt.bp2014.jcore;

import de.uni_potsdam.hpi.bpt.bp2014.database.DataObject;
import de.uni_potsdam.hpi.bpt.bp2014.database.DbDataFlow;
import de.uni_potsdam.hpi.bpt.bp2014.database.DbDataNode;

import java.util.LinkedList;

/**
 * ********************************************************************************
 * <p/>
 * _________ _______  _        _______ _________ _        _______
 * \__    _/(  ____ \( (    /|(  ____ \\__   __/( (    /|(  ____ \
 * )  (  | (    \/|  \  ( || (    \/   ) (   |  \  ( || (    \/
 * |  |  | (__    |   \ | || |         | |   |   \ | || (__
 * |  |  |  __)   | (\ \) || | ____    | |   | (\ \) ||  __)
 * |  |  | (      | | \   || | \_  )   | |   | | \   || (
 * |\_)  )  | (____/\| )  \  || (___) |___) (___| )  \  || (____/\
 * (____/   (_______/|/    )_)(_______)\_______/|/    )_)(_______/
 * <p/>
 * ******************************************************************
 * <p/>
 * Copyright © All Rights Reserved 2014 - 2015
 * <p/>
 * Please be aware of the License. You may found it in the root directory.
 * <p/>
 * **********************************************************************************
 */


/**
 * Handles the behavior of a terminating activity instance.
 */
public class TaskOutgoingControlFlowBehavior extends ParallelOutgoingBehavior {
    private final ControlNodeInstance controlNodeInstance;
    /**
     * Database Connection objects.
     */
    private final DbDataNode dbDataNode = new DbDataNode();
    private final DbDataFlow dbDataFlow = new DbDataFlow();

    /**
     * Initializes the TaskOutgoingControlFlowBehavior.
     *
     * @param activity_id         This is the database id from the activity instance.
     * @param scenarioInstance    This is an instance from the class ScenarioInstance.
     * @param fragmentInstance_id This is the database id from the fragment instance.
     * @param controlNodeInstance This is an instance from the class ControlNodeInstance.
     */
    public TaskOutgoingControlFlowBehavior(int activity_id, ScenarioInstance scenarioInstance, int fragmentInstance_id, ControlNodeInstance controlNodeInstance) {
        this.controlNode_id = activity_id;
        this.scenarioInstance = scenarioInstance;
        this.fragmentInstance_id = fragmentInstance_id;
        this.controlNodeInstance = controlNodeInstance;
    }

    @Override
    public void terminate() {
        setDataStates();
        this.checkAfterTermination();
        this.enableFollowing();
        this.runAfterTermination();
    }

    /**
     * Sets the states of the data object to the output states of the activity.
     * Sets all this data object to not on change.
     */
    private void setDataStates() {
        LinkedList<Integer> outputSets = dbDataFlow.getOutputSetsForControlNode(controlNode_id);
        //TODO: Output Set
        for (int outputSet : outputSets) {
            LinkedList<DataObject> dataObjects = dbDataNode.getDataObjectsForDataSets(outputSet);
            for (DataObject dataObject : dataObjects) {
                //resets DataObjectInstance from OnChange back to not OnChange
                scenarioInstance.setDataObjectInstanceToNotOnChange(dataObject.getId());
                scenarioInstance.changeDataObjectInstanceState(dataObject.getId(), dataObject.getStateID());
            }
        }
    }

    /**
     * Terminates all referential running activities.
     */
    public void terminateReferences() {
        for (int activity_id : ((ActivityInstance) controlNodeInstance).getReferences()) {
            scenarioInstance.terminateReferenceControlNodeInstanceForControlNodeInstanceID(activity_id);
        }
    }
}
