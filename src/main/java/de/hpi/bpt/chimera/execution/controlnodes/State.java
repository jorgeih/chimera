package de.hpi.bpt.chimera.execution.controlnodes;

import org.apache.log4j.Logger;

import de.hpi.bpt.chimera.execution.exception.IllegalStateNameException;

/**
 *
 */
public enum State {
	DATAFLOW_ENABLED("ready(Data)"), CONTROLFLOW_ENABLED("ready(ControlFlow)"), READY("ready"), EXECUTING("executing"), TERMINATED(
			"terminated"), SKIPPED("skipped"), INIT("init"), RUNNING("running"), CANCEL("cancel"), REGISTERED("registered");
	private static Logger log = Logger.getLogger(State.class);
	private String text;
	
	State() {
		this.text = "";
	}

	State(String text) {
		this.text = text;
	}

	public String getText() {
		return text;
	}

	/**
	 * Receive a State by a given text.
	 * 
	 * @param text
	 *            - of the state
	 * @return the corresponding State
	 * @throws IllegalStateNameException
	 *             if the text does not match a State.
	 */
	public static State fromString(String text) {
		if (text.isEmpty())
			return null;
		for (State state : State.values()) {
			if (state.text.equalsIgnoreCase(text)) {
				return state;
			}
		}
		IllegalStateNameException e = new IllegalStateNameException(text);
		log.error(e.getMessage());
		throw e;
	}
}
