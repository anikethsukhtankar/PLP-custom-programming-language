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

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import cop5556sp18.Parser;
import cop5556sp18.Scanner;
import cop5556sp18.Parser.SyntaxException;
import cop5556sp18.Scanner.LexicalException;

public class SimpleParserTest {

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
	 * because it lacks an identifier and a block. The test case passes because
	 * it expects an exception
	 *  
	 * @throws LexicalException
	 * @throws SyntaxException 
	 */
	@Test
	public void testEmpty() throws LexicalException, SyntaxException {
		String input = "";  //The input is the empty string.  
		Parser parser = makeParser(input);
		thrown.expect(SyntaxException.class);
		parser.parse();
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
		parser.parse();
	}	
	
	
	//This test should pass in your complete parser.  It will fail in the starter code.
	//Of course, you would want a better error message. 
	@Test
	public void testDec0() throws LexicalException, SyntaxException {
		String input = "b{int c;}";
		Parser parser = makeParser(input);
		parser.parse();
	}
	
	
	@Test
	public void testDemo1() throws LexicalException, SyntaxException {
		String input = "demo1{image h;input h from @0;show h; sleep(4000); image g[width(h),height(h)];int x;x:=0;"
				+ "while(x<width(g)){int y;y:=0;while(y<height(g)){g[x,y]:=h[y,x];y:=y+1;};x:=x+1;};show g;sleep(4000);}";
		Parser parser = makeParser(input);
		parser.parse();
	}
	
	@Test
	public void makeRedImage() throws LexicalException, SyntaxException {
		String input = "makeRedImage{image im[256,256];int x;int y;x:=0;y:=0;while(x<width(im)) {y:=0;while(y<height(im)) {im[x,y]:=<<255,255,0,0>>;y:=y+1;};x:=x+1;};show im;}";
		Parser parser = makeParser(input);
		parser.parse();
	}
	
	@Test
	public void testPolarR2() throws LexicalException, SyntaxException {
		String input = "PolarR2{image im[1024,1024];int x;x:=0;while(x<width(im)) {int y;y:=0;while(y<height(im)) {float p;p:=polar_r[x,y];int r;r:=int(p)%Z;im[x,y]:=<<Z,0,0,r>>;y:=y+1;};x:=x+1;};show im;}";
		Parser parser = makeParser(input);
		parser.parse();
	}

	@Test
	public void testSamples() throws LexicalException, SyntaxException {
		String input = "samples{image bird; input bird from @0;show bird;sleep(4000);image bird2[width(bird),height(bird)];int x;x:=0;while(x<width(bird2)) {int y;y:=0;while(y<height(bird2)) {blue(bird2[x,y]):=red(bird[x,y]);green(bird2[x,y]):=blue(bird[x,y]);red(bird2[x,y]):=green(bird[x,y]);alpha(bird2[x,y]):=Z;y:=y+1;};x:=x+1;};show bird2;sleep(4000);}";
		Parser parser = makeParser(input);
		parser.parse();
	}
	
	@Test
	public void testBlockKeywordError() throws LexicalException, SyntaxException {
		String input = "Z{}";  
		Parser parser = makeParser(input);
		thrown.expect(SyntaxException.class);
		parser.parse();
	}
	
	@Test
	public void testDeclaration1() throws LexicalException, SyntaxException {
		String input = "b{int ident;}";  
		Parser parser = makeParser(input);
		parser.parse();
	}
	
	@Test
	public void testDeclarationFailure1() throws LexicalException, SyntaxException {
		String input = "b{int ident}";  //The input is the empty string.  
		Parser parser = makeParser(input);
		thrown.expect(SyntaxException.class);
		parser.parse();
	}
	
	@Test
	public void testDeclaration2() throws LexicalException, SyntaxException {
		String input = "b{float ident;}";  
		Parser parser = makeParser(input);
		parser.parse();
	}
	
	@Test
	public void testDeclarationFailure2() throws LexicalException, SyntaxException {
		String input = "b{float Z;}";  //The input is the empty string.  
		Parser parser = makeParser(input);
		thrown.expect(SyntaxException.class);
		parser.parse();
	}
	
	@Test
	public void testDeclaration3() throws LexicalException, SyntaxException {
		String input = "b{boolean ident;}";  
		Parser parser = makeParser(input);
		parser.parse();
	}
	
	@Test
	public void testDeclaration4() throws LexicalException, SyntaxException {
		String input = "b{filename ident;}";  
		Parser parser = makeParser(input);
		parser.parse();
	}
	
	@Test
	public void testDeclaration5() throws LexicalException, SyntaxException {
		String input = "b{image ident;}";  
		Parser parser = makeParser(input);
		parser.parse();
	}
	
	@Test
	public void testDeclaration6() throws LexicalException, SyntaxException {
		String input = "b{image ident[ident1+ident2,ident3*ident4];}";  
		Parser parser = makeParser(input);
		parser.parse();
	}
	
	@Test
	public void testStatementInput() throws LexicalException, SyntaxException {
		String input = "b{input ident from @(ident1**ident2);}";  
		Parser parser = makeParser(input);
		parser.parse();
	}
	
	@Test
	public void testStatementInputFailure() throws LexicalException, SyntaxException {
		String input = "b{input from @(ident1*ident2);}";  //The input is the empty string.  
		Parser parser = makeParser(input);
		thrown.expect(SyntaxException.class);
		parser.parse();
	}
	
	@Test
	public void testStatementWrite() throws LexicalException, SyntaxException {
		String input = "b{write ident1 to ident2;}";  
		Parser parser = makeParser(input);
		parser.parse();
	}
	
	@Test
	public void testStatementWriteFailure() throws LexicalException, SyntaxException {
		String input = "b{write a b}";  //The input is the empty string.  
		Parser parser = makeParser(input);
		thrown.expect(SyntaxException.class);
		parser.parse();
	}
	
	@Test
	public void testStatementAssign1() throws LexicalException, SyntaxException {
		String input = "b{ident:=Z;}";  
		Parser parser = makeParser(input);
		parser.parse();
	}
	
	@Test
	public void testStatementAssignFailure() throws LexicalException, SyntaxException {
		String input = "b{ident1:=ident2:=ident3;}";  //The input is the empty string.  
		Parser parser = makeParser(input);
		thrown.expect(SyntaxException.class);
		parser.parse();
	}
	
	@Test
	public void testStatementAssign2() throws LexicalException, SyntaxException {
		String input = "b{ident[default_height,default_width]:=<<4,4.3,true,42>>;}";  
		Parser parser = makeParser(input);
		parser.parse();
	}
	
	@Test
	public void testStatementAssign3() throws LexicalException, SyntaxException {
		String input = "b{alpha(ident[!true,+61]):=-33;}";  
		Parser parser = makeParser(input);
		parser.parse();
	}
	
	@Test
	public void testStatementWhile() throws LexicalException, SyntaxException {
		String input = "b{while(sin(cos(log(3.14)))) {};}";  
		Parser parser = makeParser(input);
		parser.parse();
	}
	
	@Test
	public void testStatementWhileFailure() throws LexicalException, SyntaxException {
		String input = "b{while(ident:=ident2) {};}";  //The input is the empty string.  
		Parser parser = makeParser(input);
		thrown.expect(SyntaxException.class);
		parser.parse();
	}
	
	@Test
	public void testStatementIf() throws LexicalException, SyntaxException {
		String input = "b{if(int[cart_x(a),cart_y(b)]) {};}";  
		Parser parser = makeParser(input);
		parser.parse();
	}
	
	@Test
	public void testStatementIfFailure() throws LexicalException, SyntaxException {
		String input = "b{if(;) {};}";  //The input is the empty string.  
		Parser parser = makeParser(input);
		thrown.expect(SyntaxException.class);
		parser.parse();
	}
	
	@Test
	public void testStatementIfFailure2() throws LexicalException, SyntaxException {
		String input = "b{if(expr) {}}";  //The input is the empty string.  
		Parser parser = makeParser(input);
		thrown.expect(SyntaxException.class);
		parser.parse();
	}
	
	@Test
	public void testStatementShow() throws LexicalException, SyntaxException {
		String input = "b{show width(3.14);}";  
		Parser parser = makeParser(input);
		parser.parse();
	}
	
	@Test
	public void testStatementShowFailure() throws LexicalException, SyntaxException {
		String input = "b{show width(3.14) to height(3.14);}";  //The input is the empty string.  
		Parser parser = makeParser(input);
		thrown.expect(SyntaxException.class);
		parser.parse();
	}
	
	@Test
	public void testStatementSleep() throws LexicalException, SyntaxException {
		String input = "b{sleep height(3.14);}";  
		Parser parser = makeParser(input);
		parser.parse();
	}
	
	@Test
	public void testStatementSleepFailure() throws LexicalException, SyntaxException {
		String input = "b{show ident==1{};}";  //The input is the empty string.  
		Parser parser = makeParser(input);
		thrown.expect(SyntaxException.class);
		parser.parse();
	}
	
	@Test
	public void testExpression() throws LexicalException, SyntaxException {
		String input = "b{sleep expr**expr%expr*expr/expr+expr-expr>expr<expr>=expr<=expr!=expr==expr&expr|expr?expr:expr;}";  
		Parser parser = makeParser(input);
		parser.parse();
	}
	
	@Test
	public void testExpressionFailure() throws LexicalException, SyntaxException {
		String input = "b{show *-expr**expr**expr;}";   
		Parser parser = makeParser(input);
		thrown.expect(SyntaxException.class);
		parser.parse();
	}
	
	@Test
	public void testFuntions() throws LexicalException, SyntaxException {
		String input = "b{if(sin(cos(atan(abs(<<ident1,ident2,1,1.0>>)))) * log(3.14)) {ident:=cart_x(a)|cart_y(b)|polar_a(c)|polar_r(d);};}";  
		Parser parser = makeParser(input);
		parser.parse();
	}
	
	@Test
	public void testFuntionFailure() throws LexicalException, SyntaxException {
		String input = "b{if(sin) {};}";  
		Parser parser = makeParser(input);
		thrown.expect(SyntaxException.class);
		parser.parse();
	}
	
	@Test
	public void testNameOnly() throws LexicalException, SyntaxException {
		String input = "b";  
		Parser parser = makeParser(input);
		thrown.expect(SyntaxException.class);
		parser.parse();
	}

	@Test
	public void testNoBraces() throws LexicalException, SyntaxException {
		String input = "b int k;";
		Parser parser = makeParser(input);
		thrown.expect(SyntaxException.class);
		parser.parse();
	}
	
	@Test
	public void testExpressionKeyword() throws SyntaxException, LexicalException {
		String input = "b{if(Z-old) {};}";
		Parser parser = makeParser(input);
		parser.parse();
	}
	
	@Test
	public void testOrExpression() throws SyntaxException, LexicalException {
		String input = "b{if(g+h == true ? h : j) {};}";
		Parser parser = makeParser(input);
		parser.parse();
	}
	
	@Test
	public void testCommentBetween() throws SyntaxException, LexicalException {
		
		String input = "b{sleep /*this is a comment*/ 4000;}"; 
		Parser parser = makeParser(input);
		parser.parse();
		
	}
	
	
	@Test
	public void testBooleanExp() throws SyntaxException, LexicalException {
		String input = "prog{boolean ident1; boolean ident2; k := ident1 | ident2 & ident1;}"; 
		Parser parser = makeParser(input);
		parser.parse();	
	}
	
	@Test
	public void testFilenameExp() throws SyntaxException, LexicalException {
		String input = "prog{filename ident1; filename ident2; k := ident1 | ident2 & ident1;}"; 
		Parser parser = makeParser(input);
		parser.parse();	
	}
	
	@Test
	public void testColorFunc() throws SyntaxException, LexicalException {
		String input = "prog{sleep alpha(pi)*red[green(x),blue(y)];}"; 
		Parser parser = makeParser(input);
		parser.parse();	
	}
}
	

