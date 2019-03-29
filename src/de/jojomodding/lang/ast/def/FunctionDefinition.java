package de.jojomodding.lang.ast.def;

import de.jojomodding.lang.env.Environment;
import de.jojomodding.lang.ast.pattern.PatternListRow;
import de.jojomodding.lang.env.ElabEnvironment;
import de.jojomodding.lang.exception.ElaborationException;
import de.jojomodding.lang.exception.EvaluationException;
import de.jojomodding.lang.type.FunctionType;
import de.jojomodding.lang.type.QuantizedType;
import de.jojomodding.lang.type.Type;
import de.jojomodding.lang.type.TypeVariable;
import de.jojomodding.lang.value.DefinedFunctionValue;
import de.jojomodding.lang.value.Value;

import java.util.*;

public class FunctionDefinition extends Definition {

    private String name;
    private Type resultType;
    private PatternListRow plr;

    public FunctionDefinition(String name, Type rt, PatternListRow plr){
        this.name = name;
        this.resultType = rt;
        this.plr = plr;
        asChildren(plr);
    }

    @Override
    public List<String> elaborate(ElabEnvironment env) throws ElaborationException {
        Type ft = resultType;
        Type tft = env.newType();
        Map.Entry<List<Type>, Type> e = plr.elaborate(env.replaceIn(name, tft));
        for(int i = e.getKey().size() -1; i >= 0; i--){
            ft = new FunctionType(e.getKey().get(i), ft);
        }
        env.forcetype(this, e.getValue(), resultType);
        env.forcetype(this, ft, tft);
        ft = env.resolve(ft);
        List<TypeVariable> tvs = env.allTVars(ft);
        if(tvs.isEmpty()) env.setVariable(name, ft);
        else env.setVariable(name, new QuantizedType(tvs, ft));
        return List.of(name);
    }

    @Override
    public void evaluate(Environment<Value> env) throws EvaluationException {
        DefinedFunctionValue v = new DefinedFunctionValue(name, plr, env);
        env.setVariable(name, v);
    }
}
