 /**
 * JUunit tests for the Scanner for the class project in COP5556 Programming Language Principles 
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

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import cop5556sp18.Scanner.LexicalException;
import cop5556sp18.Scanner.Token;
import static cop5556sp18.Scanner.Kind.*;

public class ScannerTest {

	//set Junit to be able to catch exceptions
	@Rule
	public ExpectedException thrown = ExpectedException.none();

	
	//To make it easy to print objects and turn this output on and off
	static boolean doPrint = true;
	private void show(Object input) {
		if (doPrint) {
			System.out.println(input.toString());
		}
	}

	/**
	 *Retrieves the next token and checks that it is an EOF token. 
	 *Also checks that this was the last token.
	 *
	 * @param scanner
	 * @return the Token that was retrieved
	 */
	
	Token checkNextIsEOF(Scanner scanner) {
		Scanner.Token token = scanner.nextToken();
		assertEquals(Scanner.Kind.EOF, token.kind);
		assertFalse(scanner.hasTokens());
		return token;
	}


	/**
	 * Retrieves the next token and checks that its kind, position, length, line, and position in line
	 * match the given parameters.
	 * 
	 * @param scanner
	 * @param kind
	 * @param pos
	 * @param length
	 * @param line
	 * @param pos_in_line
	 * @return  the Token that was retrieved
	 */
	Token checkNext(Scanner scanner, Scanner.Kind kind, int pos, int length, int line, int pos_in_line) {
		Token t = scanner.nextToken();
		assertEquals(kind, t.kind);
		assertEquals(pos, t.pos);
		assertEquals(length, t.length);
		assertEquals(line, t.line());
		assertEquals(pos_in_line, t.posInLine());
		return t;
	}

	/**
	 * Retrieves the next token and checks that its kind and length match the given
	 * parameters.  The position, line, and position in line are ignored.
	 * 
	 * @param scanner
	 * @param kind
	 * @param length
	 * @return  the Token that was retrieved
	 */
	Token checkNext(Scanner scanner, Scanner.Kind kind, int length) {
		Token t = scanner.nextToken();
		assertEquals(kind, t.kind);
		assertEquals(length, t.length);
		return t;
	}
	


	/**
	 * Simple test case with an empty program.  The only Token will be the EOF Token.
	 *   
	 * @throws LexicalException
	 */
	@Test
	public void testEmpty() throws LexicalException {
		String input = "";  //The input is the empty string.  This is legal
		show(input);        //Display the input 
		Scanner scanner = new Scanner(input).scan();  //Create a Scanner and initialize it
		show(scanner);   //Display the Scanner
		checkNextIsEOF(scanner);  //Check that the only token is the EOF token.
	}
	
	/**
	 * Test illustrating how to put a new line in the input program and how to
	 * check content of tokens.
	 * 
	 * Because we are using a Java String literal for input, we use \n for the
	 * end of line character. (We should also be able to handle \n, \r, and \r\n
	 * properly.)
	 * 
	 * Note that if we were reading the input from a file, the end of line 
	 * character would be inserted by the text editor.
	 * Showing the input will let you check your input is 
	 * what you think it is.
	 * 
	 * @throws LexicalException
	 */
	@Test
	public void testSemi() throws LexicalException {
		String input = ";;\n;;";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner, SEMI, 0, 1, 1, 1);
		checkNext(scanner, SEMI, 1, 1, 1, 2);
		checkNext(scanner, SEMI, 3, 1, 2, 1);
		checkNext(scanner, SEMI, 4, 1, 2, 2);
		checkNextIsEOF(scanner);
	}
	
	@Test
	public void testIdentifier() throws LexicalException {
		String input = ";;\nalpha alpha$t_ if";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner, SEMI, 0, 1, 1, 1);
		checkNext(scanner, SEMI, 1, 1, 1, 2);
		checkNext(scanner, KW_alpha, 3, 5, 2, 1);
		checkNext(scanner, IDENTIFIER, 9, 8, 2, 7);
		checkNext(scanner, KW_if, 18, 2, 2, 16);
		checkNextIsEOF(scanner);
	}
	
	@Test
	public void testIdentifier2() throws LexicalException {
		String input = "\r\n1ABC ";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner, INTEGER_LITERAL, 2, 1, 2, 1);
		checkNext(scanner, IDENTIFIER, 3, 3, 2, 2);
		checkNextIsEOF(scanner);
	}
	
	@Test
	public void testIdentifier3() throws LexicalException {
		String input = "\r\nsinZ atrueb ";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner, IDENTIFIER, 2, 4, 2, 1);
		checkNext(scanner, IDENTIFIER, 7, 6, 2, 6);
		checkNextIsEOF(scanner);
	}
	
	@Test
	public void testDot2() throws LexicalException {
		String input = "A.B..";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner, IDENTIFIER, 0, 1, 1, 1);
		checkNext(scanner, DOT, 1, 1, 1, 2);
		checkNext(scanner, IDENTIFIER, 2, 1, 1, 3);
		checkNext(scanner, DOT, 3, 1, 1, 4);
		checkNext(scanner, DOT, 4, 1, 1, 5);
		checkNextIsEOF(scanner);
	}
	
	@Test
	public void finiteFloatTest() throws LexicalException {
		String input = "340000000000000000000000000000000000000000000000000";
		show(input);
		thrown.expect(LexicalException.class);  //Tell JUnit to expect a LexicalException
		try {
			Scanner scanner = new Scanner(input).scan();
			show(scanner);
		} catch (LexicalException e) {  //Catch the exception
			show(e);                    //Display it
			assertEquals(0,e.getPos()); //Check that it occurred in the expected position
			throw e;                    //Rethrow exception so JUnit will see it
		}
	}
	
	@Test
	public void testFloat_Literal2() throws LexicalException {
		String input = ".1.1";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner, FLOAT_LITERAL, 0, 2, 1, 1);
		checkNext(scanner, FLOAT_LITERAL, 2, 2, 1, 3);
		checkNextIsEOF(scanner);
	}
	
	@Test
	public void testFloat_Literal3() throws LexicalException {
		String input = ".0 0. 003.";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner, FLOAT_LITERAL, 0, 2, 1, 1);
		checkNext(scanner, FLOAT_LITERAL, 3, 2, 1, 4);
		checkNext(scanner, INTEGER_LITERAL, 6, 1, 1, 7);
		checkNext(scanner, INTEGER_LITERAL, 7, 1, 1, 8);
		checkNext(scanner, FLOAT_LITERAL, 8, 2, 1, 9);
		checkNextIsEOF(scanner);
	}
	
	@Test
	public void testMultipleEquals() throws LexicalException {
		String input = "====";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner, OP_EQ, 0, 2, 1, 1);
		checkNext(scanner, OP_EQ, 2, 2, 1, 3);
		checkNextIsEOF(scanner);
	}
	
	@Test
	public void failIdentifier() throws LexicalException {
		String input = "_abc $abc";
		show(input);
		thrown.expect(LexicalException.class);  //Tell JUnit to expect a LexicalException
		try {
			new Scanner(input).scan();
		} catch (LexicalException e) {  //Catch the exception
			show(e);                    //Display it
			assertEquals(0,e.getPos()); //Check that it occurred in the expected position
			throw e;                    //Rethrow exception so JUnit will see it
		}
	}
	
	@Test
	public void testKW_alpha() throws LexicalException {
		String input = ";;\nalpha";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner, SEMI, 0, 1, 1, 1);
		checkNext(scanner, SEMI, 1, 1, 1, 2);
		checkNext(scanner, KW_alpha, 3, 5, 2, 1);
		checkNextIsEOF(scanner);
	}
	
	@Test
	public void testKW_Z() throws LexicalException {
		String input = ";;\nZ;";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner, SEMI, 0, 1, 1, 1);
		checkNext(scanner, SEMI, 1, 1, 1, 2);
		checkNext(scanner, KW_Z, 3, 1, 2, 1);
		checkNext(scanner, SEMI, 4, 1, 2, 2);
		checkNextIsEOF(scanner);
	}
	
	@Test
	public void testLine_Terminator() throws LexicalException {
		String input = ";; \t\f\r\ncart_x;";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner, SEMI, 0, 1, 1, 1);
		checkNext(scanner, SEMI, 1, 1, 1, 2);
		checkNext(scanner, KW_cart_x, 7, 6, 2, 1);
		checkNext(scanner, SEMI, 13, 1, 2, 7);
		checkNextIsEOF(scanner);
	}
	
	@Test
	public void testComment() throws LexicalException {
		String input = "/*ab*cd***/fghy*/abc";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner, IDENTIFIER, 11, 4, 1, 12);
		checkNext(scanner, OP_TIMES, 15, 1, 1, 16);
		checkNext(scanner, OP_DIV, 16, 1, 1, 17);
		checkNext(scanner, IDENTIFIER, 17, 3, 1, 18);
		checkNextIsEOF(scanner);
	}
	
	@Test
	public void failComment() throws LexicalException {
		String input = "/* abc**";
		show(input);
		thrown.expect(LexicalException.class);  //Tell JUnit to expect a LexicalException
		try {
			new Scanner(input).scan();
		} catch (LexicalException e) {  //Catch the exception
			show(e);                    //Display it
			assertEquals(8,e.getPos()); //Check that it occurred in the expected position
			throw e;                    //Rethrow exception so JUnit will see it
		}
	}
	
	@Test
	public void illegalCharComment() throws LexicalException {
		String input = "/*;;if*/~";
		show(input);
		thrown.expect(LexicalException.class);  //Tell JUnit to expect a LexicalException
		try {
			new Scanner(input).scan();
		} catch (LexicalException e) {  //Catch the exception
			show(e);                    //Display it
			assertEquals(8,e.getPos()); //Check that it occurred in the expected position
			throw e;                    //Rethrow exception so JUnit will see it
		}
	}
	
	@Test
	public void testInteger_Literal() throws LexicalException {
		String input = "0123 5601 789654";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner, INTEGER_LITERAL, 0, 1, 1, 1);
		checkNext(scanner, INTEGER_LITERAL, 1, 3, 1, 2);
		checkNext(scanner, INTEGER_LITERAL, 5, 4, 1, 6);
		checkNext(scanner, INTEGER_LITERAL, 10, 6, 1, 11);
		checkNextIsEOF(scanner);
	}
	
	@Test
	public void testBoolean_Literal() throws LexicalException {
		String input = "true false trueer";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner, BOOLEAN_LITERAL, 0, 4, 1, 1);
		checkNext(scanner, BOOLEAN_LITERAL, 5, 5, 1, 6);
		checkNext(scanner, IDENTIFIER, 11, 6, 1, 12);
		checkNextIsEOF(scanner);
	}
	
	@Test
	public void testFloat_Literal() throws LexicalException {
		String input = "2.3 4. .7 2..7";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner, FLOAT_LITERAL, 0, 3, 1, 1);
		checkNext(scanner, FLOAT_LITERAL, 4, 2, 1, 5);
		checkNext(scanner, FLOAT_LITERAL, 7, 2, 1, 8);
		checkNext(scanner, FLOAT_LITERAL, 10, 2, 1, 11);
		checkNext(scanner, FLOAT_LITERAL, 12, 2, 1, 13);
		checkNextIsEOF(scanner);
	}
	
	@Test
	public void failFloat_Bound() throws LexicalException {
		String input = "999999999999999999999999999999999999999.999999999999999999999";
		show(input);
		thrown.expect(LexicalException.class);  //Tell JUnit to expect a LexicalException
		try {
			new Scanner(input).scan();
		} catch (LexicalException e) {  //Catch the exception
			show(e);                    //Display it
			assertEquals(0,e.getPos()); //Check that it occurred in the expected position
			throw e;                    //Rethrow exception so JUnit will see it
		}
	}
	
	@Test
	public void failInteger_Bound() throws LexicalException {
		String input = "123456789123456789123456789";
		show(input);
		thrown.expect(LexicalException.class);  //Tell JUnit to expect a LexicalException
		try {
			new Scanner(input).scan();
		} catch (LexicalException e) {  //Catch the exception
			show(e);                    //Display it
			assertEquals(0,e.getPos()); //Check that it occurred in the expected position
			throw e;                    //Rethrow exception so JUnit will see it
		}
	}
	
	@Test
	public void testDot() throws LexicalException {
		String input = ".72 ..";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner, FLOAT_LITERAL, 0, 3, 1, 1);
		checkNext(scanner, DOT, 4, 1, 1, 5);
		checkNext(scanner, DOT, 5, 1, 1, 6);
		checkNextIsEOF(scanner);
	}
	
	@Test
	public void testColon() throws LexicalException {
		String input = "::= :=: :abc a:bc :=";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner, OP_COLON, 0, 1, 1, 1);
		checkNext(scanner, OP_ASSIGN, 1, 2, 1, 2);
		checkNext(scanner, OP_ASSIGN, 4, 2, 1, 5);
		checkNext(scanner, OP_COLON, 6, 1, 1, 7);
		checkNext(scanner, OP_COLON, 8, 1, 1, 9);
		checkNext(scanner, IDENTIFIER, 9, 3, 1, 10);
		checkNext(scanner, IDENTIFIER, 13, 1, 1, 14);
		checkNext(scanner, OP_COLON, 14, 1, 1, 15);
		checkNext(scanner, IDENTIFIER, 15, 2, 1, 16);
		checkNext(scanner, OP_ASSIGN, 18, 2, 1, 19);
		checkNextIsEOF(scanner);
	}
	
	@Test
	public void testEq() throws LexicalException {
		String input = "== a==bc==";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner, OP_EQ, 0, 2, 1, 1);
		checkNext(scanner, IDENTIFIER, 3, 1, 1, 4);
		checkNext(scanner, OP_EQ, 4, 2, 1, 5);
		checkNext(scanner, IDENTIFIER, 6, 2, 1, 7);
		checkNext(scanner, OP_EQ, 8, 2, 1, 9);
		checkNextIsEOF(scanner);
	}
	
	@Test
	public void testExclaimation() throws LexicalException {
		String input = "!= a!bc! !!";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner, OP_NEQ, 0, 2, 1, 1);
		checkNext(scanner, IDENTIFIER, 3, 1, 1, 4);
		checkNext(scanner, OP_EXCLAMATION, 4, 1, 1, 5);
		checkNext(scanner, IDENTIFIER, 5, 2, 1, 6);
		checkNext(scanner, OP_EXCLAMATION, 7, 1, 1, 8);
		checkNext(scanner, OP_EXCLAMATION, 9, 1, 1, 10);
		checkNext(scanner, OP_EXCLAMATION, 10, 1, 1, 11);
		checkNextIsEOF(scanner);
	}
	
	@Test
	public void testTimes() throws LexicalException {
		String input = "** e*00* **";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner, OP_POWER, 0, 2, 1, 1);
		checkNext(scanner, IDENTIFIER, 3, 1, 1, 4);
		checkNext(scanner, OP_TIMES, 4, 1, 1, 5);
		checkNext(scanner, INTEGER_LITERAL, 5, 1, 1, 6);
		checkNext(scanner, INTEGER_LITERAL, 6, 1, 1, 7);
		checkNext(scanner, OP_TIMES, 7, 1, 1, 8);
		checkNext(scanner, OP_POWER, 9, 2, 1, 10);
		checkNextIsEOF(scanner);
	}
	
	@Test
	public void failIllegalEq() throws LexicalException {
		String input = ":==";
		show(input);
		thrown.expect(LexicalException.class);  //Tell JUnit to expect a LexicalException
		try {
			new Scanner(input).scan();
		} catch (LexicalException e) {  //Catch the exception
			show(e);                    //Display it
			assertEquals(3,e.getPos()); //Check that it occurred in the expected position
			throw e;                    //Rethrow exception so JUnit will see it
		}
	}
	

	
	/**
	 * This example shows how to test that your scanner is behaving when the
	 * input is illegal.  In this case, we are giving it an illegal character '~' in position 2
	 * 
	 * The example shows catching the exception that is thrown by the scanner,
	 * looking at it, and checking its contents before rethrowing it.  If caught
	 * but not rethrown, then JUnit won't get the exception and the test will fail.  
	 * 
	 * The test will work without putting the try-catch block around 
	 * new Scanner(input).scan(); but then you won't be able to check 
	 * or display the thrown exception.
	 * 
	 * @throws LexicalException
	 */
	@Test
	public void failIllegalChar() throws LexicalException {
		String input = ";;~";
		show(input);
		thrown.expect(LexicalException.class);  //Tell JUnit to expect a LexicalException
		try {
			new Scanner(input).scan();
		} catch (LexicalException e) {  //Catch the exception
			show(e);                    //Display it
			assertEquals(2,e.getPos()); //Check that it occurred in the expected position
			throw e;                    //Rethrow exception so JUnit will see it
		}
	}




	@Test
	public void testParens() throws LexicalException {
		String input = "()[] }{";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner, LPAREN, 0, 1, 1, 1);
		checkNext(scanner, RPAREN, 1, 1, 1, 2);
		checkNext(scanner, LSQUARE, 2, 1, 1, 3);
		checkNext(scanner, RSQUARE, 3, 1, 1, 4);
		checkNext(scanner, RBRACE, 5, 1, 1, 6);
		checkNext(scanner, LBRACE, 6, 1, 1, 7);
		checkNextIsEOF(scanner);
	}
	
	@Test
	public void testCommentLine() throws LexicalException {
		String input = "/*\n*/ab";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner, IDENTIFIER, 5, 2, 2, 3);
		checkNextIsEOF(scanner);
	}
	

	
}
	

