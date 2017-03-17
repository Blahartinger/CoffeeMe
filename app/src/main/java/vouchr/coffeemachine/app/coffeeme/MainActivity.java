package vouchr.coffeemachine.app.coffeeme;

import android.app.Dialog;
import android.app.ProgressDialog;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import vouchr.coffee.models.CoffeePot;
import vouchr.coffee.models.CoffeePotBuilder;

public class MainActivity extends AppCompatActivity {

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

    private List<CoffeePot> coffeePots = null;

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

        setSupportActionBar(toolbar);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAddNewPotDialog();
                Snackbar.make(view, "Added new Pot!", Snackbar.LENGTH_SHORT).show();
            }
        });

        outputTextView.setVerticalScrollBarEnabled(true);
        outputTextView.setMovementMethod(new ScrollingMovementMethod());

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.calling_google_sheets_api));
    }

    private void showAddNewPotDialog() {
        BrewCoffeePotView brewView = new BrewCoffeePotView(MainActivity.this);
        Dialog brewDialog = new Dialog(MainActivity.this, R.style.BrewCoffeePotDialog);
        brewDialog.setContentView(brewView);

//        addPreviousPotIfAvailable(); // TODO: add to dialog

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
            CoffeeSheetSession.session(this).addCoffeePot(newPot).subscribe(new Observer<Boolean>() {
                @Override
                public void onSubscribe(Disposable d) {
                    progressDialog.show();
                }

                @Override
                public void onNext(Boolean aBoolean) {

                }

                @Override
                public void onError(Throwable e) {
                    progressDialog.hide();
                }

                @Override
                public void onComplete() {
                    progressDialog.hide();
                }
            });
        }
    }

    private void getResultsFromApi() {
        CoffeeSheetSession.session(this).getCoffeePots().doOnNext(new Consumer<List<CoffeePot>>() {
            @Override
            public void accept(List<CoffeePot> coffeePots) throws Exception {
                MainActivity.this.coffeePots = coffeePots;
            }
        }).map(new Function<List<CoffeePot>, List<String>>() {
            @Override
            public List<String> apply(List<CoffeePot> coffeePots) throws Exception {
                List<String> output = new ArrayList<>();
                for (CoffeePot pot : coffeePots) {
                    output.add(pot.toString());
                }
                return output;
            }
        }).observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<List<String>>() {

            @Override
            public void onSubscribe(Disposable d) {
                outputTextView.setText("");
                progressDialog.show();
            }

            @Override
            public void onNext(List<String> results) {

                if (results == null || results.size() == 0) {
                    outputTextView.setText(R.string.no_results_returned);
                } else {
                    results.add(0, "Data retrieved using the Google Sheets API:");
                    outputTextView.setText(TextUtils.join(outputTextView.getText() + "\n", results));
                }
            }

            @Override
            public void onError(Throwable e) {
                progressDialog.hide();
            }

            @Override
            public void onComplete() {
                progressDialog.hide();
            }
        });
    }
}
