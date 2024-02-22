package org.koekepan.VAST.Packet;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class BehaviourHandler<T> {

    private final Map<Class<? extends T>, Behaviour<T>> behaviours = new ConcurrentHashMap<Class<? extends T>, Behaviour<T>>();

    public void registerBehaviour(Class<? extends T> type, Behaviour<T> behaviour) {
        if (behaviour != null) {
            behaviours.put(type, behaviour);
        } else {
            behaviours.remove(type);
        }
    }


    public Behaviour<T> getBehaviour(Class<? extends T> type) {
        return behaviours.get(type);
    }


    public boolean hasBehaviour(Class<? extends T> type) {
        return behaviours.containsKey(type);
    }


    public void clearBehaviours() {
        behaviours.clear();
    }


    public Set<Class<? extends T>> getTypes() {
        return new HashSet<Class<? extends T>>(behaviours.keySet());
    }

    public void process(T object) {
        Behaviour<T> behaviour = behaviours.get(object.getClass());
        if (behaviour != null) {
            behaviour.process(object);
        }
        else {
            System.out.println("No behaviour found for object of type: " + object.getClass().getName());
        }
    }

    public void printBehaviours() {
        for (Map.Entry<Class<? extends T>, Behaviour<T>> entry : behaviours.entrySet()) {
            System.out.println("Behaviour Type: " + entry.getKey().getName());
            System.out.println("Behaviour Instance: " + entry.getValue().toString());
        }
    }

    // Static merge method
    public static <T> BehaviourHandler<T> mergeBehaviourHandlers(BehaviourHandler<T> handler1, BehaviourHandler<T> handler2) {
        BehaviourHandler<T> newHandler = new BehaviourHandler<>();

        // Copy behaviours from the first handler
        for (Class<? extends T> type : handler1.getTypes()) {
            newHandler.registerBehaviour(type, handler1.getBehaviour(type));
        }

        // Copy behaviours from the second handler, possibly overwriting
        for (Class<? extends T> type : handler2.getTypes()) {
            newHandler.registerBehaviour(type, handler2.getBehaviour(type));
        }

        return newHandler;
    }


}
