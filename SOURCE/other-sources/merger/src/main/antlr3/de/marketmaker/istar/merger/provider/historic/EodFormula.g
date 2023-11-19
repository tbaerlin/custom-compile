grammar EodFormula;

options {
	output=AST;
	// ANTLR can handle literally any tree node type.
	// For convenience, specify the Java type
	ASTLabelType=CommonTree; // type of $stat.tree ref etc...
}

@lexer::header {
package de.marketmaker.istar.merger.provider.historic;
}
@parser::header {
package de.marketmaker.istar.merger.provider.historic;
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

// grammar
formula :    func
	    ;
func	:	 FN^ '('! args ')'!
	    ;
args	:	NUM
	    |	func (','! func)*
	    ;
FN	    :	'A'..'Z'+
	    ;
NUM	    :	'0'..'9'+
	    ;
WS	    :	(' '|'\t'|'\r'|'\n')+ {skip();}
	    ;