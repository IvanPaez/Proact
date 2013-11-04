/*
 * Copyright (c) 2013 Université de Rennes 1, France
 * Author: Ivan Paez ivan.paez_anaya@irisa.fr
 *
 */
package org.jpmml.example;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.dmg.pmml.*;

import com.beust.jcommander.Parameter;
import org.jpmml.evaluator.TreeModelEvaluator;

public class FirePotentialExample extends Example {


    @Parameter (
            names = {"--model"},
            description = "The PMML file"
    )
    private File model = null;

	static
	public void main(String... args) throws Exception {
		execute(FirePotentialExample.class, args);
	}

	@Override
	public void execute() throws Exception {

        PMML pmml = createFirePotentialModel();

        System.out.println("This is the Fire Potential PMML model...");

        if(this.model != null){
            IOUtil.marshal(pmml, this.model);
        }

        TreeModelEvaluator treeModelEvaluator = new TreeModelEvaluator(pmml);

        Map<FieldName, ?> arguments = EvaluationExample.readArguments(treeModelEvaluator);

        Map<FieldName, ?> result = treeModelEvaluator.evaluate(arguments);

        EvaluationExample.writeResult(treeModelEvaluator, result);
	}

    static private PMML createFirePotentialModel(){
        Header header = new Header()
                .withCopyright("www.inria.fr")
                .withDescription("A very small binary tree model to show significant wildland fire potential.");

        PMML pmml = new PMML(header, new DataDictionary(), "4.1");



        Node n1 = createNode("1", new True(), "normal");

        TreeModel treeModel = new TreeModel(new MiningSchema(), n1, MiningFunctionType.CLASSIFICATION);
        treeModel.withModelName("firePotential");

        pmml.withModels(treeModel);

        FieldName temperature = FieldName.create("temperature");
        declareField(pmml, treeModel, temperature, FieldUsageType.ACTIVE, null);

        FieldName humidity = FieldName.create("humidity");
        declareField(pmml, treeModel, humidity, FieldUsageType.ACTIVE, null);

        FieldName windy = FieldName.create("windy");
        declareField(pmml, treeModel, windy, FieldUsageType.ACTIVE, createValues("true", "false"));

        FieldName outlook = FieldName.create("outlook");
        declareField(pmml, treeModel, outlook, FieldUsageType.ACTIVE, createValues("sunny", "cloudy", "rain"));

        FieldName outcome = FieldName.create("outcome");
        declareField(pmml, treeModel, outcome, FieldUsageType.PREDICTED, createValues("above-normal", "normal", "below-normal"));

        DataDictionary dataDictionary = pmml.getDataDictionary();
        dataDictionary.withNumberOfFields(5);

        //
        // Upper half of the tree
        //

        Predicate n2Predicate = createSimplePredicate(outlook, SimplePredicate.Operator.EQUAL, "sunny");

        Node n2 = createNode("2", n2Predicate, "normal");
        n1.withNodes(n2);

        Predicate n3Predicate = createCompoundPredicate(CompoundPredicate.BooleanOperator.SURROGATE,
                createSimplePredicate(temperature, SimplePredicate.Operator.LESS_THAN, "90"),     //32 C
                createSimplePredicate(temperature, SimplePredicate.Operator.GREATER_THAN, "50")   // 10 C
        );

        Node n3 = createNode("3", n3Predicate, "normal");
        n2.withNodes(n3);

        Predicate n4Predicate = createCompoundPredicate(CompoundPredicate.BooleanOperator.AND,
                                createSimplePredicate(humidity, SimplePredicate.Operator.LESS_OR_EQUAL,"30"),
                                createSimplePredicate(windy, SimplePredicate.Operator.EQUAL, "true")
                );

        Node n4 = createNode("4", n4Predicate, "above-normal");
        n3.withNodes(n4);

        Predicate n5Predicate = createSimplePredicate(humidity, SimplePredicate.Operator.GREATER_THAN, "30");

        Node n5 = createNode("5", n5Predicate, "normal");
        n3.withNodes(n5);


        Predicate n6Predicate = createCompoundPredicate(CompoundPredicate.BooleanOperator.OR,
                createSimplePredicate(temperature, SimplePredicate.Operator.GREATER_OR_EQUAL, "90"),
                createSimplePredicate(temperature, SimplePredicate.Operator.LESS_OR_EQUAL, "50")
        );

        Node n6 = createNode("6", n6Predicate, "normal");
        n2.withNodes(n6);


        Predicate n7Predicate = createCompoundPredicate(CompoundPredicate.BooleanOperator.AND,
                createSimplePredicate(temperature, SimplePredicate.Operator.GREATER_OR_EQUAL, "90"),
                createSimplePredicate(humidity, SimplePredicate.Operator.LESS_OR_EQUAL, "30"),
                createSimplePredicate(windy, SimplePredicate.Operator.EQUAL, "true")
        );

        Node n7 = createNode("7", n7Predicate, "above-normal");
        n6.withNodes(n7);

        Predicate n8Predicate = createCompoundPredicate(CompoundPredicate.BooleanOperator.AND,
                createSimplePredicate(temperature, SimplePredicate.Operator.LESS_OR_EQUAL, "50"),
                createSimplePredicate(humidity, SimplePredicate.Operator.GREATER_THAN, "30")
        );

        Node n8 = createNode("8", n8Predicate, "below-normal");
        n6.withNodes(n8);

        //
        // Lower half of the tree
        //

        Predicate n9Predicate = createCompoundPredicate(CompoundPredicate.BooleanOperator.OR,
                createSimplePredicate(outlook, SimplePredicate.Operator.EQUAL, "cloudy"),
                createSimplePredicate(outlook, SimplePredicate.Operator.EQUAL, "rain")
        );

        Node n9 = createNode("9", n9Predicate, "normal");
        n1.withNodes(n9);

        Predicate n10Predicate = createCompoundPredicate(CompoundPredicate.BooleanOperator.AND,
                createSimplePredicate(temperature, SimplePredicate.Operator.GREATER_THAN, "50"),
                createSimplePredicate(temperature, SimplePredicate.Operator.LESS_THAN, "90"),
                createSimplePredicate(outlook, SimplePredicate.Operator.EQUAL, "cloudy"),
                createSimplePredicate(humidity, SimplePredicate.Operator.GREATER_THAN, "30"),
                createSimplePredicate(windy, SimplePredicate.Operator.EQUAL, "false")
        );

        Node n10 = createNode("10", n10Predicate, "normal");
        n9.withNodes(n10);

        Predicate n11Predicate = createCompoundPredicate(CompoundPredicate.BooleanOperator.OR,
                createSimplePredicate(temperature, SimplePredicate.Operator.LESS_OR_EQUAL, "50"),
                createSimplePredicate(outlook, SimplePredicate.Operator.EQUAL, "rain"),
                createSimplePredicate(humidity, SimplePredicate.Operator.GREATER_THAN, "65"),
                createSimplePredicate(windy, SimplePredicate.Operator.EQUAL, "true")
        );

        Node n11 = createNode("11", n11Predicate, "below-normal");
        n9.withNodes(n11);

        return pmml;

    }


    static private void declareField(PMML pmml, Model model, FieldName name, FieldUsageType usage, List<Value> values){
        OpType opType = (values != null ? OpType.CATEGORICAL : OpType.CONTINUOUS);
        DataType dataType = (values != null ? DataType.STRING : DataType.DOUBLE);

        DataField dataField = new DataField(name, opType, dataType)
                .withValues(values);

        DataDictionary dataDictionary = pmml.getDataDictionary();
        dataDictionary.withDataFields(dataField);

        MiningField miningField = new MiningField(name)
                .withUsageType((FieldUsageType.ACTIVE).equals(usage) ? null : usage);

        MiningSchema miningSchema = model.getMiningSchema();
        miningSchema.withMiningFields(miningField);
    }

    static private List<Value> createValues(String... strings){
        List<Value> values = new ArrayList<Value>();

        for(String string : strings){
            values.add(new Value(string));
        }

        return values;
    }

    static private Node createNode(String id, Predicate predicate, String score){
        return new Node()
                .withId(id)
                .withPredicate(predicate)
                .withScore(score);
    }

    static private SimplePredicate createSimplePredicate(FieldName name, SimplePredicate.Operator operator, String value){
        return new SimplePredicate(name, operator)
                .withValue(value);
    }

    static private CompoundPredicate createCompoundPredicate(CompoundPredicate.BooleanOperator operator, Predicate... predicates){
        return new CompoundPredicate(operator)
                .withPredicates(predicates);
    }

}