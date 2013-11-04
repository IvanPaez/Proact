/*
 * Copyright (c) 2013 University of Tartu
 */
package org.dmg.pmml;

import javax.xml.bind.annotation.*;

@XmlTransient
abstract
public class ParameterCell extends PMMLObject {

	abstract
	public String getParameterName();

	abstract
	public void setParameterName(String parameterName);

	abstract
	public String getTargetCategory();

	abstract
	public void setTargetCategory(String targetCategory);
}