package de.jojomodding.lang.ast.pattern;

import de.jojomodding.lang.env.Environment;
import de.jojomodding.lang.ast.ASTElement;
import de.jojomodding.lang.env.ElabEnvironment;
import de.jojomodding.lang.exception.ElaborationException;
import de.jojomodding.lang.exception.EvaluationException;
import de.jojomodding.lang.type.Type;
import de.jojomodding.lang.value.Value;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class PatternList extends ASTElement {

    private List<Pattern> subpatterns;

    public PatternList(Pattern... p){
        subpatterns = Arrays.asList(p);
        asChildren(p);
    }

    public PatternList(List<Pattern> p){
        this.subpatterns = p;
        asChildren(p.toArray(new Pattern[0]));
    }

    public List<Type> getType(ElabEnvironment env) throws ElaborationException {
        Type[] t = new Type[subpatterns.size()];
        for(int i = 0; i < subpatterns.size(); i++){
            t[i] = subpatterns.get(i).getType(env);
        }
        return Arrays.asList(t);
    }

    public Map<String, Type> getTypes(ElabEnvironment env) throws ElaborationException {
        Map<String, Type> m = Map.of();
        for(Pattern p : subpatterns) {
            try {
                m = Pattern.mergeMaps(m, p.getTypes(env));
            } catch (Pattern.DuplicateEntryException e) {
                throw new ElaborationException(this, "Duplicate entry "+e.getDuplicateEntry()+" in pattern");
            }
        }
        return m;
    }

    public Optional<Map<String, Value>> matchValues(Environment<Value> env, List<Value> vv) throws EvaluationException {
        Optional<Map<String, Value>> om = Optional.of(Map.of());
        for(int i = 0; i < vv.size(); i++){
            Pattern p = subpatterns.get(i);
            Value v = vv.get(i);
            Optional<Map<String, Value>> ov = p.matchValue(env, v);
            if(ov.isPresent() && om.isPresent()){
                try {
                    om = Optional.of(Pattern.mergeMaps(om.get(), ov.get()));
                } catch (Pattern.DuplicateEntryException e) {
                    throw new EvaluationException(this, "Duplcate entry "+e.getDuplicateEntry()+" in pattern");
                }
            }else om = Optional.empty();
        }
        return om;
    }

    public int size() {
        return this.subpatterns.size();
    }
}
