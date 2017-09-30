package androidseminar.speechtotext;

import android.app.Activity;
import android.app.SearchManager;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.nio.InvalidMarkException;
import java.util.ArrayList;
import java.util.Locale;

public class MainActivityNoDialog extends Activity implements RecognitionListener {

    private TextView myTextView;
    private ListView myListView;
    private ImageButton myImageButton;
    private ProgressBar myProgressBar;

    private SpeechRecognizer mySpeechRecognizer = null;
    private Intent myRecognizerIntent;

    private String LOG_TAG = "VoiceRecognitionActivity";

    private boolean IsClicked = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_do_diag);

        myTextView = ((TextView) findViewById(R.id.txtSpeechInput));
        myListView = (ListView) findViewById(R.id.listSpeechInput);
        myImageButton = (ImageButton) findViewById(R.id.btnSpeak);
        myProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        myProgressBar.setVisibility(View.INVISIBLE);
        myProgressBar.setMax(10);

        myRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        myRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM); // Considers input in free form
        myRecognizerIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);

        //TODO: 5. Create SpeechRecognizer and set RecognitionListener
        mySpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        mySpeechRecognizer.setRecognitionListener(this);
    }

    public void startRecognitionUI() {
        myProgressBar.setVisibility(View.VISIBLE);
        myProgressBar.setIndeterminate(true);
        myImageButton.setSelected(true);
        IsClicked = true;
    }

    public void endRecognitionUI() {
        myProgressBar.setIndeterminate(false);
        myProgressBar.setVisibility(View.INVISIBLE);
        myImageButton.setSelected(false);
        IsClicked = false;
    }

    @Override
    public void onBeginningOfSpeech() {
        Log.i(LOG_TAG, "onBeginningOfSpeech");
        myProgressBar.setIndeterminate(false);
    }

    @Override
    public void onBufferReceived(byte[] buffer) {
        Log.i(LOG_TAG, "onBufferReceived: " + buffer);
    }

    @Override
    public void onEndOfSpeech() {
        Log.i(LOG_TAG, "onEndOfSpeech");
        endRecognitionUI();
    }

    @Override
    public void onError(int errorCode) {
        String errorMessage = getErrorText(errorCode);
        Log.e(LOG_TAG, "FAILED " + errorMessage);
        myTextView.setText(errorMessage);
        endRecognitionUI();
    }

    @Override
    public void onEvent(int arg0, Bundle arg1) {
        Log.i(LOG_TAG, "onEvent");
    }

    @Override
    public void onResults(Bundle results) {
        Log.i(LOG_TAG, "onResults");

        //TODO 6. Get result list, update UI and analyze result
        ArrayList<String> resultList = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        updateUIElements(resultList);
        analyzeSpeechInput(resultList.get(0));
    }

    @Override
    public void onPartialResults(Bundle results) {
        Log.i(LOG_TAG, "onPartialResults");

        //TODO: 7. Get partial result list and update UI
        ArrayList<String> partialResultList = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        updateUIElements(partialResultList);
    }

    @Override
    public void onReadyForSpeech(Bundle arg0) {
        Log.i(LOG_TAG, "onReadyForSpeech");
    }

    @Override
    public void onRmsChanged(float rmsdB) {
        Log.i(LOG_TAG, "onRmsChanged: " + rmsdB);
        myProgressBar.setProgress((int) rmsdB);
    }

    public void onClick(View v) {
        IsClicked = !IsClicked;

        //TODO: 8. How to add Feedback?
        if(IsClicked) {
            startRecognitionUI();
            mySpeechRecognizer.startListening(myRecognizerIntent);
        } else {
            endRecognitionUI();
            mySpeechRecognizer.stopListening();
        }
    }

    public static String getErrorText(int errorCode) {
        String message;
        switch (errorCode) {
            case SpeechRecognizer.ERROR_AUDIO:
                message = "Audio recording error";
                break;
            case SpeechRecognizer.ERROR_CLIENT:
                message = "Client side error";
                break;
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                message = "Insufficient permissions";
                break;
            case SpeechRecognizer.ERROR_NETWORK:
                message = "Network error";
                break;
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                message = "Network timeout";
                break;
            case SpeechRecognizer.ERROR_NO_MATCH:
                message = "No match";
                break;
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                message = "RecognitionService busy";
                break;
            case SpeechRecognizer.ERROR_SERVER:
                message = "error from server";
                break;
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                message = "No speech input";
                break;
            default:
                message = "Didn't understand, please try again.";
                break;
        }
        return message;
    }

    /**
     * update text and list elements
     */
    void updateUIElements(ArrayList<String> results) {
        myTextView.setText(results.get(0));
        // populate the Matches
        myListView.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1,
                results));
    }

    /**
     * Start searching
     */
    void analyzeSpeechInput(String result) {
        result = result.toLowerCase();
        if (result.contains("suche") || result.contains("search")) {
            startGoogleSearch(result);
        } else if (result.contains("weather") || (result.contains("regenschirm") || (result.contains("wetter")))) {
            startWeatherSearch(result);
        }
    }

    /**
     * Search weather for current location
     */
    private void startWeatherSearch(String searchQuery) {
        Intent search = new Intent(Intent.ACTION_WEB_SEARCH);
        search.putExtra(SearchManager.QUERY, "Wetter");
        startActivity(search);
    }

    /**
     * Start searching
     */
    void startGoogleSearch(String searchQuery) {
        if(searchQuery.contains("search")) searchQuery = searchQuery.replace("search","");
        else if (searchQuery.contains("suche nach")) searchQuery = searchQuery.replace("suche nach","");
        else if (searchQuery.contains("suche")) searchQuery = searchQuery.replace("suche","");

        Intent search = new Intent(Intent.ACTION_WEB_SEARCH);
        search.putExtra(SearchManager.QUERY, searchQuery);
        startActivity(search);
    }
}
