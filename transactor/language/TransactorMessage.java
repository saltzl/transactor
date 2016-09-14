/**
 * TransactorMessage.java
 * **********************************************************
 * Author: Lee Saltzman (saltzl@rpi.edu)
 *
 *
 * Variation of the message class that carries worldview information
 *
 */

package transactor.language;

import salsa.language.*;
import gc.*;
import java.io.*;
import java.util.Vector;

public class TransactorMessage extends Message {

	public Worldview msg_wv;

	public TransactorMessage(ActorReference source, Object target, Worldview msg_wv, Object methodName, Object[] arguments, Token synchronizationToken, Token continuationToken)  {
		this(source, target, msg_wv, methodName, arguments, synchronizationToken, continuationToken,true,true);
	}

	public TransactorMessage(WeakReference source, WeakReference target, Worldview msg_wv, String methodName, Object[] arguments, boolean requireAck) {
		super(source,target,methodName,arguments,requireAck);
		this.needAck=requireAck;
		this.target=target;
		this.source=source;
		this.msg_wv = msg_wv;
		this.methodName=methodName;
		this.arguments=arguments;
		this.continuationToken=null;
		if (needAck) {this.waitAck();}
	}

	public TransactorMessage(ActorReference source, Object target, Worldview msg_wv, Object methodName, Object[] arguments, Token synchronizationToken, Token continuationToken, boolean requireAck) {
		this(source,target, msg_wv,methodName,arguments,synchronizationToken, continuationToken, requireAck, true);
	}

	/**
	 * These are the constructors for message.  The second is used if the message
	 * is part of a join continuation.  The constructor discovers which tokens are
	 * needed for it to be processed, and tries to resolve tokens which already have
	 * a user specified value. (ie done through code like: token x = 5;)
	 * This constructor allows for the target actor and method name to be tokens as
	 * well as the parameters.
	 */
	public TransactorMessage(ActorReference source, Object target, Worldview msg_wv, Object methodName, Object[] arguments, Token synchronizationToken, Token continuationToken, boolean requireAck, boolean isCallByValue) {
		super(source, target, methodName,arguments,synchronizationToken,continuationToken,requireAck,isCallByValue);
		this.msg_wv = msg_wv;
		this.worldviewSplit();
	}

	/**
	 *	The following methods return a human readable String representation
	 *	of this message.
	 */
	public String toString() {
		String description = methodName + "(";

		for (int i = 0; i < arguments.length; i++) {
			if (arguments[i] == null) {
				description += "null";
			} else {
				description += arguments[i].getClass().getName();
			}
			if (i+1 != arguments.length) description += ", ";
		}
		description += "), target: ";

		if (target != null) description += target.toString();
		else description += "null";

		description += ", source: ";

		if (source != null) description += source.toString();
		else description += "null";

		return description;
	}

    /*
     *Reference Splitting for worldviews
     *
     */
    public void worldviewSplit() {
    	byte[] serializedWorldView;

    	try {
    		ByteArrayOutputStream bos = new ByteArrayOutputStream();
    		ObjectOutputStream outStream = new ObjectOutputStream( bos);
    		outStream.writeObject(msg_wv);
    		outStream.flush();
    		serializedWorldView = bos.toByteArray();
    		outStream.close();
    		bos.close();

    	}
    	catch (Exception e) {System.err.println("Message Class, worldviewSplit() method: Error on serializing method arguments:"+e); return;}

    	try {
    		ByteArrayInputStream bis = new ByteArrayInputStream(serializedWorldView);
    		ObjectInputStream inStream;
    		inStream = new ObjectInputStream(bis);
    		msg_wv= (Worldview) inStream.readObject();
    		inStream.close();
    		bis.close();
    	}
    	catch (Exception e) {System.err.println("Message Class, worldviewSplit() method:Error on deserializing method arguments:"+e); }
    }


    public Worldview getWorldview() {
    	return this.msg_wv;
    }
}
