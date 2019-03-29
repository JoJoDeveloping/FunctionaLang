package de.jojomodding.lang.ast.pattern;

import de.jojomodding.lang.env.Environment;
import de.jojomodding.lang.env.ElabEnvironment;
import de.jojomodding.lang.exception.ElaborationException;
import de.jojomodding.lang.exception.EvaluationException;
import de.jojomodding.lang.type.TupleType;
import de.jojomodding.lang.type.Type;
import de.jojomodding.lang.value.TupleValue;
import de.jojomodding.lang.value.Value;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class TuplePattern extends Pattern {

    private List<Pattern> subpatterns;

    public TuplePattern(Pattern... p){
        subpatterns = Arrays.asList(p);
        asChildren(p);
    }

    public TuplePattern(List<Pattern> p){
        this.subpatterns = p;
        asChildren(p.toArray(new Pattern[0]));
    }

    @Override
    public Type getType(ElabEnvironment env) throws ElaborationException {
        Type[] t = new Type[subpatterns.size()];
        for(int i = 0; i < subpatterns.size(); i++){
            t[i] = subpatterns.get(i).getType(env);
        }
        return new TupleType(t);
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
        if(!(vv instanceof TupleValue)) throw new EvaluationException(this, "Can't match non-tuple to tuple pattern");
        TupleValue tv = (TupleValue) vv;
        if(tv.entries().size() != subpatterns.size()) throw new EvaluationException(this, "Value and pattern size differs");
        Optional<Map<String, Value>> om = Optional.of(Map.of());
        for(int i = 0; i < tv.entries().size(); i++){
            Pattern p = subpatterns.get(i);
            Value v = tv.entries().get(i);
            Optional<Map<String, Value>> ov = p.matchValue(env, v);
            if(ov.isPresent() && om.isPresent()){
                try {
                    om = Optional.of(Pattern.mergeMaps(om.get(), ov.get()));
                } catch (DuplicateEntryException e) {
                    throw new EvaluationException(this, "Duplcate entry "+e.getDuplicateEntry()+" in pattern");
                }
            }else om = Optional.empty();
        }
        return om;
    }
}
