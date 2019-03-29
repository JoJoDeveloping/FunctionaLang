package de.jojomodding.lang.value;

import de.jojomodding.lang.env.ElabEnvironment;
import de.jojomodding.lang.type.BaseType;
import de.jojomodding.lang.type.Type;

import java.math.BigInteger;

public abstract class ConstantValue extends Value{

    public abstract Type getType(ElabEnvironment env);

    @Override
    public abstract boolean equals(Object o);

    public static class IntegerValue extends ConstantValue{

        private BigInteger value;

        public IntegerValue(int i){
            this.value = BigInteger.valueOf(i);
        }

        public IntegerValue(long l){
            this.value = BigInteger.valueOf(l);
        }

        public IntegerValue(BigInteger i){
            this.value = i;
        }

        @Override
        public Type getType(ElabEnvironment env) {
            return BaseType.INT;
        }

        public BigInteger getValue(){
            return value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            IntegerValue that = (IntegerValue) o;
            return value.equals(that.value);
        }

        @Override
        public String toString() {
            return value.toString();
        }
    }

    public static class BooleanValue extends ConstantValue{

        private boolean value;

        public BooleanValue(boolean b){
            this.value = b;
        }

        @Override
        public Type getType(ElabEnvironment env) {
            return BaseType.BOOL;
        }

        public boolean getValue(){
            return value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            BooleanValue that = (BooleanValue) o;
            return value == that.value;
        }

        @Override
        public String toString() {
            return value?"true":"false";
        }
    }

}
