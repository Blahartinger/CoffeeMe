package vouchr.coffeemachine.app.coffeeme;

import android.content.Context;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.schedulers.Schedulers;
import vouchr.coffee.models.CoffeePot;

/**
 * Created by Bryan on 2017-03-16.
 */

public class CoffeeSheetSession {

    private CoffeeSheetService coffeeSheetService;

    public CoffeeSheetSession(Context context, CoffeeSheetService coffeeSheetService) {
        this.coffeeSheetService = coffeeSheetService;
    }

    public Observable<List<CoffeePot>> getCoffeePots() {
        return Observable.create(new ObservableOnSubscribe<List<CoffeePot>>() {
            @Override
            public void subscribe(ObservableEmitter<List<CoffeePot>> e) throws Exception {
                try {
                    e.onNext(coffeeSheetService.getCoffeePots());
                } catch (Throwable t) {
                    t.printStackTrace();
                    e.onError(t);
                }
            }
        }).subscribeOn(Schedulers.io());
    }
}