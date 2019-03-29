package de.jojomodding.lang.value;

import de.jojomodding.lang.type.FunctionType;
import de.jojomodding.lang.type.Type;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class TupleValue extends Value {

    private List<Value> values;

    public TupleValue(Value... types){
        this.values = Arrays.asList(types);
    }

    @Override
    public String toString() {
        if(values.size() == 0){
            return "()";
        }else if(values.size() == 1){
            throw new RuntimeException("Tuple with size 1");
        }else{
            return values.stream().map(Object::toString).collect(Collectors.joining(", ", "(", ")"));
        }
    }

    public List<Value> entries(){
        return values;
    }

}
