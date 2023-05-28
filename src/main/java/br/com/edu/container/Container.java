package br.com.edu.container;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

public final class Container {

    private final Map<Class<?>, Object> instances = new HashMap<>();
    private final Map<Class<?>, Class<?>> abstractions = new HashMap<>();

    public <T> void register(final Class<T> clazz) {
        if (instances.containsKey(clazz)) {
            return;
        }

        try {
            final T instance = newInstance(clazz);
            instances.put(clazz, instance);
            injectDependencies(instance);
        } catch (final InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException ex) {
            throw new RuntimeException("Failed to register bean: %s".formatted(clazz.getSimpleName()), ex);
        }
    }

    public <T, I extends T> void register(final Class<T> abstraction, final Class<I> implementation) {
        if (!abstraction.isInterface() && isAbstraction(abstraction)) {
            throw new IllegalArgumentException("The '%s' should be an interface or an abstract class".formatted(abstraction.getSimpleName()));
        }

        if (abstractions.containsKey(abstraction)) {
            return;
        }

        abstractions.put(abstraction, implementation);
        register(implementation);
    }

    public <T> T resolve(final Class<T> clazz) {
        @SuppressWarnings("unchecked")
        final T instance = (T) instances.get(clazz);

        if (instance != null) {
            return instance;
        }

        final Class<?> implementation = abstractions.get(clazz);

        if (implementation == null) {
            throw new RuntimeException("The '%s' class is unregistered on the container".formatted(clazz.getSimpleName()));
        }

        try {
            @SuppressWarnings("unchecked")
            final T implementationInstance = (T) newInstance(implementation);
            injectDependencies(implementationInstance);
            instances.put(clazz, implementationInstance);
            return implementationInstance;
        } catch (final InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException ex) {
            throw new RuntimeException("Cannot instantiate the class: %s".formatted(clazz.getSimpleName()));
        }
    }

    private boolean isAbstraction(final Class<?> clazz) {
        return (clazz.getModifiers() & Modifier.ABSTRACT) == 0;
    }

    private <T> T newInstance(final Class<T> clazz) throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        final Constructor<?>[] constructors = clazz.getDeclaredConstructors();

        if (constructors.length > 1) {
            throw new IllegalStateException("The '%s' class should be have one constructor only".formatted(clazz.getSimpleName()));
        }

        final Constructor<?> constructor = constructors[0];
        final Class<?>[] parameterTypes = constructor.getParameterTypes();
        final Object[] parameters = new Object[parameterTypes.length];

        for (int i = 0; i < parameterTypes.length; i++) {
            parameters[i] = resolve(parameterTypes[i]);
        }

        constructor.setAccessible(true);

        @SuppressWarnings("unchecked")
        final T instance = (T) constructor.newInstance(parameters);

        return instance;
    }

    private <T> void injectDependencies(final T instanceClass) throws IllegalAccessException {
        final Class<?> typeClass = instanceClass.getClass();

        for (final Field field : typeClass.getDeclaredFields()) {
            if (field.isAnnotationPresent(Inject.class)) {
                field.setAccessible(true);

                final Class<?> fieldType = field.getType();
                final Object instanceDependency = resolve(fieldType);

                field.set(instanceClass, instanceDependency);
            }
        }
    }

}
