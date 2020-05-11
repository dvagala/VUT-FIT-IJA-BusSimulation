package entities;

import gui.*;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Shape;
import javafx.scene.text.Text;
import misc.SimulationSettings;

import java.time.LocalTime;
import java.util.List;


public class Bus extends Coordinate {

    private final BusRoute busRoute;
    private double speedPixelsPerSecond = 2;
    private final int busCircleSize = 7;
    private double travelledDistance = 0;
    private Node node;
    public static int waitAtFirstStopMinutes = 2;

    public Bus(BusRoute busRoute) {
        this.busRoute = busRoute;
        this.setX(busRoute.getStops().get(0).getX());
        this.setY(busRoute.getStops().get(0).getY());
    }

    public Node getNode() {
        if(node == null){
            Shape circle = new Circle(this.getX(), this.getY(), busCircleSize, busRoute.getColor());
            VBox vBox = new VBox();
            vBox.getChildren().add(circle);
            Text text = new Text(this.getX() - busCircleSize*4, this.getY() + busCircleSize*4, String.valueOf(busRoute.getRouteNumber()));
            vBox.getChildren().add(text);
            vBox.setLayoutX(this.getX() - busCircleSize);
            vBox.setLayoutY(this.getY() - busCircleSize);
            vBox.setAlignment(Pos.TOP_LEFT);
            node = vBox;
        }
        return node;
    }

    public void updateNodePosition(LocalTime currentTime){
        if(node != null){

            // Bus waits on first stop or last stop
            BusStop nextStop = getNextStop(currentTime);
            if(nextStop == null){
                return;
            }

            travelledDistance += speedPixelsPerSecond* SimulationSettings.updateIntervalMs*SimulationSettings.speedRatio/1000;

            double distanceFromStartToNextStop = busRoute.getDistanceFromStartToRoutePoint(nextStop);

            // If bus would goes beyond bus stop, this will move back to the bus stop and wait there till the departure time
            if(travelledDistance > distanceFromStartToNextStop){
                travelledDistance = distanceFromStartToNextStop;
            }

            setNodePosition(busRoute.getCoordinateByDistance(travelledDistance));
        }
    }

    private void setNodePosition(Coordinate position){
        System.out.println("update bus pos: " + position.getX() + ", " + position.getY());
        node.setLayoutX(position.getX() - busCircleSize);
        node.setLayoutY(position.getY() - busCircleSize);
    }

    private BusStop getNextStop(LocalTime localTime) {
        RouteSchedule currentRouteSchedule = this.getCurrentRouteSchedule(localTime);

        if(currentRouteSchedule == null){
            return null;
        }

        for (int i = 0; i < currentRouteSchedule.getDepartures().size() - 1; i++) {
            if (localTime.compareTo(currentRouteSchedule.getDepartures().get(i)) >= 0 && localTime.compareTo(currentRouteSchedule.getDepartures().get(i + 1)) < 0) {
                return busRoute.getStops().get(i + 1);
            }
        }
        return null;
    }

    public RouteSchedule getCurrentRouteSchedule(LocalTime localTime){
        for (RouteSchedule routeSchedule : this.busRoute.getRouteSchedules()){
            if(localTime.compareTo(routeSchedule.getFirstStopDeparture()) > 0 && localTime.compareTo(routeSchedule.getLastStopDeparture()) < 0){
                return routeSchedule;
            }
        }

        return null;
    }

    public static Bus getVisibleBusByRoute(List<Bus> visibleBuses, BusRoute busRoute) {
        for(Bus visibleBus : visibleBuses){
            if(visibleBus.busRoute == busRoute){
                return visibleBus;
            }
        }
        return null;
    }
}

