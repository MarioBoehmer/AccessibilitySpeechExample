package devorama.marioboehmer.accessibilityspeechexample;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 * Simple demo for better accessibility. Serves as an example for text to speech capabilities of android which is supported
 * from version 1.6 upwards and for speech recognition.
 * 
 * @author mboehmer
 */
public class SpeechActivity extends Activity implements OnInitListener {
	private Button speakButton;
	private Button listenButton;
	private EditText editText;
	private TextToSpeech mTts;
	private static final int TTS_ACTIVITY_RESULT_CODE = 1;
	private static final int SPEECH_RECOGNITION_ACTIVITY_RESULT_CODE = 2;
	private boolean tTSInitialzed;
	private boolean speechRecognitionInstalled;
	private Context context = this;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		speakButton = (Button) findViewById(R.id.speak_button);
		listenButton = (Button) findViewById(R.id.listen_button);
		editText = (EditText) findViewById(R.id.edittext);
		
		// check for TTS
		Intent checkTTSIntent = new Intent();
		checkTTSIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
		startActivityForResult(checkTTSIntent, TTS_ACTIVITY_RESULT_CODE);
		
		// check for speech recognition
		PackageManager pm = getPackageManager();
		List<ResolveInfo> activities = pm.queryIntentActivities(
		  new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), PackageManager.MATCH_DEFAULT_ONLY);
		if (activities.size() != 0) {
			speechRecognitionInstalled = true;
		}

		speakButton.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				if (tTSInitialzed) {
					// check if your locale is supported by tts
					if (mTts.isLanguageAvailable(Locale.getDefault()) == TextToSpeech.LANG_COUNTRY_AVAILABLE) {
						mTts.setLanguage(Locale.getDefault());
						mTts.speak(editText.getText().toString(),
								TextToSpeech.QUEUE_FLUSH, null);
						// use the tts queue for following texts
						// mTts.speak(editText.getText().toString(),
						// TextToSpeech.QUEUE_ADD, null);
						// or flush the queue to cancel current spoken text and
						// start new text
						// mTts.speak(editText.getText().toString(),
						// TextToSpeech.QUEUE_FLUSH, null);
					} else {
						Toast localeNotSupportedToast = Toast.makeText(context,
								R.string.locale_not_supported_text,
								Toast.LENGTH_SHORT);
						localeNotSupportedToast.show();
					}
				} else {
					Toast ttsNotInitializedToast = Toast.makeText(context,
							R.string.tts_not_initialized_text,
							Toast.LENGTH_SHORT);
					ttsNotInitializedToast.show();
				}
			}
		});
		
		listenButton.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				if(speechRecognitionInstalled) {
					Intent recognizeSpeechIntent = new Intent();
					recognizeSpeechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
			                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
					recognizeSpeechIntent.putExtra(RecognizerIntent.EXTRA_PROMPT, getText(R.string.speech_recognition_prompt_text));
					recognizeSpeechIntent.setAction(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
					startActivityForResult(recognizeSpeechIntent, SPEECH_RECOGNITION_ACTIVITY_RESULT_CODE);
				}
			}
		});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == TTS_ACTIVITY_RESULT_CODE) {
			if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
				// success, create the TTS instance
				mTts = new TextToSpeech(this, this);
			} else {
				// missing data, install it
				Intent installIntent = new Intent();
				installIntent
						.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
				startActivity(installIntent);
			}
		} else if (requestCode == SPEECH_RECOGNITION_ACTIVITY_RESULT_CODE) {
			ArrayList<String> matches = data.getStringArrayListExtra(
                    RecognizerIntent.EXTRA_RESULTS);
			if(matches.size() != 0) {
				// we only want to show the best matching result
				editText.setText(matches.get(0));
			} else {
				Toast speechNotRecognized = Toast.makeText(context,
						R.string.speech_not_recognized_text,
						Toast.LENGTH_SHORT);
				speechNotRecognized.show();
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onInit(int status) {
		if (status == TextToSpeech.SUCCESS) {
			tTSInitialzed = true;
		}
	}

	@Override
	protected void onDestroy() {
		if (mTts != null) {
			mTts.shutdown();
		}
		super.onDestroy();
	}
}