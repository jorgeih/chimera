package de.uni_potsdam.hpi.bpt.bp2014.jcore.executionbehaviors;

import de.uni_potsdam.hpi.bpt.bp2014.jcore.ScenarioInstance;
import de.uni_potsdam.hpi.bpt.bp2014.jcore.controlnodes.AbstractControlNodeInstance;
import de.uni_potsdam.hpi.bpt.bp2014.jcore.controlnodes.ActivityInstance;
import org.apache.commons.lang3.NotImplementedException;

import java.util.Map;

/**
 * Performs the execution for one activity.
 */
public class TaskExecutionBehavior {
	private final ScenarioInstance scenarioInstance;
	protected final int activityInstanceId;
	private final AbstractControlNodeInstance controlNodeInstance;

	/**
	 * Initializes.
	 *
	 * @param activityInstanceId Id from the activity instance in the database.
	 * @param scenarioInstance    This is an instance from the class ScenarioInstance.
	 * @param controlNodeInstance This is an AbstractControlNodeInstance.
	 */
	public TaskExecutionBehavior(int activityInstanceId, ScenarioInstance scenarioInstance,
			AbstractControlNodeInstance controlNodeInstance) {
		this.activityInstanceId = activityInstanceId;
		this.scenarioInstance = scenarioInstance;
		this.controlNodeInstance = controlNodeInstance;
	}

	/**
	 * Executes the behavior of the activity.
	 */
	public void execute() {
        // TODO find out if this should be abstract
	}

	/**
	 * @param canTerminate a boolean stating whether it can terminate.
	 */
	protected void setCanTerminate(boolean canTerminate) {
		((ActivityInstance) controlNodeInstance).setCanTerminate(canTerminate);
	}

	/**
	 * @param values a Map of Keys and Values.
	 */
	public void setDataAttributeValues(Map<Integer, String> values) {
	}

	public ScenarioInstance getScenarioInstance() {
		return scenarioInstance;
	}

	public AbstractControlNodeInstance getControlNodeInstance() {
		return controlNodeInstance;
	}
}