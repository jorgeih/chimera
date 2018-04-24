package de.hpi.bpt.chimera.execution.controlnodes.event.behavior;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import de.hpi.bpt.chimera.CaseExecutionerTestHelper;
import de.hpi.bpt.chimera.CaseModelTestHelper;
import de.hpi.bpt.chimera.execution.CaseExecutioner;
import de.hpi.bpt.chimera.execution.FragmentInstance;
import de.hpi.bpt.chimera.execution.controlnodes.State;
import de.hpi.bpt.chimera.execution.controlnodes.activity.AbstractActivityInstance;
import de.hpi.bpt.chimera.execution.controlnodes.event.AbstractEventInstance;
import de.hpi.bpt.chimera.execution.controlnodes.event.BoundaryEventInstance;
import de.hpi.bpt.chimera.execution.controlnodes.event.IntermediateCatchEventInstance;
import de.hpi.bpt.chimera.execution.controlnodes.event.StartEventInstance;
import de.hpi.bpt.chimera.execution.data.DataAttributeInstance;
import de.hpi.bpt.chimera.execution.data.DataObject;
import de.hpi.bpt.chimera.model.CaseModel;

public class MessageReceiveBehaviorTest {
	private final String filepath = "src/test/resources/execution/event/receiveEventBehaviorTest.json";
	private CaseModel cm;
	private CaseExecutioner caseExecutioner;
	private String eventJson = "{" + "\"a\": \"1\"," + "\"b\": \"2\"," + "}";

	@Before
	public void setup() {
		cm = CaseModelTestHelper.parseCaseModel(filepath);
		caseExecutioner = new CaseExecutioner(cm, cm.getName());
		caseExecutioner.startCase();
	}

	// TODO: to test receive event properly you need to mock unicorn
	@Test
	public void testIntermediateReceiveBehavior() {
		FragmentInstance intermediateFragment = CaseExecutionerTestHelper.getFragmentInstanceByName(caseExecutioner, "IntermediateFragment");
		assertNotNull(intermediateFragment);

		// StartEvent and ReceiveEvent
		assertEquals("StartEvent and ReceiveEvent should be in the FragmentInstance", intermediateFragment.getControlNodeInstances().size(), 2);

		StartEventInstance startEventInstance = (StartEventInstance) CaseExecutionerTestHelper.getEventInstanceByName(intermediateFragment, "startEvent");
		assertEquals(State.TERMINATED, startEventInstance.getState());

		assertEquals(0, caseExecutioner.getDataManager().getDataObjects().size());
		AbstractEventInstance eventInstance = CaseExecutionerTestHelper.getEventInstanceByName(intermediateFragment, "intermediateReceiveEvent");
		assertTrue(IntermediateCatchEventInstance.class.isInstance(eventInstance));
		assertTrue(MessageReceiveEventBehavior.class.isInstance(eventInstance.getBehavior()));

		MessageReceiveEventBehavior receiveBehavior = (MessageReceiveEventBehavior) eventInstance.getBehavior();
		assertEquals("ReceiveEvent in incorrect state", State.REGISTERED, eventInstance.getState());

		if (caseExecutioner.getRegisteredEventBehaviors().size() == 0) {
			// the event instance was not properly registered in Unicorn so it
			// needs to be added manually to the CaseExecutioner
			caseExecutioner.addRegisteredEventBehavior(receiveBehavior);
		}

		// elude the call of receive event in EventRestService
		receiveBehavior.setEventJson(eventJson);
		eventInstance.terminate();

		assertEquals("ReceiveEvent terminated properly", State.TERMINATED, eventInstance.getState());
		assertEquals("StartEvent, ReceiveEvent and EndEvent should be in the FragmentInstance", 3, intermediateFragment.getControlNodeInstances().size());

		assertEquals("One DataObject should be created", 1, caseExecutioner.getDataManager().getDataObjects().size());
		DataObject dataObject = caseExecutioner.getDataManager().getDataObjects().get(0);
		DataAttributeInstance attributeInstance = dataObject.getDataAttributeInstances().get(0);
		assertEquals("DataObject was not properly written", "1", attributeInstance.getValue());
	}

	@Test
	public void testBoundaryReceiveBehavior() {
		FragmentInstance boundaryFragment = CaseExecutionerTestHelper.getFragmentInstanceByName(caseExecutioner, "BoundaryFragment");
		assertNotNull(boundaryFragment);

		assertEquals("StartEvent and Task should be in the FragmentInstance", boundaryFragment.getControlNodeInstances().size(), 2);
		assertEquals(caseExecutioner.getDataManager().getDataObjects().size(), 0);

		AbstractActivityInstance task = CaseExecutionerTestHelper.getActivityInstanceByName(caseExecutioner, "task");
		assertEquals("BoundaryEvents", task.getControlNode().getAttachedBoundaryEvents().size(), 1);
		task.begin();
		assertEquals("task has not begun properly", task.getState(), State.RUNNING);
		assertEquals("StartEvent, Task and BoundaryEvent should be in the FragmentInstance", boundaryFragment.getControlNodeInstances().size(), 3);

		AbstractEventInstance eventInstance = CaseExecutionerTestHelper.getEventInstanceByName(boundaryFragment, "boundaryReceiveEvent");
		assertTrue(BoundaryEventInstance.class.isInstance(eventInstance));
		assertTrue(MessageReceiveEventBehavior.class.isInstance(eventInstance.getBehavior()));
		MessageReceiveEventBehavior receiveBehavior = (MessageReceiveEventBehavior) eventInstance.getBehavior();
		assertEquals("ReceiveEvent registered properly", eventInstance.getState(), State.REGISTERED);

		// elude the call of receive event in EventRestService
		receiveBehavior.setEventJson(eventJson);
		eventInstance.terminate();

		assertEquals("ReceiveEvent terminated properly", eventInstance.getState(), State.TERMINATED);
		assertEquals("StartEvent, Task, BoundaryEvent and Task after BoundaryEvent should be in the FragmentInstance", boundaryFragment.getControlNodeInstances().size(), 4);
		assertEquals("Task was not canceled", task.getState(), State.CANCEL);

		assertEquals("One DataObject should be created", caseExecutioner.getDataManager().getDataObjects().size(), 1);
		DataObject dataObject = caseExecutioner.getDataManager().getDataObjects().get(0);
		DataAttributeInstance attributeInstance = dataObject.getDataAttributeInstances().get(0);
		assertEquals("DataObject was not properly written", attributeInstance.getValue(), "2");
	}
}
