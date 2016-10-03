/*
	Code generation for the following grammar segment:
	
	RollbackStatement := "rollback;"
*/

package salsac.definitions;

import salsac.*;

public class RollbackStatement extends SimpleNode {

	public RollbackStatement(int id) 					{ super(id); }
	public RollbackStatement(SalsaParser p, int id)	{ super(p, id); }

	public String getJavaCode() {
		return SalsaCompiler.getIndent() + "this.rollback(false);return;\n";
	}
}
