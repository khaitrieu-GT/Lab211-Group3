package model;

import java.time.LocalDate;
import java.time.LocalTime;

public class Match {
    private Long id;
    private String name;
    private String title;
    private LocalDate matchDate;
    private LocalTime startTime;
    private String status;
    private Team homeTeam;
    private Team awayTeam;
    private Stadium stadium;

    public Match(Long id, String name, String title, LocalDate matchDate, LocalTime startTime,
                 Team homeTeam, Team awayTeam, Stadium stadium) {
        this.id = id;
        this.name = name;
        this.title = title;
        this.matchDate = matchDate;
        this.startTime = startTime;
        this.homeTeam = homeTeam;
        this.awayTeam = awayTeam;
        this.stadium = stadium;
        this.status = "UPCOMING";
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getTitle() { return title; }
    public LocalDate getMatchDate() { return matchDate; }
    public LocalTime getStartTime() { return startTime; }
    public String getStatus() { return status; }
    public Team getHomeTeam() { return homeTeam; }
    public Team getAwayTeam() { return awayTeam; }
    public Stadium getStadium() { return stadium; }
    public void setStatus(String status) { this.status = status; }

    @Override
    public String toString() {
        return id + " | " + name + " | " + homeTeam.getName() + " vs " + awayTeam.getName()
                + " | " + matchDate + " " + startTime + " | " + status;
    }
}
