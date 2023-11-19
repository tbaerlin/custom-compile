grammar MarketManagerQuery;

options {
	output=AST;
	// ANTLR can handle literally any tree node type.
	// For convenience, specify the Java type
	ASTLabelType=CommonTree; // type of $stat.tree ref etc...
}

@lexer::header {
package de.marketmaker.istar.merger.provider.news;
}
@parser::header {
package de.marketmaker.istar.merger.provider.news;
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

query 	: expr 
	;

expr	: orCondition
	;

orCondition
	:	and_condition  ( OR_OP^ and_condition)*
	;
	
and_condition
	:	term ( AND_OP^ term)*
	;
	
term
	:	keywordExpr
	|	categoryExpr
	|	referenceExpr
	|	languageExpr
	|	providerExpr
	|       parExpr
	|	notExpr
	;
	
parExpr	:	OPEN_PAREN! expr CLOSE_PAREN!
	;

notExpr	:	NOT_OP^ OPEN_PAREN! expr CLOSE_PAREN!
	;

keywordExpr
	:	'keyword' EQ id		-> ^('keyword' id) 
	|	'keyword' EQ ALL 	-> ^('keyword' ALL)	
	|	'keyword' EQ PHRASE 	-> ^('keyword' PHRASE)	
	;
		
referenceExpr
	:	'reference' EQ IDENTIFIER	-> ^('reference' IDENTIFIER)
	;
	
providerExpr
	:	'provider' EQ id	-> ^('provider' id)
	;
	
languageExpr
	:	'language' EQ id	-> ^('language' id)
	;
	
categoryExpr
	:	'category' EQ IDENTIFIER	-> ^('category' IDENTIFIER)
	;
	
id 	:	IDENTIFIER ( ( '&' | '\'' | '-' | '+' | '.' | ',' | '/' ) IDENTIFIER )*
	;
	
IDENTIFIER     : 
        ('a' .. 'z' | 'A'..'Z' | '0'..'9' | '\u00C0' .. '\u00FF' | '@' | '_' | '%' | '$' )+
    ;
		
EQ	:	'=';	

AND_OP	: 	'&';
OR_OP	: 	'|';
NOT_OP	: 	'!';

ALL	:	'*';

PHRASE  : '\"' ( ~('\"') )* '\"';
	
OPEN_PAREN 	: '(' ;
CLOSE_PAREN 	: ')' ;

WS : (' '|'\t'|'\n'|'\r')+ {skip();} ;
