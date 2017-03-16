package vouchr.coffeemachine.app.coffeeme;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.sheets.v4.SheetsScopes;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import vouchr.coffee.models.CoffeePot;
import vouchr.coffee.models.CoffeePotBuilder;

public class MainActivity extends AppCompatActivity {

    private static final String[] SCOPES = {SheetsScopes.SPREADSHEETS};

    @BindView(R.id.outputTextView)
    protected TextView outputTextView;
    @BindView(R.id.callGoogleSheetsButton)
    protected Button callApiButton;
    @BindView(R.id.coffeePotList)
    protected ListView coffeePotList;
    @BindView(R.id.content_main_layout)
    protected LinearLayout content_main_layout;
    @BindView(R.id.toolbar)
    protected Toolbar toolbar;
    @BindView(R.id.fab)
    protected FloatingActionButton fab;
    protected ProgressDialog progressDialog;

    private GoogleAccountCredential googleAccountCredential;
    private List<CoffeePot> coffeePots = null;

    private GoogleCredentialUIHelper googleCredentialUIHelper;
    private CoffeeSheetSession sharedCoffeeSheetSession = null;


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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        googleCredentialUIHelper = new GoogleCredentialUIHelper(this);

        setSupportActionBar(toolbar);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                showAddNewPotDialog();
                addPreviousPotIfAvailable();
                Snackbar.make(view, "Added new Pot!", Snackbar.LENGTH_SHORT).show();
            }
        });

        outputTextView.setVerticalScrollBarEnabled(true);
        outputTextView.setMovementMethod(new ScrollingMovementMethod());

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.calling_google_sheets_api));

        // Initialize credentials and service object.
        googleAccountCredential = GoogleAccountCredential.usingOAuth2(getApplicationContext(), Arrays.asList(SCOPES)).setBackOff(new ExponentialBackOff());
    }

    private void showAddNewPotDialog() {
        BrewCoffeePotView brewView = new BrewCoffeePotView(MainActivity.this);
        Dialog brewDialog = new Dialog(MainActivity.this, R.style.BrewCoffeePotDialog);
        brewDialog.setContentView(brewView);
        brewDialog.show();
    }

    @OnClick(R.id.callGoogleSheetsButton)
    protected void callApiButtonClick() {
        callApiButton.setEnabled(false);
        outputTextView.setText("");
        getResultsFromApi();
        callApiButton.setEnabled(true);
    }

    private CoffeePot getPreviousCoffeePot() {
        return coffeePots.get(coffeePots.size() - 1);
    }

    private void addPreviousPotIfAvailable() {
        CoffeePot previousPot = getPreviousCoffeePot();
        if (previousPot != null) {
            SimpleDateFormat format = new SimpleDateFormat("M/dd/YYYY", Locale.ENGLISH);
            CoffeePot newPot = CoffeePotBuilder.coffeePotBuilderFromCoffeePot(previousPot)
                    .setDateString(format.format(new Date()))
                    .setAvgRating(0.0d)
                    .createCoffeePot();
//            new AddCoffeePotRequestTask(googleAccountCredential).execute(newPot);
        }
    }

    private CoffeeSheetSession sharedCoffeeSheetSession() {
        if(sharedCoffeeSheetSession == null) {
            sharedCoffeeSheetSession = new CoffeeSheetSession(MainActivity.this, new CoffeeSheetService(googleCredentialUIHelper.getGoogleAccountCredential()));
        }
        return sharedCoffeeSheetSession;
    }

    /**
     * Attempt to call the API, after verifying that all the preconditions are
     * satisfied. The preconditions are: Google Play Services installed, an
     * account was selected and the device currently has online access. If any
     * of the preconditions are not satisfied, the app will prompt the user as
     * appropriate.
     */
    private void getResultsFromApi() {
        googleCredentialUIHelper.authenticate().flatMap(new Function<Ignore, ObservableSource<List<CoffeePot>>>() {
            @Override
            public ObservableSource<List<CoffeePot>> apply(Ignore ignore) throws Exception {
                return sharedCoffeeSheetSession().getCoffeePots();
            }
        }).observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<List<CoffeePot>>() {
            @Override
            public void onSubscribe(Disposable d) {
                outputTextView.setText("");
                progressDialog.show();
            }

            @Override
            public void onNext(List<CoffeePot> coffeePots) {

                List<String> output = new ArrayList<>();
                for (CoffeePot pot : coffeePots) {
                    output.add(pot.toString());
                }

                progressDialog.hide();
                if (output == null || output.size() == 0) {
                    outputTextView.setText(R.string.no_results_returned);
                } else {
                    output.add(0, "Data retrieved using the Google Sheets API:");
                    outputTextView.setText(TextUtils.join("\n", output));
                }
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        googleCredentialUIHelper.onActivityResult(requestCode, resultCode, data);
    }

//    private class AddCoffeePotRequestTask extends AsyncTask<CoffeePot, Void, Void> {
//        private com.google.api.services.sheets.v4.Sheets gSheetsService = null;
//        private Exception lastException = null;
//
//        AddCoffeePotRequestTask(GoogleAccountCredential credential) {
//            HttpTransport transport = AndroidHttp.newCompatibleTransport();
//            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
//            gSheetsService = new com.google.api.services.sheets.v4.Sheets.Builder(transport, jsonFactory, credential)
//                    .setApplicationName("CoffeeMe")
//                    .build();
//        }
//
//        @Override
//        protected Void doInBackground(CoffeePot... coffeePots) {
//            try {
//                addNewPot(coffeePots[0]);
//                return null;
//            } catch (Exception e) {
//                lastException = e;
//                cancel(true);
//                return null;
//            }
//        }
//
//        private void addNewPot(CoffeePot pot) {
//            String spreadsheetId = "1a5KdfYJdqvlYzv2BscGwxeZ2cf880HGAK_keWCbijOE";
//            String newPotRange = null;
//            try {
//                int rowIndex = MainActivity.this.coffeePots.size();
//                newPotRange = "A" + rowIndex + ":O" + rowIndex;
//                ValueRange newPotRow = new ValueRange();
//                newPotRow.setValues(Collections.singletonList(Arrays.<Object>asList(pot.getDateString(), pot.getBarista(), pot.getBeanName(), pot.getRoast(), pot.getTbspCount(), pot.getAvgRating())));
//                this.gSheetsService.spreadsheets().values().append(spreadsheetId, newPotRange, newPotRow).setValueInputOption("RAW").execute();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//
//        @Override
//        protected void onPreExecute() {
//            outputTextView.setText("");
//            progressDialog.show();
//        }
//
//        @Override
//        protected void onPostExecute(Void output) {
//            progressDialog.hide();
//        }
//
//        @Override
//        protected void onCancelled() {
//            progressDialog.hide();
//            if (lastException != null) {
//                if (lastException instanceof GooglePlayServicesAvailabilityIOException) {
//                    Utils.showGooglePlayServicesAvailabilityErrorDialog(((GooglePlayServicesAvailabilityIOException) lastException).getConnectionStatusCode(), MainActivity.this, REQUEST_GOOGLE_PLAY_SERVICES);
//                } else if (lastException instanceof UserRecoverableAuthIOException) {
//                    startActivityForResult(((UserRecoverableAuthIOException) lastException).getIntent(), MainActivity.REQUEST_AUTHORIZATION);
//                } else {
//                    outputTextView.setText(String.format(Locale.ENGLISH, getString(R.string.the_following_error_occurred), lastException.getMessage()));
//                }
//            } else {
//                outputTextView.setText(R.string.request_cancelled);
//            }
//        }
//    }
//
//    /**
//     * An asynchronous task that handles the Google Sheets API call.
//     * Placing the API calls in their own task ensures the UI stays responsive.
//     */
//    private class CoffeeMeDataFetchRequestTask extends AsyncTask<Void, Void, List<String>> {
//        private com.google.api.services.sheets.v4.Sheets gSheetsService = null;
//        private Exception lastException = null;
//
//        CoffeeMeDataFetchRequestTask(GoogleAccountCredential credential) {
//            HttpTransport transport = AndroidHttp.newCompatibleTransport();
//            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
//            gSheetsService = new com.google.api.services.sheets.v4.Sheets.Builder(transport, jsonFactory, credential)
//                    .setApplicationName("CoffeeMe")
//                    .build();
//        }
//
//        /**
//         * Background task to call Google Sheets API.
//         *
//         * @param params no parameters needed for this task.
//         */
//        @Override
//        protected List<String> doInBackground(Void... params) {
//            try {
//                return getDataFromApi();
//            } catch (Exception e) {
//                lastException = e;
//                cancel(true);
//                return null;
//            }
//        }
//
//        /**
//         * Fetch a list of names and majors of students in a sample spreadsheet:
//         * https://docs.google.com/spreadsheets/d/1a5KdfYJdqvlYzv2BscGwxeZ2cf880HGAK_keWCbijOE/edit
//         *
//         * @return List of Date, Barista, Beans, Avg Rating
//         * @throws IOException
//         */
//        private List<String> getDataFromApi() throws IOException {
//
//            List<String> results = new ArrayList<String>();
//            results.add("Date, Barista, Beans, Roast, Tbsp Count, Avg Rating");
//            results.addAll(getCoffeePots());
//            results.addAll(getBeans());
//            results.addAll(getRoasts());
//            return results;
//        }
//
//        private List<String> getCoffeePots() throws IOException {
//            String spreadsheetId = "1a5KdfYJdqvlYzv2BscGwxeZ2cf880HGAK_keWCbijOE";
//            String range = "Coffee Scores!A2:O";
//            List<String> results = new ArrayList<String>();
//            ValueRange response = this.gSheetsService.spreadsheets().values()
//                    .get(spreadsheetId, range)
//                    .execute();
//            List<List<Object>> values = response.getValues();
//            List<CoffeePot> coffeePots = new ArrayList<>();
//            if (values != null) {
//                for (List row : values) {
//                    String dateString = (String) row.get(0);
//                    String barista = (String) row.get(1);
//                    String beans = (String) row.get(2);
//                    String roast = (String) row.get(3);
//                    Float tbsp = Float.parseFloat((String) row.get(4));
//                    Double avgRating = Double.parseDouble((String) row.get(5));
//                    CoffeePot pot = CoffeePotBuilder.init().setDateString(dateString)
//                            .setBarista(barista)
//                            .setBeanName(beans)
//                            .setRoast(roast)
//                            .setTbspCount(tbsp)
//                            .setAvgRating(avgRating)
//                            .createCoffeePot();
//                    results.add(pot.toString());
//                    coffeePots.add(pot);
//                }
//                MainActivity.this.coffeePots = coffeePots;
//            }
//            return results;
//        }
//
//        private List<String> getBeans() throws IOException {
//            String spreadsheetId = "1a5KdfYJdqvlYzv2BscGwxeZ2cf880HGAK_keWCbijOE";
//            String range = "Beans!A2:A";
//            List<String> results = new ArrayList<String>();
//            ValueRange response = this.gSheetsService.spreadsheets().values()
//                    .get(spreadsheetId, range)
//                    .execute();
//            List<List<Object>> values = response.getValues();
//            if (values != null) {
//                results.add("Date, Barista, Beans, Avg Rating");
//                for (List row : values) {
//                    results.add((String) row.get(0));
//                }
//            }
//            return results;
//        }
//
//        private List<String> getRoasts() throws IOException {
//            String spreadsheetId = "1a5KdfYJdqvlYzv2BscGwxeZ2cf880HGAK_keWCbijOE";
//            String range = "Roasts!A:A";
//            List<String> results = new ArrayList<String>();
//            ValueRange response = this.gSheetsService.spreadsheets().values()
//                    .get(spreadsheetId, range)
//                    .execute();
//            List<List<Object>> values = response.getValues();
//            if (values != null) {
//                results.add("Date, Barista, Beans, Avg Rating");
//                for (List row : values) {
//                    results.add((String) row.get(0));
//                }
//            }
//            return results;
//        }
//
//
//        @Override
//        protected void onPreExecute() {
//            outputTextView.setText("");
//            progressDialog.show();
//        }
//
//        @Override
//        protected void onPostExecute(List<String> output) {
//            progressDialog.hide();
//            if (output == null || output.size() == 0) {
//                outputTextView.setText(R.string.no_results_returned);
//            } else {
//                output.add(0, "Data retrieved using the Google Sheets API:");
//                outputTextView.setText(TextUtils.join("\n", output));
//            }
//        }
//
//        @Override
//        protected void onCancelled() {
//            progressDialog.hide();
//            if (lastException != null) {
//                if (lastException instanceof GooglePlayServicesAvailabilityIOException) {
//                    Utils.showGooglePlayServicesAvailabilityErrorDialog(
//                            ((GooglePlayServicesAvailabilityIOException) lastException)
//                                    .getConnectionStatusCode(), MainActivity.this, REQUEST_GOOGLE_PLAY_SERVICES);
//                } else if (lastException instanceof UserRecoverableAuthIOException) {
//                    startActivityForResult(((UserRecoverableAuthIOException) lastException).getIntent(), MainActivity.REQUEST_AUTHORIZATION);
//                } else {
//                    outputTextView.setText(String.format(Locale.ENGLISH, getString(R.string.the_following_error_occurred), lastException.getMessage()));
//                }
//            } else {
//                outputTextView.setText(R.string.request_cancelled);
//            }
//        }
//    }
}
