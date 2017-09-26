package de.hpi.bpt.chimera.execution.gateway;

import org.apache.log4j.Logger;

import de.hpi.bpt.chimera.execution.ControlNodeInstance;
import de.hpi.bpt.chimera.execution.FragmentInstance;
import de.hpi.bpt.chimera.jcore.controlnodes.State;
import de.hpi.bpt.chimera.model.fragment.bpmn.AbstractControlNode;
import de.hpi.bpt.chimera.model.fragment.bpmn.ParallelGateway;

public class ParallelGatewayInstance extends ControlNodeInstance {
	private ParallelGateway parallelGateway;
	private int enabledInputCount = 0;
	private static Logger log = Logger.getLogger(ControlNodeInstance.class);


	/**
	 * Create a new ParallelGatewayInstance
	 * 
	 * @param parallelGateway
	 * @param fragmentInstance
	 */
	public ParallelGatewayInstance(ParallelGateway parallelGateway, FragmentInstance fragmentInstance) {
		super(parallelGateway, fragmentInstance);
		this.setParallelGateway(parallelGateway);
		this.state = State.INIT;
	}


	@Override
	public void enableControlFlow() {
		int noOfincomingControlFlows = this.getParallelGateway().getIncomingControlNodes().size();
		enabledInputCount++;
		log.info(String.format("A controlflow of a ParallelGateway was enabled (%d of %d)", enabledInputCount, noOfincomingControlFlows));
		if (enabledInputCount == noOfincomingControlFlows) {
			this.state = State.RUNNING;
			this.getFragmentInstance().enableFollowing(this.getParallelGateway());
			this.getFragmentInstance().getCase().getCaseExecutioner().startAutomaticTasks();
			this.enabledInputCount = 0;
		}
	}


	@Override
	public void begin() {
		// Is there anything to do here?
	}


	@Override
	public void terminate() {
		// Is there anything to do here?

	}


	@Override
	public void skip() {
		this.state = State.SKIPPED;
		this.enabledInputCount = 0;
		// TODO Is there anything else to do here?

	}


	// GETTER & SETTER
	public ParallelGateway getParallelGateway() {
		return parallelGateway;
	}

	public void setParallelGateway(ParallelGateway paralelGateway) {
		this.parallelGateway = paralelGateway;
	}
}
