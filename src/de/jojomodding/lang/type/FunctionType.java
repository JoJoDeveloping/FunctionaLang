package de.jojomodding.lang.type;

import de.jojomodding.lang.env.ElabEnvironment;

import java.util.Objects;

public class FunctionType extends Type {

    private Type from, to;

    public FunctionType(Type from, Type to){
        this.from = from;
        this.to = to;
    }

    public Type getArgumentType(){
        return from;
    }

    public Type getResultType(){
        return to;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FunctionType that = (FunctionType) o;
        return Objects.equals(from, that.from) &&
                Objects.equals(to, that.to);
    }

    @Override
    public int hashCode() {
        return Objects.hash(from, to);
    }

    @Override
    public String toString() {
        return (from instanceof FunctionType ? "("+from+")" : from.toString())+"->"+to;
    }

    @Override
    public String deparse(ElabEnvironment env, DeparseHelper dh) {
        return (from instanceof FunctionType ? "("+from.deparse(env, dh)+")" : from.deparse(env, dh))+"->"+to.deparse(env, dh);
    }
}
