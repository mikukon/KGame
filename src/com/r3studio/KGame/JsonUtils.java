package com.r3studio.KGame;

import java.lang.reflect.Type;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;

/**
 * @author zgwong
 */
public class JsonUtils {
	private static Gson sGson;

	static {
		sGson = new Gson();
	}

	private JsonUtils() {
	}

	public static String toJson(Object obj) {
		return getGson().toJson(obj);
	}

	public static String toJsonDateSerializer(Object obj, final String dateformat) {
		Gson gson = new GsonBuilder().registerTypeHierarchyAdapter(Date.class, new JsonSerializer<Date>() {
			
			@Override
			public JsonElement serialize(Date src, Type typeOfSrc, JsonSerializationContext context) {
				SimpleDateFormat format = new SimpleDateFormat(dateformat);
				return new JsonPrimitive(format.format(src));
			}
		}).setDateFormat(dateformat).create();
		return gson.toJson(obj);
	}
	
	public static <T> T toObject(String json, Class<T> clazz) {
		return getGson().fromJson(json, clazz);
	}
	
	public static <T> T toObjectDateSerializer(String json, Class<T> clazz, final String dateformat) {
		Gson gson = new GsonBuilder().registerTypeAdapter(Date.class, new JsonDeserializer<Date>() {
			@Override
			public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
				SimpleDateFormat format = new SimpleDateFormat(dateformat);
				String date = json.getAsString();
				try {
					return format.parse(date);
				} catch (ParseException e) {
					e.printStackTrace();
				}
				return null;
			}
		}).setDateFormat(dateformat).create();
		return (T) gson.fromJson(json, clazz);
	}
	
	public static Map<?, ?> toMap(String json) {
		return getGson().fromJson(json, new TypeToken<Map<?, ?>>() {}.getType());
	}
	
	public static Object getJsonValue(String json, String key) {
		Object rulsObj = null;
		Map<?, ?> rulsMap = toMap(json);
		if (rulsMap != null && rulsMap.size() > 0) {
			rulsObj = rulsMap.get(key);
		}
		return rulsObj;
	}
	
	public static Gson getGson() {
		if (sGson == null) {
			sGson = new Gson();
		}
		return sGson;
	}
	
	public static Gson getFilterGson(String []options){
		ExclusionStrategy excludeStrategy = new SetterExclusionStrategy(options);
		Gson sGson = new GsonBuilder().setExclusionStrategies(excludeStrategy)
				.create();
		return sGson;
	}
	
	/**
	 * 过滤帮助类
	 */
	private static class SetterExclusionStrategy implements ExclusionStrategy {
		private String[] fields;

		public SetterExclusionStrategy(String[] fields) {
			this.fields = fields;

		}

		@Override
		public boolean shouldSkipClass(Class<?> arg0) {
			return false;
		}

		/**
		 * 过滤字段的方法
		 */
		@Override
		public boolean shouldSkipField(FieldAttributes f) {
			if (fields != null) {
				for (String name : fields) {
					if (f.getName().equals(name)) {
						/** true 代表此字段要过滤 */
						return true;
					}
				}
			}
			return false;
		}

	}

}
