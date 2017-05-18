package de.hpi.bpt.chimera.model.condition;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;

@Entity
public class TerminationCondition {
	@Id
	@GeneratedValue(strategy = GenerationType.TABLE)
	private int dbId;
	@OneToMany(cascade = CascadeType.PERSIST)
	List<TerminationConditionComponent> conditions;

	public TerminationCondition() {
		this.conditions = new ArrayList<>();
	}

	public List<TerminationConditionComponent> getConditions() {
		return conditions;
	}

	public void setConditions(List<TerminationConditionComponent> conditions) {
		this.conditions = conditions;
	}

	public void addConditionComponent(TerminationConditionComponent condition) {
		this.conditions.add(condition);
	}
}
