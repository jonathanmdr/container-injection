package br.com.edu.container;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ContainerTest {

    private Container container;

    @BeforeEach
    void setup() {
        this.container = new Container();
    }

    @Test
    void givenADependencyClass_whenCallsRegister_shouldBeRegisterDependency() {
        this.container.register(BaseClass.class);
        final BaseClass baseClass = this.container.resolve(BaseClass.class);
        assertNotNull(baseClass);
    }

    @Test
    void givenADependencyGenericWithInterface_whenCallsRegister_shouldBeRegisterDependency() {
        this.container.register(InterfaceAbstraction.class, InterfaceAbstractionImpl.class);
        final InterfaceAbstraction interfaceAbstraction = this.container.resolve(InterfaceAbstraction.class);
        assertNotNull(interfaceAbstraction);
        assertTrue(interfaceAbstraction instanceof InterfaceAbstractionImpl);
    }

    @Test
    void givenADependencyGenericWithAbstractClass_whenCallsRegister_shouldBeRegisterDependency() {
        this.container.register(AbstractionClass.class, AbstractionClassImpl.class);
        final AbstractionClass abstractionClass = this.container.resolve(AbstractionClass.class);
        assertNotNull(abstractionClass);
        assertTrue(abstractionClass instanceof AbstractionClassImpl);
    }

    @Test
    void givenAClassConstructorBasedWithDeclaredDependencies_whenCallsResolve_shouldBeReturnValidInstanceWithDependencies() {
        this.container.register(InterfaceAbstraction.class, InterfaceAbstractionImpl.class);
        this.container.register(AbstractionClass.class, AbstractionClassImpl.class);
        this.container.register(ClassWithConstructorInjection.class);

        final ClassWithConstructorInjection classWithConstructorInjection = this.container.resolve(ClassWithConstructorInjection.class);
        assertNotNull(classWithConstructorInjection);
        assertNotNull(classWithConstructorInjection.interfaceAbstraction());
        assertNotNull(classWithConstructorInjection.abstractionClass());
        assertTrue(classWithConstructorInjection.interfaceAbstraction() instanceof InterfaceAbstractionImpl);
        assertTrue(classWithConstructorInjection.abstractionClass() instanceof AbstractionClassImpl);
    }

    @Test
    void givenAClassAnnotationBasedWithDeclaredDependencies_whenCallsResolve_shouldBeReturnValidInstanceWithDependencies() {
        this.container.register(InterfaceAbstraction.class, InterfaceAbstractionImpl.class);
        this.container.register(AbstractionClass.class, AbstractionClassImpl.class);
        this.container.register(ClassWithAnnotationInjection.class);

        final ClassWithAnnotationInjection classWithAnnotationInjection = this.container.resolve(ClassWithAnnotationInjection.class);
        assertNotNull(classWithAnnotationInjection);
        assertNotNull(classWithAnnotationInjection.interfaceAbstraction());
        assertNotNull(classWithAnnotationInjection.abstractionClass());
        assertTrue(classWithAnnotationInjection.interfaceAbstraction() instanceof InterfaceAbstractionImpl);
        assertTrue(classWithAnnotationInjection.abstractionClass() instanceof AbstractionClassImpl);
    }

    @Test
    void givenAConcreteClass_whenCallsRegisterAbstractionWithConcreteClass_shouldBeThrownIllegalArgumentException() {
        final var exception = assertThrows(
            IllegalArgumentException.class,
            () -> this.container.register(BaseClass.class, BaseClass.class)
        );

        assertEquals(
            "The 'BaseClass' should be an interface or an abstract class",
            exception.getMessage()
        );
    }

    @Test
    void givenAClassWithMultipleConstructors_whenCallsRegister_shouldBeThrownIllegalStateException() {
        final var exception = assertThrows(
            IllegalStateException.class,
            () -> this.container.register(ClassWithMultipleConstructors.class)
        );

        assertEquals(
            "The 'ClassWithMultipleConstructors' class should be have one constructor only",
            exception.getMessage()
        );
    }

    @Test
    void givenAnUnregisteredClass_whenCallsResolve_shouldBeThrownRuntimeException() {
        final var exception = assertThrows(
            RuntimeException.class,
            () -> this.container.resolve(BaseClass.class)
        );

        assertEquals(
            "The 'BaseClass' class is unregistered on the container",
            exception.getMessage()
        );
    }

    @Test
    void givenAClassWithInstantiateProblem_whenCallsRegister_shouldBeThrownRuntimeException() {
        final var exception = assertThrows(
            RuntimeException.class,
            () -> this.container.register(ClassThrowsWhenInstantiate.class)
        );

        assertEquals(
            "Failed to register bean: ClassThrowsWhenInstantiate",
            exception.getMessage()
        );
    }

    @Test
    void givenMultipleAbstractionClasses_whenCallsRegisterTwoTimes_shouldBeIgnoreSecondInvocation() {
        this.container.register(AbstractionClass.class, AbstractionClassImpl.class);
        this.container.register(AbstractionClass.class, OtherAbstractionClassImpl.class);

        final var abstractionClass = this.container.resolve(AbstractionClass.class);

        assertNotNull(abstractionClass);
        assertTrue(abstractionClass instanceof AbstractionClassImpl);
    }

    @Test
    void givenAClass_whenCallsRegisterTwoTimes_shouldBeIgnoreSecondInvocation() {
        this.container.register(BaseClass.class);
        this.container.register(BaseClass.class);

        final var baseClass = this.container.resolve(BaseClass.class);

        assertNotNull(baseClass);
    }

    static class BaseClass { }

    interface InterfaceAbstraction { }

    static class InterfaceAbstractionImpl implements InterfaceAbstraction { }

    static abstract class AbstractionClass { }

    static class AbstractionClassImpl extends AbstractionClass { }

    static class OtherAbstractionClassImpl extends AbstractionClass { }

    static class ClassWithConstructorInjection {
        private final InterfaceAbstraction interfaceAbstraction;
        private final AbstractionClass abstractionClass;

        public ClassWithConstructorInjection(
            final InterfaceAbstraction interfaceAbstraction,
            final AbstractionClass abstractionClass
        ) {
            this.interfaceAbstraction = interfaceAbstraction;
            this.abstractionClass = abstractionClass;
        }

        public InterfaceAbstraction interfaceAbstraction() {
            return this.interfaceAbstraction;
        }

        public AbstractionClass abstractionClass() {
            return this.abstractionClass;
        }
    }

    static class ClassWithAnnotationInjection {
        @Inject
        private InterfaceAbstraction interfaceAbstraction;
        @Inject
        private AbstractionClass abstractionClass;

        public InterfaceAbstraction interfaceAbstraction() {
            return this.interfaceAbstraction;
        }

        public AbstractionClass abstractionClass() {
            return this.abstractionClass;
        }
    }

    static class ClassWithMultipleConstructors {
        public ClassWithMultipleConstructors() { }
        public ClassWithMultipleConstructors(final BaseClass baseClass) { }
    }

    static class ClassThrowsWhenInstantiate extends AbstractionClass {
        public ClassThrowsWhenInstantiate() throws InstantiationException {
            throw new InstantiationException("");
        }
    }

}
