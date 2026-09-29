package model;

public class Seat extends BaseEntity {
    private String sectionId;
    private String rowNumber;
    private int seatNumber;
    private SeatStatus status;
    private int version; // Phục vụ Optimistic Locking cho TV4

    public Seat() {
        super();
    }

    public Seat(String id, String sectionId, String rowNumber, int seatNumber, SeatStatus status, int version) {
        super(id);
        this.sectionId = sectionId;
        this.rowNumber = rowNumber;
        this.seatNumber = seatNumber;
        this.status = status;
        this.version = version;
    }

    public String getSectionId() { return this.sectionId; }
    public void setSectionId(String sectionId) { this.sectionId = sectionId; }

    public String getRowNumber() { return this.rowNumber; }
    public void setRowNumber(String rowNumber) { this.rowNumber = rowNumber; }

    public int getSeatNumber() { return this.seatNumber; }
    public void setSeatNumber(int seatNumber) { this.seatNumber = seatNumber; }

    public SeatStatus getStatus() { return this.status; }
    public void setStatus(SeatStatus status) { this.status = status; }

    public int getVersion() { return this.version; }
    public void setVersion(int version) { this.version = version; }

    @Override
    public String toCsvLine() {
        return String.join(",", this.getId(), this.sectionId, this.rowNumber, String.valueOf(this.seatNumber), this.status.name(), String.valueOf(this.version));
    }

    public static Seat fromCsvLine(String csvLine) {
        String[] parts = csvLine.split(",");
        if (parts.length < 6) return null;
        return new Seat(
            parts[0].trim(),
            parts[1].trim(),
            parts[2].trim(),
            Integer.parseInt(parts[3].trim()),
            SeatStatus.valueOf(parts[4].trim()),
            Integer.parseInt(parts[5].trim())
        );
    }
}