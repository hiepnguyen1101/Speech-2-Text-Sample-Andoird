package vn.tgg.s2t;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {
    private final int REQ_CODE_SPEECH_INPUT = 100;
    public static FirebaseFirestore FirebaseFirestoreInstance = FirebaseFirestore.getInstance();
    @BindView(R.id.txtSpeechInput)
    public TextView txtSpeechInput;
    @BindView(R.id.btnSpeak)
    public ImageButton btnSpeak;
    public static String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setupElementEventListener();
    }

    void setupElementEventListener() {
        btnSpeak.setOnClickListener(v -> promptSpeechInput());
    }

    private void promptSpeechInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                getString(R.string.speech_prompt));
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(),
                    getString(R.string.speech_not_supported),
                    Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Receiving speech input
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {

                    ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    for (String _result : result) {
                        Log.e("MainActivity", _result);
                    }
                    AlertDialog.Builder builderSingle = new AlertDialog.Builder(MainActivity.this);
                    builderSingle.setIcon(R.drawable.ico_mic);
                    builderSingle.setTitle("Chọn một gợi ý mà bạn muốn");
                    final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.select_dialog_singlechoice);
                    arrayAdapter.add("Muốn nói lại");
                    for (String _suggestion : result) {
                        arrayAdapter.add(_suggestion);
                    }
                    builderSingle.setNegativeButton("Hủy bỏ", (dialog, which) -> dialog.dismiss());

                    builderSingle.setAdapter(arrayAdapter, (dialog, which) -> {
                        if (arrayAdapter.getItem(which).equals("Muốn nói lại")) {
                            txtSpeechInput.setText("");
                            promptSpeechInput();
                            return;
                        }
                        Map<String, Object> text = new HashMap<>();
                        text.put("id", UUID.randomUUID().toString());
                        text.put("device", Settings.Secure.getString(this.getContentResolver(),
                                Settings.Secure.ANDROID_ID));
                        text.put("date", new Date().toString());
                        text.put("text", arrayAdapter.getItem(which));
                        FirebaseFirestoreInstance.collection("text").document().set(text).addOnSuccessListener(aVoid -> Log.d(TAG, "DocumentSnapshot successfully written!")).addOnFailureListener(e -> Log.w(TAG, "Error writing document", e));
                        txtSpeechInput.setText(arrayAdapter.getItem(which));
                    });
                    builderSingle.show();

                }
                break;
            }
        }
    }




}
