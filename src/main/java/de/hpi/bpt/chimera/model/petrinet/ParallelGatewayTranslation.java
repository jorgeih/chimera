package de.hpi.bpt.chimera.model.petrinet;

import de.hpi.bpt.chimera.model.fragment.bpmn.SequenceFlowAssociation;
import de.hpi.bpt.chimera.model.fragment.bpmn.gateway.AbstractGateway;

public class ParallelGatewayTranslation extends AbstractGatewayTranslation {
	public ParallelGatewayTranslation(TranslationContext translationContext, AbstractGateway gateway) {
		super(translationContext, gateway);

		final String prefixString = this.context.getPrefixString();

		for (SequenceFlowAssociation incomingSequenceFlow : gateway.getIncomingSequenceFlows()) {
			inputPlaces.add(addPlace(prefixString + "from_" + incomingSequenceFlow.getSourceRef().getId()));
		}
		for (SequenceFlowAssociation outgoingSequenceFlow : gateway.getOutgoingSequenceFlows()) {
			outputPlaces.add(addPlace(prefixString + "to_" + outgoingSequenceFlow.getTargetRef().getId()));
		}

		assert (getInputPlaces().size() == 1 || getOutputPlaces().size() == 1);

		Transition transition = addTransition(prefixString);
		if (isSplit()) {
			transition.addInputPlace(getInitialPlace());
			for (Place outputPlace : getOutputPlaces()) {
				transition.addOutputPlace(outputPlace);
			}
		} else {
			transition.addOutputPlace(getFinalPlace());
			for (Place inputPlace : getInputPlaces()) {
				transition.addInputPlace(inputPlace);
			}
		}
	}
}