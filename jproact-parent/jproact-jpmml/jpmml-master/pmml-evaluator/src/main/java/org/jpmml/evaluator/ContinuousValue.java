/*
 * Copyright (c) 2013 University of Tartu
 */
package org.jpmml.evaluator;

import org.dmg.pmml.*;

public class ContinuousValue extends FieldValue {

	public ContinuousValue(DataType dataType, Object value){
		super(dataType, value);
	}

	@Override
	public OpType getOpType(){
		return OpType.CONTINUOUS;
	}
}