
package gui;


import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

import java.net.URL;
import java.time.LocalTime;
import java.util.*;

public class MainController  implements Initializable {

    public List<IMapElement> mapElements = new ArrayList<>();

    private Timer timer = new Timer();

    @FXML
    private Text clockText;

    double simulationSpeedRatio = 60;

    private LocalTime localTime = LocalTime.of(10, 29, 0, 0);
    int updateIntervalMs = 200;

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

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                clockText.setText(localTime.toString());

                placeBusesOnMapIfItsTime();

                for(Bus visibleBus : visibleBuses){
                    visibleBus.updateNodePosition(localTime, updateIntervalMs, simulationSpeedRatio);
                }


                localTime = localTime.plusNanos((long) (updateIntervalMs*1000000*simulationSpeedRatio));
            }
        }, 0, updateIntervalMs);

    }

    private void placeBusesOnMapIfItsTime(){
        for(BusRoute busRoute : busRoutes){
            for(RouteSchedule routeSchedule : busRoute.getRouteSchedules()){
                if(localTime.compareTo(routeSchedule.getFirstStopDeparture()) > 0 && localTime.compareTo(routeSchedule.getLastStopDeparture()) < 0){
                    if(Bus.getVisibleBusByRoute(visibleBuses, busRoute) == null){
                        Bus bus = new Bus(busRoute);
                        visibleBuses.add(bus);
                        Platform.runLater(() -> {
                            mapPane.getChildren().add(bus.getNode());
                        });
                    }
                }else if(localTime.compareTo(routeSchedule.getLastStopDeparture()) > 0){
                    Bus bus = Bus.getVisibleBusByRoute(visibleBuses, busRoute);
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

        BusStop busStop = new BusStop(new Coordinate(100, 50));
        BusStop busStop2 = new BusStop(new Coordinate(260, 140));
        BusStop busStop3 = new BusStop(new Coordinate(520, 100));

        BusRoute busRoute = new BusRoute("first route",
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

        busRoute.setColor(Color.rgb(255, 0, 0, 0.3));

        busRoute.setRouteSchedules(Arrays.asList(new RouteSchedule(Arrays.asList(
                LocalTime.of(10, 30),
                LocalTime.of(10, 35),
                LocalTime.of(10, 45)
        ))));

        busRoutes.add(busRoute);

        mapPane.getChildren().addAll(street.getUiElements());
        mapPane.getChildren().addAll(street2.getUiElements());
        mapPane.getChildren().addAll(street3.getUiElements());
        mapPane.getChildren().addAll(busStop.getUiElement());
        mapPane.getChildren().addAll(busStop2.getUiElement());
        mapPane.getChildren().addAll(busStop3.getUiElement());
        mapPane.getChildren().addAll(busRoute.getUiElements());


//        mapPane.getChildren().remove
    }

    @FXML
    public Button btn;

    @FXML
    public Pane mapPane;

    @FXML
    public ScrollPane scrollPane;


}



