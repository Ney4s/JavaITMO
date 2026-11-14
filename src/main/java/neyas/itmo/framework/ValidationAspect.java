package neyas.itmo.framework;

import neyas.itmo.exception.DomainValidationException;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.ConstructorSignature;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.annotation.Annotation;
import java.lang.reflect.Executable;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;

@Aspect
public class ValidationAspect {

    @Before("execution(neyas.itmo.entity..*.new(..)) && !within(neyas.itmo.entity.type..*)")
    public void validateConstructor(JoinPoint joinPoint) {
        if (joinPoint.getSignature() instanceof ConstructorSignature signature) {
            validate(signature.getConstructor(), joinPoint.getArgs());
        }
    }

    @Before("execution(* neyas.itmo.service..*.*(..))")
    public void validateMethod(JoinPoint joinPoint) {
        if (joinPoint.getSignature() instanceof MethodSignature signature) {
            validate(signature.getMethod(), joinPoint.getArgs());
        }
    }

    private void validate(Executable executable, Object[] arguments) {
        if (executable == null) {
            return;
        }
        List<String> violations = collect(executable, arguments);
        if (!violations.isEmpty()) {
            throw new DomainValidationException(String.join("; ", violations));
        }
    }

    private List<String> collect(Executable executable, Object[] arguments) {
        Parameter[] parameters = executable.getParameters();
        List<String> violations = new ArrayList<>();

        for (int index = 0; index < parameters.length && index < arguments.length; index++) {
            Parameter parameter = parameters[index];
            Object argument = arguments[index];
            String name = label(parameter);

            if (parameter.isAnnotationPresent(NotNull.class) && argument == null) {
                violations.add("%s не может быть null".formatted(name));
            }
            if (parameter.isAnnotationPresent(NotBlank.class) && isBlank(argument)) {
                violations.add("%s не может быть пустым".formatted(name));
            }
            if (parameter.isAnnotationPresent(Positive.class) && isNotPositive(argument)) {
                violations.add("%s должно быть больше нуля".formatted(name));
            }
            if (parameter.isAnnotationPresent(PositiveOrZero.class) && isNegative(argument)) {
                violations.add("%s не может быть отрицательным".formatted(name));
            }
        }
        return violations;
    }

    private String label(Parameter parameter) {
        String value = valueOf(parameter.getAnnotation(NotNull.class));
        if (value.isBlank()) {
            value = valueOf(parameter.getAnnotation(NotBlank.class));
        }
        if (value.isBlank()) {
            value = valueOf(parameter.getAnnotation(Positive.class));
        }
        if (value.isBlank()) {
            value = valueOf(parameter.getAnnotation(PositiveOrZero.class));
        }
        return value.isBlank() ? parameter.getName() : value;
    }

    private String valueOf(Annotation annotation) {
        return switch (annotation) {
            case NotNull notNull -> notNull.value();
            case NotBlank notBlank -> notBlank.value();
            case Positive positive -> positive.value();
            case PositiveOrZero positiveOrZero -> positiveOrZero.value();
            case null, default -> "";
        };
    }

    private boolean isBlank(Object argument) {
        return argument == null || (argument instanceof CharSequence text && text.toString().isBlank());
    }

    private boolean isNotPositive(Object argument) {
        return argument instanceof Number number && number.doubleValue() <= 0;
    }

    private boolean isNegative(Object argument) {
        return argument instanceof Number number && number.doubleValue() < 0;
    }
}
