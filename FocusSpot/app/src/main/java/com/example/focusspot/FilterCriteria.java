package com.example.focusspot;

public class FilterCriteria {
    private String noise;
    private String crowd;
    private String space;

    public FilterCriteria() {}

    public FilterCriteria(String noise, String crowd, String space) {
        this.noise = noise;
        this.crowd = crowd;
        this.space = space;
    }

    public String getNoise() { return noise; }
    public void setNoise(String noise) { this.noise = noise; }

    public String getCrowd() { return crowd; }
    public void setCrowd(String crowd) { this.crowd = crowd; }

    public String getSpace() { return space; }
    public void setSpace(String space) { this.space = space; }

    public boolean isEmpty() {
        return (noise == null || noise.isEmpty()) &&
               (crowd == null || crowd.isEmpty()) &&
               (space == null || space.isEmpty());
    }
}
