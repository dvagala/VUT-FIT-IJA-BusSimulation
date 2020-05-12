
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
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.*;


import java.io.FileReader;
import java.io.IOException;
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
    private int into(Object x){ //Long to int
        return ((Long)x).intValue();
    }

    private void setUpData() {
        JSONObject jo = new JSONObject();
        JSONParser parser = new JSONParser();
        try {
            Object obj = parser.parse(new FileReader("data.json"));
            jo = (JSONObject) obj;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        JSONObject jStreets = (JSONObject) jo.get("streets");
        JSONObject jBusStops = (JSONObject) jo.get("busStops");
        JSONObject jBusRoutes = (JSONObject) jo.get("busRoutes");

        List<Street> streets = new ArrayList<>();
        List<BusStop> busStops = new ArrayList<>();


        for (Object var : jStreets.keySet()){
            JSONObject s = (JSONObject) jStreets.get(var); //street
            ArrayList<JSONArray> coors = (ArrayList<JSONArray>) s.get("coors");
            List<Coordinate> coordinates = new ArrayList<>();
            for (JSONArray jC: coors){
                Coordinate c = new Coordinate(into(jC.get(0)), into(jC.get(1))); //x,y value of coord
                coordinates.add(c);
            }
            Street street = new Street(coordinates);
            street.setColor(Color.valueOf((String) s.get("color")));
            streets.add(street);
        }

        for (Object var: jBusStops.keySet()){
            JSONObject jBusStop = (JSONObject) jBusStops.get(var);
            JSONArray jC = (JSONArray) jBusStop.get("c");
            BusStop busStop = new BusStop(new Coordinate(into(jC.get(0)), into(jC.get(1))), (String) jBusStop.get("name"));
            busStops.add(busStop);
        }

        for (Object var: jBusRoutes.keySet()) {
            JSONObject jBusRoute = (JSONObject) jBusRoutes.get(var);
            List<IRoutePoint> data = new ArrayList <>();
            for (JSONArray a : (ArrayList<JSONArray>) jBusRoute.get("data")){
                int x = into(a.get(1)); // value contains idx of busStop or street
                int y = into(a.get(2)); // value says which streetCoordinate should be used
                if(a.get(0).equals("b")){ // if bus, else street
                    data.add(busStops.get(x));
                }else{
                    data.add(streets.get(x).getCoordinate(y));
                }
            }
            BusRoute busRoute = new BusRoute(into(jBusRoute.get("id")), data);
            busRoute.setColor(Color.valueOf((String) jBusRoute.get("color")));
            ArrayList<LocalTime> localTimes = new ArrayList <>();
            for (JSONArray s : (ArrayList<JSONArray>) jBusRoute.get("schedules")){
                localTimes.add(LocalTime.of(into(s.get(0)),into(s.get(1))));
            }
            busRoute.setRouteSchedulesByFirstDepartureTimes(localTimes);
            busRoutes.add(busRoute);
        }

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



