package com.jsonsync;

import com.jsonsync.JSONSyncRuntimeException;
import com.jsonsync.utils.json.JSONArray;
import com.jsonsync.utils.json.JSONException;
import com.jsonsync.utils.json.JSONObject;

/**
 *
 * @author Arnaud CAVAILHEZ
 */
class JSONUtils {

    public static int indexOfObjectInJSONArray(Object o, JSONArray array) {
        for (int i = 0; i < array.length(); i++) {
            try {
                if (array.get(i).equals(o)) {
                    return i;
                }
            } catch (JSONException ex) {
                throw new JSONSyncRuntimeException("#indexOfObjectInJSONArray failed ", ex);
            }
        }
        return -1;
    }

    public static JSONArray copyJSONArray(JSONArray array) {
        try {
            return new JSONArray(array.toString());
        } catch (JSONException ex) {
            throw new JSONSyncRuntimeException("Could not copy JSON", ex);
        }
    }

    public static JSONObject copyJSON(JSONObject json) {
        try {
            return new JSONObject(json.toString());
        } catch (JSONException ex) {
            throw new JSONSyncRuntimeException("Could not copy JSON", ex);
        }
    }
}
