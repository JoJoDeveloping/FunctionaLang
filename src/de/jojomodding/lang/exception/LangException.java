package de.jojomodding.lang.exception;

import de.jojomodding.lang.parsing.CodePosition;

public abstract class LangException extends Exception{

    public abstract CodePosition position();

    public abstract String format();

}
