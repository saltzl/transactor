/*
	Code generation for the following grammar segment:
	
	Ident := <IDENTIFIER>
*/

package salsac.definitions;

import salsac.*;

public class Ident extends SimpleNode {

	public Ident(int id) 			{ super(id); }
	public Ident(SalsaParser p, int id)	{ super(p, id); }

	public String getJavaCode() {
		return this.getToken(0).image;
	}
}
