package com.example.focusspot;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

public class FavoritesActivity extends AppCompatActivity {
    RecyclerView recyclerView;
    PlaceAdapter adapter;
    List<Place> allPlaces;
    List<Place> favoritePlaces;
    DBHandler db;
    BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_favorites);

        db = new DBHandler(this);
        recyclerView = findViewById(R.id.recyclerViewFavorites);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_favorites);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                startActivity(new Intent(FavoritesActivity.this, MainActivity.class));
                return true;
            } else if (id == R.id.nav_suggest) {
                startActivity(new Intent(FavoritesActivity.this, SuggestionActivity.class));
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
        bottomNavigationView.setSelectedItemId(R.id.nav_favorites);
        loadFavorites();
    }

    private void loadFavorites() {
        allPlaces = db.getAllPlaces();
        favoritePlaces = new ArrayList<>();
        SharedPreferences sharedPreferences = getSharedPreferences("Favorites", Context.MODE_PRIVATE);

        for (Place place : allPlaces) {
            if (sharedPreferences.getBoolean(place.getName(), false)) {
                favoritePlaces.add(place);
            }
        }

        if (favoritePlaces.isEmpty()) {
            findViewById(R.id.emptyStateLayout).setVisibility(android.view.View.VISIBLE);
            recyclerView.setVisibility(android.view.View.GONE);
        } else {
            findViewById(R.id.emptyStateLayout).setVisibility(android.view.View.GONE);
            recyclerView.setVisibility(android.view.View.VISIBLE);
        }

        adapter = new PlaceAdapter(favoritePlaces);
        recyclerView.setAdapter(adapter);
    }
}
