package cop5556sp18;
/* *
 * Initial code for Parser for the class project in COP5556 Programming Language Principles 
 * at the University of Florida, Spring 2018.
 * 
 * This software is solely for the educational benefit of students 
 * enrolled in the course during the Spring 2018 semester.  
 * 
 * This software, and any software derived from it,  may not be shared with others or posted to public web sites,
 * either during the course or afterwards.
 * 
 *  @Beverly A. Sanders, 2018
 */


import cop5556sp18.Scanner.Token;
import cop5556sp18.AST.ASTNode;
import cop5556sp18.AST.Block;
import cop5556sp18.AST.Declaration;
import cop5556sp18.AST.Expression;
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
import cop5556sp18.AST.LHS;
import cop5556sp18.AST.LHSIdent;
import cop5556sp18.AST.LHSPixel;
import cop5556sp18.AST.LHSSample;
import cop5556sp18.AST.PixelSelector;
import cop5556sp18.AST.Program;
import cop5556sp18.AST.Statement;
import cop5556sp18.AST.StatementAssign;
import cop5556sp18.AST.StatementIf;
import cop5556sp18.AST.StatementInput;
import cop5556sp18.AST.StatementShow;
import cop5556sp18.AST.StatementSleep;
import cop5556sp18.AST.StatementWhile;
import cop5556sp18.AST.StatementWrite;
import cop5556sp18.Scanner.Kind;
import static cop5556sp18.Scanner.Kind.*;

import java.util.ArrayList;
import java.util.List;


public class Parser {
	
	@SuppressWarnings("serial")
	public static class SyntaxException extends Exception {
		Token t;

		public SyntaxException(Token t, String message) {
			super(message);
			this.t = t;
		}

	}



	Scanner scanner;
	Token t;

	Parser(Scanner scanner) {
		this.scanner = scanner;
		t = scanner.nextToken();
	}


	public Program parse() throws SyntaxException {
		Program p = program();
		matchEOF();
		return p;
	}

	/*
	 * Program ::= Identifier Block
	 */
	public Program program() throws SyntaxException {
		Token first = t;
		Token progName = match(IDENTIFIER);
		Block block = block();
		return new Program(first,progName,block);
	}
	
	/*
	 * Block ::=  { (  (Declaration | Statement) ; )* }
	 */
	
	Kind[] firstDec = { KW_int, KW_boolean, KW_image, KW_float, KW_filename };
	Kind[] firstStatement = {KW_input, KW_write, IDENTIFIER, KW_red, KW_green, KW_blue, KW_alpha, KW_while, KW_if, KW_show, KW_sleep};

	public Block block() throws SyntaxException {
		Token first = t;
		List<ASTNode> decsOrStatements = new ArrayList<ASTNode>();
		Declaration d = null;
		Statement s = null;
		match(LBRACE);
		while (isKind(firstDec)|isKind(firstStatement)) {
	     if (isKind(firstDec)) {
			d = declaration();
			decsOrStatements.add(d);
		} else if (isKind(firstStatement)) {
			s = statement();
			decsOrStatements.add(s);
		}
			match(SEMI);
		}
		match(RBRACE);
		return new Block(first,decsOrStatements);
	}
	
	Kind[] firstType = { KW_int, KW_boolean, KW_float, KW_filename };
	
	public Declaration declaration() throws SyntaxException {
		Token first = t;
		Token type = null;
		Token name = null;
		Expression width = null;
		Expression height = null;
		if (isKind(firstType)) {
			type = type();
			name = match(IDENTIFIER);
			return new Declaration(first,type,name,width,height);
		} 
		else if (isKind(KW_image)) {
			type = t;
			consume();
			name = match(IDENTIFIER);
			if(isKind(LSQUARE)) {
				consume();
				width = expression();
				match(COMMA);
				height = expression();
				match(RSQUARE);
				return new Declaration(first,type,name,width,height);
			}
			else {
				return new Declaration(first,type,name,width,height);
			}
		}
		else {
			throw new SyntaxException(t,"Expecting start of Declaration");
		}
	}
	
	public Token type() throws SyntaxException {
		if(isKind(firstType)) {
			Token type = t;
			consume();
			return type;
		}
		else {
			throw new SyntaxException(t,"Expecting token of type Type");
		}
	}
	
	Kind[] firstLHS = { IDENTIFIER, KW_red, KW_green, KW_blue, KW_alpha };
	
	public Statement statement() throws SyntaxException {
		Token first = t;
		if(isKind(KW_input)) {
			consume();
			Token destName = match(IDENTIFIER);
			match(KW_from);
			match(OP_AT);
			Expression e = expression();
			return new StatementInput(first,destName,e);
		}
		else if(isKind(KW_write)) {
			consume();
			Token sourceName = match(IDENTIFIER);
			match(KW_to);
			Token destName = match(IDENTIFIER);
			return new StatementWrite(first,sourceName,destName);
		}
		else if(isKind(firstLHS)) {
			LHS lhs = lhs();
			match(OP_ASSIGN);
			Expression e = expression();
			return new StatementAssign(first,lhs,e);
		}
		else if(isKind(KW_while)) {
			consume();
			match(LPAREN);
			Expression guard = expression();
			match(RPAREN);
			Block b = block();
			return new StatementWhile(first,guard,b);
		}
		else if(isKind(KW_if)) {
			consume();
			match(LPAREN);
			Expression guard = expression();
			match(RPAREN);
			Block b = block();
			return new StatementIf(first,guard,b);
		}
		else if(isKind(KW_show)) {
			consume();
			Expression e = expression();
			return new StatementShow(first,e);
		}
		else if(isKind(KW_sleep)) {
			consume();
			Expression duration = expression();
			return new StatementSleep(first,duration);
		}
		else {
			throw new SyntaxException(t,"Expecting start of Statement");
		}
	}
	
	Kind[] firstColor = { KW_red, KW_green, KW_blue, KW_alpha };
	
	public LHS lhs() throws SyntaxException {
		Token first = t;
		Token name = null;
		if(isKind(IDENTIFIER)) {
			name = t;
			consume();
			if(isKind(LSQUARE)) {
				PixelSelector pixelSelector = pixelselector();
				return new LHSPixel(first,name,pixelSelector);
			}
			else {
				return new LHSIdent(first,name);
			}
		}
		else if(isKind(firstColor)) {
			Token color = t;
			consume();
			match(LPAREN);
			name = match(IDENTIFIER);
			PixelSelector pixel = pixelselector();
			match(RPAREN);
			return new LHSSample(first,name,pixel,color);
		}
		else {
			throw new SyntaxException(t,"Expecting LHS");
		}
	}
	
	public Token color() throws SyntaxException {
		if(isKind(firstColor)) {
			Token color = t;
			consume();
			return color;
		}
		else {
			throw new SyntaxException(t,"Expecting token of type Color");
		}
	}
	
	public PixelSelector pixelselector() throws SyntaxException {
		Token first = t;
		if(isKind(LSQUARE)) {
			consume();
			Expression ex = expression();
			match(COMMA);
			Expression ey = expression();
			match(RSQUARE);
			return new PixelSelector(first,ex,ey);
		}
		else {
			throw new SyntaxException(t,"Expecting start of PixelSelector");
		}
	}
	
	public Expression expression() throws SyntaxException {
		Token first = t;
		Expression guard = orexpression();
		Expression trueExpression = null;
		Expression falseExpression = null;
		if(isKind(OP_QUESTION)) {
			consume();
			trueExpression = expression();
			match(OP_COLON);
			falseExpression = expression();
			return new ExpressionConditional(first,guard,trueExpression,falseExpression);
		}
		else {
			return guard;
		}
	}
	
	public Expression orexpression() throws SyntaxException {
		Token first = t;
		Expression leftExpression = andexpression();
		while(isKind(OP_OR)) {
			Token op = t;
			consume();
			Expression rightExpression = andexpression();
			leftExpression = new ExpressionBinary(first,leftExpression,op,rightExpression);
		}
		return leftExpression;
	}
	
	public Expression andexpression() throws SyntaxException {
		Token first = t;
		Expression leftExpression = eqexpression();
		while(isKind(OP_AND)) {
			Token op = t;
			consume();
			Expression rightExpression = eqexpression();
			leftExpression = new ExpressionBinary(first,leftExpression,op,rightExpression);
		}
		return leftExpression;
	}
	
	public Expression eqexpression() throws SyntaxException {
		Token first = t;
		Expression leftExpression = relexpression();
		while(isKind(OP_EQ) || isKind(OP_NEQ)) {
			Token op = t;
			consume();
			Expression rightExpression = relexpression();
			leftExpression = new ExpressionBinary(first,leftExpression,op,rightExpression);
		}
		return leftExpression;
	}
	
	public Expression relexpression() throws SyntaxException {
		Token first = t;
		Expression leftExpression = addexpression();
		while(isKind(OP_LT) || isKind(OP_GT) || isKind(OP_LE) || isKind(OP_GE)) {
			Token op = t;
			consume();
			Expression rightExpression = addexpression();
			leftExpression = new ExpressionBinary(first,leftExpression,op,rightExpression);
		}
		return leftExpression;
	}
	
	public Expression addexpression() throws SyntaxException {
		Token first = t;
		Expression leftExpression = multexpression();
		while(isKind(OP_PLUS) || isKind(OP_MINUS)) {
			Token op = t;
			consume();
			Expression rightExpression = multexpression();
			leftExpression = new ExpressionBinary(first,leftExpression,op,rightExpression);
		}
		return leftExpression;
	}
	
	public Expression multexpression() throws SyntaxException {
		Token first = t;
		Expression leftExpression = powexpression();
		while(isKind(OP_TIMES) || isKind(OP_DIV) || isKind(OP_MOD)) {
			Token op = t;
			consume();
			Expression rightExpression = powexpression();
			leftExpression = new ExpressionBinary(first,leftExpression,op,rightExpression);
		}
		return leftExpression;
	}
	
	public Expression powexpression() throws SyntaxException {
		Token first = t;
		Expression leftExpression = unaryexpression();
		if(isKind(OP_POWER)) {
			Token op = t;
			consume();
			Expression rightExpression = powexpression();
			leftExpression = new ExpressionBinary(first,leftExpression,op,rightExpression);
		}
		return leftExpression;
	}
	
	public Expression unaryexpression() throws SyntaxException {
		Token first = t;
		Token op = null;
		Expression expression = null;
		if(isKind(OP_PLUS) || isKind(OP_MINUS)) {
			op = t;
			consume();
			expression = unaryexpression();
			return new ExpressionUnary(first,op,expression);
		}
		else {
			expression = unarynotplusminus();
			return expression;
		}
	}
	Kind[] firstPrimary = { IDENTIFIER, INTEGER_LITERAL, BOOLEAN_LITERAL, FLOAT_LITERAL,
			KW_Z/* Z */, KW_default_width/* default_width */, KW_default_height/* default_height */, 
			KW_width /* width */, KW_height /* height*/, 
			KW_cart_x/* cart_x*/, KW_cart_y/* cart_y */, 
			KW_polar_a/* polar_a*/, KW_polar_r/* polar_r*/, KW_abs/* abs */, KW_sin/* sin*/, KW_cos/* cos */, 
			KW_atan/* atan */, KW_log/* log */, KW_int/* int */, KW_float /* float */, 
			KW_red /* red */, KW_blue /* blue */, 
			KW_green /* green */, KW_alpha /* alpha*/,
			LPAREN, LPIXEL};
	
	
	public Expression unarynotplusminus() throws SyntaxException {
		Token first = null;
		Token op = null;
		Expression expression = null;
		if(isKind(OP_EXCLAMATION)) {
			op = t;
			consume();
			expression = unaryexpression();
			return new ExpressionUnary(first,op,expression);
		}
		else if(isKind(firstPrimary)) {
			expression = primary();
			return expression;
		}
		else {
			throw new SyntaxException(t,"Expecting token of type Unary");
		}
	}
	
	Kind[] firstFunctionName = { 
			KW_width /* width */, KW_height /* height*/, 
			KW_cart_x/* cart_x*/, KW_cart_y/* cart_y */, 
			KW_polar_a/* polar_a*/, KW_polar_r/* polar_r*/, 
			KW_abs/* abs */, KW_sin/* sin*/, KW_cos/* cos */, 
			KW_atan/* atan */, KW_log/* log */, 
			KW_int/* int */, KW_float /* float */, 
			KW_red /* red */, KW_blue /* blue */, 
			KW_green /* green */, KW_alpha /* alpha*/};
	
	Kind[] firstPredefinedName = { KW_Z/* Z */, KW_default_width/* default_width */, KW_default_height/* default_height */ };
	
	public Expression primary() throws SyntaxException {
		Token first = t;
		if(isKind(INTEGER_LITERAL)) {
			Token intLiteral = t;
			consume();
			return new ExpressionIntegerLiteral(first,intLiteral);
		}
		else if(isKind(BOOLEAN_LITERAL)) {
			Token booleanLiteral = t;
			consume();
			return new ExpressionBooleanLiteral(first,booleanLiteral);
		}
		else if(isKind(FLOAT_LITERAL)) {
			Token floatLit = t;
			consume();
			return new ExpressionFloatLiteral(first,floatLit);
		}
		else if(isKind(LPAREN)) {
			consume();
			Expression e = expression();
			match(RPAREN);
			return e;
		}
		else if(isKind(firstFunctionName)) {
			Expression e = functionapplication();
			return e;
		}
		else if(isKind(IDENTIFIER)) {
			Token name = t;
			consume();
			if(isKind(LSQUARE)) {
				PixelSelector p = pixelselector();
				return new ExpressionPixel(first,name,p);
			}
			else {
				return new ExpressionIdent(first,name);
			}
		}
		else if(isKind(firstPredefinedName)) {
			Token name = t;
			consume();
			return new ExpressionPredefinedName(first,name);
		}
		else if(isKind(LPIXEL)) {
			Expression p = pixelconstructor();
			return p;
		}
		else {
			throw new SyntaxException(t,"Expecting token of type Primary");
		}
	}

	public Expression functionapplication() throws SyntaxException {
		Token first = t;
		if(isKind(firstFunctionName)) {
			Token function = t;
			consume();
			if(isKind(LPAREN)) {
				consume();
				Expression e = expression();
				match(RPAREN);
				return new ExpressionFunctionAppWithExpressionArg(first,function,e);
			}
			else if(isKind(LSQUARE)) {
				consume();
				Expression e0 = expression();
				match(COMMA);
				Expression e1 = expression();
				match(RSQUARE);
				return new ExpressionFunctionAppWithPixel(first,function,e0,e1);
			}
			else {
				throw new SyntaxException(t,"Expecting set of expressions after Function Name");
			}
		}
		else {
			throw new SyntaxException(t,"Expecting start of Function Name");
		}
	}

	public Expression pixelconstructor() throws SyntaxException {
		Token first = t;
		if(isKind(LPIXEL)) {
			consume();
			Expression alpha = expression();
			match(COMMA);
			Expression red = expression();
			match(COMMA);
			Expression green = expression();
			match(COMMA);
			Expression blue = expression();
			match(RPIXEL);
			return new ExpressionPixelConstructor(first,alpha,red,green,blue);
		}
		else {
			throw new SyntaxException(t,"Expecting start of Pixel Constructor");
		}
	}

	protected boolean isKind(Kind kind) {
		return t.kind == kind;
	}

	protected boolean isKind(Kind... kinds) {
		for (Kind k : kinds) {
			if (k == t.kind)
				return true;
		}
		return false;
	}


	/**
	 * Precondition: kind != EOF
	 * 
	 * @param kind
	 * @return
	 * @throws SyntaxException
	 */
	private Token match(Kind kind) throws SyntaxException {
		Token tmp = t;
		if (isKind(kind)) {
			consume();
			return tmp;
		}
		throw new SyntaxException(t,"Kind mismatch in Match"); //TODO  give a better error message!
	}


	private Token consume() throws SyntaxException {
		Token tmp = t;
		if (isKind( EOF)) {
			throw new SyntaxException(t,"EOF encountered incorrectly"); //TODO  give a better error message!  
			//Note that EOF should be matched by the matchEOF method which is called only in parse().  
			//Anywhere else is an error. */
		}
		t = scanner.nextToken();
		return tmp;
	}


	/**
	 * Only for check at end of program. Does not "consume" EOF so no attempt to get
	 * nonexistent next Token.
	 * 
	 * @return
	 * @throws SyntaxException
	 */
	private Token matchEOF() throws SyntaxException {
		if (isKind(EOF)) {
			return t;
		}
		throw new SyntaxException(t,"End of File Expected"); //TODO  give a better error message!
	}
	

}

