package de.jojomodding.lang.value;

import de.jojomodding.lang.type.Datatype;
import de.jojomodding.lang.type.TupleType;

public class ConstructorValue extends Value {

    private Datatype.DatatypeDef.DatatypeConstr constr;
    private Value param;

    public ConstructorValue(Datatype.DatatypeDef.DatatypeConstr c){
        this.constr = c;
        this.param = null;
    }

    public ConstructorValue apply(Value v){
        ConstructorValue cv = new ConstructorValue(constr);
        cv.param = v;
        return cv;
    }

    public boolean isApplied(){
        return param != null;
    }

    public Value getParam(){
        return param;
    }

    private String oldToString(){
        return constr.getName()+(param!=null?" "+param.toString():"");
    }

    @Override
    public String toString() {
        if(param instanceof TupleValue && constr.getParent().getName().equals("list") && constr.getName().equals("op::")){
            StringBuilder sb = new StringBuilder("[");
            boolean b = false;
            ConstructorValue cv = this;
            while (true){
                if(b) sb.append(", ");
                b=true;
                if(!(cv.param instanceof TupleValue && cv.constr.getParent().getName().equals("list") && cv.constr.getName().equals("op::")))
                    return oldToString();
                TupleValue tv = (TupleValue) cv.param;
                if(tv.entries().size() != 2)
                    return oldToString();
                Value v = tv.entries().get(0);
                sb.append(v.toString());
                Value nv = tv.entries().get(1);
                if(nv instanceof AtomValue && ((AtomValue) nv).getTypedef() == constr.getParent() && ((AtomValue) nv).getName().equals("nil")){
                    break;
                }
                if(!(nv instanceof ConstructorValue))
                    return oldToString();
                cv = (ConstructorValue) nv;
            }
            sb.append("]");
            return sb.toString();
        }
        return oldToString();
    }

    public Datatype.DatatypeDef.DatatypeConstr getConstr() {
        return constr;
    }
}
