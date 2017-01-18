package pl.codeinprogress.notes.presenter.auth;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.Log;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import pl.codeinprogress.notes.view.BaseActivity;
import pl.codeinprogress.notes.view.LoginActivity;

/**
 * Class hor handling authentication.
 * Works wih Firebase and local preferences.
 */

public class Auth {

    private final String ID_KEY = "USER_ID";
    private final String NAME_KEY = "USER_NAME";
    private final String EMAIL_KEY = "USER_EMAIL";
    private final String IMAGE_KEY = "USER_IMAGE";
    private final String STATUS_KEY = "USER_STATUS";
    private final SharedPreferences manager;
    private final FirebaseAuth auth;
    private static Auth instance;
    private Context context;

    private Auth(Context ctx) {
        this.context = ctx;
        this.manager = PreferenceManager.getDefaultSharedPreferences(ctx);
        this.auth = FirebaseAuth.getInstance();
        auth.addAuthStateListener(new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = auth.getCurrentUser();
                if (user != null) {
                    Credentials credentials = fromFirebaseUser(user);
                    onLoggedIn(credentials);
                }
            }
        });
    }

    public static Auth getInstance(Context ctx) {
        if (instance == null) {
            instance = new Auth(ctx);
        }

        return instance;
    }

    public void login(String email, String password, final BaseActivity activity) {
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                activity.finish();
            }
        });
    }

    public void logout() {
        auth.signOut();
        onLoggedOut();
        Intent intent = new Intent(context, LoginActivity.class);
        context.startActivity(intent);
    }

    public void singup(final Credentials credentials, String password, final BaseActivity activity) {
        auth.createUserWithEmailAndPassword(credentials.getEmail(), password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                log("User has been created with email " + credentials.getEmail());
                onSignedUp(credentials, activity);
            }
        });
    }

    public void onSignedUp(Credentials credentials, BaseActivity activity) {
        FirebaseUser user = auth.getCurrentUser();
        Uri image = null;
        try {
            image = Uri.parse(credentials.getImage());
        } catch (Exception e) {

        }


        UserProfileChangeRequest profileUpdates;
        profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(credentials.getName())
                .setPhotoUri(image)
                .build();

        if (user != null) {
            user.updateProfile(profileUpdates)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                log("Firebase user updated");
                            }
                        }
                    });
        }

        saveCredentials(credentials);
        activity.setResult(Activity.RESULT_OK);
        activity.finish();
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

    private void log(String message) {
        Log.d(Auth.class.getSimpleName(), message);
    }

    public Credentials getCredentials() {
        String name = manager.getString(NAME_KEY, null);
        String id = manager.getString(ID_KEY, null);
        String email = manager.getString(EMAIL_KEY, null);
        String image = manager.getString(IMAGE_KEY, null);
        boolean state = manager.getBoolean(STATUS_KEY, false);

        return new Credentials(name, id, email, image, state);
    }

    public static Credentials fromFirebaseUser(FirebaseUser user) {
        String image = null;
        if (user.getPhotoUrl() != null) {
            image = user.getPhotoUrl().toString();
        }

        return new Credentials(
                user.getDisplayName(),
                user.getUid(),
                user.getEmail(),
                image,
                true
        );
    }

}
