package nl.han.ica.icss.transforms;

import nl.han.ica.datastructures.HANLinkedList;
import nl.han.ica.datastructures.IHANLinkedList;
import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.*;
import nl.han.ica.icss.ast.operations.AddOperation;
import nl.han.ica.icss.ast.operations.MultiplyOperation;
import nl.han.ica.icss.ast.operations.SubtractOperation;
import nl.han.ica.icss.ast.operations.DivideOperation;

import java.util.ArrayList;
import java.util.HashMap;

public class Evaluator implements Transform {

    private IHANLinkedList<HashMap<String, Literal>> variableValues;

    public Evaluator() {
        variableValues = new HANLinkedList<>();
    }

    // Applies transformations to AST by:
    // - Evaluating variable assignments
    // - Evaluating expressions in declarations
    // - Evaluating if clauses
    // - handling variable scopes
    @Override
    public void apply(AST ast) {
        // Initialize variable values linked list
        variableValues = new HANLinkedList<>();
        // Start with global scope
        variableValues.addFirst(new HashMap<>());
        // Evaluate the stylesheet
        evaluateStylesheet(ast.root);
    }

    // Evaluates all variable assignments and stylerules in the stylesheet
    private void evaluateStylesheet(Stylesheet stylesheet) {
        // Get all children
        ArrayList<ASTNode> children = stylesheet.getChildren();

        // Loop through all children
        for (int i = 0; i < children.size(); i++) {
            ASTNode child = children.get(i);

            if (child instanceof VariableAssignment) {
                evaluateVariableAssignment((VariableAssignment) child);
            } else if (child instanceof Stylerule) {
                evaluateStylerule((Stylerule) child);
            }
        }
    }

    // Evaluates a stylerule and replaces expressions with literals
    private void evaluateStylerule(Stylerule stylerule) {
        // Enter new scope
        variableValues.addFirst(new HashMap<>());

        // Get current children
        ArrayList<ASTNode> children = stylerule.getChildren();
        ArrayList<ASTNode> newChildren = new ArrayList<>();

        // Process each child
        for (ASTNode child : children) {
            if (child instanceof Declaration) {
                // Evaluate the declaration's expression
                Declaration decl = (Declaration) child;
                decl.expression = evaluateExpression(decl.expression);
                newChildren.add(decl);
            } else if (child instanceof VariableAssignment) {
                // Store variable value but don't add to output
                evaluateVariableAssignment((VariableAssignment) child);
            } else if (child instanceof IfClause) {
                // Evaluate if clause and add resulting nodes
                ArrayList<ASTNode> ifResult = evaluateIfClause((IfClause) child);
                newChildren.addAll(ifResult);
            } else {
                newChildren.add(child);
            }
        }

        // Replace children with evaluated children
        stylerule.body = newChildren;

        // Exit scope
        variableValues.removeFirst();
    }

    // Evaluates and stores a variable assignment
    private void evaluateVariableAssignment(VariableAssignment assignment) {
        // Evaluate the expression to a literal
        Literal value = (Literal) evaluateExpression(assignment.expression);
        // Store in current scope
        variableValues.getFirst().put(assignment.name.name, value);
    }

    // Evaluates an if clause and returns the appropriate body content
    private ArrayList<ASTNode> evaluateIfClause(IfClause ifClause) {
        // Evaluate the condition
        Expression condition = evaluateExpression(ifClause.conditionalExpression);

        // Determine if condition is true or false
        boolean conditionValue;
        if (condition instanceof BoolLiteral) {
            conditionValue = ((BoolLiteral) condition).value;
        } else {
            // If not a boolean, treat as false
            conditionValue = false;
        }

        // Enter new scope for if/else body
        variableValues.addFirst(new HashMap<>());

        ArrayList<ASTNode> result = new ArrayList<>();

        if (conditionValue) {
            // Evaluate if body
            for (ASTNode child : ifClause.body) {
                if (child instanceof Declaration) {
                    // if declaration, evaluate expression
                    Declaration decl = (Declaration) child;
                    decl.expression = evaluateExpression(decl.expression);
                    result.add(decl);
                } else if (child instanceof VariableAssignment) {
                    // else if variable assignment, store value
                    evaluateVariableAssignment((VariableAssignment) child);
                } else if (child instanceof IfClause) {
                    // else if nested if clause, evaluate it
                    result.addAll(evaluateIfClause((IfClause) child));
                }
            }
        } else if (ifClause.elseClause != null) {
            // Evaluate else body
            for (ASTNode child : ifClause.elseClause.body) {
                if (child instanceof Declaration) {
                    // if declaration, evaluate expression
                    Declaration decl = (Declaration) child;
                    decl.expression = evaluateExpression(decl.expression);
                    result.add(decl);
                } else if (child instanceof VariableAssignment) {
                    // else if variable assignment, store value
                    evaluateVariableAssignment((VariableAssignment) child);
                } else if (child instanceof IfClause) {
                    // else if nested if clause, evaluate it
                    result.addAll(evaluateIfClause((IfClause) child));
                }
            }
        }
        // If condition is false and no else clause, return empty list

        // Exit scope
        variableValues.removeFirst();

        return result;
    }

    // Evaluates an expression and returns a literal
    private Expression evaluateExpression(Expression expression) {
        // Check if already a literal
        if (expression instanceof Literal) {
            return expression;
        } else if (expression instanceof VariableReference) {
            // else if it's a variable reference, look up its value
            return evaluateVariableReference((VariableReference) expression);
        } else if (expression instanceof Operation) {
            // else if it's an operation, evaluate it
            return evaluateOperation((Operation) expression);
        }

        return expression;
    }

    // Looks up a variable value in the scope chain
    private Literal evaluateVariableReference(VariableReference reference) {
        // Get the variable name
        String varName = reference.name;

        // Search through scopes from innermost to outermost
        for (int i = 0; i < variableValues.getSize(); i++) {
            // Get current scope
            HashMap<String, Literal> scope = variableValues.get(i);
            // If variable found, return its value
            if (scope.containsKey(varName)) {
                return scope.get(varName);
            }
        }

        // Variable not found, return default value
        return new ScalarLiteral(0);
    }

    // Evaluates an operation and returns the result as a literal
    private Literal evaluateOperation(Operation operation) {
        // Evaluate both operands first
        Literal left = (Literal) evaluateExpression(operation.lhs);
        Literal right = (Literal) evaluateExpression(operation.rhs);

        // Check operation type and evaluate
        if (operation instanceof AddOperation) {
            // Evaluate addition
            return evaluateAddition(left, right);
        } else if (operation instanceof SubtractOperation) {
            // Evaluate subtraction
            return evaluateSubtraction(left, right);
        } else if (operation instanceof MultiplyOperation) {
            // Evaluate multiplication
            return evaluateMultiplication(left, right);
        } else if (operation instanceof DivideOperation) {
            // Evaluate division
            return evaluateDivision(left, right);
        }

        return left;
    }

    // Evaluates addition of two literals
    private Literal evaluateAddition(Literal left, Literal right) {
        // Add pixels
        if (left instanceof PixelLiteral && right instanceof PixelLiteral) {
            int result = ((PixelLiteral) left).value + ((PixelLiteral) right).value;
            return new PixelLiteral(result);
        // Add percentages
        } else if (left instanceof PercentageLiteral && right instanceof PercentageLiteral) {
            int result = ((PercentageLiteral) left).value + ((PercentageLiteral) right).value;
            return new PercentageLiteral(result);
        // Add scalars
        } else if (left instanceof ScalarLiteral && right instanceof ScalarLiteral) {
            int result = ((ScalarLiteral) left).value + ((ScalarLiteral) right).value;
            return new ScalarLiteral(result);
        }
        return left;
    }

    // Evaluates subtraction of two literals
    private Literal evaluateSubtraction(Literal left, Literal right) {
        // Subtract pixels
        if (left instanceof PixelLiteral && right instanceof PixelLiteral) {
            int result = ((PixelLiteral) left).value - ((PixelLiteral) right).value;
            return new PixelLiteral(result);
        // Subtract percentages
        } else if (left instanceof PercentageLiteral && right instanceof PercentageLiteral) {
            int result = ((PercentageLiteral) left).value - ((PercentageLiteral) right).value;
            return new PercentageLiteral(result);
        // Subtract scalars
        } else if (left instanceof ScalarLiteral && right instanceof ScalarLiteral) {
            int result = ((ScalarLiteral) left).value - ((ScalarLiteral) right).value;
            return new ScalarLiteral(result);
        }
        return left;
    }

    // Evaluates multiplication of two literals
    private Literal evaluateMultiplication(Literal left, Literal right) {
        // Multiply scalar with pixel
        if (left instanceof ScalarLiteral && right instanceof PixelLiteral) {
            int result = ((ScalarLiteral) left).value * ((PixelLiteral) right).value;
            return new PixelLiteral(result);
        // Multiply pixel with scalar
        } else if (left instanceof PixelLiteral && right instanceof ScalarLiteral) {
            int result = ((PixelLiteral) left).value * ((ScalarLiteral) right).value;
            return new PixelLiteral(result);
        // Multiply scalar with percentage
        } else if (left instanceof ScalarLiteral && right instanceof PercentageLiteral) {
            int result = ((ScalarLiteral) left).value * ((PercentageLiteral) right).value;
            return new PercentageLiteral(result);
        // Multiply percentage with scalar
        } else if (left instanceof PercentageLiteral && right instanceof ScalarLiteral) {
            int result = ((PercentageLiteral) left).value * ((ScalarLiteral) right).value;
            return new PercentageLiteral(result);
        // Multiply scalars
        } else if (left instanceof ScalarLiteral && right instanceof ScalarLiteral) {
            int result = ((ScalarLiteral) left).value * ((ScalarLiteral) right).value;
            return new ScalarLiteral(result);
        }
        return left;
    }

    // Evaluates division of two literals
    private Literal evaluateDivision(Literal left, Literal right) {
        // Divide scalar with pixel
        if (left instanceof ScalarLiteral && right instanceof PixelLiteral) {
            int result = ((ScalarLiteral) left).value / ((PixelLiteral) right).value;
            return new PixelLiteral(result);
        // Divide pixel with scalar
        } else if (left instanceof PixelLiteral && right instanceof ScalarLiteral) {
            int rightValue = ((ScalarLiteral) right).value;
            if (rightValue == 0) {
                return left; // Avoid division by zero // imp!
            }
            int result = ((PixelLiteral) left).value / rightValue;
            return new PixelLiteral(result);
        // Divide scalar with percentage
        } else if (left instanceof ScalarLiteral && right instanceof PercentageLiteral) {
            int result = ((ScalarLiteral) left).value / ((PercentageLiteral) right).value;
            return new PercentageLiteral(result);
        // Divide percentage with scalar
        } else if (left instanceof PercentageLiteral && right instanceof ScalarLiteral) {
            int rightValue = ((ScalarLiteral) right).value;
            if (rightValue == 0) {
                return left; // Avoid division by zero
            }
            int result = ((PercentageLiteral) left).value / rightValue;
            return new PercentageLiteral(result);
        // Divide scalars
        } else if (left instanceof ScalarLiteral && right instanceof ScalarLiteral) {
            int rightValue = ((ScalarLiteral) right).value;
            if (rightValue == 0) {
                return left; // Avoid division by zero
            }
            int result = ((ScalarLiteral) left).value / rightValue;
            return new ScalarLiteral(result);
        }
        return left;
    }
}
