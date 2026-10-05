package TV2Seller.model;

public class Seller {
    private int sellerId;
    private String name;
    private String email;
    private String status;
    
    public Seller() {
    }

    public int getSellerId() {
        return sellerId;
    }

    public void setSellerId(int sellerId) {
        this.sellerId = sellerId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Seller(int sellerId, String name, String email, String status) {
        this.sellerId = sellerId;
        this.name = name;
        this.email = email;
        this.status = status;
    }

    public void createMatch(){
        System.out.println("Seller creates a match.");
    }

    public void updateMatch(){
        System.out.println("Seller updates a match.");
    }

    public void deleteMatch(){
        System.out.println("Seller deletes a match.");
    }

    public void viewMatch(){
        System.out.println("Seller views matches.");
    }

    public void createTicket(){
        System.out.println("Seller creates a ticket.");
    }

    public void updateTicket(){
        System.out.println("Seller updates a ticket.");
    }

    public void sellTicket(){
        System.out.println("Seller sells a ticket");
    }

    @Override 
    public String toString(){
        return "Seller{" +
               "sellerId=" + sellerId +
               ", name='" + name + '\'' +
               ", email='" + email + '\'' +
               ", status='" + status + '\'' +
               '}';
    }
}
