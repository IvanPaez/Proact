/*
 * Copyright, KNIME.com AG, Zurich, Switzerland
 */
package org.jpmml.evaluator;

import java.util.*;

import org.jpmml.manager.*;

import org.dmg.pmml.*;

import com.google.common.cache.*;
import com.google.common.collect.*;

public class SparseArrayUtil {

	private SparseArrayUtil(){
	}

	static
	public <E extends Number> int getSize(SparseArray<E> sparseArray){
		Integer n = sparseArray.getN();
		if(n != null){
			return n.intValue();
		}

		SortedMap<Integer, E> content = getContent(sparseArray);

		return content.size();
	}

	@SuppressWarnings (
		value = {"unchecked"}
	)
	static
	public <E extends Number> SortedMap<Integer, E> getContent(SparseArray<E> sparseArray){
		return (SortedMap<Integer, E>)CacheUtil.getValue(sparseArray, SparseArrayUtil.cache);
	}

	static
	public <E extends Number> double[] toArray(SparseArray<E> sparseArray){
		int size = getSize(sparseArray);

		double[] result = new double[size];

		for(int i = 0; i < size; i++){
			Number value = getValue(sparseArray, Integer.valueOf(i + 1));

			result[i] = value.doubleValue();
		}

		return result;
	}

	static
	public <E extends Number> SortedMap<Integer, E> parse(SparseArray<E> sparseArray){
		SortedMap<Integer, E> result = Maps.newTreeMap();

		List<Integer> indices = sparseArray.getIndices();
		List<E> entries = sparseArray.getEntries();

		// "Both arrays must have the same length"
		if(indices.size() != entries.size()){
			throw new InvalidFeatureException(sparseArray);
		}

		for(int i = 0; i < indices.size(); i++){
			Integer index = indices.get(i);
			E entry = entries.get(i);

			checkIndex(sparseArray, index);

			result.put(index, entry);
		}

		Integer n = sparseArray.getN();
		if(n != null && n.intValue() < result.size()){
			throw new InvalidFeatureException(sparseArray);
		}

		return result;
	}

	static
	public <E extends Number> E getValue(SparseArray<E> sparseArray, Integer index){

		if(sparseArray instanceof IntSparseArray){
			return (E)getIntValue((IntSparseArray)sparseArray, index);
		} else

		if(sparseArray instanceof RealSparseArray){
			return (E)getRealValue((RealSparseArray)sparseArray, index);
		}

		throw new UnsupportedFeatureException(sparseArray);
	}

	static
	public Integer getIntValue(IntSparseArray sparseArray, Integer index){
		Map<Integer, Integer> content = getContent(sparseArray);

		Integer result = content.get(index);
		if(result == null){
			checkIndex(sparseArray, index);

			return sparseArray.getDefaultValue();
		}

		return result;
	}

	static
	public Double getRealValue(RealSparseArray sparseArray, Integer index){
		Map<Integer, Double> content = getContent(sparseArray);

		Double result = content.get(index);
		if(result == null){
			checkIndex(sparseArray, index);

			return sparseArray.getDefaultValue();
		}

		return result;
	}

	static
	private <E extends Number> void checkIndex(SparseArray<E> sparseArray, Integer index){
		Integer n = sparseArray.getN();

		if(index < 1 || (n != null && index > n)){
			throw new EvaluationException();
		}
	}

	private static final LoadingCache<SparseArray<?>, SortedMap<Integer, ? extends Number>> cache = CacheBuilder.newBuilder()
		.weakKeys()
		.build(new CacheLoader<SparseArray<?>, SortedMap<Integer, ? extends Number>>(){

			@Override
			public SortedMap<Integer, ? extends Number> load(SparseArray<?> sparseArray){
				return parse(sparseArray);
			}

		});
}