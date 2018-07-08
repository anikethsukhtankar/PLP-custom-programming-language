 /**
 * JUunit tests for the Parser for the class project in COP5556 Programming Language Principles 
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

package cop5556sp18;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import cop5556sp18.Scanner.Kind;
import cop5556sp18.Parser;
import cop5556sp18.Scanner;
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
import cop5556sp18.AST.StatementInput;
import cop5556sp18.AST.StatementShow;
import cop5556sp18.AST.StatementSleep;
import cop5556sp18.AST.StatementWhile;
import cop5556sp18.AST.LHS;
import cop5556sp18.AST.LHSIdent;
import cop5556sp18.AST.LHSPixel;
import cop5556sp18.AST.LHSSample;
import cop5556sp18.AST.PixelSelector;
import cop5556sp18.AST.Program;
import cop5556sp18.AST.Statement;
import cop5556sp18.AST.StatementAssign;
import cop5556sp18.AST.StatementIf;
import cop5556sp18.AST.StatementWrite;
import cop5556sp18.Parser.SyntaxException;
import cop5556sp18.Scanner.LexicalException;
import cop5556sp18.Scanner.Token;
import static cop5556sp18.Scanner.Kind.*;

@SuppressWarnings("unused")
public class ParserTest {

	//set Junit to be able to catch exceptions
	@Rule
	public ExpectedException thrown = ExpectedException.none();

	
	//To make it easy to print objects and turn this output on and off
	static final boolean doPrint = true;
	private void show(Object input) {
		if (doPrint) {
			System.out.println(input.toString());
		}
	}


	//creates and returns a parser for the given input.
	private Parser makeParser(String input) throws LexicalException {
		show(input);        //Display the input 
		Scanner scanner = new Scanner(input).scan();  //Create a Scanner and initialize it
		show(scanner);   //Display the Scanner
		Parser parser = new Parser(scanner);
		return parser;
	}
	

	
	/**
	 * Simple test case with an empty program.  This throws an exception 
	 * because it lacks an identifier and a block
	 *   
	 * @throws LexicalException
	 * @throws SyntaxException 
	 */
	@Test
	public void testEmpty() throws LexicalException, SyntaxException {
		String input = "";  //The input is the empty string.  
		thrown.expect(SyntaxException.class);
		Parser parser = makeParser(input);
		Program p = parser.parse();
	}
	
	@Test
	public void testDemo1() throws LexicalException, SyntaxException {
		String input = "demo1{image h;input h from @0;show h; sleep(4000); image g[width(h),height(h)];int x;x:=0;"
				+ "while(x<width(g)){int y;y:=0;while(y<height(g)){g[x,y]:=h[y,x];y:=y+1;};x:=x+1;};show g;sleep(4000);}";
		Parser parser = makeParser(input);
		Program p = parser.parse();
		show(p);
		assertEquals(p.toString(),"Program [progName=demo1, block=Block [decsOrStatements=[Declaration [type=KW_image, name=h, width=null, height=null], StatementInput [destName=h, e=ExpressionIntegerLiteral [value=0]], ShowStatement [e=ExpressionIdent [name=h]], StatementSleep [duration=ExpressionIntegerLiteral [value=4000]], Declaration [type=KW_image, name=g, width=ExpressionFunctionApp [function=KW_width, e=ExpressionIdent [name=h]], height=ExpressionFunctionApp [function=KW_height, e=ExpressionIdent [name=h]]], Declaration [type=KW_int, name=x, width=null, height=null], StatementAssign [lhs=LHSIdent [name=x], e=ExpressionIntegerLiteral [value=0]], StatementWhile [guard=ExpressionBinary [leftExpression=ExpressionIdent [name=x], op=OP_LT, rightExpression=ExpressionFunctionApp [function=KW_width, e=ExpressionIdent [name=g]]], b=Block [decsOrStatements=[Declaration [type=KW_int, name=y, width=null, height=null], StatementAssign [lhs=LHSIdent [name=y], e=ExpressionIntegerLiteral [value=0]], StatementWhile [guard=ExpressionBinary [leftExpression=ExpressionIdent [name=y], op=OP_LT, rightExpression=ExpressionFunctionApp [function=KW_height, e=ExpressionIdent [name=g]]], b=Block [decsOrStatements=[StatementAssign [lhs=LHSPixel [name=g, pixelSelector=PixelSelector [ex=ExpressionIdent [name=x], ey=ExpressionIdent [name=y]]], e=ExpressionPixel [name=h, pixelSelector=PixelSelector [ex=ExpressionIdent [name=y], ey=ExpressionIdent [name=x]]]], StatementAssign [lhs=LHSIdent [name=y], e=ExpressionBinary [leftExpression=ExpressionIdent [name=y], op=OP_PLUS, rightExpression=ExpressionIntegerLiteral [value=1]]]]]], StatementAssign [lhs=LHSIdent [name=x], e=ExpressionBinary [leftExpression=ExpressionIdent [name=x], op=OP_PLUS, rightExpression=ExpressionIntegerLiteral [value=1]]]]]], ShowStatement [e=ExpressionIdent [name=g]], StatementSleep [duration=ExpressionIntegerLiteral [value=4000]]]]]");
	}
	
	@Test
	public void makeRedImage() throws LexicalException, SyntaxException {
		String input = "makeRedImage{image im[256,256];int x;int y;x:=0;y:=0;while(x<width(im)) {y:=0;while(y<height(im)) {im[x,y]:=<<255,255,0,0>>;y:=y+1;};x:=x+1;};show im;}";
		Parser parser = makeParser(input);
		Program p = parser.parse();
		show(p);
		assertEquals(p.toString(),"Program [progName=makeRedImage, block=Block [decsOrStatements=[Declaration [type=KW_image, name=im, width=ExpressionIntegerLiteral [value=256], height=ExpressionIntegerLiteral [value=256]], Declaration [type=KW_int, name=x, width=null, height=null], Declaration [type=KW_int, name=y, width=null, height=null], StatementAssign [lhs=LHSIdent [name=x], e=ExpressionIntegerLiteral [value=0]], StatementAssign [lhs=LHSIdent [name=y], e=ExpressionIntegerLiteral [value=0]], StatementWhile [guard=ExpressionBinary [leftExpression=ExpressionIdent [name=x], op=OP_LT, rightExpression=ExpressionFunctionApp [function=KW_width, e=ExpressionIdent [name=im]]], b=Block [decsOrStatements=[StatementAssign [lhs=LHSIdent [name=y], e=ExpressionIntegerLiteral [value=0]], StatementWhile [guard=ExpressionBinary [leftExpression=ExpressionIdent [name=y], op=OP_LT, rightExpression=ExpressionFunctionApp [function=KW_height, e=ExpressionIdent [name=im]]], b=Block [decsOrStatements=[StatementAssign [lhs=LHSPixel [name=im, pixelSelector=PixelSelector [ex=ExpressionIdent [name=x], ey=ExpressionIdent [name=y]]], e=ExpressionPixelConstructor [alpha=ExpressionIntegerLiteral [value=255], red=ExpressionIntegerLiteral [value=255], green=ExpressionIntegerLiteral [value=0], blue=ExpressionIntegerLiteral [value=0]]], StatementAssign [lhs=LHSIdent [name=y], e=ExpressionBinary [leftExpression=ExpressionIdent [name=y], op=OP_PLUS, rightExpression=ExpressionIntegerLiteral [value=1]]]]]], StatementAssign [lhs=LHSIdent [name=x], e=ExpressionBinary [leftExpression=ExpressionIdent [name=x], op=OP_PLUS, rightExpression=ExpressionIntegerLiteral [value=1]]]]]], ShowStatement [e=ExpressionIdent [name=im]]]]]");
	}
	
	@Test
	public void testPolarR2() throws LexicalException, SyntaxException {
		String input = "PolarR2{image im[1024,1024];int x;x:=0;while(x<width(im)) {int y;y:=0;while(y<height(im)) {float p;p:=polar_r[x,y];int r;r:=int(p)%Z;im[x,y]:=<<Z,0,0,r>>;y:=y+1;};x:=x+1;};show im;}";
		Parser parser = makeParser(input);
		Program p = parser.parse();
		show(p);
		assertEquals(p.toString(),"Program [progName=PolarR2, block=Block [decsOrStatements=[Declaration [type=KW_image, name=im, width=ExpressionIntegerLiteral [value=1024], height=ExpressionIntegerLiteral [value=1024]], Declaration [type=KW_int, name=x, width=null, height=null], StatementAssign [lhs=LHSIdent [name=x], e=ExpressionIntegerLiteral [value=0]], StatementWhile [guard=ExpressionBinary [leftExpression=ExpressionIdent [name=x], op=OP_LT, rightExpression=ExpressionFunctionApp [function=KW_width, e=ExpressionIdent [name=im]]], b=Block [decsOrStatements=[Declaration [type=KW_int, name=y, width=null, height=null], StatementAssign [lhs=LHSIdent [name=y], e=ExpressionIntegerLiteral [value=0]], StatementWhile [guard=ExpressionBinary [leftExpression=ExpressionIdent [name=y], op=OP_LT, rightExpression=ExpressionFunctionApp [function=KW_height, e=ExpressionIdent [name=im]]], b=Block [decsOrStatements=[Declaration [type=KW_float, name=p, width=null, height=null], StatementAssign [lhs=LHSIdent [name=p], e=ExpressionFunctionAppWithPixel [name=KW_polar_r, e0=ExpressionIdent [name=x], e1=ExpressionIdent [name=y]]], Declaration [type=KW_int, name=r, width=null, height=null], StatementAssign [lhs=LHSIdent [name=r], e=ExpressionBinary [leftExpression=ExpressionFunctionApp [function=KW_int, e=ExpressionIdent [name=p]], op=OP_MOD, rightExpression=ExpressionPredefinedName [name=KW_Z]]], StatementAssign [lhs=LHSPixel [name=im, pixelSelector=PixelSelector [ex=ExpressionIdent [name=x], ey=ExpressionIdent [name=y]]], e=ExpressionPixelConstructor [alpha=ExpressionPredefinedName [name=KW_Z], red=ExpressionIntegerLiteral [value=0], green=ExpressionIntegerLiteral [value=0], blue=ExpressionIdent [name=r]]], StatementAssign [lhs=LHSIdent [name=y], e=ExpressionBinary [leftExpression=ExpressionIdent [name=y], op=OP_PLUS, rightExpression=ExpressionIntegerLiteral [value=1]]]]]], StatementAssign [lhs=LHSIdent [name=x], e=ExpressionBinary [leftExpression=ExpressionIdent [name=x], op=OP_PLUS, rightExpression=ExpressionIntegerLiteral [value=1]]]]]], ShowStatement [e=ExpressionIdent [name=im]]]]]");
	}

	@Test
	public void testSamples() throws LexicalException, SyntaxException {
		String input = "samples{image bird; input bird from @0;show bird;sleep(4000);image bird2[width(bird),height(bird)];int x;x:=0;while(x<width(bird2)) {int y;y:=0;while(y<height(bird2)) {blue(bird2[x,y]):=red(bird[x,y]);green(bird2[x,y]):=blue(bird[x,y]);red(bird2[x,y]):=green(bird[x,y]);alpha(bird2[x,y]):=Z;y:=y+1;};x:=x+1;};show bird2;sleep(4000);}";
		Parser parser = makeParser(input);
		Program p = parser.parse();
		show(p);
		assertEquals(p.toString(),"Program [progName=samples, block=Block [decsOrStatements=[Declaration [type=KW_image, name=bird, width=null, height=null], StatementInput [destName=bird, e=ExpressionIntegerLiteral [value=0]], ShowStatement [e=ExpressionIdent [name=bird]], StatementSleep [duration=ExpressionIntegerLiteral [value=4000]], Declaration [type=KW_image, name=bird2, width=ExpressionFunctionApp [function=KW_width, e=ExpressionIdent [name=bird]], height=ExpressionFunctionApp [function=KW_height, e=ExpressionIdent [name=bird]]], Declaration [type=KW_int, name=x, width=null, height=null], StatementAssign [lhs=LHSIdent [name=x], e=ExpressionIntegerLiteral [value=0]], StatementWhile [guard=ExpressionBinary [leftExpression=ExpressionIdent [name=x], op=OP_LT, rightExpression=ExpressionFunctionApp [function=KW_width, e=ExpressionIdent [name=bird2]]], b=Block [decsOrStatements=[Declaration [type=KW_int, name=y, width=null, height=null], StatementAssign [lhs=LHSIdent [name=y], e=ExpressionIntegerLiteral [value=0]], StatementWhile [guard=ExpressionBinary [leftExpression=ExpressionIdent [name=y], op=OP_LT, rightExpression=ExpressionFunctionApp [function=KW_height, e=ExpressionIdent [name=bird2]]], b=Block [decsOrStatements=[StatementAssign [lhs=LHSSample [name=bird2, pixelSelector=PixelSelector [ex=ExpressionIdent [name=x], ey=ExpressionIdent [name=y]], color=KW_blue], e=ExpressionFunctionApp [function=KW_red, e=ExpressionPixel [name=bird, pixelSelector=PixelSelector [ex=ExpressionIdent [name=x], ey=ExpressionIdent [name=y]]]]], StatementAssign [lhs=LHSSample [name=bird2, pixelSelector=PixelSelector [ex=ExpressionIdent [name=x], ey=ExpressionIdent [name=y]], color=KW_green], e=ExpressionFunctionApp [function=KW_blue, e=ExpressionPixel [name=bird, pixelSelector=PixelSelector [ex=ExpressionIdent [name=x], ey=ExpressionIdent [name=y]]]]], StatementAssign [lhs=LHSSample [name=bird2, pixelSelector=PixelSelector [ex=ExpressionIdent [name=x], ey=ExpressionIdent [name=y]], color=KW_red], e=ExpressionFunctionApp [function=KW_green, e=ExpressionPixel [name=bird, pixelSelector=PixelSelector [ex=ExpressionIdent [name=x], ey=ExpressionIdent [name=y]]]]], StatementAssign [lhs=LHSSample [name=bird2, pixelSelector=PixelSelector [ex=ExpressionIdent [name=x], ey=ExpressionIdent [name=y]], color=KW_alpha], e=ExpressionPredefinedName [name=KW_Z]], StatementAssign [lhs=LHSIdent [name=y], e=ExpressionBinary [leftExpression=ExpressionIdent [name=y], op=OP_PLUS, rightExpression=ExpressionIntegerLiteral [value=1]]]]]], StatementAssign [lhs=LHSIdent [name=x], e=ExpressionBinary [leftExpression=ExpressionIdent [name=x], op=OP_PLUS, rightExpression=ExpressionIntegerLiteral [value=1]]]]]], ShowStatement [e=ExpressionIdent [name=bird2]], StatementSleep [duration=ExpressionIntegerLiteral [value=4000]]]]]");
	}
	
	/**
	 * Smallest legal program.
	 *   
	 * @throws LexicalException
	 * @throws SyntaxException 
	 */
	@Test
	public void testSmallest() throws LexicalException, SyntaxException {
		String input = "b{}";  
		Parser parser = makeParser(input);
		Program p = parser.parse();
		show(p);
		assertEquals("b", p.progName);
		assertEquals(0, p.block.decsOrStatements.size());
	}	
	
	
	/**
	 * Checks that an element in a block is a declaration with the given type and name.
	 * The element to check is indicated by the value of index.
	 * 
	 * @param block
	 * @param index
	 * @param type
	 * @param name
	 * @return
	 */
	Declaration checkDec(Block block, int index, Kind type,
			String name) {
		ASTNode node = block.decOrStatement(index);
		assertEquals(Declaration.class, node.getClass());
		Declaration dec = (Declaration) node;
		assertEquals(type, dec.type);
		assertEquals(name, dec.name);
		return dec;
	}	
	
	@Test
	public void testDec0() throws LexicalException, SyntaxException {
		String input = "b{int c; image j;}";
		Parser parser = makeParser(input);
		Program p = parser.parse();
		show(p);	
		checkDec(p.block, 0, Kind.KW_int, "c");
		checkDec(p.block, 1, Kind.KW_image, "j");
	}
	
	@Test
	public void testDec1() throws LexicalException, SyntaxException {
		String input = "b{image ident[ident1+ident2,ident3*ident4];}";
		Parser parser = makeParser(input);
		Program p = parser.parse();
		show(p);
		assertEquals("b", p.progName);
		assertEquals(1, p.block.decsOrStatements.size());
		checkDec(p.block, 0, Kind.KW_image, "ident");
		Declaration node = (Declaration) p.block.decOrStatement(0);
		assertEquals(ExpressionBinary.class, node.width.getClass());
		ExpressionBinary width = (ExpressionBinary) node.width;
		assertEquals(ExpressionIdent.class, width.leftExpression.getClass());
		ExpressionIdent left1 = (ExpressionIdent) width.leftExpression;
		assertEquals("ident1", left1.name);
		assertEquals(Kind.OP_PLUS, width.op);
		assertEquals(ExpressionIdent.class, width.rightExpression.getClass());
		ExpressionIdent right1 = (ExpressionIdent) width.rightExpression;
		assertEquals("ident2", right1.name);
		assertEquals(ExpressionBinary.class, node.height.getClass());
		ExpressionBinary height = (ExpressionBinary) node.height;
		assertEquals(ExpressionIdent.class, height.leftExpression.getClass());
		ExpressionIdent left2 = (ExpressionIdent) height.leftExpression;
		assertEquals("ident3", left2.name);
		assertEquals(Kind.OP_TIMES, height.op);
		assertEquals(ExpressionIdent.class, height.rightExpression.getClass());
		ExpressionIdent right2 = (ExpressionIdent) height.rightExpression;
		assertEquals("ident4", right2.name);
	}
	
	@Test
	public void testStatementInput() throws LexicalException, SyntaxException {
		String input = "input h from @0";
		Parser parser = makeParser(input);
		Statement e = parser.statement();  //call expression here instead of parse
		show(e);
		assertEquals(StatementInput.class, e.getClass());
		StatementInput b = (StatementInput)e;
		assertEquals(ExpressionIntegerLiteral.class, b.e.getClass());
		assertEquals("h", b.destName);
		ExpressionIntegerLiteral exp = (ExpressionIntegerLiteral) b.e;
		assertEquals(0, exp.value);
	}
	
	@Test
	public void testStatementShow() throws LexicalException, SyntaxException {
		String input = "show h";
		Parser parser = makeParser(input);
		Statement e = parser.statement();  //call expression here instead of parse
		show(e);
		assertEquals(StatementShow.class, e.getClass());
		StatementShow b = (StatementShow)e;
		assertEquals(ExpressionIdent.class, b.e.getClass());
		ExpressionIdent exp = (ExpressionIdent) b.e;
		assertEquals("h", exp.name);
	}
	
	@Test
	public void testStatementSleep() throws LexicalException, SyntaxException {
		String input = "sleep(4000)";
		Parser parser = makeParser(input);
		Statement e = parser.statement();  //call expression here instead of parse
		show(e);
		assertEquals(StatementSleep.class, e.getClass());
		StatementSleep b = (StatementSleep)e;
		assertEquals(ExpressionIntegerLiteral.class, b.duration.getClass());
		ExpressionIntegerLiteral exp = (ExpressionIntegerLiteral) b.duration;
		assertEquals(4000, exp.value);
	}
	
	@Test
	public void testStatementWhile() throws LexicalException, SyntaxException {
		String input = "while(y<height(g)){g[x,y]:=h[y,x];y:=y+1;}";
		Parser parser = makeParser(input);
		Statement e = parser.statement();  //call expression here instead of parse
		show(e);
		assertEquals(StatementWhile.class, e.getClass());
		StatementWhile b = (StatementWhile)e;
		assertEquals(ExpressionBinary.class, b.guard.getClass());
		assertEquals(Block.class, b.b.getClass());
		ExpressionBinary guard = (ExpressionBinary) b.guard;
		assertEquals(ExpressionIdent.class, guard.leftExpression.getClass());
		assertEquals(ExpressionFunctionAppWithExpressionArg.class, guard.rightExpression.getClass());
		Block block = (Block) b.b;
		assertEquals(2, block.decsOrStatements.size());
	}
	
	public void testStatementIf() throws LexicalException, SyntaxException {
		String input = "if(x<width(g)){y:=y+1;}";
		Parser parser = makeParser(input);
		Statement e = parser.statement();  //call expression here instead of parse
		show(e);
		assertEquals(StatementIf.class, e.getClass());
		StatementIf b = (StatementIf)e;
		assertEquals(ExpressionBinary.class, b.guard.getClass());
		assertEquals(Block.class, b.b.getClass());
		ExpressionBinary guard = (ExpressionBinary) b.guard;
		assertEquals(ExpressionIdent.class, guard.leftExpression.getClass());
		assertEquals(ExpressionFunctionAppWithExpressionArg.class, guard.rightExpression.getClass());
		Block block = (Block) b.b;
		assertEquals(1, block.decsOrStatements.size());
	}
	
	@Test
	public void testStatementWrite() throws LexicalException, SyntaxException {
		String input = "write a to b";
		Parser parser = makeParser(input);
		Statement e = parser.statement();  //call expression here instead of parse
		show(e);
		assertEquals(StatementWrite.class, e.getClass());
		StatementWrite b = (StatementWrite)e;
		assertEquals(b.sourceName, "a");
		assertEquals(b.destName, "b");
	}
	
	@Test
	public void testStatementAssignment() throws LexicalException, SyntaxException {
		String input = "green(ident[default_width,default_height]) := <<+a,-b,!c,d>>";
		Parser parser = makeParser(input);
		Statement e = parser.statement();  //call expression here instead of parse
		show(e);
		assertEquals(StatementAssign.class, e.getClass());
		StatementAssign b = (StatementAssign)e;
		assertEquals(LHSSample.class, b.lhs.getClass());
		assertEquals(ExpressionPixelConstructor.class, b.e.getClass());
		LHSSample lhs = (LHSSample)b.lhs;
		assertEquals(lhs.color, KW_green);
		assertEquals(lhs.pixelSelector.getClass(), PixelSelector.class);
		assertEquals(lhs.name, "ident");
		PixelSelector ps = (PixelSelector) lhs.pixelSelector;
		assertEquals(ps.ex.getClass(),ExpressionPredefinedName.class);
		assertEquals(ps.ey.getClass(),ExpressionPredefinedName.class);
		ExpressionPixelConstructor pixel = (ExpressionPixelConstructor) b.e;
		assertEquals(pixel.alpha.getClass(),ExpressionUnary.class);
		ExpressionUnary alpha = (ExpressionUnary) pixel.alpha;
		assertEquals(alpha.op,OP_PLUS);
		assertEquals(alpha.expression.getClass(),ExpressionIdent.class);
		assertEquals(pixel.red.getClass(),ExpressionUnary.class);
		ExpressionUnary red = (ExpressionUnary) pixel.red;
		assertEquals(red.op,OP_MINUS);
		assertEquals(red.expression.getClass(),ExpressionIdent.class);
		assertEquals(pixel.green.getClass(),ExpressionUnary.class);
		ExpressionUnary green = (ExpressionUnary) pixel.green;
		assertEquals(green.op,OP_EXCLAMATION);
		assertEquals(green.expression.getClass(),ExpressionIdent.class);
		assertEquals(pixel.blue.getClass(),ExpressionIdent.class);
		ExpressionIdent blue = (ExpressionIdent) pixel.blue;
		assertEquals(blue.name,"d");
	}
	
	/** This test illustrates how you can test specific grammar elements by themselves by
	 * calling the corresponding parser method directly, instead of calling parse.
	 * This requires that the methods are visible (not private). 
	 * 
	 * @throws LexicalException
	 * @throws SyntaxException
	 */
	
	@Test
	public void testExpressionBinary() throws LexicalException, SyntaxException {
		String input = "x + 2";
		Parser parser = makeParser(input);
		Expression e = parser.expression();  //call expression here instead of parse
		show(e);	
		assertEquals(ExpressionBinary.class, e.getClass());
		ExpressionBinary b = (ExpressionBinary)e;
		assertEquals(ExpressionIdent.class, b.leftExpression.getClass());
		ExpressionIdent left = (ExpressionIdent)b.leftExpression;
		assertEquals("x", left.name);
		assertEquals(ExpressionIntegerLiteral.class, b.rightExpression.getClass());
		ExpressionIntegerLiteral right = (ExpressionIntegerLiteral)b.rightExpression;
		assertEquals(2, right.value);
		assertEquals(OP_PLUS, b.op);
	}

	@Test
	public void testExpressionConditional() throws LexicalException, SyntaxException {
		String input = "x > 5.0 ? a : b";
		Parser parser = makeParser(input);
		Expression e = parser.expression();  //call expression here instead of parse
		show(e);	
		assertEquals(ExpressionConditional.class, e.getClass());
		ExpressionConditional s = (ExpressionConditional)e;
		assertEquals(ExpressionBinary.class, s.guard.getClass());
		ExpressionBinary b = (ExpressionBinary) s.guard;
		ExpressionIdent left = (ExpressionIdent)b.leftExpression;
		assertEquals("x", left.name);
		assertEquals(ExpressionFloatLiteral.class, b.rightExpression.getClass());
		ExpressionFloatLiteral right = (ExpressionFloatLiteral)b.rightExpression;
		assert(Float.compare((float) 5.0, right.value) == 0);
		assertEquals(OP_GT, b.op);
		assertEquals(s.trueExpression.getClass(),ExpressionIdent.class);
		assertEquals(s.falseExpression.getClass(),ExpressionIdent.class);
	}
	
	@Test
	public void testExpressionBinary2() throws LexicalException, SyntaxException {
		String input = "ident[a + b, c % d] | true";
		Parser parser = makeParser(input);
		Expression e = parser.expression();  //call expression here instead of parse
		show(e);	
		assertEquals(ExpressionBinary.class, e.getClass());
		ExpressionBinary b = (ExpressionBinary)e;
		assertEquals(ExpressionPixel.class, b.leftExpression.getClass());
		ExpressionPixel left = (ExpressionPixel)b.leftExpression;
		assertEquals("ident", left.name);
		PixelSelector ps = (PixelSelector) left.pixelSelector;
		ExpressionBinary first = (ExpressionBinary)ps.ex;
		assertEquals(first.leftExpression.getClass(),ExpressionIdent.class);
		assertEquals(first.rightExpression.getClass(),ExpressionIdent.class);
		assertEquals(first.op,OP_PLUS);
		ExpressionBinary second = (ExpressionBinary)ps.ey;
		assertEquals(second.leftExpression.getClass(),ExpressionIdent.class);
		assertEquals(second.rightExpression.getClass(),ExpressionIdent.class);
		assertEquals(second.op,OP_MOD);
		assertEquals(ExpressionBooleanLiteral.class, b.rightExpression.getClass());
		ExpressionBooleanLiteral right = (ExpressionBooleanLiteral)b.rightExpression;
		assertEquals(true, right.value);
		assertEquals(OP_OR, b.op);
	}
	
	@Test
	public void testExpressionFunctionAppWithPixel() throws LexicalException, SyntaxException {
		String input = "atan[a,b] ** c";
		Parser parser = makeParser(input);
		Expression e = parser.expression();  //call expression here instead of parse
		show(e);	
		assertEquals(ExpressionBinary.class, e.getClass());
		ExpressionBinary b = (ExpressionBinary)e;
		assertEquals(ExpressionFunctionAppWithPixel.class, b.leftExpression.getClass());
		ExpressionFunctionAppWithPixel left = (ExpressionFunctionAppWithPixel)b.leftExpression;
		assertEquals(KW_atan, left.name);
		assertEquals(ExpressionIdent.class, left.e0.getClass());
		assertEquals(ExpressionIdent.class, left.e1.getClass());
		assertEquals(ExpressionIdent.class, b.rightExpression.getClass());
		ExpressionIdent right = (ExpressionIdent)b.rightExpression;
		assertEquals("c", right.name);
		assertEquals(OP_POWER, b.op);
	}
}
	

