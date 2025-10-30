grammar ICSS;

//--- LEXER: ---

// IF support:
IF: 'if';
ELSE: 'else';
BOX_BRACKET_OPEN: '[';
BOX_BRACKET_CLOSE: ']';

//Literals
TRUE: 'TRUE';
FALSE: 'FALSE';
PIXELSIZE: [0-9]+ 'px';
PERCENTAGE: [0-9]+ '%';
SCALAR: [0-9]+;

//Color value takes precedence over id idents
fragment HEX: [0-9a-fA-F];
COLOR: '#' HEX HEX HEX HEX HEX HEX; //UPPERCASE now also supported // prefer to force UC only, but tests would fail.
// fragment HEX: [0-9A-Fa-f]; // like this

//////////////////////////////////
// Strengthened ruleset for IDENTs:
// IDENT's must start with a lower case letter (after the '.' or '#')
// may include digits and have parts separated by hyphens
// Trailing hyphens are forbidden, hyphens themselves must be separated by at least one letter/digit
// eg: .hamburger-menu, .b0x, .a44255325

//Specific identifiers for id's and css classes
fragment IDENT_START: [a-z];
fragment IDENT_PART: [a-z0-9];

ID_IDENT: '#' IDENT_START IDENT_PART* ( '-'+ IDENT_PART+ )*; //edited to ensure the ID starts with a lower case letter and only contains lower case letters, numbers and hyphens
CLASS_IDENT: '.' IDENT_START IDENT_PART* ( '-'+ IDENT_PART+ )*; // dito #0132


//General identifiers
LOWER_IDENT: [a-z] [a-z0-9-]*;
CAPITAL_IDENT: [A-Z] [A-Za-z0-9_]*;

//All whitespace is skipped
WS: [ \t\r\n]+ -> skip;

//
OPEN_BRACE: '{';
CLOSE_BRACE: '}';
SEMICOLON: ';';
COLON: ':';
PLUS: '+';
MIN: '-';
MUL: '*';
ASSIGNMENT_OPERATOR: ':=';

// TODO: Lighter version that allows the LEXER to continue BUT reports the error.


//--- PARSER: ---

// A stylesheet contains one or more variableAssignments and/or stylerules and then ends //
// eg:
//
// h {
// background-color: #ffffff;
// }
//
// .red-box {
// background-color: #ff0000;
// width: 50px;
// }
stylesheet: (variableAssignment | stylerule)* EOF;

// A stylerule applies to a selector (ID, tag or class), and contains one (or more) of any of the following declarations, VariableAssignmens or ifClauses
// eg:
// .blue-box {
// background-color: #0000ff;
// width: 50px;
// }
stylerule: selector OPEN_BRACE (declaration | variableAssignment | ifClause)* CLOSE_BRACE; // ASSIGNMENT 192

// A selector can be an ID, tag or class. an alternative (different, ANTLR dislikes the reusal of the same names, no matter case) '#' name for use outside of this file.
// eg:
selector: tagSelector #TagSelectr | idSelector #IdSelectr | classSelector #ClassSelectr;

// A tagSelector identifies lowercase expression
// eg: div
tagSelector: LOWER_IDENT;

// An idSelector selects valid ID's
// eg: #banana-bread
idSelector: ID_IDENT;

// A classSelector selects valid classes
// eg: .pumpkin-pie-12
classSelector: CLASS_IDENT;

// A declaration defines a property (lower case) and assigns an expression as its value, separated by a ':' and ends with a semicolon
// eg: width: 30%
declaration: LOWER_IDENT COLON expression SEMICOLON;

// A variableAssignment starts with a capitalized identifier and assigns an expression as its value, separated by ':=' and ends with a semicolon
// eg: WidthVar := 50px
variableAssignment: CAPITAL_IDENT ASSIGNMENT_OPERATOR expression SEMICOLON;

// an expression consists of one or more multiplicativeExpressions that it can add or subtract from one another
// eg: 20px * 3 - 20px * 2
expression: multiplicativeExpression ( (PLUS | MIN) multiplicativeExpression )* #AdditiveExpression;

//  A multiplicativeExpression consists of one or more primaryExpressions that it can multiply with one another
// eg 3px * 4
multiplicativeExpression: primaryExpression ( MUL primaryExpression )* #MultiplicativeExpr; // ANTLR dislikes the reusal of the samen names, no matter case

// A primaryExpression can be:
// A literal
// eg: 20px
// A reference to a variable
// eg: WidthVar
// An expression within parentheses
// eg: [20px + 3px]
primaryExpression: literal #LiteralExpression| CAPITAL_IDENT #VariableReferenceExpression| BOX_BRACKET_OPEN expression BOX_BRACKET_CLOSE #ParenthesizedExpression;

// A literal is just like a lil guy
// eg: #ff00ff or 20px
literal:
      PIXELSIZE #PixelLiteral
    | PERCENTAGE #PercentageLiteral
    | SCALAR #ScalarLiteral // needs to come after PIXELSIZE and PERCENTAGE, otherwise they'd be passed over every time.
    | COLOR #ColorLiteral
    | TRUE #BoolLiteralTrue
    | FALSE #BoolLiteralFalse;

// An ifClause starts with an if [expression] followed by a block surrounded with curly braces, that block can contain declarations, variableAssignments or even a nested ifClause. at the end of the block it may contain an else clause.
// eg:
// if[AdjustColor] {
//	    color: #124532;
//	    if[UseLinkColor]{
//	        background-color: LinkColor;
//	    } else {
//	        background-color: #000000;
//	    }
//	}
ifClause: IF BOX_BRACKET_OPEN expression BOX_BRACKET_CLOSE OPEN_BRACE (declaration | variableAssignment | ifClause)* CLOSE_BRACE (elseClause)?;

// An elseClause opens with élse' followed by a block surrounded with curly braces, that block can contain declarations, variableAssignments or even a nested ifClause
elseClause: ELSE OPEN_BRACE (declaration | variableAssignment | ifClause)* CLOSE_BRACE;
