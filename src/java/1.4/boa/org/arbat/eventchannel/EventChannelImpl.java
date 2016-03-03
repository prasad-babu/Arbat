/*
 * ************************************************************
 *     Arbat - Open Source Java CORBA services implementation *
 * ************************************************************
 *   $Id: EventChannelImpl.java,v 1.1 2006/12/18 07:38:14 drogatkin Exp $
 */
package org.arbat.eventchannel;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Properties;
import edu.emory.mathcs.backport.java.util.concurrent.LinkedBlockingQueue;
import edu.emory.mathcs.backport.java.util.concurrent.ThreadPoolExecutor;
import edu.emory.mathcs.backport.java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.io.IOException;
import org.omg.CORBA.Any;
import org.omg.CORBA.BooleanHolder;
import org.omg.CORBA.BAD_PARAM;
import org.omg.CORBA.ORB;
import org.omg.CosEventChannelAdmin.*;
import org.omg.CosEventComm.*;
import org.omg.CosEventChannelAdmin.*;

public class EventChannelImpl extends _EventChannelImplBase {
	protected ConsumerAdmin consumerAdmin;
	protected SupplierAdmin supplierAdmin;
	protected List/*<PushConsumer>*/ pushConsumers;
	protected List/*<PullConsumer>*/ pullConsumers;
	protected List/*<PushSupplier>*/ pushSuppliers;
	protected List/*<PullSupplier>*/ pullSuppliers;
	
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
		//eventQueue = new LinkedBlockingQueue<Any>(100); // config
		int preSize = getIntPropWithDefault(ESTIMATE_CONSUMERS_PROP, 10, configuration);
		pushConsumers = new ArrayList/*<PushConsumer>*/(preSize);
		pullConsumers = new ArrayList/*<PullConsumer>*/(preSize);
		pushSuppliers = new ArrayList/*<PushSupplier>*/(preSize);
		pullSuppliers = new ArrayList/*<PullSupplier>*/(preSize);
		threadPool = new ThreadPoolExecutor(getIntPropWithDefault(THREADS_PROP, 10, configuration),
				getIntPropWithDefault(MAX_THREADS_PROP, 20, configuration),
				getIntPropWithDefault(THREAD_ALIVE_SEC_PROP, 180, configuration),
				TimeUnit.SECONDS,
				new LinkedBlockingQueue/*<Runnable>*/(getIntPropWithDefault(QUEUE_CAP_PROP, Integer.MAX_VALUE, configuration)));
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
             //   try {
                    consumerAdmin = ConsumerAdminHelper.narrow(new ConsumerAdminImpl());
               /// } catch(org.omg.CORBA.UserException ue) {
              //      errorReport("ConsumerAdminImpl", null, ue);
               // }
		}
		return consumerAdmin;
	}

	public SupplierAdmin for_suppliers () {
		synchronized(this) {
			if (supplierAdmin == null)
//                try {
                    supplierAdmin = SupplierAdminHelper.narrow(new SupplierAdminImpl());
  //              } catch(org.omg.CORBA.UserException ue) {
    //                errorReport("ConsumerAdminImpl", null, ue);
      //          }
		}
		return supplierAdmin;
	}
	
	public void destroy () {
	}

	class ConsumerAdminImpl extends _ConsumerAdminImplBase {
		ProxyPushSupplier proxyPushSupplier;
		ProxyPullSupplier proxyPullSupplier;
		public ProxyPushSupplier obtain_push_supplier () {
			synchronized(this) {
				if (proxyPushSupplier == null)
             //       try {
                        proxyPushSupplier = ProxyPushSupplierHelper.narrow(new ProxyPushSupplierImpl());
               //     } catch(org.omg.CORBA.UserException ue) {
                 //       errorReport("ConsumerAdminImpl", null, ue);
                   // }
			}
			return proxyPushSupplier;
		}
		
		public ProxyPullSupplier obtain_pull_supplier () {
			synchronized(this) {
				if (proxyPullSupplier == null)
//                    try {
                        proxyPullSupplier = ProxyPullSupplierHelper.narrow(new ProxyPullSupplierImpl());
  //                  } catch(org.omg.CORBA.UserException ue) {
    //                    errorReport("ConsumerAdminImpl", null, ue);
      //              }
			}
			return proxyPullSupplier;
		}
	}
	
	class ProxyPushSupplierImpl extends _ProxyPushSupplierImplBase {
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
			}
		}
		
		public void disconnect_push_supplier () {
			Iterator i = pushConsumers.iterator();
			while(i.hasNext())
				((PushConsumer)i.next()).disconnect_push_consumer ();
		}
	}
	
	class ProxyPullSupplierImpl extends _ProxyPullSupplierImplBase {
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
			Iterator i = pullConsumers.iterator();
			while ( i.hasNext() )
				((PullConsumer)i.next()).disconnect_pull_consumer();
		}
	}
	
	class SupplierAdminImpl extends _SupplierAdminImplBase {
		ProxyPushConsumer proxyPushConsumer;
		ProxyPullConsumer proxyPullConsumer;
		public ProxyPushConsumer obtain_push_consumer () {
			synchronized(this) {
				if (proxyPushConsumer == null)
        //            try {
                        proxyPushConsumer = ProxyPushConsumerHelper.narrow(new ProxyPushConsumerImpl());
          //          } catch(org.omg.CORBA.UserException ue) {
            //            errorReport("ConsumerAdminImpl", null, ue);
              //      }
			}
			return proxyPushConsumer;
		}
		
		public ProxyPullConsumer obtain_pull_consumer () {
			synchronized(this) {
				if (proxyPullConsumer == null)
                //    try {
                        proxyPullConsumer = ProxyPullConsumerHelper.narrow(new ProxyPullConsumerImpl());
                  //  } catch(org.omg.CORBA.UserException ue) {
                    //    errorReport("ConsumerAdminImpl", null, ue);
                   // }
			}
			return proxyPullConsumer;
		}
	}
	
	class ProxyPushConsumerImpl extends _ProxyPushConsumerImplBase {
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
					List pushConsumersClone;
					synchronized(pushConsumers) {
						pushConsumersClone = (List)((ArrayList)pushConsumers).clone();
					}
					Iterator i = pushConsumersClone.iterator();
					while( i.hasNext() ) {
						final PushConsumer push_consumer = (PushConsumer)i.next();
						threadPool.execute(new Runnable() {
							public void run() {
								try {
									push_consumer.push(data);
								} catch (Disconnected de) {
									removeDisconnectedConsumer(push_consumer);
								} catch (org.omg.CORBA.COMM_FAILURE cf) { // org.omg.CORBA.SystemException
									removeDisconnectedConsumer(push_consumer);
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
			Iterator i =  pushSuppliers.iterator();
			while( i.hasNext() )
				((PushSupplier)i.next()).disconnect_push_supplier();
		}
		
		protected void removeDisconnectedConsumer(PushConsumer push_consumer) {
			try {
				push_consumer.disconnect_push_consumer();
			} catch (org.omg.CORBA.COMM_FAILURE cf) { // org.omg.CORBA.SystemException
			}
			synchronized(pushConsumers) {
				pushConsumers.remove(push_consumer);
			}
			
		}

		protected void removeBadConsumer(PushConsumer push_consumer) {
			synchronized(pushConsumers) {
				Iterator pci = pushConsumers.iterator();
				while (pci.hasNext())
//					if (pci.next() == push_consumer) {
					// TODO verify if it works
					if (push_consumer._is_equivalent((org.omg.CORBA.Object)pci.next())) { 
						pci.remove(); 
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
	
	class ProxyPullConsumerImpl extends _ProxyPullConsumerImplBase {
		public void connect_pull_supplier (PullSupplier pull_supplier) 
			throws AlreadyConnected, TypeError {
			if (pull_supplier == null)
				throw new BAD_PARAM();
		}
		
		public void disconnect_pull_consumer () {
		}
	}
}