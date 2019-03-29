package de.jojomodding.lang.type;

import de.jojomodding.lang.env.ElabEnvironment;

import java.util.List;
import java.util.stream.Collectors;

public class QuantizedType extends Type {

    private List<TypeVariable> qv;
    private Type t;

    public QuantizedType(List<TypeVariable> tvars, Type t){
        qv = tvars;
        this.t = t;
    }

    @Override
    public String deparse(ElabEnvironment env, DeparseHelper dh) {
        return "∀ "+qv.stream().map(t -> t.deparse(env, dh)).collect(Collectors.joining(", "))+": "+t.deparse(env, dh);
    }

    @Override
    public String toString() {
        return "∀ "+qv.stream().map(TypeVariable::toString).collect(Collectors.joining(", "))+": "+t.toString();
    }

    public List<TypeVariable> getQuantized() {
        return qv;
    }

    public Type getSubtype(){
        return t;
    }
}
