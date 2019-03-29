package de.jojomodding.lang.value;

import de.jojomodding.lang.env.ElabEnvironment;
import de.jojomodding.lang.type.Datatype;
import de.jojomodding.lang.type.Type;

import java.util.Objects;

public class AtomValue extends ConstantValue{

    private Datatype.DatatypeDef def;
    private String atom;

    public AtomValue(Datatype.DatatypeDef def, String atom){
        this.def = def;
        this.atom = atom;
    }

    public String getName(){
        return atom;
    }

    public Datatype.DatatypeDef getTypedef(){
        return def;
    }

    @Override
    public String toString() {
        return atom;
    }

    @Override
    public Type getType(ElabEnvironment env) {
        return env.dequantizeType(def.getAtomType());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AtomValue atomValue = (AtomValue) o;
        return def == atomValue.def &&
                atom.equals(atomValue.atom);
    }

    @Override
    public int hashCode() {
        return Objects.hash(def, atom);
    }
}
