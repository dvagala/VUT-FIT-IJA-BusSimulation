package gui;

import java.time.LocalTime;
import java.util.List;

class RouteSchedule {

   private List<LocalTime> departures;

   public RouteSchedule() {
   }

   public RouteSchedule(List<LocalTime> departures) {
      this.departures = departures;
   }

   public List<LocalTime> getDepartures() {
      return departures;
   }

   public LocalTime getFirstStopDeparture(){
      return departures.get(0);
   }

   public LocalTime getLastStopDeparture(){
      return departures.get(departures.size() - 1);
   }

   public void setDepartures(List<LocalTime> departures) {
      this.departures = departures;
   }
}
