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

//Comments are skipped
LINE_COMMENT: '//' ~[\r\n]* -> skip;
BLOCK_COMMENT: '/*' .*? '*/' -> skip;

//All whitespace is skipped
WS: [ \t\r\n]+ -> skip;

//Operators
OPEN_BRACE: '{';
CLOSE_BRACE: '}';
SEMICOLON: ';';
COLON: ':';
PLUS: '+';
MIN: '-';
MUL: '*';
DIV: '/';
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

// A propertyName is a lower case identifier
// eg: background-color
propertyName: LOWER_IDENT;

// A declaration defines a property (lower case) and assigns an expression as its value, separated by a ':' and ends with a semicolon
// eg: width: 30%
declaration: LOWER_IDENT COLON expression SEMICOLON;

// A variableAssignment starts with a capitalized identifier and assigns an expression as its value, separated by ':=' and ends with a semicolon
// eg: WidthVar := 50px
variableAssignment: CAPITAL_IDENT ASSIGNMENT_OPERATOR expression SEMICOLON;
// Addition and Subtraction, low precedence
expression:
      expression PLUS multiplicativeExpression #AddExpression
      // Example: 10px + 20px
      // addition happens after evaluating the multiplicative expressions.

    | expression MIN multiplicativeExpression #SubtractExpression
      // Example: 30px - 10px
      // subtraction happens after evaluating the multiplicative expressions.

    | multiplicativeExpression #PassthroughExpression
      // Example: 5px
      // directly pass through a multiplicative expression without addition or subtraction allowing these expressions to exist on their own.
     ;

// Multiplication and Division, high precedence
multiplicativeExpression:
      multiplicativeExpression MUL primaryExpression  #MultiplyExpression
      // Example: 10px * 2
      // multiplication is evaluated before addition or subtraction.

    | multiplicativeExpression DIV primaryExpression  #DivideExpression
      // Example: 100px / 2
      // division is evaluated before addition or subtraction.

    | primaryExpression                                #PassthroughMultiplicativeExpression
      // Example: 5px
      // directly pass through a primary expression
     ;


// Primary expressions, highest precedence - literals, variables, parentheses
primaryExpression:
      BOX_BRACKET_OPEN expression BOX_BRACKET_CLOSE #ParenthesizedExpression
      // Example: (10px + 5px) * 2 -> the addition inside parentheses happens first, and the result is multiplied by 2.

    | literal #LiteralExpression
      // Example: 10px, 20, #ff00ff -> literals are simple values evaluated directly.

    | CAPITAL_IDENT #VariableReference
      // Example: WidthVar
      // refers to a variable, which is evaluated directly.
    ;

// A literal is just like a lil guy
// eg: #ff00ff or 20px
literal:
      PIXELSIZE #PixelLiteral
    | PERCENTAGE #PercentageLiteral
    | SCALAR #ScalarLiteral // needs to come after PIXELSIZE and PERCENTAGE, otherwise they'd be passed over every time.
    | COLOR #ColorLiteral
    | boolLiteral #BooleanLiteral;

// A boolean literal can be TRUE or FALSE
boolLiteral: TRUE | FALSE;

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
