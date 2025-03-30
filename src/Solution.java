import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Solution {
    private List<Turnus> turnuses = new ArrayList<>();

    public Solution() {
        // Default constructor
    }

    public Solution(Solution other) {
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
        double deadheads = turnuses.stream()
                .mapToDouble(Turnus::getDeadheadDistance)
                .sum();

        return getNumberOfTurnuses() * 1000 + getTotalEnergyUsed() + deadheads * 100;
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
