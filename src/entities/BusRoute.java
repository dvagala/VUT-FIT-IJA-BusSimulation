package entities;


import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import misc.ColorHelper;

import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BusRoute {



    private List<Street> passingStreets = new ArrayList<>();

    private final List<IRoutePoint> routePoints;
    private List<BusStop> busStops = new ArrayList<>();
    private List<RouteSchedule> routeSchedules = new ArrayList<>();
    private Color color = Color.rgb(255, 0, 0);
    private final int routeNumber;
    private Node node = null;

    public BusRoute(int routeNumber, List<IRoutePoint> routePoints) {
        this.routeNumber = routeNumber;
        this.routePoints = routePoints;

        for(IRoutePoint routePoint : routePoints){
            if(routePoint instanceof BusStop){
                busStops.add((BusStop) routePoint);
            }
        }
    }

    public List<Street> getPassingStreets() {
        return passingStreets;
    }

    public List<BusStop> getStops() {
        return busStops;
    }

    public List<IRoutePoint> getRoutePoints() {
        return routePoints;
    }

    public Color getColor() {
        return color;
    }

    public int getRouteNumber() {
        return routeNumber;
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

        pane.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
//                System.out.println("route clicked");
            }
        });

        return pane;
    }

    public Node getNode(){
        if(node == null){
            node = createNode();
        }
        return node;
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

    public void setRouteSchedulesByFirstDepartureTimes(List<LocalTime> firstStopDepartureTimes){

        for (LocalTime firstDepartureTime : firstStopDepartureTimes){

            List<RouteScheduleEntry> entries = new ArrayList<>();
            entries.add(new RouteScheduleEntry(busStops.get(0), firstDepartureTime));

            LocalTime lastStopDepartureTime = firstDepartureTime;
            double distanceBetweenStops = 0;

            IRoutePoint a = null;
            IRoutePoint b = null;
            for (int i = 0; i < routePoints.size() - 1; i++) {
                a = routePoints.get(i);
                b = routePoints.get(i+1);

                distanceBetweenStops += getDistanceBetweenRoutePoints(a,b);

                if(b instanceof BusStop){
                    int secondsBetweenStops = (int) (distanceBetweenStops/Bus.speedPixelsPerSecond);

                    LocalTime calculatedDepartureTime = lastStopDepartureTime.plusSeconds(secondsBetweenStops + Bus.minutesToWaitAtStopAtLeast*60);
                    LocalTime calculatedDepartureTimeRounded = calculatedDepartureTime.truncatedTo(ChronoUnit.MINUTES).plusMinutes(1);

                    entries.add(new RouteScheduleEntry((BusStop) b, calculatedDepartureTimeRounded));
                    lastStopDepartureTime = calculatedDepartureTime;
                    distanceBetweenStops = 0;
                }
            }
            routeSchedules.add(new RouteSchedule(entries));
        }
    }

    public void calculatePassingStreets(List<Street> allStreets){
        for(Street street : allStreets){
//            System.out.println( "check street: " + street.getName());
//            if(isRoutePassingStreet(street, routePoints)){
//                if(!passingStreets.contains(street)){
//                    passingStreets.add(street);
//                    System.out.println("route: " + routeNumber + ", add street: " + street.getName());
//                }
//            }
            passingStreets.add(street);
        }
    }

    private boolean isRoutePassingStreet(Street street, List<IRoutePoint> routePoints) {
        List<IRoutePoint> routePointsNotBusStops = new ArrayList<>();
        for(IRoutePoint routePoint : routePoints){
            if(!(routePoint instanceof BusStop)){
                routePointsNotBusStops.add(routePoint);
            }
        }


        for (int i = 0; i < routePointsNotBusStops.size()-1; i++) {
            for (int j = 0; j < street.getCoordinates().size()-1; j++) {
                if(routePointsNotBusStops.get(i) == street.getCoordinate(j)){
                    if(routePointsNotBusStops.get(i+1) == street.getCoordinate(j+1)){
                        return true;
                    }

                    if(j-1 >= 0){
                        if(routePointsNotBusStops.get(i+1) == street.getCoordinate(j-1)){
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

}
