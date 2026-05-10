import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public abstract class TypeToken<T> {

    private final Type capturedType;

    protected TypeToken() {
        Type superclass = getClass().getGenericSuperclass();
        if (!(superclass instanceof ParameterizedType pt))
            throw new RuntimeException("TypeToken must be created with a type argument, e.g. new TypeToken<List<String>>(){}");
        this.capturedType = pt.getActualTypeArguments()[0];
    }

    public Type getType() { return capturedType; }
}
