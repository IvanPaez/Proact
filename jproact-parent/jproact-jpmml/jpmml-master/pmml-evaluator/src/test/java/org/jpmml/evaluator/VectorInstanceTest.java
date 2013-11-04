/*
 * Copyright, KNIME.com AG, Zurich, Switzerland
 */
package org.jpmml.evaluator;

import java.util.*;

import org.dmg.pmml.*;

import org.junit.*;

import static org.junit.Assert.*;

public class VectorInstanceTest extends SupportVectorMachineModelEvaluatorTest {

	@Test
	public void evaluate() throws Exception {
		SupportVectorMachineModelEvaluator evaluator = createEvaluator();

		assertTrue(VerificationUtil.acceptable(-0.3995764, evaluate(evaluator, 0.0d, 0.0d)));
		assertTrue(VerificationUtil.acceptable(0.3995764, evaluate(evaluator, 0.0d, 1.0d)));
		assertTrue(VerificationUtil.acceptable(0.3995764, evaluate(evaluator, 1.0d, 0.0d)));
		assertTrue(VerificationUtil.acceptable(-0.3995764, evaluate(evaluator, 1.0d, 1.0d)));
	}

	static
	private double evaluate(Evaluator evaluator, double x1, double x2){
		Map<FieldName, ?> arguments = createArguments("x1", x1, "x2", x2);

		Map<FieldName, ?> result = evaluator.evaluate(arguments);

		FieldName targetField = evaluator.getTargetField();

		Number targetValue = (Number)result.get(targetField);

		return targetValue.doubleValue();
	}
}