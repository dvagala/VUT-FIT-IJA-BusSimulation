package gui;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Shape;
import javafx.scene.text.Text;

import java.util.ArrayList;
import java.util.List;

class BusStop extends Coordinate{
//    private Coordinate coordinate = new Coordinate();
    private String name = "def Stop";

    public BusStop(Coordinate coordinate, String name) {
        this.setX(coordinate.getX());
        this.setY(coordinate.getY());
        this.name = name;
    }

    public BusStop() {
    }

    public BusStop(Coordinate coordinate) {
        this.setX(coordinate.getX());
        this.setY(coordinate.getY());
    }


    public Node getUiElement(){
        int size = 6;
        Polygon polygon = new Polygon(this.getX()-size, this.getY() + size, this.getX()+size, this.getY() + size, this.getX(), this.getY() - size );
        polygon.setFill(Color.BLUE);

        VBox vBox = new VBox();
        vBox.getChildren().add(polygon);
        vBox.getChildren().add(new Text(this.getX() - size*4, this.getY() + size*4, name));
        vBox.setLayoutX(this.getX() - size);
        vBox.setLayoutY(this.getY() - size);
        vBox.setAlignment(Pos.TOP_LEFT);

        return vBox;
    }
}
