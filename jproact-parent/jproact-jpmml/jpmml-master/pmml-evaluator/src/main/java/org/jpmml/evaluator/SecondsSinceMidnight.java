/*
 * Copyright (c) 2013 University of Tartu
 */
package org.jpmml.evaluator;

import org.joda.time.*;
import org.joda.time.field.*;
import org.joda.time.format.*;

public class SecondsSinceMidnight implements Comparable<SecondsSinceMidnight> {

	private Seconds seconds = null;


	public SecondsSinceMidnight(Seconds seconds){
		setSeconds(seconds);
	}

	@Override
	public int compareTo(SecondsSinceMidnight that){
		return (this.getSeconds()).compareTo(that.getSeconds());
	}

	@Override
	public int hashCode(){
		return getSeconds().hashCode();
	}

	@Override
	public boolean equals(Object object){

		if(object instanceof SecondsSinceMidnight){
			SecondsSinceMidnight that = (SecondsSinceMidnight)object;

			return (this.getSeconds()).equals(that.getSeconds());
		}

		return false;
	}

	public int intValue(){
		return getSeconds().getSeconds();
	}

	public Seconds getSeconds(){
		return this.seconds;
	}

	private void setSeconds(Seconds seconds){
		this.seconds = seconds;
	}

	static
	public DateTimeFormatter getFormat(){

		if(SecondsSinceMidnight.format == null){
			SecondsSinceMidnight.format = createFormat();
		}

		return SecondsSinceMidnight.format;
	}

	static
	private DateTimeFormatter createFormat(){
		DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder();
		builder = builder.appendSignedDecimal(HoursOfEpochFieldType.getInstance(), 1, 4)
					.appendLiteral(':')
					.appendFixedDecimal(DateTimeFieldType.minuteOfHour(), 2)
					.appendLiteral(':')
					.appendFixedDecimal(DateTimeFieldType.secondOfMinute(), 2);

		return builder.toFormatter();
	}

	private static DateTimeFormatter format = null;

	static
	private class HoursOfEpochFieldType extends DateTimeFieldType {

		private HoursOfEpochFieldType(){
			super("hoursOfEpoch");
		}

		@Override
		public DurationFieldType getDurationType(){
			return DurationFieldType.hours();
		}

		@Override
		public DurationFieldType getRangeDurationType(){
			return null;
		}

		@Override
		public DateTimeField getField(Chronology chronology){
			chronology = DateTimeUtils.getChronology(chronology);

			return new PreciseDurationDateTimeField(this, chronology.hours()){

				@Override
				public int get(long millis){
					long hours = (millis / HoursOfEpochFieldType.millisInHour);

					return FieldUtils.safeToInt(hours);
				}

				@Override
				public DurationField getRangeDurationField(){
					return null;
				}

				@Override
				public int getMinimumValue(){
					return 0;
				}

				@Override
				public int getMaximumValue(){
					return Integer.MAX_VALUE;
				}
			};
		}

		@Override
		public int hashCode(){
			return getName().hashCode();
		}

		@Override
		public boolean equals(Object object){

			if(object instanceof HoursOfEpochFieldType){
				HoursOfEpochFieldType that = (HoursOfEpochFieldType)object;

				return (this.getName()).equals(that.getName());
			}

			return false;
		}

		static
		public HoursOfEpochFieldType getInstance(){

			if(HoursOfEpochFieldType.instance == null){
				HoursOfEpochFieldType.instance = new HoursOfEpochFieldType();
			}

			return HoursOfEpochFieldType.instance;
		}

		private static HoursOfEpochFieldType instance;

		private static final long millisInHour = (60L * 60L * 1000L);
	}
}