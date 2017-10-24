package de.hpi.bpt.chimera.execution;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.apache.log4j.Logger;

import de.hpi.bpt.chimera.execution.activity.AbstractActivityInstance;
import de.hpi.bpt.chimera.execution.event.StartEventInstance;
import de.hpi.bpt.chimera.execution.gateway.AbstractGatewayInstance;
import de.hpi.bpt.chimera.execution.gateway.ExclusiveGatewayInstance;
import de.hpi.bpt.chimera.jcore.controlnodes.State;
import de.hpi.bpt.chimera.model.fragment.Fragment;
import de.hpi.bpt.chimera.model.fragment.bpmn.AbstractControlNode;
import de.hpi.bpt.chimera.model.fragment.bpmn.event.StartEvent;

public class FragmentInstance {
	private static final Logger log = Logger.getLogger(FragmentInstance.class);

	private String id;
	private Fragment fragment;
	private Case caze;
	// TODO: think about whether it is needed to store terminated events, etc.
	// Activities should be recorded for the history.
	/**
	 * Map of Id of ControlNodeInstance to ControlNodeInstance.
	 */
	private Map<String, ControlNodeInstance> controlNodeInstances;
	/**
	 * Map of Id of ControlNode to the corresponding ControlNodeInstanc.
	 */
	private Map<String, ControlNodeInstance> controlNodes;

	public FragmentInstance(Fragment fragment, Case caze) {
		this.id = UUID.randomUUID().toString().replace("-", "");
		this.fragment = fragment;
		this.caze = caze;
		this.controlNodeInstances = new HashMap<>();
		this.controlNodes = new HashMap<>();
	}

	/**
	 * Start the Case by creating an Instance of the StartEvent and enable the
	 * ControlFlow of that Instance.
	 */
	public void start() {
		StartEvent startEvent = fragment.getBpmnFragment().getStartEvent();
		StartEventInstance startEventInstance = (StartEventInstance) ControlNodeInstanceFactory.createControlNodeInstance(startEvent, this);
		addControlNodeInstance(startEventInstance);
		startEventInstance.enableControlFlow();
	}

	/**
	 * Update DataFlow of all ActivityInstances.
	 */
	public void updateDataFlow() {
		for (ControlNodeInstance nodeInstance : controlNodeInstances.values()) { 
			if (nodeInstance instanceof AbstractActivityInstance) {
				((AbstractActivityInstance) nodeInstance).checkDataFlow();
			}
		} 
	}

	/**
	 * Create Instances of the following ControlNodes of controlNode and enable
	 * the ControlFlow of them.
	 * 
	 * @param node
	 */
	public void createFollowing(AbstractControlNode controlNode) {
		for (AbstractControlNode following : controlNode.getOutgoingControlNodes()) {
			if (isInstantiated(following)) {
				ControlNodeInstance nodeInstance = controlNodes.get(following.getId());
				nodeInstance.enableControlFlow();
			} else {
				ControlNodeInstance nodeInstance = ControlNodeInstanceFactory.createControlNodeInstance(following, this);
				addControlNodeInstance(nodeInstance);
				nodeInstance.enableControlFlow();
			}
		}
	}

	public ControlNodeInstance createControlNodeInstance(AbstractControlNode controlNode) {
		if (isInstantiated(controlNode)) {
			return controlNodes.get(controlNode.getId());
		} else {
			ControlNodeInstance nodeInstance = ControlNodeInstanceFactory.createControlNodeInstance(controlNode, this);
			addControlNodeInstance(nodeInstance);
			return nodeInstance;
		}
	}

	/**
	 * 
	 * @param controlNodes
	 * @return
	 */
	public boolean checkExclusiveGatewayBehaviour(ControlNodeInstance instance, List<ControlNodeInstance> instancesToRemove) {
		return false;
	}

	// HELPER METHODS
	/**
	 * 
	 * @param nodeInstance
	 */
	private void addControlNodeInstance(ControlNodeInstance nodeInstance) {
		controlNodeInstances.put(nodeInstance.getId(), nodeInstance);
		controlNodes.put(nodeInstance.getControlNode().getId(), nodeInstance);
	}

	/**
	 * 
	 * @param node
	 * @return true if the ControlNode is instantiated.
	 */
	public boolean isInstantiated(AbstractControlNode node) {
		return controlNodes.containsKey(node.getId());
	}

	/**
	 * 
	 * @param nodes
	 * @return true if all ControlNodes are instantiated.
	 */
	public boolean areInstantiated(List<AbstractControlNode> nodes) {
		for (AbstractControlNode node : nodes) {
			if (!isInstantiated(node))
				return false;
		}
		return true;
	}


	/**
	 * 
	 * @param nodes
	 * @return true if the ControlNode is terminated.
	 */
	public boolean isTerminated(AbstractControlNode node) {
		if (controlNodes.containsKey(node.getId()) && controlNodes.get(node.getId()).getState() == State.TERMINATED) {
			return true;
		} else {
			return false;
		}
	}

	// TODO maybe not needed >>> maybe a separate map for executing (Xor-)
	// Gateways is better and faster
	/**
	 * @return a ArrayList of executing gateways.
	 */
	public Map<String, ExclusiveGatewayInstance> getExecutingExclusiveGateways() {
		return this.controlNodes.entrySet().stream().filter(x -> (x.getValue().getClass().equals(ExclusiveGatewayInstance.class) && x.getValue().getState() == State.EXECUTING)).collect(Collectors.toMap(p -> p.getKey(), p -> (ExclusiveGatewayInstance) p.getValue()));
	}

	// TODO maybe not needed >>> maybe a separate map for executing (Xor-)
	// Gateways is better and faster
	/**
	 * @return a ArrayList of executing gateways.
	 */
	public Map<String, AbstractGatewayInstance> getExecutingGateways() {
		return this.controlNodes.entrySet().stream().filter(x -> (x.getValue() instanceof AbstractGatewayInstance && x.getValue().getState() == State.EXECUTING)).collect(Collectors.toMap(p -> p.getKey(), p -> (AbstractGatewayInstance) p.getValue()));
	}

	/**
	 * 
	 * @param controlNodeId
	 * @return the Instance of the ControlNode with the given Id. Null if that
	 *         ControlNode isn't instantiated yet.
	 */
	public ControlNodeInstance getControlNodeInstance(String controlNodeId) {
		// TODO is it possible that someone calls this method for a controllnode
		// that isn't instantiated yet?
		if (controlNodes.containsKey(controlNodeId)) {
			return controlNodes.get(controlNodeId);
		} else {
			return null;
		}
	}


	/**
	 * Checks whether the given ControlNodeInstance is part of a branch of an
	 * executing Gateway.In this case all the ControlNodeInstances of all the
	 * other branches of this Gateway will be skipped. (used for activities
	 * after ExclusiveGateways)
	 *
	 * @param controlNodeInstance
	 *            The control node instance, which was transferred to the state
	 *            ready.
	 */
	public void skipAlternativeControlNodes(ControlNodeInstance controlNodeInstance) {
		Map<String, ExclusiveGatewayInstance> gateways = this.getExecutingExclusiveGateways();
		for (ExclusiveGatewayInstance gateway : gateways.values()) {
			if (gateway.containsControlNodeInFollowing(controlNodeInstance.getControlNode())) {
				// log.info(String.format("skipping alternatives for the
				// following Gateway: %s", gateway.getId()));
				gateway.skipAlternativeBranches(controlNodeInstance.getControlNode());
			}
		}
	}


	// GETTER & SETTER
	public String getId() {
		return id;
	}

	public Fragment getFragment() {
		return fragment;
	}

	public void setFragment(Fragment fragment) {
		this.fragment = fragment;
	}

	public Map<String, ControlNodeInstance> getControlNodeInstances() {
		return controlNodeInstances;
	}

	public void setControlNodeInstances(Map<String, ControlNodeInstance> controlNodeInstances) {
		this.controlNodeInstances = controlNodeInstances;
	}

	public Case getCase() {
		return caze;
	}

	public void setCase(Case caze) {
		this.caze = caze;
	}
}
