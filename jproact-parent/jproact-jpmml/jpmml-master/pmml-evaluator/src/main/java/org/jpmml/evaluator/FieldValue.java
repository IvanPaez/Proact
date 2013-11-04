/*
 * Copyright (c) 2013 University of Tartu
 */
package org.jpmml.evaluator;

import java.io.*;

import org.dmg.pmml.*;

import com.google.common.base.*;
import com.google.common.base.Objects.ToStringHelper;

import org.joda.time.*;

abstract
public class FieldValue implements Serializable {

	private DataType dataType = null;

	private Object value = null;


	public FieldValue(DataType dataType, Object value){
		setDataType(dataType);
		setValue(value);
	}

	abstract
	public OpType getOpType();

	/**
	 * Checks if this value is equal to the reference value.
	 *
	 * @param string The reference value.
	 */
	public boolean equalsString(String string){
		return TypeUtil.equals(getDataType(), getValue(), parseValue(string));
	}

	public boolean equalsAnyString(Iterable<String> strings){

		for(String string : strings){
			boolean equals = equalsString(string);

			if(equals){
				return true;
			}
		}

		return false;
	}

	public boolean equalsValue(FieldValue value){
		DataType dataType = TypeUtil.getResultDataType(getDataType(), value.getDataType());

		return TypeUtil.equals(dataType, getValue(), value.getValue());
	}

	public boolean equalsAnyValue(Iterable<FieldValue> values){

		for(FieldValue value : values){
			boolean equals = equalsValue(value);

			if(equals){
				return true;
			}
		}

		return false;
	}

	/**
	 * Calculates the order between this value and the reference value.
	 *
	 * @param string The reference value.
	 */
	public int compareToString(String string){
		return TypeUtil.compare(getDataType(), getValue(), parseValue(string));
	}

	public int compareToValue(FieldValue value){
		DataType dataType = TypeUtil.getResultDataType(getDataType(), value.getDataType());

		return TypeUtil.compare(dataType, getValue(), value.getValue());
	}

	public Object parseValue(String string){
		DataType dataType = getDataType();

		return TypeUtil.parse(dataType, string);
	}

	public String asString(){
		Object value = getValue();

		if(value instanceof String){
			return (String)value;
		}

		throw new TypeCheckException(DataType.STRING, value);
	}

	public Integer asInteger(){
		Object value = getValue();

		if(value instanceof Integer){
			return (Integer)value;
		}

		throw new TypeCheckException(DataType.INTEGER, value);
	}

	public Number asNumber(){
		Object value = getValue();

		if(value instanceof Number){
			return (Number)value;
		}

		throw new TypeCheckException(DataType.DOUBLE, value);
	}

	public Boolean asBoolean(){
		Object value = getValue();

		if(value instanceof Boolean){
			return (Boolean)value;
		}

		throw new TypeCheckException(DataType.BOOLEAN, value);
	}

	public LocalDate asLocalDate(){
		Object value = getValue();

		if(value instanceof LocalDate){
			return (LocalDate)value;
		} else

		if(value instanceof LocalDateTime){
			LocalDateTime instant = (LocalDateTime)value;

			return instant.toLocalDate();
		}

		throw new TypeCheckException(DataType.DATE, value);
	}

	public LocalTime asLocalTime(){
		Object value = getValue();

		if(value instanceof LocalTime){
			return (LocalTime)value;
		} else

		if(value instanceof LocalDateTime){
			LocalDateTime instant = (LocalDateTime)value;

			return instant.toLocalTime();
		}

		throw new TypeCheckException(DataType.TIME, value);
	}

	public LocalDateTime asLocalDateTime(){
		Object value = getValue();

		if(value instanceof LocalDate){
			LocalDate instant = (LocalDate)value;

			return new LocalDateTime(instant.getYear(), instant.getMonthOfYear(), instant.getDayOfMonth(), 0, 0, 0);
		} else

		if(value instanceof LocalDateTime){
			return (LocalDateTime)value;
		}

		throw new TypeCheckException(DataType.DATE_TIME, value);
	}

	public DateTime asDateTime(){
		Object value = getValue();

		if(value instanceof LocalDate){
			LocalDate instant = (LocalDate)value;

			return instant.toDateTimeAtStartOfDay();
		} else

		if(value instanceof LocalTime){
			LocalTime instant = (LocalTime)value;

			return instant.toDateTimeToday();
		} else

		if(value instanceof LocalDateTime){
			LocalDateTime instant = (LocalDateTime)value;

			return instant.toDateTime();
		}

		throw new TypeCheckException(DataType.DATE_TIME, value);
	}

	@Override
	public String toString(){
		ToStringHelper helper = Objects.toStringHelper(getClass())
			.add("opType", getOpType())
			.add("dataType", getDataType())
			.add("value", getValue());

		return helper.toString();
	}

	public DataType getDataType(){
		return this.dataType;
	}

	private void setDataType(DataType dataType){

		if(dataType == null){
			throw new NullPointerException();
		}

		this.dataType = dataType;
	}

	public Object getValue(){
		return this.value;
	}

	private void setValue(Object value){

		if(value == null){
			throw new NullPointerException();
		}

		this.value = value;
	}
}