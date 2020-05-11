package entities;

import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;

import java.util.ArrayList;
import java.util.List;

public class Street{
    private final List<Coordinate> coordinates;
    private Paint color = Color.BLACK;

    public Street(List<Coordinate> coordinates) {
        this.coordinates = new ArrayList(coordinates);
    }

    public void setColor(Paint color) {
        this.color = color;
    }
    public Coordinate getCoordinate(int index) {
        return coordinates.get(index);
    }
    public void addCoordinate(Coordinate coordinate){
        coordinates.add(coordinate);
    }

    public List<Node> getNodes(){

        List<Node> nodes = new ArrayList<>();

        for (Coordinate coordinate : coordinates) {
            nodes.add(new Circle(coordinate.getX(), coordinate.getY(), 7, Color.PINK));
        }

        for (int i = 0; i < coordinates.size()-1; i++) {
            Coordinate first = coordinates.get(i);
            Coordinate second = coordinates.get(i+1);

            Line line = new Line(first.getX(), first.getY(), second.getX(), second.getY());
            line.setStroke(this.color);
            nodes.add(line);
        }

        return nodes;
    }
}
