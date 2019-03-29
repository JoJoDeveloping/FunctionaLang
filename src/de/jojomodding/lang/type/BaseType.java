package de.jojomodding.lang.type;

import de.jojomodding.lang.env.ElabEnvironment;

public class BaseType extends Type {

    private String name;
    private boolean eq;


    public static final BaseType INT = new BaseType("int");
    public static final BaseType BOOL = new BaseType("bool");

    private BaseType(String name){
        this.name = name;
        this.eq = true;
    }

    public String toString(){
        return name;
    }

    @Override
    public String deparse(ElabEnvironment env, DeparseHelper dh) {
        return name;
    }
}
