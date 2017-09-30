package de.hpi.bpt.chimera.model.fragment.bpmn;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;

import de.hpi.bpt.chimera.model.condition.DataObjectStateCondition;
import de.hpi.bpt.chimera.model.datamodel.DataClass;
import de.hpi.bpt.chimera.model.datamodel.ObjectLifecycleState;

@Entity
public class DataNode {
	@Id
	@GeneratedValue(strategy = GenerationType.TABLE)
	private int dbId;

	private String id;

	private String name;
	@OneToOne(cascade = CascadeType.ALL)
	private DataObjectStateCondition dataObjectState;
	private String jsonPath;

	// GETTER & SETTER
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
	public DataObjectStateCondition getDataObjectState() {
		return dataObjectState;
	}
	public void setDataObjectState(DataObjectStateCondition dataObjectState) {
		this.dataObjectState = dataObjectState;
	}

	public String getJsonPath() {
		return jsonPath;
	}

	public void setJsonPath(String jsonPath) {
		this.jsonPath = jsonPath;
	}

	public DataClass getDataClass() {
		return dataObjectState.getDataClass();
	}

	public ObjectLifecycleState getObjectLifecycleState() {
		return dataObjectState.getState();
	}
}
