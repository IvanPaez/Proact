/*
 * Copyright (c) 2013 University of Tartu
 */
package org.jpmml.evaluator;

import java.util.*;

import org.dmg.pmml.*;

/**
 * @see ResultFeatureType#REASON_CODE
 */
public interface HasReasonCodeRanking extends ResultFeature {

	List<String> getReasonCodeRanking();
}