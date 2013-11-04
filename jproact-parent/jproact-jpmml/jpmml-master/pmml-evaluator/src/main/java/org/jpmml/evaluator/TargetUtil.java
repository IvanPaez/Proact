/*
 * Copyright (c) 2013 University of Tartu
 */
package org.jpmml.evaluator;

import java.util.*;

import org.jpmml.manager.*;

import org.dmg.pmml.*;

import com.google.common.collect.*;

public class TargetUtil {

	private TargetUtil(){
	}

	static
	public Map<FieldName, ? extends Number> evaluateRegression(Double value, ModelManagerEvaluationContext context){
		ModelManager<?> modelManager = context.getModelManager();

		FieldName targetField = modelManager.getTargetField();

		return evaluateRegression(Collections.singletonMap(targetField, value), context);
	}

	/**
	 * Evaluates the {@link Targets} element for {@link MiningFunctionType#REGRESSION regression} models.
	 */
	static
	public Map<FieldName, ? extends Number> evaluateRegression(Map<FieldName, Double> predictions, ModelManagerEvaluationContext context){
		ModelManager<?> modelManager = context.getModelManager();

		Targets targets = modelManager.getOrCreateTargets();
		if(Iterables.isEmpty(targets)){
			return predictions;
		}

		Map<FieldName, Number> result = Maps.newLinkedHashMap();

		Collection<Map.Entry<FieldName, Double>> entries = predictions.entrySet();
		for(Map.Entry<FieldName, Double> entry : entries){
			FieldName key = entry.getKey();
			Number value = entry.getValue();

			Target target = modelManager.getTarget(key);
			if(target != null){

				if(value != null){
					value = process(target, entry.getValue());
				} else

				{
					value = getDefaultValue(target);
				}
			}

			result.put(key, value);
		}

		return result;
	}

	static
	public Map<FieldName, ? extends ClassificationMap<?>> evaluateClassification(ClassificationMap<?> value, ModelManagerEvaluationContext context){
		ModelManager<?> modelManager = context.getModelManager();

		FieldName targetField = modelManager.getTargetField();

		return evaluateClassification(Collections.singletonMap(targetField, value), context);
	}

	/**
	 * Evaluates the {@link Targets} element for {@link MiningFunctionType#CLASSIFICATION classification} models.
	 */
	static
	public Map<FieldName, ? extends ClassificationMap<?>> evaluateClassification(Map<FieldName, ? extends ClassificationMap<?>> predictions, ModelManagerEvaluationContext context){
		ModelManager<?> modelManager = context.getModelManager();

		Targets targets = modelManager.getOrCreateTargets();
		if(Iterables.isEmpty(targets)){
			return predictions;
		}

		Map<FieldName, ClassificationMap<?>> result = Maps.newLinkedHashMap();

		Collection<? extends Map.Entry<FieldName, ? extends ClassificationMap<?>>> entries = predictions.entrySet();
		for(Map.Entry<FieldName, ? extends ClassificationMap<?>> entry : entries){
			FieldName key = entry.getKey();
			ClassificationMap<?> value = entry.getValue();

			Target target = modelManager.getTarget(key);
			if(target != null){

				if(value == null){
					value = getPriorProbabilities(target);
				}
			}

			result.put(key, value);
		}

		return result;
	}

	static
	public Number process(Target target, Double value){
		double result = value.doubleValue();

		Double min = target.getMin();
		if(min != null){
			result = Math.max(result, min.doubleValue());
		}

		Double max = target.getMax();
		if(max != null){
			result = Math.min(result, max.doubleValue());
		}

		result = (result * target.getRescaleFactor() + target.getRescaleConstant());

		Target.CastInteger castInteger = target.getCastInteger();
		if(castInteger == null){
			return result;
		}

		switch(castInteger){
			case ROUND:
				return (int)Math.round(result);
			case CEILING:
				return (int)Math.ceil(result);
			case FLOOR:
				return (int)Math.floor(result);
			default:
				throw new UnsupportedFeatureException(target, castInteger);
		}
	}

	static
	public TargetValue getTargetValue(Target target, Object value){
		DataType dataType = TypeUtil.getDataType(value);

		List<TargetValue> targetValues = target.getTargetValues();
		for(TargetValue targetValue : targetValues){

			if(TypeUtil.equals(dataType, value, TypeUtil.parseOrCast(dataType, targetValue.getValue()))){
				return targetValue;
			}
		}

		return null;
	}

	static
	private Double getDefaultValue(Target target){
		List<TargetValue> values = target.getTargetValues();
		if(values.size() != 1){
			throw new InvalidFeatureException(target);
		}

		TargetValue value = values.get(0);
		if(value.getValue() != null || value.getPriorProbability() != null){
			throw new InvalidFeatureException(value);
		}

		return value.getDefaultValue();
	}

	static
	private DefaultClassificationMap<String> getPriorProbabilities(Target target){
		DefaultClassificationMap<String> result = new DefaultClassificationMap<String>();

		List<TargetValue> values = target.getTargetValues();
		for(TargetValue value : values){

			if(value.getDefaultValue() != null){
				throw new InvalidFeatureException(value);
			}

			result.put(value.getValue(), value.getPriorProbability());
		}

		return result;
	}
}