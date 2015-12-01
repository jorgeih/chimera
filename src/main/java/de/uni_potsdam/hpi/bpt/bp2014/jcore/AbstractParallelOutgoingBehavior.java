package de.uni_potsdam.hpi.bpt.bp2014.jcore;

import java.util.LinkedList;

/**
 * This class represents parallel outgoing behavior.
 */
public abstract class AbstractParallelOutgoingBehavior extends AbstractOutgoingBehavior {
	/**
	 * Set all following control nodes to control flow enabled and initializes them.
	 */
	public void enableFollowing() {
		LinkedList<Integer> followingControlNodeIds = this.getDbControlFlow()
				.getFollowingControlNodes(this.getControlNodeId());
		for (int followingControlNodeId : followingControlNodeIds) {
			AbstractControlNodeInstance followingControlNodeInstance =
					createFollowingNodeInstance(followingControlNodeId);
			//enable following instances
			followingControlNodeInstance.enableControlFlow();
		}
	}

	/**
	 * Initializes and creates the following control node instance
	 * for the given control node instance id.
	 *
	 * @param controlNodeId This is the database id from the control node instance.
	 * @return the created control node instance.
	 */
	protected AbstractControlNodeInstance createFollowingNodeInstance(int controlNodeId) {
		for (AbstractControlNodeInstance controlNodeInstance
				: this.getScenarioInstance().getControlNodeInstances()) {
			if (controlNodeId == controlNodeInstance.getControlNodeId()
					&& !controlNodeInstance.getClass()
					.equals(ActivityInstance.class)
					&& !controlNodeInstance
					.getStateMachine().getState().equals("terminated")) {
				return controlNodeInstance;
			}
		}
		String type = this.getDbControlNode().getType(controlNodeId);
		return createControlNode(type, controlNodeId);
	}
}
