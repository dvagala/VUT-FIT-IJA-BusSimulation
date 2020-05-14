package entities;


import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import misc.ColorHelper;

import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * Predstavuje autobusovu linku, ktora sa sklada z "route points" co su autobusove zastavky, alebo zlomy ulic.
 * Jedna linka moze obsahovat viac odchodov autobusov.
 * @author Dominik Vagala (xvagal00)
 * @author Jakub Vinš (xvinsj00)
 */
public class BusRoute {

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

    public void clearRoutePoints(){
        routePoints.clear();
    }

    public void clearBusStopss(){
        busStops.clear();
    }

    public void clearRouteSchedules(){
        routeSchedules.clear();
    }

    public void addBusStop(BusStop busStop){
        busStops.add(busStop);
    }

    public void addRoutePoint(IRoutePoint routePoint){
        routePoints.add(routePoint);
    }

    public IRoutePoint getLastRoutePoint(){
        return routePoints.get(routePoints.size()-1);
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

    public void updateNode(){
        Pane pane = (Pane) node;
        for (int i = 0; i < routePoints.size()-1; i++) {
            IRoutePoint first = routePoints.get(i);
            IRoutePoint second = routePoints.get(i+1);

            Line line = new Line(first.getX(), first.getY(), second.getX(), second.getY());
            line.setStroke(ColorHelper.getLighterColor(color, 0.3));
            line.setStrokeWidth(7);
            pane.getChildren().add(line);
        }
        pane.setOpacity(0.5);
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

    // This will automatically calculate departure times from bus stops, just by departure time from very first bus stop and simulation settings
    // It will create new RouteSchedules. It is used when bus is not visible on map.
    public List<RouteScheduleEntry> getCalculatedEntriesByFirstDepartureTime(LocalTime firstDepartureTime){
        List<RouteScheduleEntry> entries = new ArrayList<>();
        entries.add(new RouteScheduleEntry(busStops.get(0), firstDepartureTime, firstDepartureTime));

        LocalTime lastStopDepartureTime = firstDepartureTime;
        double secondsBetweenStops = 0;
        double secondsBetweenStopsNonDelayed = 0;

        boolean thisDepartureTimeIsDelayed = false;

        IRoutePoint a = null;
        IRoutePoint b = null;
        for (int i = 0; i < routePoints.size() - 1; i++) {
            a = routePoints.get(i);
            b = routePoints.get(i+1);

            int currentSegmentTrafficRate = a.getStreetAfter().getTrafficRate();
            if(currentSegmentTrafficRate > 1){
                secondsBetweenStops += getDistanceBetweenRoutePoints(a,b)*currentSegmentTrafficRate/Bus.speedPixelsPerSecond;
                thisDepartureTimeIsDelayed = true;
            }else{
                secondsBetweenStops += getDistanceBetweenRoutePoints(a,b)/Bus.speedPixelsPerSecond;
            }

            secondsBetweenStopsNonDelayed += getDistanceBetweenRoutePoints(a,b)/Bus.speedPixelsPerSecond;

            if(b instanceof BusStop){
                LocalTime calculatedDepartureTime = lastStopDepartureTime.plusSeconds((int) secondsBetweenStops + Bus.minutesToWaitAtStopAtLeast*60);
                LocalTime calculatedDepartureTimeRounded = calculatedDepartureTime.truncatedTo(ChronoUnit.MINUTES).plusMinutes(1);

                LocalTime calculatedDepartureTimeNonDelayed = lastStopDepartureTime.plusSeconds((int) secondsBetweenStopsNonDelayed + Bus.minutesToWaitAtStopAtLeast*60);
                LocalTime calculatedDepartureTimeRoundedNonDelayed = calculatedDepartureTimeNonDelayed.truncatedTo(ChronoUnit.MINUTES).plusMinutes(1);

                RouteScheduleEntry entry = new RouteScheduleEntry((BusStop) b, calculatedDepartureTimeRounded, calculatedDepartureTimeRoundedNonDelayed);
                entry.setDelayed(thisDepartureTimeIsDelayed);
                entries.add(entry);
                lastStopDepartureTime = calculatedDepartureTime;
                secondsBetweenStops = 0;
            }
        }

        return entries;
    }

    public void setRouteSchedulesByFirstDepartureTimes(List<LocalTime> firstStopDepartureTimes){
        for (LocalTime firstDepartureTime : firstStopDepartureTimes){
            routeSchedules.add(new RouteSchedule(getCalculatedEntriesByFirstDepartureTime(firstDepartureTime)));
        }
    }

    public void recalculateDepartureTimes(LocalTime currentTime){
        for (RouteSchedule routeSchedule : routeSchedules){
            if(routeSchedule.getBus() == null){
                // Bus is not visible so we can calculate more easily
                routeSchedule.setEntries(getCalculatedEntriesByFirstDepartureTime(routeSchedule.getFirstStopDepartureTime()));
            }else{
                // Bus is visible we should calculate with realtime
                recalculateEntriesWhenBusIsVisible(routeSchedule, currentTime);
            }
        }
    }


    // This will automatically calculate departure times from bus stops, just by departure time from very first bus stop and simulation settings
    // This will not create new RouteSchedules, just update existing. It is used when bus is already visible on map
    private void recalculateEntriesWhenBusIsVisible(RouteSchedule routeSchedule, LocalTime currentTime) {

        int lastVisitedRoutePointIndex = routePoints.lastIndexOf(routeSchedule.getBus().getLastVisitedRoutePoint());
        // Prevent weird bug
        if(lastVisitedRoutePointIndex < 0){
            lastVisitedRoutePointIndex = 0;
        }

        double secondsToNextStop = 0;
        IRoutePoint a = null;
        IRoutePoint b = null;
        for (int i = lastVisitedRoutePointIndex; i < routePoints.size() - 1; i++) {
            a = routePoints.get(i);
            b = routePoints.get(i+1);

            int currentSegmentTrafficRate = a.getStreetAfter().getTrafficRate();
            if(lastVisitedRoutePointIndex == i){
                a = routeSchedule.getBus();
            }
            secondsToNextStop += (getDistanceBetweenRoutePoints(a,b)*currentSegmentTrafficRate)/Bus.speedPixelsPerSecond;

            if(b instanceof BusStop){
                int scheduleEntryIndex = routeSchedule.getIndexOfEntryByBusStop((BusStop) b);

                LocalTime calculatedDepartureTime = currentTime.plusSeconds((long) secondsToNextStop + Bus.minutesToWaitAtStopAtLeast*60);
                LocalTime calculatedDepartureTimeRounded = calculatedDepartureTime.truncatedTo(ChronoUnit.MINUTES).plusMinutes(1);

                routeSchedule.getEntries().get(scheduleEntryIndex).setDepartureTime(calculatedDepartureTimeRounded);

                if(routeSchedule.getEntries().get(scheduleEntryIndex).getDepartureTime().compareTo(routeSchedule.getEntries().get(scheduleEntryIndex).getNonDelayedDepartureTime()) == 0){
                    routeSchedule.getEntries().get(scheduleEntryIndex).setDelayed(false);
                }else {
                    routeSchedule.getEntries().get(scheduleEntryIndex).setDelayed(true);
                }
            }
        }
    }
}
