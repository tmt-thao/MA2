public class Charger {
    private int index;
    private int location;
    private int chargingSpeed;

    public Charger(int index, int location, int chargingSpeed) {
        this.index = index;
        this.location = location;
        this.chargingSpeed = chargingSpeed;
    }

    public int getIndex() {
        return index;
    }

    public int getLocation() {
        return location;
    }

    public int getChargingSpeed() {
        return chargingSpeed;
    }

    @Override
    public String toString() {
        return "Charger{" +
                "index=" + index +
                ", location=" + location +
                ", chargingSpeed=" + chargingSpeed +
                '}';
    }
}
