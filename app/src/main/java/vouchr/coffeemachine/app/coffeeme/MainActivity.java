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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import vouchr.coffee.models.CoffeePot;

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showAddNewPotDialog() {
        BrewCoffeePotView brewView = new BrewCoffeePotView(MainActivity.this);
        final Dialog brewDialog = new Dialog(MainActivity.this, R.style.BrewCoffeePotDialog);
        brewView.setCoffeePotViewAdapter(new BrewCoffeePotView.BrewCoffeePotViewAdapter() {
            @Override
            public void onPotBrewed(CoffeePot coffeePot) {
                brewDialog.dismiss();
            }

            @Override
            public void onCancelled() {

            }
        });

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
//                    outputTextView.setText(TextUtils.join(outputTextView.getText() + "\n", results));
                    coffeePotList.setAdapter(new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, results));;
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
