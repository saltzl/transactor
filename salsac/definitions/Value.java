/*
	Code generation for the following grammar segment:

	Value := [Prefix] Variable [Suffix] (PrimarySuffix)* | "dependent" | TransGet | TransSet
*/


package salsac.definitions;

import salsac.*;

public class Value extends SimpleNode {

	public Value(int id)			{ super(id); }
	public Value(SalsaParser p, int id)	{ super(p, id); }

	public String getType() {
		try{
		   if (tokens.length > 0 && getToken(0).image.equals("dependent")) return "boolean";
		} catch (NullPointerException e) {}

		if (getChild(0) instanceof Prefix) return ((Variable)getChild(1)).getType();
		else if (getChild(0) instanceof TransGet) return ((TransGet)getChild(0)).getType();
		else if (getChild(0) instanceof TransSet) return ((TransSet)getChild(0)).getType();
		else return ((Variable)getChild(0)).getType();
	}

	public boolean isToken() {
		try{
			if (getToken(0).image.equals("dependent")) return false;
		} catch (NullPointerException e) {}
		int variablePosition = 0;
		if (getChild(0) instanceof Prefix) variablePosition = 1;

		String childCode = getChild(variablePosition).getJavaCode();

		String childType = SalsaCompiler.symbolTable.getSymbolType(childCode);

		if ((childType != null) && (childType.equals("token") || childType.equals("next"))) return true;
		return false;
	}

	public String getJavaCode() {
		String code = "";
		try{
			if (getToken(0).image.equals("dependent")) return "this.isDependent()";
		} catch (NullPointerException e) {}
		for (int i = 0; i < children.length; i++) {
			String childCode = getChild(i).getJavaCode();
            if (childCode.equals("self")) {
				if (getChild(children.length-1) instanceof Variable) {
					childCode = "self";
				} else {
					childCode = "this";
				}
			}
			code += childCode;
		}
		return code;
	}
}
