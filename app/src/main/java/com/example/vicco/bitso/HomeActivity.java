package com.example.vicco.bitso;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Build;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import app.adapters.ListViewCompoundBalanceAdapter;
import app.adapters.RecyclerViewLedgerAdapater;
import app.adapters.ViewPagerAdapter;
import app.fragments.FragmentCard;
import app.fragments.FragmentChat;
import app.fragments.FragmentHome;
import app.fragments.FragmentUserActivity;
import connectivity.HttpHandler;
import models.AppBitsoOperation;
import models.BitsoBalance;
import models.BitsoTicker;
import models.CompoundBalanceElement;

import Utils.Utils;

public class HomeActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemClickListener {
    private final String TAG = HomeActivity.class.getSimpleName();

    public static List<AppBitsoOperation> slistElements;

    private List<CompoundBalanceElement> mBalanceListElements;
    private ListViewCompoundBalanceAdapter mListViewCompoundBalanceAdapter;
    public RecyclerViewLedgerAdapater mRecyclerViewLedgerAdapater;
    private ActionBarDrawerToggle mActionBarDrawerToggle;
    private ProgressDialog mProgressDialog;

    private CoordinatorLayout iCoordinatorLayout;
    private Toolbar iToolbar;
    private NavigationView iNavigationView;
    private DrawerLayout iBalanceDrawer;
    private ListView iBalancesList;
    private LinearLayout iProfileLinearLayout;
    private LinearLayout iConfigurationsLinearLayout;
    private ImageView iDotsMenu;
    private RecyclerView iRecyclerView;
    private TextView iToolBarCurrencyAmount;//toolbarCurrencyAmount
    private TextView iToolbarCurrencyAmountLbl;//

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Interface Elements
        {
            slistElements = new ArrayList<AppBitsoOperation>();

            iNavigationView = (NavigationView) findViewById(R.id.balanceRightPanelView);
            iCoordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinator_layout);
            iBalanceDrawer =
                    (DrawerLayout) findViewById(R.id.balanceRightPanelDrawer);
            iToolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(iToolbar);

            iBalancesList = (ListView) findViewById(R.id.balanceList);
            iProfileLinearLayout = (LinearLayout) findViewById(R.id.balance_profile);
            iConfigurationsLinearLayout = (LinearLayout) findViewById(R.id.balance_configuration);
            iDotsMenu = (ImageView) findViewById(R.id.dots_icon);
            iRecyclerView = (RecyclerView) findViewById(R.id.item_list);
            iToolBarCurrencyAmount = (TextView) findViewById(R.id.toolbarCurrencyAmount);
            iToolbarCurrencyAmountLbl = (TextView) findViewById(R.id.toolbarCurrencyAmountLbl);
        }

        // Member elements
        {
            mBalanceListElements =
                    new ArrayList<CompoundBalanceElement>();
            mListViewCompoundBalanceAdapter = new ListViewCompoundBalanceAdapter(
                    LayoutInflater.from(getApplicationContext()),
                    mBalanceListElements);
            mActionBarDrawerToggle = getActionBarDrawerToggle();
            mRecyclerViewLedgerAdapater = new RecyclerViewLedgerAdapater(slistElements, HomeActivity.this);
            mProgressDialog = new ProgressDialog(this);
        }

        // Interface and interactions
        {
            iBalancesList.setAdapter(mListViewCompoundBalanceAdapter);
            iRecyclerView.setAdapter(mRecyclerViewLedgerAdapater);
            iBalancesList.setOnItemClickListener(this);
            iProfileLinearLayout.setOnClickListener(this);
            iConfigurationsLinearLayout.setOnClickListener(this);
            iBalanceDrawer.setDrawerListener(mActionBarDrawerToggle);
            iDotsMenu.setOnClickListener(this);
        }

        // Processes
        {
            getLedgers();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_search:
                if (Utils.isNetworkAvailable(this)) {
                    new GetCompoundBalance().execute();
                } else {
                    Log.d(TAG, getResources().getString(R.string.no_internet_connection));
                }

                if (iBalanceDrawer.isDrawerOpen(GravityCompat.END)) {
                    iBalanceDrawer.closeDrawer(GravityCompat.END);
                } else {
                    iBalanceDrawer.openDrawer(GravityCompat.END);
                }

                return Boolean.TRUE;
            default:
                return Boolean.TRUE;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.balance_profile:
                Toast.makeText(this, getResources().getString(R.string.click_profile),
                        Toast.LENGTH_LONG).show();
                break;
            case R.id.balance_configuration:
                Toast.makeText(this, getResources().getString(R.string.click_configurations),
                        Toast.LENGTH_LONG).show();
                break;
            case R.id.dots_icon:
                if (Utils.isNetworkAvailable(this)) {
                    new GetCompoundBalance().execute();
                } else {
                    Log.d(TAG, getResources().getString(R.string.no_internet_connection));
                }

                if (iBalanceDrawer.isDrawerOpen(GravityCompat.END)) {
                    iBalanceDrawer.closeDrawer(GravityCompat.END);
                } else {
                    iBalanceDrawer.openDrawer(GravityCompat.END);
                }
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        CompoundBalanceElement element = mBalanceListElements.get(position);
        iToolBarCurrencyAmount.setText("$" + element.getTotal().toString());
        iToolBarCurrencyAmount.setTextColor(element.getColor());

        iToolbarCurrencyAmountLbl.setText(element.getCurrency());

        if (iBalanceDrawer.isDrawerOpen(GravityCompat.END)) {
            iBalanceDrawer.closeDrawer(GravityCompat.END);
        } else {
            iBalanceDrawer.openDrawer(GravityCompat.END);
        }

    }

    private ActionBarDrawerToggle getActionBarDrawerToggle() {
        return new ActionBarDrawerToggle(
                this, iBalanceDrawer, iToolbar, R.string.open_drower,
                R.string.close_drower) {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                super.onDrawerSlide(drawerView, slideOffset);

                // Ask coordinator to move menu width from rigth to left
                iCoordinatorLayout.setTranslationX(-(slideOffset * drawerView.getWidth()));
                iBalanceDrawer.bringChildToFront(drawerView);
                iBalanceDrawer.requestLayout();
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                Log.d(TAG, "Drawer opened");
                if (Utils.isNetworkAvailable(HomeActivity.this)) {
                    new GetCompoundBalance().execute();
                } else {
                    Log.d(TAG, getResources().getString(R.string.no_internet_connection));
                }
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                Log.d(TAG, "Drawer closed");
            }
        };
    }

    private void getLedgers() {
        new FillListAsyncTask().execute("/api/v3/ledger", "GET", "");
    }

    // Async tasks
    private class GetCompoundBalance extends AsyncTask<Void, Void, Void> {
        private List<CompoundBalanceElement> balanceListElements;
        private boolean validAPILevel = Boolean.FALSE;
        private String value = "";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            HttpHandler.initHttpHandler(HomeActivity.this);
            balanceListElements = new ArrayList<CompoundBalanceElement>();
        }

        @Override
        protected Void doInBackground(Void... strings) {
            // Get balance
            String balanceResponse = HttpHandler.makeServiceCall("/api/v3/balance/",
                    "GET", "", true);
            // Get ticker
            String tickerResponse =
                    HttpHandler.sendGet("https://api.bitso.com/v3/ticker/", "");
            if (!processCompoundBalance(balanceResponse, tickerResponse)) {
                String error = getString(R.string.no_compound_balance);
                Log.e(TAG, error);
                Toast.makeText(HomeActivity.this, error, Toast.LENGTH_LONG).show();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            // Update List
            mListViewCompoundBalanceAdapter.notifyDataSetChanged();

            TextView textView = (TextView) findViewById(R.id.toolbarCurrencyAmount);
            ;
            textView.setText(value);
        }

        private boolean processCompoundBalance(String stringBalance, String stringTicker) {
            BitsoBalance balance = null;
            BitsoTicker[] tickers = null;
            int totalCurrencyTickers = 0;

            if ((stringBalance != null) && (stringTicker != null)) {
                Log.d(TAG, stringBalance);
                Log.d(TAG, stringTicker);
                try {
                    // Process balance
                    JSONObject jsonBalance = new JSONObject(stringBalance);
                    balance = new BitsoBalance(jsonBalance);

                    // ProcessTicker
                    JSONObject jsonTicker = new JSONObject(stringTicker);
                    if (jsonTicker.has("success") && jsonTicker.has("payload")) {
                        JSONArray currencyTickers = jsonTicker.getJSONArray("payload");
                        totalCurrencyTickers = currencyTickers.length();
                        tickers = new BitsoTicker[totalCurrencyTickers];
                        for (int i = 0; i < totalCurrencyTickers; i++) {
                            tickers[i] = new BitsoTicker(currencyTickers.getJSONObject(i));
                        }
                    }

                    // Verification
                    if ((balance == null) || (tickers == null)) {
                        return false;
                    }

                    // Start building compound balance
                    BigDecimal total = balance.mxnAvailable;
                    BigDecimal mxnAmount = total;
                    mxnAmount = mxnAmount.setScale(4, RoundingMode.DOWN);

                    balanceListElements.add(
                            new CompoundBalanceElement(
                                    getResources().getString(R.string.mxn_balance),
                                    mxnAmount, R.drawable.balance_divider_mxn,
                                    R.color.bitso_green));

                    for (int i = 0; i < totalCurrencyTickers; i++) {
                        BitsoTicker currentTicker = tickers[i];
                        BigDecimal currentLast = currentTicker.last;
                        String header = "";
                        BigDecimal currencyAmount = null;
                        int drawableElement = -1;
                        int color = -1;
                        switch (currentTicker.book) {
                            case BTC_MXN:
                                currencyAmount = balance.btcAvailable;
                                header = getResources().getString(R.string.btc_balance);
                                drawableElement = R.drawable.balance_divider_btc;
                                color = R.color.balance_btc;
                                break;
                            case ETH_MXN:
                                currencyAmount = balance.ethAvailable;
                                header = getResources().getString(R.string.eth_balance);
                                drawableElement = R.drawable.balance_divider_eth;
                                color = R.color.balance_eth;
                                break;
                            default:
                                break;
                        }

                        total = total.add(currencyAmount.multiply(currentLast));

                        currencyAmount = currencyAmount.setScale(4, RoundingMode.DOWN);

                        total = total.setScale(2, RoundingMode.DOWN);

                        value = "$" + total.toString();

                        balanceListElements.add(new CompoundBalanceElement(
                                header, currencyAmount, drawableElement, color));
                    }

                    balanceListElements.add(0, new CompoundBalanceElement(
                            getResources().getString(R.string.hdr_balances), total, -1, R.color.balance_amount));

                    mListViewCompoundBalanceAdapter.processList(balanceListElements);

                    return true;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            return false;
        }
    }

    private class FillListAsyncTask extends AsyncTask<String, Void, Void> {
        private List<CompoundBalanceElement> balanceListElements;
        private boolean validAPILevel = Boolean.FALSE;
        private String value = "";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressDialog.setMessage(getResources().getString(R.string.fetching_ledger));
            mProgressDialog.setCancelable(Boolean.TRUE);
            mProgressDialog.show();

            balanceListElements = new ArrayList<CompoundBalanceElement>();
        }

        @Override
        protected Void doInBackground(String... strings) {
            HttpHandler.initHttpHandler(HomeActivity.this);

            // Get ledger
            String jsonResponse = HttpHandler.makeServiceCall(strings[0],
                    strings[1], strings[2], true);
            if (jsonResponse != null) {
                Log.d(TAG, jsonResponse);
                try {
                    JSONObject jsonObject = new JSONObject(jsonResponse);
                    processResponse(jsonObject);
                } catch (final JSONException e) {
                    Log.e(TAG, "JSON Object parsing error: " + e.getMessage());
                    Toast.makeText(HomeActivity.this, "Json parsing error: " +
                            e.getMessage(), Toast.LENGTH_LONG).show();
                }
            } else {
                Log.e(TAG, "Couldn't get json from server.");
                Toast.makeText(HomeActivity.this, "Json parsing error: " +
                                "Couldn't get json from server. Check LogCat for possible errors!",
                        Toast.LENGTH_LONG).show();
            }

            // Get compound balance
            // Get balance
            String balanceResponse = HttpHandler.makeServiceCall("/api/v3/balance/",
                    "GET", "", true);
            // Get ticker
            String tickerResponse =
                    HttpHandler.sendGet("https://api.bitso.com/v3/ticker/", "");
            if (!processCompoundBalance(balanceResponse, tickerResponse)) {
                String error = getString(R.string.no_compound_balance);
                Log.e(TAG, error);
                Toast.makeText(HomeActivity.this, error, Toast.LENGTH_LONG).show();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            // Dismiss progress dialog
            if (mProgressDialog.isShowing()) {
                mProgressDialog.dismiss();
            }

            // Update List
            mRecyclerViewLedgerAdapater.notifyDataSetChanged();

            // Update compound list
            mListViewCompoundBalanceAdapter.notifyDataSetChanged();

            TextView textView = (TextView) findViewById(R.id.toolbarCurrencyAmount);
            ;
            textView.setText(value);

        }

        private void processResponse(JSONObject jsonObject) {
            if (jsonObject.has("payload")) {
                try {
                    JSONArray jsonArray = jsonObject.getJSONArray("payload");
                    int totalElements = jsonArray.length();
                    for (int i = 0; i < totalElements; i++) {
                        slistElements.add(new AppBitsoOperation(jsonArray.getJSONObject(i)));
                    }
                } catch (final JSONException e) {
                    Log.e(TAG, "JSON Object parsing error: " + e.getMessage());
                    Toast.makeText(HomeActivity.this, "Json parsing error: " +
                                    "Json parsing error: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                }
            } else {
                Log.e(TAG, "JSON does not contain payload key.");
                Toast.makeText(HomeActivity.this, "Json parsing error: " +
                        "Bad JSON response, not payload key found", Toast.LENGTH_LONG).show();
            }
        }

        private boolean processCompoundBalance(String stringBalance, String stringTicker) {
            BitsoBalance balance = null;
            BitsoTicker[] tickers = null;
            int totalCurrencyTickers = 0;

            if ((stringBalance != null) && (stringTicker != null)) {
                Log.d(TAG, stringBalance);
                Log.d(TAG, stringTicker);
                try {
                    // Process balance
                    JSONObject jsonBalance = new JSONObject(stringBalance);
                    balance = new BitsoBalance(jsonBalance);

                    // ProcessTicker
                    JSONObject jsonTicker = new JSONObject(stringTicker);
                    if (jsonTicker.has("success") && jsonTicker.has("payload")) {
                        JSONArray currencyTickers = jsonTicker.getJSONArray("payload");
                        totalCurrencyTickers = currencyTickers.length();
                        tickers = new BitsoTicker[totalCurrencyTickers];
                        for (int i = 0; i < totalCurrencyTickers; i++) {
                            tickers[i] = new BitsoTicker(currencyTickers.getJSONObject(i));
                        }
                    }

                    // Verification
                    if ((balance == null) || (tickers == null)) {
                        return false;
                    }

                    // Start building compound balance
                    BigDecimal total = balance.mxnAvailable;
                    BigDecimal mxnAmount = total;
                    mxnAmount = mxnAmount.setScale(4, RoundingMode.DOWN);

                    balanceListElements.add(
                            new CompoundBalanceElement(
                                    getResources().getString(R.string.mxn_balance),
                                    mxnAmount, R.drawable.balance_divider_mxn,
                                    R.color.bitso_green));

                    for (int i = 0; i < totalCurrencyTickers; i++) {
                        BitsoTicker currentTicker = tickers[i];
                        BigDecimal currentLast = currentTicker.last;
                        String header = "";
                        BigDecimal currencyAmount = null;
                        int drawableElement = -1;
                        int color = -1;
                        switch (currentTicker.book) {
                            case BTC_MXN:
                                currencyAmount = balance.btcAvailable;
                                header = getResources().getString(R.string.btc_balance);
                                drawableElement = R.drawable.balance_divider_btc;
                                color = R.color.balance_btc;
                                break;
                            case ETH_MXN:
                                currencyAmount = balance.ethAvailable;
                                header = getResources().getString(R.string.eth_balance);
                                drawableElement = R.drawable.balance_divider_eth;
                                color = R.color.balance_eth;
                                break;
                            default:
                                break;
                        }

                        total = total.add(currencyAmount.multiply(currentLast));

                        currencyAmount = currencyAmount.setScale(4, RoundingMode.DOWN);

                        total = total.setScale(2, RoundingMode.DOWN);

                        value = "$" + total.toString();

                        balanceListElements.add(new CompoundBalanceElement(
                                header, currencyAmount, drawableElement, color));
                    }

                    balanceListElements.add(0, new CompoundBalanceElement(
                            getResources().getString(R.string.hdr_balances), total, -1, R.color.balance_amount));

                    mListViewCompoundBalanceAdapter.processList(balanceListElements);

                    return true;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            return false;
        }
    }
}