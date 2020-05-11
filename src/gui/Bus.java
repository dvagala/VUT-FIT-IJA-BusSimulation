package gui;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Shape;
import javafx.scene.text.Text;

import java.time.Duration;
import java.time.LocalTime;
import java.util.List;

class Bus extends Coordinate{

    private BusRoute busRoute;

    private double speedPixelsPerSecond = 2;
    int busCircleSize = 7;

    double travelledDistance = 0;

    public Bus(BusRoute busRoute) {
        this.busRoute = busRoute;
        this.setX(busRoute.getStops().get(0).getX());
        this.setY(busRoute.getStops().get(0).getY());
    }


    public Node getNode() {

        if(node == null){
            Paint color = busRoute.getColor();
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

    private Node node;

    public void updateNodePosition(LocalTime localTime, int updateIntervalMs, double simulationSpeedRatio){
        if(node != null){

            travelledDistance += speedPixelsPerSecond*updateIntervalMs*simulationSpeedRatio/1000;

            BusStop nextStop = getNextStop(localTime);
            if(nextStop == null){
                return;
            }

            double distanceFromStartToNextStop = busRoute.getDistanceFromStartToRoutePoint(nextStop);

            // If bus would goes beyond bus stop, it will move back to the bus stop and wait there till the departure time
            if(travelledDistance > distanceFromStartToNextStop){
                travelledDistance = distanceFromStartToNextStop;
            }

            setNodePosition(busRoute.getCoordinateByDistance(travelledDistance));

        }
    }

    private void setNodePosition(Coordinate position){
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






















//    public void updateNodePosition2(LocalTime localTime){
////        int p1 = getRoutePointsCountBetweenStops(busRoute.getStops().get(0), busRoute.getStops().get(1), busRoute.getRoutePoints());
////        int p2 = getRoutePointsCountBetweenStops(busRoute.getStops().get(1), busRoute.getStops().get(2), busRoute.getRoutePoints());
////        System.out.println("0-1 points: " + p1);
////        System.out.println("1-2 points: " + p2);
//
////        double p1 = getLengthBetweenRoutePoints(busRoute.getRoutePoints().get(0), busRoute.getRoutePoints().get(1));
////        double p2 = getLengthBetweenRoutePoints(busRoute.getRoutePoints().get(1), busRoute.getRoutePoints().get(2));
////        System.out.println("0-1 points: " + p1);
////        System.out.println("1-2 points: " + p2);
////
////        if(true)
////        return;
//
//        if(node != null){
//            RouteSchedule currentRouteSchedule = this.getCurrentRouteSchedule(localTime);
//
//            System.out.println(currentRouteSchedule.getDepartures().size());
//            for (int i = 0; i < currentRouteSchedule.getDepartures().size() - 1; i++) {
////                System.out.println("index: " + i);
////                System.out.println("localTime: " + localTime);
////                System.out.println("currentRouteSchedule.getDepartures().get(i): " + currentRouteSchedule.getDepartures().get(i));
////                System.out.println("currentRouteSchedule.getDepartures().get(i+1): " + currentRouteSchedule.getDepartures().get(i+1));
//
//                if(localTime.compareTo(currentRouteSchedule.getDepartures().get(i)) >= 0 && localTime.compareTo(currentRouteSchedule.getDepartures().get(i+1)) < 0){
//                    long elapsedMsFromLastStop = Duration.between(currentRouteSchedule.getDepartures().get(i), localTime).toMillis();
//                    long drivedDistanceFromLastStop = elapsedMsFromLastStop/1000;
//                    BusStop lastStop = busRoute.getStops().get(i);
//                    BusStop nextStop = busRoute.getStops().get(i+1);
//
//                    List<IRoutePoint> routePoints = busRoute.getRoutePoints();
//
//                    int lastRoutePointIndex = 0;
//                    for (int j = 0; j < routePoints.size(); j++) {
//                        if(routePoints.get(i) == lastStop){
//                            lastRoutePointIndex = i;
//                            break;
//                        }
//                    }
//
//                    double distanceFromLastStopToLastRoutePoint = 0;
//                    int routePointsCount = getRoutePointsCountBetweenStops(lastStop, nextStop, routePoints);
//                    double currentSegmentLength = 0;
//                    for (int j = 0; j < routePointsCount; j++) {
//                        double nextSegmentLength = getLengthBetweenRoutePoints(routePoints.get(lastRoutePointIndex), routePoints.get(lastRoutePointIndex+1));
//
//                        if(nextSegmentLength + distanceFromLastStopToLastRoutePoint > drivedDistanceFromLastStop){
//                            currentSegmentLength = nextSegmentLength;
//                            break;
//                        }
//
//                        lastRoutePointIndex++;
//                        distanceFromLastStopToLastRoutePoint += nextSegmentLength;
//                    }
//                    double drivedDistanceFromLastRoutePoint = drivedDistanceFromLastStop - distanceFromLastStopToLastRoutePoint;
//                    double busLocationInCurrentSegmentRatio = drivedDistanceFromLastRoutePoint/currentSegmentLength;
//
//                    int xAdd = (int) ((routePoints.get(lastRoutePointIndex+1).getX() - routePoints.get(lastRoutePointIndex).getX())*busLocationInCurrentSegmentRatio);
//                    int yAdd = (int) ((routePoints.get(lastRoutePointIndex+1).getY() - routePoints.get(lastRoutePointIndex).getY())*busLocationInCurrentSegmentRatio);
//                    node.setLayoutX(node.getLayoutX() + xAdd);
//                    node.setLayoutY(node.getLayoutY() + yAdd);
//
//                    System.out.println("first point: " + routePoints.get(lastRoutePointIndex).getX() + ", "+ routePoints.get(lastRoutePointIndex).getY());
//                    System.out.println("second point: " + routePoints.get(lastRoutePointIndex+1).getX() + ", " + routePoints.get(lastRoutePointIndex+1).getY());
//                }
//            }
//        }
//    }


//
//
//    private int getRoutePointsCountBetweenStops(BusStop firstStop, BusStop secondStop, List<IRoutePoint> routePoints){
//        boolean countOn = false;
//        int sum = 0;
//        for (IRoutePoint routePoint : routePoints) {
//            if (routePoint == secondStop) {
//                return sum;
//            }
//
//            if (countOn) {
//                sum++;
//            }
//
//            if (routePoint == firstStop) {
//                countOn = true;
//            }
//        }
//        return 0;
//    }
