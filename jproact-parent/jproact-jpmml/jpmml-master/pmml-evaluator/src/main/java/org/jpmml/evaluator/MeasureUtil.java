/*
 * Copyright (c) 2013 University of Tartu
 */
package org.jpmml.evaluator;

import java.util.*;

import org.jpmml.manager.*;

import org.dmg.pmml.*;

import com.google.common.collect.*;

public class MeasureUtil {

	private MeasureUtil(){
	}

	static
	public boolean isSimilarity(Measure measure){
		return (measure instanceof SimpleMatching || measure instanceof Jaccard || measure instanceof Tanimoto || measure instanceof BinarySimilarity);
	}

	static
	public Double evaluateSimilarity(ComparisonMeasure comparisonMeasure, List<? extends ComparisonField> comparisonFields, BitSet flags, BitSet referenceFlags){
		Measure measure = comparisonMeasure.getMeasure();

		double a11 = 0d;
		double a10 = 0d;
		double a01 = 0d;
		double a00 = 0d;

		for(int i = 0; i < comparisonFields.size(); i++){

			if(flags.get(i)){

				if(referenceFlags.get(i)){
					a11 += 1d;
				} else

				{
					a10 += 1d;
				}
			} else

			{
				if(referenceFlags.get(i)){
					a01 += 1d;
				} else

				{
					a00 += 1d;
				}
			}
		}

		double numerator;
		double denominator;

		if(measure instanceof SimpleMatching){
			numerator = (a11 + a00);
			denominator = (a11 + a10 + a01 + a00);
		} else

		if(measure instanceof Jaccard){
			numerator = (a11);
			denominator = (a11 + a10 + a01);
		} else

		if(measure instanceof Tanimoto){
			numerator = (a11 + a00);
			denominator = (a11 + 2d * (a10 + a01) + a00);
		} else

		if(measure instanceof BinarySimilarity){
			BinarySimilarity binarySimilarity = (BinarySimilarity)measure;

			numerator = (binarySimilarity.getC11Parameter() * a11 + binarySimilarity.getC10Parameter() * a10 + binarySimilarity.getC01Parameter() * a01 + binarySimilarity.getC00Parameter() * a00);
			denominator = (binarySimilarity.getD11Parameter() * a11 + binarySimilarity.getD10Parameter() * a10 + binarySimilarity.getD01Parameter() * a01 + binarySimilarity.getD00Parameter() * a00);
		} else

		{
			throw new UnsupportedFeatureException(measure);
		}

		try {
			return (numerator / denominator);
		} catch(ArithmeticException ae){
			throw new InvalidResultException(null);
		}
	}

	static
	public BitSet toBitSet(List<FieldValue> values){
		BitSet result = new BitSet(values.size());

		for(int i = 0; i < values.size(); i++){
			FieldValue value = values.get(i);

			if((MeasureUtil.ZERO).equalsValue(value)){
				result.set(i, false);
			} else

			if((MeasureUtil.ONE).equalsValue(value)){
				result.set(i, true);
			} else

			{
				throw new EvaluationException();
			}
		}

		return result;
	}

	static
	public boolean isDistance(Measure measure){
		return (measure instanceof Euclidean || measure instanceof SquaredEuclidean || measure instanceof Chebychev || measure instanceof CityBlock || measure instanceof Minkowski);
	}

	static
	public Double evaluateDistance(ComparisonMeasure comparisonMeasure, List<? extends ComparisonField> comparisonFields, List<FieldValue> values, List<FieldValue> referenceValues, Double adjustment){
		Measure measure = comparisonMeasure.getMeasure();

		double innerPower;
		double outerPower;

		if(measure instanceof Euclidean){
			innerPower = outerPower = 2;
		} else

		if(measure instanceof SquaredEuclidean){
			innerPower = 2;
			outerPower = 1;
		} else

		if(measure instanceof Chebychev || measure instanceof CityBlock){
			innerPower = outerPower = 1;
		} else

		if(measure instanceof Minkowski){
			Minkowski minkowski = (Minkowski)measure;

			double p = minkowski.getPParameter();
			if(p < 0){
				throw new InvalidFeatureException(minkowski);
			}

			innerPower = outerPower = p;
		} else

		{
			throw new UnsupportedFeatureException(measure);
		}

		List<Double> distances = Lists.newArrayList();

		comparisonFields:
		for(int i = 0; i < comparisonFields.size(); i++){
			ComparisonField comparisonField = comparisonFields.get(i);

			FieldValue value = values.get(i);
			if(value == null){
				continue comparisonFields;
			}

			FieldValue referenceValue = referenceValues.get(i);

			Double distance = evaluateInnerFunction(comparisonMeasure, comparisonField, value, referenceValue, innerPower);

			distances.add(distance);
		}

		if(measure instanceof Euclidean || measure instanceof SquaredEuclidean || measure instanceof CityBlock || measure instanceof Minkowski){
			double sum = 0;

			for(Double distance : distances){
				sum += distance.doubleValue();
			}

			return Math.pow(sum * adjustment.doubleValue(), 1d / outerPower);
		} else

		if(measure instanceof Chebychev){
			Double max = Collections.max(distances);

			return max.doubleValue() * adjustment.doubleValue();
		} else

		{
			throw new UnsupportedFeatureException(measure);
		}
	}

	static
	private double evaluateInnerFunction(ComparisonMeasure comparisonMeasure, ComparisonField comparisonField, FieldValue value, FieldValue referenceValue, Double power){
		CompareFunctionType compareFunction = comparisonField.getCompareFunction();

		if(compareFunction == null){
			compareFunction = comparisonMeasure.getCompareFunction();

			// The ComparisonMeasure element is limited to "attribute-less" comparison functions
			switch(compareFunction){
				case ABS_DIFF:
				case DELTA:
				case EQUAL:
					break;
				case GAUSS_SIM:
				case TABLE:
					throw new InvalidFeatureException(comparisonMeasure);
				default:
					throw new UnsupportedFeatureException(comparisonMeasure, compareFunction);
			}
		}

		double distance;

		switch(compareFunction){
			case ABS_DIFF:
				{
					double z = difference(value, referenceValue);

					distance = Math.abs(z);
				}
				break;
			case GAUSS_SIM:
				{
					Double similarityScale = comparisonField.getSimilarityScale();
					if(similarityScale == null){
						throw new InvalidFeatureException(comparisonField);
					}

					double z = difference(value, referenceValue);
					double s = similarityScale.doubleValue();

					distance = Math.exp(-Math.log(2d) * Math.pow(z, 2d) / Math.pow(s, 2d));
				}
				break;
			case DELTA:
				{
					boolean equals = equals(value, referenceValue);

					distance = (equals ? 0d : 1d);
				}
				break;
			case EQUAL:
				{
					boolean equals = equals(value, referenceValue);

					distance = (equals ? 1d : 0d);
				}
				break;
			case TABLE:
				throw new UnsupportedFeatureException(comparisonField, compareFunction);
			default:
				throw new UnsupportedFeatureException(comparisonField, compareFunction);
		}

		return comparisonField.getFieldWeight() * Math.pow(distance, power.doubleValue());
	}

	static
	private double difference(FieldValue x, FieldValue y){
		return ((x.asNumber()).doubleValue() - (y.asNumber()).doubleValue());
	}

	static
	private boolean equals(FieldValue x, FieldValue y){
		return (x).equalsValue(y);
	}

	static
	public Double calculateAdjustment(List<FieldValue> values, List<Double> adjustmentValues){
		double sum = 0d;
		double nonmissingSum = 0d;

		for(int i = 0; i < values.size(); i++){
			FieldValue value = values.get(i);

			Double adjustmentValue = adjustmentValues.get(i);

			sum += adjustmentValue.doubleValue();
			nonmissingSum += (value != null ? adjustmentValue.doubleValue() : 0d);
		}

		return (sum / nonmissingSum);
	}

	static
	public Double calculateAdjustment(List<FieldValue> values){
		double sum = 0d;
		double nonmissingSum = 0d;

		for(int i = 0; i < values.size(); i++){
			FieldValue value = values.get(i);

			sum += 1d;
			nonmissingSum += (value != null ? 1d : 0d);
		}

		return (sum / nonmissingSum);
	}

	private static final FieldValue ZERO = FieldValueUtil.create(DataType.DOUBLE, OpType.CONTINUOUS, 0d);
	private static final FieldValue ONE = FieldValueUtil.create(DataType.DOUBLE, OpType.CONTINUOUS, 1d);
}