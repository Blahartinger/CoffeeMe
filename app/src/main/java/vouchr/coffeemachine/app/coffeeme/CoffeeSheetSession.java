package vouchr.coffeemachine.app.coffeeme;

import android.content.Context;
import android.content.Intent;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;
import vouchr.coffee.models.CoffeePot;

/**
 * Created by Bryan on 2017-03-16.
 */

public class CoffeeSheetSession {

    private static CoffeeSheetSession __session__ = null;

    private Context context;
    private Intent intent;
    private PublishSubject<CoffeeSheetService> sessionEmitter;
    private CoffeeSheetService coffeeSheetServiceCachedInstance;

    private CoffeeSheetSession() {
    }

    private static synchronized CoffeeSheetSession instance() {
        if (__session__ == null) {
            __session__ = new CoffeeSheetSession();
        }
        return __session__;
    }

    public static CoffeeSheetSession session(Context context) {

        CoffeeSheetSession instance = instance();

        instance.context = context.getApplicationContext();
        Intent intent = new Intent(instance.context, CoffeeSheetSessionHiddenActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        instance.intent = intent;
        instance.sessionEmitter = PublishSubject.create();
        instance.sessionEmitter.share();

        return instance;
    }

    public void success(CoffeeSheetService coffeeSheetService) {
        this.coffeeSheetServiceCachedInstance = coffeeSheetService;
        sessionEmitter.onNext(coffeeSheetService);
        sessionEmitter.onComplete();
    }

    public void failure(Throwable t) {
        sessionEmitter.onError(t);
    }

    private Observable<CoffeeSheetService> authObservable() {
        if (coffeeSheetServiceCachedInstance != null) {
            return Observable.just(coffeeSheetServiceCachedInstance).observeOn(Schedulers.io());
        } else {
            return sessionEmitter.doOnSubscribe(new Consumer<Disposable>() {
                @Override
                public void accept(Disposable disposable) throws Exception {
                    if(intent != null) {
                        context.startActivity(intent);
                        intent = null;
                    }
                }
            }).observeOn(Schedulers.io());
        }
    }

    public Observable<List<CoffeePot>> getCoffeePots() {
        return authObservable().flatMap(new Function<CoffeeSheetService, ObservableSource<List<CoffeePot>>>() {
            @Override
            public ObservableSource<List<CoffeePot>> apply(CoffeeSheetService coffeeSheetService) throws Exception {
                try {
                    return Observable.just(coffeeSheetService.getCoffeePots());
                } catch (Throwable t) {
                    t.printStackTrace();
                    return Observable.error(t);
                }
            }
        });
    }

    public Observable<Boolean> addCoffeePot(final CoffeePot coffeePot) {
        return authObservable().flatMap(new Function<CoffeeSheetService, ObservableSource<Boolean>>() {
            @Override
            public ObservableSource<Boolean> apply(CoffeeSheetService coffeeSheetService) throws Exception {
                try {
                    return Observable.just(coffeeSheetService.addNewPot(coffeePot));
                } catch (Throwable t) {
                    t.printStackTrace();
                    return Observable.error(t);
                }
            }
        });
    }

    public Observable<List<String>> getCoffeeBeans() {
        return authObservable().flatMap(new Function<CoffeeSheetService, ObservableSource<List<String>>>() {
            @Override
            public ObservableSource<List<String>> apply(CoffeeSheetService coffeeSheetService) throws Exception {
                try {
                    return Observable.just(coffeeSheetService.getBeans());
                } catch (Throwable t) {
                    t.printStackTrace();
                    return Observable.error(t);
                }
            }
        });
    }

    public Observable<List<String>> getCoffeeRoasts() {
        return authObservable().flatMap(new Function<CoffeeSheetService, ObservableSource<List<String>>>() {
            @Override
            public ObservableSource<List<String>> apply(CoffeeSheetService coffeeSheetService) throws Exception {
                try {
                    return Observable.just(coffeeSheetService.getRoasts());
                } catch (Throwable t) {
                    t.printStackTrace();
                    return Observable.error(t);
                }
            }
        });
    }

    public Observable<List<String>> getBaristas() {
        return authObservable().flatMap(new Function<CoffeeSheetService, ObservableSource<List<String>>>() {
            @Override
            public ObservableSource<List<String>> apply(CoffeeSheetService coffeeSheetService) throws Exception {
                try {
                    return Observable.just(coffeeSheetService.getBaristas());
                } catch (Throwable t) {
                    t.printStackTrace();
                    return Observable.error(t);
                }
            }
        });
    }
}