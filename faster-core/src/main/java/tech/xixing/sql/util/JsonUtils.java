package tech.xixing.sql.util;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import tech.xixing.sql.constants.SqlConstants;

import java.util.Optional;

/**
 * Utility class for JSON operations and validation
 * 
 * @author liuzhifei
 * @since 0.2
 */
@Slf4j
public final class JsonUtils {
    
    private JsonUtils() {
        // Utility class - prevent instantiation
    }
    
    /**
     * Safely parse a JSON string into JSONArray
     * 
     * @param jsonString the JSON string to parse
     * @return Optional containing JSONArray if parsing is successful
     */
    public static Optional<JSONArray> parseJsonArray(String jsonString) {
        if (jsonString == null || jsonString.trim().isEmpty()) {
            log.warn("Attempted to parse null or empty JSON string");
            return Optional.empty();
        }
        
        try {
            JSONArray jsonArray = JSON.parseArray(jsonString);
            if (jsonArray == null) {
                log.warn("JSON parsing resulted in null array");
                return Optional.empty();
            }
            return Optional.of(jsonArray);
        } catch (JSONException e) {
            log.error("Failed to parse JSON array: {}", e.getMessage());
            return Optional.empty();
        }
    }
    
    /**
     * Safely parse a JSON string into JSONObject
     * 
     * @param jsonString the JSON string to parse
     * @return Optional containing JSONObject if parsing is successful
     */
    public static Optional<JSONObject> parseJsonObject(String jsonString) {
        if (jsonString == null || jsonString.trim().isEmpty()) {
            log.warn("Attempted to parse null or empty JSON string");
            return Optional.empty();
        }
        
        try {
            JSONObject jsonObject = JSON.parseObject(jsonString);
            if (jsonObject == null) {
                log.warn("JSON parsing resulted in null object");
                return Optional.empty();
            }
            return Optional.of(jsonObject);
        } catch (JSONException e) {
            log.error("Failed to parse JSON object: {}", e.getMessage());
            return Optional.empty();
        }
    }
    
    /**
     * Validate if a string is valid JSON
     * 
     * @param jsonString the JSON string to validate
     * @return true if valid JSON, false otherwise
     */
    public static boolean isValidJson(String jsonString) {
        if (jsonString == null || jsonString.trim().isEmpty()) {
            return false;
        }
        
        try {
            JSON.parse(jsonString);
            return true;
        } catch (JSONException e) {
            return false;
        }
    }
    
    /**
     * Validate if a JSONArray is not null and not empty
     * 
     * @param jsonArray the JSONArray to validate
     * @return true if valid and not empty
     */
    public static boolean isValidAndNotEmpty(JSONArray jsonArray) {
        return jsonArray != null && !jsonArray.isEmpty();
    }
    
    /**
     * Validate if a JSONObject is not null and not empty
     * 
     * @param jsonObject the JSONObject to validate
     * @return true if valid and not empty
     */
    public static boolean isValidAndNotEmpty(JSONObject jsonObject) {
        return jsonObject != null && !jsonObject.isEmpty();
    }
    
    /**
     * Get a safe string representation of JSON for logging
     * 
     * @param jsonString the JSON string
     * @return truncated string for logging
     */
    public static String getSafeLogString(String jsonString) {
        if (jsonString == null) {
            return "null";
        }
        
        if (jsonString.length() <= SqlConstants.MAX_JSON_LOG_LENGTH) {
            return jsonString;
        }
        
        return jsonString.substring(0, SqlConstants.MAX_JSON_LOG_LENGTH) + SqlConstants.JSON_TRUNCATED_SUFFIX;
    }
}
