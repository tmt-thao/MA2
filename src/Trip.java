public class Trip implements TurnusElement {
    private int id;
    private int startStop;
    private int endStop;
    private int startTime;
    private int endTime;
    private double energyConsumption;
    
    public Trip(int id, int startStop, int endStop, int startTime, int endTime, double energyConsumption) {
        this.id = id;
        this.startStop = startStop;
        this.endStop = endStop;
        this.startTime = startTime;
        this.endTime = endTime;
        this.energyConsumption = energyConsumption;
    }

    public Trip(Trip other) {
        this.id = other.id;
        this.startStop = other.startStop;
        this.endStop = other.endStop;
        this.startTime = other.startTime;
        this.endTime = other.endTime;
        this.energyConsumption = other.energyConsumption;
    }
    

    public int getId() {
        return id;
    }

    public int getStartStop() {
        return startStop;
    }

    public int getEndStop() {
        return endStop;
    }

    public int getStartTime() {
        return startTime;
    }

    public int getEndTime() {
        return endTime;
    }

    public double getEnergyConsumption() {
        return energyConsumption;
    }

    public int getDuration() {
        return endTime - startTime;
    }

    @Override
    public double getEnergyDelta() {
        return -energyConsumption;
    }

    @Override
    public String toString() {
        return "Trip{" +
                "id=" + id +
                ", startStop=" + startStop +
                ", endStop=" + endStop +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", energyConsumption=" + energyConsumption +
                '}';
    }
}