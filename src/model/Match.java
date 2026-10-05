package model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import model.enums.MatchStatus;
import util.CsvUtil;

/**
 * Tran dau - hop nhat Match cua Khanh (Seller) va Khoa (Buyer).
 * homeTeam/awayTeam/stadium la tham chieu duoc controller nap them de hien thi.
 */
public class Match extends BaseEntity {
    private String name;
    private String competition;     // Khoa: title
    private String homeTeamId;
    private String awayTeamId;
    private String stadiumId;
    private String sellerId;        // Seller tao tran
    private LocalDate matchDate;
    private LocalTime startTime;
    private MatchStatus status;
    private LocalDateTime createdAt;

    private Team homeTeam;
    private Team awayTeam;
    private Stadium stadium;

    public Match() {
        super();
    }

    public Match(String id, String name, String competition, String homeTeamId, String awayTeamId,
                 String stadiumId, String sellerId, LocalDate matchDate, LocalTime startTime) {
        super(id);
        this.name = name;
        this.competition = competition;
        this.homeTeamId = homeTeamId;
        this.awayTeamId = awayTeamId;
        this.stadiumId = stadiumId;
        this.sellerId = sellerId;
        this.matchDate = matchDate;
        this.startTime = startTime;
        this.status = MatchStatus.UPCOMING;
        this.createdAt = LocalDateTime.now();
    }

    /** Khanh: Match.openSale. */
    public void openSale() {
        if (this.status == MatchStatus.CANCELLED) {
            throw new IllegalStateException("Cancelled match cannot open sale");
        }
        if (hasStarted()) {
            throw new IllegalStateException("Match has already started");
        }
        this.status = MatchStatus.OPEN;
    }

    /** Khanh: Match.closeSale. */
    public void closeSale() {
        if (this.status != MatchStatus.OPEN) {
            throw new IllegalStateException("Only OPEN match can close sale");
        }
        this.status = MatchStatus.CLOSED;
    }

    public void cancel() {
        this.status = MatchStatus.CANCELLED;
    }

    public boolean isOnSale() {
        return this.status == MatchStatus.OPEN && !hasStarted();
    }

    public LocalDateTime getStartDateTime() {
        return LocalDateTime.of(this.matchDate, this.startTime);
    }

    public boolean hasStarted() {
        return !LocalDateTime.now().isBefore(getStartDateTime());
    }

    public String getName() { return this.name; }
    public void setName(String name) { this.name = name; }

    public String getCompetition() { return this.competition; }
    public void setCompetition(String competition) { this.competition = competition; }

    public String getHomeTeamId() { return this.homeTeamId; }
    public void setHomeTeamId(String homeTeamId) { this.homeTeamId = homeTeamId; }

    public String getAwayTeamId() { return this.awayTeamId; }
    public void setAwayTeamId(String awayTeamId) { this.awayTeamId = awayTeamId; }

    public String getStadiumId() { return this.stadiumId; }
    public void setStadiumId(String stadiumId) { this.stadiumId = stadiumId; }

    public String getSellerId() { return this.sellerId; }
    public void setSellerId(String sellerId) { this.sellerId = sellerId; }

    public LocalDate getMatchDate() { return this.matchDate; }
    public void setMatchDate(LocalDate matchDate) { this.matchDate = matchDate; }

    public LocalTime getStartTime() { return this.startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }

    public MatchStatus getStatus() { return this.status; }
    public void setStatus(MatchStatus status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return this.createdAt; }

    public Team getHomeTeam() { return this.homeTeam; }
    public void setHomeTeam(Team homeTeam) { this.homeTeam = homeTeam; }

    public Team getAwayTeam() { return this.awayTeam; }
    public void setAwayTeam(Team awayTeam) { this.awayTeam = awayTeam; }

    public Stadium getStadium() { return this.stadium; }
    public void setStadium(Stadium stadium) { this.stadium = stadium; }

    @Override
    public String toCsvLine() {
        return CsvUtil.join(getId(), this.name, this.competition, this.homeTeamId, this.awayTeamId, this.stadiumId,
                this.sellerId, this.matchDate, this.startTime, this.status, this.createdAt);
    }

    public static Match fromCsvLine(String csvLine) {
        String[] p = CsvUtil.split(csvLine);
        if (p.length < 11) {
            return null;
        }
        Match match = new Match(p[0], p[1], p[2], p[3], p[4], p[5], p[6], CsvUtil.toDate(p[7]), CsvUtil.toTime(p[8]));
        match.status = CsvUtil.toEnum(MatchStatus.class, p[9]);
        match.createdAt = CsvUtil.toDateTime(p[10]);
        return match;
    }

    @Override
    public String toString() {
        String teams = this.homeTeam != null && this.awayTeam != null
                ? this.homeTeam.getName() + " vs " + this.awayTeam.getName()
                : this.homeTeamId + " vs " + this.awayTeamId;
        String place = this.stadium != null ? this.stadium.getName() : this.stadiumId;
        return String.format("%-5s | %-26s | %-34s | %s %s | %-22s | %s",
                getId(), this.name, teams, this.matchDate, this.startTime, place, this.status);
    }
}
