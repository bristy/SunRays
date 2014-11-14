package weather.sunrays.com.sunrays;

import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);

            // Create some dummy data to be shown in list view
            String[] dummyForcastData = new String[]{
                    "Today - sunny - 88/63",
                    "Tomorrow - foggy - 70/40",
                    "Weds - Cloudy - 72/73",
                    "Thursday - Asteroid 70/75",
                    "Friday - Heavy Rain 65-56",
                    "Saturday - HELP TRAPPED IN WEATHER STATION 70/68",
                    "Sunday - sunny - 86/70"
            };
            List<String> fakeData = new ArrayList<String>(Arrays.asList(dummyForcastData));
            ArrayAdapter<String> forcastAdapter = new ArrayAdapter<String>(
                    // Context
                    getActivity(),
                    // list view item layout
                    R.layout.list_item_forecast,
                    // textview id
                    R.id.list_item_forecast_textview,
                    // data
                    fakeData);
            ListView weatherListView = (ListView) rootView.findViewById(R.id.list_view_forecast);
            weatherListView.setAdapter(forcastAdapter);
            return rootView;
        }
    }
}
