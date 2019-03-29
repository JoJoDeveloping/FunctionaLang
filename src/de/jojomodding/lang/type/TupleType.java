package de.jojomodding.lang.type;

import de.jojomodding.lang.env.ElabEnvironment;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class TupleType extends Type{

    private List<Type> types;

    public TupleType(Type... types){
        this.types = Arrays.asList(types);
    }

    @Override
    public String toString() {
        if(types.size() == 0){
            return "unit";
        }else if(types.size() == 1){
            throw new RuntimeException("Tuple with size 1");
        }else{
            return types.stream().map(t -> {
                if(t instanceof FunctionType) return "("+t.toString()+")";
                return t.toString();
            }).collect(Collectors.joining("*"));
        }
    }

    @Override
    public String deparse(ElabEnvironment env, DeparseHelper dh) {
        if(types.size() == 0){
            return "unit";
        }else if(types.size() == 1){
            throw new RuntimeException("Tuple with size 1");
        }else{
            return types.stream().map(t -> {
                if(t instanceof FunctionType) return "("+t.deparse(env, dh)+")";
                return t.deparse(env, dh);
            }).collect(Collectors.joining("*"));
        }
    }

    public List<Type> entries(){
        return types;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TupleType tupleType = (TupleType) o;
        return types.equals(tupleType.types);
    }

    @Override
    public int hashCode() {
        return Objects.hash(types);
    }
}
