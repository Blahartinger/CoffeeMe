package vouchr.coffeemachine.app.coffeeme;

import android.Manifest;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.annotation.NonNull;

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.sheets.v4.SheetsScopes;

import java.util.Arrays;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

import static android.app.Activity.RESULT_OK;

/**
 * Created by Bryan on 2017-03-16.
 */

public class GoogleCredentialUIHelper implements EasyPermissions.PermissionCallbacks {

    private static final String PREF_ACCOUNT_NAME = "accountName";

    private static final int REQUEST_ACCOUNT_PICKER = 1000;
    private static final int REQUEST_AUTHORIZATION = 1001;
    private static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    private static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1003;

    private static final String[] SCOPES = {SheetsScopes.SPREADSHEETS};

    public GoogleAccountCredential getGoogleAccountCredential() {
        return googleAccountCredential;
    }

    private GoogleAccountCredential googleAccountCredential = null;

    private Activity activity;

    private PublishSubject<Ignore> authenticationPublisher;

    public GoogleCredentialUIHelper(Activity activity) {
        this.activity = activity;
        // Initialize credentials and service object.
        googleAccountCredential = GoogleAccountCredential.usingOAuth2(activity.getApplicationContext(), Arrays.asList(SCOPES)).setBackOff(new ExponentialBackOff());
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode != RESULT_OK) {
//                    outputTextView.setText(R.string.this_app_requires_google_play_services);
                    authenticationPublisher.onError(new Exception(activity.getString(R.string.this_app_requires_google_play_services)));
                } else {
                    authenticationPublisher.onNext(Ignore.GET);
                }
                break;
            case REQUEST_ACCOUNT_PICKER:
                if (resultCode == RESULT_OK && data != null && data.getExtras() != null) {
                    String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        SharedPreferences settings = activity.getPreferences(Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(PREF_ACCOUNT_NAME, accountName);
                        editor.apply();
                        googleAccountCredential.setSelectedAccountName(accountName);
                        authenticationPublisher.onNext(Ignore.GET);
                    }
                }
                break;
            case REQUEST_AUTHORIZATION:
                if (resultCode == RESULT_OK) {
                    authenticationPublisher.onNext(Ignore.GET);
                }
                break;
        }
    }

    /**
     * Attempt to call the API, after verifying that all the preconditions are
     * satisfied. The preconditions are: Google Play Services installed, an
     * account was selected and the device currently has online access. If any
     * of the preconditions are not satisfied, the app will prompt the user as
     * appropriate.
     */
    public Observable<Ignore> authenticate() {
        return Observable.create(new ObservableOnSubscribe<Ignore>() {
            @Override
            public void subscribe(ObservableEmitter<Ignore> e) throws Exception {
                if (!Utils.isGooglePlayServicesAvailable(activity)) {
                    authenticationPublisher = PublishSubject.create();
                    e.onError(new Exception("ERROR_AQUIRE_GOOGLE_PLAY_SERVICES"));
                    Utils.acquireGooglePlayServices(activity, REQUEST_GOOGLE_PLAY_SERVICES);
                } else if (googleAccountCredential.getSelectedAccountName() == null) {
                    authenticationPublisher = PublishSubject.create();
                    e.onError(new Exception("ERROR_CHOOSE_ACCOUNT"));
                    chooseAccount();
                } else if (!Utils.isDeviceOnline(activity)) {
//                    outputTextView.setText(R.string.no_network_connection_available);
                    e.onError(new Exception(activity.getString(R.string.no_network_connection_available)));
                } else {
                    authenticationPublisher.onNext(Ignore.GET);
                }
            }
        }).observeOn(Schedulers.io()).onErrorResumeNext(new Function<Throwable, ObservableSource<? extends Ignore>>() {
            @Override
            public ObservableSource<? extends Ignore> apply(Throwable throwable) throws Exception {
                if(throwable instanceof Exception){
                    Exception e = (Exception) throwable;
                    if(e.getMessage().equals("ERROR_CHOOSE_ACCOUNT")) {
                        return authenticationPublisher;
                    }
                }
                return Observable.error(throwable);
            }
        });
    }



    /**
     * Attempts to set the account used with the API credentials. If an account
     * name was previously saved it will use that one; otherwise an account
     * picker dialog will be shown to the user. Note that the setting the
     * account to use with the credentials object requires the app to have the
     * GET_ACCOUNTS permission, which is requested here if it is not already
     * present. The AfterPermissionGranted annotation indicates that this
     * function will be rerun automatically whenever the GET_ACCOUNTS permission
     * is granted.
     */
    @AfterPermissionGranted(REQUEST_PERMISSION_GET_ACCOUNTS)
    private void chooseAccount() {
        if (EasyPermissions.hasPermissions(activity, Manifest.permission.GET_ACCOUNTS)) {
            String accountName = activity.getPreferences(Context.MODE_PRIVATE).getString(PREF_ACCOUNT_NAME, null);
            if (accountName != null) {
                googleAccountCredential.setSelectedAccountName(accountName);
                authenticationPublisher.onNext(Ignore.GET);
            } else {
                // Start a dialog from which the user can choose an account
                activity.startActivityForResult(googleAccountCredential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
            }
        } else {
            // Request the GET_ACCOUNTS permission via a user dialog
            EasyPermissions.requestPermissions(this, activity.getString(R.string.app_needs_access_to_your_google_account), REQUEST_PERMISSION_GET_ACCOUNTS, Manifest.permission.GET_ACCOUNTS);
        }
    }

    /**
     * Respond to requests for permissions at runtime for API 23 and above.
     *
     * @param requestCode  The request code passed in
     *                     requestPermissions(android.app.Activity, String, int, String[])
     * @param permissions  The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     *                     which is either PERMISSION_GRANTED or PERMISSION_DENIED. Never null.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            activity.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    /**
     * Callback for when a permission is granted using the EasyPermissions
     * library.
     *
     * @param requestCode The request code associated with the requested
     *                    permission
     * @param list        The requested permission list. Never null.
     */
    @Override
    public void onPermissionsGranted(int requestCode, List<String> list) {
        // Do nothing.
    }

    /**
     * Callback for when a permission is denied using the EasyPermissions
     * library.
     *
     * @param requestCode The request code associated with the requested
     *                    permission
     * @param list        The requested permission list. Never null.
     */
    @Override
    public void onPermissionsDenied(int requestCode, List<String> list) {
        // Do nothing.
    }
}
