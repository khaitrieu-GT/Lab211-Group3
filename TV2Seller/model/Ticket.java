package TV2Seller.model;

public class Ticket {
   private int ticketId;
   private int matchId;
   private int seatId;
   private int sectionId;
   private String ticketType;
   private double price;
   private String status;
   
   public Ticket() {
   }

   public Ticket(int ticketId, int matchId, int seatId, int sectionId, String ticketType, double price, String status) {
    this.ticketId = ticketId;
    this.matchId = matchId;
    this.seatId = seatId;
    this.sectionId = sectionId;
    this.ticketType = ticketType;
    this.price = price;
    this.status = status;
   }

   public void createTicket(){
    System.out.println("Ticket created.");
   }

   public void updateTicket(){
    System.out.println("Ticket updated.");
   }

   public void putOnSale(){
    status="AVAILABLE";
    System.out.println("Ticket is available for sale.");
   }

   public void stopSale(){
    status = "CANCELLED";
    System.out.println("Ticket sale stopped.");
   }

   public void viewTicket(){
    System.out.println(this);
   }

   public void sellTicket(){
    status = "SOLD";
    System.out.println("Ticket sold successfully.");
   }

   public int getTicketId() {
    return ticketId;
   }

   public void setTicketId(int ticketId) {
    this.ticketId = ticketId;
   }

   public int getMatchId() {
    return matchId;
   }

   public void setMatchId(int matchId) {
    this.matchId = matchId;
   }

   public int getSeatId() {
    return seatId;
   }

   public void setSeatId(int seatId) {
    this.seatId = seatId;
   }

   public int getSectionId() {
    return sectionId;
   }

   public void setSectionId(int sectionId) {
    this.sectionId = sectionId;
   }

   public String getTicketType() {
    return ticketType;
   }

   public void setTicketType(String ticketType) {
    this.ticketType = ticketType;
   }

   public double getPrice() {
    return price;
   }

   public void setPrice(double price) {
    this.price = price;
   }

   public String getStatus() {
    return status;
   }

   public void setStatus(String status) {
    this.status = status;
   }

   @Override 
   public String toString(){
    return "Ticket{" + ticketId +
           ", matchId=" + matchId +
           ", seatId=" + seatId +
           ", sectionId=" + sectionId +
           ", ticketType='"+ ticketType + '\'' +
           ", price=" + price +
           ", status='" + status + '\'' +
           '}';
   }
}
