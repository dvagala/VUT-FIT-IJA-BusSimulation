package entities;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Shape;
import javafx.scene.text.Text;
import misc.SimulationSettings;

import java.time.LocalTime;
import java.util.List;

import static entities.BusRoute.getDistanceBetweenRoutePoints;
import static java.time.temporal.ChronoUnit.SECONDS;


/**
 * Predstavuje konkretny autobus, ktory moze byt vykresleny na mape.
 * @author Dominik Vagala (xvagal00)
 * @author Jakub Vinš (xvinsj00)
 */
public class Bus extends Coordinate {

    private final BusRoute busRoute;
    private RouteSchedule currentRouteSchedule;
    public static double speedPixelsPerSecond = 2;
    public int currentStreetTrafficRate = 1;
    private final int busCircleSize = 7;
    private double travelledDistance = 0;
    private Node node;
    public static int minutesToWaitAtStopAtLeast = 2;
    private Text busTextNode;
    private IRoutePoint lastVisitedRoutePoint = null;
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


    public void setCurrentRouteSchedule(RouteSchedule routeSchedule) {
        currentRouteSchedule = routeSchedule;
    }

    private Node createNode(){

        Shape circle = new Circle(this.getX(), this.getY(), busCircleSize, busRoute.getColor());
        VBox vBox = new VBox();
        vBox.getChildren().add(circle);
        busTextNode = new Text(this.getX() - busCircleSize*4, this.getY() + busCircleSize*4, String.valueOf(busRoute.getRouteNumber()));
        vBox.getChildren().add(busTextNode);
        vBox.setLayoutX(this.getX() - busCircleSize);
        vBox.setLayoutY(this.getY() - busCircleSize);
        vBox.setAlignment(Pos.TOP_LEFT);

        vBox.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                event.consume();
                listener.busWasClicked();
            }
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

    // Move bus on map
    public void updateNodePosition(LocalTime currentTime){

        BusStop nextStop = getNextStop(currentTime);

        // Bus position is calculated by time only on the start of simulation
        if(currentTime == SimulationSettings.startTime){
            travelledDistance = getTravelledDistanceByTime(currentTime, nextStop);
            return;
        }

        if(busIsWaitingOnFirstStop(currentTime)){
            setNodePosition(busRoute.getStops().get(0));
            return;
        }

        if(nextStop == null){
            setNodePosition(busRoute.getStops().get(busRoute.getStops().size()-1));
            return;
        }

        currentStreetTrafficRate = getCurrentStreetTrafficRate(travelledDistance);

        // Position is calculated as simulation, every tick of main loop travelled distance gets bigger
        travelledDistance += (speedPixelsPerSecond*((double)(SimulationSettings.updateIntervalMs)/1000)*SimulationSettings.speedRatio)/currentStreetTrafficRate;

        double distanceFromStartToNextStop = busRoute.getDistanceFromStartToRoutePoint(nextStop);

        // If bus would goes beyond bus stop, this will move back to the bus stop and wait there till the departure time
        if(travelledDistance > distanceFromStartToNextStop){
            travelledDistance = distanceFromStartToNextStop;
        }

        // Prevent weird bug
        if(travelledDistance < 0){
            travelledDistance = 0;
        }

        Coordinate newBusPosition = getCoordinateByTravelledDistance(travelledDistance);
        setNodePosition(newBusPosition);
    }

    private int getCurrentStreetTrafficRate(double distance) {
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
            return 1;
        }

        if(a.getStreetAfter() != null && busTextNode != null){
            return currentStreetTrafficRate = a.getStreetAfter().getTrafficRate();
        }
        return 1;
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

        lastVisitedRoutePoint = a;

        double driven = (distance - length) / getDistanceBetweenRoutePoints(a,b);
        return new Coordinate((int) (a.getX() + (b.getX() - a.getX()) * driven), (int) (a.getY() + (b.getY() - a.getY())*driven));
    }



    private void setNodePosition(Coordinate position){
        this.setX(position.getX());
        this.setY(position.getY());
        if(node != null){
            Platform.runLater(() -> {
                node.setLayoutX(position.getX() - busCircleSize);
                node.setLayoutY(position.getY() - busCircleSize);
            });
        }
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

    public IRoutePoint getLastVisitedRoutePoint() {
        return lastVisitedRoutePoint;
    }

    public interface OnBusClickListener {
        void busWasClicked();
    }
}

