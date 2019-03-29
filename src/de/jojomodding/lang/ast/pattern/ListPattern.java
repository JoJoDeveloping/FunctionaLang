package de.jojomodding.lang.ast.pattern;

import de.jojomodding.lang.ast.expression.Expression;
import de.jojomodding.lang.env.ElabEnvironment;
import de.jojomodding.lang.env.Environment;
import de.jojomodding.lang.exception.ElaborationException;
import de.jojomodding.lang.exception.EvaluationException;
import de.jojomodding.lang.type.Datatype;
import de.jojomodding.lang.type.TupleType;
import de.jojomodding.lang.type.Type;
import de.jojomodding.lang.value.AtomValue;
import de.jojomodding.lang.value.ConstructorValue;
import de.jojomodding.lang.value.TupleValue;
import de.jojomodding.lang.value.Value;

import java.util.*;

public class ListPattern extends Pattern {

    private List<Pattern> subpatterns;

    public ListPattern(Pattern... p){
        subpatterns = Arrays.asList(p);
        asChildren(p);
    }

    public ListPattern(List<Pattern> p){
        this.subpatterns = p;
        asChildren(p.toArray(new Pattern[0]));
    }

    @Override
    public Type getType(ElabEnvironment env) throws ElaborationException {
        Type bt = env.newType();
        for(Pattern e : subpatterns)
            env.forcetype(this, bt, e.getType(env));
        return new Datatype(env.getDatatypeDef("list"), bt);
    }

    @Override
    public Map<String, Type> getTypes(ElabEnvironment env) throws ElaborationException {
        Map<String, Type> m = Map.of();
        for(Pattern p : subpatterns) {
            try {
                m = Pattern.mergeMaps(m, p.getTypes(env));
            } catch (DuplicateEntryException e) {
                throw new ElaborationException(this, "Duplicate entry "+e.getDuplicateEntry()+" in pattern");
            }
        }
        return m;
    }

    @Override
    public Optional<Map<String, Value>> matchValue(Environment<Value> env, Value vv) throws EvaluationException {
        Value vtm = vv;
        Datatype.DatatypeDef.DatatypeConstr cc = env.isDatatypeConstr("op::").get();
        Map<String, Value> matched = Map.of();
        for(Pattern p : subpatterns){
            if(!(vtm instanceof ConstructorValue))
                return Optional.empty();
            ConstructorValue cv = (ConstructorValue) vtm;
            if(cv.getConstr() != cc || !cv.isApplied() || !(cv.getParam() instanceof TupleValue))
                return Optional.empty();
            TupleValue tv = (TupleValue) cv.getParam();
            if(tv.entries().size() != 2)
                return Optional.empty();
            Optional<Map<String, Value>> mv = p.matchValue(env, tv.entries().get(0));
            if(!mv.isPresent()) return mv;
            try {
                matched = Pattern.mergeMaps(matched, mv.get());
            } catch (DuplicateEntryException e) {
                throw new EvaluationException(this, "Duplicate entry "+e.getDuplicateEntry()+" in pattern");
            }
            vtm = tv.entries().get(1);
        }
        if(vtm instanceof AtomValue && ((AtomValue) vtm).getTypedef() == cc.getParent() && ((AtomValue) vtm).getName().equals("nil"))
            return Optional.of(matched);
        return Optional.empty();
    }

}
