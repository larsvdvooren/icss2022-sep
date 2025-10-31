package nl.han.ica.icss.parser;

import nl.han.ica.datastructures.HANStack;
import nl.han.ica.datastructures.IHANStack;
import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.*;
import nl.han.ica.icss.ast.operations.AddOperation;
import nl.han.ica.icss.ast.operations.MultiplyOperation;
import nl.han.ica.icss.ast.operations.SubtractOperation;
import nl.han.ica.icss.ast.selectors.ClassSelector;
import nl.han.ica.icss.ast.selectors.IdSelector;
import nl.han.ica.icss.ast.selectors.TagSelector;
import nl.han.ica.icss.ast.operations.DivideOperation;

/**
 * This class extracts the ICSS Abstract Syntax Tree from the Antlr Parse tree.
 */
public class ASTListener extends ICSSBaseListener {

    //Accumulator attributes:
    private final AST ast;

    //Use this to keep track of the parent nodes when recursively traversing the ast
    private final IHANStack<ASTNode> currentContainer;

    public ASTListener() {
        ast = new AST();
        currentContainer = new HANStack<>();
    }

    public AST getAST() {
        return ast;
    }

    // Stylesheet //
    @Override
    public void enterStylesheet(ICSSParser.StylesheetContext ctx) {
        Stylesheet stylesheet = new Stylesheet();
        currentContainer.push(stylesheet);
    }

    @Override
    public void exitStylesheet(ICSSParser.StylesheetContext ctx) {
        Stylesheet stylesheet = (Stylesheet) currentContainer.pop();
        ast.setRoot(stylesheet);
    }

    // Stylerule //
    @Override
    public void enterStylerule(ICSSParser.StyleruleContext ctx) {
        currentContainer.push(new Stylerule());
    }

    @Override
    public void exitStylerule(ICSSParser.StyleruleContext ctx) {
        Stylerule stylerule = (Stylerule) currentContainer.pop();
        currentContainer.peek().addChild(stylerule);
    }

    // Declaration //
    @Override
    public void enterDeclaration(ICSSParser.DeclarationContext ctx) {
        Declaration declaration = new Declaration();
        declaration.property = new PropertyName(ctx.LOWER_IDENT().getText());
        currentContainer.push(declaration);
    }

    @Override
    public void exitDeclaration(ICSSParser.DeclarationContext ctx) {
        Declaration declaration = (Declaration) currentContainer.pop();
        currentContainer.peek().addChild(declaration);
    }

    // Selectrs //
    @Override
    public void enterClassSelectr(ICSSParser.ClassSelectrContext ctx) {
        ClassSelector classSelector = new ClassSelector(ctx.getText());
        currentContainer.push(classSelector);
    }

    @Override
    public void exitClassSelectr(ICSSParser.ClassSelectrContext ctx) {
        ClassSelector classSelector = (ClassSelector) currentContainer.pop();
        currentContainer.peek().addChild(classSelector);
    }

    @Override
    public void enterIdSelectr(ICSSParser.IdSelectrContext ctx) {
        IdSelector idSelector = new IdSelector(ctx.getText());
        currentContainer.push(idSelector);
    }

    @Override
    public void exitIdSelectr(ICSSParser.IdSelectrContext ctx) {
        IdSelector idSelector = (IdSelector) currentContainer.pop();
        currentContainer.peek().addChild(idSelector);
    }

    @Override
    public void enterTagSelectr(ICSSParser.TagSelectrContext ctx) {
        TagSelector tagSelector = new TagSelector(ctx.getText());
        currentContainer.push(tagSelector);
    }

    @Override
    public void exitTagSelectr(ICSSParser.TagSelectrContext ctx) {
        TagSelector tagSelector = (TagSelector) currentContainer.pop();
        currentContainer.peek().addChild(tagSelector);
    }

    // Literals // Start ////////////////////////////
    @Override
    public void enterPixelLiteral(ICSSParser.PixelLiteralContext ctx) {
        PixelLiteral pixelLiteral = new PixelLiteral(Integer.parseInt(ctx.getText().replace("px", "")));
        currentContainer.push(pixelLiteral);
    }

    @Override
    public void exitPixelLiteral(ICSSParser.PixelLiteralContext ctx) {
        PixelLiteral pixelLiteral = (PixelLiteral) currentContainer.pop();
        currentContainer.peek().addChild(pixelLiteral);
    }

    @Override
    public void enterPercentageLiteral(ICSSParser.PercentageLiteralContext ctx) {
        PercentageLiteral percentageLiteral = new PercentageLiteral(Integer.parseInt(ctx.getText().replace("%", "")));
        currentContainer.push(percentageLiteral);
    }

    @Override
    public void exitPercentageLiteral(ICSSParser.PercentageLiteralContext ctx) {
        PercentageLiteral percentageLiteral = (PercentageLiteral) currentContainer.pop();
        currentContainer.peek().addChild(percentageLiteral);
    }

    @Override
    public void enterColorLiteral(ICSSParser.ColorLiteralContext ctx) {
        ColorLiteral colorLiteral = new ColorLiteral(ctx.getText());
        currentContainer.push(colorLiteral);
    }

    @Override
    public void exitColorLiteral(ICSSParser.ColorLiteralContext ctx) {
        ColorLiteral colorLiteral = (ColorLiteral) currentContainer.pop();
        currentContainer.peek().addChild(colorLiteral);
    }

    @Override
    public void enterScalarLiteral(ICSSParser.ScalarLiteralContext ctx) {
        ScalarLiteral scalarLiteral = new ScalarLiteral(Integer.parseInt(ctx.getText()));
        currentContainer.push(scalarLiteral);
    }

    @Override
    public void exitScalarLiteral(ICSSParser.ScalarLiteralContext ctx) {
        ScalarLiteral scalarLiteral = (ScalarLiteral) currentContainer.pop();
        currentContainer.peek().addChild(scalarLiteral);
    }
    // Literals // End //////////////////////////////

    @Override
    public void enterBooleanLiteral(ICSSParser.BooleanLiteralContext ctx) {
        BoolLiteral boolLiteral = new BoolLiteral(ctx.getText().equals("TRUE"));
        currentContainer.push(boolLiteral);
    }

    @Override
    public void exitBooleanLiteral(ICSSParser.BooleanLiteralContext ctx) {
        BoolLiteral boolLiteral = (BoolLiteral) currentContainer.pop();
        currentContainer.peek().addChild(boolLiteral);
    }

    // Properties // Start ////////////////////////////
    @Override
    public void enterPropertyName(ICSSParser.PropertyNameContext ctx) {
        PropertyName propertyName = new PropertyName(ctx.getText());
        currentContainer.push(propertyName);
    }

    @Override
    public void exitPropertyName(ICSSParser.PropertyNameContext ctx) {
        PropertyName propertyName = (PropertyName) currentContainer.pop();
        currentContainer.peek().addChild(propertyName);
    }
    // Properties // End //////////////////////////////

    // Variables // Start ////////////////////////////
    @Override
    public void enterVariableReference(ICSSParser.VariableReferenceContext ctx) {
        VariableReference variableReference = new VariableReference(ctx.getText());
        currentContainer.push(variableReference);
    }

	@Override
	public void exitVariableReference(ICSSParser.VariableReferenceContext ctx) {
		VariableReference variableReference = (VariableReference) currentContainer.pop();
		currentContainer.peek().addChild(variableReference);
	}

	@Override
	public void enterVariableAssignment(ICSSParser.VariableAssignmentContext ctx) {
		VariableAssignment variableAssignment = new VariableAssignment();
		variableAssignment.name = new VariableReference(ctx.CAPITAL_IDENT().getText());
		currentContainer.push(variableAssignment);
	}

	@Override
	public void exitVariableAssignment(ICSSParser.VariableAssignmentContext ctx) {
		VariableAssignment variableAssignment = (VariableAssignment) currentContainer.pop();
		currentContainer.peek().addChild(variableAssignment);
	}
    // Variables // End //////////////////////////////

	// if else // Start //////////////////////////////
	@Override
	public void enterIfClause(ICSSParser.IfClauseContext ctx) {
		currentContainer.push(new IfClause());
	}

	@Override
	public void exitIfClause(ICSSParser.IfClauseContext ctx) {
		IfClause ifClause = (IfClause) currentContainer.pop();
		currentContainer.peek().addChild(ifClause);
	}

	@Override
	public void enterElseClause(ICSSParser.ElseClauseContext ctx) {
		currentContainer.push(new ElseClause());
	}

	@Override
	public void exitElseClause(ICSSParser.ElseClauseContext ctx) {
		ElseClause elseClause = (ElseClause) currentContainer.pop();
		currentContainer.peek().addChild(elseClause);
	}
    // if else // End ////////////////////////////////

	// Expressions //
	// Add Start +++++++++++++++++++++++++++++++++++++++++/
	@Override
	public void enterAddExpression(ICSSParser.AddExpressionContext ctx) {
		currentContainer.push(new AddOperation());
	}

	@Override
	public void exitAddExpression(ICSSParser.AddExpressionContext ctx) {
		Operation operation = (Operation) currentContainer.pop();
		currentContainer.peek().addChild(operation);
	}
    // Add End +++++++++++++++++++++++++++++++++++++++++++/

	// Subtract Start ------------------------------------------/
	@Override
	public void enterSubtractExpression(ICSSParser.SubtractExpressionContext ctx) {
		currentContainer.push(new SubtractOperation());
	}

	@Override
	public void exitSubtractExpression(ICSSParser.SubtractExpressionContext ctx) {
		Operation operation = (Operation) currentContainer.pop();
		currentContainer.peek().addChild(operation);
	}
    // Subtract End --------------------------------------------/

	// Multiply Start ******************************************/
	@Override
	public void enterMultiplyExpression(ICSSParser.MultiplyExpressionContext ctx) {
		currentContainer.push(new MultiplyOperation());
	}

	@Override
	public void exitMultiplyExpression(ICSSParser.MultiplyExpressionContext ctx) {
		Operation operation = (Operation) currentContainer.pop();
		currentContainer.peek().addChild(operation);
	}
    // Multiply End ******************************************/

	// Divide Start ///////////////////////////////////////////
	@Override
	public void enterDivideExpression(ICSSParser.DivideExpressionContext ctx) {
		currentContainer.push(new DivideOperation());
	}

	@Override
	public void exitDivideExpression(ICSSParser.DivideExpressionContext ctx) {
		Operation operation = (Operation) currentContainer.pop();
		currentContainer.peek().addChild(operation);
	}
	// Divide End /////////////////////////////////////////////
}
