
package gui;


import entities.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import misc.SimulationSettings;

import java.net.URL;
import java.time.LocalTime;
import java.util.*;

import static java.time.temporal.ChronoUnit.MINUTES;

public class MainController  implements Initializable{

    @FXML
    private Text clockText;

    @FXML
    public Pane mapPane;

    @FXML
    public ScrollPane scrollPane;

    private LocalTime currentTime = SimulationSettings.startTime;
    List<BusRoute> busRoutes = new ArrayList<>();
    List<Bus> visibleBuses = new ArrayList<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        scrollPane.addEventFilter(ScrollEvent.SCROLL, event -> {
            double ratio = event.getDeltaY() > 0 ? 1.1 : 0.9;
            mapPane.setScaleX(ratio * mapPane.getScaleX());
            mapPane.setScaleY(ratio * mapPane.getScaleY());
            event.consume();
        });

        this.setUpData();

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
//                System.out.println("neu frame");
                clockText.setText(currentTime.toString());
                handleBusesVisibilityOnMap();

                for(Bus visibleBus : visibleBuses){
                    visibleBus.updateNodePosition(currentTime);
                }

                currentTime = currentTime.plusNanos((long) (SimulationSettings.updateIntervalMs*1000000*SimulationSettings.speedRatio));
            }
        }, 0, SimulationSettings.updateIntervalMs);

    }

    private void handleBusesVisibilityOnMap(){
        for(BusRoute busRoute : busRoutes){
            for(RouteSchedule routeSchedule : busRoute.getRouteSchedules()){
                if(currentTime.until(routeSchedule.getFirstStopDepartureTime(), MINUTES) < Bus.waitAtFirstStopMinutes && currentTime.compareTo(routeSchedule.getLastStopDepartureTime()) < 0){
                    if(Bus.getVisibleBusByRouteAndStartTime(visibleBuses, busRoute, routeSchedule.getFirstStopDepartureTime()) == null){
                        Bus bus = new Bus(busRoute);
                        bus.setOnBusClickListener(new Bus.OnBusClickListener() {
                            @Override
                            public void busWasClicked() {
                                for(Bus b : visibleBuses){
                                    if(b != bus){
                                        b.getBusRoute().getNode().setOpacity(0.0);
                                    }
                                }
                            }
                        });
                        visibleBuses.add(bus);
                        Platform.runLater(() -> mapPane.getChildren().add(bus.getNode()));
                    }
                }else if(currentTime.compareTo(routeSchedule.getLastStopDepartureTime()) > 0){
                    Bus bus = Bus.getVisibleBusByRouteAndStartTime(visibleBuses, busRoute, routeSchedule.getFirstStopDepartureTime());
                    if(bus != null){
                        visibleBuses.remove(bus);
                        Platform.runLater(() -> mapPane.getChildren().remove(bus.getNode()));
                    }
                }
            }
        }
    }

    private void setUpData(){
        Street street = new Street(Arrays.asList(new Coordinate(50, 50)));
        street.addCoordinate(new Coordinate(200, 50));
        street.addCoordinate(new Coordinate(300, 200));
        street.addCoordinate(new Coordinate(400, 200));
        street.addCoordinate(new Coordinate(400, 100));
        street.addCoordinate(new Coordinate(550, 100));
        street.setColor(Color.GREEN);

        Street street2 = new Street(Arrays.asList(new Coordinate(350, 200)));
        street2.addCoordinate(new Coordinate(350, 300));
        street2.addCoordinate(new Coordinate(150, 300));
        street2.setColor(Color.RED);

        Street street3 = new Street(Arrays.asList(new Coordinate(350, 250)));
        street3.addCoordinate(new Coordinate(500, 250));
        street3.addCoordinate(new Coordinate(500, 100));
        street3.setColor(Color.ORANGE);

        List<Street> streets = Arrays.asList(
                street, street2, street3
        );


        BusStop busStop = new BusStop(new Coordinate(100, 50), "husitska");
        BusStop busStop2 = new BusStop(new Coordinate(260, 140), "ceska");
        BusStop busStop3 = new BusStop(new Coordinate(520, 100), "semilasso");



        BusRoute busRoute = new BusRoute(3,
                Arrays.asList(
                        busStop,
                        street.getCoordinate(1),
                        busStop2,
                        street.getCoordinate(2),
                        street2.getCoordinate(0),
                        street3.getCoordinate(0),
                        street3.getCoordinate(1),
                        street3.getCoordinate(2),
                        busStop3
                ));

        busRoute.setColor(Color.rgb(255, 0, 0));

        busRoute.setRouteSchedules(Arrays.asList(new RouteSchedule(Arrays.asList(
                LocalTime.of(10, 30),
                LocalTime.of(10, 35),
                LocalTime.of(10, 45)
        ))));

        busRoutes.add(busRoute);

        BusStop busStop4 = new BusStop(new Coordinate(200, 300), "cervinkova");
        BusStop busStop5 = new BusStop(new Coordinate(400, 140), "skacelka");
        BusStop busStop6 = new BusStop(new Coordinate(440, 250), "dobrovskeho");

        BusRoute busRoute2 = new BusRoute(5,
                Arrays.asList(
                        busStop4,
                        street2.getCoordinate(1),
                        street2.getCoordinate(0),

                        street.getCoordinate(2),
                        busStop2,
                        street.getCoordinate(2),


                        street.getCoordinate(3),
                        busStop5,
                        street.getCoordinate(4),
                        street3.getCoordinate(2),
                        street3.getCoordinate(1),
                        busStop6
                ));


        busRoute2.setColor(Color.rgb(0, 0, 255));

        busRoute2.setRouteSchedules(Arrays.asList(new RouteSchedule(Arrays.asList(
                LocalTime.of(10, 35),
                LocalTime.of(10, 40),
                LocalTime.of(10, 45),
                LocalTime.of(10, 50)
        )), new RouteSchedule(Arrays.asList(
                LocalTime.of(10, 37),
                LocalTime.of(10, 42),
                LocalTime.of(10, 47),
                LocalTime.of(10, 52)
        ))));

        busRoutes.add(busRoute2);

        List<BusStop> busStops = Arrays.asList(
                busStop, busStop2, busStop3, busStop4, busStop5, busStop6
        );



        for(Street s : streets){
            mapPane.getChildren().addAll(s.getNodes());
        }


        for(BusRoute route : busRoutes){
            mapPane.getChildren().addAll(route.getNode());
        }

        for(BusStop s : busStops){
            mapPane.getChildren().addAll(s.getNode());
        }

    }

}



