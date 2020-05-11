package gui;

import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Shape;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class Street implements IMapElement{
    private List<Coordinate> coordinates = new ArrayList<>();

    public void setColor(Paint color) {
        this.color = color;
    }

    private Paint color = Color.BLACK;

    public Street() {
    }

    public Street(List<Coordinate> coordinates) {
        this.coordinates = new ArrayList(coordinates);
    }

    public Coordinate getCoordinate(int index) {
        return coordinates.get(index);
    }

    public List<Coordinate> getCoordinates() {
        return coordinates;
    }

    public void addCoordinate(Coordinate coordinate){
        coordinates.add(coordinate);
    }

    public List<Node> getUiElements(){

        List<Node> shapes = new ArrayList<>();

        for (Coordinate coordinate : coordinates) {
            shapes.add(new Circle(coordinate.getX(), coordinate.getY(), 7, Color.PINK));
        }

        for (int i = 0; i < coordinates.size()-1; i++) {
            Coordinate first = coordinates.get(i);
            Coordinate second = coordinates.get(i+1);

            Line line = new Line(first.getX(), first.getY(), second.getX(), second.getY());
            line.setStroke(this.color);
            shapes.add(line);
        }

        return shapes;
    }
}
