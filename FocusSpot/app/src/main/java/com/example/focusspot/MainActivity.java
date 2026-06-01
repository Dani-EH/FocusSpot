package com.example.focusspot;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import android.widget.ImageButton;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    RecyclerView recyclerView;
    PlaceAdapter adapter;
    List<Place> placeList;
    DBHandler db;
    SearchView searchView;
    BottomNavigationView bottomNavigationView;
    ImageButton btnFilter;
    FilterCriteria filterCriteria = new FilterCriteria();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);


        db = new DBHandler(this);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        searchView = findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (adapter != null) {
                    adapter.getFilter().filter(newText);
                }
                return false;
            }
        });

        bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_home);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_favorites) {
                startActivity(new Intent(MainActivity.this, FavoritesActivity.class));
                return true;
            } else if (id == R.id.nav_suggest) {
                startActivity(new Intent(MainActivity.this, SuggestionActivity.class));
                return true;
            }
            return true;
        });

        btnFilter = findViewById(R.id.btnFilter);
        btnFilter.setOnClickListener(v -> showFilterBottomSheet());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        bottomNavigationView.setSelectedItemId(R.id.nav_home);
        loadPlaces();
    }

    private void loadPlaces() {
        placeList = db.getAllPlaces();
        
        if (placeList.isEmpty()) {
            db.addPlace(new Place("Stories", "Loud", "Moderate", "Limited", "Yes", "A popular café for working or studying, but it can get noisy during busy hours.", "Stories Aley", "Cafe", R.mipmap.stories));
            db.addPlace(new Place("Library", "Quiet", "Low", "Available", "Yes", "A quiet study-friendly place with good space availability and a calm atmosphere.", "Public Library", "Library", R.mipmap.logo));
            db.addPlace(new Place("Cafe X", "Medium", "Moderate", "Full", "No", "A casual café that is sometimes good for work, but it may become crowded.", "Cafe X", "Cafe", R.mipmap.logo));
            placeList = db.getAllPlaces();
        }

        adapter = new PlaceAdapter(placeList);
        recyclerView.setAdapter(adapter);
        adapter.setFilterCriteria(filterCriteria);
    }

    private void showFilterBottomSheet() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        bottomSheetDialog.setContentView(R.layout.filter_bottom_sheet);

        ChipGroup noiseGroup = bottomSheetDialog.findViewById(R.id.noiseChipGroup);
        ChipGroup crowdGroup = bottomSheetDialog.findViewById(R.id.crowdChipGroup);
        ChipGroup spaceGroup = bottomSheetDialog.findViewById(R.id.spaceChipGroup);

        if (filterCriteria.getNoise() != null) {
            for (int i = 0; i < noiseGroup.getChildCount(); i++) {
                Chip chip = (Chip) noiseGroup.getChildAt(i);
                if (chip.getText().toString().equalsIgnoreCase(filterCriteria.getNoise())) {
                    chip.setChecked(true);
                }
            }
        }
        if (filterCriteria.getCrowd() != null) {
            for (int i = 0; i < crowdGroup.getChildCount(); i++) {
                Chip chip = (Chip) crowdGroup.getChildAt(i);
                if (chip.getText().toString().equalsIgnoreCase(filterCriteria.getCrowd())) {
                    chip.setChecked(true);
                }
            }
        }
        if (filterCriteria.getSpace() != null) {
            for (int i = 0; i < spaceGroup.getChildCount(); i++) {
                Chip chip = (Chip) spaceGroup.getChildAt(i);
                if (chip.getText().toString().equalsIgnoreCase(filterCriteria.getSpace())) {
                    chip.setChecked(true);
                }
            }
        }

        bottomSheetDialog.findViewById(R.id.btnApply).setOnClickListener(v -> {
            int noiseId = noiseGroup.getCheckedChipId();
            if (noiseId != View.NO_ID) {
                filterCriteria.setNoise(((Chip) noiseGroup.findViewById(noiseId)).getText().toString());
            } else {
                filterCriteria.setNoise(null);
            }

            int crowdId = crowdGroup.getCheckedChipId();
            if (crowdId != View.NO_ID) {
                filterCriteria.setCrowd(((Chip) crowdGroup.findViewById(crowdId)).getText().toString());
            } else {
                filterCriteria.setCrowd(null);
            }

            int spaceId = spaceGroup.getCheckedChipId();
            if (spaceId != View.NO_ID) {
                filterCriteria.setSpace(((Chip) spaceGroup.findViewById(spaceId)).getText().toString());
            } else {
                filterCriteria.setSpace(null);
            }

            adapter.setFilterCriteria(filterCriteria);
            bottomSheetDialog.dismiss();
        });

        bottomSheetDialog.findViewById(R.id.btnReset).setOnClickListener(v -> {
            filterCriteria = new FilterCriteria();
            adapter.setFilterCriteria(filterCriteria);
            bottomSheetDialog.dismiss();
        });

        bottomSheetDialog.show();
    }
}
