/*
 * Copyright (c) 2012 University of Tartu
 */
package org.jpmml.example;

import java.io.*;
import java.util.*;

import org.jpmml.manager.*;

import org.dmg.pmml.*;

import com.beust.jcommander.Parameter;

public class TreeModelTranslationExample extends Example {

	@Parameter (
		names = "--model",
		description = "The PMML file with a Decision tree model",
		required = true
	)
	private File model = null;


	static
	public void main(String... args) throws Exception {
		execute(TreeModelTranslationExample.class, args);
	}

	@Override
	public void execute() throws Exception {
		PMML pmml = IOUtil.unmarshal(this.model);

		TreeModel treeModel = PMMLManager.find(pmml.getModels(), TreeModel.class);

		format(treeModel.getNode(), "");
	}

	static
	public void format(Node node, String indent){
		Predicate predicate = node.getPredicate();
		if(predicate == null){
			throw new IllegalArgumentException("Missing predicate");
		}

		System.out.println(indent + "if(" + format(predicate) + "){");

		List<Node> children = node.getNodes();
		for(Node child : children){
			format(child, indent + "\t");
		}

		if(node.getScore() != null){
			System.out.println(indent + "\t" + "return \"" + node.getScore() + "\";");
		}

		System.out.println(indent + "}");
	}

	static
	private String format(Predicate predicate){

		if(predicate instanceof SimplePredicate){
			return format((SimplePredicate)predicate);
		} else

		if(predicate instanceof CompoundPredicate){
			return format((CompoundPredicate)predicate);
		}

		if(predicate instanceof True){
			return "true";
		} else

		if(predicate instanceof False){
			return "false";
		}

		throw new IllegalArgumentException("Unsupported predicate " + predicate);
	}

	static
	private String format(SimplePredicate simplePredicate){
		StringBuffer sb = new StringBuffer();

		sb.append((simplePredicate.getField()).getValue());
		sb.append(' ').append(format(simplePredicate.getOperator())).append(' ');
		sb.append(simplePredicate.getValue());

		return sb.toString();
	}

	static
	private String format(SimplePredicate.Operator operator){

		switch(operator){
			case EQUAL:
				return "==";
			case NOT_EQUAL:
				return "!=";
			case LESS_THAN:
				return "<";
			case LESS_OR_EQUAL:
				return "<=";
			case GREATER_THAN:
				return ">";
			case GREATER_OR_EQUAL:
				return ">=";
			default:
				throw new IllegalArgumentException();
		}
	}

	static
	private String format(CompoundPredicate compoundPredicate){
		StringBuffer sb = new StringBuffer();

		List<Predicate> predicates = compoundPredicate.getPredicates();

		sb.append('(').append(format(predicates.get(0))).append(')');

		for(Predicate predicate : predicates.subList(1, predicates.size())){
			sb.append(' ').append(format(compoundPredicate.getBooleanOperator())).append(' ');
			sb.append('(').append(format(predicate)).append(')');
		}

		return sb.toString();
	}

	static
	private String format(CompoundPredicate.BooleanOperator operator){

		switch(operator){
			case AND:
				return "&";
			case OR:
				return "|";
			case XOR:
				return "^";
			default:
				throw new IllegalArgumentException();
		}
	}
}