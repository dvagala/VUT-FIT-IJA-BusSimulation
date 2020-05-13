
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
//                System.out.println("neu frame, curr time: " + currentTime);
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
//                    if(Bus.getVisibleBusByRouteAndSchedule(visibleBuses, busRoute, routeSchedule) == null){
                    if(routeSchedule.getBus() == null){
                        Bus bus = new Bus(busRoute, routeSchedule);
                        bus.setOnBusClickListener(new Bus.OnBusClickListener() {
                            @Override
                            public void busWasClicked() {
                                Platform.runLater(() -> {

                                    for(BusRoute r : busRoutes){
                                        r.getNode().setOpacity(0.0);
                                    }
                                    Pane nodeLayout = (Pane) busRoute.getNode();
                                    nodeLayout.setOpacity(0.5);

                                    routeDeparturesGridPane.getChildren().clear();
                                    fillRouteDeparturesGridPane(bus);
                                    routeDeparturesNumberText.setText("Route number " + bus.getBusRoute().getRouteNumber());
                                });
                            }
                        });
                        visibleBuses.add(bus);
                        routeSchedule.setBus(bus);
                        Platform.runLater(() -> mapPane.getChildren().add(bus.getNode()));
                    }
                }else if(currentTime.compareTo(routeSchedule.getLastStopDepartureTime()) > 0){
                    Bus bus = Bus.getVisibleBusByRouteAndSchedule(visibleBuses, busRoute, routeSchedule);
                    if(bus != null){
                        visibleBuses.remove(bus);
                        routeSchedule.setBus(null);
                        Platform.runLater(() ->{
                            mapPane.getChildren().remove(bus.getNode());
                            if(bus == selectedBus){
                                onCloseDeparturesBtnClick();
                            }
                        });
                    }
                }
            }
        }
    }

    private Bus selectedBus = null;

    private void fillRouteDeparturesGridPane( Bus selectedBus){
        routeDeparturesGridPane.getChildren().clear();
        this.selectedBus = selectedBus;
        for (int i = 0; i < selectedBus.getCurrentRouteSchedule().getEntries().size(); i++) {
            RouteScheduleEntry entry = selectedBus.getCurrentRouteSchedule().getEntries().get(i);
            routeDeparturesGridPane.add(new Text(entry.getBusStop().getName()), 0, i);
            Text text = new Text(String.valueOf(entry.getDepartureTime()));
            if(entry.isDelayed()){
                text.setFill(Color.RED);
            }
            routeDeparturesGridPane.add(text, 1, i);
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
//            Object obj = parser.parse(new FileReader("data-simple.json"));
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

        //Load Streets
        SortedSet<String> keys = new TreeSet<String>(jStreets.keySet());
        for (String var : keys){
            JSONObject s = (JSONObject) jStreets.get(var); //street
            ArrayList<JSONArray> coors = (ArrayList<JSONArray>) s.get("coors");
            List<Coordinate> coordinates = new ArrayList<>();
            for (JSONArray jC: coors){
                Coordinate c = new Coordinate(into(jC.get(0)), into(jC.get(1))); //x,y value of coord
                coordinates.add(c);
            }

            Street street = new Street(coordinates, (String)s.get("name"));
            street.setColor(Color.valueOf((String) s.get("color")));
            streets.add(street);
        }

        //Load BusStops
        keys = new TreeSet<String>(jBusStops.keySet());
        for (String var: keys){
            JSONObject jBusStop = (JSONObject) jBusStops.get(var);
            JSONArray jC = (JSONArray) jBusStop.get("c");
            BusStop busStop = new BusStop(new Coordinate(into(jC.get(0)), into(jC.get(1))), (String) jBusStop.get("name"));
            busStops.add(busStop);
        }

        //Load BusRoutes
        for (Object var: jBusRoutes.keySet()) {
            JSONObject jBusRoute = (JSONObject) jBusRoutes.get(var);
            List<IRoutePoint> data = new ArrayList <>();
            for (JSONArray a : (ArrayList<JSONArray>) jBusRoute.get("data")){
                int z = into(a.get(0)); // value contains idx of street that is right behind this route point
                int x = into(a.get(2)); // value contains idx of busStop or street
                int y = into(a.get(3)); // value says which streetCoordinate should be used
                IRoutePoint point;
                if(a.get(1).equals("b")){ // if bus, else street
                    point = busStops.get(x);
                }else{
                    point = streets.get(x).getCoordinate(y);
                }
                if(z == -1){   // last point
                    point.setStreetAfter(null);
                }
                else{
                    point.setStreetAfter(streets.get(z));
                }
                data.add(point);
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

//        System.out.println(streets.size());

        for(BusRoute route : busRoutes){
            mapPane.getChildren().addAll(route.getNode());
        }

        for(Street s : streets){
            mapPane.getChildren().addAll(s.getNode());

            s.setOnTrafficChangeListener(trafficRate -> {
                for(BusRoute busRoute : busRoutes){
                    busRoute.recalculateDepartureTimes(currentTime);
                    if(selectedBus != null){
//                        System.out.println("fill de p times");
                        fillRouteDeparturesGridPane(selectedBus);
                    }
                }
            });
        }

        for(BusStop s : busStops){
            mapPane.getChildren().addAll(s.getNode());
        }

    }

    public void onCloseDeparturesBtnClick() {

        Platform.runLater(() -> {
            for(BusRoute r : busRoutes){
                r.getNode().setOpacity(0.0);
            }

            routeDeparturesNumberText.setText("Click on bus to see departures");
            routeDeparturesGridPane.getChildren().clear();
        });

        selectedBus = null;
    }

    public void onSetTimeBtnClick() {
        Platform.runLater(() -> {
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
        });
    }

    public void onSetSpeedBtnClick() {
        Platform.runLater(() -> {
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
        });
    }
}



