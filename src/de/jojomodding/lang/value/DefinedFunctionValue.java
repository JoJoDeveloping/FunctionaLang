package de.jojomodding.lang.value;

import de.jojomodding.lang.env.Environment;
import de.jojomodding.lang.ast.pattern.PatternListRow;

import java.util.Arrays;
import java.util.List;

public class DefinedFunctionValue extends Value{

    private String name;
    private Value[] values;
    private PatternListRow plr;
    private Environment<Value> env;
    private int appliedUntil;

    public DefinedFunctionValue(String name, PatternListRow def, Environment<Value> env){
        this.env = env.replaceIn(name, this);
        values = new Value[def.size()];
        appliedUntil = 0;
        this.name = name;
        this.plr = def;
    }

    private DefinedFunctionValue(DefinedFunctionValue otr) {
        super();
        this.values = Arrays.copyOf(otr.values, otr.values.length);
        this.appliedUntil = otr.appliedUntil;
        this.env = otr.env.deepCopy();
        this.plr = otr.plr;
    }

    public boolean hasAllParams(){
        return appliedUntil == plr.size();
    }

    public DefinedFunctionValue applyValue(Value v){
        DefinedFunctionValue dfv = new DefinedFunctionValue(this);
        if(dfv.appliedUntil < dfv.values.length)
            dfv.values[dfv.appliedUntil++] = v;
        return dfv;
    }

    public PatternListRow getDefinition(){
        return plr;
    }

    public List<Value> getValues(){
        return Arrays.asList(values);
    }

    @Override
    public String toString() {
        return "fn";
    }

    public Environment<Value> getEnv() {
        return env;
    }
}
