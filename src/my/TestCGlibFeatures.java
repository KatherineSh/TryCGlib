package my;
import static org.junit.Assert.*;

import java.lang.reflect.Method;

import org.junit.Before;
import org.junit.Test;

import my.TestClass.Something;
import net.sf.cglib.proxy.Dispatcher;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.FixedValue;
import net.sf.cglib.proxy.InvocationHandler;
import net.sf.cglib.proxy.LazyLoader;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import net.sf.cglib.proxy.ProxyRefDispatcher;


public class TestCGlibFeatures {
	
	private Enhancer enhancer;
	
	@Before
	public void setUp(){
		enhancer = new Enhancer();
	}

	//------- FixedValue -----
	/**
	 * The FixedValue is a callback interface that simply returns the value from the proxied method.
	 * It has some drawbacks because we are not able to decide which method a proxy should intercept,
	 * and which method should be invoked from a superclass. 
	 */
	
	@Test
	public void tryFixedValue_ok() throws Exception {
	  enhancer.setSuperclass(TestClass.class);
	  /*enhancer.setCallback(new FixedValue() {
	    @Override
	    public Object loadObject() throws Exception {
	      return "Hello cglib!";
	    }
	  });*/
	  enhancer.setCallback((FixedValue) () -> "Hello cglib!");
	  
	  TestClass proxy = (TestClass) enhancer.create();
	  String result = proxy.test(null);
	  System.out.println(result);
	  System.out.println("class returned: " + proxy.getClass());
	  assertEquals("Hello cglib!", result);
	}
	
	@Test(expected = Exception.class)
	public void tryFixedValue_whenCallHashCode() {
	  enhancer.setSuperclass(TestClass.class);
	  enhancer.setCallback(new FixedValue() {
	    @Override
	    public Object loadObject() throws Exception {
	      return "Hello cglib!";
	    }
	  });
	  
	  TestClass proxy = (TestClass) enhancer.create();
	  proxy.hashCode();
	  }
	
	/* throw java.lang.IllegalArgumentException: Cannot subclass final class FinalTestClass */
	@Test(expected=IllegalArgumentException.class)
	public void tryFixedValue_whenFinal() {
	  enhancer.setSuperclass(FinalTestClass.class);
	  enhancer.setCallback(new FixedValue() {
	    @Override
	    public Object loadObject() throws Exception {
	      return "Hello cglib!";
	    }
	  });
	  
	  FinalTestClass proxy = (FinalTestClass) enhancer.create();
	  proxy.test(null);
	  }
	
	//------- InvocationHandler -----
	
	/*  Any method call will call the same InvocationHandler (can result in an endless loop) */
	@Test(expected=RuntimeException.class)
	public void tryInvocationHandler() throws Exception {
	  Enhancer enhancer = new Enhancer();
	  enhancer.setSuperclass(TestClass.class);
	  enhancer.setCallback(new InvocationHandler() {
	    @Override
	    public Object invoke(Object proxy, Method method, Object[] args) 
	        throws Throwable {
	      if (method.getDeclaringClass() != Object.class && method.getReturnType() == String.class) {
	        return "Hello cglib!";
	      } else {
	        throw new RuntimeException("Do not know what to do.");
	      }
	    }
	  });
	  TestClass proxy = (TestClass) enhancer.create();
	  
	  assertEquals("Hello cglib!", proxy.test(null));
	  assertNotEquals("Hello cglib!", proxy.toString());
	}
	
	//---------MethodInterceptor------
	/**
	 * The MethodInterceptor allows full control over the intercepted method and offers some 
	 * utilities for calling the method of the enhanced class in their original state. 
	 * The other methods are more efficient and cglib is often used in edge case frameworks 
	 * where efficiency plays a significant role. 
	 * The creation and linkage of the MethodInterceptor requires for example the generation 
	 * of a different type of byte code and the creation of some runtime objects that 
	 * are not required with the InvocationHandler. Because of that, there are other classes 
	 * that can be used with the Enhancer.
	 * 
	 */
	
	@Test
	public void tryMethodInterceptor() throws Exception {
	  Enhancer enhancer = new Enhancer();
	  enhancer.setSuperclass(TestClass.class);
	  /*enhancer.setCallback(new MethodInterceptor() {
	    @Override
	    public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy)
	        throws Throwable {
	      if (method.getDeclaringClass() != Object.class && method.getReturnType() == String.class) {
	        return "Hello cglib!";
	      } else {
	        return proxy.invokeSuper(obj, args);
	      }
	    }
	  });*/
	  enhancer.setCallback((MethodInterceptor) (obj, method, args, proxy) -> {
		  //Not intercepting all calls when method signature is not from the Object class,
		  //meaning that i.e. toString() or hashCode() methods will not be intercepted. 
		  //Besides that, we are intercepting only methods that returns a String. 
		    if (method.getDeclaringClass() != Object.class && method.getReturnType() == String.class) {
		        return "Hello cglib!";
		    } else {
		        return proxy.invokeSuper(obj, args);
		    }
		});
	  
	  TestClass proxy = (TestClass) enhancer.create();
	  assertEquals("Hello cglib!", proxy.test(null));
	  assertNotEquals("Hello cglib!", proxy.toString());
	  proxy.hashCode(); // Does not throw an exception or result in an endless loop.
	}

	//------- LazyLoader-------
	/**
	 * The LazyLoader is actually supposed to return an instance of a subclass of the enhanced class. 
	 * This instance is requested only when a method is called on the enhanced object and then stored 
	 * for future invocations of the generated proxy. . This makes sense if your object is expensive 
	 * in its creation without knowing if the object will ever be used. 
	 */
	
	@Test
	public void tryLazyLoader() throws Exception {
	  Enhancer enhancer = new Enhancer();
	  enhancer.setSuperclass(TestClass.class);
	  enhancer.setCallback(new LazyLoader() {
		@Override
			public Object loadObject() throws Exception {
				return new TestClass();
				};
	  });
	  TestClass proxy = (TestClass) enhancer.create();
	  Something hh1 = proxy.getSomething();
	  Something hh2 = proxy.getSomething();
	}
	
	//-------Dispatcher-------
	/**
	 * The Dispatcher is like the LazyLoader but will be invoked on every method call without 
	 * storing the loaded object. This allows to change the implementation of a class without 
	 * changing the reference to it. Again, be aware that some constructor must be called for 
	 * both the proxy and the generated objects.
	 */
	
	@Test
	public void tryDispatcher() throws Exception {
	  Enhancer enhancer = new Enhancer();
	  enhancer.setSuperclass(TestClass.class);
	  enhancer.setCallback(new Dispatcher() {
		@Override
			public Object loadObject() throws Exception {
				return new TestClass();
				};
	  });
	  TestClass proxy = (TestClass) enhancer.create();
	  Something hh1 = proxy.getSomething();
	  Something hh2 = proxy.getSomething();
	}
	
	//----------ProxyRefDispatcher-------
	/**
	 * This class carries a reference to the proxy object it is invoked 
	 * from in its signature. This allows for example to delegate method 
	 * calls to another method of this proxy. 
	 * Be aware that this can easily 
	 * cause an endless loop and will always cause an endless loop if the same 
	 * method is called from within ProxyRefDispatcher#loadObject(Object).
	 */
	
	@Test
	public void tryProxyRefDispatcher() throws Exception {
	  Enhancer enhancer = new Enhancer();
	  enhancer.setSuperclass(TestClass.class);
	  enhancer.setCallback(new ProxyRefDispatcher() {
		@Override
		public Object loadObject(Object arg0) throws Exception {
			return bye(arg0);
		};
		public Object bye(Object arg0) throws Exception {
			return new TestClass("delegaated");
		};
	  });
	  
	  TestClass proxy = (TestClass) enhancer.create(); //1 call of loadObject
	  String v = proxy.test(); //2 call of loadObject
	  assertEquals("delegaated", v);
	}
}
