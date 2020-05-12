package entities;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Shape;
import javafx.scene.text.Text;

import java.time.LocalTime;
import java.util.List;

import static entities.BusRoute.getDistanceBetweenRoutePoints;
import static java.time.temporal.ChronoUnit.SECONDS;


public class Bus extends Coordinate {



    private final BusRoute busRoute;


    private final RouteSchedule currentRouteSchedule;
    public static double speedPixelsPerSecond = 2;
    private final int busCircleSize = 7;
    private double travelledDistance = 0;
    private Node node;
    public static int minutesToWaitAtStopAtLeast = 2;


    private OnBusClickListener listener;

    public Bus(BusRoute busRoute, RouteSchedule routeSchedule) {
        this.busRoute = busRoute;
        this.setX(busRoute.getStops().get(0).getX());
        this.setY(busRoute.getStops().get(0).getY());
        this.currentRouteSchedule = routeSchedule;
    }

    public RouteSchedule getCurrentRouteSchedule() {
        return currentRouteSchedule;
    }

    private Node createNode(){
        Shape circle = new Circle(this.getX(), this.getY(), busCircleSize, busRoute.getColor());
        VBox vBox = new VBox();
        vBox.getChildren().add(circle);
        Text text = new Text(this.getX() - busCircleSize*4, this.getY() + busCircleSize*4, String.valueOf(busRoute.getRouteNumber()));
        vBox.getChildren().add(text);
        vBox.setLayoutX(this.getX() - busCircleSize);
        vBox.setLayoutY(this.getY() - busCircleSize);
        vBox.setAlignment(Pos.TOP_LEFT);

        vBox.setOnMouseClicked(event -> {
            event.consume();
            listener.busWasClicked();
        });


        return vBox;
    }

    public BusRoute getBusRoute() {
        return busRoute;
    }

    public Node getNode() {
        if(node == null){
            node = createNode();
        }
        return node;
    }

    public void updateNodePosition(LocalTime currentTime){
        if(node != null){

            if(busIsWaitingOnFirstStop(currentTime)){
                setNodePosition(busRoute.getStops().get(0));
                return;
            }

            BusStop nextStop = getNextStop(currentTime);

            travelledDistance = getTravelledDistanceByTime(currentTime, nextStop);
            double distanceFromStartToNextStop = busRoute.getDistanceFromStartToRoutePoint(nextStop);

            // If bus would goes beyond bus stop, this will move back to the bus stop and wait there till the departure time
            if(travelledDistance > distanceFromStartToNextStop){
                travelledDistance = distanceFromStartToNextStop;
            }

            setNodePosition(getCoordinateByTravelledDistance(travelledDistance));
        }
    }

    public boolean busIsWaitingOnFirstStop(LocalTime currentTime){
        return  currentTime.compareTo(currentRouteSchedule.getFirstStopDepartureTime()) < 0;
    }

    private double getTravelledDistanceByTime(LocalTime currentTime, BusStop nextStop) {
        BusStop lastStop = null;
        for(BusStop s : busRoute.getStops()){
            if(s == nextStop){
                break;
            }
            lastStop = s;
        }

        double distanceToLastStop = 0;

        IRoutePoint a = null;
        IRoutePoint b = null;
        for (int i = 0; i < busRoute.getRoutePoints().size() - 1; i++) {
            a = busRoute.getRoutePoints().get(i);
            b = busRoute.getRoutePoints().get(i+1);

            if(a == lastStop){
                break;
            }
            distanceToLastStop += getDistanceBetweenRoutePoints(a,b);
        }

        LocalTime departureTimeFromLastStop = null;

        for (RouteScheduleEntry routeScheduleEntry : currentRouteSchedule.getEntries()){
            if(routeScheduleEntry.getBusStop() == lastStop){
                departureTimeFromLastStop = routeScheduleEntry.getDepartureTime();
            }
        }

        if(departureTimeFromLastStop == null){
            return 0;
        }

        return distanceToLastStop + departureTimeFromLastStop.until(currentTime, SECONDS)*speedPixelsPerSecond;
    }

    public Coordinate getCoordinateByTravelledDistance(double distance){
        double length = 0;

        IRoutePoint a = null;
        IRoutePoint b = null;
        for (int i = 0; i < busRoute.getRoutePoints().size() - 1; i++) {
            a = busRoute.getRoutePoints().get(i);
            b = busRoute.getRoutePoints().get(i+1);

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



    private void setNodePosition(Coordinate position){
//        System.out.println("update bus pos: " + position.getX() + ", " + position.getY());
        node.setLayoutX(position.getX() - busCircleSize);
        node.setLayoutY(position.getY() - busCircleSize);
    }

    private BusStop getNextStop(LocalTime localTime) {
        for (int i = 0; i < currentRouteSchedule.getEntries().size() - 1; i++) {
            if (localTime.compareTo(currentRouteSchedule.getEntries().get(i).getDepartureTime()) >= 0 && localTime.compareTo(currentRouteSchedule.getEntries().get(i + 1).getDepartureTime()) < 0) {
                return busRoute.getStops().get(i + 1);
            }
        }
        return null;
    }

    public static Bus getVisibleBusByRouteAndSchedule(List<Bus> visibleBuses, BusRoute busRoute, RouteSchedule routeSchedule) {
        for(Bus visibleBus : visibleBuses){
            if(visibleBus.busRoute == busRoute){
                if(visibleBus.currentRouteSchedule == routeSchedule){
                    return visibleBus;
                }
            }
        }
        return null;
    }

    public void setOnBusClickListener(OnBusClickListener listener) {
        this.listener = listener;
    }

    public interface OnBusClickListener {
        void busWasClicked();
    }
}

