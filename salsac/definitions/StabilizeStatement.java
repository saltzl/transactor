/*
	Code generation for the following grammar segment:
	
	StabilizeStatement := "stabilize;"
*/

package salsac.definitions;

import salsac.*;

public class StabilizeStatement extends SimpleNode {

	public StabilizeStatement(int id) 					{ super(id); }
	public StabilizeStatement(SalsaParser p, int id)	{ super(p, id); }

	public String getJavaCode() {
		return SalsaCompiler.getIndent() + "this.stabilize();\n";
	}
}
