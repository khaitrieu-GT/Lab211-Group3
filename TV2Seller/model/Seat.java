package TV2Seller.model;

public class Seat {
    private int seatId;
    private int sectionId;
    private String row;
    private int seatNumber;
    private String status;
    private int version;

    public Seat() {
    }

    public Seat(int seatId, int sectionId,
                String row, int seatNumber,
                String status, int version) {

        this.seatId = seatId;
        this.sectionId = sectionId;
        this.row = row;
        this.seatNumber = seatNumber;
        this.status = status;
        this.version = version;
    }

    public String getStatus() {
        return status;
    }

    public void updateStatus(String status) {
        this.status = status;
        this.version++;
    }

    public int getVersion() {
        return version;
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

    public String getRow() {
        return row;
    }

    public void setRow(String row) {
        this.row = row;
    }

    public int getSeatNumber() {
        return seatNumber;
    }

    public void setSeatNumber(int seatNumber) {
        this.seatNumber = seatNumber;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    @Override
    public String toString() {
        return "Seat{" +
                "seatId=" + seatId +
                ", sectionId=" + sectionId +
                ", row='" + row + '\'' +
                ", seatNumber=" + seatNumber +
                ", status='" + status + '\'' +
                ", version=" + version +
                '}';
    }
}
