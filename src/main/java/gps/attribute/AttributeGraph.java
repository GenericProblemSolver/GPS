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
package gps.attribute;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import gps.util.Tuple;

/**
 * This class represents a IGraph structure of all Fields in a given Object.
 *
 * @author wahler@tzi.de
 */
public class AttributeGraph {
    /**
     * a list of all references of already used <code>Object</code>s
     */
    final List<Object> references;

    /**
     * a list of all nodes that were created while this IGraph was made; it does
     * not contain duplicates
     */
    final List<AttributeValue> createdNodes;

    /**
     * a list of all primitive nodes that were created while this IGraph was
     * made; it does not contain duplicates
     */
    private final List<AttributeValue> primitiveNodes;

    /**
     * the root of this graph
     */
    private final AttributeValue root;

    /**
     * This set contains all classes of primitive types. It is used to check
     * whether or not an <code>Object</code> is of primitive type.
     */
    static final Set<Class<?>> PRIMITIVES = primitiveTypes();

    /**
     * the Logger of this class
     */
    private static Logger LOGGER = Logger.getAnonymousLogger();

    /**
     * Fills the {@link AttributeGraph#PRIMITIVES} set with its values.
     *
     * Source: http://stackoverflow.com/questions/709961/
     * determining-if-an-object-is-of-primitive-type
     */
    private static Set<Class<?>> primitiveTypes() {
        Set<Class<?>> ret = new HashSet<Class<?>>();
        ret.add(Boolean.class);
        ret.add(Character.class);
        ret.add(Byte.class);
        ret.add(Short.class);
        ret.add(Integer.class);
        ret.add(Long.class);
        ret.add(Float.class);
        ret.add(Double.class);
        ret.add(Void.class);
        return ret;
    }

    /**
     * Creates a new <code>AttributeGraph</code> which will have as root the
     * given <code>Object</code>. It needs to be build afterwards with
     * {@link AttributeGraph#buildGraph()}.
     * 
     * @param o
     *            the <code>Object</code> from which the graph be created
     */
    public AttributeGraph(final Object o) {
        if (o == null) {
            throw new IllegalArgumentException(
                    "The given Object cannot be null.");
        }
        references = new ArrayList<>();
        createdNodes = new ArrayList<>();
        primitiveNodes = new ArrayList<>();
        root = new AttributeValue(o, o.getClass().getSimpleName(), null, -1);
        LOGGER.setLevel(Level.WARNING);
    }

    /**
     * Creates a new <code>AttributeGraph</code>. Is private, because the static
     * method {@link AttributeGraph#fromObject(Object)} should be used.
     *
     * @param av
     *            the root of this graph
     */
    private AttributeGraph(final AttributeValue av) {
        if (av == null) {
            throw new IllegalArgumentException(
                    "The given AttributeValue cannot be null.");
        }
        references = new ArrayList<>();
        createdNodes = new ArrayList<>();
        primitiveNodes = new ArrayList<>();
        root = av;
        LOGGER.setLevel(Level.WARNING);
    }

    /**
     * Builds this <code>AttributeGraph</code> if it was not before
     */
    public void buildGraph() {
        if (root.getFields().size() > 0) {
            LOGGER.info("IGraph was already build.");
            return;
        }
        LOGGER.info("Starting to build the graph.");
        long t = System.nanoTime();
        build(root);
        LOGGER.warning(
                "Building the graph took " + (System.nanoTime() - t) + "ns.");
    }

    /**
     * Returns a new <code>AttributeGraph</code> for the given
     * <code>Object</code>.
     *
     * @param o
     *            the Object of which the graph should be created
     *
     * @return a graph for that object
     */
    public static AttributeGraph fromObject(final Object o) {
        if (o == null) {
            throw new IllegalArgumentException();
        }

        AttributeValue root = new AttributeValue(o,
                o.getClass().getSimpleName(), null, -1);
        AttributeGraph ret = new AttributeGraph(root);

        LOGGER.info("Starting to build the graph.");
        long t = System.nanoTime();
        ret.build(root);
        LOGGER.warning(
                "Building the graph took " + (System.nanoTime() - t) + "ns.");
        return ret;
    }

    /**
     * For a given {@link AttributeValue} this method creates the graph
     * structure using recursion. No fields are ignored. It also fills a list
     * with all nodes and a second one for all primitives.
     *
     * Enums, Collections, Primitives and Arrays will be handled differently
     * than all other types.
     *
     * @param av
     *            the starting point
     */
    private void build(final AttributeValue av) {
        if (Thread.interrupted()) {
            return;
        }

        if (createdNodes.contains(av)) {
            return;
        }

        createdNodes.add(av);

        if (av.isPrimitive()) {
            primitiveNodes.add(av);
            return;
        }

        Object v = av.getValue();

        boolean contains = false;
        if (referenceContains(references, v)) {
            contains = true;
        }
        references.add(v);

        List<Field> children = new ArrayList<>();
        children.clear();

        // get Superclass Fields
        if (!PRIMITIVES.contains(v.getClass())) {
            while (v.getClass().getSuperclass() != null
                    && v.getClass() != java.lang.Class.class) {
                children.addAll(
                        Arrays.asList(v.getClass().getDeclaredFields()));
                v = v.getClass().getSuperclass();
            }
        }

        // Handling of Arrays
        // Arrays do not have fields so their values have to be gotten
        // in another way
        if (av.getValue().getClass().isArray()) {
            LOGGER.info("Unpacking Array of length "
                    + Array.getLength(av.getValue()));
            for (int i = 0; i < Array.getLength(av.getValue()); i++) {
                // The value of a field can be null
                // AttributeValues with null cannot be created
                if (Array.get(av.getValue(), i) == null) {
                    continue;
                }
                AttributeValue child = new AttributeValue(
                        Array.get(av.getValue(), i), "[" + i + "]",
                        av.getIdentifier(), i);
                AttributeValue rep = getDuplicate(child);
                if (rep != null) {
                    child = rep;
                }
                references.add(Array.get(av.getValue(), i));

                if (createdNodes.contains(child)) {
                    child = createdNodes.get(createdNodes.indexOf(child));
                }
                av.getFields().add(child);
                child.getFields().add(av);

                if (PRIMITIVES.contains(child.getValue().getClass())) {
                    LOGGER.info("Processing primitive type: "
                            + child.getValue().getClass());
                    primitiveNodes.add(child);
                    createdNodes.add(child);
                    av.getPrimitives().add(child);
                } else {
                    if (!contains) {
                        build(child);
                    } else {
                        createdNodes.add(child);
                    }
                }
            }
            return;
        }
        // Set all fields accessible
        for (int i = 0; i < children.size(); i++) {
            children.get(i).setAccessible(true);
        }

        LOGGER.info("Amount of Fields for " + av.getValue().getClass() + ": "
                + children.size());

        // if (contains) {
        // return;
        // }
        for (Field f : children) {
            Object value = new Object();

            // get the value of the field
            try {
                value = f.get(av.getValue());
            } catch (IllegalArgumentException | IllegalAccessException e) {
                e.printStackTrace();
                return;
            }

            // The value of a field can be null
            // AttributeValues with null cannot be created
            if (value == null) {
                continue;
            }

            if (!PRIMITIVES.contains(value.getClass())) {
                if (value instanceof Collection<?>) {
                    LOGGER.info(
                            "Processing collection type: " + value.getClass());
                    AttributeValue list = new AttributeValue(value, f.getName(),
                            av.getIdentifier(), -1);
                    AttributeValue rep = getDuplicate(list);
                    if (rep != null) {
                        list = rep;
                    }
                    AttributeValue size = new AttributeValue(
                            ((Collection<?>) value).size(), "size",
                            list.getIdentifier(), -1);

                    createdNodes.add(list);
                    createdNodes.add(size);
                    primitiveNodes.add(size);

                    av.getFields().add(list);
                    list.getFields().add(av);
                    list.getFields().add(size);
                    size.getFields().add(list);
                    list.getPrimitives().add(size);

                    int index = 0;
                    for (Object o : (Collection<?>) value) {
                        if (referenceContains(references, o)) {
                            // continue;
                        }
                        references.add(o);
                        AttributeValue child = new AttributeValue(o,
                                "(" + index + ")", list.getIdentifier(), index);
                        AttributeValue repChild = getDuplicate(child);
                        if (repChild != null) {
                            child = repChild;
                        }
                        list.getFields().add(child);
                        child.getFields().add(list);
                        if (child.isPrimitive()) {
                            createdNodes.add(child);
                            primitiveNodes.add(child);
                            list.getPrimitives().add(child);
                        } else {
                            if (!contains) {
                                build(child);
                            } else {
                                createdNodes.add(child);
                            }
                        }
                        index++;
                    }

                } else if (value.getClass().isEnum()) {
                    LOGGER.info("Processing Enum: " + value.getClass());

                    AttributeValue child = new AttributeValue(value,
                            f.getName(), av.getIdentifier(), -1);
                    AttributeValue repChild = getDuplicate(child);
                    if (repChild != null) {
                        child = repChild;
                    }

                    if (createdNodes.contains(child)) {
                        child = createdNodes.get(createdNodes.indexOf(child));
                    }
                    av.getFields().add(child);
                    child.getFields().add(av);

                    createdNodes.add(child);

                } else {
                    LOGGER.info("Processing \"standard\" type: "
                            + value.getClass());

                    AttributeValue child = new AttributeValue(value,
                            f.getName(), av.getIdentifier(), -1);
                    AttributeValue repChild = getDuplicate(child);
                    if (repChild != null) {
                        child = repChild;
                    }
                    av.getFields().add(child);
                    child.getFields().add(av);
                    if (!contains) {
                        build(child);
                    } else {
                        createdNodes.add(child);
                    }
                }
            } else {
                LOGGER.info("Processing primitive type: " + value.getClass()
                        + " (" + f.getName() + ")");

                AttributeValue child = new AttributeValue(value, f.getName(),
                        av.getIdentifier(), -1);
                createdNodes.add(child);
                primitiveNodes.add(child);
                av.getFields().add(child);
                child.getFields().add(av);
                av.getPrimitives().add(child);
            }
        }
    }

    /**
     * Checks if a given <code>Object</code> is already inside the
     * {@link AttributeGraph#references} list. It uses <code>==</code> to check
     * for reference equality.
     *
     * @param o
     *            the <code>Object</code> to check
     *
     * @return <code>true</code>, if the reference of the given
     *         <code>Object</code> is in the list, <code>false</code> otherwise
     */
    private boolean referenceContains(final List<Object> refs, final Object o) {
        if (PRIMITIVES.contains(o.getClass())) {
            return false;
        }
        boolean ret = false;
        for (Object check : refs) {
            ret |= o == check;
        }
        return ret;
    }

    /**
     * Gets the {@link AttributeValue} that is equal (both need the same
     * {@link AttributeValue#identifier}) to the given one out of the
     * {@link AttributeGraph#createdNodes} list. Returns <code>null</code> if
     * the given <code>AttributeValue</code> is not in the list.
     *
     * @param av
     *            the <code>AttributeValue</code> which duplicate is wanted
     *
     * @return <code>null</code> if there is no <code>AttributeValue</code> like
     *         the given one in the list, the already created one otherwise
     */
    private AttributeValue getDuplicate(final AttributeValue av) {
        if (referenceContains(references, av.getValue())) {
            for (AttributeValue check : createdNodes) {
                if (check.getValue() == av.getValue()) {
                    return check;
                }
            }

        }
        return null;
    }

    /**
     * Removes a given {@link AttributeValue} from this
     * <code>AttributeGraph</code>. Also removes all nodes that are not
     * connected to the root
     *
     * To do so a list with all reachable from the {@link AttributeGraph#root}
     * are collected. All unreachable nodes are removed.
     *
     * @param a
     *            the <code>AttributeValue</code> to remove
     *
     * @return <code>true</code> when removal was successful, <code>false</code>
     *         otherwise
     */
    boolean remove(final IObject a) {
        if (!createdNodes.contains(a)) {
            LOGGER.info("Given node is not part of this graph.");
            return false;
        }
        long t = System.nanoTime();

        // remove the specified node and its occurrence
        // in all its fields
        for (IObject rem : a.getFields()) {
            rem.getFields().remove(a);
        }
        createdNodes.remove(a);
        if (a.isPrimitive()) {
            LOGGER.info("One node was removed.");
            return true;
        }

        List<AttributeValue> reachable = new ArrayList<>();
        reachable = reachableNodes(root, reachable);
        int removed = createdNodes.size() + 1 - reachable.size();
        createdNodes.clear();
        createdNodes.addAll(reachable);
        if (removed == 1) {
            LOGGER.info("One node was removed.");
        } else {
            LOGGER.info(removed + " nodes were removed.");
        }
        LOGGER.info("Removal took " + (System.nanoTime() - t) + "ns.");
        return true;
    }

    /**
     * Removes a list of {@link AttributeValue}s out of this
     * <code>AttributeGraph</code>. Also removes all nodes that are not
     * reachable after the first removal.
     * 
     * @param toRemove
     *            the list of <code>AttributeValue</code>s to remove
     * 
     * @return <code>true</code>, if at least one <code>AttributeValue</code>
     *         was removed, <code>false</code> otherwise
     */
    boolean removeList(final List<IObject> toRemove) {
        int removable = 0;
        for (IObject a : toRemove) {
            if (createdNodes.contains(a)) {
                removable++;
            }
        }
        if (removable == 0) {
            LOGGER.info("No nodes were removed");
            return false;
        }
        createdNodes.removeAll(toRemove);
        for (AttributeValue ca : createdNodes) {
            ca.getFields().removeAll(toRemove);
            ca.getPrimitives().removeAll(toRemove);
        }
        List<AttributeValue> reachable = reachableNodes(root,
                new ArrayList<AttributeValue>());
        removable = createdNodes.size() - reachable.size();
        createdNodes.clear();
        createdNodes.addAll(reachable);
        LOGGER.info(removable + " nodes were removed.");
        return true;
    }

    /**
     * Creates a list of nodes which can be reached from the given one.
     *
     * @param root
     *            the root from which to check
     *
     * @param reachable
     *            a list of nodes checked
     *
     * @return a list of all reachable nodes
     */
    private List<AttributeValue> reachableNodes(final AttributeValue root,
            final List<AttributeValue> reachable) {
        reachable.add(root);
        for (IObject a : root.getFields()) {
            if (reachable.contains(a)) {
                continue;
            }
            reachableNodes((AttributeValue) a, reachable);
        }
        return reachable;
    }

    /**
     * Traverses this <code>AttributeGraph</code> with depth first search.
     *
     * @param onlyPrimitives
     *            if <code>true</code> only primitive nodes will be returned
     *
     * @return a list of {@link AttributeValue}s
     */
    public List<AttributeValue> depthFirstTraversal(
            final boolean onlyPrimitives) {
        List<AttributeValue> nodes = new ArrayList<>();
        Set<AttributeValue> checked = new HashSet<>();
        checked.add(root);
        if (onlyPrimitives && root.isPrimitive()) {
            nodes.add(root);
            return nodes;
        } else if (!onlyPrimitives) {
            nodes.add(root);
        }
        for (IObject io : root.getFields()) {
            nodes.addAll(depthFirstTraversalHelper(checked, (AttributeValue) io,
                    onlyPrimitives));
        }
        return nodes;
    }

    /**
     * Helps traversing the <code>AttributeGraph</code>. Uses recursion to get
     * all nodes.
     *
     * @param checked
     *            a set of already checked {@link AttributeValue}s
     *
     * @param now
     *            the <code>AttributeValue</code> that is used at the moment
     *
     * @param onlyPrimitives
     *            if <code>true</code> only primitive nodes will be returned
     *
     * @return a list of traversed <code>AttributeValue</code>s
     */
    private List<AttributeValue> depthFirstTraversalHelper(
            final Set<AttributeValue> checked, final AttributeValue now,
            final boolean onlyPrimitives) {
        List<AttributeValue> nodes = new ArrayList<>();
        if (!checked.contains(now)) {
            checked.add(now);
            if (onlyPrimitives) {
                if (now.isPrimitive()) {
                    nodes.add(now);
                    return nodes;
                }
            } else {
                nodes.add(now);
            }
        } else {
            return nodes;
        }
        for (IObject io : now.getFields()) {
            if (nodes.contains(io)) {
                continue;
            }
            nodes.addAll(depthFirstTraversalHelper(checked, (AttributeValue) io,
                    onlyPrimitives));
        }
        return nodes;
    }

    /**
     * Filters a given list of {@link AttributeValue}s so that it only contains
     * nodes whose value's <code>class</code> is the same or a subclass of
     * <code>filterType</code>.
     * 
     * @param toFilter
     *            the list to filter
     * 
     * @param filterType
     *            the class of which all nodes value has to be type of
     * 
     * @return a list which only contains nodes matching the given criterion
     */
    public static List<AttributeValue> filterList(
            final List<AttributeValue> toFilter, final Class<?> filterType) {
        if (toFilter == null || filterType == null) {
            throw new IllegalArgumentException();
        }
        if (toFilter.isEmpty()) {
            return toFilter;
        }
        List<AttributeValue> filtered = new ArrayList<>();
        for (AttributeValue av : toFilter) {
            if (filterType.isAssignableFrom(av.getValue().getClass())) {
                filtered.add(av);
            }
        }
        return filtered;
    }

    /**
     * Matches two {@link AttributeGraph}s against each other. If two
     * {@link AttributeValue}s are equal (i.e. they have the same
     * {@link AttributeValue#identifier}) the {@link IAttributeMatcher} is
     * applied on them.
     * 
     * If one of the Graphs was not build, this method will also build them.
     * 
     * @param ag
     *            the second <code>AttributeGraph</code>
     * 
     * @param func
     *            the function to use on two nodes
     */
    public void matchAll(final AttributeGraph ag, IAttributeMatcher func) {
        if (ag.getRoot().getFields().isEmpty()) {
            ag.buildGraph();
        }
        if (root.getFields().isEmpty()) {
            buildGraph();
        }
        for (AttributeValue one : createdNodes) {
            for (AttributeValue two : ag.createdNodes) {
                if (one.equals(two)) {
                    func.onMatch(one, two);
                    // no AttributeValue can have two equal nodes
                    break;
                }
            }
        }
    }

    /**
     * Matches two <code>AttributeGraph</code>s against each other. The given
     * IGraph should be empty and will be build as needed. Only builds the graph
     * when the given {@link IAttributeMatcher} returns <code>true</code> for
     * two equal {@link AttributeValue}s.
     * 
     * @param ag
     *            the (empty) <code>AttributeGraph</code> to match with this one
     * 
     * @param func
     *            the matching function which gets applied on two equal
     *            <code>AttributeValue</code>s
     */
    public AttributeGraph matchAllUncached(final AttributeGraph ag,
            IAttributeMatcher func) {
        long t = System.nanoTime();
        LOGGER.info("Start matching Graphs.");
        if (root.equals(ag.root)) {
            if (func.onMatch(root, ag.root)) {
                ag.buildStep(ag.root);
                buildStep(root);
                List<Object> checkedRef = new ArrayList<>();
                checkedRef.add(root.getValue());
                matchUncachedHelper(root, ag.root, ag, checkedRef, func);
            }
        }
        LOGGER.info("Matching took " + (System.nanoTime() - t) + "ns.");
        return ag;
    }

    /**
     * Helps matching two empty graphs.
     * 
     * @param fst
     *            the current {@link AttributeValue} out of this
     *            <code>AttributeGraph</code>
     * 
     * @param snd
     *            the current <code>AttributeValue</code> out of the given
     *            <code>AttributeGraph</code>
     * 
     * @param sndG
     *            the given <code>AttributeGraph</code>
     * 
     * @param checkedRef
     *            a <code>List</code> of all checked <code>Object</code>s
     * 
     * @param func
     *            the function which gets applied on two equal
     *            <code>AttributeValue</code>s
     */
    private void matchUncachedHelper(final IObject fst, final IObject snd,
            final AttributeGraph sndG, final List<Object> checkedRef,
            final IAttributeMatcher func) {
        for (IObject af : fst.getFields()) {
            if (referenceContains(checkedRef, af.getValue())) {
                continue;
            }
            checkedRef.add(af.getValue());
            for (IObject as : snd.getFields()) {
                if (af.equals(as)) {
                    func.onMatch(af, as);
                }
            }
        }
    }

    /**
     * Builds this <code>AttributeGraph</code> one step further, so all fields
     * for a given {@link AttributeValue} are created.
     * 
     * @param av
     *            <code>AttributeValue</code> to build
     */
    private void buildStep(final AttributeValue av) {
        if (Thread.interrupted()) {
            return;
        }

        if (createdNodes.contains(av)) {
            return;
        }

        createdNodes.add(av);

        if (av.isPrimitive()) {
            primitiveNodes.add(av);
            return;
        }

        Object v = av.getValue();
        references.add(v);

        List<Field> children = new ArrayList<>();
        children.clear();

        // get Superclass Fields
        if (!PRIMITIVES.contains(v.getClass())) {
            while (v.getClass().getSuperclass() != null
                    && v.getClass() != java.lang.Class.class) {
                children.addAll(
                        Arrays.asList(v.getClass().getDeclaredFields()));
                v = v.getClass().getSuperclass();
            }
        }

        // Handling of Arrays
        // Arrays do not have fields so their values have to be gotten
        // in another way
        if (av.getValue().getClass().isArray()) {
            LOGGER.info("Unpacking Array of length "
                    + Array.getLength(av.getValue()));
            for (int i = 0; i < Array.getLength(av.getValue()); i++) {
                // The value of a field can be null
                // AttributeValues with null cannot be created
                if (Array.get(av.getValue(), i) == null) {
                    continue;
                }
                AttributeValue child = new AttributeValue(
                        Array.get(av.getValue(), i), "[" + i + "]",
                        av.getIdentifier(), i);
                AttributeValue rep = getDuplicate(child);
                if (rep != null) {
                    child = rep;
                }
                references.add(Array.get(av.getValue(), i));

                if (createdNodes.contains(child)) {
                    child = createdNodes.get(createdNodes.indexOf(child));
                }
                av.getFields().add(child);
                child.getFields().add(av);

                if (PRIMITIVES.contains(child.getValue().getClass())) {
                    LOGGER.info("Processing primitive type: "
                            + child.getValue().getClass());
                    primitiveNodes.add(child);
                    createdNodes.add(child);
                    av.getPrimitives().add(child);
                }
            }
            return;
        }
        // Set all fields accessible
        for (int i = 0; i < children.size(); i++) {
            children.get(i).setAccessible(true);
        }

        LOGGER.info("Amount of Fields for " + av.getValue().getClass() + ": "
                + children.size());

        for (Field f : children) {
            Object value = new Object();

            // get the value of the field
            try {
                value = f.get(av.getValue());
            } catch (IllegalArgumentException | IllegalAccessException e) {
                e.printStackTrace();
                return;
            }

            // The value of a field can be null
            // AttributeValues with null cannot be created
            if (value == null) {
                continue;
            }

            if (!PRIMITIVES.contains(value.getClass())) {
                if (value instanceof Collection<?>) {
                    LOGGER.info(
                            "Processing collection type: " + value.getClass());
                    AttributeValue list = new AttributeValue(value, f.getName(),
                            av.getIdentifier(), -1);
                    AttributeValue rep = getDuplicate(list);
                    if (rep != null) {
                        list = rep;
                    }
                    AttributeValue size = new AttributeValue(
                            ((Collection<?>) value).size(), "size",
                            list.getIdentifier(), -1);

                    createdNodes.add(list);
                    createdNodes.add(size);
                    primitiveNodes.add(size);

                    av.getFields().add(list);
                    list.getFields().add(av);
                    list.getFields().add(size);
                    size.getFields().add(list);
                    list.getPrimitives().add(size);

                    int index = 0;
                    for (Object o : (Collection<?>) value) {
                        if (referenceContains(references, o)) {
                            // continue;
                        }
                        references.add(o);
                        AttributeValue child = new AttributeValue(o,
                                "(" + index + ")", list.getIdentifier(), index);
                        AttributeValue repChild = getDuplicate(child);
                        if (repChild != null) {
                            child = repChild;
                        }
                        list.getFields().add(child);
                        child.getFields().add(list);
                        if (child.isPrimitive()) {
                            createdNodes.add(child);
                            primitiveNodes.add(child);
                            list.getPrimitives().add(child);
                        }
                        index++;
                    }

                } else if (value.getClass().isEnum()) {
                    LOGGER.info("Processing Enum: " + value.getClass());

                    AttributeValue child = new AttributeValue(value,
                            f.getName(), av.getIdentifier(), -1);
                    AttributeValue repChild = getDuplicate(child);
                    if (repChild != null) {
                        child = repChild;
                    }

                    if (createdNodes.contains(child)) {
                        child = createdNodes.get(createdNodes.indexOf(child));
                    }
                    av.getFields().add(child);
                    child.getFields().add(av);

                    createdNodes.add(child);

                } else {
                    LOGGER.info("Processing \"standard\" type: "
                            + value.getClass());

                    AttributeValue child = new AttributeValue(value,
                            f.getName(), av.getIdentifier(), -1);
                    AttributeValue repChild = getDuplicate(child);
                    if (repChild != null) {
                        child = repChild;
                    }
                    av.getFields().add(child);
                    child.getFields().add(av);
                }
            } else {
                LOGGER.info("Processing primitive type: " + value.getClass()
                        + " (" + f.getName() + ")");

                AttributeValue child = new AttributeValue(value, f.getName(),
                        av.getIdentifier(), -1);
                createdNodes.add(child);
                primitiveNodes.add(child);
                av.getFields().add(child);
                child.getFields().add(av);
                av.getPrimitives().add(child);
            }
        }
    }

    /**
     * Matches two {@link AttributeGraph}s against each other. First a list of
     * {@link Tuple}s is created, which contains {@link AttributeValue}s which
     * have the same {@link AttributeValue#identifier} and are both primitive.
     * After that all nodes that are not in the given graph are removed from
     * this one and on the remaining primitives the given function is applied.
     * 
     * If one of the Graphs was not build, this method will also build them.
     * 
     * @param ag
     *            the second <code>AttributeGraph</code>
     * 
     * @param func
     *            the function to use on two nodes
     */
    public void matchPrimitives(final AttributeGraph ag,
            IAttributeMatcher func) {
        if (ag.getRoot().getFields().isEmpty()) {
            ag.buildGraph();
        }
        if (root.getFields().isEmpty()) {
            buildGraph();
        }
        List<Tuple<AttributeValue, AttributeValue>> matched = new ArrayList<>();
        for (AttributeValue one : primitiveNodes) {
            for (AttributeValue two : ag.primitiveNodes) {
                if (one.equals(two)) {
                    matched.add(new Tuple<AttributeValue, AttributeValue>(one,
                            two));
                }
            }
        }
        List<IObject> toRemove = new ArrayList<>();
        toRemove.addAll(primitiveNodes);
        for (Tuple<AttributeValue, AttributeValue> t : matched) {
            toRemove.remove(t.getX());
        }
        removeList(toRemove);
        primitiveNodes.removeAll(toRemove);
        for (Tuple<AttributeValue, AttributeValue> tuple : matched) {
            func.onMatch(tuple.getX(), tuple.getY());
        }
    }

    /**
     * Matches two {@link AttributeGraph}s against each other. First a list of
     * {@link Tuple}s is created, which contains {@link AttributeValue}s which
     * have the same {@link AttributeValue#identifier} and are of the specified
     * type. After that the given function is applied on all <code>Tuple</code>
     * s.
     * 
     * @param ag
     *            the second <code>AttributeGraph</code>
     * 
     * @param func
     *            the function to use on two nodes
     * 
     * @param type
     *            the class of which all nodes value has to be type of
     */
    public void matchType(final AttributeGraph ag, IAttributeMatcher func,
            final Class<?> type) {
        List<Tuple<AttributeValue, AttributeValue>> matched = new ArrayList<>();
        for (AttributeValue one : createdNodes) {
            for (AttributeValue two : ag.createdNodes) {
                if (one.equals(two)
                        && type.isAssignableFrom(one.getValue().getClass())
                        && type.isAssignableFrom(two.getValue().getClass())) {
                    matched.add(new Tuple<AttributeValue, AttributeValue>(one,
                            two));
                }
            }
        }
        for (Tuple<AttributeValue, AttributeValue> tuple : matched) {
            func.onMatch(tuple.getX(), tuple.getY());
        }
    }

    /**
     * Returns the root of this AttributeGraph.
     *
     * @return the root
     */
    public AttributeValue getRoot() {
        return root;
    }

    /**
     * Returns a list of all primitives of this IGraph.
     *
     * @return a list of primitives
     */
    public List<AttributeValue> getPrimitives() {
        return primitiveNodes;
    }

    /**
     * Returns a simple <code>String</code> representation of this IGraph. The
     * <code>String</code> contains all connections of every node created.
     */
    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        for (AttributeValue a : createdNodes) {
            sb.append(a.toString() + "\n");
        }
        return sb.toString();
    }

    /**
     * Checks two <code>AttributeGraph</code>s for equality.
     *
     * Two Graphs are equal if they both have the same amount of children and
     * all these children are equal.
     */
    @Override
    public boolean equals(final Object o) {
        if (!(o instanceof AttributeGraph)) {
            return false;
        }
        if (createdNodes.size() != ((AttributeGraph) o).createdNodes.size()
                || !root.equals(((AttributeGraph) o).root)) {
            return false;
        }

        boolean nodes = true;
        for (int i = 0; i < createdNodes.size(); i++) {
            nodes &= createdNodes.get(i)
                    .equals(((AttributeGraph) o).createdNodes.get(i));
        }
        return nodes;
    }

    @Override
    public int hashCode() {
        return Objects.hash(createdNodes, root);
    }

}
