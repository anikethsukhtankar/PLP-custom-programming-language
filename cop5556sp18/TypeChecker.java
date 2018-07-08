package cop5556sp18;

import java.util.LinkedList;

import cop5556sp18.Scanner.Kind;
import cop5556sp18.Scanner.Token;
import cop5556sp18.Types.Type;
import cop5556sp18.AST.ASTNode;
import cop5556sp18.AST.ASTVisitor;
import cop5556sp18.AST.Block;
import cop5556sp18.AST.Declaration;
import cop5556sp18.AST.ExpressionBinary;
import cop5556sp18.AST.ExpressionBooleanLiteral;
import cop5556sp18.AST.ExpressionConditional;
import cop5556sp18.AST.ExpressionFloatLiteral;
import cop5556sp18.AST.ExpressionFunctionAppWithExpressionArg;
import cop5556sp18.AST.ExpressionFunctionAppWithPixel;
import cop5556sp18.AST.ExpressionIdent;
import cop5556sp18.AST.ExpressionIntegerLiteral;
import cop5556sp18.AST.ExpressionPixel;
import cop5556sp18.AST.ExpressionPixelConstructor;
import cop5556sp18.AST.ExpressionPredefinedName;
import cop5556sp18.AST.ExpressionUnary;
import cop5556sp18.AST.LHSIdent;
import cop5556sp18.AST.LHSPixel;
import cop5556sp18.AST.LHSSample;
import cop5556sp18.AST.PixelSelector;
import cop5556sp18.AST.Program;
import cop5556sp18.AST.StatementAssign;
import cop5556sp18.AST.StatementIf;
import cop5556sp18.AST.StatementInput;
import cop5556sp18.AST.StatementShow;
import cop5556sp18.AST.StatementSleep;
import cop5556sp18.AST.StatementWhile;
import cop5556sp18.AST.StatementWrite;

public class TypeChecker implements ASTVisitor {

	SymbolTable st;
	private int next_scope;
	private int current_scope;
	LinkedList<Integer> scope_stack;

	TypeChecker() {
		st = new SymbolTable();
		scope_stack = new LinkedList<Integer>();
		next_scope = 0;
		current_scope = 0;
	}
	
	public void enterScope() {
		current_scope = next_scope++;
		scope_stack.addFirst(current_scope);
	}
	
	public void leaveScope() {
		scope_stack.removeFirst();
		if(scope_stack.size() != 0) {
			current_scope = scope_stack.peekFirst();
		}
		else {
			current_scope = 0;
		}
	}

	@SuppressWarnings("serial")
	public static class SemanticException extends Exception {
		Token t;

		public SemanticException(Token t, String message) {
			super(message);
			this.t = t;
		}
	}

	
	
	// Name is only used for naming the output file. 
	// Visit the child block to type check program.
	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {
		program.block.visit(this, arg);
		return null;
	}

	@Override
	public Object visitBlock(Block block, Object arg) throws Exception {
		enterScope();
		for(ASTNode a : block.decsOrStatements) {
			a.visit(this, arg);
		}
		leaveScope();
		return null;
	}

	@Override
	public Object visitDeclaration(Declaration declaration, Object arg) throws Exception {
		String decName = declaration.name;
		if(st.existInScope(decName, current_scope)) {
			throw new SemanticException(declaration.firstToken, "The Variable has already been declared previously in the same scope");
		}
		else {
			if(declaration.width != null && declaration.height != null) {
				if(Types.getType(declaration.type) != Type.IMAGE) {
					throw new SemanticException(declaration.firstToken, "Only Image type can have attributes width and height");
				}
				Type t0 = (Type) declaration.width.visit(this, arg);
				if(t0 != Type.INTEGER) {
					throw new SemanticException(declaration.firstToken, "Width Attribute has to be an Integer");
				}
				Type t1 = (Type) declaration.height.visit(this, arg);
				if(t1 != Type.INTEGER) {
					throw new SemanticException(declaration.firstToken, "Height Attribute has to be an Integer");
				}
			}
			else if(declaration.width == null && declaration.height == null) {
				//do nothing
			}
			else if(declaration.width == null || declaration.height == null) {
				throw new SemanticException(declaration.firstToken, "Width and Height can either be both null or both non null");
			}
			st.insert(decName, current_scope, declaration);
		}
		return null;
	}

	@Override
	public Object visitStatementWrite(StatementWrite statementWrite, Object arg) throws Exception {
		statementWrite.sourceDec = st.lookup(statementWrite.sourceName, scope_stack);
		if(statementWrite.sourceDec == null) throw new SemanticException(statementWrite.firstToken,"Source Declaration cannot be null");
		statementWrite.destDec = st.lookup(statementWrite.destName, scope_stack);
		if(statementWrite.destDec == null) throw new SemanticException(statementWrite.firstToken,"Destination Declaration cannot be null");
		if(Types.getType(statementWrite.sourceDec.type) != Type.IMAGE) {
			throw new SemanticException(statementWrite.sourceDec.firstToken, "Only Image can be source in Statement Write");
		}
		if(Types.getType(statementWrite.destDec.type) != Type.FILE) {
			throw new SemanticException(statementWrite.destDec.firstToken, "Only Filename can be destination in Statement Write");
		}
		return null;
	}

	@Override
	public Object visitStatementInput(StatementInput statementInput, Object arg) throws Exception {
		statementInput.dec = st.lookup(statementInput.destName, scope_stack);
		if(statementInput.dec == null) throw new SemanticException(statementInput.firstToken, "Destination name cannot be null in Statement Input");
		Type t = (Type) statementInput.e.visit(this, arg);
		if(t != Type.INTEGER) {
			throw new SemanticException(statementInput.firstToken, "Destination Name Attribute has to be an Integer in Statement Write");
		}
		return null;
	}

	@Override
	public Object visitPixelSelector(PixelSelector pixelSelector, Object arg) throws Exception {
		Type t0 = (Type) pixelSelector.ex.visit(this, arg);
		Type t1 = (Type) pixelSelector.ey.visit(this, arg);
		if(t0 != t1) throw new SemanticException(pixelSelector.firstToken, "The types of Ex and Ey must be the same in Pixel Selector");
		if(!(t0 == Type.INTEGER || t0 == Type.FLOAT)) throw new SemanticException(pixelSelector.firstToken, "Expressions must be of type either Integer or Float in Pixel Selector");
		return null;
	}

	@Override
	public Object visitExpressionConditional(ExpressionConditional expressionConditional, Object arg) throws Exception {
		Type t = (Type) expressionConditional.guard.visit(this, arg);
		if(t != Type.BOOLEAN) throw new SemanticException(expressionConditional.firstToken,"Expression Conditional guard must be of type Boolean");
		Type t0 = (Type) expressionConditional.trueExpression.visit(this, arg);
		Type t1 = (Type) expressionConditional.falseExpression.visit(this, arg);
		if(t0 != t1) throw new SemanticException(expressionConditional.firstToken, "The types of True Expression and False Expression do not match in Expression Conditional");
		expressionConditional.type = t1;
		return expressionConditional.type;
	}

	@Override
	public Object visitExpressionBinary(ExpressionBinary expressionBinary, Object arg) throws Exception {
		Type t0 = (Type) expressionBinary.leftExpression.visit(this, arg);
		Type t1 = (Type) expressionBinary.rightExpression.visit(this, arg);
		Type inf = null;
		if(t0 == Type.INTEGER && t1 == Type.INTEGER) {
			if(expressionBinary.op == Kind.OP_PLUS || 
			   expressionBinary.op == Kind.OP_MINUS ||
			   expressionBinary.op == Kind.OP_TIMES ||
			   expressionBinary.op == Kind.OP_DIV ||
			   expressionBinary.op == Kind.OP_MOD ||
			   expressionBinary.op == Kind.OP_POWER ||
			   expressionBinary.op == Kind.OP_AND ||
			   expressionBinary.op == Kind.OP_OR			   
			  ) {
				inf = Type.INTEGER;
			}
			else if(expressionBinary.op == Kind.OP_EQ || 
					   expressionBinary.op == Kind.OP_NEQ ||
					   expressionBinary.op == Kind.OP_GT ||
					   expressionBinary.op == Kind.OP_GE ||
					   expressionBinary.op == Kind.OP_LT ||
					   expressionBinary.op == Kind.OP_LE			   
					  ) {
				inf = Type.BOOLEAN;
			}
			else {
				throw new SemanticException(expressionBinary.firstToken, "Operator not defined on Integer");
			}
		}
		else if(t0 == Type.FLOAT && t1 == Type.FLOAT) {
			if(expressionBinary.op == Kind.OP_PLUS || 
			   expressionBinary.op == Kind.OP_MINUS ||
			   expressionBinary.op == Kind.OP_TIMES ||
			   expressionBinary.op == Kind.OP_DIV ||
			   expressionBinary.op == Kind.OP_POWER			   
			  ) {
				inf = Type.FLOAT;
			}
			else if(expressionBinary.op == Kind.OP_EQ || 
					   expressionBinary.op == Kind.OP_NEQ ||
					   expressionBinary.op == Kind.OP_GT ||
					   expressionBinary.op == Kind.OP_GE ||
					   expressionBinary.op == Kind.OP_LT ||
					   expressionBinary.op == Kind.OP_LE			   
					  ) {
				inf = Type.BOOLEAN;
			}
			else {
				throw new SemanticException(expressionBinary.firstToken, "Operator not defined on Float");
			}
		}
		else if(t0 == Type.FLOAT && t1 == Type.INTEGER) {
			if(expressionBinary.op == Kind.OP_PLUS || 
			   expressionBinary.op == Kind.OP_MINUS ||
			   expressionBinary.op == Kind.OP_TIMES ||
			   expressionBinary.op == Kind.OP_DIV ||
			   expressionBinary.op == Kind.OP_POWER			   
			  ) {
				inf = Type.FLOAT;
			}
			else {
				throw new SemanticException(expressionBinary.firstToken, "Operator not defined on Float and Integer");
			}
		}
		else if(t0 == Type.INTEGER && t1 == Type.FLOAT) {
			if(expressionBinary.op == Kind.OP_PLUS || 
			   expressionBinary.op == Kind.OP_MINUS ||
			   expressionBinary.op == Kind.OP_TIMES ||
			   expressionBinary.op == Kind.OP_DIV ||
			   expressionBinary.op == Kind.OP_POWER			   
			  ) {
				inf = Type.FLOAT;
			}
			else {
				throw new SemanticException(expressionBinary.firstToken, "Operator not defined on Integer and Float");
			}
		}
		else if(t0 == Type.BOOLEAN && t1 == Type.BOOLEAN) {
			if(expressionBinary.op == Kind.OP_AND || 
			   expressionBinary.op == Kind.OP_OR 		   
			  ) {
				inf = Type.BOOLEAN;
			}
			else if(expressionBinary.op == Kind.OP_EQ || 
					   expressionBinary.op == Kind.OP_NEQ ||
					   expressionBinary.op == Kind.OP_GT ||
					   expressionBinary.op == Kind.OP_GE ||
					   expressionBinary.op == Kind.OP_LT ||
					   expressionBinary.op == Kind.OP_LE			   
					  ) {
				inf = Type.BOOLEAN;
			}
			else {
				throw new SemanticException(expressionBinary.firstToken, "Operator not defined on Boolean");
			}
		}
		else {
			throw new SemanticException(expressionBinary.firstToken, "Illegal Binary Expression");
		}
		expressionBinary.type = inf;
		return inf;
	}

	@Override
	public Object visitExpressionUnary(ExpressionUnary expressionUnary, Object arg) throws Exception {
		Type t = (Type) expressionUnary.expression.visit(this, arg);
		expressionUnary.type = t;
		return expressionUnary.type;
	}

	@Override
	public Object visitExpressionIntegerLiteral(ExpressionIntegerLiteral expressionIntegerLiteral, Object arg)
			throws Exception {
		expressionIntegerLiteral.type = Type.INTEGER;
		return expressionIntegerLiteral.type;
	}

	@Override
	public Object visitBooleanLiteral(ExpressionBooleanLiteral expressionBooleanLiteral, Object arg) throws Exception {
		expressionBooleanLiteral.type = Type.BOOLEAN;
		return expressionBooleanLiteral.type;
	}

	@Override
	public Object visitExpressionPredefinedName(ExpressionPredefinedName expressionPredefinedName, Object arg)
			throws Exception {
		expressionPredefinedName.type = Type.INTEGER;
		return expressionPredefinedName.type;
	}

	@Override
	public Object visitExpressionFloatLiteral(ExpressionFloatLiteral expressionFloatLiteral, Object arg)
			throws Exception {
		expressionFloatLiteral.type = Type.FLOAT;
		return expressionFloatLiteral.type;
	}

	@Override
	public Object visitExpressionFunctionAppWithExpressionArg(
			ExpressionFunctionAppWithExpressionArg expressionFunctionAppWithExpressionArg, Object arg)
			throws Exception {
		Type t = (Type) expressionFunctionAppWithExpressionArg.e.visit(this, arg);
		Kind f = expressionFunctionAppWithExpressionArg.function;
		Type inf = null;
		if(t == Type.INTEGER) {
			if(f == Kind.KW_abs || f == Kind.KW_red || f == Kind.KW_green || f == Kind.KW_blue || f == Kind.KW_alpha) {
				inf = Type.INTEGER;
			}
			else if(f == Kind.KW_float) {
				inf = Type.FLOAT;
			}
			else if(f == Kind.KW_int) {
				inf = Type.INTEGER;
			}
			else {
				throw new SemanticException(expressionFunctionAppWithExpressionArg.firstToken,"Function not supported for type Integer Expression Function App With Expression Arg");
			}
		}
		else if(t == Type.FLOAT) {
			if(f == Kind.KW_abs || f == Kind.KW_sin|| f == Kind.KW_cos || f == Kind.KW_atan || f == Kind.KW_log) {
				inf = Type.FLOAT;
			}
			else if(f == Kind.KW_float) {
				inf = Type.FLOAT;
			}
			else if(f == Kind.KW_int) {
				inf = Type.INTEGER;
			}
			else {
				throw new SemanticException(expressionFunctionAppWithExpressionArg.firstToken,"Function not supported for type Float Expression Function App With Expression Arg");
			}
		}
		else if(t == Type.IMAGE) {
			if(f == Kind.KW_width || f == Kind.KW_height) {
				inf = Type.INTEGER;
			}
			else {
				throw new SemanticException(expressionFunctionAppWithExpressionArg.firstToken,"Function not supported for type Image Expression Function App With Expression Arg");
			}
		}
		else {
			throw new SemanticException(expressionFunctionAppWithExpressionArg.firstToken,"Expression type not supported for Expression Function App With Expression Arg");
		}
		expressionFunctionAppWithExpressionArg.type = inf;
		return inf;
	}

	@Override
	public Object visitExpressionFunctionAppWithPixel(ExpressionFunctionAppWithPixel expressionFunctionAppWithPixel,
			Object arg) throws Exception {
		if(expressionFunctionAppWithPixel.name == Kind.KW_cart_x || expressionFunctionAppWithPixel.name == Kind.KW_cart_y) {
			Type t0 = (Type) expressionFunctionAppWithPixel.e0.visit(this, arg);
			Type t1 = (Type) expressionFunctionAppWithPixel.e1.visit(this, arg);
			if(t0 != Type.FLOAT)
				throw new SemanticException(expressionFunctionAppWithPixel.firstToken,"Arguments to cart must be of type Float");
			if(t1 != Type.FLOAT)
				throw new SemanticException(expressionFunctionAppWithPixel.firstToken,"Arguments to cart must be of type Float");
			expressionFunctionAppWithPixel.type = Type.INTEGER;
		}
		else if(expressionFunctionAppWithPixel.name == Kind.KW_polar_a || expressionFunctionAppWithPixel.name == Kind.KW_polar_r) {
			Type t0 = (Type) expressionFunctionAppWithPixel.e0.visit(this, arg);
			Type t1 = (Type) expressionFunctionAppWithPixel.e1.visit(this, arg);
			if(t0 != Type.INTEGER)
				throw new SemanticException(expressionFunctionAppWithPixel.firstToken,"Arguments to polar must be of type Integer");
			if(t1 != Type.INTEGER)
				throw new SemanticException(expressionFunctionAppWithPixel.firstToken,"Arguments to polar must be of type Integer");
			expressionFunctionAppWithPixel.type = Type.FLOAT;
		}
		return expressionFunctionAppWithPixel.type;
	}

	@Override
	public Object visitExpressionPixelConstructor(ExpressionPixelConstructor expressionPixelConstructor, Object arg)
			throws Exception {
		Type alpha = (Type) expressionPixelConstructor.alpha.visit(this, arg);
		Type red = (Type) expressionPixelConstructor.red.visit(this, arg);
		Type green = (Type) expressionPixelConstructor.green.visit(this, arg);
		Type blue = (Type) expressionPixelConstructor.blue.visit(this, arg);
		if(alpha != Type.INTEGER || red != Type.INTEGER || green != Type.INTEGER || blue != Type.INTEGER)
			throw new SemanticException(expressionPixelConstructor.firstToken,"Pixel Constructor arguments must be of type Integer");
		expressionPixelConstructor.type = Type.INTEGER;
		return expressionPixelConstructor.type;
	}

	@Override
	public Object visitStatementAssign(StatementAssign statementAssign, Object arg) throws Exception {
		Type t0 = (Type) statementAssign.e.visit(this, arg);
		Type t1 = (Type) statementAssign.lhs.visit(this, arg);
		if(t0 != t1)
			throw new SemanticException(statementAssign.firstToken, "Type of LHS and expression must match");
		return t1;
	}

	@Override
	public Object visitStatementShow(StatementShow statementShow, Object arg) throws Exception {
		Type t = (Type) statementShow.e.visit(this, arg);
		if(!(t == Type.INTEGER || t == Type.BOOLEAN || t == Type.FLOAT || t == Type.IMAGE))
			throw new SemanticException(statementShow.firstToken,"Argument to Statement Show must be of type Integer, Float, Boolean or Image");
		return t;
	}

	@Override
	public Object visitExpressionPixel(ExpressionPixel expressionPixel, Object arg) throws Exception {
		expressionPixel.dec = st.lookup(expressionPixel.name, scope_stack);
		if(expressionPixel.dec == null)
			throw new SemanticException(expressionPixel.firstToken, "Expression Pixel Declaration cannot be null");
		if(Types.getType(expressionPixel.dec.type) != Type.IMAGE)
			throw new SemanticException(expressionPixel.firstToken, "Expression Pixel Declaration has to be of type Image");
		expressionPixel.pixelSelector.visit(this, arg);
		expressionPixel.type = Type.INTEGER;
		return expressionPixel.type;
	}

	@Override
	public Object visitExpressionIdent(ExpressionIdent expressionIdent, Object arg) throws Exception {
		expressionIdent.dec = st.lookup(expressionIdent.name, scope_stack);
		if(expressionIdent.dec == null)
			throw new SemanticException(expressionIdent.firstToken, "Expression Ident declaration cannot be null");
		expressionIdent.type = Types.getType(expressionIdent.dec.type);
		return expressionIdent.type;
	}

	@Override
	public Object visitLHSSample(LHSSample lhsSample, Object arg) throws Exception {
		lhsSample.dec = st.lookup(lhsSample.name, scope_stack);
		if(lhsSample.dec == null)
			throw new SemanticException(lhsSample.firstToken, "LHS Sample Declaration cannot be null");
		if(Types.getType(lhsSample.dec.type) != Type.IMAGE)
			throw new SemanticException(lhsSample.firstToken, "LHS Sample Declaration has to be of type Image");
		lhsSample.pixelSelector.visit(this, arg);
		lhsSample.type = Type.INTEGER;
		return lhsSample.type;
	}

	@Override
	public Object visitLHSPixel(LHSPixel lhsPixel, Object arg) throws Exception {
		lhsPixel.dec = st.lookup(lhsPixel.name, scope_stack);
		if(lhsPixel.dec == null)
			throw new SemanticException(lhsPixel.firstToken, "LHS Pixel Declaration cannot be null");
		if(Types.getType(lhsPixel.dec.type) != Type.IMAGE)
			throw new SemanticException(lhsPixel.firstToken, "LHS Pixel Declaration has to be of type Image");
		lhsPixel.pixelSelector.visit(this, arg);
		lhsPixel.type = Type.INTEGER;
		return lhsPixel.type;
	}

	@Override
	public Object visitLHSIdent(LHSIdent lhsIdent, Object arg) throws Exception {
		lhsIdent.dec = st.lookup(lhsIdent.name, scope_stack);
		if(lhsIdent.dec == null)
			throw new SemanticException(lhsIdent.firstToken, "LHS Ident Declaration cannot be null");
		lhsIdent.type = Types.getType(lhsIdent.dec.type);
		return lhsIdent.type;
	}

	@Override
	public Object visitStatementIf(StatementIf statementIf, Object arg) throws Exception {
		Type t = (Type) statementIf.guard.visit(this, arg);
		if(t != Type.BOOLEAN)
			throw new SemanticException(statementIf.firstToken, "Guard must be of type Boolean in Statement If");
		statementIf.b.visit(this, arg);
		return null;
	}

	@Override
	public Object visitStatementWhile(StatementWhile statementWhile, Object arg) throws Exception {
		Type t = (Type) statementWhile.guard.visit(this, arg);
		if(t != Type.BOOLEAN)
			throw new SemanticException(statementWhile.firstToken, "Guard must be of type Boolean in Statement While");
		statementWhile.b.visit(this, arg);
		return null;
	}

	@Override
	public Object visitStatementSleep(StatementSleep statementSleep, Object arg) throws Exception {
		Type t = (Type) statementSleep.duration.visit(this, arg);
		if(t != Type.INTEGER)
			throw new SemanticException(statementSleep.firstToken, "Duration must be of type Integer in Statement Sleep");
		return null;
	}


}
