package de.hpi.bpt.chimera.execution.gateway;

import java.util.List;

import org.apache.log4j.Logger;

import de.hpi.bpt.chimera.execution.ExecutionService;
import de.hpi.bpt.chimera.execution.FragmentInstance;
import de.hpi.bpt.chimera.jcore.controlnodes.State;
import de.hpi.bpt.chimera.model.fragment.bpmn.AbstractControlNode;
import de.hpi.bpt.chimera.model.fragment.bpmn.event.StartEvent;
import de.hpi.bpt.chimera.model.fragment.bpmn.gateway.ParallelGateway;

public class ParallelGatewayInstance extends AbstractGatewayInstance {
	private static final Logger log = Logger.getLogger(ParallelGatewayInstance.class);

	private ParallelGateway parallelGateway;

	/**
	 * Create a new ParallelGatewayInstance
	 * 
	 * @param parallelGateway
	 * @param fragmentInstance
	 */
	public ParallelGatewayInstance(ParallelGateway parallelGateway, FragmentInstance fragmentInstance) {
		super(parallelGateway, fragmentInstance);
		this.setParallelGateway(parallelGateway);
		this.setState(State.INIT);
	}


	@Override
	public void enableControlFlow() {
		if (checkEnabled()) {
			setState(State.READY);
			begin();
		}

		// if
		// (getFragmentInstance().areInstantiated(parallelGateway.getIncomingControlNodes()))
		// {
		// setState(State.READY);
		// begin();
		// }
	}


	@Override
	public void begin() {
		setState(State.EXECUTING);
		terminate();
	}


	@Override
	public void terminate() {
		getFragmentInstance().createFollowing(parallelGateway);
		setState(State.TERMINATED);
	}


	@Override
	public void skip() {
		setState(State.SKIPPED);
	}


	/**
	 * Checks if all control node instances before this gateway have been
	 * terminated
	 *
	 * @return true if this gateway can get enabled. false if not.
	 */
	private Boolean checkEnabled() {
		List<AbstractControlNode> predecessors = parallelGateway.getIncomingControlNodes();
		// if a start Event ist before this Gateway it is enabled
		if (predecessors.size() == 1 && predecessors.get(0).getClass().equals(StartEvent.class)) {
			return true;
		}
		// looks that all predecessors are terminated or executing Gateways
		for (AbstractControlNode node : predecessors) {
			if (!getFragmentInstance().isTerminated(node) && !getFragmentInstance().getExecutingGateways().containsKey(node)) {
				return false;
			}
		}
		return true;
	}


	// GETTER & SETTER
	public ParallelGateway getParallelGateway() {
		return parallelGateway;
	}

	public void setParallelGateway(ParallelGateway paralelGateway) {
		this.parallelGateway = paralelGateway;
	}
}
