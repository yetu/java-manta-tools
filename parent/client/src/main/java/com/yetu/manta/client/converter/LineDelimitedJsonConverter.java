package com.yetu.manta.client.converter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import retrofit.converter.ConversionException;
import retrofit.converter.Converter;
import retrofit.mime.TypedByteArray;
import retrofit.mime.TypedInput;
import retrofit.mime.TypedOutput;

/**
 * This Converter should be able to parse line delimited JSON objects.
 * 
 * @author till
 *
 */
public class LineDelimitedJsonConverter implements Converter {

	private static final String MIME_TYPE = "application/json; charset=UTF-8";

	private ObjectMapper mapper;

	public LineDelimitedJsonConverter(ObjectMapper mapper) {
		this.mapper = mapper;
	}

	public LineDelimitedJsonConverter() {
		this(new ObjectMapper());
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Object fromBody(TypedInput body, Type type)
			throws ConversionException {
		try {
			// Check if we have a parametrized type like List<MantaObject>
			if (type instanceof ParameterizedType) {
				ParameterizedType paramType = (ParameterizedType) type;
				Class<?> rawType = (Class<?>) paramType.getRawType();
				Type[] args = paramType.getActualTypeArguments();
				// If the raw type has the super interface collection we
				// probably want
				// to parse line delimited input
				if (Collection.class.isAssignableFrom(rawType)) {
					if (args.length != 1) {
						throw new ConversionException(
								"Unusual amount of type arguments for collection type");
					}
					Collection resultList = createCollectionObject(rawType);
					Type listType = args[0];
					InputStream is = body.in();
					BufferedReader reader = new BufferedReader(
							new InputStreamReader(is));
					String line = "";
					while ((line = reader.readLine()) != null) {
						JavaType javaType = mapper.getTypeFactory()
								.constructType(listType);
						Object value = mapper.readValue(line, javaType);
						resultList.add(value);
					}
					return resultList;
				}
			}
			// We probably don't have a line delimited response
			return defaultFromBody(body, type);
		} catch (JsonProcessingException e) {
			// If we weren't able to read line delimited JSON objects, try it
			// the old
			// fashioned way
			return defaultFromBody(body, type);
		} catch (IOException e) {
			throw new ConversionException("Can't read from body", e);
		}
	}

	@SuppressWarnings("rawtypes")
	private Collection<?> createCollectionObject(Class<?> clazz) {
		try {
			if (clazz.getConstructor(new Class<?>[] {}) != null) {
				return (Collection<?>) clazz.newInstance();
			}
		} catch (InstantiationException | IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return new ArrayList();
	}

	private Object defaultFromBody(TypedInput body, Type type)
			throws ConversionException {
		try {
			JavaType javaType = mapper.getTypeFactory().constructType(type);
			return mapper.readValue(body.in(), javaType);
		} catch (JsonParseException e) {
			throw new ConversionException(e);
		} catch (JsonMappingException e) {
			throw new ConversionException(e);
		} catch (IOException e) {
			throw new ConversionException(e);
		}
	}

	@Override
	public TypedOutput toBody(Object object) {
		try {
			String json = mapper.writeValueAsString(object);
			return new TypedByteArray(MIME_TYPE, json.getBytes("UTF-8"));
		} catch (JsonProcessingException e) {
			throw new AssertionError(e);
		} catch (UnsupportedEncodingException e) {
			throw new AssertionError(e);
		}
	}

}
