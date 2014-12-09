package org.doubango.imsdroid;

public class Point {

	float x, y;
    float dx, dy;
    private int index,compass;
    private boolean beSelected;
    
    public boolean isBeSelected() {
		return beSelected;
	}

	public void setBeSelected(boolean beSelected) {
		this.beSelected = beSelected;
	}

	public int getCompass() {
		return compass;
	}

	public void setCompass(int compass) {
		this.compass = compass;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	@Override
    public String toString() {
        return x + ", " + y;
    }
}
