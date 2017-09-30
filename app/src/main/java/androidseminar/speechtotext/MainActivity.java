package androidseminar.speechtotext;

import android.app.SearchManager;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private TextView myTextView;
    private ListView myListView;
    private final int REQ_CODE_SPEECH_INPUT = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        myTextView = ((TextView) findViewById(R.id.txtSpeechInput));
        myListView = (ListView) findViewById(R.id.listSpeechInput);
    }

    public void onClick(View v) {
        //0. check if recognition is available on this device
        if (SpeechRecognizer.isRecognitionAvailable(this)) promptSpeechInput();
        else showToastMessage(getResources().getString(R.string.speech_not_supported));
    }


    /**
     * Showing google speech input dialog and start speech recognizer
     */
    private void promptSpeechInput() {
        //TODO: 1. create new intent which shows mic dialog box to recognize speech input
        Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH); // Simply takes user’s speech input and returns it to same activity

        //1.1. add some extra parameters
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM); // Considers input in free form
        i.putExtra(RecognizerIntent.EXTRA_PROMPT, R.string.speech_prompt); // text hint for the user to speak

        try {
            //TODO: 2. start the speech recognizer intent using startActivityForResult()
            startActivityForResult(i, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException e) {
            showToastMessage(getResources().getString(R.string.speech_not_supported));
        }
    }

    /**
     * Receiving speech input
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // check if the request code is equal to ours
        if(requestCode == REQ_CODE_SPEECH_INPUT) {

            // If Voice recognition is successful then it returns RESULT_OK
            if (resultCode == RESULT_OK) {

                if (data != null) {
                    // TODO: 3. Get result list
                    ArrayList<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

                    // 3.1 Update UI elements with our result list
                    updateUIElements(results);

                    // 3.2 Analyze result string
                    analyzeSpeechInput(results.get(0));
                }

            //Result code for various error.
            } else if(resultCode == RecognizerIntent.RESULT_AUDIO_ERROR){
                showToastMessage("Audio Error");
            } else if(resultCode == RecognizerIntent.RESULT_CLIENT_ERROR){
                showToastMessage("Client Error");
            } else if(resultCode == RecognizerIntent.RESULT_NETWORK_ERROR){
                showToastMessage("Network Error");
            } else if(resultCode == RecognizerIntent.RESULT_NO_MATCH){
                showToastMessage("No Match");
            } else if(resultCode == RecognizerIntent.RESULT_SERVER_ERROR){
                showToastMessage("Server Error");
            }
        }
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
     * analyze result string
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

    /**
     * Helper method to show the error messages
     **/
    void showToastMessage(String message){
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
