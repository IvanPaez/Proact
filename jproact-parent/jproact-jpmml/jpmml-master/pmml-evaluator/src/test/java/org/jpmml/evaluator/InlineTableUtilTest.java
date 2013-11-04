/*
 * Copyright (c) 2013 University of Tartu
 */
package org.jpmml.evaluator;

import java.util.*;

import org.junit.*;

import com.google.common.collect.*;

import static org.junit.Assert.*;

public class InlineTableUtilTest {

	@Test
	public void matchSingleColumn(){
		Map<String, String> first = createRow(new String[][]{{"value", "1"}, {"output", "first"}});
		Map<String, String> second = createRow(new String[][]{{"value", "2"}, {"output", "second"}});
		Map<String, String> third = createRow(new String[][]{{"value", "3"}, {"output", "third"}});

		Table<Integer, String, String> table = createTable(first, second, third);

		assertEquals(first, InlineTableUtil.match(table, createValues(new Object[][]{{"value", "1"}})));
		assertEquals(second, InlineTableUtil.match(table, createValues(new Object[][]{{"value", 2}})));
		assertEquals(third, InlineTableUtil.match(table, createValues(new Object[][]{{"value", 3d}})));

		assertEquals(null, InlineTableUtil.match(table, createValues(new Object[][]{{"value", "false"}})));
	}

	@Test
	public void matchMultipleColumns(){
		Map<String, String> firstTrue = createRow(new String[][]{{"value", "1"}, {"flag", "true"}, {"output", "firstTrue"}});
		Map<String, String> firstFalse = createRow(new String[][]{{"value", "1"}, {"flag", "false"}, {"output", "firstFalse"}});
		Map<String, String> secondTrue = createRow(new String[][]{{"value", "2"}, {"flag", "true"}, {"output", "secondTrue"}});
		Map<String, String> secondFalse = createRow(new String[][]{{"value", "2"}, {"flag", "false"}, {"output", "secondFalse"}});
		Map<String, String> thirdTrue = createRow(new String[][]{{"value", "3"}, {"flag", "true"}, {"output", "thirdTrue"}});
		Map<String, String> thirdFalse = createRow(new String[][]{{"value", "3"}, {"flag", "false"}, {"output", "thirdFalse"}});

		Table<Integer, String, String> table = createTable(firstTrue, firstFalse, secondTrue, secondFalse, thirdTrue, thirdFalse);

		assertEquals(null, InlineTableUtil.match(table, createValues(new Object[][]{{"value", "1"}})));

		assertEquals(firstTrue, InlineTableUtil.match(table, createValues(new Object[][]{{"value", "1"}, {"flag", "true"}})));
		assertEquals(firstFalse, InlineTableUtil.match(table, createValues(new Object[][]{{"value", "1"}, {"flag", false}})));

		assertEquals(secondTrue, InlineTableUtil.match(table, createValues(new Object[][]{{"value", 2}, {"flag", "true"}})));
		assertEquals(secondFalse, InlineTableUtil.match(table, createValues(new Object[][]{{"value", 2}, {"flag", false}})));

		assertEquals(thirdTrue, InlineTableUtil.match(table, createValues(new Object[][]{{"value", 3d}, {"flag", "true"}})));
		assertEquals(thirdFalse, InlineTableUtil.match(table, createValues(new Object[][]{{"value", 3d}, {"flag", false}})));
	}

	static
	private Map<String, String> createRow(String[][] strings){
		Map<String, String> result = Maps.newLinkedHashMap();

		for(int i = 0; i < strings.length; i++){
			result.put(strings[i][0], strings[i][1]);
		}

		return result;
	}

	static
	private Table<Integer, String, String> createTable(Map<String, String>... rows){
		Table<Integer, String, String> result = TreeBasedTable.create();

		for(int i = 0; i < rows.length; i++){
			Map<String, String> row = rows[i];

			Integer rowKey = Integer.valueOf(i + 1);

			Collection<Map.Entry<String, String>> entries = row.entrySet();
			for(Map.Entry<String, String> entry : entries){
				result.put(rowKey, entry.getKey(), entry.getValue());
			}
		}

		return result;
	}

	static
	private Map<String, FieldValue> createValues(Object[][] objects){
		Map<String, FieldValue> result = Maps.newLinkedHashMap();

		for(int i = 0; i < objects.length; i++){
			result.put((String)objects[i][0], FieldValueUtil.create(objects[i][1]));
		}

		return result;
	}
}