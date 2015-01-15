package weather.sunrays.com.fragment;

/**
 * Created by brajesh on 15/11/14.
 */

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.ActionProvider;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import weather.sunrays.com.helper.NetworkHelper;
import weather.sunrays.com.network.RestClient;
import weather.sunrays.com.sunrays.DetailActivity;
import weather.sunrays.com.sunrays.R;

/**
 * A forecast fragment containing a simple view.
 */
public class ForecastFragment extends Fragment {


    private static final String TAG = ForecastFragment.class.getSimpleName();

    private static final String BASE_URL = "http://api.openweathermap.org/data/2.5/forecast/daily?";
    private RestClient mRestClient = null;
    private String mUrl = BASE_URL + "q=122001&mode=json&units=metric&cnt=7";

    private ArrayAdapter<String> mForecastAdapter;
    private ListView mWeatherListView;


    public ForecastFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // set this view has own menu options
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        View rootView = inflater.inflate(R.layout.fragment_main, container, false);


        mForecastAdapter = new ArrayAdapter<String>(
                // Context
                getActivity(),
                // list view item layout
                R.layout.list_item_forecast,
                // textview id
                R.id.list_item_forecast_textview,
                // data
                new ArrayList<String>());
        ListView weatherListView = (ListView) rootView.findViewById(R.id.list_view_forecast);
        weatherListView.setAdapter(mForecastAdapter);
        weatherListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Toast.makeText(getActivity(),
                        mForecastAdapter.getItem(position),
                        Toast.LENGTH_SHORT)
                        .show();
                String forecast = mForecastAdapter.getItem(position);
                Intent detailIntent = new Intent(getActivity(), DetailActivity.class);
                detailIntent.putExtra(Intent.EXTRA_TEXT, forecast);
                startActivity(detailIntent);
            }
        });

        return rootView;
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_forecast_fragment, menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item click here
        // all parent action bar menu item click are handled in parent
        // activity
        int id = item.getItemId();
        switch (id) {
            case R.id.action_refresh:
                updateWeather();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }

    }


    private void updateWeather() {
        SharedPreferences preferences = PreferenceManager
                .getDefaultSharedPreferences(getActivity());
        String postalCode = preferences.getString(getString(R.string.pref_location_key),
                getString(R.string.pref_location_default));
        new WeatherFetchTask().execute(postalCode);
    }

    private class WeatherFetchTask extends AsyncTask<String, Void, String[]> {
        private final String TAG = WeatherFetchTask.class.getSimpleName();
        private static final boolean DEBUG = false;

        final String format = "json";
        final String unit = "metric";
        final int numOfDays = 7;

        final String QUERY_PARAM = "q";
        final String FORMAT_PARAM = "mode";
        final String UNIT_PARAM = "units";
        final String DAYS_PARAM = "cnt";

        @Override
        protected void onPostExecute(String[] forecastData) {
            super.onPostExecute(forecastData);
            if (forecastData == null) {
                // error occurred
                return;
            }
            mForecastAdapter.clear();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                mForecastAdapter.addAll(forecastData);
            } else {
                for (String str : forecastData) {
                    mForecastAdapter.add(str);
                }
            }
            // We dont have to call notifyDataSetChanged
            // Array Adapter do this for us
            // mForecastAdapter.notifyDataSetChanged();

        }

        @Override
        protected String[] doInBackground(String... params) {
            // if there is no postal code return
            if (params.length == 0) {
                return null;
            }

            Map<String, String> urlParam = new HashMap<String, String>();
            urlParam.put(QUERY_PARAM, params[0]);
            urlParam.put(FORMAT_PARAM, format);
            urlParam.put(UNIT_PARAM, unit);
            urlParam.put(DAYS_PARAM, Integer.toString(numOfDays));
            String url = NetworkHelper.makeUrl(BASE_URL, urlParam);
            if (DEBUG) {
                Log.v(TAG, url);
            }
            mRestClient = new RestClient(url, "GET");
            String forecastJsonDataStr = mRestClient.getJSONData();
            try {
                return getWeatherDataFromJson(forecastJsonDataStr, numOfDays);
            } catch (JSONException e) {
                Log.e(TAG, "ERROR:   " + e.getMessage());
                e.printStackTrace();
            }
            // this will occur only when there is error
            return null;
        }

        /* The date/time conversion code is going to be moved outside the asynctask later,
        * so for convenience we're breaking it out into its own method now.
        */
        private String getReadableDateString(long time) {
            // Because the API returns a unix timestamp (measured in seconds),
            // it must be converted to milliseconds in order to be converted to valid date.
            Date date = new Date(time * 1000);
            SimpleDateFormat format = new SimpleDateFormat("E, MMM d");
            return format.format(date).toString();
        }

        /**
         * Prepare the weather high/lows for presentation.
         */
        private String formatHighLows(double high, double low) {
            // For presentation, assume the user doesn't care about tenths of a degree.

            SharedPreferences preferences = PreferenceManager
                    .getDefaultSharedPreferences(getActivity());
            String unitType = preferences.getString(getString(R.string.pref_units_key),
                    getString(R.string.pref_units_metric));
            if (unitType.equals(getString(R.string.pref_units_imperial))) {
                high = (high * 1.8) + 32;
                low = (low * 1.8) + 32;
            } else if (!unitType.equals(getString(R.string.pref_units_metric))) {
                // this should not happen
                Log.d(TAG, "Unit type not found " + unitType);
            }

            long roundedHigh = Math.round(high);
            long roundedLow = Math.round(low);

            String highLowStr = roundedHigh + "/" + roundedLow;
            return highLowStr;
        }

        /**
         * Take the String representing the complete forecast in JSON Format and
         * pull out the data we need to construct the Strings needed for the wireframes.
         * <p/>
         * Fortunately parsing is easy:  constructor takes the JSON string and converts it
         * into an Object hierarchy for us.
         */
        private String[] getWeatherDataFromJson(String forecastJsonStr, int numDays)
                throws JSONException {

            // These are the names of the JSON objects that need to be extracted.
            final String OWM_LIST = "list";
            final String OWM_WEATHER = "weather";
            final String OWM_TEMPERATURE = "temp";
            final String OWM_MAX = "max";
            final String OWM_MIN = "min";
            final String OWM_DATETIME = "dt";
            final String OWM_DESCRIPTION = "main";

            JSONObject forecastJson = new JSONObject(forecastJsonStr);
            JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);

            String[] resultStrs = new String[numDays];
            for (int i = 0; i < weatherArray.length(); i++) {
                // For now, using the format "Day, description, hi/low"
                String day;
                String description;
                String highAndLow;

                // Get the JSON object representing the day
                JSONObject dayForecast = weatherArray.getJSONObject(i);

                // The date/time is returned as a long.  We need to convert that
                // into something human-readable, since most people won't read "1400356800" as
                // "this saturday".
                long dateTime = dayForecast.getLong(OWM_DATETIME);
                day = getReadableDateString(dateTime);

                // description is in a child array called "weather", which is 1 element long.
                JSONObject weatherObject = dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);
                description = weatherObject.getString(OWM_DESCRIPTION);

                // Temperatures are in a child object called "temp".  Try not to name variables
                // "temp" when working with temperature.  It confuses everybody.
                JSONObject temperatureObject = dayForecast.getJSONObject(OWM_TEMPERATURE);
                double high = temperatureObject.getDouble(OWM_MAX);
                double low = temperatureObject.getDouble(OWM_MIN);

                highAndLow = formatHighLows(high, low);
                resultStrs[i] = day + " - " + description + " - " + highAndLow;
            }
            if (DEBUG) {
                for (String dayForecast : resultStrs) {
                    Log.v(TAG, dayForecast);
                }
            }
            return resultStrs;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        updateWeather();
    }
}
