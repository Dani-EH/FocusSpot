package com.example.focusspot;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class PlaceAdapter extends RecyclerView.Adapter<PlaceAdapter.ViewHolder> implements Filterable {
    private List<Place> placeList;
    private List<Place> placeListFull;
    private FilterCriteria currentCriteria = new FilterCriteria();
    private String currentQuery = "";

    public PlaceAdapter(List<Place> placeList) {
        this.placeList = placeList;
        this.placeListFull = new ArrayList<>(placeList);
    }

    public void setFilterCriteria(FilterCriteria criteria) {
        this.currentCriteria = criteria;
        getFilter().filter(currentQuery);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, suitabilityTag, category, metrics;
        ImageView image, favorite;

        public ViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.placeName);
            suitabilityTag = itemView.findViewById(R.id.suitabilityTag);
            category = itemView.findViewById(R.id.placeCategory);
            metrics = itemView.findViewById(R.id.placeMetrics);
            image = itemView.findViewById(R.id.placeImage);
            favorite = itemView.findViewById(R.id.favoriteIcon);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_place, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Place place = placeList.get(position);
        holder.name.setText(place.getName());
        holder.category.setText(place.getCategory() + " · " + place.getLocation());
        holder.metrics.setText("Noise: " + place.getNoise() + "  Crowd: " + place.getCrowd() + "  Space: " + place.getSpace());

        try {
            holder.image.setImageResource(place.getImageResId());
        } catch (android.content.res.Resources.NotFoundException e) {
            holder.image.setImageResource(R.mipmap.logo);
        }

        Context context = holder.itemView.getContext();
        SharedPreferences sharedPreferences = context.getSharedPreferences("Favorites", Context.MODE_PRIVATE);
        boolean isFavorite = sharedPreferences.getBoolean(place.getName(), false);

        if (isFavorite) {
            holder.favorite.setImageResource(android.R.drawable.btn_star_big_on);
        } else {
            holder.favorite.setImageResource(android.R.drawable.btn_star_big_off);
        }

        holder.favorite.setOnClickListener(v -> {
            boolean currentFav = sharedPreferences.getBoolean(place.getName(), false);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(place.getName(), !currentFav);
            editor.apply();
            notifyItemChanged(position);
        });

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), PlaceInfoActivity.class);
            intent.putExtra("name", place.getName());
            v.getContext().startActivity(intent);
        });

        String suitability = getSuitability(place);
        holder.suitabilityTag.setText(suitability);
        holder.suitabilityTag.setTextColor(android.graphics.Color.WHITE);

        if (suitability.equals("Suitable")) {
            holder.suitabilityTag.setBackgroundResource(R.drawable.tag_green);
        } else if (suitability.equals("Moderate")) {
            holder.suitabilityTag.setBackgroundResource(R.drawable.tag_yellow);
        } else if (suitability.equals("Status Unknown")) {
            holder.suitabilityTag.setBackgroundColor(android.graphics.Color.GRAY);
        } else {
            holder.suitabilityTag.setBackgroundResource(R.drawable.tag_red);
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

        if (noise.equals("unknown") ||
                crowd.equals("unknown") ||
                space.equals("unknown") ||
                noise.isEmpty() ||
                crowd.isEmpty() ||
                space.isEmpty()) {
            return "Status Unknown";
        }

        if (space.equals("full") ||
                crowd.equals("high") ||
                noise.equals("loud")) {
            return "Not Suitable";
        }

        if (space.equals("limited") ||
                crowd.equals("moderate") ||
                noise.equals("medium")) {
            return "Moderate";
        }

        return "Suitable";
    }

    @Override
    public int getItemCount() {
        return placeList.size();
    }

    @Override
    public Filter getFilter() {
        return placeFilter;
    }

    private Filter placeFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            currentQuery = constraint == null ? "" : constraint.toString();
            List<Place> filteredList = new ArrayList<>();

            String filterPattern = currentQuery.toLowerCase().trim();

            for (Place item : placeListFull) {
                boolean matchesSearch = item.getName().toLowerCase().contains(filterPattern) ||
                        item.getCategory().toLowerCase().contains(filterPattern) ||
                        item.getLocation().toLowerCase().contains(filterPattern);;
                boolean matchesNoise = currentCriteria.getNoise() == null || currentCriteria.getNoise().isEmpty() || item.getNoise().equalsIgnoreCase(currentCriteria.getNoise());
                boolean matchesCrowd = currentCriteria.getCrowd() == null || currentCriteria.getCrowd().isEmpty() || item.getCrowd().equalsIgnoreCase(currentCriteria.getCrowd());
                boolean matchesSpace = currentCriteria.getSpace() == null || currentCriteria.getSpace().isEmpty() || item.getSpace().equalsIgnoreCase(currentCriteria.getSpace());

                if (matchesSearch && matchesNoise && matchesCrowd && matchesSpace) {
                    filteredList.add(item);
                }
            }

            FilterResults results = new FilterResults();
            results.values = filteredList;
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            placeList.clear();
            placeList.addAll((List) results.values);
            notifyDataSetChanged();
        }
    };
}
