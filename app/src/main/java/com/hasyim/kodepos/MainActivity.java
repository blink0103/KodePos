package com.hasyim.kodepos;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {
    private String TAG = MainActivity.class.getSimpleName();
    private ProgressDialog pDialog;
    private ListView lv;

    // URL to get propinsi list JSON
    final static String url = "https://kodepos-2d475.firebaseio.com/list_propinsi.json";

    ArrayList<HashMap<String, String>> posList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //header
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.drawable.ico_kecil);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        //judul
        getSupportActionBar().setTitle("  Cari Kode Pos");
        getSupportActionBar().setSubtitle("  API dari kodepos.firebaseio.com"); //tampil subtitle

        // menampung data propinsi
        posList = new ArrayList<>();

        // listview untuk menampilkan daftar propinsi
        lv = (ListView) findViewById(R.id.list_pos);
        lv.setOnItemClickListener(this);

        // Get JSON data
        new GetPost().execute();
    }

    // Async task class to get json by making HTTP call
    private class GetPost extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage("Mohon Ditunggu...");
            pDialog.setCancelable(false);
            pDialog.show();

        }

        @Override
        protected Void doInBackground(Void... arg0) {
            HttpHandler sh = new HttpHandler();

            // Making a request to url and getting response
            String jsonStr = sh.makeServiceCall(url);

            Log.e(TAG, "Response from url: " + jsonStr);

            if (jsonStr != null) {
                try {
                    JSONObject jsonObj = new JSONObject(jsonStr);

                    // Parser flat object
                    Iterator<String> iter = jsonObj.keys();
                    while (iter.hasNext()) {
                        String key = iter.next();
                        try {
                            Object value = jsonObj.get(key);
                            String nama = value.toString();

                            // tmp hash map for single GetPost
                            HashMap<String, String> pos = new HashMap<>();

                            // adding each child node to HashMap key => value
                            pos.put("name", nama);
                            pos.put("idp", key);

                            // adding pos to pos list
                            posList.add(pos);

                        } catch (JSONException e) {
                            // Something went wrong!
                        }
                    }
                } catch (final JSONException e) {
                    Log.e(TAG, "Json parsing error: " + e.getMessage());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(),
                                    "Data tidak ada.",
                                    Toast.LENGTH_LONG)
                                    .show();
                        }
                    });

                }
            } else {
                Log.e(TAG, "Couldn't get json from server.");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(),
                                "Terjadi kesalahan coba lain waktu.",
                                Toast.LENGTH_LONG)
                                .show();
                    }
                });

            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            // Dismiss the progress dialog
            if (pDialog.isShowing())
                pDialog.dismiss();

            // Sorting a-z
            Collections.sort(posList, new Comparator<HashMap< String,String >>() {

                @Override
                public int compare(HashMap<String, String> lhs,
                                   HashMap<String, String> rhs) {
                    return lhs.get("name").compareTo(rhs.get("name"));
                }
            });

            // Updating parsed JSON data into ListView
            ListAdapter adapter = new SimpleAdapter(
                    MainActivity.this, posList,
                    R.layout.list_daerah, new String[]{"name"}, new int[]{R.id.name});

            lv.setAdapter(adapter);
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
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Info Aplikasi");
            builder.setCancelable(false);
            builder.setMessage(getResources().getString(R.string.info));
            builder.setPositiveButton("OK", null);
            builder.setNegativeButton("Cancel", null);
            builder.show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        // mengambil idp & name dari list yang kita click
        String idp = posList.get(position).get("idp");
        String name = posList.get(position).get("name");

        // mengirim parameter idp & name ke PropinsiActivity
        Intent intent = new Intent(MainActivity.this, PropinsiActivity.class);
        intent.putExtra("idp", posList.get(position).get("idp"));
        intent.putExtra("name", posList.get(position).get("name"));
        startActivity(intent);
    }
}
