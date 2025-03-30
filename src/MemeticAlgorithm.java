import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class MemeticAlgorithm {
    private int populationSize;
    private int generations;
    private double mutationRate;

    private List<Solution> population;
    private Random random = new Random();

    private List<Trip> trips;

    public MemeticAlgorithm(int popSize, int generations, double mutationRate, List<Trip> trips) {
        this.populationSize = popSize;
        this.generations = generations;
        this.mutationRate = mutationRate;
        this.population = new ArrayList<>();
        this.trips = trips;
    }

    public void run() {
        initializePopulation();

        for (int gen = 0; gen < generations; gen++) {
            List<Solution> newPopulation = new ArrayList<>();

            for (int i = 0; i < populationSize; i++) {
                Solution parent1 = selectParent();
                Solution parent2 = selectParent();
                Solution child = crossover(parent1, parent2);

                mutate(child);
                localImprove(child);
                newPopulation.add(child);
            }

            population = newPopulation;
            System.out.println("Generation " + gen + ": Turnuses = " + getBestSolution().getNumberOfTurnuses());
        }

        System.out.println("Final best solution: " + getBestSolution());
    }

    private void initializePopulation() {
        for (int i = 0; i < populationSize; i++) {
            // Solution solution = generateRandomSolution();
            Solution solution = generateGreedySolution();
            population.add(solution);
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
                    TurnusElement last = elements.get(elements.size() - 1);
    
                    // ak časovo stíha a deadhead nie je príliš ďaleko (tu to ešte nezohľadňujeme úplne presne)
                    if (last.getEndTime() + 5 <= trip.getStartTime()) {
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
            t.getElements().sort(Comparator.comparingInt(TurnusElement::getStartTime));
            solution.addTurnus(t);
        }
    
        return solution;
    }
    

    private Solution selectParent() {
        // Tournament selection (zatiaľ náhodne)
        return population.get(random.nextInt(population.size()));
    }

    private Solution crossover(Solution parent1, Solution parent2) {
        Solution child = new Solution();
        Set<Integer> usedTripIds = new HashSet<>();
    
        Random rand = new Random();
    
        // Copy half of the turnuses from parent1
        List<Turnus> p1Turnuses = new ArrayList<>(parent1.getTurnuses());
        Collections.shuffle(p1Turnuses);
    
        int half = p1Turnuses.size() / 2;
        for (int i = 0; i < half; i++) {
            Turnus t = new Turnus(p1Turnuses.get(i));
            child.addTurnus(t);
            for (TurnusElement e : t.getElements()) {
                if (e instanceof Trip trip) {
                    usedTripIds.add(trip.getId());
                }
            }
        }
    
        // Fill remaining trips from parent2
        List<Trip> remainingTrips = new ArrayList<>();
        for (Turnus t : parent2.getTurnuses()) {
            for (TurnusElement e : t.getElements()) {
                if (e instanceof Trip trip && !usedTripIds.contains(trip.getId())) {
                    remainingTrips.add(new Trip(trip));
                    usedTripIds.add(trip.getId());
                }
            }
        }
    
        // Greedy assignment of remaining trips
        for (Trip trip : remainingTrips) {
            boolean added = false;
            for (Turnus t : child.getTurnuses()) {
                Turnus test = new Turnus(t);
                test.addElement(trip);
                test.getElements().sort(Comparator.comparingInt(TurnusElement::getStartTime));
                if (test.isFeasible()) {
                    t.addElement(trip);
                    t.getElements().sort(Comparator.comparingInt(TurnusElement::getStartTime));
                    added = true;
                    break;
                }
            }
            if (!added) {
                Turnus newTurnus = new Turnus();
                newTurnus.addElement(trip);
                child.addTurnus(newTurnus);
            }
        }
    
        return child;
    }
    


    private void mutate(Solution solution) {
        if (random.nextDouble() > mutationRate) return;

        Random rand = new Random();
        if (solution.getTurnuses().size() < 2) return;
    
        Set<Integer> usedTripIds = solution.getUsedTripIds();
    
        // Vyber náhodný turnus (zdroj)
        Turnus fromTurnus = solution.getTurnuses().get(rand.nextInt(solution.getTurnuses().size()));
        List<TurnusElement> fromElements = fromTurnus.getElements();
    
        List<Trip> fromTrips = new ArrayList<>();
        for (TurnusElement e : fromElements) {
            if (e instanceof Trip trip) {
                fromTrips.add(trip);
            }
        }
    
        if (fromTrips.isEmpty()) return;
    
        Trip selectedTrip = fromTrips.get(rand.nextInt(fromTrips.size()));
        fromElements.removeIf(e -> (e instanceof Trip t) && t.getId() == selectedTrip.getId());
        usedTripIds.remove(selectedTrip.getId());
    
        // Vyber náhodný cieľový turnus
        Turnus toTurnus = solution.getTurnuses().get(rand.nextInt(solution.getTurnuses().size()));
        if (toTurnus == fromTurnus) return;
    
        if (!usedTripIds.contains(selectedTrip.getId())) {
            toTurnus.addElement(selectedTrip);
            toTurnus.getElements().sort(Comparator.comparingInt(TurnusElement::getStartTime));
    
            if (!fromTurnus.isFeasible() || !toTurnus.isFeasible()) {
                toTurnus.getElements().removeIf(e -> (e instanceof Trip t) && t.getId() == selectedTrip.getId());
                fromTurnus.addElement(selectedTrip);
                fromTurnus.getElements().sort(Comparator.comparingInt(TurnusElement::getStartTime));
                usedTripIds.add(selectedTrip.getId());
            } else {
                usedTripIds.add(selectedTrip.getId());
            }
        } else {
            fromTurnus.addElement(selectedTrip);
            fromTurnus.getElements().sort(Comparator.comparingInt(TurnusElement::getStartTime));
            usedTripIds.add(selectedTrip.getId());
        }
    }
    
    private void localImprove(Solution solution) {
        List<Turnus> original = new ArrayList<>(solution.getTurnuses());
        List<Turnus> improved = new ArrayList<>();
        Set<Integer> usedTripIds = new HashSet<>();
    
        for (Turnus t : original) {
            List<Trip> tripsToMove = new ArrayList<>();
            for (TurnusElement e : t.getElements()) {
                if (e instanceof Trip trip) {
                    tripsToMove.add(trip);
                }
            }
    
            boolean allMoved = true;
    
            for (Trip trip : tripsToMove) {
                boolean moved = false;
    
                for (Turnus target : improved) {
                    if (!usedTripIds.contains(trip.getId())) {
                        Turnus test = new Turnus(target);
                        test.addElement(trip);
                        test.getElements().sort(Comparator.comparingInt(TurnusElement::getStartTime));
    
                        if (test.isFeasible()) {
                            target.addElement(trip);
                            target.getElements().sort(Comparator.comparingInt(TurnusElement::getStartTime));
                            usedTripIds.add(trip.getId());
                            moved = true;
                            break;
                        }
                    }
                }
    
                if (!moved) {
                    Turnus newTurnus = new Turnus();
                    newTurnus.addElement(trip);
                    improved.add(newTurnus);
                    usedTripIds.add(trip.getId());
                    allMoved = false;
                }
            }
        }
    
        solution.getTurnuses().clear();
        solution.getTurnuses().addAll(improved);
    }
    


    public Solution getBestSolution() {
        return population.stream()
                .min(Comparator.comparingDouble(Solution::getFitness))
                .orElse(null);
    }
}
