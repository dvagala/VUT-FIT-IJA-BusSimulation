package entities;

import java.time.LocalTime;
import java.util.List;


/**
 * Predstavuje jednu trasu autobusu, ktora obsauje zoznam zastavok a odchodu z nich.
 * @author Dominik Vagala (xvagal00)
 * @author Jakub Vinš (xvinsj00)
 */
public class RouteSchedule {

   private List<RouteScheduleEntry> entries;

   private Bus bus = null;

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
