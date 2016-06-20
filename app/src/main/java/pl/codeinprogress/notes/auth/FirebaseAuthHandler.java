package pl.codeinprogress.notes.auth;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.util.Log;
import android.util.Patterns;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

import pl.codeinprogress.notes.R;
import pl.codeinprogress.notes.firebase.FirebaseApplication;

/**
 * Created by tomaszmartin on 12.06.16.
 * <p/>
 * Class hor handling authorization.
 */

public class FirebaseAuthHandler {

    private final String ID_KEY = "USER_ID";
    private final String NAME_KEY = "USER_NAME";
    private final String EMAIL_KEY = "USER_EMAIL";
    private final String IMAGE_KEY = "USER_IMAGE";
    private final String STATUS_KEY = "USER_STATUS";
    private SharedPreferences manager;
    private final FirebaseApplication application;

    public FirebaseAuthHandler(FirebaseApplication app) {
        this.application = app;
        manager = PreferenceManager.getDefaultSharedPreferences(application);
        application.getAuth().addAuthStateListener(new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = application.getAuth().getCurrentUser();
                if (user != null) {
                    Credentials credentials = Credentials.fromFirebaseUser(user);
                    onLoggedIn(credentials);
                } else {
                    // User is signed out
                }
            }
        });
    }

    public void login(String email, String password) {
        application.getAuth().signInWithEmailAndPassword(email, password);
    }

    public void singup(final Credentials credentials, String password) {
        application.getAuth().createUserWithEmailAndPassword(credentials.getEmail(), password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        onSignedUp(credentials);
                    }
                });
    }

    public void onSignedUp(Credentials credentials) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        UserProfileChangeRequest profileUpdates;
        profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(credentials.getName())
                .setPhotoUri(Uri.parse(credentials.getImage()))
                .build();

        user.updateProfile(profileUpdates)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                        }
                    }
                });

        saveCredentials(credentials);
    }

    public void onLoggedIn(Credentials credentials) {
        saveCredentials(credentials);
    }

    public void onLoggedOut() {
        Credentials credentials = new Credentials(
                null,
                null,
                null,
                null,
                false
        );
        saveCredentials(credentials);
    }

    public boolean isLogged() {
        return manager.getBoolean(STATUS_KEY, false);
    }

    private void saveCredentials(Credentials credentials) {
        Editor editor = manager.edit();
        editor.putString(ID_KEY, credentials.getId());
        editor.putString(NAME_KEY, credentials.getName());
        editor.putString(EMAIL_KEY, credentials.getEmail());
        editor.putString(IMAGE_KEY, credentials.getImage());
        editor.putBoolean(STATUS_KEY, credentials.isLogged());
        editor.apply();
    }

}