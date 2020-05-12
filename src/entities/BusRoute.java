package entities;


import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Line;
import misc.ColorHelper;
import sun.nio.fs.GnomeFileTypeDetector;

import java.util.ArrayList;
import java.util.List;

public class BusRoute {

    private final List<IRoutePoint> routePoints;
    private List<BusStop> stackPane = new ArrayList<>();
    private List<RouteSchedule> routeSchedules = new ArrayList<>();
    private Color color = Color.rgb(255, 0, 0);
    private final int routeNumber;
    private Node node = null;

    public List<BusStop> getStops() {
        return stackPane;
    }

    public Color getColor() {
        return color;
    }

    public int getRouteNumber() {
        return routeNumber;
    }

    public BusRoute(int routeNumber, List<IRoutePoint> routePoints) {
        this.routeNumber = routeNumber;
        this.routePoints = routePoints;

        for(IRoutePoint routePoint : routePoints){
            if(routePoint instanceof BusStop){
                stackPane.add((BusStop) routePoint);
            }
        }
    }

    public List<RouteSchedule> getRouteSchedules() {
        return routeSchedules;
    }

    public void setRouteSchedules(List<RouteSchedule> routeSchedules) {
        this.routeSchedules = routeSchedules;
    }

    public void addPoint(IRoutePoint routePoint){
        routePoints.add(routePoint);
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public Node createNode(){

        Pane pane = new Pane();

        pane.setLayoutX(0);
        pane.setLayoutY(0);

        for (int i = 0; i < routePoints.size()-1; i++) {
            IRoutePoint first = routePoints.get(i);
            IRoutePoint second = routePoints.get(i+1);

            Line line = new Line(first.getX(), first.getY(), second.getX(), second.getY());
            line.setStroke(ColorHelper.getLighterColor(color, 0.3));
            line.setStrokeWidth(7);
            pane.getChildren().add(line);
        }
        pane.setOpacity(0.0);

        return pane;
    }

    public Node getNode(){
        if(node == null){
            node = createNode();
        }
        return node;
    }

    public Coordinate getCoordinateByDistance(double distance){
        double length = 0;

        IRoutePoint a = null;
        IRoutePoint b = null;
        for (int i = 0; i < routePoints.size() - 1; i++) {
            a = routePoints.get(i);
            b = routePoints.get(i+1);

            if(length + getDistanceBetweenRoutePoints(a, b) >= distance){
                break;
            }
            length += getDistanceBetweenRoutePoints(a,b);
        }

        if(a == null || b == null){
            return null;
        }

        double driven = (distance - length) / getDistanceBetweenRoutePoints(a,b);
        return new Coordinate((int) (a.getX() + (b.getX() - a.getX()) * driven), (int) (a.getY() + (b.getY() - a.getY())*driven));
    }

    public static double getDistanceBetweenRoutePoints(IRoutePoint firstRoutePoint, IRoutePoint secondRoutePoint){
        int a = Math.abs(firstRoutePoint.getX() - secondRoutePoint.getX());
        int b = Math.abs(firstRoutePoint.getY() - secondRoutePoint.getY());
        return Math.sqrt(Math.pow(a, 2) + Math.pow(b,2));
    }

    public double getDistanceFromStartToRoutePoint(IRoutePoint routePoint){
        double distance = 0;

        for (int i = 0; i < routePoints.size()-1; i++) {
            IRoutePoint firstRoutePoint = routePoints.get(i);
            IRoutePoint secondRoutePoint = routePoints.get(i+1);

            int a = Math.abs(firstRoutePoint.getX() - secondRoutePoint.getX());
            int b = Math.abs(firstRoutePoint.getY() - secondRoutePoint.getY());
            distance += Math.sqrt(Math.pow(a, 2) + Math.pow(b,2));

            if(secondRoutePoint == routePoint){
                return distance;
            }
        }

        return 0;
    }
}
