/*******************************************************************************
 * Copyright (c) 2019-02-19 @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>.
 * All rights reserved.
 *
 * Contributors:
 *     <a href="mailto:iffiff1@gmail.com">Tyler Chen</a> - initial API and implementation.
 ******************************************************************************/
package org.hitchain.hit.util;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;

import java.lang.reflect.Type;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * JsonUtil
 *
 * @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>
 * @since 2019-02-19
 */
public class JsonHelper {
    /* gson use yyyy-MM-dd'T'HH:mm:ss.SSSXXX date format */
    public static final Gson GSON = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
            .registerTypeAdapter(Date.class, new DateTypeAdapter())
            .create();
    public static final Gson GSON_PRETTY = new GsonBuilder()
            .setPrettyPrinting()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
            .registerTypeAdapter(Date.class, new DateTypeAdapter())
            .create();

    private JsonHelper() {
    }

    /**
     * json string to object.
     *
     * @param clazz
     * @param json
     * @return
     * @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>
     * @since Jul 19, 2016
     */
    public static <T> T toObject(Class<T> clazz, CharSequence json) {
        return (T) GSON.fromJson(json.toString(), clazz);
    }

    /**
     * json string to List Object.
     *
     * @param clazz
     * @param json
     * @return
     * @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>
     * @since Jul 19, 2016
     */
    public static <T> List<T> toList(Class<T> clazz, CharSequence json) {
        return (List<T>) GSON.fromJson(json.toString(), new TypeToken<List<T>>() {
        }.getType());
    }

    /**
     * json string to list object by class name.
     *
     * @param className
     * @param json
     * @return
     * @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>
     * @since Jul 19, 2016
     */
    public static <T> List<T> toList(String className, CharSequence json) {
        try {
            return (List<T>) GSON.fromJson(json.toString(), new TypeToken<List<T>>() {
            }.getType());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<T>();
    }

    /**
     * json string to map.
     *
     * @param json
     * @return
     * @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>
     * @since Jul 19, 2016
     */
    public static Map toMap(String json) {
        LinkedHashMap fromJson = GSON.fromJson(json, LinkedHashMap.class);
        return fromJson;
    }

    /**
     * json string to list object.
     *
     * @param json
     * @return
     * @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>
     * @since Jul 19, 2016
     */
    public static List toList(String json) {
        Gson gson = JsonHelper.GSON;
        List fromJson = (List) gson.fromJson(json, ArrayList.class);
        return fromJson;
    }

    /**
     * json string to object list or map.
     *
     * @param json
     * @return
     * @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>
     * @since Jul 19, 2016
     */
    public static <T> T toJson(String json) {
        if (json != null && (json = json.trim()).startsWith("[")) {
            return (T) toList(json);
        } else {
            return (T) toMap(json);
        }
    }

    /**
     * object to pretty json string.
     *
     * @param obj
     * @return
     * @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>
     * @since Jul 19, 2016
     */
    public static String toJson(Object obj) {
        return GSON.toJson(obj);
    }

    /**
     * object to json string.
     *
     * @param obj
     * @return
     * @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>
     * @since Jul 19, 2016
     */
    public static String toPrettyJson(Object obj) {
        return GSON_PRETTY.toJson(obj);
    }

    public static class DateTypeAdapter implements JsonSerializer<Date>, JsonDeserializer<Date> {
        public JsonElement serialize(Date ts, Type t, JsonSerializationContext jsc) {
            String dfString = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(ts.getTime()));
            return new JsonPrimitive(dfString);
        }

        public Date deserialize(JsonElement json, Type t, JsonDeserializationContext jsc) throws JsonParseException {
            if (!(json instanceof JsonPrimitive)) {
                throw new JsonParseException("The date should be a string value");
            }

            String asString = json.getAsString();
            if (StringUtils.isBlank(asString)) {
                return null;
            }
            Date date = null;
            try {
                date = DateUtils.parseDate(asString, "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd", "yyyy/MM/dd HH:mm:ss",
                        "yyyy/MM/dd", "yyyy-MM-dd'T'HH:mm:ss.SSS", "yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
            } catch (ParseException e) {
                throw new JsonParseException(e);
            }
            if (t == Date.class) {
                return date;
            } else if (t == Timestamp.class) {
                return new Timestamp(date.getTime());
            } else if (t == java.sql.Date.class) {
                return new java.sql.Date(date.getTime());
            } else {
                throw new IllegalArgumentException(getClass() + " cannot deserialize to " + t);
            }
        }
    }
}
