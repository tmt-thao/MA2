import java.util.ArrayList;
import java.util.List;

public class Turnus {
    private List<TurnusElement> elements = new ArrayList<>();
    private static final double BATTERY_CAPACITY = 140.0; // kWh
    private static final double MIN_BATTERY_LEVEL = 0.0; // kWh

    public Turnus() {
        // Default constructor
    }

    public Turnus(Turnus other) {
        this.elements = new ArrayList<>();

        for (TurnusElement e : other.getElements()) {
            if (e instanceof Trip trip) {
                this.elements.add(new Trip(trip));
            } else if (e instanceof ChargingEvent ce) {
                this.elements.add(new ChargingEvent(ce));
            }
        }
    }    

    public void addElement(TurnusElement element) {
        elements.add(element);
    }

    public List<TurnusElement> getElements() {
        return elements;
    }

    public boolean isFeasible() {
        double currEnergy = BATTERY_CAPACITY;
        Trip prev = null;
    
        for (TurnusElement element : elements) {
            // Ak nasleduje Trip po Trip-e, skontroluj deadhead
            if (element instanceof Trip currTrip) {
                if (prev != null) {
                    // Deadhead energia
                    double dhEnergy = StaticData.getDeadheadEnergy(prev.getEndStop(), currTrip.getStartStop());
                    currEnergy -= dhEnergy;
    
                    // (Voliteľné) deadhead časový check
                    int dhTime = StaticData.getTravelTime(prev.getEndStop(), currTrip.getStartStop());
                    if (prev.getEndTime() + dhTime > currTrip.getStartTime()) {
                        return false; // nestíha sa presun
                    }
                }
    
                currEnergy += currTrip.getEnergyDelta();
                prev = currTrip;
            }
            else {
                // ChargingEvent
                currEnergy += element.getEnergyDelta();
            }
    
            // Ochrana batérie
            if (currEnergy > BATTERY_CAPACITY) currEnergy = BATTERY_CAPACITY;
            if (currEnergy < MIN_BATTERY_LEVEL) return false;
        }
    
        return true;
    }
    

    public double getTotalEnergyUsed() {
        return elements.stream()
                .mapToDouble(TurnusElement::getEnergyDelta)
                .filter(d -> d < 0)
                .map(Math::abs)
                .sum();
    }

    public int getDeadheadCount() {
        int count = 0;
        Trip prev = null;

        for (TurnusElement e : elements) {
            if (e instanceof Trip curr) {
                if (prev != null && prev.getEndStop() != curr.getStartStop()) {
                        count++;
                }

                prev = curr;
            }
        }

        return count;
    }

    public double getDeadheadDistance() {
        int total = 0;
        Trip prev = null;

        for (TurnusElement e : elements) {
            if (e instanceof Trip curr) {
                if (prev != null && prev.getEndStop() != curr.getStartStop()) {
                        total += StaticData.getTravelDistance(prev.getEndStop(), curr.getStartStop());
                }

                prev = curr;
            }
        }

        return total;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Turnus:\n");
        for (TurnusElement element : elements) {
            sb.append(element.toString()).append("\n");
        }
        return sb.toString();
    }
}
