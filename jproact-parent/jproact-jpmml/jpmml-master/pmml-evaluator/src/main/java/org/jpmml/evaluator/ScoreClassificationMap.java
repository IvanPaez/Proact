/*
 * Copyright, KNIME.com AG, Zurich, Switzerland
 */
package org.jpmml.evaluator;

import java.util.*;

import com.google.common.annotations.*;

@Beta
public class ScoreClassificationMap extends ClassificationMap<String> implements HasReasonCodeRanking {

	private Number result = null;


	protected ScoreClassificationMap(Number result){
		super(Type.VOTE);

		setResult(result);
	}

	@Override
	public Number getResult(){
		return this.result;
	}

	private void setResult(Number result){
		this.result = result;
	}

	@Override
	public List<String> getReasonCodeRanking(){
		return getWinnerKeys();
	}
}