package com.cts.adstudio.finance.shared;

import com.cts.adstudio.finance.shared.exception.IllegalStatusTransitionException;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Shared engine that validates allowed status transitions for ANY entity
 * (Backend Plan §7 Day 3, Risk Register). Modules register their state machines
 * at startup and call {@link #validate}; no module implements inline status checks.
 *
 * Part of this service's common layer (could be extracted to a shared library).
 * Billing registers its invoice state machines into it at startup.
 */
@Component("financeStatusTransitionValidator")
public class StatusTransitionValidator {

    // enumType -> (currentName -> allowed target names)
    private final Map<Class<? extends Enum<?>>, Map<String, Set<String>>> machines = new ConcurrentHashMap<>();

    public <E extends Enum<E>> void register(Class<E> enumType, Map<E, Set<E>> transitions) {
        Map<String, Set<String>> byName = new HashMap<>();
        transitions.forEach((from, tos) ->
                byName.put(from.name(), tos.stream().map(Enum::name).collect(Collectors.toSet())));
        machines.put(enumType, byName);
    }

    /** Throws IllegalStatusTransitionException (HTTP 422) if current -> target is not allowed. */
    public <E extends Enum<E>> void validate(E current, E target) {
        if (current == target) return;
        Map<String, Set<String>> machine = machines.get(current.getDeclaringClass());
        Set<String> allowed = machine == null ? null : machine.get(current.name());
        if (allowed == null || !allowed.contains(target.name())) {
            throw new IllegalStatusTransitionException(
                    current.getDeclaringClass().getSimpleName(), current.name(), target.name());
        }
    }
}
