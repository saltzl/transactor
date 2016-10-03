/*
	Code generation for the following grammar segment:

	TransSet := "this" <DOTGET> <IDENTIFIER> (PrimarySuffix())*
*/


package salsac.definitions;

import salsac.*;

public class TransSet extends SimpleNode {

	public TransSet(int id) 			{ super(id); }
	public TransSet(SalsaParser p, int id)		{ super(p, id); }

	public String getType() {
		return SalsaCompiler.symbolTable.getSymbolType( getToken(2).image );
	}

	public String getJavaCode() {
		String code = "";
		code += "(("+this.getType() + ")this.getMutTState(\"" + getToken(2).image + "\"))";
		
		if(children == null) return code;
		for(int i = 0; i < children.length; ++i){
			code += getChild(i).getJavaCode();
		}
		return code;
	}

}
