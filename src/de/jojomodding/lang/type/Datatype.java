package de.jojomodding.lang.type;

import de.jojomodding.lang.env.ElabEnvironment;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class Datatype extends Type{

    private DatatypeDef type;
    private List<Type> spec;

    public Datatype(DatatypeDef bt, Type... spec){
        this.type = bt;
        this.spec = Arrays.asList(spec);
    }

    public Datatype(DatatypeDef bt, List<? extends Type> t){
        this.type = bt;
        this.spec = (List<Type>) t;
    }

    public DatatypeDef getDef(){
        return type;
    }

    public List<Type> subtypes(){
        return spec;
    }

    @Override
    public String deparse(ElabEnvironment env, DeparseHelper help) {
        if(spec.size() == 0) return type.name;
        else if(spec.size() == 1) {
            Type t = spec.get(0);
            if(t instanceof FunctionType || t instanceof TupleType) return "("+t.deparse(env, help)+") "+type.name;
            else return t.deparse(env, help)+" "+type.name;
        }else{
            return spec.stream().map(t -> t.deparse(env, help)).collect(Collectors.joining(", ", "(", ") "+type.name));
        }
    }


    @Override
    public String toString() {
        if(spec.size() == 0) return type.name;
        else if(spec.size() == 1) {
            Type t = spec.get(0);
            if(t instanceof FunctionType || t instanceof TupleType) return "("+t.toString()+") "+type.name;
            else return t.toString()+" "+type.name;
        }else{
            return spec.stream().map(Type::toString).collect(Collectors.joining(", ", "(", ") "+type.name));
        }
    }


    public static class DatatypeDef{
        private String name;
        private int subtypes;
        private boolean eq;

        private QuantizedType atomType;

        public DatatypeDef(String name, List<TypeVariable> subtypes){
            this.subtypes = subtypes.size();
            this.name = name;
            eq = true;
            atomType = new QuantizedType(subtypes, new Datatype(this, subtypes));
        }

        public void addAtom(String name){
            atoms.add(name);
        }

        public DatatypeConstr addNewConstr(String name, List<TypeVariable> tvs, Type ts, boolean tseq) {
            DatatypeConstr dc = new DatatypeConstr();
            dc.name = name;
            dc.st = ts;
            dc.tvs = tvs;
            eq &= tseq;
            this.constrs.add(dc);
            return dc;
        }

        private Set<String> atoms = new HashSet<>();
        private Set<DatatypeConstr> constrs = new HashSet<>();

        public boolean canEquality() {
            return eq;
        }

        public String getName() {
            return name;
        }

        public int getAmountOfSpecifyingTypes() {
            return subtypes;
        }

        public Set<String> getAtoms() {
            return atoms;
        }

        public Set<DatatypeConstr> getConstrs() {
            return constrs;
        }

        public QuantizedType getAtomType() {
            return atomType;
        }

        public class DatatypeConstr{
            public List<TypeVariable> tvs;
            private String name;
            private Type st;

            public String getName() {
                return name;
            }

            public Type getRawType(){
                return st;
            }

            public QuantizedType getQuantizedType(){
                return new QuantizedType(tvs, new FunctionType(st, new Datatype(DatatypeDef.this, tvs)));
            }

            public DatatypeDef getParent(){
                return DatatypeDef.this;
            }

        }
    }

}
