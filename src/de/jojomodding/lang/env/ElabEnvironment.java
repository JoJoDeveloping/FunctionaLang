package de.jojomodding.lang.env;

import de.jojomodding.lang.ast.ASTElement;
import de.jojomodding.lang.exception.ElaborationException;
import de.jojomodding.lang.type.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ElabEnvironment extends Environment<Type> {

    private Map<String, Type> forcedTypes = new HashMap<>();
    private Set<String> requireEquality = new HashSet<String>();
    private Map<String, TypeVariable> userProvided = new HashMap<>();
    private HashSet<String> seen1 = new HashSet<>(), seen2 = new HashSet<>();

    public void forcetype(ASTElement me, Type t1, Type t2) throws ElaborationException {
        _forcetype(me, t1, t2);
        if(t1 instanceof TypeVariable) isValid(me, ((TypeVariable) t1).getName());
        if(t2 instanceof TypeVariable) isValid(me, ((TypeVariable) t2).getName());
    }

    private void _forcetype(ASTElement me, Type t1, Type t2) throws ElaborationException {
        String tv1n = null, tv2n = null;
        Type t1r = null, t2r = null;
        if(t1 instanceof TypeVariable){
            TypeVariable tv = (TypeVariable) t1;
            tv1n = tv.getName();
            if(forcedTypes.containsKey(tv.getName()))
                t1r = forcedTypes.get(tv.getName());
        }
        if(t2 instanceof TypeVariable){
            TypeVariable tv = (TypeVariable) t2;
            tv2n = tv.getName();
            if(forcedTypes.containsKey(tv.getName()))
                t2r = forcedTypes.get(tv.getName());
        }

        if(tv1n == null && tv2n == null){
            if(t1 instanceof BaseType && t2 instanceof BaseType){
                if(!t1.equals(t2)) throw new ElaborationException(me, "Expected type "+t2+", but got "+t1);
            }else if(t1 instanceof FunctionType && t2 instanceof FunctionType){
                _forcetype(me, ((FunctionType) t1).getArgumentType(), ((FunctionType) t2).getArgumentType());
                _forcetype(me, ((FunctionType) t1).getResultType(), ((FunctionType) t2).getResultType());
            }else if(t1 instanceof TupleType && t2 instanceof TupleType){
                List<Type> t1l = ((TupleType) t1).entries(), t2l = ((TupleType) t2).entries();
                if(t1l.size() != t2l.size()) throw new ElaborationException(me, "Tuples of different length!");
                for(int i = 0; i < t1l.size(); i++){
                    _forcetype(me, t1l.get(i), t2l.get(i));
                }
            }else if(t1 instanceof Datatype && t2 instanceof Datatype){
                if(((Datatype) t1).getDef() == ((Datatype) t2).getDef()){
                    for(int i = 0; i < ((Datatype) t1).subtypes().size(); i++){
                        _forcetype(me, ((Datatype) t1).subtypes().get(i), ((Datatype) t2).subtypes().get(i));
                    }
                }else throw new ElaborationException(me, "Expected "+t2+", but got "+t1);
            }else
                throw new ElaborationException(me, "Expected type "+t2+", but got "+t1);
        }else if (tv1n != null && tv2n == null){
            if(t1r != null){
                circ1(me, tv1n);
                _forcetype(me, t1r, t2);
                uncirc1(tv1n);
            }else forcedTypes.put(tv1n, t2);
        }else if(tv1n == null && tv2n != null){
            if(t2r != null){
                circ2(me, tv2n);
                _forcetype(me, t1, t2r);
                uncirc2(tv2n);
            }else forcedTypes.put(tv2n, t1);
        }else if(tv1n != null && tv2n != null){
            if(tv1n.equals(tv2n)) return;
            if(t1r != null && t2r != null){
                circ1(me, tv1n);
                circ2(me, tv2n);
                _forcetype(me, t1r, t2r);
                uncirc1(tv1n);
                uncirc2(tv2n);
            }else if(t1r != null && t2r == null){
                circ1(me, tv1n);
                _forcetype(me, t1r, t2);
                uncirc1(tv1n);
            }else if(t1r == null && t2r != null){
                circ2(me, tv2n);
                _forcetype(me, t1, t2r);
                uncirc2(tv2n);
            }else if(t1r == null && t2r == null)
                forcedTypes.put(tv1n, t2);
        }

        if(hasEquality(t1))
            forceEquality(me, t2);
        else if(hasEquality(t2))
            forceEquality(me, t1);
    }

    private void circ1(ASTElement exp, String name) throws ElaborationException {
        if(seen1.contains(name))
            throw new ElaborationException(exp, "Circularity during elaboration!");
        seen1.add(name);
    }

    private void uncirc1(String name){
        seen1.remove(name);
    }

    private void circ2(ASTElement exp, String name) throws ElaborationException {
        if(seen2.contains(name))
            throw new ElaborationException(exp, "Circularity during elaboration!");
        seen2.add(name);
    }

    private void uncirc2(String name){
        seen2.remove(name);
    }

    private void isValid(ASTElement e, String name) throws ElaborationException {
        circ1(e, name);
        if(forcedTypes.containsKey(name)){
            Type t = forcedTypes.get(name);
            if(t instanceof BaseType){/*do nothing*/}
            else if(t instanceof FunctionType){
                Type arg = ((FunctionType) t).getArgumentType(), res = ((FunctionType) t).getResultType();
                if(arg instanceof TypeVariable) isValid(e, ((TypeVariable) arg).getName());
                if(res instanceof TypeVariable) isValid(e, ((TypeVariable) res).getName());
            }else if(t instanceof TypeVariable) isValid(e, ((TypeVariable) t).getName());
            else if(t instanceof TupleType){
                List<Type> tl = ((TupleType) t).entries();
                for(Type tt : tl)
                    if(tt instanceof TypeVariable) isValid(e, ((TypeVariable) tt).getName());

            }else if(t instanceof Datatype){
                for(Type tt : ((Datatype) t).subtypes())
                    if(tt instanceof TypeVariable)
                        isValid(e, ((TypeVariable) tt).getName());
            }
        }
        uncirc1(name);
    }


    public boolean hasEquality(Type t){
        if(t instanceof BaseType){
            return true;
        }else if(t instanceof FunctionType){
            return false;
        }else if(t instanceof TypeVariable){
            return requireEquality.contains(((TypeVariable) t).getName());
        }else if(t instanceof TupleType){
            return ((TupleType) t).entries().stream().allMatch(this::hasEquality);
        }else if(t instanceof Datatype){
            return ((Datatype) t).getDef().canEquality() && ((Datatype) t).subtypes().stream().allMatch(this::hasEquality);
        }else
            throw new RuntimeException("Unknown class "+t.getClass());
    }

    public boolean canEquality(Type t) {
        if(t instanceof BaseType){
            return true;
        }else if(t instanceof FunctionType){
            return false;
        }else if(t instanceof TypeVariable){
            return true;
        }else if(t instanceof TupleType){
            return ((TupleType) t).entries().stream().allMatch(this::canEquality);
        }else if(t instanceof Datatype){
            return ((Datatype) t).getDef().canEquality() && ((Datatype) t).subtypes().stream().allMatch(this::canEquality);
        }else
            throw new RuntimeException("Unknown class "+t.getClass());
    }

    public void forceEquality(ASTElement me, Type t) throws ElaborationException{
        if(t instanceof FunctionType){
            throw new ElaborationException(me, "Functions do not have equality!");
        }else if(t instanceof TupleType) {
            for(Type tt : ((TupleType) t).entries())
                forceEquality(me, tt);
        }else if(t instanceof TypeVariable){
            if(forcedTypes.containsKey(((TypeVariable) t).getName())){
                circ1(me, ((TypeVariable) t).getName());
                forceEquality(me, forcedTypes.get(((TypeVariable) t).getName()));
                uncirc1(((TypeVariable) t).getName());
                return;
            }
            requireEquality.add(((TypeVariable) t).getName());
        }else if(t instanceof Datatype){
            if(!((Datatype) t).getDef().canEquality())
                throw new ElaborationException(me, "Type "+((Datatype) t).getDef().getName()+" does not admit equality!");
            for(Type tt : ((Datatype) t).subtypes())
                forceEquality(me, tt);
        }
    }

    @Override
    public ElabEnvironment replaceIn(String key, Type newValue) {
        ElabEnvironment env = deepCopy();
        env.impl.put(key, newValue);
        return env;
    }


    public ElabEnvironment replaceAllIn(Map<String, Type> vt) {
        ElabEnvironment o = deepCopy();
        o.impl.putAll(vt);
        return o;
    }

    public Type resolve(Type t){
        if(t instanceof TypeVariable){
            String name = ((TypeVariable) t).getName();
            if(forcedTypes.containsKey(name)){
                return resolve(forcedTypes.get(name));
            }
            return t;
        }else if(t instanceof FunctionType){
            return new FunctionType(resolve(((FunctionType) t).getArgumentType()), resolve(((FunctionType) t).getResultType()));
        }else if(t instanceof TupleType){
            return new TupleType(((TupleType) t).entries().stream().map(this::resolve).toArray(Type[]::new));
        }else if(t instanceof Datatype){
            return new Datatype(((Datatype) t).getDef(), ((Datatype) t).subtypes().stream().map(this::resolve).collect(Collectors.toList()));
        }else
            return t;
    }

    private AtomicInteger tvarC = new AtomicInteger(0);

    public TypeVariable newType(){
        return newType(false);
    }

    public TypeVariable newType(boolean eq){
        StringBuilder sb = new StringBuilder();
        format(sb, tvarC.getAndIncrement());
        TypeVariable tv = new TypeVariable(sb.toString());
        if(eq)requireEquality.add(sb.toString());
        return tv;
    }

    public static StringBuilder format(StringBuilder sb, int i){
        if(i < 26){
            sb.append((char)('a'+i));
        }else{
            format(sb, i/26);
            format(sb, i%26);
        }
        return sb;
    }

    public boolean hasEquality(String name) {
        return requireEquality.contains(name);
    }

    public TypeVariable translateUserProvidedVar(String tvar) {
        return translateUserProvidedVar(tvar, false);
    }

    public void newUservarScope(){
        userProvided.clear();
    }

    public Type withEquality(TypeVariable tv) {
        requireEquality.add(tv.getName());
        return tv;
    }

    public Type replaceTVars(Map<String, String> rep, Type t){
        if(t instanceof BaseType) return t;
        else if(t instanceof FunctionType){
            return new FunctionType(replaceTVars(rep, ((FunctionType) t).getArgumentType()), replaceTVars(rep, ((FunctionType) t).getResultType()));
        }else if(t instanceof TupleType){
            return new TupleType(((TupleType) t).entries().stream().map(tt -> replaceTVars(rep, tt)).toArray(Type[]::new));
        }else if(t instanceof TypeVariable){
            String s = rep.get(((TypeVariable) t).getName());
            if(s != null) return new TypeVariable(s);
            return t;
        }else if(t instanceof Datatype){
            return new Datatype(((Datatype) t).getDef(), ((Datatype) t).subtypes().stream().map(v -> replaceTVars(rep, v)).collect(Collectors.toList()));
        }else
            return t;
    }

    public Type dequantizeType(QuantizedType t){
        Map<String, String> m = t.getQuantized().stream().collect(Collectors.toMap(TypeVariable::getName, tv -> newType(hasEquality(tv)).getName()));
        return replaceTVars(m, t.getSubtype());
    }

    public List<TypeVariable> allTVars(Type t) {
        if(t instanceof BaseType) return List.of();
        else if(t instanceof FunctionType){
            return Stream.of(((FunctionType) t).getArgumentType(), ((FunctionType) t).getResultType()).flatMap(tt -> allTVars(tt).stream()).distinct().collect(Collectors.toList());
        }else if(t instanceof TupleType){
            return ((TupleType) t).entries().stream().flatMap(tt -> allTVars(tt).stream()).distinct().collect(Collectors.toList());
        }else if(t instanceof TypeVariable){
            return List.of((TypeVariable)t);
        }else if(t instanceof Datatype){
            return ((Datatype) t).subtypes().stream().flatMap(tt -> allTVars(tt).stream()).distinct().collect(Collectors.toList());
        }else return List.of();
    }

    @Override
    public ElabEnvironment deepCopy() {
        ElabEnvironment env = new ElabEnvironment();
        env.tvarC = tvarC;
        env.forcedTypes = forcedTypes;
        env.requireEquality = requireEquality;
        env.impl = new HashMap<>(impl);
        env.userProvided = userProvided;
        env.types = types;
        env.dtdefs = dtdefs;
        return env;
    }


    public TypeVariable translateUserProvidedVar(String tvar, boolean b) {
        return userProvided.computeIfAbsent(tvar, s->newType(b));
    }
}
