import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

public class MemeticAlgorithm {
    private int populationSize;
    private int generations;
    private double mutationRate;

    private List<Solution> population;
    private Random random = new Random();

    private List<Trip> trips;

    private Solution bestSolution;
    private double bestTurnuses = Integer.MAX_VALUE;

    public MemeticAlgorithm(int popSize, int generations, double mutationRate, List<Trip> trips) {
        this.populationSize = popSize;
        this.generations = generations;
        this.mutationRate = mutationRate;
        this.population = new ArrayList<>();
        this.trips = trips;
    }

    public void run() {
        Solution bestSol = null;
        int bestNoOfTurnuses = Integer.MAX_VALUE;

        initializePopulation();
        System.out.println("Best generated turnus count: " + getBestSolution().getNumberOfTurnuses());

        int tripCount = 0;
        for (Turnus t : bestSolution.getTurnuses()) {
            tripCount += t.getElements().size();
        }
        System.out.println(tripCount + " " + bestSolution.getNumberOfTurnuses());

        for (int gen = 0; gen < generations; gen++) {
            List<Solution> newPopulation = new ArrayList<>();

            while (newPopulation.size() < populationSize) {
                Solution parent1 = selectParent();
                Solution parent2 = selectParent();

                Solution child = crossover(parent1, parent2);
                simulation(child);

                mutate(child);
                ensureAllTripsPresent(child, trips);
                simulation(child);

                localImprove(child);
                ensureAllTripsPresent(child, trips);

                simulation(child);

                newPopulation.add(child);
            }

            // mutationRate = mutationRate * (1.0 - (double) gen / generations);
            population = newPopulation;

            for (Solution s : population) {
                if (s.getNumberOfTurnuses() < bestNoOfTurnuses) {
                    bestNoOfTurnuses = s.getNumberOfTurnuses();
                    bestSol = s;
                }
            }

            System.out.println("Generation " + gen + ": Turnuses = " + bestNoOfTurnuses + "; No. of trips: " + bestSol.getUsedTripIds().size());
            // System.out.println("Generation " + gen + ": Turnuses = " + getBestSolution().getNumberOfTurnuses() + "; No. of trips: " + getBestSolution().getUsedTripIds().size());
        }

        System.out.println("Final best solution: " + bestSol);

        
        int tc = 0;
        for (Turnus t : bestSol.getTurnuses()) {
            tc += t.getElements().size();
        }
        System.out.println(tc + " " + bestSol.getNumberOfTurnuses());
    }

    private void initializePopulation() {
        while (population.size() < populationSize) {
            System.out.println("-> Generating solution #" + (population.size() + 1));
            Solution solution = generateRandomSolution();
            boolean fixable = true;

            for (Turnus t : solution.getTurnuses()) {
                if (!t.isFeasible()) {
                    System.out.println("   - Checking turnus " + t + " for feasibility...");
                    boolean fixed = ChargingHelper.tryFixTurnusAtFailure(t, solution.getUsedChargingEventIds());
                    if (!fixed) {
                        System.out.println("     ✖ Could not fix turnus " + t);
                        fixable = false;
                        break;
                    }
                    System.out.println("     ✔ Turnus " + t + " fixed");
                }
            }

            if (fixable && solution.isFeasible()) {
                updateBestSolution(solution);
                population.add(solution);
                System.out.println(population.size() + " / " + populationSize + " generated.");
            }
        }
    }

    private void simulation(Solution solution) {
        boolean fixable = true;

        for (Turnus t : solution.getTurnuses()) {
            if (!t.isFeasible()) {
                boolean fixed = ChargingHelper.tryFixTurnusAtFailure(t, solution.getUsedChargingEventIds());
                if (!fixed) {
                    fixable = false;
                    break;
                }
            }
        }

        if (fixable && solution.isFeasible()) {
            updateBestSolution(solution);
        }
    }

    private Solution generateRandomSolution() {
        List<Trip> shuffledTrips = new ArrayList<>(trips);
        Collections.shuffle(shuffledTrips); // začni náhodne

        Solution solution = new Solution();

        for (Trip trip : shuffledTrips) {
            boolean assigned = false;

            // skúsiť priradiť do existujúceho turnusu
            for (Turnus turnus : solution.getTurnuses()) {
                Turnus test = new Turnus(turnus);
                test.addElement(trip);
                test.getElements().sort(Comparator.comparingInt(TurnusElement::getStartTime));

                if (test.isFeasible()) {
                    turnus.addElement(trip);
                    turnus.getElements().sort(Comparator.comparingInt(TurnusElement::getStartTime));
                    assigned = true;
                    break;
                }
            }

            if (!assigned) {
                Turnus newTurnus = new Turnus();
                newTurnus.addElement(trip);
                solution.addTurnus(newTurnus);
            }
        }

        return solution;
    }

    private Solution generateGreedySolution() {
        List<Trip> sortedTrips = new ArrayList<>(trips);
        sortedTrips.sort(Comparator.comparingInt(Trip::getStartTime));

        Solution solution = new Solution();
        List<Turnus> turnuses = new ArrayList<>();

        for (Trip trip : sortedTrips) {
            boolean assigned = false;

            for (Turnus turnus : turnuses) {
                // posledný prvok v turnuse (ak existuje)
                List<TurnusElement> elements = turnus.getElements();

                if (!elements.isEmpty()) {
                    // ešte sa nerieši charging event
                    Trip last = (Trip) elements.get(elements.size() - 1);

                    // ak časovo stíha a deadhead nie je príliš ďaleko (tu to ešte nezohľadňujeme
                    // úplne presne)
                    if (last.getEndTime() + StaticData.getTravelTime(last.getEndStop(), trip.getStartStop()) <= trip
                            .getStartTime()) {
                        turnus.addElement(trip);
                        assigned = true;
                        break;
                    }
                } else {
                    // prázdny turnus – pridaj rovno
                    turnus.addElement(trip);
                    assigned = true;
                    break;
                }
            }

            if (!assigned) {
                Turnus newTurnus = new Turnus();
                newTurnus.addElement(trip);
                turnuses.add(newTurnus);
            }
        }

        // Zoradiť každé turnusové elementy podľa času (istota)
        for (Turnus t : turnuses) {
            // t.getElements().sort(Comparator.comparingInt(TurnusElement::getStartTime));
            solution.addTurnus(t);
        }

        return solution;
    }

    private Solution selectParent() {
        List<Solution> tournament = new ArrayList<>();

        for (int i = 0; i < 3; i++) {
            Solution s = population.get(random.nextInt(population.size()));
            tournament.add(s);
        }

        return tournament.stream()
                .min(Comparator
                        // .comparingInt(Solution::getNumberOfTurnuses)
                        // .thenComparingDouble(Solution::getFitness))
                        .comparingDouble(Solution::getFitness))
                .orElse(null);
    }

    private Solution crossover(Solution parent1, Solution parent2) {
        Solution child = new Solution();
        Set<Integer> usedTripIds = new HashSet<>();

        // 1. Zober najlepšie turnusy z oboch rodičov (podľa deadhead distance)
        List<Turnus> allTurnuses = new ArrayList<>();
        allTurnuses.addAll(parent1.getTurnuses());
        allTurnuses.addAll(parent2.getTurnuses());

        allTurnuses.sort(Comparator.comparingDouble(Turnus::getDeadheadDistance));

        for (Turnus t : allTurnuses) {
            boolean valid = true;
            Turnus copy = new Turnus();

            for (TurnusElement e : t.getElements()) {
                if (e instanceof Trip trip) {
                    if (usedTripIds.contains(trip.getId())) {
                        valid = false;
                        break;
                    }
                    usedTripIds.add(trip.getId());
                    copy.addElement(new Trip(trip));
                } else {
                    copy.addElement(e);
                }
            }
            if (valid)
                child.addTurnus(copy);
        }

        // 2. Priraď chýbajúce tripy
        for (Trip trip : trips) {
            if (!usedTripIds.contains(trip.getId())) {
                boolean placed = false;
                for (Turnus t : child.getTurnuses()) {
                    Turnus test = new Turnus(t);
                    test.addElement(trip);
                    test.getElements().sort(Comparator.comparingInt(TurnusElement::getStartTime));
                    if (test.isFeasible()) {
                        t.addElement(trip);
                        t.getElements().sort(Comparator.comparingInt(TurnusElement::getStartTime));
                        placed = true;
                        break;
                    }
                }
                if (!placed) {
                    Turnus newTurnus = new Turnus();
                    newTurnus.addElement(trip);
                    child.addTurnus(newTurnus);
                }
            }
        }

        updateBestSolution(child);
        return child;
    }

    private void mutate(Solution solution) {
        if (random.nextDouble() > mutationRate)
            return;
        if (solution.getTurnuses().size() < 2)
            return;

        // Nájdeme najhorší turnus (napr. podľa deadhead vzdialenosti)
        Turnus worst = solution.getTurnuses().stream()
                .max(Comparator.comparingDouble(Turnus::getDeadheadDistance))
                .orElse(null);

        if (worst == null || worst.getElements().isEmpty()) return;

        List<Trip> worstTrips = new ArrayList<>();

        for (TurnusElement e : worst.getElements()) {
            if (e instanceof Trip trip) worstTrips.add(trip);
        }

        if (worstTrips.isEmpty()) return;

        // Vyber náhodný trip z najhoršieho turnusu
        Trip selected = worstTrips.get(random.nextInt(worstTrips.size()));

        // Odstráň trip z pôvodného turnusu
        worst.getElements().removeIf(e -> e instanceof Trip t && t.getId() == selected.getId());

        // Skús ho priradiť do iného existujúceho turnusu
        boolean inserted = false;
        for (Turnus target : solution.getTurnuses()) {
            if (target == worst)
                continue;

            Turnus test = new Turnus(target);
            test.addElement(selected);
            test.getElements().sort(Comparator.comparingInt(TurnusElement::getStartTime));

            if (test.isFeasible()) {
                target.addElement(selected);
                target.getElements().sort(Comparator.comparingInt(TurnusElement::getStartTime));
                inserted = true;
                break;
            }
        }

        // Ak sa nedal vložiť nikam, vytvor nový turnus
        if (!inserted) {
            Turnus newTurnus = new Turnus();
            newTurnus.addElement(selected);
            solution.addTurnus(newTurnus);
        }

        updateBestSolution(solution);
    }

    private void localImprove(Solution solution) {
        List<Turnus> turnuses = new ArrayList<>(solution.getTurnuses());
        turnuses.sort(Comparator.comparingDouble(Turnus::getDeadheadDistance).reversed());

        boolean improved = true;
        while (improved) {
            improved = false;
            outer: for (int i = 0; i < turnuses.size(); i++) {
                for (int j = i + 1; j < turnuses.size(); j++) {
                    Turnus t1 = turnuses.get(i);
                    Turnus t2 = turnuses.get(j);

                    Turnus combined = new Turnus();
                    combined.getElements().addAll(t1.getElements());
                    combined.getElements().addAll(t2.getElements());
                    combined.getElements().sort(Comparator.comparingInt(TurnusElement::getStartTime));

                    if (combined.isFeasible()) {
                        turnuses.set(i, combined);
                        turnuses.remove(j);
                        improved = true;
                        break outer;
                    }
                }
            }
        }

        solution.getTurnuses().clear();
        solution.getTurnuses().addAll(turnuses);
        updateBestSolution(solution);
    }

    public void ensureAllTripsPresent(Solution solution, List<Trip> allTrips) {
        Set<Integer> present = solution.getUsedTripIds();
        List<Trip> missing = allTrips.stream()
                .filter(t -> !present.contains(t.getId()))
                .collect(Collectors.toList());

        for (Trip trip : missing) {
            boolean placed = false;

            for (Turnus turnus : solution.getTurnuses()) {
                Turnus test = new Turnus(turnus);
                test.addElement(trip);
                test.getElements().sort(Comparator.comparingInt(TurnusElement::getStartTime));

                if (test.isFeasible()) {
                    turnus.addElement(trip);
                    turnus.getElements().sort(Comparator.comparingInt(TurnusElement::getStartTime));
                    placed = true;
                    break;
                }
            }

            if (!placed) {
                Turnus newTurnus = new Turnus();
                newTurnus.addElement(trip);
                solution.addTurnus(newTurnus);
            }
        }
    }

    public void updateBestSolution(Solution solution) {
        double fitness = solution.getFitness();
        int turnuses = solution.getNumberOfTurnuses();

        ensureAllTripsPresent(solution, trips);

        if (bestSolution == null ||
                turnuses < bestTurnuses ||
                (turnuses == bestTurnuses && fitness > bestSolution.getFitness())) {

            bestTurnuses = turnuses;
            bestSolution = new Solution(solution); // clone
        }
    }

    public Solution getBestSolution() {
        return bestSolution;
    }
}
