package pl.codeinprogress.notes.ui.tasks;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.io.File;
import pl.codeinprogress.notes.R;
import pl.codeinprogress.notes.firebase.FirebaseApplication;
import pl.codeinprogress.notes.model.Note;

/**
 * Created by tomaszmartin on 18.06.2015.
 */

public class DeleteNoteTask extends AsyncTask<Note, Void, Void> {

    private Context context;

    public DeleteNoteTask(Context context) {
        this.context = context.getApplicationContext();
    }

    @Override
    protected Void doInBackground(Note... notes) {
        Note note = notes[0];
        deleteFromFile(note);
        deleteFromFirebase(note);
        return null;
    }

    private void deleteFromFile(Note note) {
        File file = new File(Environment.getExternalStorageDirectory() + File.separator + note.getFileName());
        file.delete();
    }

    private void deleteFromFirebase(Note note) {
        if (context instanceof FirebaseApplication) {
            FirebaseApplication application = (FirebaseApplication) context;
            FirebaseStorage storage = application.getStorage();
            StorageReference reference = storage.getReferenceFromUrl(context.getString(R.string.firebase_storage_bucket));
            StorageReference current = reference.child(note.getFileName());
            current.delete();
        }
    }

}