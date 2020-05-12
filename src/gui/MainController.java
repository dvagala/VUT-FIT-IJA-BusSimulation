
package gui;


import entities.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import misc.SimulationSettings;

import java.net.URL;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static java.time.temporal.ChronoUnit.MINUTES;

public class MainController  implements Initializable{

    @FXML
    private Text clockText;

    @FXML
    private TextField setTimeTextField;

    @FXML
    private TextField setSpeedTextField;

    @FXML
    private Text setTimeWrongFormatText;

    @FXML
    private Text setSpeedWrongFormatText;

    @FXML
    private Text routeDeparturesNumberText;

    @FXML
    public GridPane routeDeparturesGridPane;

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
                clockText.setText(currentTime.format(DateTimeFormatter.ofPattern("HH:mm:ss")));
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
                if(currentTime.until(routeSchedule.getFirstStopDepartureTime(), MINUTES) < Bus.minutesToWaitAtStopAtLeast && currentTime.compareTo(routeSchedule.getLastStopDepartureTime()) < 0){
                    if(Bus.getVisibleBusByRouteAndSchedule(visibleBuses, busRoute, routeSchedule) == null){
                        Bus bus = new Bus(busRoute, routeSchedule);
                        bus.setOnBusClickListener(new Bus.OnBusClickListener() {
                            @Override
                            public void busWasClicked() {
                                for(BusRoute r : busRoutes){
                                    r.getNode().setOpacity(0.0);
                                }

                                Pane nodeLayout = (Pane) busRoute.getNode();
                                nodeLayout.setOpacity(0.5);

                                routeDeparturesGridPane.getChildren().clear();
                                fillRouteDeparturesGridPane(routeDeparturesGridPane, bus);
                                routeDeparturesNumberText.setText("Route number " + bus.getBusRoute().getRouteNumber());
                            }
                        });
                        visibleBuses.add(bus);
                        Platform.runLater(() -> mapPane.getChildren().add(bus.getNode()));
                    }
                }else if(currentTime.compareTo(routeSchedule.getLastStopDepartureTime()) > 0){
                    Bus bus = Bus.getVisibleBusByRouteAndSchedule(visibleBuses, busRoute, routeSchedule);
                    if(bus != null){
                        visibleBuses.remove(bus);
                        Platform.runLater(() -> mapPane.getChildren().remove(bus.getNode()));
                    }
                }
            }
        }
    }

    private void fillRouteDeparturesGridPane(GridPane gridPane, Bus selectedBus){
        Text text = new Text("hee");
        for (int i = 0; i < selectedBus.getCurrentRouteSchedule().getEntries().size(); i++) {
            RouteScheduleEntry entry = selectedBus.getCurrentRouteSchedule().getEntries().get(i);
            gridPane.add(new Text(entry.getBusStop().getName()), 0, i);
            gridPane.add(new Text(String.valueOf(entry.getDepartureTime())), 1, i);
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

        busRoute.setRouteSchedulesByFirstDepartureTimes(Arrays.asList(
                LocalTime.of(10, 30)
        ));

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
        busRoute2.setRouteSchedulesByFirstDepartureTimes(Arrays.asList(
                LocalTime.of(10, 33),
                LocalTime.of(10, 40)
        ));

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

    public void onCloseDeparturesBtnClick() {
        for(BusRoute r : busRoutes){
            r.getNode().setOpacity(0.0);
        }

        routeDeparturesNumberText.setText("Click on bus to see departures");
        routeDeparturesGridPane.getChildren().clear();
    }

    public void onSetTimeBtnClick() {
        if(setTimeTextField.getText().isEmpty()){
            return;
        }

        boolean timeWasSetCorrectly = true;
        try{
            currentTime = LocalTime.parse(setTimeTextField.getText(), DateTimeFormatter.ofPattern("HH:mm:ss"));
        }catch (Exception e){
            try{
                currentTime = LocalTime.parse(setTimeTextField.getText(), DateTimeFormatter.ofPattern("HH:mm"));
            }catch (Exception e2){
                timeWasSetCorrectly = false;
                setTimeWrongFormatText.setVisible(true);
            }
        }finally {
            setTimeTextField.clear();
        }

        if(timeWasSetCorrectly){
            setTimeWrongFormatText.setVisible(false);
        }
    }

    public void onSetSpeedBtnClick() {
        if(setSpeedTextField.getText().isEmpty()){
            return;
        }

        if(setSpeedTextField.getText().matches("\\d+x")){
            try {
                SimulationSettings.speedRatio = Double.parseDouble(setSpeedTextField.getText().substring(0, setSpeedTextField.getText().length()-1));
                setSpeedWrongFormatText.setVisible(false);
            }catch (Exception e){
                setSpeedWrongFormatText.setVisible(true);
            }
        }else{
            setSpeedWrongFormatText.setVisible(true);
        }

        setSpeedTextField.clear();
    }
}



