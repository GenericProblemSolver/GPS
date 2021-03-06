grammar GDL;

description :  (gdlRule | sentence)+ ;

gdlRule : '('  '<='  head=sentence ( literal)*  ')';

sentence : property=propLit |   '('  relation=relLit  ')' ;

literal : ( '(' (orLit | notLit | distinctLit | relLit) ')' ) 
        | propLit;
notLit : 'not' literal | '~' literal;
orLit : 'or' ( literal)+ ;
distinctLit : 'distinct'   term   term;
propLit : constant;
relLit : constant (  term)+;

term : ( '(' funcTerm  ')' ) | varTerm | constTerm;
funcTerm : constant (  term)*;
varTerm : '?' constant;
constTerm : constant;


constant : ident | number;
/* ident is any string of letters, digits, and underscores */
ident: ID;

number: NR;

NR : [0-9]+;

ID : [a-zA-Z] [a-zA-Z0-9]* ;

COMMENT : ';'[A-Za-z0-9; \r\t]* -> skip;
WS : [ \t\r\n]+ -> skip;