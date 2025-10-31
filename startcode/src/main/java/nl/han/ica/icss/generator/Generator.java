package nl.han.ica.icss.generator;

import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.*;
import nl.han.ica.icss.ast.selectors.ClassSelector;
import nl.han.ica.icss.ast.selectors.IdSelector;
import nl.han.ica.icss.ast.selectors.TagSelector;

public class Generator {

	// Generates output CSS string from the AST
	public String generate(AST ast) {
		// Create string builder for CSS output
		StringBuilder sb = new StringBuilder();
		// Generate the stylesheet
		generateStylesheet(ast.root, sb);
		// Returns the CSS as string
		return sb.toString();
	}

	// Generates CSS for all stylerules in the stylesheet
	// Loops through all children of the stylesheet and generates CSS for each stylerule
	private void generateStylesheet(Stylesheet stylesheet, StringBuilder sb) {
		// Loop through all children
		for (ASTNode child : stylesheet.getChildren()) {
			// Ignore non stylerules
			if (child instanceof Stylerule) {
				// Generate CSS for the stylerule
				generateStylerule((Stylerule) child, sb);
			}
		}
	}

	// Generates CSS for a single stylerule
	// Loops through children to find selector and declarations
	private void generateStylerule(Stylerule stylerule, StringBuilder sb) {
		// Generate selector first
		for (ASTNode child : stylerule.getChildren()) {
			// Find the child node that is a selector
			if (child instanceof Selector) {
				// Generate the selector text (tag, id, or class)
				generateSelector((Selector) child, sb);
				break;
			}
		}

		// Add opening brace
		sb.append(" {\n");

		// Generate all declarations
		for (ASTNode child : stylerule.getChildren()) {
			// Find the child nodes that are declarations
			if (child instanceof Declaration) {
				// Generate the declaration text
				generateDeclaration((Declaration) child, sb, 1);
			}
		}

		// Add closing brace and newlines
		sb.append("}\n\n");
	}

	// Generates the selector text (tag, id, or class)
	// Loops through possible selector types and appends the right string
	private void generateSelector(Selector selector, StringBuilder sb) {
		// Check what type of selector and append the text
		if (selector instanceof TagSelector) {
			// Tag selector (eg. div, p)
			sb.append(((TagSelector) selector).tag);
		} else if (selector instanceof IdSelector) {
			// ID selector (eg. #header)
			sb.append(((IdSelector) selector).id);
		} else if (selector instanceof ClassSelector) {
			// Class selector (eg. .container)
			sb.append(((ClassSelector) selector).cls);
		}
	}

	// Generates a CSS declaration with proper indentation
	// Loops through indentation levels and appends property and value
	private void generateDeclaration(Declaration declaration, StringBuilder sb, int indentLevel) {
		// Add indentation (2 spaces per level)
		for (int i = 0; i < indentLevel; i++) {
			sb.append("  ");
		}

		// Add property name, followed by a colon and space
		sb.append(declaration.property.name);
		sb.append(": ");
		// Add value from expression
		generateExpression(declaration.expression, sb);
		// Add semicolon and newline
		sb.append(";\n");
	}

	// Generates the CSS value from an expression (should be a literal after transformation)
	// Checks the type of literal and appends the correct format
	private void generateExpression(Expression expression, StringBuilder sb) {
		// Check type and format accordingly
		if (expression instanceof PixelLiteral) {
			// Pixel value (eg. 10px)
			sb.append(((PixelLiteral) expression).value).append("px");
		} else if (expression instanceof PercentageLiteral) {
			// Percentage value (eg. 50%)
			sb.append(((PercentageLiteral) expression).value).append("%");
		} else if (expression instanceof ColorLiteral) {
			// Color value (eg. #FF0000)
			sb.append(((ColorLiteral) expression).value);
		} else if (expression instanceof ScalarLiteral) {
			// Scalar value (eg. 42)
			sb.append(((ScalarLiteral) expression).value);
		} else if (expression instanceof BoolLiteral) {
			// Boolean value
			sb.append(((BoolLiteral) expression).value ? "TRUE" : "FALSE");
		}
	}
}
