package de.jojomodding.lang.type;

import de.jojomodding.lang.env.ElabEnvironment;

import java.util.Objects;

public class TypeVariable extends Type{

    private String name;

    public TypeVariable(String name){
        this.name = name;
    }

    public String getName(){
        return name;
    }

    @Override
    public String toString() {
        return "'"+name;
    }

    @Override
    public String deparse(ElabEnvironment env, DeparseHelper dh) {
        String aname = dh.fromfirst.computeIfAbsent(name, s -> ElabEnvironment.format(new StringBuilder(), dh.i++).toString());
        if(!dh.quantifiedVars.contains(this.name)){
            aname = "~"+aname.toUpperCase();
            env.markAsFree(this);
        }
        return (env.hasEquality(this.name)?"''":"'")+aname;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TypeVariable that = (TypeVariable) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

}
