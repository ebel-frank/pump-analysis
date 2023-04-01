package com.ebel_frank.pumpanalysis.model;

import java.util.List;

public class ThinkSpeakData {
    private String pumpStatus;
    private List<Float> waterLvlList;

    public ThinkSpeakData(List<Float> waterLvlList, String pumpStatus) {
        this.pumpStatus = pumpStatus;
        this.waterLvlList = waterLvlList;
    }

    public float getWaterLevel() {
        return waterLvlList.get(waterLvlList.size()-1);
    }

    public void setWaterLevel(float waterLevel) {
        this.waterLvlList.set(waterLvlList.size()-1, waterLevel);
    }

    public String getPumpStatus() {
        return pumpStatus;
    }

    public void setPumpStatus(String pumpStatus) {
        this.pumpStatus = pumpStatus;
    }

    public List<Float> getWaterLvlList() {
        return waterLvlList;
    }

    public void setWaterLvlList(List<Float> waterLvlList) {
        this.waterLvlList = waterLvlList;
    }
}
