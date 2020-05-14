
package gui;


import entities.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
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
import java.util.List;

import static java.time.temporal.ChronoUnit.MINUTES;

/**
 * Tato trieda ovlada hlavnu smycku programu, kde sa vykonava simulacia a obsluhuju sa vstupy od uzivatela.
 * @author Dominik Vagala (xvagal00)
 * @author Jakub Vinš (xvinsj00)
 */
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
    private HBox modifyRouteHbox;

    @FXML
    private Button modifyRouteBtn;

    @FXML
    public GridPane routeDeparturesGridPane;

    @FXML
    public Pane mapPane;

    @FXML
    public ScrollPane scrollPane;

    // Simulation clock time
    private LocalTime currentTime = SimulationSettings.startTime;
    List<BusRoute> busRoutes = new ArrayList<>();

    // Buses that are currently shown on map
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

        // Simulation main loop
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
            if(nowModifyingRoute){
                for (Bus bus : visibleBuses) {
                    bus.getCurrentRouteSchedule().setBus(null);
                    Platform.runLater(() -> {
                        mapPane.getChildren().remove(bus.getNode());
                    });
                }
                visibleBuses.clear();
                currentTime = SimulationSettings.startTime;
                return;
            }

            clockText.setText(currentTime.format(DateTimeFormatter.ofPattern("HH:mm:ss")));
            handleBusesVisibilityOnMap();

            // Move all buses based on time or distance they've travelled
            for (Bus visibleBus : visibleBuses) {
                visibleBus.updateNodePosition(currentTime);
            }

            currentTime = currentTime.plusNanos((long) (SimulationSettings.updateIntervalMs*1000000*SimulationSettings.speedRatio));
            }
        }, 0, SimulationSettings.updateIntervalMs);
    }

    // This will place bus on map if it should be there according to their Route Schedule
    // Also removes bus if drived to the last stop
    private void handleBusesVisibilityOnMap(){
        for(BusRoute busRoute : busRoutes){
            for(RouteSchedule routeSchedule : busRoute.getRouteSchedules()){
                if(currentTime.until(routeSchedule.getFirstStopDepartureTime(), MINUTES) < Bus.minutesToWaitAtStopAtLeast && currentTime.compareTo(routeSchedule.getLastStopNonDelayedDepartureTime()) < 0){
                    if(routeSchedule.getBus() == null){
                        Bus bus = new Bus(busRoute, routeSchedule);
                        bus.setOnBusClickListener(() -> Platform.runLater(() -> {
                            for(BusRoute r : busRoutes){
                                r.getNode().setOpacity(0.0);
                            }
                            Pane nodeLayout = (Pane) busRoute.getNode();
                            nodeLayout.setOpacity(0.5);

                            routeDeparturesGridPane.getChildren().clear();
                            fillRouteDeparturesGridPane(bus);
                            routeDeparturesNumberText.setText("Route number " + bus.getBusRoute().getRouteNumber());
                            modifyRouteBtn.setVisible(true);
                        }));
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
                        });
                    }
                }
            }
        }
    }

    // When user click to bus to highlight route and see departure times, here will be that clicked bus
    private Bus selectedBus = null;

    // Show departure times to user
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

    // Long to int
    private int into(Object x){ //Long to int
        return ((Long)x).intValue();
    }

    // Parse data about map and buses from json file
    private void setUpData() {
        JSONObject jo = new JSONObject();
        JSONParser parser = new JSONParser();
        try {
            Object obj = parser.parse(new FileReader("data/data.json"));
//            Object obj = parser.parse(new FileReader("data/data-simple.json"));
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

        for(BusRoute route : busRoutes){
            mapPane.getChildren().addAll(route.getNode());
        }

        for(BusStop s : busStops){
            mapPane.getChildren().addAll(s.getNode());
            s.setOnBusStopClickListener((Polygon polygon) -> {
               if(nowModifyingRoute) {
                   Platform.runLater(() -> {
                       if(modifiedBusRoute.getRoutePoints().size() > 0){
                           if(modifiedBusRoute.getLastRoutePoint().getStreetAfter() == null){
                               routeModifyWarningText.setText("Click on street name that comes out of the route point!");
                               routeModifyWarningText.setVisible(true);
                               return;
                           }
                       }

                       routeModifyWarningText.setVisible(false);
                       polygon.setFill(Color.RED);
                       s.setStreetAfter(null);
                       modifiedBusRoute.addPoint(s);
                       modifiedBusRoute.addBusStop(s);
                       modifiedBusRoute.updateNode();

                       modifiedBusRoute.clearRouteSchedules();
                       modifiedBusRoute.setRouteSchedulesByFirstDepartureTimes(modifiedBusRouteFirstDepartureTimes);

                       for(RouteSchedule routeSchedule : modifiedBusRoute.getRouteSchedules()){
                           if(routeSchedule.getFirstStopDepartureTime() == selectedBus.getCurrentRouteSchedule().getFirstStopDepartureTime()){
                               selectedBus.setCurrentRouteSchedule(routeSchedule);
                           }
                       }

                       fillRouteDeparturesGridPane(selectedBus);
                   });
               }
            });
        }

        for(Street s : streets){
            mapPane.getChildren().addAll(s.getNode());

            s.setOnTrafficChangeListener(new Street.Listener() {
                @Override
                public void trafficHasChanged(int trafficRate) {
                    for(BusRoute busRoute : busRoutes){
                        busRoute.recalculateDepartureTimes(currentTime);
                        if(selectedBus != null){
                            fillRouteDeparturesGridPane(selectedBus);
                        }
                    }
                }

                @Override
                public void streetEndingCircleWasClicked(Circle c) {
                    if(nowModifyingRoute) {
                        Platform.runLater(() -> {
                            if(modifiedBusRoute.getRoutePoints().size() > 0){
                                if(modifiedBusRoute.getLastRoutePoint().getStreetAfter() == null){
                                    routeModifyWarningText.setText("Click on street name that comes out of the route point!");
                                    routeModifyWarningText.setVisible(true);
                                    return;
                                }
                            }
                            if(modifiedBusRoute.getRoutePoints().size() == 0){
                                routeModifyWarningText.setText("First route point has to be a Bus Stop!");
                                routeModifyWarningText.setVisible(true);
                                return;
                            }

                            routeModifyWarningText.setVisible(false);
                            c.setFill(Color.RED);
                            IRoutePoint routePoint = new Coordinate((int) c.getCenterX(), (int) c.getCenterY());
                            routePoint.setStreetAfter(null);
                            modifiedBusRoute.addRoutePoint(routePoint);
                            modifiedBusRoute.updateNode();
                        });
                    }
                }

                @Override
                public void streetNameWasClicked(Text streetNameTextNode) {
                    if(nowModifyingRoute) {
                        Platform.runLater(() -> {
                            if(modifiedBusRoute.getRoutePoints().size() > 0){
                                if(modifiedBusRoute.getLastRoutePoint().getStreetAfter() != null){
                                    routeModifyWarningText.setText("Click on route point!");
                                    routeModifyWarningText.setVisible(true);
                                    return;
                                }
                            }

                            if(modifiedBusRoute.getRoutePoints().size() == 0){
                                routeModifyWarningText.setText("Click on a Bus Stop to start route!");
                                routeModifyWarningText.setVisible(true);
                                return;
                            }

                            routeModifyWarningText.setVisible(false);
                            streetNameTextNode.setFill(Color.RED);
                            new java.util.Timer().schedule(
                                    new java.util.TimerTask() {
                                        @Override
                                        public void run() {
                                            streetNameTextNode.setFill(Color.BLACK);
                                        }
                                    },
                                    700
                            );
                            modifiedBusRoute.getLastRoutePoint().setStreetAfter(s);
                        });
                    }
                }
            });
        }
    }

    public void onCloseDeparturesBtnClick() {
        Platform.runLater(() -> {
            for(BusRoute r : busRoutes){
                r.getNode().setOpacity(0.0);
            }

            routeDeparturesNumberText.setText("Click on bus to see departures");
            routeDeparturesGridPane.getChildren().clear();
            modifyRouteBtn.setVisible(false);
        });
    }

    public void onSetTimeBtnClick() {
        Platform.runLater(() -> {
            if (setTimeTextField.getText().isEmpty()) {
                return;
            }

            try {
                currentTime = LocalTime.parse(setTimeTextField.getText(), DateTimeFormatter.ofPattern("HH:mm:ss"));
            } catch (Exception e) {
                try {
                    currentTime = LocalTime.parse(setTimeTextField.getText(), DateTimeFormatter.ofPattern("HH:mm"));
                } catch (Exception e2) {
                    setTimeWrongFormatText.setVisible(true);
                    setTimeTextField.clear();
                    return;
                }
            }

            setTimeTextField.clear();
            setTimeWrongFormatText.setVisible(false);

            SimulationSettings.startTime = currentTime;
            onCloseDeparturesBtnClick();

            for (Bus bus : visibleBuses) {
                bus.getCurrentRouteSchedule().setBus(null);
                mapPane.getChildren().remove(bus.getNode());
            }
            visibleBuses.clear();
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


    // When user wants to modify route it will switch to this mode
    private boolean nowModifyingRoute = false;
    private BusRoute modifiedBusRoute;
    private List<LocalTime> modifiedBusRouteFirstDepartureTimes = new ArrayList<>();

    @FXML
    private Text routeModifyWarningText;

    // This will handle entering and exiting from modifying route mode
    public void onModifyRouteBtnClick() {
        if(nowModifyingRoute){
            nowModifyingRoute = false;
            modifyRouteBtn.setText("Modify route");
            modifyRouteHbox.getChildren().remove(0);
            routeModifyWarningText.setVisible(false);

            for(IRoutePoint routePoint : modifiedBusRoute.getRoutePoints()){
                if(routePoint instanceof BusStop){
                    BusStop busStop = (BusStop) routePoint;
                    VBox vBox = (VBox) busStop.getNode();
                    Polygon polygon = (Polygon) vBox.getChildren().get(0);
                    polygon.setFill(Color.BLUE);
                }else{
                    Pane pane = (Pane) routePoint.getStreetAfter().getNode();
                    for(Node child : pane.getChildren()){
                        if(child instanceof Circle){
                            Circle c = (Circle) child;
                            c.setFill(Color.PINK);
                        }
                    }
                }
            }

            for(BusRoute r : busRoutes){
                r.getNode().setOpacity(0.0);
            }

            onCloseDeparturesBtnClick();

        }else{
            nowModifyingRoute = true;
            Text modifyRouteText = new Text("Select route points with following streets.");
            modifyRouteText.setWrappingWidth(134);
            modifyRouteHbox.getChildren().add(0, modifyRouteText);
            modifyRouteBtn.setText("ok");

            modifiedBusRoute = selectedBus.getBusRoute();
            modifiedBusRoute.clearBusStopss();
            modifiedBusRoute.clearRoutePoints();

            for (RouteSchedule routeSchedule : selectedBus.getBusRoute().getRouteSchedules()){
                modifiedBusRouteFirstDepartureTimes.add(routeSchedule.getFirstStopDepartureTime());
            }

            Platform.runLater(() -> {
                Pane pane = (Pane) modifiedBusRoute.getNode();
                pane.getChildren().clear();
                routeDeparturesGridPane.getChildren().clear();
            });
        }
    }
}



