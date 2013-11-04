/*
 * Copyright (c) 2012 University of Tartu
 */
package org.jpmml.evaluator;

import java.util.*;

import org.jpmml.manager.*;

import org.dmg.pmml.*;

/**
 * <p>
 * Performs the evaluation of a {@link Model} in "interpreted mode".
 * </p>
 *
 * Obtaining {@link org.jpmml.evaluator.Evaluator} instance:
 * <pre>
 * PMML pmml = ...;
 * PMMLManager pmmlManager = new PMMLManager(pmml);
 * Evaluator evaluator = (Evaluator)pmmlManager.getModelManager(null, ModelEvaluatorFactory.getInstance());
 * </pre>
 *
 * Preparing {@link org.jpmml.evaluator.Evaluator#getActiveFields() active fields}:
 * <pre>
 * Map&lt;FieldName, FieldValue&gt; arguments = new LinkedHashMap&lt;FieldName, FieldValue&gt;();
 * List&lt;FieldName&gt; activeFields = evaluator.getActiveFields();
 * for(FieldName activeField : activeFields){
 *   FieldValue activeValue = evaluator.prepare(activeField, ...);
 *   arguments.put(activeField, activeValue);
 * }
 * </pre>
 *
 * Performing the {@link org.jpmml.evaluator.Evaluator#evaluate(java.util.Map) evaluation}:
 * <pre>
 * Map&lt;FieldName, ?&gt; result = evaluator.evaluate(arguments);
 * </pre>
 *
 * Retrieving the value of the {@link org.jpmml.evaluator.Evaluator#getTargetField() target field} and {@link org.jpmml.evaluator.Evaluator#getOutputFields() output fields}:
 * <pre>
 * FieldName targetField = evaluator.getTargetField();
 * Object targetValue = result.get(targetField);
 *
 * List&lt;FieldName&gt; outputFields = evaluator.getOutputFields();
 * for(FieldName outputField : outputFields){
 *   Object outputValue = result.get(outputField);
 * }
 * </pre>
 *
 * Decoding {@link Computable complex value} to simple value:
 * <pre>
 * Object value = ...;
 * if(value instanceof Computable){
 *   Computable computable = (Computable)value;
 *
 *   value = computable.getResult();
 * }
 * </pre>
 *
 * @see EvaluatorUtil
 */
public interface Evaluator extends Consumer {

	/**
	 * Prepares the input value for a field.
	 *
	 * First, the value is converted from the user-supplied representation to internal representation.
	 * Later on, the value is subjected to missing value treatment, invalid value treatment and outlier treatment.
	 *
	 * @param name The name of the field
	 * @param string The input value in user-supplied representation. Use <code>null</code> to represent a missing input value.
	 *
	 * @throws PMMLException If the input value preparation fails.
	 *
	 * @see #getDataField(FieldName)
	 * @see #getMiningField(FieldName)
	 */
	FieldValue prepare(FieldName name, Object value);

	/**
	 * @param arguments Map of {@link #getActiveFields() active field} values.
	 *
	 * @return Map of {@link #getPredictedFields() predicted field} and {@link #getOutputFields() output field} values.
	 * Simple values are represented using the Java equivalents of PMML data types (eg. String, Integer, Float, Double etc.).
	 * Complex values are represented as instances of {@link Computable} that return simple values.
	 * A missing result is represented by <code>null</code>.
	 *
	 * @throws PMMLException If the evaluation fails.
	 * This is either {@link InvalidFeatureException} or {@link UnsupportedFeatureException} if there is a persistent structural problem with the PMML class model.
	 * This is {@link EvaluationException} (or one of its subclasses) if there is a problem with the evaluation request (eg. badly prepared arguments).
	 *
	 * @see Computable
	 */
	Map<FieldName, ?> evaluate(Map<FieldName, ?> arguments);
}