package de.jojomodding.lang.env;

import de.jojomodding.lang.env.Environment;
import de.jojomodding.lang.ast.def.DatatypeDefinition;
import de.jojomodding.lang.env.ElabEnvironment;
import de.jojomodding.lang.type.*;
import de.jojomodding.lang.value.Value;

import java.util.List;

public class BasicTypes {

    public Datatype.DatatypeDef listTypedef;
    public Datatype.DatatypeDef.DatatypeConstr listCons;
    private DatatypeDefinition listDef;

    public void initElab(ElabEnvironment env){
        TypeVariable lt = env.newType();
        listTypedef = new Datatype.DatatypeDef("list", List.of(lt));
        listTypedef.addAtom("nil");
        Datatype listType = new Datatype(listTypedef, List.of(lt));
        listCons = listTypedef.addNewConstr("op::", List.of(lt), new TupleType(lt, listType), true);
        env.addDatatypeDef("list", listTypedef);
        listDef = new DatatypeDefinition(listTypedef, List.of(lt));
        listDef.elaborate(env);
    }

    public void initEval(Environment<Value> v){
        v.addDatatypeDef("list", listTypedef);
        listDef.evaluate(v);
    }

}
