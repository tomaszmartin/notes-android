package pl.codeinprogress.notes.view;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.SharedElementCallback;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.util.Log;

import javax.inject.Inject;

import pl.codeinprogress.notes.R;
import pl.codeinprogress.notes.injection.ApplicationComponent;
import pl.codeinprogress.notes.injection.ApplicationModule;
import pl.codeinprogress.notes.injection.DaggerApplicationComponent;
import pl.codeinprogress.notes.injection.NotesRepositoryModule;
import pl.codeinprogress.notes.model.NotesRepository;
import pl.codeinprogress.notes.util.Analytics;
import pl.codeinprogress.notes.util.scheduler.AndroidSchedulerProvider;

public class BaseActivity extends AppCompatActivity {

    @Inject
    NotesRepository injectedRepository;
    @Inject
    AndroidSchedulerProvider scheduler;
    @Inject
    Analytics analytics;
    @Inject
    SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                .detectAll()
                .penaltyLog()
                .build();
        StrictMode.setThreadPolicy(policy);

        ApplicationComponent component = DaggerApplicationComponent.builder()
                .applicationModule(new ApplicationModule(this))
                .notesRepositoryModule(new NotesRepositoryModule(this))
                .build();
        component.inject(this);

        analytics.sendScreen(getTag());
    }

    public String getTag() {
        return this.getClass().getSimpleName();
    }

    public void log(String message) {
        Log.d(getTag(), message);
    }

    public void switchNightMode() {
        boolean isNight = getResources().getBoolean(R.bool.isNight);
        if (isNight) {
            setDay();
        } else {
            setNight();
        }
    }

    public NotesRepository getRepository() {
        return injectedRepository;
    }

    public AndroidSchedulerProvider getScheduler() {
        return scheduler;
    }

    public SharedPreferences getPreferences() {
        return preferences;
    }



    private void setDay() {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        recreate();
    }

    private void setNight() {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        recreate();
    }

}
