/*
 * Copyright 2016  Generic Problem Solver Project
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package gps.games.algorithm.recycling;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import gps.common.AbstractAlgorithm;
import gps.games.GamesModule;
import gps.games.algorithm.heuristic.DeltaHeuristic;
import gps.games.algorithm.singleplayer.AStar;
import gps.games.algorithm.singleplayer.BreadthFirst;
import gps.games.algorithm.singleplayer.DepthFirst;
import gps.games.algorithm.singleplayer.common.AbstractSingleplayerSearch;
import gps.games.algorithm.singleplayer.common.datastruct.ToEvalSortedList;
import gps.games.wrapper.Game;
import gps.games.wrapper.successor.INode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 *
 * An extension of {@link AbstractINodeRecycler}. Extracts sub paths from the elements of the {@link #population} and
 * uses {@link BreadthFirst} search and {@link RecyclingSearchProblem}s to optimize them.
 *
 * @author jschloet@tzi.de
 * @param <T> The type of the problem to be solved
 */
public class SearchRecycler<T> extends AbstractINodeRecycler<T> {

    /**
     * This attribute determines the length of the sub paths that are extracted from the elements
     * of the {@link #population}.
     *
     * Only sub paths of this length or shorter are optimized.
     */
    //TODO:: Determine the seperator. Maybe use the branching factor to do so.
    //Right now IterativeNMCS provides different values via the getOptions method.
    private int seperator = 240;

    /**
     * This value is used to determine how the element of the {@link #population} to be optimized is
     * selected. It is used to determine whether the next element is selected randomly or if the best
     * known element is optimized.
     */
    private int alternationSelection = 1;

    /**
     * Used to manage the random selection of the next element to be optimized as well as the selection of
     * the sub path to be optimized
     */
    private Random random = new Random();

    /**
     * To be able to interrupt the bfs when this thread is interrupted this
     * thread pool is used. The bfs is run in another thread and gets interrupted
     * when this thread gets interrupted.
     */
    // I know creating threads is expensive and a lot of bfs threads will be created
    // but right now I know no other way to do handle it. I read somewhere that thread
    // pools reuse threads so I hope the creation is not that expensive.
    private ExecutorService threadPool;

    /**
     * The search algorithm to use during the to optimize known solutions in the {@link #search(RecyclingElement)} method.
     * Can be past to {@link #selectElement(boolean)} to get an instance of the corresponding algorithms.
     */
    private SearchRecyclerAlgorithm algorithm = SearchRecyclerAlgorithm.DEPTH_FIRST_SEARCH;

    /**
     * If set, {@link #algorithm} is changed during the {@link #selectElement(boolean)} methods. That way
     * different algorithms are used in the {@link #search(RecyclingElement)} method.
     */
    private boolean all;

    /**
     * Constructor. Calls the super constructor and sets the {@link #seperator}
     * with the given value. The {@link #seperator} determines the length of the sub paths that are extracted from the elements
     * of the {@link #population}.
     *
     * Only sub paths of that length or shorter are optimized.
     *
     * @param pSeperator The new value of {@link #seperator}
     */
    public SearchRecycler(int pSeperator, SearchRecyclerAlgorithm pAlgorithm) {
        super();
        seperator = pSeperator;
        if (!pAlgorithm.equals(SearchRecyclerAlgorithm.ALL)) {
            algorithm = pAlgorithm;
        } else {
            all = true;
        }
    }

    /**
     * Selects an element to be optimized using {@link #selectElement(boolean)}. Uses the
     * {@link #search(RecyclingElement)} method to optimize it and adds it to the {@link #population}.
     */
    @Override
    protected void optimize() {
        if (!population.isEmpty()) {
            alternationSelection = (alternationSelection + 1) % 2;
            RecyclingElement<T> element = selectElement(
                    alternationSelection == 1);
            search(element);
            population.add(element);
        }
    }

    /**
     * Return the value of {@link SearchRecyclerAlgorithm} that is used in this {@link SearchRecycler}.
     * If {@link #all} is set, the value is {@link SearchRecyclerAlgorithm#ALL}. Otherwise the value
     * of {@link #algorithm} can be returned.
     *
     * This method is necessary, because using algorithm might need this value to include it in
     * the {@link AbstractAlgorithm#getName()} method.
     *
     * @return The search algorithm that is used in this {@link SearchRecycler}
     */
    public SearchRecyclerAlgorithm getSearchRecyclerAlgorithm() {
        if (all) {
            return SearchRecyclerAlgorithm.ALL;
        }
        return algorithm;
    }

    /**
     * Randomly extracts a sub path from the given element and optimizes it
     * by formulating a {@link RecyclingSearchProblem} using the first element
     * of the sub path as starting point and the last element as the goal state and
     * applying {@link BreadthFirst} search. Replaces the original sub path with the
     * optimized one.
     *
     * @param element The {@link RecyclingSearchProblem} to be optimized
     */
    private void search(RecyclingElement<T> element) {
        // I instantiate it here, because kryo seem to have problems to copy threads/threadpool
        if (threadPool == null || threadPool.isShutdown()) {
            threadPool = Executors.newFixedThreadPool(1,
                    new ThreadFactoryBuilder().setDaemon(true).build());
        }
        int limit = (seperator < element.elem.size()) ? seperator
                : element.elem.size() - 2;
        int randomNumber = random.nextInt(element.elem.size() - limit - 1);
        INode<T> start = element.elem.get(randomNumber);
        INode<T> goal = element.elem.get(randomNumber + limit);
        RecyclingSearchProblem<T> searchProblem = new RecyclingSearchProblem<>(
                start, goal);
        GamesModule<RecyclingSearchProblem<T>> module = new GamesModule<>(
                new Game<>(searchProblem));
        module.setDepthlimit(limit);
        //Start the bfs in another thread to be able to cancel it on interrupt
        Future<List<INode<T>>> future = threadPool
                .submit(() -> getSearchAlgorithm(algorithm, module,
                        goal.getGame().copy()).stateSequence()
                                .orElse(new ArrayList<>()).stream()
                                .map(RecyclingSearchProblem::getCurrent)
                                .collect(Collectors.toList()));
        //Wait for the bfs to be finished
        List<INode<T>> subsequence;
        try {
            subsequence = future.get();
        } catch (InterruptedException e) {
            // On interrupt use the empty sub sequence
            subsequence = new ArrayList<>();
            threadPool.shutdownNow();
            // Set the interrupt flag again to stop the algorithm
            Thread.currentThread().interrupt();
        } catch (ExecutionException e) {
            // On Exceptions in the bfs use the empty list as well
            subsequence = new ArrayList<>();
            e.printStackTrace();
        }
        for (int i = randomNumber; !subsequence.isEmpty()
                && i <= randomNumber + limit; i++) {
            element.elem.remove(randomNumber);
        }
        while (!element.elem.isEmpty() && !subsequence.isEmpty()
                && !goal.getGame()
                        .equals(subsequence.get(subsequence.size() - 1)
                                .getGame())
                && randomNumber < element.elem.size()) {
            element.elem.remove(randomNumber);
        }
        element.elem.addAll(randomNumber, subsequence);
    }

    /**
     * Selects an element to be optimized in {@link #search(RecyclingElement)}.
     * If the given value is {@code true} the element is selected randomly.
     * Otherwise the element is selected according to its {@link gps.games.algorithm.nestedMonteCarloSearch.NMCS#utilityAbstraction(int, INode)}
     * value (the element with the highest value is selected).
     *
     * @param selectRandom Determines the selection strategy to be used
     * @return The selected {@link RecyclingElement}
     */
    private RecyclingElement<T> selectElement(boolean selectRandom) {
        RecyclingElement<T> element;
        if (selectRandom) {
            int randomNumber = random.nextInt(population.size());
            element = population.get(randomNumber);
            population.remove(randomNumber);
        } else {
            element = population.get(0);
            population.remove(element);
        }
        return element;
    }

    /**
     * Selects an {@link AbstractSingleplayerSearch} algorithm to be used in the {@link #search(RecyclingElement)} method. Return the
     * algorithm that corresponds to the given {@link SearchRecyclerAlgorithm} value. Uses the given {@link GamesModule} to instantiate
     * the algorithm. If {@link #all} is set, the given {@link SearchRecyclerAlgorithm} is not used, but the next algorithm according to
     * the order of {@link SearchRecyclerAlgorithm#values()}.
     *
     * If {@link SearchRecyclerAlgorithm#ASTAR} is given, the terminal is used to create a {@link DeltaHeuristic}.
     *
     * @param pAlgorithm The Algorithm to be selected
     * @param gamesModule The Module for the search problem to be solved
     * @param terminal A terminal state used to create a {@link DeltaHeuristic} to be used in {@link AStar}. If the given {@link SearchRecyclerAlgorithm} is
     *                 {@link SearchRecyclerAlgorithm#ASTAR} this value must not be {@code null}
     * @return The selected {@link AbstractSingleplayerSearch}
     */
    private AbstractSingleplayerSearch<RecyclingSearchProblem<T>> getSearchAlgorithm(
            SearchRecyclerAlgorithm pAlgorithm,
            GamesModule<RecyclingSearchProblem<T>> gamesModule,
            Game<T> terminal) {
        AbstractSingleplayerSearch<RecyclingSearchProblem<T>> toReturn = null;
        if (all) {
            //If all is set take the algorithm with the next ordinal (mod values.length). Exclude the All value.
            int ordinal = (algorithm.ordinal() + 1)
                    % (SearchRecyclerAlgorithm.values().length - 1);
            pAlgorithm = SearchRecyclerAlgorithm.values()[ordinal];
            algorithm = pAlgorithm;
        }
        switch (pAlgorithm) {
        case ASTAR:
            try {
                toReturn = new AStar<>(gamesModule,
                        DeltaHeuristic.createUsingGivenTerminal(terminal),
                        ToEvalSortedList.class);
            } catch (Exception e) {
                // If that somehow fails use bfs:
                toReturn = new BreadthFirst<>(gamesModule);
            }
            break;
        case DEPTH_FIRST_SEARCH:
            toReturn = new DepthFirst<>(gamesModule);
            break;
        case BREADTH_FIRST_SEARCH:
            toReturn = new BreadthFirst<>(gamesModule);
            break;
        default:
            break;
        }
        return toReturn;
    }

    /**
     * Shuts down the {@link #threadPool} before the {@link AbstractRecycler} terminates.
     */
    @Override
    protected void clean() {
        if (threadPool != null) {
            threadPool.shutdown();
        }
    }

    /**
     * Returns a list of variants of  {@link SearchRecycler}s.
     *
     * @return A list of {@link SearchRecycler} variants
     */
    public static List<?> getSearchRecyclerVariants() {
        return Arrays.asList(
                new SearchRecycler<Object>(30,
                        SearchRecyclerAlgorithm.BREADTH_FIRST_SEARCH),
                new SearchRecycler<Object>(30,
                        SearchRecyclerAlgorithm.DEPTH_FIRST_SEARCH),
                new SearchRecycler<Object>(30, SearchRecyclerAlgorithm.ASTAR),
                new SearchRecycler<Object>(30, SearchRecyclerAlgorithm.ALL),
                new SearchRecycler<Object>(240,
                        SearchRecyclerAlgorithm.BREADTH_FIRST_SEARCH),
                new SearchRecycler<Object>(240,
                        SearchRecyclerAlgorithm.DEPTH_FIRST_SEARCH),
                new SearchRecycler<Object>(240, SearchRecyclerAlgorithm.ASTAR),
                new SearchRecycler<Object>(240, SearchRecyclerAlgorithm.ALL),
                null);
    }

}