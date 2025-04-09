import java.util.*;
import java.util.stream.Collectors;

public class ChargingHelper {

    public static boolean tryFixTurnusAtFailure(Turnus turnus, Set<Integer> usedChargingEventIds) {
        int failIndex = turnus.findFirstEnergyFailureIndex();
        if (failIndex == -1) return true; // nič nebolo chybné

        List<TurnusElement> elements = turnus.getElements();
        double currEnergy = 125.0;
        Trip prev = null;

        // Simuluj spotrebu po prvý chybný bod a zisti, koľko energie chýba
        for (int i = 0; i < failIndex; i++) {
            TurnusElement element = elements.get(i);

            if (element instanceof Trip currTrip) {
                if (prev != null) {
                    currEnergy -= StaticData.getDeadheadEnergy(prev.getEndStop(), currTrip.getStartStop());
                }
                currEnergy += currTrip.getEnergyDelta();
                prev = currTrip;
            } else {
                currEnergy += element.getEnergyDelta();
            }

            if (currEnergy > 125.0) currEnergy = 125.0;
        }

        // Počítať, koľko energie bude treba pre zvyšok (od failIndex ďalej)
        double needed = 0.0;
        List<TurnusElement> fromFail = elements.subList(failIndex, elements.size());
        Trip failPrev = null;
        for (TurnusElement e : fromFail) {
            if (e instanceof Trip currTrip) {
                if (failPrev != null) {
                    needed += StaticData.getDeadheadEnergy(failPrev.getEndStop(), currTrip.getStartStop());
                }
                needed += -currTrip.getEnergyDelta();
                failPrev = currTrip;
            }
        }

        double missingEnergy = needed - currEnergy;
        if (missingEnergy <= 0) return true; // chyba bola v čase, nie v energii

        int insertBefore = failIndex;
        int tripStartTime = elements.get(failIndex).getStartTime();

        // Hľadaj možné zastávky pre nabíjanie pred failIndex-om
        Set<Integer> prevStops = new HashSet<>();
        for (int i = 0; i < failIndex; i++) {
            TurnusElement e = elements.get(i);
            if (e instanceof Trip t) {
                prevStops.add(t.getEndStop());
            }
        }

        List<ChargingEvent> candidates = StaticData.chargingEvents.stream()
                .filter(e -> !usedChargingEventIds.contains(e.getId()))
                .filter(e -> e.getEndTime() <= tripStartTime)
                .filter(e -> prevStops.contains(e.getCharger().getLocation()))
                .sorted(Comparator.comparingInt(ChargingEvent::getStartTime))
                .collect(Collectors.toList());

        for (ChargingEvent c : candidates) {
            if (missingEnergy <= 0) break;
            elements.add(insertBefore, c);
            System.out.println("     + Inserting ChargingEvent " + c.getId() + " (+" + c.getChargedEnergy() + " kWh)");
            usedChargingEventIds.add(c.getId());
            insertBefore++;
            missingEnergy -= c.getChargedEnergy();
        }

        elements.sort(Comparator.comparingInt(TurnusElement::getStartTime));
        System.out.println("     -> Final feasibility: " + turnus.isFeasible());
        return turnus.isFeasible();
    }
}
