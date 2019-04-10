# FunctionaLang
A functional language that is mostly inspired by SML, although there are a few differences

## Differences like
- Patterns (especially as-patterns) work slightly different
- Equality is `==`
- Integers have no upper/lower bound (BigInteger)
- Unary `-` is a minus, too. Note that this does not affect `op-`
- No datatype definitions in let-in expressions, so we don't open that can of worms.

## Example program:
Courtesy of [Rosetta code's SML Y combinator](https://rosettacode.org/wiki/Y_combinator#Standard_ML)
```
datatype 'a mu = Roll of ('a mu -> 'a)
fun unroll (Roll x) = x;
(fn f => (fn x => fn a => f (unroll x x) a) (Roll (fn x => fn a => f (unroll x x) a)))  (fn f => fn 0=>0|x => x+f(x-1));
it 10;
```
### Output
```
Roll = Roll : ∀ 'a: ('a mu->'a)->'a mu
unroll = fn : ∀ 'a: 'a mu->'a mu->'a
it = fn : int->int
it = 55 : int
```
