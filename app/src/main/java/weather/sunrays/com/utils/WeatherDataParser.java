package weather.sunrays.com.utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by brajesh on 23/11/14.
 */
public class WeatherDataParser {

    /**
     *
     * @param weatherJsonStr json data for weather
     *                       by api call:
     *                       http://api.openweathermap.org/data/2.5/forecast/daily?id=524901&cnt=7&mode=json&unit=metric
     * @param dayIndex day 0-indexed
     * @return max temperature of the indexed day
     */
    public static double getMaxTemperatureOfDay(String weatherJsonStr, int dayIndex) throws JSONException {
        final String DAY_DATA_LIST = "list";
        final String DAY_DATA_TEMP = "temp";
        final String DAY_MAX_TEMP = "max";

        JSONObject weatherJson = new JSONObject(weatherJsonStr);
        JSONArray AllDaysWeatherData = weatherJson.getJSONArray(DAY_DATA_LIST);
        JSONObject dayData = AllDaysWeatherData.getJSONObject(dayIndex);
        JSONObject dayTemp = dayData.getJSONObject(DAY_DATA_TEMP);
        double maxTemp = dayTemp.getDouble(DAY_MAX_TEMP);
        return maxTemp;

    }
}
