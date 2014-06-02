package transactor.language;

// Import declarations generated by the SALSA compiler, do not modify.
import java.io.IOException;
import java.util.Vector;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.InvocationTargetException;

import salsa.language.Actor;
import salsa.language.ActorReference;
import salsa.language.Message;
import salsa.language.RunTime;
import salsa.language.ServiceFactory;
import gc.WeakReference;
import salsa.language.Token;
import salsa.language.exceptions.*;
import salsa.language.exceptions.CurrentContinuationException;

import salsa.language.UniversalActor;

import salsa.naming.UAN;
import salsa.naming.UAL;
import salsa.naming.MalformedUALException;
import salsa.naming.MalformedUANException;

import salsa.resources.SystemService;

import salsa.resources.ActorService;

// End SALSA compiler generated import delcarations.

import java.io.*;
import java.util.*;
import java.lang.reflect.Field;
import gc.*;
import java.net.URI;

// NOTE: The order of execution of worldview modification is important in some methods
public class Transactor extends UniversalActor  {

	public static ActorReference getReferenceByName(UAN uan)	{ return new Transactor(false, uan); }
	public static ActorReference getReferenceByName(String uan)	{ return Transactor.getReferenceByName(new UAN(uan)); }
	public static ActorReference getReferenceByLocation(UAL ual)	{ return new Transactor(false, ual); }

	public static ActorReference getReferenceByLocation(String ual)	{ return Transactor.getReferenceByLocation(new UAL(ual)); }
	public Transactor(boolean o, UAN __uan)	{ super(false,__uan); }
	public Transactor(boolean o, UAL __ual)	{ super(false,__ual); }
	public Transactor()		{  }
/*
	public UniversalActor construct (Transactor self) {
		Object[] __arguments = { self };
		this.send( new Message(this, this, "construct", __arguments, null, null) );
		return this;
	}

	public UniversalActor construct () {
		Object[] __arguments = {  };
		this.send( new Message(this, this, "construct", __arguments, null, null) );
		return this;
	}
*/
	public class State extends UniversalActor .State {
		public Transactor self;
		public void updateSelf(ActorReference actorReference) {}

		public State() {
			this(null, null);
		}

		public State(UAN __uan, UAL __ual) {
			super(__uan, __ual);
            // For future implementation:
            // Here we process the "self" that was passed up through super(__uan, __uan, self) calls from subclasses 
            // this.self = self; etc.... from current construct method
            // NOTE: we can't do the above since self UAN has not been created yet at the UniversalActor.State super class so we 
            // should leave evaluation of self in the construct() below ***
            // NOTE: We can compile super.updateSelf(this) in subclass updateSelf() instead of inserting super() in subclass constructor but both have the same efficiency ***
			addClassName( "transactor.language.Transactor$State" );
			addMethodsForClasses();
		}

        public void process(Message msg) {}

		private Worldview wv;
        // We can use Self 
        // if we use self to bundle the UAN and UAL, then we need to create a custom equals() for WV mapping
        // Migration must have UAN which should not change, so UAL should not change for annoynomous actor
        // Therefore we don't have to worry about change of name unless it is possible to change UAN
		private String name;

        // Transactor Universal Storage Location: URI that locates where a persistent state is saved and identifies the protocol used to save/load states from stable storage to be used by the TStorageService
        private URI USL;

        // Transaction director aka the pinger used to reconcile dependency information within a set of participants of a transaction
        public Pinger transDirector;
        
        /* 
         * Super constructor must be called from subclasses of transactors
         * Transactor complier would need to insert super(self) into the construct method AND create a noargs construct is none is found 
         * self is needed since subclasses overrides its parents memeber variables and hides it from the parent
         * so we need access to self in these methods
         */
		public void construct(Transactor self){
            // Future implementation might want to include wv info for dep passing in Message objects
            // -> When sending messages the source transactor would carry it's WV as a member variable we can check against
            // ->-> but wv needs to updated with self everytime it is changed, self is meant to just be a reference so we want to pass wv into the message object instead of packaging it with self
            // How is self updated when migrating in SALSA?
			this.self = self;
			if (self.getUAN() != null) 
                this.name = self.getUAN().toString();
			else 
            this.name = self.getUAL().toString();
            try { this.USL = new URI("file:///Users/carrykuang/Documents/transactor/"+name.substring(name.lastIndexOf("/") + 1)+".ser"); }
            // TODO: Handle malformed uri
            catch (Exception e) { e.printStackTrace(); }
			wv = new Worldview();
			HashMap new_histMap = new HashMap();
			new_histMap.put(name, new History());
			wv.setHistMap(new_histMap);
            this.setWV(wv);
		}
    
        // NOTE: Compiler needs to insert default no args contructor to subclass if none found to call super construct(self)
        // This constructor is unused...
        public void construct() {}
        
		public void setWV(Worldview wv) {
			this.wv = wv;
		}

        /*
         * recvMsg resolves Worldview dependencies before passing message into mailbox
         */
		public void recvMsg(Message msg, Worldview msg_wv) {
            //System.out.println("Current mailbox for " + name + " : \n" + mailbox + "\n");
            //System.out.println("next msg for " + name + " : \n" + msg + "\n");
            //System.out.println("msgs wv :\n" + msg_wv);
            //System.out.println("MY WV:\n" + this.wv);
			Worldview union = wv.union(msg_wv);
            //System.out.println("UNION:::\n" + union);
            //System.out.println("HERE : " + msg);
			HashSet current = new HashSet();
			current.add(name);
			if (union.invalidates(wv.getHistMap(), current)) {
                //System.out.println("msg caued rollback.................");
                /*** [rcv3] ***/
				if (wv.getHistMap().get(name).isPersistent()) {
					
                    //System.out.println("roll back cause of dep\n\n");
					Worldview new_wv = new Worldview();
					HashMap new_histMap = new HashMap();
					new_histMap.put(name, union.getHistMap().get(name));
					new_wv.setHistMap(new_histMap);

                    // Message remains and resend to recheck dependencies
                    // self<-recvMsg(msg, msg_wv)
                    Object args[] = { msg, msg_wv };
                    Message pass_msg = new Message( self, self, "recvMsg", args, null, null, false );
                    self.send(pass_msg);

                    // rollback should be last call since no calls after should be processed
					this.rollback(true, new_wv);
                    //if (isDestroyed())
                     //   return;
                   
				}
                /*** [rcv4] ***/
				else {
                    //System.out.println("DESTROYED................\n");
					this.destroy();
				}
			}
            /*** [rcv2] ***/
			else if (union.invalidates(msg_wv.getHistMap(), msg_wv.getRootSet())) {
                // Message is invalidate so we send ack and ignore
                //System.out.println("message invalidated~~~~~~~~~~~~\n"+msg+"\n");
                responseAck(msg);
                wv = union;
                wv.setRootSet(new HashSet());
                //System.out.println("message wv: \n" + msg_wv + "\n\n");
			}
            /*** [rcv1] ***/
			else {
				wv = union;
                //System.out.println("Got msg: " + msg + "\n\n");
				self.send(msg);
			}
            //System.out.println("after mailbox for " + name + " : \n" + mailbox + "\n");
		}

        /*
         * sendMsg calls recvMsg of recipient with msg and wv arguements
         * Continuation might be able to handled internally in Message object 
         */
        /*** [snd] ***/
        // NOTE: We need sendMsg to be sequential since we need to track wv sequentially by the message 
        // send in here is concurrent to follow the actor model 
		public void sendMsg(String method, Object[] params, Transactor recipient) {
            // Create the Message
			Message msg = new Message(self, recipient, method, params, null, null);
            // Need to set property to ensure message is processed next after recvMsg is processed
			Object[] msg_property = {"highPriority"};
			msg.setProperty("priority", msg_property);
            //System.out.println("this is a msg:\n" + (String)msg.getPropertyParameters()[0]);
            // recipient<-recvMsg(msg, this.wv)
            
            // Reference splitting in Message initialization prevents local object reference sharing
            // so we capture the current Worldview at this instance, 
            // therefore worldview modifications after this call will not be reflected
            Object args[] = { msg, this.wv };
            Message recvMsg = new Message( self, recipient, "recvMsg", args, null, null );
            //System.out.println(name + " sending : " + msg);
            //System.out.println("Sending msg: " + msg + "\n\n");
            __messages.add(recvMsg);
		}
        
        /* 
         * newTActor -> returns new Transactor with dependencies set of parent and child 
         * Should be called as such:
         * [Transactor Class] [name] = (Transactor Class) this.newTActor(new [Transactor Class]([args]))
         * NOTE: stabilize and checkpoint and msg sends are not supported within the constructor of a tranasactor and will produce unknown outcomes 
         *       Stabilization and checkpoint should be place in a initialize message handler following transactor construction*
         */
        /*** [new] ***/
		public Transactor newTActor(Transactor new_T) {
			String new_name;
			if (new_T.getUAN()!=null) 
                new_name = new_T.getUAN().toString();
			else 
                new_name = new_T.getUAL().toString();
            // adds new t to histMap for both
			wv.getHistMap().put(new_name, new History());
            // sets depGraph to t dependent on parents root set plus parent
			wv.getDepGraph().put(new_name, new HashSet());
			wv.getDepGraph().get(new_name).add(name);
			Iterator i = wv.getRootSet().iterator();
			while (i.hasNext()) {
				wv.getDepGraph().get(new_name).add((String)i.next());
			}
            // adds t to root set of parent
			wv.getRootSet().add(new_name);
			Worldview new_wv = new Worldview(wv.getHistMap(), wv.getDepGraph(), new HashSet());
            // new_T<-setWV(new_wv)
            Object args[] = { new_wv };
            Message msg = new Message( self, new_T, "setWV", args, null, null );
            Object[] propInfo = {"highPriority"};
            msg.setProperty( "priority", propInfo );
            new_T.send(msg);
        
			return new_T;
		}

        /*** [sta1], [sta2] ***/
		public void stabilize() {
			wv.getHistMap().get(name).stabilize();
            // TODO: Save stable state
            // stable and checkpointed saved states would need to be seperate
            // when reloading we reload last saved state (stable or checkpoint) if suddent failure
            // we reload last checkpoint only on normal run time rollback
            // need to determine most recent saved state (stable/checkpoint) to load upon recovery from failure
            // how to recover from failure?
            // on system start, each transactor check for saved state?
		}

        /*** [dep1], [dep2] ***/
		public boolean dependent() {
			return !wv.independent(name);
		}

        // TODO: extend with USL and ftp protocol with better filenaming scheme
		public void checkpoint() {
            // If this Transactor is stable 
            //System.out.println(name + ": " + wv.getHistMap().get(name) + " : checkingpointing......");
			if (!dependent()&&wv.getHistMap().get(name).isStable()) {
                sendGeneratedMessages(); // send out current messages so they won't be saved
                /*** [chk1] ***/
                // Update this history to indicate checkpoint
				wv.getHistMap().get(name).checkpoint();
				HashMap new_histMap = new HashMap();
				new_histMap.put(name, wv.getHistMap().get(name));
				wv = new Worldview();
                // Reset Dep Graph and Root Set 
				wv.setHistMap(new_histMap);

                // Empty mailboxes so we don't stored messages with state
                // Do we want to keep these to restart the network on node failures? 
                Vector temp_mailbox = mailbox;
                Hashtable temp_pendingMessages = pendingMessages;
                Vector temp_unresolvedTokens = unresolvedTokens;
                mailbox = new Vector();
                pendingMessages = new Hashtable();
                unresolvedTokens = new Vector();
                
                ServiceFactory.getTStorage().store(this, USL);

                // Place messages back in mailboxes
                // NOTE: We should end processing of the current message after checkpoint completes
                //       The messages in the mailbox should process as usual 
                mailbox.addAll(temp_mailbox);
                pendingMessages.putAll(temp_pendingMessages);
                unresolvedTokens.addAll(temp_unresolvedTokens);
                
			}
			else {
                /*** [chk2] ***/
                // If we are not stable then empty our root set and stop processing of current message
                //System.out.println(name + " :  not stable, cannot checkpoint...");
				wv.setRootSet(new HashSet());
			}
            // TODO: End parent method here to transition to ready state
            // transactor complier should compile checkpoint; => this.checkpoint(); return;
		}

        /*** [self] ***/
		public Transactor self() {
			return (Transactor) this.getTState("self");
		}

        /* 
         * Force - False: Programatic rollback
         *         True: Dependency rollback
         * TODO: extend with USL and filename scheme
         */
		public void rollback(boolean force, Worldview updatedWV) {
			if (!wv.getHistMap().get(name).isStable()||force) {
                /*** [rol2] ***/
				if (wv.getHistMap().get(name).isPersistent()) {
                    //System.out.println(self + ": rolling back...........");
                   
                    // We need to send out previous messages first to carry on current worldview before a rollback
                    //sendGeneratedMessages(); // End message instead and let process send them, msg init takes care of wv snapshot

                    // Create a placeholder to collect messages while the transactor is in the process of rolling back
                    UAN savedUAN = getUAN();
                    UAL savedUAL = getUAL();
                    ServiceFactory.getNaming().setEntry(savedUAN, savedUAL, new Rollbackholder(savedUAN, savedUAL));
                    SystemService localSystem = ServiceFactory.getSystem();
                 
                    // Rollback current history and set to rollbacked state 
                    // History needs to be set each time on rollback to track multiple rollbacks to increment incarnation
					wv.getHistMap().get(name).rollback();
					HashMap new_histMap = new HashMap();
					new_histMap.put(name, wv.getHistMap().get(name));
					Worldview new_wv = new Worldview();
					new_wv.setHistMap(new_histMap);

                    // For updating WV from dependency invalidation of received message
                    if (updatedWV != null)
                        new_wv = updatedWV;

                    Transactor.State savedState = (Transactor.State) ServiceFactory.getTStorage().get(USL);

                    ActorMemory mem = getActorMemory();
                    Object[] reloadArgs = { savedState, new_wv, mailbox, pendingMessages, unresolvedTokens, mem };
                    Message reloadMsg = new Message(self, localSystem, "reloadTransactor", reloadArgs, null, null, false);
                    localSystem.send(reloadMsg);

                    // For GC purposes, full GC may not be supported or function correctly
                    this.forceAllRefSilent();

                    // This stops this states thread to cause this actor to "die" 
                    this.rollingback = true;
				}
                /*** [rol3] ***/
				else {
					this.destroy();
                    // Messages after a rollback is theoretically illegal...
                    // We don't want to clear __messages because messages before annihilation should be sent out
                    // those occuring after shouldn't not be sent
                    // we need a way to end the parent method that invokes rollback 
                    //__messages.clear();
				}
			}
			else {
                /*** [rol1] ***/
				wv.setRootSet(new HashSet());
			}
            // TODO: End parent method to transition to ready state at the end of rollback
		}

		public String getString() {
			return name+" -> "+wv.getHistMap().get(name).toString()+"\n"+wv.toString();
		}

        // TODO: Handle exceptions
		public boolean setTState(String field, Object newValue) {
            Field myField;

			if (!wv.getHistMap().get(name).isStable()) {
                /*** [set1] ***/
                try {
                    myField = this.getClass().getDeclaredField(field);
                    myField.setAccessible(true);
                    myField.set(this, newValue);
                } catch (NoSuchFieldException e) {
                    try {
                        myField = this.getClass().getField(field);
                        myField.setAccessible(true);
                        myField.set(this, newValue);
                    } catch (NoSuchFieldException f) { 
                        System.out.println("[SETSTATE] no such field exception: " + field);
                        return false; 
                    } catch (IllegalAccessException g) { 
                        System.out.println("[SETSTATE] illegal access exception 1");
                        return false; 
                    }
                } catch (IllegalAccessException g) { 
                    System.out.println("[SETSTATE] illegal access exception 2");
                    return false; 
                }

				if (!wv.getDepGraph().containsKey(name)) {
					wv.getDepGraph().put(name, new HashSet());
				}
				Iterator i = wv.getRootSet().iterator();
				while (i.hasNext()) {
					wv.getDepGraph().get(name).add((String)i.next());
				}
				return true;
			}
            /*** [set2] ***/
			return false;
		}
        
        /*
         * Need to handle member variable retrieval 
         */
        /*** [get] ***/
        // TODO: Handle exceptions
		public Object getTState(String field) {
            Object value;
            Field myField;
            try {
                myField = this.getClass().getDeclaredField(field);
                myField.setAccessible(true);
                value = myField.get(this);
            } catch (NoSuchFieldException e) {
                try {
                    myField = this.getClass().getField(field);
                    myField.setAccessible(true);
                    value = myField.get(this);
                } catch (NoSuchFieldException f) { 
                    System.out.println("[GETSTATE] no such field exception: " + field);
                    return null; 
                } catch (IllegalAccessException g) { 
                    System.out.println("[GETSTATE] illegal access exception 1");
                    return null; 
                }
            } catch (IllegalAccessException g) { 
                System.out.println("[GETSTATE] illegal access exception 2");
                System.out.println(g);
                return null; 
            }

			wv.getRootSet().add(name);
            return value;
		}

        // NOTE: Sending the trigger message to self ot start the transaciton places its name in the root set
        // which may be undesirable in certain circumstances...
        public void transactionStart(String msg, Object[] msg_args, Pinger director){
            this.setTState("transDirector", director);
            this.sendMsg(msg, msg_args, this.self());
        }

        public void pingreq(Transactor[] pingreqs) {
            for (Object t : pingreqs){
                this.sendMsg("ping", new Object[0], (Transactor)t);
            }
            this.checkpoint(); return;
        }

        public void ping() {
            this.checkpoint(); return;
        }

        public void startTransaction(Transactor[] participants, Transactor coordinator, String msg, Object[] msg_args){
            Object[][] transaction = {{participants, coordinator, msg, msg_args}};
            this.sendMsg("startTransaction", transaction, ServiceFactory.getTransDirector());
        }
	}
}
