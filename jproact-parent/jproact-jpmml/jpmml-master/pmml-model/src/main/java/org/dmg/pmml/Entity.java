/*
 * Copyright (c) 2013 University of Tartu
 */
package org.dmg.pmml;

import javax.xml.bind.annotation.*;

@XmlTransient
abstract
public class Entity extends PMMLObject implements HasId {

	@Override
	abstract
	public String getId();

	@Override
	abstract
	public void setId(String id);
}