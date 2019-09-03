/**
 * Copyright 2018 The CloudEvents Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.cloudevents.json;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;

import io.cloudevents.Attributes;
import io.cloudevents.fun.DataMarshaller;
import io.cloudevents.fun.DataUnmarshaller;

import java.io.InputStream;
import java.time.ZonedDateTime;
import java.util.Map;

public final class Json {

    public static final ObjectMapper MAPPER = new ObjectMapper();

    static {
        // add Jackson datatype for ZonedDateTime
        MAPPER.registerModule(new Jdk8Module());

        final SimpleModule module = new SimpleModule();
        module.addSerializer(ZonedDateTime.class, new ZonedDateTimeSerializer());
        module.addDeserializer(ZonedDateTime.class, new ZonedDateTimeDeserializer());
        MAPPER.registerModule(module);
    }

    /**
     * Encode a POJO to JSON using the underlying Jackson mapper.
     *
     * @param obj a POJO
     * @return a String containing the JSON representation of the given POJO.
     * @throws IllegalStateException if a property cannot be encoded.
     */
    public static String encode(final Object obj) throws IllegalStateException {
        try {
            return MAPPER.writeValueAsString(obj);
        } catch (Exception e) {
        	e.printStackTrace();
            throw new IllegalStateException("Failed to encode as JSON: " + e.getMessage());
        }
    }
    
    public static <T> T fromInputStream(final InputStream inputStream,
    		Class<T> clazz) {
    	try {
            return MAPPER.readValue(inputStream, clazz);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to encode as JSON: " 
            			+ e.getMessage());
        }
    }
 
    /**
     * Decode a given JSON string to a POJO of the given class type.
     *
     * @param str the JSON string.
     * @param clazz the class to map to.
     * @param <T> the generic type.
     * @return an instance of T or {@code null} when {@code str} is an empty string or {@code null}
     * @throws IllegalStateException when there is a parsing or invalid mapping.
     */
    protected static <T> T decodeValue(final String str, final Class<T> clazz) throws IllegalStateException {
    	
    	if(null!= str && !"".equals(str.trim())) {
	        try {
	            return MAPPER.readValue(str.trim(), clazz);
	        } catch (Exception e) {
	            throw new IllegalStateException("Failed to decode: " + e.getMessage());
	        }
    	}
    	
    	return null;
    }

    /**
     * Decode a given JSON string to a POJO of the given type.
     *
     * @param str the JSON string.
     * @param type the type to map to.
     * @param <T> the generic type.
     * @return an instance of T or {@code null} when {@code str} is an empty string or {@code null}
     * @throws IllegalStateException when there is a parsing or invalid mapping.
     */
    public static <T> T decodeValue(final String str, final TypeReference<T> type) throws IllegalStateException {
    	if(null!= str && !"".equals(str.trim())) {
	        try {
	            return MAPPER.readValue(str.trim(), type);
	        } catch (Exception e) {
	            throw new IllegalStateException("Failed to decode: " + e.getMessage(), e);
	        }
    	}
    	return null;
    }
    
    /**
     * Creates a JSON Data Unmarshaller
     * @param <T> The 'data' type
     * @param <A> The attributes type
     * @param type The type of 'data'
     * @return A new instance of {@link DataUnmarshaller}
     */
    public static <T, A extends Attributes> DataUnmarshaller<String, T, A> 
    umarshaller(Class<T> type) {
    	return new DataUnmarshaller<String, T, A>() {
			@Override
			public T unmarshal(String payload, A attributes) {
				return Json.decodeValue(payload, type);
			}
		};
    }
    
    /**
     * Creates a JSON Data Marshaller
     * @param <T> The 'data' type
     * @return A new instance of {@link DataMarshaller}
     */
    public static <T> DataMarshaller<String, T> marshaller() {
    	return new DataMarshaller<String, T>() {
			@Override
			public String marshal(T data, Map<String, Object> headers) {
				return Json.encode(data);
			}
		};
    }

    private Json() {
        // no-op
    }
}
