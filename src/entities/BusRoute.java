package entities;


import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Line;

import java.util.ArrayList;
import java.util.List;

public class BusRoute {

    private final List<IRoutePoint> routePoints;
    private List<BusStop> stops = new ArrayList<>();
    private List<RouteSchedule> routeSchedules = new ArrayList<>();
    private Color color = Color.rgb(255, 0, 0);
    private final int routeNumber;

    public List<BusStop> getStops() {
        return stops;
    }

    public Paint getColor() {
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
                stops.add((BusStop) routePoint);
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

    public List<Node> getNodes(){
        List<Node> nodes = new ArrayList<>();

        for (int i = 0; i < routePoints.size()-1; i++) {
            IRoutePoint first = routePoints.get(i);
            IRoutePoint second = routePoints.get(i+1);

            Line line = new Line(first.getX(), first.getY(), second.getX(), second.getY());
            Color opaqueColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), 0.0);
            line.setStroke(opaqueColor);
            line.setStrokeWidth(7);
            nodes.add(line);
        }

        return nodes;
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
