package TV2Seller.model;

import java.util.ArrayList;
import java.util.List;

public class Section {
   private int sectionId;
   private int stadiumId;
   private String name;
   private int capacity;
   private String status;

   private List<Seat> seats = new ArrayList<>();

   public Section() {
   }

   public Section(int sectionId, int stadiumId, String name, int capacity, String status) {
    this.sectionId = sectionId;
    this.stadiumId = stadiumId;
    this.name = name;
    this.capacity = capacity;
    this.status = status;
   }

   public int getSectionId() {
    return sectionId;
   }

   public void setSectionId(int sectionId) {
    this.sectionId = sectionId;
   }

   public int getStadiumId() {
    return stadiumId;
   }

   public void setStadiumId(int stadiumId) {
    this.stadiumId = stadiumId;
   }

   public String getName() {
    return name;
   }

   public void setName(String name) {
    this.name = name;
   }

   public int getCapacity() {
    return capacity;
   }

   public void setCapacity(int capacity) {
    this.capacity = capacity;
   }

   public String getStatus() {
    return status;
   }

   public void setStatus(String status) {
    this.status = status;
   }

   public List<Seat> getSeats() {
    return seats;
   }

   public void setSeats(List<Seat> seats) {
    this.seats = seats;
   }

   public void updateSection(String name, int capacity){
    this.name = name;
    this.capacity = capacity;
   }

   public void updateStatus(String status){
    this.status = status;
   }

   @Override
   public String toString() {
    return "Section{" + 
           "sectionId=" + sectionId +
           ", stadiumId" + stadiumId +
           ", name='" + name + '\'' +
           ", capacity=" + capacity +
           ", status='" + status + '\'' +
           '}'; 
   }

   
   
}
