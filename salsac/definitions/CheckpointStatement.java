/*
	Code generation for the following grammar segment:
	
	CheckpointStatement := "checkpoint;"
*/

package salsac.definitions;

import salsac.*;

public class CheckpointStatement extends SimpleNode {

	public CheckpointStatement(int id) 					{ super(id); }
	public CheckpointStatement(SalsaParser p, int id)	{ super(p, id); }

	public String getJavaCode() {
		return SalsaCompiler.getIndent() + "this.checkpoint();";
	}
}
