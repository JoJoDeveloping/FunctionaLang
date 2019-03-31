package de.jojomodding.lang.ast.def;

import de.jojomodding.lang.env.Environment;
import de.jojomodding.lang.env.ElabEnvironment;
import de.jojomodding.lang.type.*;
import de.jojomodding.lang.value.AtomValue;
import de.jojomodding.lang.value.ConstructorValue;
import de.jojomodding.lang.value.Value;

import java.util.ArrayList;
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
        List<String> pts = new ArrayList<>(dtdef.getAtoms().size() + dtdef.getConstrs().size());
        for(String s : dtdef.getAtoms()){
            env.setVariable(s, dtt);
            pts.add(s);
        }
        for(Datatype.DatatypeDef.DatatypeConstr constr : dtdef.getConstrs()){
            env.setVariable(constr.getName(), constr.getQuantizedType());
            pts.add(constr.getName());
        }
        return pts;
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
