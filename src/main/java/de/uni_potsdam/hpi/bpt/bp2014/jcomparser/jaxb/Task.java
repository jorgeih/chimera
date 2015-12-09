package de.uni_potsdam.hpi.bpt.bp2014.jcomparser.jaxb;

import de.uni_potsdam.hpi.bpt.bp2014.jcomparser.xml.Node;

import javax.xml.bind.annotation.*;
import java.util.List;

/**
 * Class used to read in Activity from BPMN standard.
 * Example String
 * <bpmn:task id="Task_0c9phqs" name="Drink coffee">
 *      <bpmn:incoming>SequenceFlow_0ct04t8</bpmn:incoming>
 *      <bpmn:outgoing>SequenceFlow_1gyvb8d</bpmn:outgoing>
 * </bpmn:task>
 */
@XmlRootElement(name = "bpmn:task")
@XmlAccessorType(XmlAccessType.NONE)
public class Task {
    @XmlAttribute(name = "id")
    private String id;
    @XmlAttribute(name = "name")
    private String name;
    @XmlElement(name = "bpmn:incoming")
    private String incoming;
    @XmlElement(name = "bpmn:outgoing")
    private String outgoing;
    @XmlElement(name = "bpmn:dataOutputAssociation")
    private List<DataOutputAssociation> dataOutputAssociations;
    @XmlElement(name = "bpmn:dataInputAssociation")
    private List<DataInputAssociation> dataInputAssociations;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIncoming() {
        return incoming;
    }

    public void setIncoming(String incoming) {
        this.incoming = incoming;
    }

    public String getOutgoing() {
        return outgoing;
    }

    public void setOutgoing(String outgoing) {
        this.outgoing = outgoing;
    }

    public List<DataOutputAssociation> getDataOutputAssociations() {
        return dataOutputAssociations;
    }

    public void setDataOutputAssociations(List<DataOutputAssociation> dataOutputAssociations) {
        this.dataOutputAssociations = dataOutputAssociations;
    }

    public List<DataInputAssociation> getDataInputAssociations() {
        return dataInputAssociations;
    }

    public void setDataInputAssociations(List<DataInputAssociation> dataInputAssociations) {
        this.dataInputAssociations = dataInputAssociations;
    }

    public Node convertToNode() {
        Node node = new Node();
        node.setType("Task");
        node.setText(this.name);
        node.setId(this.id);
        node.setGlobal(false);
        return node;
    }
}
