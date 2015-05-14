package com.yetu.manta.client.converter;

import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import retrofit.mime.TypedFile;

import com.yetu.manta.client.representations.MantaObject;

public class LineDelimitedConverterTest {

	private LineDelimitedJsonConverter converter;

	@Before
	public void createConverter() {
		converter = new LineDelimitedJsonConverter();
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testReadingLineDelimitedInput() throws Exception {
		TypedFile input = new TypedFile("application/json", new File(
				"src/test/resources/list.response"));

		Type type = getListResponseType();

		ArrayList<MantaObject> readObjects = (ArrayList<MantaObject>) converter
				.fromBody(input, type);
		Assert.assertEquals(8, readObjects.size());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testParsingWithSingleItemList() throws Exception {
		TypedFile input = new TypedFile("application/json", new File(
				"src/test/resources/single_list_item.response"));

		ArrayList<MantaObject> readObjects = (ArrayList<MantaObject>) converter
				.fromBody(input, getListResponseType());
		Assert.assertEquals(1, readObjects.size());

	}

	private Type getListResponseType() {
		// Ok, this is a rather bizarre workaorund. Normally the RestHandler
		// would
		// discover the return type of the rest interface method and pass it as
		// the Type
		// to the converter. But for this test we need also a Type wich is the
		// ParametrizedType
		// of the ArrayList. Since we only have getGenericSuperclass to retrieve
		// this Type
		// we subclass ArrayList<MantaOject> so getGenericSuperclass returns the
		// ParametrizedType of ArrayList<MantaObject>
		ArrayList<MantaObject> mantaList = new ArrayList<MantaObject>() {

			/**
					 * 
					 */
			private static final long serialVersionUID = -5269024697742652415L;
		};
		Type type = mantaList.getClass().getGenericSuperclass();
		return type;
	}
}
