grammar FinderQuery;

options {
    output=AST;
    // ANTLR can handle literally any tree node type.
    // For convenience, specify the Java type
    ASTLabelType=CommonTree; // type of $stat.tree ref etc...
}

tokens {
    AND;
    IN;
    NOT;
    OR;
    IS_NULL;
    IS_NOT_NULL;
}

@lexer::header {
package de.marketmaker.istar.merger.web.finder;
}
@lexer::members {
    public void emitErrorMessage(String msg) {
        throw new RuntimeException(msg);
    }
}
@parser::header {
package de.marketmaker.istar.merger.web.finder;
}
@members {
protected void mismatch(IntStream input, int ttype, BitSet follow)
throws RecognitionException
{
throw new MismatchedTokenException(ttype, input);
}
public Object recoverFromMismatchedSet(IntStream input,
RecognitionException e,
BitSet follow)
throws RecognitionException
{
throw e;
}
}
// Alter code generation so catch-clauses get replace with
// this action.
@rulecatch {
catch (RecognitionException e) {
throw e;
}
}

query
  : expr
  ;

expr
  : orCondition
  ;

orCondition
  :  andCondition  ( OR_OP andCondition)* -> ^(OR andCondition+)
  ;

andCondition
  :  term ( AND_OP term)* -> ^(AND term+)
  ;

term
  :  relExpr
  |  inExpr
  |  isExpr
  |   parExpr
  |  notExpr
  ;

parExpr  
  :  OPEN_PAREN! expr CLOSE_PAREN!
  ;

notExpr  
  :  ('!'  | NOT_EXPR) OPEN_PAREN expr CLOSE_PAREN -> ^(NOT expr)
  ;

relExpr  
  :  FIELDNAME  REL^ ( DECIMAL | QUOTED )
  ;

inExpr
  :  FIELDNAME IN_OP setExpr -> ^(IN FIELDNAME setExpr)
  ;

isExpr
  :  FIELDNAME IS_OP (NOT_EXPR NULL -> ^(IS_NOT_NULL FIELDNAME)
                       |        NULL -> ^(IS_NULL FIELDNAME)
                      )
  ;

setExpr
  :   OPEN_PAREN! commaSeparatedValues? CLOSE_PAREN!
  ;

commaSeparatedValues
  :  QUOTED (','! QUOTED)*
  |  DECIMAL (','! DECIMAL)*
  ;

REL
  :  ('<' | '<=' | '>' | '>=' | '=' | '==' | '=~' | '!=')
  ;

DECIMAL 
  : ('-' | '+')? ('0'..'9')+ ('.' ('0'..'9')*)?
  ;

QUOTED
  : '\'' ( '\'\'' | ~('\'') )* '\''
  ;

NOT_EXPR :   N O T;
AND_OP   :   '&&' | A N D;
OR_OP    :   '||' | O R;
IN_OP    :  I N;
IS_OP    :  I S;
NULL     :   N U L L;

OPEN_PAREN    : '(' ;
CLOSE_PAREN   : ')' ;

FIELDNAME
  : ('a' .. 'z' | 'A'..'Z') ('a' .. 'z' | 'A'..'Z' | '0'..'9' | '\u00C4' | '\u00D6' | '\u00DC' | '\u00DF' | '\u00E4' | '\u00F6' | '\u00FC' | '@' | '_')*
  ;

fragment A:('a'|'A');
fragment B:('b'|'B');
fragment C:('c'|'C');
fragment D:('d'|'D');
fragment E:('e'|'E');
fragment F:('f'|'F');
fragment G:('g'|'G');
fragment H:('h'|'H');
fragment I:('i'|'I');
fragment J:('j'|'J');
fragment K:('k'|'K');
fragment L:('l'|'L');
fragment M:('m'|'M');
fragment N:('n'|'N');
fragment O:('o'|'O');
fragment P:('p'|'P');
fragment Q:('q'|'Q');
fragment R:('r'|'R');
fragment S:('s'|'S');
fragment T:('t'|'T');
fragment U:('u'|'U');
fragment V:('v'|'V');
fragment W:('w'|'W');
fragment X:('x'|'X');
fragment Y:('y'|'Y');
fragment Z:('z'|'Z');

WS : (' '|'\t'|'\n'|'\r')+ {skip();} ;
