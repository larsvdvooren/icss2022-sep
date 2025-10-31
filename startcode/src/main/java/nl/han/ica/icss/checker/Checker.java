package nl.han.ica.icss.checker;

import nl.han.ica.datastructures.HANLinkedList;
import nl.han.ica.datastructures.IHANLinkedList;
import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.*;
import nl.han.ica.icss.ast.operations.AddOperation;
import nl.han.ica.icss.ast.operations.MultiplyOperation;
import nl.han.ica.icss.ast.operations.SubtractOperation;
import nl.han.ica.icss.ast.types.ExpressionType;
import nl.han.ica.icss.ast.operations.DivideOperation;

import java.util.HashMap;

public class Checker {

    private IHANLinkedList<HashMap<String, ExpressionType>> variableTypes;

    // Checks the AST for semantic errors
    public void check(AST ast) {
        // Initialize variable types linked list
        variableTypes = new HANLinkedList<>();
        // Start with global scope
        variableTypes.addFirst(new HashMap<>());
        // Check the stylesheet
        checkStylesheet(ast.root);
    }

    // Checks all variable assignments and stylerules in the stylesheet
    private void checkStylesheet(Stylesheet stylesheet) {
        // Loop through all children
        for (ASTNode child : stylesheet.getChildren()) {
            if (child instanceof VariableAssignment) {
                checkVariableAssignment((VariableAssignment) child);
            } else if (child instanceof Stylerule) {
                checkStylerule((Stylerule) child);
            }
        }
    }

    // Checks a stylerule and its contents
    private void checkStylerule(Stylerule stylerule) {
        // Enter new scope for stylerule
        variableTypes.addFirst(new HashMap<>());

        // Check all children in the stylerule
        for (ASTNode child : stylerule.getChildren()) {
            if (child instanceof Declaration) {
                checkDeclaration((Declaration) child);
            } else if (child instanceof VariableAssignment) {
                checkVariableAssignment((VariableAssignment) child);
            } else if (child instanceof IfClause) {
                checkIfClause((IfClause) child);
            }
        }

        // Exit scope
        variableTypes.removeFirst();
    }

    // Checks if a declaration has the correct property-value type match
    private void checkDeclaration(Declaration declaration) {
        // Get the type of the value
        ExpressionType valueType = getExpressionType(declaration.expression);

        // Get the property name
        String propertyName = declaration.property.name;
        // Check if color properties have color values
        if (propertyName.equals("color") || propertyName.equals("background-color")) {
            if (valueType != ExpressionType.COLOR) {
                declaration.setError("Property " + propertyName + " requires a color value");
            }
        // Check if size properties have pixel or percentage values
        } else if (propertyName.equals("width") || propertyName.equals("height")) {
            if (valueType != ExpressionType.PIXEL && valueType != ExpressionType.PERCENTAGE) {
                declaration.setError("Property " + propertyName + " requires a pixel or percentage value");
            }
        }
    }

    // Checks and stores a variable assignment
    private void checkVariableAssignment(VariableAssignment assignment) {
        // Get the type of the expression
        ExpressionType type = getExpressionType(assignment.expression);
        // Store variable type in current scope
        variableTypes.getFirst().put(assignment.name.name, type);
    }

    // Checks an if clause and its body
    private void checkIfClause(IfClause ifClause) {
        // Check if condition is boolean
        ExpressionType conditionType = getExpressionType(ifClause.conditionalExpression);
        if (conditionType != ExpressionType.BOOL) {
            ifClause.setError("If condition must be a boolean value");
        }

        // Enter new scope for if body
        variableTypes.addFirst(new HashMap<>());

        // Checks all children in the if body
        for (ASTNode child : ifClause.body) {
            if (child instanceof Declaration) {
                checkDeclaration((Declaration) child);
            } else if (child instanceof VariableAssignment) {
                checkVariableAssignment((VariableAssignment) child);
            } else if (child instanceof IfClause) {
                checkIfClause((IfClause) child);
            }
        }

        // Exit if scope
        variableTypes.removeFirst();

        // Check else clause if present
        if (ifClause.elseClause != null) {
            checkElseClause(ifClause.elseClause);
        }
    }

    // Checks an else clause and its body
    private void checkElseClause(ElseClause elseClause) {
        // Enter new scope for else body
        variableTypes.addFirst(new HashMap<>());

        // Check all children in the else body
        for (ASTNode child : elseClause.body) {
            // Checks for declaration
            if (child instanceof Declaration) {
                checkDeclaration((Declaration) child);
            // Checks for variable assignment
            } else if (child instanceof VariableAssignment) {
                checkVariableAssignment((VariableAssignment) child);
            // Checks for if clause
            } else if (child instanceof IfClause) {
                checkIfClause((IfClause) child);
            }
        }

        // Exit else scope
        variableTypes.removeFirst();
    }

    // Returns the type of an expression
    private ExpressionType getExpressionType(Expression expression) {
        // Check what type of expression and return the appropriate type
        if (expression instanceof PixelLiteral) {
            return ExpressionType.PIXEL;
        } else if (expression instanceof PercentageLiteral) {
            return ExpressionType.PERCENTAGE;
        } else if (expression instanceof ColorLiteral) {
            return ExpressionType.COLOR;
        } else if (expression instanceof ScalarLiteral) {
            return ExpressionType.SCALAR;
        } else if (expression instanceof BoolLiteral) {
            return ExpressionType.BOOL;
        } else if (expression instanceof VariableReference) {
            return checkVariableReference((VariableReference) expression);
        } else if (expression instanceof Operation) {
            return checkOperation((Operation) expression);
        }

        return ExpressionType.UNDEFINED;
    }

    // Checks if a variable is defined and returns its type
    private ExpressionType checkVariableReference(VariableReference reference) {
        // Get the variable name
        String varName = reference.name;

        // Search through scopes from innermost to outermost
        for (int i = 0; i < variableTypes.getSize(); i++) {
            HashMap<String, ExpressionType> scope = variableTypes.get(i);
            if (scope.containsKey(varName)) {
                return scope.get(varName);
            }
        }

        // Variable not found in any scope
        reference.setError("Variable " + varName + " is not defined");
        return ExpressionType.UNDEFINED;
    }

    // Checks operations for type compatibility
    private ExpressionType checkOperation(Operation operation) {
        // Get types of left and right operands
        ExpressionType leftType = getExpressionType(operation.lhs);
        ExpressionType rightType = getExpressionType(operation.rhs);

        // Check addition and subtraction operations
        if (operation instanceof AddOperation || operation instanceof SubtractOperation) {
            // Check if operands are of same type for + and -
            if (leftType != rightType) {
                operation.setError("Operands of addition/subtraction must be of the same type");
                return ExpressionType.UNDEFINED;
            }

            // Check if colors are used in operations
            if (leftType == ExpressionType.COLOR) {
                operation.setError("Colors cannot be used in operations");
                return ExpressionType.UNDEFINED;
            }

            return leftType;

        // Check multiplication operations
        } else if (operation instanceof MultiplyOperation) {
            // Check if at least one operand is scalar for multiplication
            if (leftType != ExpressionType.SCALAR && rightType != ExpressionType.SCALAR) {
                operation.setError("At least one operand of multiplication must be a scalar");
                return ExpressionType.UNDEFINED;
            }

            // Check if colors are used in operations
            if (leftType == ExpressionType.COLOR || rightType == ExpressionType.COLOR) {
                operation.setError("Colors cannot be used in operations");
                return ExpressionType.UNDEFINED;
            }

            // Return non-scalar type, or scalar if both are scalar
            if (leftType == ExpressionType.SCALAR) {
                return rightType;
            } else {
                return leftType;
            }

        // Check division operations
        } else if (operation instanceof DivideOperation) {
            // Check if at least one operand is scalar for division
            if (leftType != ExpressionType.SCALAR && rightType != ExpressionType.SCALAR) {
                operation.setError("At least one operand of division must be a scalar");
                return ExpressionType.UNDEFINED;
            }

            // Check if colors are used in operations
            if (leftType == ExpressionType.COLOR || rightType == ExpressionType.COLOR) {
                operation.setError("Colors cannot be used in operations");
                return ExpressionType.UNDEFINED;
            }

            // Return non-scalar type, or scalar if both are scalar
            if (leftType == ExpressionType.SCALAR) {
                return rightType;
            } else {
                return leftType;
            }
        }
        return ExpressionType.UNDEFINED;
    }
}
