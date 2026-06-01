package com.example.focusspot;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class PlaceInfoActivity extends AppCompatActivity {

    TextView placeName, placeDescription, placeNoise, placeSpace, placeCrowd, placeWifi, placeCategory, suitabilityTag;
    ImageView placeImage, favoriteBtn;
    Button back, updateStatus, viewOnMaps;
    DBHandler db;
    String name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_place_info);

        db = new DBHandler(this);

        placeName = findViewById(R.id.place_name);
        placeDescription = findViewById(R.id.place_description);
        placeNoise = findViewById(R.id.place_noise);
        placeSpace = findViewById(R.id.place_space);
        placeCrowd = findViewById(R.id.place_crowd);
        placeWifi = findViewById(R.id.place_wifi);
        placeCategory = findViewById(R.id.place_category);
        suitabilityTag = findViewById(R.id.suitability_tag_info);
        placeImage = findViewById(R.id.place_image_info);
        favoriteBtn = findViewById(R.id.favorite_btn_info);
        updateStatus = findViewById(R.id.update_status);
        viewOnMaps = findViewById(R.id.view_on_maps);

        name = getIntent().getStringExtra("name");

        SharedPreferences sharedPreferences = getSharedPreferences("Favorites", Context.MODE_PRIVATE);
        updateFavoriteIcon(sharedPreferences.getBoolean(name, false));

        favoriteBtn.setOnClickListener(v -> {
            boolean currentFav = sharedPreferences.getBoolean(name, false);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(name, !currentFav);
            editor.apply();
            updateFavoriteIcon(!currentFav);
        });

        back = findViewById(R.id.back);
        back.setOnClickListener(v -> finish());

        updateStatus.setOnClickListener(v -> {
            Intent intent = new Intent(this, UpdateLevelsActivity.class);
            intent.putExtra("name", name);
            startActivity(intent);
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void updateFavoriteIcon(boolean isFavorite) {
        if (isFavorite) {
            favoriteBtn.setImageResource(android.R.drawable.btn_star_big_on);
        } else {
            favoriteBtn.setImageResource(android.R.drawable.btn_star_big_off);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPlaceData();
    }

    private void loadPlaceData() {
        Place place = db.getPlaceByName(name);
        if (place != null) {
            placeName.setText(place.getName());
            placeDescription.setText("Description: " + place.getDescription());
            placeNoise.setText("Noise Level: " + place.getNoise());
            placeCrowd.setText("Crowd Level: " + place.getCrowd());
            placeSpace.setText("Space Availability: " + place.getSpace());
            placeCategory.setText(place.getCategory() + " · " + place.getLocation());
            placeWifi.setText("WiFi: " + place.getWifi());
            placeImage.setImageResource(place.getImageResId());

            String suitability = getSuitability(place);
            suitabilityTag.setText(suitability);
            if (suitability.equals("Suitable")) {
                suitabilityTag.setBackgroundResource(R.drawable.tag_green);
            } else if (suitability.equals("Moderate")) {
                suitabilityTag.setBackgroundResource(R.drawable.tag_yellow);
            } else if (suitability.equals("Status Unknown")) {
                suitabilityTag.setBackgroundColor(android.graphics.Color.GRAY);
            } else {
                suitabilityTag.setBackgroundResource(R.drawable.tag_red);
            }

            viewOnMaps.setOnClickListener(v -> {
                Uri gmmIntentUri = Uri.parse("geo:0,0?q=" + Uri.encode(place.getLocation()));
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                if (mapIntent.resolveActivity(getPackageManager()) != null) {
                    startActivity(mapIntent);
                } else {
                    startActivity(new Intent(Intent.ACTION_VIEW, gmmIntentUri));
                }
            });
        }
    }

    private String getSuitability(Place place) {
        String noise = place.getNoise();
        String crowd = place.getCrowd();
        String space = place.getSpace();

        if (noise == null) noise = "";
        if (crowd == null) crowd = "";
        if (space == null) space = "";

        noise = noise.trim().toLowerCase();
        crowd = crowd.trim().toLowerCase();
        space = space.trim().toLowerCase();

        if (noise.equals("unknown") || crowd.equals("unknown") || space.equals("unknown") ||
                noise.isEmpty() || crowd.isEmpty() || space.isEmpty()) {
            return "Status Unknown";
        }

        if (space.equals("full") || crowd.equals("high") || noise.equals("loud")) {
            return "Not Suitable";
        }

        if (space.equals("limited") || crowd.equals("moderate") || noise.equals("medium")) {
            return "Moderate";
        }

        return "Suitable";
    }
}
