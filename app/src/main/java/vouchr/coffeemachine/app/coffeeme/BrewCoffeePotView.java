package vouchr.coffeemachine.app.coffeeme;

import android.app.ProgressDialog;
import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function3;
import vouchr.coffee.models.CoffeePot;
import vouchr.coffee.models.CoffeePotBuilder;

/**
 * Created by Bryan on 3/15/2017.
 */

public class BrewCoffeePotView extends LinearLayout {

    @BindView(R.id.prepopulateLastPotButton)
    Button prepopulateLastPotButton;
    @BindView(R.id.dateEditText)
    EditText dateEditText;
    @BindView(R.id.baristaSpinner)
    Spinner baristaSpinner;
    @BindView(R.id.beansSpinner)
    Spinner beansSpinner;
    @BindView(R.id.roastSpinner)
    Spinner roastSpinner;
    @BindView(R.id.tbpsSeekBar)
    SeekBar tbpsSeekBar;
    @BindView(R.id.tbpsTextView)
    TextView tbpsTextView;
    @BindView(R.id.brewButton)
    Button brewButton;
    ProgressDialog progressDialog;
    private BrewCoffeePotViewAdapter coffeePotViewAdapter;
    private CoffeePotBuilder coffeePotBuilder;
    private List<String> beansList;
    private List<String> roastsList;
    private List<String> baristasList;

    public BrewCoffeePotView(Context context) {
        super(context);
        init();
    }

    public BrewCoffeePotView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BrewCoffeePotView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void setCoffeePotViewAdapter(BrewCoffeePotViewAdapter brewCoffeePotViewAdapter) {
        this.coffeePotViewAdapter = brewCoffeePotViewAdapter;
    }

    private void init() {
        inflate(getContext(), R.layout.brew_new_coffee_pot, this);
        ButterKnife.bind(this);

        progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("Brewing...");

        tbpsSeekBar.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    int progress = 0;

                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progresValue, boolean fromUser) {
                        progress = progresValue;
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                        // Do something here,
                        //if you want to do anything at the start of
                        // touching the seekbar
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        // Display the value in textview
                        BrewCoffeePotView.this.setTbsp(progress);
                    }
                });
        baristaSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                coffeePotBuilder.setBarista(baristasList.get(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        beansSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                coffeePotBuilder.setBarista(beansList.get(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        roastSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                coffeePotBuilder.setBarista(roastsList.get(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        CoffeeSheetSession session = CoffeeSheetSession.session(getContext());

        Observable.combineLatest(session.getCoffeeBeans(), session.getCoffeeRoasts(), session.getBaristas(), new Function3<List<String>, List<String>, List<String>, Map<String, List<String>>>() {
            @Override
            public Map<String, List<String>> apply(List<String> beans, List<String> roasts, List<String> baristas) throws Exception {

                Map<String, List<String>> data = new HashMap<>();

                data.put("BEANS", beans);
                data.put("ROASTS", roasts);
                data.put("BARISTAS", baristas);

                return data;
            }
        }).observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<Map<String, List<String>>>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(Map<String, List<String>> data) {
                BrewCoffeePotView.this.beansSpinner.setAdapter(new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, beansList = data.get("BEANS")));
                BrewCoffeePotView.this.roastSpinner.setAdapter(new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, roastsList = data.get("ROASTS")));
                BrewCoffeePotView.this.baristaSpinner.setAdapter(new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, baristasList = data.get("BARISTAS")));
                setDefaults();
            }

            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
                setDefaults();
            }

            @Override
            public void onComplete() {

            }
        });
    }

    private void setDefaults() {
        coffeePotBuilder = CoffeePotBuilder.init();
        setDate(new Date());
        BrewCoffeePotView.this.setTbsp(10);
    }

    private void setDate(Date date) {
        String dateString = Utils.formatDateMddYY(date);
        coffeePotBuilder.setDateString(dateString);
        dateEditText.setText(dateString);
    }

    private void setTbsp(int progress) {
        coffeePotBuilder.setTbspCount((float) progress);
        tbpsTextView.setText(String.format(getContext().getString(R.string.tbps_out_of), progress, tbpsSeekBar.getMax()));
    }

    @OnClick({R.id.prepopulateLastPotButton, R.id.brewButton})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.prepopulateLastPotButton:
                CoffeeSheetSession.session(getContext()).getCoffeePots().observeOn(AndroidSchedulers.mainThread()).doOnError(new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        // TODO handle error
                    }
                }).subscribe(new Consumer<List<CoffeePot>>() {
                    @Override
                    public void accept(List<CoffeePot> coffeePots) throws Exception {
                        if (coffeePots.size() > 0) {
                            CoffeePot lastPotBrewed = coffeePots.get(coffeePots.size() - 1);
                            BrewCoffeePotView.this.populateWithCoffeePot(lastPotBrewed);
                        }
                        BrewCoffeePotView.this.setDefaults();
                    }
                });

                break;
            case R.id.brewButton:
                brewButton.setEnabled(false);
                progressDialog.show();
                if (coffeePotBuilder.isValidPot()) {
                    final CoffeePot newPotBrewed = coffeePotBuilder.createCoffeePot();
                    if (newPotBrewed != null) {
                        CoffeeSheetSession.session(getContext()).addCoffeePot(newPotBrewed).subscribe(new Consumer<Boolean>() {
                            @Override
                            public void accept(Boolean success) throws Exception {
                                progressDialog.hide();
                                brewButton.setEnabled(true);
                                if (success) {
                                    if (coffeePotViewAdapter != null) {
                                        BrewCoffeePotView.this.coffeePotViewAdapter.onPotBrewed(newPotBrewed);
                                    }
                                } else {
                                    // TODO: handle error
                                }
                            }
                        });
                    }
                }
                break;
        }
    }

    private void populateWithCoffeePot(CoffeePot lastPotBrewed) {
        BrewCoffeePotView.this.setDefaults();

        coffeePotBuilder = CoffeePotBuilder.coffeePotBuilderFromCoffeePot(lastPotBrewed);

        int indexForBarista = baristasList.indexOf(lastPotBrewed.getBarista());
        int indexForBean = beansList.indexOf(lastPotBrewed.getBeanName());
        int indexForRoast = roastsList.indexOf(lastPotBrewed.getRoast());

        baristaSpinner.setSelection(indexForBarista, true);
        beansSpinner.setSelection(indexForBean, true);
        roastSpinner.setSelection(indexForRoast, true);
    }

    public interface BrewCoffeePotViewAdapter {
        void onPotBrewed(CoffeePot coffeePot);

        void onCancelled();
    }
}
