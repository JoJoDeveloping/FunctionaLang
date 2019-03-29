package de.jojomodding.lang.ast.def;

import de.jojomodding.lang.env.Environment;
import de.jojomodding.lang.env.ElabEnvironment;
import de.jojomodding.lang.type.*;
import de.jojomodding.lang.value.AtomValue;
import de.jojomodding.lang.value.ConstructorValue;
import de.jojomodding.lang.value.Value;

import java.util.List;

public class DatatypeDefinition extends Definition{

    private Datatype.DatatypeDef dtdef;
    private List<TypeVariable> tvs;
    public DatatypeDefinition(Datatype.DatatypeDef dtdef, List<TypeVariable> tvs) {
        this.dtdef = dtdef;
        this.tvs = tvs;
    }

    @Override
    public List<String> elaborate(ElabEnvironment env) {
        Datatype dt = new Datatype(dtdef, tvs);
        QuantizedType dtt = new QuantizedType(tvs, dt);
        for(String s : dtdef.getAtoms()){
            env.setVariable(s, dtt);
        }
        for(Datatype.DatatypeDef.DatatypeConstr constr : dtdef.getConstrs()){
            env.setVariable(constr.getName(), constr.getQuantizedType());
        }
        return List.of();
    }

    @Override
    public void evaluate(Environment<Value> env) {
        for(String s : dtdef.getAtoms()){
            env.setVariable(s, new AtomValue(dtdef, s));
        }
        for(Datatype.DatatypeDef.DatatypeConstr c : dtdef.getConstrs()){
            env.setVariable(c.getName(), new ConstructorValue(c));
        }
    }
}
