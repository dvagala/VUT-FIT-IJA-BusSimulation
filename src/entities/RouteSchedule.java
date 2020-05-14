package entities;

import java.time.LocalTime;
import java.util.List;

public class RouteSchedule {

   //   private List<LocalTime> departures;
   private List<RouteScheduleEntry> entries;

   private Bus bus = null;
   private boolean isBusOnMap = false;

   public RouteSchedule() {
   }

   public RouteSchedule(List<RouteScheduleEntry> entries) {
      this.entries = entries;
   }

   public List<RouteScheduleEntry> getEntries() {
      return entries;
   }

   public void setEntries(List<RouteScheduleEntry> entries) {
      this.entries = entries;
   }

   public LocalTime getFirstStopDepartureTime(){
      return entries.get(0).getDepartureTime();
   }

   public LocalTime getLastStopDepartureTime(){
      return entries.get(entries.size() - 1).getDepartureTime();
   }

   public LocalTime getLastStopNonDelayedDepartureTime(){
      return entries.get(entries.size() - 1).getNonDelayedDepartureTime();
   }

   public boolean isBusOnMap() {
      return isBusOnMap;
   }

   public void setBusOnMap(boolean busOnMap) {
      isBusOnMap = busOnMap;
   }

   public Bus getBus() {
      return bus;
   }

   public void setBus(Bus bus) {
      this.bus = bus;
   }

   public int getIndexOfEntryByBusStop(BusStop busStop){
      for (int i = 0; i < entries.size(); i++) {
         if(entries.get(i).getBusStop() == busStop){
            return i;
         }
      }
      return 0;
   }

}
