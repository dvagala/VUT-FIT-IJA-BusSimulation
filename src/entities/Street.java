package entities;

import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import javafx.scene.transform.Rotate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Street{

    private final List<Coordinate> coordinates;
    private Color color = Color.BLACK;
    private int trafficRate = 1;
    private String name;
    private Listener listener;

    public Street(List<Coordinate> coordinates, String name) {
        this.coordinates = new ArrayList<>(coordinates);
        this.name = name;
    }

    public int getTrafficRate() {
        return trafficRate;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public Coordinate getCoordinate(int index) {
        return coordinates.get(index);
    }

    private Node node = null;

    private Text streetNameTextNode = null;

    private ContextMenu setUpContextMenu(){
        final ContextMenu contextMenu = new ContextMenu();
        List<MenuItem> menuItems = Arrays.asList(
                new MenuItem("Traffic 1x"),
                new MenuItem("Traffic 2x"),
                new MenuItem("Traffic 3x"),
                new MenuItem("Traffic 4x"),
                new MenuItem("Traffic 5x"),
                new MenuItem("Traffic 6x"),
                new MenuItem("Traffic 7x"),
                new MenuItem("Traffic 8x"),
                new MenuItem("Traffic 9x"),
                new MenuItem("Traffic 10x")
        );

        for (int i = 0; i < menuItems.size(); i++) {
            int finalI = i;
            menuItems.get(i).setOnAction(event -> {
                Platform.runLater(() -> {
                    if(finalI == 0){
                        streetNameTextNode.setText(name);
                    }else{
                        streetNameTextNode.setText(name + " traffic: " + (finalI+1) + "x");
                    }
                });
                trafficRate = finalI + 1;
                listener.trafficHasChanged(trafficRate);
            });
        }
        contextMenu.getItems().addAll(menuItems);

        return contextMenu;
    }

    public Node createNode(){
        Pane pane = new Pane();

        pane.setLayoutX(0);
        pane.setLayoutY(0);

        ContextMenu contextMenu = setUpContextMenu();
        for (Coordinate coordinate : coordinates) {
            Circle c = new Circle(coordinate.getX(), coordinate.getY(), 7, Color.PINK);

            c.setMouseTransparent(false);
            c.setOnMouseClicked(event -> {
                listener.streetEndingCircleWasClicked(c);
                event.consume();
            });

            pane.getChildren().add(c);
        }
        for (int i = 0; i < coordinates.size()-1; i++) {
            Coordinate first = coordinates.get(i);
            Coordinate second = coordinates.get(i+1);

            if(i == coordinates.size()/2-1 && streetNameTextNode == null){
                streetNameTextNode = new Text(first.getX()+(second.getX()-first.getX())/2 -30, first.getY()+(second.getY()-first.getY())/2 -4, this.name);
                streetNameTextNode.getTransforms().add(new Rotate(getAngle(first, second), first.getX()+(second.getX()-first.getX())/2, first.getY()+(second.getY()-first.getY())/2));
                streetNameTextNode.setStyle("-fx-font: 10 arial;");
                streetNameTextNode.setOnMouseClicked(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent event) {
                        if (event.getButton() == MouseButton.SECONDARY) {
                            Platform.runLater(() -> contextMenu.show(pane, event.getScreenX(), event.getScreenY()));
                        }else if (event.getButton() == MouseButton.PRIMARY) {
                            listener.streetNameWasClicked(streetNameTextNode);
                        }
                        event.consume();
                    }
                });
                pane.getChildren().add(streetNameTextNode);
            }

            Line line = new Line(first.getX(), first.getY(), second.getX(), second.getY());
            line.setStroke(this.color);
            pane.getChildren().add(line);
        }
        pane.setPickOnBounds(false);

        return pane;
    }

    public float getAngle(Coordinate coordinate1, Coordinate coordinate2) {
        float angle = (float) Math.toDegrees(Math.atan2(coordinate2.getY() - coordinate1.getY(), coordinate2.getX() - coordinate1.getX()));

        if(angle < 0){
            angle += 360;
        }

        return angle;
    }

    public Node getNode(){
        if(node == null){
            node = createNode();
        }

        return node;
    }

    public void setOnTrafficChangeListener(Listener listener) {
        this.listener = listener;
    }

    public interface Listener{
        void trafficHasChanged(int trafficRate);
        void streetEndingCircleWasClicked(Circle c);
        void streetNameWasClicked(Text streetNameTextNode);
    }
}
