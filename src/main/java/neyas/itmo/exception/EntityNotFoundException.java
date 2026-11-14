package neyas.itmo.exception;

public class EntityNotFoundException extends DomainException {
    public EntityNotFoundException(String message) {
        super(message);
    }

    public static EntityNotFoundException of(Class<?> entityType, Object id){
        return new EntityNotFoundException("Обьект %s с ID: %s".formatted(entityType.getSimpleName(), id));
    }
}
