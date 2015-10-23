package model;

public class Map extends TableItem {

    double size;

    public Map(String name, double size) {
        this.title = name;
        this.size = size;
    }

    public double getSize() {
        return size;
    }

    public void setSize(double size) {
        this.size = size;
    }
}
