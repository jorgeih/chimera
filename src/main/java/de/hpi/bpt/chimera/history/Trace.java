package de.hpi.bpt.chimera.history;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.List;

/**
 *
 */
public class Trace {
	int scenarioInstanceId;

	public Trace(int scenarioInstanceId) {
		this.scenarioInstanceId = scenarioInstanceId;
	}

	public void appendToLog(Element rootElement) {
		Document doc = rootElement.getOwnerDocument();
		Element traceXml = doc.createElement("trace");
		List<LogEntry> logEntries = new DbLogEntry().getLogEntriesForScenarioInstance(this.scenarioInstanceId);
		for (LogEntry logEntry : logEntries) {
			//ProM only cares about completed activities
			if ("terminated".equals(logEntry.getNewValue())) {
				logEntry.appendToTrace(traceXml);
			}
		}
		rootElement.appendChild(traceXml);
	}
}
