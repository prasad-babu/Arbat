/*
 * ************************************************************
 *     Arbat - Open Source Java CORBA services implementation *
 * ************************************************************
 *   $Id: EventChannelImpl.java,v 1.7 2006/11/22 02:10:42 drogatkin Exp $
 */
package org.arbat.eventchannel;
import java.util.List;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Iterator;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.io.IOException;
import org.omg.CORBA.Any;
import org.omg.CORBA.BooleanHolder;
import org.omg.CORBA.BAD_PARAM;
import org.omg.CORBA.ORB;
import org.omg.CosEventChannelAdmin.ConsumerAdmin;
import org.omg.CosEventChannelAdmin.SupplierAdmin;
import org.omg.CosEventChannelAdmin.ProxyPushSupplier;
import org.omg.CosEventChannelAdmin.ProxyPullSupplier;
import org.omg.CosEventChannelAdmin.ProxyPushConsumer;
import org.omg.CosEventChannelAdmin.ProxyPullConsumer;
import org.omg.CosEventComm.PushConsumer;
import org.omg.CosEventComm.PullConsumer;
import org.omg.CosEventComm.PushSupplier;
import org.omg.CosEventComm.PullSupplier;
import org.omg.CosEventChannelAdmin.AlreadyConnected;
import org.omg.CosEventChannelAdmin.TypeError;
import org.omg.CosEventComm.Disconnected;
import org.omg.CosEventChannelAdmin.ProxyPullSupplierHelper;
import org.omg.CosEventChannelAdmin.ProxyPushSupplierHelper;
import org.omg.CosEventChannelAdmin.ProxyPushSupplierPOA;
import org.omg.CosEventChannelAdmin.EventChannelPOA;
import org.omg.CosEventChannelAdmin.ConsumerAdminPOA;
import org.omg.CosEventChannelAdmin.ConsumerAdminHelper;
import org.omg.CosEventChannelAdmin.ProxyPullSupplierPOA;
import org.omg.CosEventChannelAdmin.SupplierAdminHelper;
import org.omg.CosEventChannelAdmin.SupplierAdminPOA;
import org.omg.CosEventChannelAdmin.ProxyPullConsumerHelper;
import org.omg.CosEventChannelAdmin.ProxyPullConsumerPOA;
import org.omg.CosEventChannelAdmin.ProxyPushConsumerHelper;
import org.omg.CosEventChannelAdmin.ProxyPushConsumerPOA;
import org.omg.PortableServer.POAManagerPackage.AdapterInactive;
import org.omg.PortableServer.POAManagerPackage.State;

public class EventChannelImpl extends EventChannelPOA {
    protected org.omg.PortableServer.POA rootPoa;
	protected ConsumerAdmin consumerAdmin;
	protected SupplierAdmin supplierAdmin;
	protected List<PushConsumer> pushConsumers;
	protected List<PullConsumer> pullConsumers;
	protected List<PushSupplier> pushSuppliers;
	protected List<PullSupplier> pullSuppliers;
	
	//protected LinkedBlockingQueue<Any> eventQueue;
	protected ThreadPoolExecutor threadPool; 
	public static final String CONFIG_PROP_CLASS = "org.arbat.eventchannel.Configurator";
	protected static final String PROJECT_NAME = "arbat";
	protected static final String THREADS_PROP = "org.arbat.eventchannel.startThreads";
	protected static final String MAX_THREADS_PROP = "org.arbat.eventchannel.maxThreads";
	protected static final String QUEUE_CAP_PROP = "org.arbat.eventchannel.queueCapacity";
	protected static final String THREAD_ALIVE_SEC_PROP = "org.arbat.eventchannel.keepThreadAliveSec";
	protected static final String ESTIMATE_CONSUMERS_PROP = "org.arbat.eventchannel.estimateNumConsumers";
	
	/**
	 * parameters to set event queue size 100 worker thread initial number 10
	 * worker thread max number 15 keep alive 180 secs execution queue size
	 */
	public EventChannelImpl() {
		// TODO: considering using java.util.prefs.Preferences
		Properties configuration = null;
		String configClassName = System.getProperty(CONFIG_PROP_CLASS);
		if (configClassName != null)
			try {
				configuration = (Properties)Class.forName(configClassName).newInstance();
			} catch(Error er) {
				errorReport("Can't instantiate config class, default configuration will be used.", null, er);
			} catch(Exception ex) {
				errorReport("Can't instantiate config class, default configuration will be used.", null, ex);
			}		
		init(configuration);
	}
	
	public EventChannelImpl(ORB orb, Properties properties) {
        try {
            rootPoa = org.omg.PortableServer.POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
        
            if (State.ACTIVE.equals(rootPoa.the_POAManager().get_state()) == false) 
                throw new AdapterInactive("Root POA is inactive or other not operable state, channel isn't operable");
        } catch(org.omg.CORBA.UserException ue) {
            errorReport("Problem in obtaining POA root.", null, ue);
            return;
        }
		init(properties);
	}

	protected void init(Properties configuration) {
		if (configuration == null) {
			configuration = new Properties();
			//System.err.println("S:"+getClass().getResourceAsStream("/resource/configuration.properties"));
			try {
				configuration.load(getClass().getResourceAsStream("/resource/configuration.properties"));
			} catch(IOException ioe) {
				errorReport("Default configuration is not availalbe, check packaging.", null, ioe);
			} catch(NullPointerException npe) {
				errorReport("Default configuration is not availalbe, check packaging.", null, npe);
			}
		}		
		System.out.println("Event channel configuration: "+configuration);
		int preSize = getIntPropWithDefault(ESTIMATE_CONSUMERS_PROP, 10, configuration);
		pushConsumers = new ArrayList<PushConsumer>(preSize);
		pullConsumers = new ArrayList<PullConsumer>(preSize);
		pushSuppliers = new ArrayList<PushSupplier>(preSize);
		pullSuppliers = new ArrayList<PullSupplier>(preSize);
		threadPool = new ThreadPoolExecutor(getIntPropWithDefault(THREADS_PROP, 10, configuration),
				getIntPropWithDefault(MAX_THREADS_PROP, 20, configuration),
				getIntPropWithDefault(THREAD_ALIVE_SEC_PROP, 180, configuration),
				TimeUnit.SECONDS,
				new LinkedBlockingQueue<Runnable>(getIntPropWithDefault(QUEUE_CAP_PROP, Integer.MAX_VALUE, configuration)));
	}
			
	protected void errorReport(String errorMessage, Object[] parameters, Throwable t) {
		String message = PROJECT_NAME+":error "+errorMessage;
		if (t != null)
			Logger.getLogger(PROJECT_NAME).throwing(message, "", t);
		else
			Logger.getLogger(PROJECT_NAME).severe(message);
	}
	
	// TODO: push in some util class
	protected int getIntPropWithDefault(String name, int defVal, Properties properties) {
		int result = defVal;
		try {
			result = Integer.parseInt(properties.getProperty(name));
		} catch(Exception e) {
			// use default
		}
		return result;
	}
	
	public ConsumerAdmin for_consumers () {
		synchronized(this) {
			if (consumerAdmin == null) 
                try {
                    consumerAdmin = ConsumerAdminHelper.narrow(rootPoa.servant_to_reference(new ConsumerAdminImpl()));
                } catch(org.omg.CORBA.UserException ue) {
                    errorReport("ConsumerAdminImpl", null, ue);
                }
		}
		return consumerAdmin;
	}

	public SupplierAdmin for_suppliers () {
		synchronized(this) {
			if (supplierAdmin == null)
                try {
                    supplierAdmin = SupplierAdminHelper.narrow(rootPoa.servant_to_reference(new SupplierAdminImpl()));
                } catch(org.omg.CORBA.UserException ue) {
                    errorReport("ConsumerAdminImpl", null, ue);
                }
		}
		return supplierAdmin;
	}
	
	public void destroy () {
	}

	class ConsumerAdminImpl extends ConsumerAdminPOA {
		ProxyPushSupplier proxyPushSupplier;
		ProxyPullSupplier proxyPullSupplier;
		public ProxyPushSupplier obtain_push_supplier () {
			synchronized(this) {
				if (proxyPushSupplier == null)
                    try {
                        proxyPushSupplier = ProxyPushSupplierHelper.narrow(rootPoa.servant_to_reference(new ProxyPushSupplierImpl()));
                    } catch(org.omg.CORBA.UserException ue) {
                        errorReport("ConsumerAdminImpl", null, ue);
                    }
			}
			return proxyPushSupplier;
		}
		
		public ProxyPullSupplier obtain_pull_supplier () {
			synchronized(this) {
				if (proxyPullSupplier == null)
                    try {
                        proxyPullSupplier = ProxyPullSupplierHelper.narrow(rootPoa.servant_to_reference(new ProxyPullSupplierImpl()));
                    } catch(org.omg.CORBA.UserException ue) {
                        errorReport("ConsumerAdminImpl", null, ue);
                    }
			}
			return proxyPullSupplier;
		}
	}
	
	class ProxyPushSupplierImpl extends ProxyPushSupplierPOA {
		public void connect_push_consumer (PushConsumer push_consumer) 
			throws AlreadyConnected, TypeError {
			if (push_consumer == null)
				throw new BAD_PARAM();
			if (push_consumer instanceof Object == false)
				throw new TypeError(push_consumer.getClass().getName());
			synchronized(pushConsumers) {
				if (pushConsumers.contains(push_consumer))
					throw new AlreadyConnected();
				pushConsumers.add(push_consumer);
				if (_debug)
					errorReport("Added push cons: %s\n", new Object[] {push_consumer}, null); // debug
			}
		}
		
		public void disconnect_push_supplier () {
			for(PushConsumer pushConsumer:pushConsumers)
				pushConsumer.disconnect_push_consumer ();
		}
	}
	
	class ProxyPullSupplierImpl extends ProxyPullSupplierPOA {
		public void connect_pull_consumer (PullConsumer pull_consumer) throws AlreadyConnected {
			synchronized(pullConsumers) {
				if (pullConsumers.contains(pull_consumer))
					throw new AlreadyConnected();
				pullConsumers.add(pull_consumer);
			}
		}
		
		public Any pull () throws Disconnected {
			return null;
		}
		
		public Any try_pull (BooleanHolder has_event) throws Disconnected {
			return null;
		}
		
		public void disconnect_pull_supplier () {
			for (PullConsumer pullConsumer:pullConsumers)
				pullConsumer.disconnect_pull_consumer();
		}
	}
	
	class SupplierAdminImpl extends SupplierAdminPOA {
		ProxyPushConsumer proxyPushConsumer;
		ProxyPullConsumer proxyPullConsumer;
		public ProxyPushConsumer obtain_push_consumer () {
			synchronized(this) {
				if (proxyPushConsumer == null)
                    try {
                        proxyPushConsumer = ProxyPushConsumerHelper.narrow(rootPoa.servant_to_reference(new ProxyPushConsumerImpl()));
                    } catch(org.omg.CORBA.UserException ue) {
                        errorReport("ConsumerAdminImpl", null, ue);
                    }
			}
			return proxyPushConsumer;
		}
		
		public ProxyPullConsumer obtain_pull_consumer () {
			synchronized(this) {
				if (proxyPullConsumer == null)
                    try {
                        proxyPullConsumer = ProxyPullConsumerHelper.narrow(rootPoa.servant_to_reference(new ProxyPullConsumerImpl()));
                    } catch(org.omg.CORBA.UserException ue) {
                        errorReport("ConsumerAdminImpl", null, ue);
                    }
			}
			return proxyPullConsumer;
		}
	}
	
	class ProxyPushConsumerImpl extends ProxyPushConsumerPOA {
		public void connect_push_supplier (PushSupplier push_supplier)
			throws AlreadyConnected {
			if (push_supplier != null) {
				if (pushSuppliers.contains(push_supplier))
					throw new AlreadyConnected();
				pushSuppliers.add(push_supplier);				
			}
		}
		public void push (final Any data) throws Disconnected {
			threadPool.execute(new Runnable() {
				public void run() {
					// consider CopyOnWriteArrayList
					List<PushConsumer> clonePushConsumers;
					synchronized(pushConsumers) {
						clonePushConsumers = (List<PushConsumer>) ((ArrayList)pushConsumers).clone();
					}
					if (_debug)
						errorReport("Notifying : %s cons-s.\n", new Object[]{clonePushConsumers},null); // debug
					for(PushConsumer pushConsumer:clonePushConsumers) {
							final PushConsumer push_consumer = pushConsumer;
							threadPool.execute(new Runnable() {
								public void run() {
									try {
										push_consumer.push(data);
									} catch (Disconnected de) {
										removeBadConsumer(push_consumer);
									} catch (org.omg.CORBA.COMM_FAILURE cf) { // org.omg.CORBA.SystemException
										removeBadConsumer(push_consumer);
									} catch (org.omg.CORBA.TRANSIENT t) { // org.omg.CORBA.SystemException
										removeBadConsumer(push_consumer);
									}
								}
						    });
						}
				}
				});
			//if (eventQueue.offer (data))
			//	;
		}

		public void disconnect_push_consumer () {
			for(PushSupplier pushSupplier:pushSuppliers)
				pushSupplier.disconnect_push_supplier();
		}
		
		protected void removeBadConsumer(PushConsumer push_consumer) {
			synchronized(pushConsumers) {
				Iterator <PushConsumer> pci = pushConsumers.iterator();
				while (pci.hasNext())
//					if (pci.next() == push_consumer) {
					if (push_consumer._is_equivalent(pci.next())) { 
						pci.remove(); 
						if (_debug)
							errorReport("removed push cons: %s\n", new Object[]{push_consumer},(Throwable)null ); // debug
						break; // can be more that one entry?
					}
			}
			try {
				if (push_consumer._non_existent() == false)
					push_consumer.disconnect_push_consumer();
			} catch(org.omg.CORBA.SystemException ce) { // obviously fails
			}
		}
	}
	
	class ProxyPullConsumerImpl extends ProxyPullConsumerPOA {
		public void connect_pull_supplier (PullSupplier pull_supplier) 
			throws AlreadyConnected, TypeError {
			if (pull_supplier == null)
				throw new BAD_PARAM();
		}
		
		public void disconnect_pull_consumer () {
		}
	}
	
	private static final boolean _debug = false;
}
