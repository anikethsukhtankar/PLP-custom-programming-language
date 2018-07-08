/**
 * Starter code for CodeGenerator.java used n the class project in COP5556 Programming Language Principles 
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

import java.util.ArrayList;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import cop5556sp18.Scanner.Kind;
import cop5556sp18.Types.Type;
import cop5556sp18.AST.ASTNode;
import cop5556sp18.AST.ASTVisitor;
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

import cop5556sp18.CodeGenUtils;

public class CodeGenerator implements ASTVisitor, Opcodes {

	/**
	 * All methods and variable static.
	 */
	
	private static class LocalVarInfo{
		String name; 
		String desc; 
		Label start;
		Label end; 
		int index;
		public LocalVarInfo(String name, String desc, Label start, Label end, int index) {
			this.name = name;
			this.desc = desc;
			this.start = start;
			this.end = end;
			this.index = index;
		}
	}

	static final int Z = 255;

	ClassWriter cw;
	String className;
	String classDesc;
	String sourceFileName;
	int slot = 1;
	ArrayList<LocalVarInfo> localvars = new ArrayList<>();

	MethodVisitor mv; // visitor of method currently under construction

	/** Indicates whether genPrint and genPrintTOS should generate code. */
	final boolean DEVEL;
	final boolean GRADE;

	final int defaultWidth;
	final int defaultHeight;
	// final boolean itf = false;
	/**
	 * @param DEVEL
	 *            used as parameter to genPrint and genPrintTOS
	 * @param GRADE
	 *            used as parameter to genPrint and genPrintTOS
	 * @param sourceFileName
	 *            name of source file, may be null.
	 * @param defaultWidth
	 *            default width of images
	 * @param defaultHeight
	 *            default height of images
	 */
	public CodeGenerator(boolean DEVEL, boolean GRADE, String sourceFileName,
			int defaultWidth, int defaultHeight) {
		super();
		this.DEVEL = DEVEL;
		this.GRADE = GRADE;
		this.sourceFileName = sourceFileName;
		this.defaultWidth = defaultWidth;
		this.defaultHeight = defaultHeight;
	}
	
	@Override
	public Object visitBlock(Block block, Object arg) throws Exception {
		// add label before first instruction
		Label blockStart = new Label();
		Label blockEnd = new Label();
		mv.visitLabel(blockStart);
		for (ASTNode node : block.decsOrStatements) {
			if(node instanceof Declaration) {
				Declaration d = (Declaration) node;
				Label varStart = new Label();
				mv.visitLabel(varStart);
				d.slot = slot;
				slot++;
				Label start = varStart;
				Label end = blockEnd;
				String desc = "";
				switch(Types.getType(d.type)) {
					case BOOLEAN:
						desc = "Z";
						break;
					case INTEGER:
						desc = "I";
						break;
					case FLOAT:
						desc = "F";
						break;
					case FILE:
						desc = "Ljava/lang/String;";
						break;
					case IMAGE:
						desc = "Ljava/awt/image/BufferedImage;";
						break;
					default:
						break;
				} 
				localvars.add(new LocalVarInfo(d.name,desc,start,end,d.slot));
				node.visit(this,arg);
			}
			else {
				node.visit(this, arg);
			}
		}
		// adds label at end of code
		mv.visitLabel(blockEnd);
		return null;
	}

	@Override
	public Object visitDeclaration(Declaration declaration, Object arg)
			throws Exception {
		if(Types.getType(declaration.type) == Type.IMAGE)
		{
			Expression width = declaration.width;
			Expression height = declaration.height;
			if(width != null && height != null) {
				declaration.width.visit(this, arg);
				declaration.height.visit(this, arg);
			}
			else {
				mv.visitLdcInsn(defaultWidth);
				mv.visitLdcInsn(defaultHeight);
			}
			mv.visitMethodInsn(INVOKESTATIC, "cop5556sp18/RuntimeImageSupport", "makeImage",
					"(II)Ljava/awt/image/BufferedImage;", false);
			mv.visitVarInsn(ASTORE,declaration.slot);
		}
		return null;
	}

	@Override
	public Object visitExpressionConditional(
			ExpressionConditional expressionConditional, Object arg)
			throws Exception {
		expressionConditional.guard.visit(this,arg);
		Label l2 = new Label();
		Label l3 = new Label();
		mv.visitJumpInsn(IFEQ, l2);
		
		expressionConditional.trueExpression.visit(this,arg);
		mv.visitJumpInsn(GOTO, l3);
		
		mv.visitLabel(l2);
		
		expressionConditional.falseExpression.visit(this,arg);
		
		mv.visitLabel(l3);
		return null;
	}
	
	@Override
	public Object visitStatementIf(StatementIf statementIf, Object arg)
			throws Exception {
		statementIf.guard.visit(this,arg);
		Label l2 = new Label();
		mv.visitJumpInsn(IFEQ, l2);
		
		statementIf.b.visit(this,arg);
		
		mv.visitLabel(l2);
		return null;
	}

	@Override
	public Object visitStatementWhile(StatementWhile statementWhile, Object arg)
			throws Exception {
		Label l2 = new Label();
		mv.visitJumpInsn(GOTO, l2);
		Label l3 = new Label();
		mv.visitLabel(l3);
		
		statementWhile.b.visit(this,arg);
		
		mv.visitLabel(l2);
		statementWhile.guard.visit(this,arg);
		mv.visitJumpInsn(IFNE, l3);
		return null;
	}
	
	@Override
	public Object visitLHSSample(LHSSample lhsSample, Object arg)
			throws Exception {
		int index = lhsSample.dec.slot;
		mv.visitVarInsn(ALOAD, index);
		lhsSample.pixelSelector.visit(this,arg);
		Kind kind = lhsSample.color;
		switch(kind) {
			case KW_alpha:
				mv.visitInsn(ICONST_0);
				break;
			case KW_red:
				mv.visitInsn(ICONST_1);
				break;
			case KW_green:
				mv.visitInsn(ICONST_2);
				break;
			case KW_blue:
				mv.visitInsn(ICONST_3);
				break;
			default:
				break;
		}
		mv.visitMethodInsn(INVOKESTATIC, "cop5556sp18/RuntimeImageSupport", "updatePixelColor", "(ILjava/awt/image/BufferedImage;III)V", false);
		return null;
	}
	
	@Override
	public Object visitLHSPixel(LHSPixel lhsPixel, Object arg)
			throws Exception {
		int index = lhsPixel.dec.slot;
		mv.visitVarInsn(ALOAD, index);
		lhsPixel.pixelSelector.visit(this,arg);
		mv.visitMethodInsn(INVOKESTATIC, "cop5556sp18/RuntimeImageSupport", "setPixel", "(ILjava/awt/image/BufferedImage;II)V", false);
		return null;
	}
	
	@Override
	public Object visitLHSIdent(LHSIdent lhsIdent, Object arg)
			throws Exception {
		Type type = lhsIdent.type;
		int index = lhsIdent.dec.slot;
		switch(type) {
			case INTEGER:
				mv.visitVarInsn(ISTORE, index);
				break;
			case FLOAT:
				mv.visitVarInsn(FSTORE, index);
				break;
			case BOOLEAN:
				mv.visitVarInsn(ISTORE, index);
				break;
			case IMAGE:
				mv.visitMethodInsn(INVOKESTATIC, "cop5556sp18/RuntimeImageSupport", "deepCopy", "(Ljava/awt/image/BufferedImage;)Ljava/awt/image/BufferedImage;", false);
				mv.visitVarInsn(ASTORE, index);
				break;
			case FILE:
				mv.visitVarInsn(ASTORE, index);
				break;
			default:
				break;
		}
		return null;
	}
	
	@Override
	public Object visitExpressionUnary(ExpressionUnary expressionUnary,
			Object arg) throws Exception {
		expressionUnary.expression.visit(this, arg);
		
		Kind kind = expressionUnary.op;
		
		switch(kind) {
			case OP_EXCLAMATION:
				if(expressionUnary.getType() == Type.BOOLEAN) 
					mv.visitInsn(ICONST_1);
				else if(expressionUnary.getType() == Type.INTEGER) 
					mv.visitInsn(ICONST_M1);
				mv.visitInsn(IXOR);
				break;
			case OP_PLUS:
				break;
			case OP_MINUS:
				if(expressionUnary.getType() == Type.INTEGER) 
					mv.visitInsn(INEG);
				else if(expressionUnary.getType() == Type.FLOAT) 
					mv.visitInsn(FNEG);
				break;
			default:
				break;
		}
		return null;
	}
	
	@Override
	public Object visitStatementAssign(StatementAssign statementAssign,
			Object arg) throws Exception {
		statementAssign.e.visit(this, arg);
		statementAssign.lhs.visit(this, arg);
		return null;
	}
	
	@Override
	public Object visitStatementInput(StatementInput statementInput, Object arg)
			throws Exception {
		mv.visitVarInsn(ALOAD, 0);
		statementInput.e.visit(this,arg);
		mv.visitInsn(AALOAD);
		Type type = Types.getType(statementInput.dec.type);
		int index = statementInput.dec.slot;
		switch(type) {
			case INTEGER:
				mv.visitMethodInsn(INVOKESTATIC,"java/lang/Integer", "parseInt","(Ljava/lang/String;)I", false);
				mv.visitVarInsn(ISTORE, index);
				break;
			case FLOAT:
				mv.visitMethodInsn(INVOKESTATIC, "java/lang/Float", "parseFloat", "(Ljava/lang/String;)F", false);
				mv.visitVarInsn(FSTORE, index);
				break;
			case BOOLEAN:
				mv.visitMethodInsn(INVOKESTATIC, "java/lang/Boolean", "parseBoolean", "(Ljava/lang/String;)Z", false);
				mv.visitVarInsn(ISTORE, index);
				break;
			case IMAGE:
				if (statementInput.dec.width == null) {
					mv.visitInsn(Opcodes.ACONST_NULL);
					mv.visitInsn(Opcodes.ACONST_NULL);
				}
				else{
					statementInput.dec.width.visit(this,arg);
					mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
					statementInput.dec.height.visit(this,arg);
					mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
				}
				mv.visitMethodInsn(INVOKESTATIC, "cop5556sp18/RuntimeImageSupport", "readImage", "(Ljava/lang/String;Ljava/lang/Integer;Ljava/lang/Integer;)Ljava/awt/image/BufferedImage;", false);
				mv.visitVarInsn(ASTORE, index);
				break;
			case FILE:
				mv.visitVarInsn(ASTORE, index);
				break;
			default:
				break;
		}
		return null;
	}
	
	@Override
	public Object visitStatementSleep(StatementSleep statementSleep, Object arg)
			throws Exception {
		statementSleep.duration.visit(this,arg);
		mv.visitInsn(I2L);
		mv.visitMethodInsn(INVOKESTATIC, "java/lang/Thread", "sleep", "(J)V", false);
		return null;
	}

	@Override
	public Object visitStatementWrite(StatementWrite statementWrite, Object arg)
			throws Exception {
		int source = statementWrite.sourceDec.slot;
		int dest = statementWrite.destDec.slot;
		mv.visitVarInsn(ALOAD, source);
		mv.visitVarInsn(ALOAD, dest);
		mv.visitMethodInsn(INVOKESTATIC, "cop5556sp18/RuntimeImageSupport", "write", "(Ljava/awt/image/BufferedImage;Ljava/lang/String;)V", false);
		return null;
	}
	
	@Override
	public Object visitExpressionPixel(ExpressionPixel expressionPixel,
			Object arg) throws Exception {
		int index = expressionPixel.dec.slot;
		mv.visitVarInsn(ALOAD, index);
		expressionPixel.pixelSelector.visit(this,arg);
		mv.visitMethodInsn(INVOKESTATIC, "cop5556sp18/RuntimeImageSupport", "getPixel", "(Ljava/awt/image/BufferedImage;II)I", false);
		return null;
	}
	
	@Override
	public Object visitPixelSelector(PixelSelector pixelSelector, Object arg)
			throws Exception {
		if(pixelSelector.ex.getType() == Type.FLOAT && pixelSelector.ey.getType() == Type.FLOAT)
		{	
			//Convert to Cart X
			pixelSelector.ex.visit(this,arg);
			pixelSelector.ey.visit(this,arg);
			mv.visitInsn(F2D);
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "cos", "(D)D", false);
			mv.visitInsn(D2F);
			mv.visitInsn(FMUL);
			mv.visitInsn(F2I);
			
			//Convert to Cart Y
			pixelSelector.ex.visit(this,arg);
			pixelSelector.ey.visit(this,arg);
			mv.visitInsn(F2D);
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "sin", "(D)D", false);
			mv.visitInsn(D2F);
			mv.visitInsn(FMUL);
			mv.visitInsn(F2I);
		}
		else {
			pixelSelector.ex.visit(this,arg);
			pixelSelector.ey.visit(this,arg);
		}
		
		return null;
	}
	
	@Override
	public Object visitExpressionPixelConstructor(
			ExpressionPixelConstructor expressionPixelConstructor, Object arg)
			throws Exception {
		expressionPixelConstructor.alpha.visit(this,arg);
		expressionPixelConstructor.red.visit(this,arg);
		expressionPixelConstructor.green.visit(this,arg);
		expressionPixelConstructor.blue.visit(this,arg);
		mv.visitMethodInsn(INVOKESTATIC, "cop5556sp18/RuntimePixelOps", "makePixel",
				"(IIII)I", false);
		return null;
	}
	
	@Override
	public Object visitExpressionFunctionAppWithPixel(
			ExpressionFunctionAppWithPixel expressionFunctionAppWithPixel,
			Object arg) throws Exception {
		Kind kind = expressionFunctionAppWithPixel.name;
		switch(kind) {
			case KW_cart_x:
				expressionFunctionAppWithPixel.e0.visit(this,arg);
				expressionFunctionAppWithPixel.e1.visit(this,arg);
				mv.visitInsn(F2D);
				mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "cos", "(D)D", false);
				mv.visitInsn(D2F);
				mv.visitInsn(FMUL);
				mv.visitInsn(F2I);
				break;
			case KW_cart_y:
				expressionFunctionAppWithPixel.e0.visit(this,arg);
				expressionFunctionAppWithPixel.e1.visit(this,arg);
				mv.visitInsn(F2D);
				mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "sin", "(D)D", false);
				mv.visitInsn(D2F);
				mv.visitInsn(FMUL);
				mv.visitInsn(F2I);
				break;
			case KW_polar_a:
				expressionFunctionAppWithPixel.e1.visit(this,arg);
				mv.visitInsn(I2D);
				expressionFunctionAppWithPixel.e0.visit(this,arg);
				mv.visitInsn(I2D);
				mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "atan2", "(DD)D", false);
				mv.visitInsn(D2F);
				break;
			case KW_polar_r:
				expressionFunctionAppWithPixel.e0.visit(this,arg);
				mv.visitInsn(I2D);
				expressionFunctionAppWithPixel.e1.visit(this,arg);
				mv.visitInsn(I2D);
				mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "hypot", "(DD)D", false);
				mv.visitInsn(D2F);
				break;
			default:
				break;
		}
		return null;
	}
	
	@Override
	public Object visitExpressionFunctionAppWithExpressionArg(
			ExpressionFunctionAppWithExpressionArg expressionFunctionAppWithExpressionArg,
			Object arg) throws Exception {
		
		Kind kind = expressionFunctionAppWithExpressionArg.function;
		Type type = expressionFunctionAppWithExpressionArg.e.getType();
		expressionFunctionAppWithExpressionArg.e.visit(this,arg);
		
		switch(type) {
			case FLOAT:
				switch(kind) {
					case KW_abs:
						mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "abs", "(F)F", false);
						break;
					case KW_sin:
						mv.visitInsn(F2D);
						mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "sin", "(D)D", false);
						mv.visitInsn(D2F);
						break;
					case KW_cos:
						mv.visitInsn(F2D);
						mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "cos", "(D)D", false);
						mv.visitInsn(D2F);
						break;
					case KW_atan:
						mv.visitInsn(F2D);
						mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "atan", "(D)D", false);
						mv.visitInsn(D2F);
						break;
					case KW_log:
						mv.visitInsn(F2D);
						mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "log", "(D)D", false);
						mv.visitInsn(D2F);
						break;
					case KW_int:
						mv.visitInsn(F2I);
						break;
					case KW_float:
						break;
					default:
						break;
				}
				break;
			case INTEGER:
				switch(kind) {
					case KW_abs:
						mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "abs", "(I)I", false);
						break;
					case KW_red:
						mv.visitMethodInsn(INVOKESTATIC, "cop5556sp18/RuntimePixelOps", "getRed", "(I)I", false);
						break;
					case KW_green:
						mv.visitMethodInsn(INVOKESTATIC, "cop5556sp18/RuntimePixelOps", "getGreen", "(I)I", false);
						break;
					case KW_blue:
						mv.visitMethodInsn(INVOKESTATIC, "cop5556sp18/RuntimePixelOps", "getBlue", "(I)I", false);
						break;
					case KW_alpha:
						mv.visitMethodInsn(INVOKESTATIC, "cop5556sp18/RuntimePixelOps", "getAlpha", "(I)I", false);
						break;
					case KW_float:
						mv.visitInsn(Opcodes.I2F);
						break;
					case KW_int:
						break;
					default:
						break;
				}
				break;
			case IMAGE:
				switch(kind) {
					case KW_width:
						mv.visitMethodInsn(INVOKESTATIC, "cop5556sp18/RuntimeImageSupport", "getWidth", "(Ljava/awt/image/BufferedImage;)I", false);
						break;
					case KW_height:
						mv.visitMethodInsn(INVOKESTATIC, "cop5556sp18/RuntimeImageSupport", "getHeight", "(Ljava/awt/image/BufferedImage;)I", false);
						break;
					default:
						break;
				}
				break;
			default:
				break;
		}
		return null;
	}
	
	@Override
	public Object visitExpressionPredefinedName(
			ExpressionPredefinedName expressionPredefinedName, Object arg)
			throws Exception {
		Kind kind = expressionPredefinedName.name;
		switch(kind) {
			case KW_Z: mv.visitLdcInsn(Z);
					  break;
			case KW_default_width: mv.visitLdcInsn(defaultWidth);
								  break;
			case KW_default_height: mv.visitLdcInsn(defaultHeight);
								  break;
			default:
				break;		
		}
		return null;
	}
	
	@Override
	public Object visitExpressionIdent(ExpressionIdent expressionIdent,
			Object arg) throws Exception {
		Type type = expressionIdent.getType();
		int index = expressionIdent.dec.slot;
		switch(type) {
			case INTEGER:
				mv.visitVarInsn(ILOAD, index);
				break;
			case FLOAT:
				mv.visitVarInsn(FLOAD, index);
				break;
			case BOOLEAN:
				mv.visitVarInsn(ILOAD, index);
				break;
			case FILE:
				mv.visitVarInsn(ALOAD, index);
				break;
			case IMAGE:
				mv.visitVarInsn(ALOAD, index);
				break;
			default:
				break;
		}
		return null;
	}
	
	@Override
	public Object visitExpressionBinary(ExpressionBinary expressionBinary,
			Object arg) throws Exception {
		Type left = expressionBinary.leftExpression.getType();
		Type right = expressionBinary.rightExpression.getType();
		Label binStart = new Label();
		Label binEnd = new Label();
		boolean flag = false;
		Kind op = expressionBinary.op;
		if(left == Type.INTEGER && right == Type.INTEGER) {
			switch(op) {
			
			case OP_PLUS:
				expressionBinary.leftExpression.visit(this, arg);
				expressionBinary.rightExpression.visit(this, arg);
				mv.visitInsn(IADD);
				break;
			case OP_MINUS:
				expressionBinary.leftExpression.visit(this, arg);
				expressionBinary.rightExpression.visit(this, arg);
				mv.visitInsn(ISUB);
				break;
			case OP_TIMES:
				expressionBinary.leftExpression.visit(this, arg);
				expressionBinary.rightExpression.visit(this, arg);
				mv.visitInsn(IMUL);
				break;
			case OP_DIV:
				expressionBinary.leftExpression.visit(this, arg);
				expressionBinary.rightExpression.visit(this, arg);
				mv.visitInsn(IDIV);
				break;
			case OP_MOD:
				expressionBinary.leftExpression.visit(this, arg);
				expressionBinary.rightExpression.visit(this, arg);
				mv.visitInsn(IREM);
				break;
			case OP_POWER:
				expressionBinary.leftExpression.visit(this, arg);
				mv.visitInsn(I2D);
				expressionBinary.rightExpression.visit(this, arg);
				mv.visitInsn(I2D);
				mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "pow", "(DD)D", false);
				mv.visitInsn(D2I);
				break;
			case OP_AND:
				expressionBinary.leftExpression.visit(this, arg);
				expressionBinary.rightExpression.visit(this, arg);
				mv.visitInsn(IAND);
				break;
			case OP_OR:
				expressionBinary.leftExpression.visit(this, arg);
				expressionBinary.rightExpression.visit(this, arg);
				mv.visitInsn(IOR);
				break;
			case OP_GT:
				expressionBinary.leftExpression.visit(this, arg);
				expressionBinary.rightExpression.visit(this, arg);
				mv.visitJumpInsn(IF_ICMPGT, binStart);
				flag = true;
				break;
			case OP_LT:
				expressionBinary.leftExpression.visit(this, arg);
				expressionBinary.rightExpression.visit(this, arg);
				mv.visitJumpInsn(IF_ICMPLT, binStart);
				flag = true;
				break;
			case OP_GE:
				expressionBinary.leftExpression.visit(this, arg);
				expressionBinary.rightExpression.visit(this, arg);
				mv.visitJumpInsn(IF_ICMPGE, binStart);
				flag = true;
				break;
			case OP_LE:
				expressionBinary.leftExpression.visit(this, arg);
				expressionBinary.rightExpression.visit(this, arg);
				mv.visitJumpInsn(IF_ICMPLE, binStart);
				flag = true;
				break;
			case OP_EQ:
				expressionBinary.leftExpression.visit(this, arg);
				expressionBinary.rightExpression.visit(this, arg);
				mv.visitJumpInsn(IF_ICMPEQ, binStart);
				flag = true;
				break;
			case OP_NEQ:
				expressionBinary.leftExpression.visit(this, arg);
				expressionBinary.rightExpression.visit(this, arg);
				mv.visitJumpInsn(IF_ICMPNE, binStart);
				flag = true;
				break;
			default:
				break;
			}
		}
		else if(left == Type.FLOAT && right == Type.FLOAT) {
			switch(op) {
			
			case OP_PLUS:
				expressionBinary.leftExpression.visit(this, arg);
				expressionBinary.rightExpression.visit(this, arg);
				mv.visitInsn(FADD);
				break;
			case OP_MINUS:
				expressionBinary.leftExpression.visit(this, arg);
				expressionBinary.rightExpression.visit(this, arg);
				mv.visitInsn(FSUB);
				break;
			case OP_TIMES:
				expressionBinary.leftExpression.visit(this, arg);
				expressionBinary.rightExpression.visit(this, arg);
				mv.visitInsn(FMUL);
				break;
			case OP_DIV:
				expressionBinary.leftExpression.visit(this, arg);
				expressionBinary.rightExpression.visit(this, arg);
				mv.visitInsn(FDIV);
				break;
			case OP_POWER:
				expressionBinary.leftExpression.visit(this, arg);
				mv.visitInsn(F2D);
				expressionBinary.rightExpression.visit(this, arg);
				mv.visitInsn(F2D);
				mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "pow", "(DD)D", false);
				mv.visitInsn(D2F);
				break;
			case OP_GT:
				expressionBinary.leftExpression.visit(this, arg);
				expressionBinary.rightExpression.visit(this, arg);
				mv.visitInsn(FCMPL);
				mv.visitJumpInsn(IFGT, binStart);
				flag = true;
				break;
			case OP_LT:
				expressionBinary.leftExpression.visit(this, arg);
				expressionBinary.rightExpression.visit(this, arg);
				mv.visitInsn(FCMPG);
				mv.visitJumpInsn(IFLT, binStart);
				flag = true;
				break;
			case OP_GE:
				expressionBinary.leftExpression.visit(this, arg);
				expressionBinary.rightExpression.visit(this, arg);
				mv.visitInsn(FCMPL);
				mv.visitJumpInsn(IFGE, binStart);
				flag = true;
				break;
			case OP_LE:
				expressionBinary.leftExpression.visit(this, arg);
				expressionBinary.rightExpression.visit(this, arg);
				mv.visitInsn(FCMPG);
				mv.visitJumpInsn(IFLE, binStart);
				flag = true;
				break;
			case OP_EQ:
				expressionBinary.leftExpression.visit(this, arg);
				expressionBinary.rightExpression.visit(this, arg);
				mv.visitInsn(FCMPL);
				mv.visitJumpInsn(IFEQ, binStart);
				flag = true;
				break;
			case OP_NEQ:
				expressionBinary.leftExpression.visit(this, arg);
				expressionBinary.rightExpression.visit(this, arg);
				mv.visitInsn(FCMPL);
				mv.visitJumpInsn(IFNE, binStart);
				flag = true;
				break;
			default:
				break;
			}
		}
		else if(left == Type.FLOAT && right == Type.INTEGER) {
			switch(op) {
			
			case OP_PLUS:
				expressionBinary.leftExpression.visit(this, arg);
				expressionBinary.rightExpression.visit(this, arg);
				mv.visitInsn(I2F);
				mv.visitInsn(FADD);
				break;
			case OP_MINUS:
				expressionBinary.leftExpression.visit(this, arg);
				expressionBinary.rightExpression.visit(this, arg);
				mv.visitInsn(I2F);
				mv.visitInsn(FSUB);
				break;
			case OP_TIMES:
				expressionBinary.leftExpression.visit(this, arg);
				expressionBinary.rightExpression.visit(this, arg);
				mv.visitInsn(I2F);
				mv.visitInsn(FMUL);
				break;
			case OP_DIV:
				expressionBinary.leftExpression.visit(this, arg);
				expressionBinary.rightExpression.visit(this, arg);
				mv.visitInsn(I2F);
				mv.visitInsn(FDIV);
				break;
			case OP_POWER:
				expressionBinary.leftExpression.visit(this, arg);
				mv.visitInsn(F2D);
				expressionBinary.rightExpression.visit(this, arg);
				mv.visitInsn(I2D);
				mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "pow", "(DD)D", false);
				mv.visitInsn(D2F);
				break;
			default:
				break;
			}
		}
		else if(left == Type.INTEGER && right == Type.FLOAT) {
			switch(op) {
			
			case OP_PLUS:
				expressionBinary.leftExpression.visit(this, arg);
				mv.visitInsn(I2F);
				expressionBinary.rightExpression.visit(this, arg);
				mv.visitInsn(FADD);
				break;
			case OP_MINUS:
				expressionBinary.leftExpression.visit(this, arg);
				mv.visitInsn(I2F);
				expressionBinary.rightExpression.visit(this, arg);
				mv.visitInsn(FSUB);
				break;
			case OP_TIMES:
				expressionBinary.leftExpression.visit(this, arg);
				mv.visitInsn(I2F);
				expressionBinary.rightExpression.visit(this, arg);
				mv.visitInsn(FMUL);
				break;
			case OP_DIV:
				expressionBinary.leftExpression.visit(this, arg);
				mv.visitInsn(I2F);
				expressionBinary.rightExpression.visit(this, arg);
				mv.visitInsn(FDIV);
				break;
			case OP_POWER:
				expressionBinary.leftExpression.visit(this, arg);
				mv.visitInsn(I2D);
				expressionBinary.rightExpression.visit(this, arg);
				mv.visitInsn(F2D);
				mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "pow", "(DD)D", false);
				mv.visitInsn(D2F);
				break;
			default:
				break;
			}
		}
		else if(left == Type.BOOLEAN && right == Type.BOOLEAN) {
			expressionBinary.leftExpression.visit(this, arg);
			expressionBinary.rightExpression.visit(this, arg);
			switch(op) {
			
			case OP_AND:
				mv.visitInsn(IAND);
				break;
			case OP_OR:
				mv.visitInsn(IOR);
				break;
			case OP_GT:
				mv.visitJumpInsn(IF_ICMPGT, binStart);
				flag = true;
				break;
			case OP_LT:
				mv.visitJumpInsn(IF_ICMPLT, binStart);
				flag = true;
				break;
			case OP_GE:
				mv.visitJumpInsn(IF_ICMPGE, binStart);
				flag = true;
				break;
			case OP_LE:
				mv.visitJumpInsn(IF_ICMPLE, binStart);
				flag = true;
				break;
			case OP_EQ:
				mv.visitJumpInsn(IF_ICMPEQ, binStart);
				flag = true;
				break;
			case OP_NEQ:
				mv.visitJumpInsn(IF_ICMPNE, binStart);
				flag = true;
				break;
			default:
				break;
			}
		}
		
	
	if(flag) {
		mv.visitLdcInsn(Boolean.FALSE);
		mv.visitJumpInsn(GOTO, binEnd);
		mv.visitLabel(binStart);
		mv.visitLdcInsn(Boolean.TRUE);
		mv.visitLabel(binEnd);
	}	
		return null;
	}

	@Override
	public Object visitStatementShow(StatementShow statementShow, Object arg)
			throws Exception {
		statementShow.e.visit(this, arg);
		Type type = statementShow.e.getType();
		switch (type) {
			case INTEGER : {
				CodeGenUtils.genLogTOS(GRADE, mv, type);
				mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out",
						"Ljava/io/PrintStream;");
				mv.visitInsn(Opcodes.SWAP);
				mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream",
						"println", "(I)V", false);
			}
				break;
			case BOOLEAN : {
				CodeGenUtils.genLogTOS(GRADE, mv, type);
				mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out",
						"Ljava/io/PrintStream;");
				mv.visitInsn(Opcodes.SWAP);
				mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream",
						"println", "(Z)V", false);
			}
			break;
			case FLOAT : {
				CodeGenUtils.genLogTOS(GRADE, mv, type);
				mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out",
						"Ljava/io/PrintStream;");
				mv.visitInsn(Opcodes.SWAP);
				mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream",
						"println", "(F)V", false);
			}
			break;
			case FILE : {
				CodeGenUtils.genLogTOS(GRADE, mv, type);
				mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out",
						"Ljava/io/PrintStream;");
				mv.visitInsn(Opcodes.SWAP);
				mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream",
						"println", "(Ljava/lang/String;)V", false);
			}
			break;
			case IMAGE : {
				CodeGenUtils.genLogTOS(GRADE, mv, type);
				mv.visitMethodInsn(INVOKESTATIC, "cop5556sp18/RuntimeImageSupport", "makeFrame",
						"(Ljava/awt/image/BufferedImage;)Ljavax/swing/JFrame;", false);
				mv.visitInsn(Opcodes.POP);
			}
			break;
			default:
				break;

		}
		return null;
	}
	
	@Override
	public Object visitExpressionFloatLiteral(
			ExpressionFloatLiteral expressionFloatLiteral, Object arg)
			throws Exception {
		mv.visitLdcInsn(expressionFloatLiteral.value);
		return null;
	}
	
	@Override
	public Object visitExpressionIntegerLiteral(
			ExpressionIntegerLiteral expressionIntegerLiteral, Object arg)
			throws Exception {
		mv.visitLdcInsn(expressionIntegerLiteral.value);
		return null;
	}
	
	@Override
	public Object visitBooleanLiteral(
			ExpressionBooleanLiteral expressionBooleanLiteral, Object arg)
			throws Exception {
		mv.visitLdcInsn(expressionBooleanLiteral.value);
		return null;
	}
	
	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {
		// TODO refactor and extend as necessary
		cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
		// cw = new ClassWriter(0); //If the call to mv.visitMaxs(1, 1) crashes,
		// it is
		// sometime helpful to
		// temporarily run it without COMPUTE_FRAMES. You probably
		// won't get a completely correct classfile, but
		// you will be able to see the code that was
		// generated.
		className = program.progName;
		classDesc = "L" + className + ";";
		String sourceFileName = (String) arg;
		cw.visit(52, ACC_PUBLIC + ACC_SUPER, className, null,
				"java/lang/Object", null);
		cw.visitSource(sourceFileName, null);

		// create main method
		mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "main",
				"([Ljava/lang/String;)V", null, null);
		// initialize
		mv.visitCode();

		// add label before first instruction
		Label mainStart = new Label();
		mv.visitLabel(mainStart);

		CodeGenUtils.genLog(DEVEL, mv, "entering main");

		program.block.visit(this, arg);

		// generates code to add string to log
		CodeGenUtils.genLog(DEVEL, mv, "leaving main");

		// adds the required (by the JVM) return statement to main
		mv.visitInsn(RETURN);

		// adds label at end of code
		Label mainEnd = new Label();
		mv.visitLabel(mainEnd);
		mv.visitLocalVariable("args", "[Ljava/lang/String;", null, mainStart,
				mainEnd, 0);
		for(LocalVarInfo d : localvars) {
			mv.visitLocalVariable(d.name, d.desc, null, d.start,
					d.end, d.index);
		}
		// Because we use ClassWriter.COMPUTE_FRAMES as a parameter in the
		// constructor,
		// asm will calculate this itself and the parameters are ignored.
		// If you have trouble with failures in this routine, it may be useful
		// to temporarily change the parameter in the ClassWriter constructor
		// from COMPUTE_FRAMES to 0.
		// The generated classfile will not be correct, but you will at least be
		// able to see what is in it.
		mv.visitMaxs(0, 0);

		// terminate construction of main method
		mv.visitEnd();

		// terminate class construction
		cw.visitEnd();

		// generate classfile as byte array and return
		return cw.toByteArray();
	}

}
