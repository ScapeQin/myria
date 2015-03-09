package edu.washington.escience.myria.api.encoding;

import edu.washington.escience.myria.api.encoding.QueryConstruct.ConstructArgs;
import edu.washington.escience.myria.operator.EStep;

public class EStepEncoding extends UnaryOperatorEncoding<EStep> {

	// @Required
	// public Expression argPredicate;

	@Required
	public int argNumDimensions;

	@Required
	public int argNumComponents;

	@Override
	public EStep construct(ConstructArgs args) {
		return new EStep(null, argNumDimensions, argNumComponents);
	}
}
