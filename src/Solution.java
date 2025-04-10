import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Solution {
    private List<Turnus> turnuses = new ArrayList<>();
    private Set<Integer> usedChargingEventIds = new HashSet<>();

    public Solution() {
        // Default constructor
    }

    public Solution(Solution other) {
        this.usedChargingEventIds = new HashSet<>(other.usedChargingEventIds);

        this.turnuses = new ArrayList<>();
        for (Turnus t : other.getTurnuses()) {
            Turnus copied = new Turnus(t);
            this.turnuses.add(copied);
        }
    }

    public void addTurnus(Turnus turnus) {
        turnuses.add(turnus);
    }

    public List<Turnus> getTurnuses() {
        return turnuses;
    }

    public boolean isFeasible() {
        for (Turnus turnus : turnuses) {
            if (!turnus.isFeasible()) {
                return false;
            }
        }
        return true;
    }

    public int getNumberOfTurnuses() {
        return turnuses.size();
    }

    public double getTotalEnergyUsed() {
        return turnuses.stream()
                .mapToDouble(Turnus::getTotalEnergyUsed)
                .sum();
    }

    public double getFitness() {
        double weightTurnuses = 1000.0;
        double weightDeadhead = 100.0;
        double weightCharging = 2.0;
    
        double deadheads = turnuses.stream()
                .mapToDouble(Turnus::getDeadheadDistance)
                .sum();
        double totalCharged = turnuses.stream()
            .flatMap(t -> t.getElements().stream())
            .filter(e -> e instanceof ChargingEvent)
            .mapToDouble(TurnusElement::getEnergyDelta)
            .map(Math::abs)
            .sum();
    
        return getNumberOfTurnuses() * weightTurnuses
             + deadheads * weightDeadhead
             + totalCharged * weightCharging;
    }

    public Set<Integer> getUsedTripIds() {
        Set<Integer> ids = new HashSet<>();
        for (Turnus t : turnuses) {
            for (TurnusElement e : t.getElements()) {
                if (e instanceof Trip trip) {
                    ids.add(trip.getId());
                }
            }
        }
        return ids;
    }

    public Set<Integer> getUsedChargingEventIds() {
        return usedChargingEventIds;
    }    

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("=== Solution ===\n");
        int i = 1;
        for (Turnus t : turnuses) {
            sb.append("Turnus ").append(i++).append(":\n");
            sb.append(t.toString()).append("\n");
        }
        sb.append("Total buses: ").append(getNumberOfTurnuses()).append("\n");
        sb.append("Total energy used: ").append(getTotalEnergyUsed()).append(" kWh\n");
        return sb.toString();
    }
}
