/*
 	Code generation for the following grammar segments:

		Expression := Value (((Operator | AssignmentOperator) Value) | ("?" Expression ":" Value) )*

		Operator := "||" | "&&" | "|" | "^" | "&" | "==" | "!=" | ">" | "<" | "<=" | ">=" | "<<" | ">>" | ">>>" | "+" | "-" | "*" | "/" | "%"

		Value := [Prefix] Variable [Suffix] (PrimarySuffix)*
		Prefix := "++" | "--" | "~" | "!"
		Suffix := "++" | "--"

		Variable := ["(" Type ")"] Literal | Name | "this" | "super" | AllocationExpression | "(" Expression ")"
*/
package salsac.definitions;

import salsac.*;
import salsac.SalsaParserTreeConstants;


import java.util.Vector;


public class Expression extends SimpleNode {

	public Expression(int id) 			{ super(id); }
	public Expression(SalsaParser p, int id)	{ super(p, id); }

	public boolean hasOperations() {
		try{
			if (getToken(0).image.equals("dependent")) return false;
		} catch (NullPointerException e) {}
		if (children.length > 1) return true;
		else {
			Value value = (Value)getChild(0);
			if (value.children.length > 1) return true;
			else return false;
		}
	}

	public String getType() {
		try{
			if (getToken(0).image.equals("dependent")) return "boolean";
		} catch (NullPointerException e) {}
		return ((Value)getChild(0)).getType();
	}

	public boolean hasToken() {
		try{
			if (getToken(0).image.equals("dependent")) return false;
		} catch (NullPointerException e) {}
		boolean hasToken = false;
		for (int i = 0; i < children.length; i += 2) {
			hasToken = hasToken || ((Value)getChild(i)).isToken();
		}
		return hasToken;
	}

	public int getExpressionLine() {
		try{
			if (getToken(0).image.equals("dependent")) return getToken(0).beginLine;
		} catch (NullPointerException e) {}
		if (children.length > 1) return getChild(1).getToken(0).beginLine;
		else {
			Value value = (Value)getChild(0);
			if (value.children.length > 1) {
				if (value.getChild(0) instanceof Prefix) return value.getChild(0).getToken(0).beginLine;
				else return value.getChild(1).getToken(0).beginLine;
			}
			return -1;
		}
	}

	public String getJavaCode() {
		//Attempt to see if the user tried to use a token inside an expression,
		//which is illegal SALSA code.  if so generate an error.
		try{
			if (getToken(0).image.equals("dependent")) return "this.isDependent()";
		} catch (NullPointerException e) {}
		if (hasOperations() && hasToken()) {
			System.err.println("Salsa Compiler Error: Line " + getExpressionLine());
			String expcode = getChildCode();

			System.err.println("\tTokens cannot be part of an expression: " + expcode);
			System.exit(0);
		}

		String code = getChild(0).getJavaCode();
		for (int i = 1; i < children.length; i++) {
			if (getChild(i) instanceof Expression) {
				code += "?" + getChild(i).getJavaCode() + ":" + getChild(i+1).getJavaCode();
				i++;
			} else {
				code += getChild(i).getJavaCode();
			}
		}
		return code;
	}
}
