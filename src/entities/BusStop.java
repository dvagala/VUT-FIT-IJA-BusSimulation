package entities;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.text.Text;

/**
 * Predstavuje autobusovu zastavku.
 * @author Dominik Vagala (xvagal00)
 * @author Jakub Vinš (xvinsj00)
 */
public class BusStop extends Coordinate {

    public String getName() {
        return name;
    }

    private final String name;
    private Node node = null;

    public BusStop(Coordinate coordinate, String name) {
        this.setX(coordinate.getX());
        this.setY(coordinate.getY());
        this.name = name;
    }

    public Node createNode(){
        int size = 6;
        Polygon polygon = new Polygon(this.getX()-size, this.getY() + size, this.getX()+size, this.getY() + size, this.getX(), this.getY() - size );
        polygon.setFill(Color.BLUE);

        VBox vBox = new VBox();
        vBox.getChildren().add(polygon);
        vBox.getChildren().add(new Text(this.getX() - size*4, this.getY() + size*4, name));
        vBox.setLayoutX(this.getX() - size);
        vBox.setLayoutY(this.getY() - size);
        vBox.setAlignment(Pos.TOP_LEFT);

        polygon.setOnMouseClicked(event -> {
            listener.busStopWasClicked(polygon);
            event.consume();
        });

        return vBox;
    }

    public Node getNode(){
        if(node == null){
            node = createNode();
        }

        return node;
    }

    public void setOnBusStopClickListener(OnBusStopClickListener listener) { this.listener = listener;}

    private OnBusStopClickListener listener;

    public interface OnBusStopClickListener {
        void busStopWasClicked(Polygon polygon);
    }
}
