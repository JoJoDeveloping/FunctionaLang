package de.jojomodding.lang.env;

import de.jojomodding.lang.type.Datatype;
import de.jojomodding.lang.value.ConstantValue;
import de.jojomodding.lang.value.Value;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

public class Environment<T> {

    protected Map<String, T> impl;
    protected Map<String, Datatype.DatatypeDef> dtdefs = new HashMap<>();
    protected BasicTypes types = new BasicTypes();

    public Environment(){
        impl = new HashMap<>();
    }

    private Environment(Map<String, T> content){
        this.impl = content;
    }

    public static <V> Environment<V> empty() {
        return new Environment<V>();
    }

    public T get(String key){
        return impl.get(key);
    }

    public Environment<T> replaceIn(String key, T newValue) {
        Environment<T> e = deepCopy();
        e.impl.put(key, newValue);
        return e;
    }

    public Stream<Map.Entry<String, T>> stream(){
        return impl.entrySet().stream();
    }

    public void forEach(BiConsumer<String, T> consumer){
        impl.forEach(consumer);
    }

    public boolean has(String name) {
        return impl.containsKey(name);
    }

    public void setVariable(String s, T v){
        impl.put(s, v);
    }

    public Environment<T> deepCopy() {
        Map<String, T> copy = new HashMap<>(impl);
        Environment e = new Environment<T>(copy);
        e.dtdefs = dtdefs;
        e.types = types;
        return e;
    }

    public Environment<T> replaceAllIn(Map<String, T> vt) {
        Environment<T> o = deepCopy();
        o.setAll(vt);
        return o;
    }

    public void setAll(Map<String, T> mst) {
        impl.putAll(mst);
    }


    public Datatype.DatatypeDef getDatatypeDef(String s) {
        return dtdefs.get(s);
    }

    public void addDatatypeDef(String name, Datatype.DatatypeDef def) throws IllegalArgumentException{
        if(this.dtdefs.put(name, def) != null) throw new IllegalArgumentException("Datatype "+name+" already exists!");
    }

    public void validateDatatypeDef(Datatype.DatatypeDef def) throws IllegalArgumentException{
        Optional<Map.Entry<String, Datatype.DatatypeDef>> o = def.getAtoms().stream().map(s -> isDatatypeAtom(s).map(d -> Map.entry(s,d))).flatMap(Optional::stream).findAny();
        if(o.isPresent()){
            throw new IllegalArgumentException("Datatype atom "+o.get().getKey()+" already registered with dt "+o.get().getValue().getName());
        }
        Optional<Datatype.DatatypeDef.DatatypeConstr> oo = def.getConstrs().stream().map(Datatype.DatatypeDef.DatatypeConstr::getName).map(this::isDatatypeConstr).flatMap(Optional::stream).findAny();
        if(oo.isPresent()){
            throw new IllegalArgumentException("Datatype atom "+oo.get().getName()+" already registered with dt "+oo.get().getParent().getName());
        }
    }

    public Optional<Datatype.DatatypeDef> isDatatypeAtom(String s){
        return dtdefs.values().stream().flatMap(p -> p.getAtoms().stream().map(v -> Map.entry(p,v))).filter(p -> p.getValue().equals(s)).findFirst().map(Map.Entry::getKey);
    }

    public Optional<Datatype.DatatypeDef.DatatypeConstr> isDatatypeConstr(String s){
        return dtdefs.values().stream()
                .flatMap(p -> p.getConstrs().stream().map(v -> Map.entry(p,v)))
                .filter(p -> p.getValue().getName().equals(s)).findFirst().map(Map.Entry::getValue);
    }

    public BasicTypes getBasicTypedefs(){
        return this.types;
    }
}
