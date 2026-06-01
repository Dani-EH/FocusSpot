package com.example.focusspot;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.io.IOException;

public class UpdateLevelsActivity extends AppCompatActivity {

    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private boolean permissionToRecordAccepted = false;
    private final String[] permissions = {Manifest.permission.RECORD_AUDIO};

    TextView title;
    Spinner spinnerNoise, spinnerCrowd, spinnerSpace;
    Button btnDetect, btnSubmit;
    MediaRecorder recorder = null;
    DBHandler db;
    Place currentPlace;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_levels);

        db = new DBHandler(this);

        title = findViewById(R.id.update_title);
        spinnerNoise = findViewById(R.id.spinner_noise);
        spinnerCrowd = findViewById(R.id.spinner_crowd);
        spinnerSpace = findViewById(R.id.spinner_space);
        btnDetect = findViewById(R.id.btn_detect_noise);
        btnSubmit = findViewById(R.id.btn_submit_update);
        Button btnCancel = findViewById(R.id.button);

        String placeName = getIntent().getStringExtra("name");
        currentPlace = db.getPlaceByName(placeName);
        
        if (currentPlace != null) {
            title.setText(currentPlace.getName());
        }

        setupSpinners();

        btnDetect.setOnClickListener(v -> {
            ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION);
        });

        btnSubmit.setOnClickListener(v -> {
            if (currentPlace != null) {
                currentPlace.setNoise(spinnerNoise.getSelectedItem().toString());
                currentPlace.setCrowd(spinnerCrowd.getSelectedItem().toString());
                currentPlace.setSpace(spinnerSpace.getSelectedItem().toString());
                
                db.updatePlace(currentPlace);
                Toast.makeText(this, "Levels updated successfully!", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        btnCancel.setOnClickListener(v -> finish());
    }

    private void setupSpinners() {
        ArrayAdapter<CharSequence> noiseAdapter = ArrayAdapter.createFromResource(this,
                R.array.noise_array, android.R.layout.simple_spinner_item);
        noiseAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerNoise.setAdapter(noiseAdapter);

        ArrayAdapter<CharSequence> crowdAdapter = ArrayAdapter.createFromResource(this,
                R.array.crowd_array, android.R.layout.simple_spinner_item);
        crowdAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCrowd.setAdapter(crowdAdapter);

        ArrayAdapter<CharSequence> spaceAdapter = ArrayAdapter.createFromResource(this,
                R.array.space_array, android.R.layout.simple_spinner_item);
        spaceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSpace.setAdapter(spaceAdapter);
        

        if (currentPlace != null) {
            spinnerNoise.setSelection(getSpinnerIndex(spinnerNoise, currentPlace.getNoise()));
            spinnerCrowd.setSelection(getSpinnerIndex(spinnerCrowd, currentPlace.getCrowd()));
            spinnerSpace.setSelection(getSpinnerIndex(spinnerSpace, currentPlace.getSpace()));
        }
    }

    private int getSpinnerIndex(Spinner spinner, String value) {
        for (int i = 0; i < spinner.getCount(); i++) {
            if (spinner.getItemAtPosition(i).toString().equalsIgnoreCase(value)) {
                return i;
            }
        }
        return 0;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            permissionToRecordAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
        }
        if (permissionToRecordAccepted) {
            startNoiseDetection();
        } else {
            Toast.makeText(this, "Permission denied to record audio", Toast.LENGTH_SHORT).show();
        }
    }

    private void startNoiseDetection() {
        Toast.makeText(this, "Detecting noise level...", Toast.LENGTH_SHORT).show();
        
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        recorder.setOutputFile(getExternalCacheDir().getAbsolutePath() + "/test.3gp");

        try {
            recorder.prepare();
            recorder.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

        new Handler().postDelayed(() -> {
            if (recorder != null) {
                double amplitude = recorder.getMaxAmplitude();
                recorder.stop();
                recorder.release();
                recorder = null;
                updateNoiseSpinner(amplitude);
            }
        }, 3000);
    }

    private void updateNoiseSpinner(double amplitude) {
        int noiseLevel;
        if (amplitude < 1000) {
            noiseLevel = 0;
        } else if (amplitude < 5000) {
            noiseLevel = 1;
        } else {
            noiseLevel = 2;
        }
        spinnerNoise.setSelection(noiseLevel);
        Toast.makeText(this, "Noise level detected!", Toast.LENGTH_SHORT).show();
    }
}
