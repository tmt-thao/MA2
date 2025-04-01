public class ChargingEvent implements TurnusElement {
    private int id;
    private Charger charger;
    private int startTime;
    private int endTime;

    public ChargingEvent(int id, Charger charger, int startTime, int endTime) {
        this.id = id;
        this.charger = charger;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public ChargingEvent(ChargingEvent other) {
        this.id = other.id;
        this.charger = other.charger;
        this.startTime = other.startTime;
        this.endTime = other.endTime;
    }
    

    public int getId() {
        return id;
    }

    public Charger getCharger() {
        return charger;
    }

    public int getStartTime() {
        return startTime;
    }

    public int getEndTime() {
        return endTime;
    }

    public int getDuration() {
        return endTime - startTime;
    }

    public double getChargedEnergy() {
        return (double) getDuration() * charger.getChargingSpeed();
    }

    @Override
    public double getEnergyDelta() {
        return getChargedEnergy();
    }

    @Override
    public String toString() {
        return "ChargingEvent{" +
                "id=" + id +
                ", charger=" + charger +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                '}';
    }
}
