package vouchr.coffeemachine.app.coffeeme;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

/**
 * Created by Bryan on 2017-03-16.
 */

public class CoffeeSheetSessionHiddenActivity extends AppCompatActivity{

    private GoogleCredentialUIHelper googleCredentialUIHelper;
    private CoffeeSheetService coffeeSheetService;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        googleCredentialUIHelper = new GoogleCredentialUIHelper(this);
        coffeeSheetService = new CoffeeSheetService(googleCredentialUIHelper.getGoogleAccountCredential());
        googleCredentialUIHelper.authenticate().subscribe(new Observer<Ignore>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(Ignore ignore) {
                CoffeeSheetSession.session(CoffeeSheetSessionHiddenActivity.this).success(coffeeSheetService);
                CoffeeSheetSessionHiddenActivity.this.finish();
            }

            @Override
            public void onError(Throwable e) {
                CoffeeSheetSession.session(CoffeeSheetSessionHiddenActivity.this).failure(e);
                CoffeeSheetSessionHiddenActivity.this.finish();
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
}
