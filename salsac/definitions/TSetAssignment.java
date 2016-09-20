/*
	Code generation for the following grammar segment:

	TSetAssignment := "this" <DOTSET> <IDENTIFIER> "=" Value()
*/


package salsac.definitions;

import salsac.*;

public class TSetAssignment extends SimpleNode {

	public TSetAssignment(int id) 			{ super(id); }
	public TSetAssignment(SalsaParser p, int id)		{ super(p, id); }

	public String getType() {
		return SalsaCompiler.symbolTable.getSymbolType( getToken(2).image );
	}

	public String getJavaCode() {
		String code = "";
		code += "this.setTState(\"" + getToken(2).image + "\"," + getChild(0).getJavaCode() +  ")";
		return code;
	}

}
