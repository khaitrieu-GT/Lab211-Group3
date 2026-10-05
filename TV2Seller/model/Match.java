package TV2Seller.model;

public class Match {
    private int matchId;
    private String homeTeam;
    private String awayTeam;
    private String matchDate;
    private int stadiumId;
    private String status;
    
    public Match() {
    }

    public Match(int matchId, String homeTeam, String awayTeam, String matchDate, int stadiumId, String status) {
        this.matchId = matchId;
        this.homeTeam = homeTeam;
        this.awayTeam = awayTeam;
        this.matchDate = matchDate;
        this.stadiumId = stadiumId;
        this.status = status;
    }

    public void addMatch(){
        System.out.println("Match added.");
    }

    public void updateMatch(){
        System.out.println("Match updated.");
    }
    
    public void deleteMatch(){
        System.out.println("Match deleted.");
    }

    public void viewMatch(){
        System.out.println(this);
    }

    public void openSale(){
        status = "OPEN";
        System.out.println("Ticket sale is OPEN.");
    }

    public void closeSale(){
        status = "CLOSED";
        System.out.println("Ticket sale is CLOSED");
    }

    public int getMatchId() {
        return matchId;
    }

    public void setMatchId(int matchId) {
        this.matchId = matchId;
    }

    public String getHomeTeam() {
        return homeTeam;
    }

    public void setHomeTeam(String homeTeam) {
        this.homeTeam = homeTeam;
    }

    public String getAwayTeam() {
        return awayTeam;
    }

    public void setAwayTeam(String awayTeam) {
        this.awayTeam = awayTeam;
    }

    public String getMatchDate() {
        return matchDate;
    }

    public void setMatchDate(String matchDate) {
        this.matchDate = matchDate;
    }

    public int getStadiumId() {
        return stadiumId;
    }

    public void setStadiumId(int stadiumId) {
        this.stadiumId = stadiumId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override 
    public String toString(){
       return "Match{" +
              ", homeTeam='" + homeTeam + '\'' +
              ", awayTeam='" + awayTeam + '\'' +
              ", matchDate='" + matchDate + '\'' +
              ", stadiumId=" + stadiumId + 
              ", status='" + status + '\'' +
              '}';
    }
}
