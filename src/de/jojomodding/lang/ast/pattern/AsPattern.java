package de.jojomodding.lang.ast.pattern;

import de.jojomodding.lang.env.Environment;
import de.jojomodding.lang.env.ElabEnvironment;
import de.jojomodding.lang.exception.ElaborationException;
import de.jojomodding.lang.exception.EvaluationException;
import de.jojomodding.lang.type.Type;
import de.jojomodding.lang.value.Value;

import java.util.Map;
import java.util.Optional;

public class AsPattern extends Pattern {

    private Pattern l, r;

    public AsPattern(Pattern l, Pattern r){
        this.l = l;
        this.r = r;
        asChildren(l,r);
    }

    @Override
    public Type getType(ElabEnvironment env) throws ElaborationException {
        Type tl = l.getType(env), tr = r.getType(env);
        env.forcetype(this, tl, tr);
        return tl;
    }

    @Override
    public Map<String, Type> getTypes(ElabEnvironment env) throws ElaborationException {
        try {
            return Pattern.mergeMaps(l.getTypes(env), r.getTypes(env));
        } catch (DuplicateEntryException e) {
            throw new ElaborationException(this, "Duplicate entry "+e.getDuplicateEntry()+" in pattern!");
        }
    }

    @Override
    public Optional<Map<String, Value>> matchValue(Environment<Value> env, Value v) throws EvaluationException {
        Optional<Map<String,Value>> m1 = l.matchValue(env, v), m2 = r.matchValue(env, v);
        if(m1.isPresent() && m2.isPresent()){
            try {
                return Optional.of(Pattern.mergeMaps(m1.get(), m2.get()));
            } catch (DuplicateEntryException e) {
                throw new EvaluationException(this, "Duplicate entry "+e.getDuplicateEntry()+" in pattern!");
            }
        }
        return Optional.empty();
    }

}
