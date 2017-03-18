package vouchr.coffeemachine.app.coffeeme;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import vouchr.coffee.models.CoffeePot;

public class MainActivity extends AppCompatActivity {

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
    private MainActivity.CoffeePotListViewAdapter listAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        listAdapter = new CoffeePotListViewAdapter();
        coffeePotList.setAdapter(listAdapter);

        setSupportActionBar(toolbar);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAddNewPotDialog();
            }
        });

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.fetching_coffee_pots));

        refreshCoffeePotList();
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.menu_main, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        int id = item.getItemId();
//        if (id == R.id.action_settings) {
//            return true;
//        }
//        return super.onOptionsItemSelected(item);
//    }

    private void showAddNewPotDialog() {
        BrewCoffeePotView brewView = new BrewCoffeePotView(MainActivity.this);
        final Dialog brewDialog = new Dialog(MainActivity.this, R.style.BrewCoffeePotDialog);
        brewView.setCoffeePotViewAdapter(new BrewCoffeePotView.BrewCoffeePotViewAdapter() {
            @Override
            public void onPotBrewed(CoffeePot coffeePot) {
                brewDialog.dismiss();
                Snackbar.make(fab, "Added new Pot!", Snackbar.LENGTH_SHORT).show();
                refreshCoffeePotList();
            }

            @Override
            public void onCancelled() {

            }
        });

        brewDialog.setContentView(brewView);
        brewDialog.show();
    }

    private void refreshCoffeePotList() {
        CoffeeSheetSession.session(this).getCoffeePots().doOnNext(new Consumer<List<CoffeePot>>() {
            @Override
            public void accept(List<CoffeePot> coffeePots) throws Exception {
                MainActivity.this.coffeePots = coffeePots;
            }
        }).observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<List<CoffeePot>>() {

            @Override
            public void onSubscribe(Disposable d) {
                progressDialog.show();
            }

            @Override
            public void onNext(List<CoffeePot> coffeePots) {

                if (coffeePots == null || coffeePots.size() == 0) {
                    // TODO: show R.string.no_results_returned error
                } else {
                    listAdapter.setData(coffeePots);
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

    private class CoffeePotListViewAdapter extends BaseAdapter {

        List<CoffeePot> data = new ArrayList<>();

        public void setData(List<CoffeePot> coffeePots) {
            data.addAll(coffeePots);
            Collections.reverse(data);
            notifyDataSetInvalidated();
        }

        @Override
        public int getCount() {
            return data.size();
        }

        @Override
        public CoffeePot getItem(int position) {
            return data.get(position);
        }

        @Override
        public long getItemId(int position) {
            return getItem(position).toString().hashCode();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            ListViewCellCoffeePot potView = null;
            if (convertView != null && convertView instanceof ListViewCellCoffeePot) {
                potView = (ListViewCellCoffeePot) convertView;
            } else {
                potView = new ListViewCellCoffeePot(MainActivity.this);
            }

            CoffeePot coffeePotData = getItem(position);
            potView.setCoffeePot(coffeePotData);

            return potView;
        }
    }
}
