package de.uni_potsdam.hpi.bpt.bp2014.jcore.flowbehaviors;

import de.uni_potsdam.hpi.bpt.bp2014.database.DbControlFlow;
import de.uni_potsdam.hpi.bpt.bp2014.database.DbControlNode;
import de.uni_potsdam.hpi.bpt.bp2014.jcore.*;

/**
 * This class represents generic outgoing behavior.
 */
public abstract class AbstractOutgoingBehavior {
	private final DbControlFlow dbControlFlow = new DbControlFlow();
	private final DbControlNode dbControlNode = new DbControlNode();
	private ScenarioInstance scenarioInstance;
	private int controlNodeId;
	private int fragmentInstanceId;

	/**
	 * Terminates the control node instance.
	 */
	public abstract void terminate();

	/**
	 * Checks conditions after terminate a control node instance.
	 */
	public void checkAfterTermination() {
		scenarioInstance.checkDataFlowEnabled();
		scenarioInstance.checkExecutingGateways(controlNodeId);
	}

	/**
	 * Runs other methods after terminate a control node instance.
	 */
	public void runAfterTermination() {
		scenarioInstance.startAutomaticControlNodes();
	}

	/**
	 * @param type a type for the control node.
	 * @param controlNodeId the id of the control node to create.
	 * @return a control node instance of the specified type.
	 */
	protected AbstractControlNodeInstance createControlNode(String type, int controlNodeId) {
		AbstractControlNodeInstance controlNodeInstance = null;
		switch (type) {
		case "Activity":
		case "EmailTask":
		case "WebServiceTask":
			controlNodeInstance = new ActivityInstance(
                    controlNodeId, fragmentInstanceId, scenarioInstance);
			break;
		case "Endevent":
			controlNodeInstance = new EventInstance(
					fragmentInstanceId, scenarioInstance, "Endevent");
			break;
		case "XOR":
		case "AND":
			controlNodeInstance = new GatewayInstance(
                    controlNodeId, fragmentInstanceId, scenarioInstance);
			break;
        case "IntermediateEvent":
            controlNodeInstance = new IntermediateEvent(controlNodeId, fragmentInstanceId,
                    scenarioInstance);
            break;
        case "BoundaryEvent":
            controlNodeInstance = new BoundaryEvent(controlNodeId, fragmentInstanceId,
                    scenarioInstance);
        default:
			break;
		}
		return controlNodeInstance;
	}

	public DbControlFlow getDbControlFlow() {
		return dbControlFlow;
	}

	public DbControlNode getDbControlNode() {
		return dbControlNode;
	}

	public ScenarioInstance getScenarioInstance() {
		return scenarioInstance;
	}

	public int getControlNodeId() {
		return controlNodeId;
	}

	public int getFragmentInstanceId() {
		return fragmentInstanceId;
	}

	public void setScenarioInstance(ScenarioInstance scenarioInstance) {
		this.scenarioInstance = scenarioInstance;
	}

	public void setControlNodeId(int controlNodeId) {
		this.controlNodeId = controlNodeId;
	}

	public void setFragmentInstanceId(int fragmentInstanceId) {
		this.fragmentInstanceId = fragmentInstanceId;
	}
}