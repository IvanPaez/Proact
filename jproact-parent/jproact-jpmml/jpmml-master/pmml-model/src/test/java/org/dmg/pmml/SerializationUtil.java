/*
 * Copyright (c) 2013 University of Tartu
 */
package org.dmg.pmml;

import java.io.*;

public class SerializationUtil {

	private SerializationUtil(){
	}

	@SuppressWarnings (
		value = {"unchecked"}
	)
	static
	public <V> V clone(V object) throws Exception {
		return (V)deserializeObject(serializeObject(object));
	}

	static
	public byte[] serializeObject(Object object) throws Exception {
		ByteArrayOutputStream os = new ByteArrayOutputStream();

		try {
			ObjectOutputStream oos = new ObjectOutputStream(os);

			try {
				oos.writeObject(object);
			} finally {
				oos.close();
			}
		} finally {
			os.close();
		}

		return os.toByteArray();
	}

	static
	public Object deserializeObject(byte[] bytes) throws Exception {
		ByteArrayInputStream is = new ByteArrayInputStream(bytes);

		try {
			ObjectInputStream ois = new ObjectInputStream(is);

			try {
				return ois.readObject();
			} finally {
				ois.close();
			}
		} finally {
			is.close();
		}
	}
}