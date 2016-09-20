/*
	Code generation for the following grammar segment:

	TransGet := "this" <DOTGET> <IDENTIFIER> (PrimarySuffix())*
*/


package salsac.definitions;

import salsac.*;

public class TransGet extends SimpleNode {

	public TransGet(int id) 			{ super(id); }
	public TransGet(SalsaParser p, int id)		{ super(p, id); }

	public String getType() {
		return SalsaCompiler.symbolTable.getSymbolType( getToken(2).image );
	}

	public String getJavaCode() {
		String code = "";
		code += "((" + this.getType() + ") this.getTState(\"" + getToken(2).image + "\"))";

		if(children == null) return code;
		for(int i = 0; i < children.length; i++){
			code += getChild(i).getJavaCode();
		}
		return code;
	}

}
