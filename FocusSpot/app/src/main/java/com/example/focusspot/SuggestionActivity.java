package com.example.focusspot;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class SuggestionActivity extends AppCompatActivity {

    Button submit, cancel;
    EditText nameet, locationet, descet;
    Spinner categorySpinner;
    CheckBox wifi;
    DBHandler db;
    BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_suggestion);

        db = new DBHandler(this);

        categorySpinner = findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.category_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(adapter);

        nameet = findViewById(R.id.editTextText6);
        locationet = findViewById(R.id.editTextText7);
        descet = findViewById(R.id.editTextText8);
        wifi = findViewById(R.id.checkBox);

        submit = findViewById(R.id.submit);
        cancel = findViewById(R.id.cancel);

        submit.setOnClickListener(v -> {
            String name = nameet.getText().toString();
            String location = locationet.getText().toString();
            String description = descet.getText().toString();
            String isWifi = wifi.isChecked() ? "Yes" : "No";
            String category = categorySpinner.getSelectedItem().toString();

            if (name.isEmpty() || location.isEmpty() || description.isEmpty()) {
                Toast.makeText(this, "Please fill all text boxes.", Toast.LENGTH_SHORT).show();
            } else {
                Place newPlace = new Place(name, "Unknown", "Unknown", "Unknown", isWifi, description, location, category, R.mipmap.logo);
                db.addSuggestion(newPlace);
                Toast.makeText(this, "Suggestion sent for moderation!", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        cancel.setOnClickListener(v -> finish());

        bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_suggest);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                startActivity(new Intent(SuggestionActivity.this, MainActivity.class));
                return true;
            } else if (id == R.id.nav_favorites) {
                startActivity(new Intent(SuggestionActivity.this, FavoritesActivity.class));
                return true;
            }
            return true;
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        bottomNavigationView.setSelectedItemId(R.id.nav_suggest);
    }
}
