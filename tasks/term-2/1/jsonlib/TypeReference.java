package jsonlib;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;


public abstract class TypeReference<T> {

    private final Type type;

    protected TypeReference() {
        Type superclass = getClass().getGenericSuperclass();
        if (!(superclass instanceof ParameterizedType)) {
            throw new IllegalStateException(
                "TypeReference must be created as an anonymous subclass with a type argument, "
                + "e.g. new TypeReference<List<Person>>() {}"
            );
        }
        this.type = ((ParameterizedType) superclass).getActualTypeArguments()[0];
    }

    /** Returns the captured {@link Type}, possibly a {@link ParameterizedType}. */
    public Type getType() {
        return type;
    }

    /** Convenience: raw erased class of the captured type. */
    @SuppressWarnings("unchecked")
    public Class<T> getRawType() {
        if (type instanceof Class)           return (Class<T>) type;
        if (type instanceof ParameterizedType) return (Class<T>) ((ParameterizedType) type).getRawType();
        throw new IllegalStateException("Cannot determine raw type of: " + type);
    }
}
