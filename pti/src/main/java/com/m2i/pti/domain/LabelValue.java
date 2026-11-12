package com.m2i.pti.domain;

import java.io.Serializable;

public class LabelValue implements Serializable {
    String label;
    int value;

    public LabelValue(String label, int value) {
        this.label = label;
        this.value = value;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }
}
