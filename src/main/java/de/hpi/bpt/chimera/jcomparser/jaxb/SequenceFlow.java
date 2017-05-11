package de.hpi.bpt.chimera.jcomparser.jaxb;

import de.hpi.bpt.chimera.jcomparser.saving.Connector;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Map;

/**
 * A class used to store Sequence Flow elements from standard BPMN.
 * Example xml String
 * <bpmn:sequenceFlow id="SequenceFlow_17mh7cg"
 * sourceRef="Task_0xjpfqa"
 * targetRef="ExclusiveGateway_0ns9z26"
 * />
 */
@XmlRootElement(name = "bpmn:sequenceFlow")
@XmlAccessorType(XmlAccessType.NONE)
public class SequenceFlow extends Edge {
	@XmlAttribute(name = "id")
	private String id;
	/**
	 * Node from which edge starts. This can be a gateway, activity or event.
	 */
	@XmlAttribute(name = "sourceRef")
	private String sourceRef;
	/**
	 * Node to which edge points to. This can be a gateway, activity or event.
	 */
	@XmlAttribute(name = "targetRef")
	private String targetRef;
	/**
	 * The "name" of this flow, a String that might contain a condition.
	 */
	@XmlAttribute(name = "name")
	private String condition;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getSourceRef() {
		return sourceRef;
	}

	public void setSourceRef(String sourceRef) {
		this.sourceRef = sourceRef;
	}

	public String getTargetRef() {
		return targetRef;
	}

	public void setTargetRef(String targetRef) {
		this.targetRef = targetRef;
	}

	public String getCondition() {
		return condition;
	}

	public void setCondition(String condition) {
		this.condition = condition;
	}

	/**
	 * Saves a sequence flow by inserting it into the control flow table.
	 *
	 * @param nodeToDatabaseId Map from nodeId to database Id
	 * @return returns the databaseId of the saved edge
	 */
	public int save(Map<String, Integer> nodeToDatabaseId) {
		int targetDatabaseId = nodeToDatabaseId.get(targetRef);
		int sourceDatabaseId = nodeToDatabaseId.get(sourceRef);
		Connector connector = new Connector();
		// TODO check if condition really is a condition according to the XORGrammar.g in src/main/resources
		String conditionForDatabase = (condition == null) ? "" : condition;
		connector.insertControlFlow(sourceDatabaseId, targetDatabaseId, conditionForDatabase);
		return 0;
	}

}