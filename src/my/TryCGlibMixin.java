package my;

import static org.junit.Assert.*;

import org.junit.Test;

import net.sf.cglib.proxy.Mixin;

/**
 * A mixin is a construct that allows combining multiple objects into one. 
 * We can include a behavior of a couple of classes and expose that behavior 
 * as a single class or interface.  
 * The cglib Mixins allow the combination of several objects into a single object. 
 * However, in order to do so all objects that are included within a mixin 
 * must be backed by interfaces.
 */

public class TryCGlibMixin {

	//--------existed objects backed with interfaces
	
	public interface Interface1 {
	    String first();
	}
	 
	public interface Interface2 {
	    String second();
	}
	 
	public class Class1 implements Interface1 {
	    @Override
	    public String first() {
	        return "first behaviour";
	    }
	}
	 
	public class Class2 implements Interface2 {
	    @Override
	    public String second() {
	        return "second behaviour";
	    }
	}
	
	//------------joining into one object that implements MixinInterface 
	
	public interface MixinInterface extends Interface1, Interface2 {}
	
	@Test
	public void tryMixin() throws Exception {
		Mixin join = Mixin.create(
				new Class[] { Interface1.class, Interface2.class, MixinInterface.class}, 
				new Object[] { new Class1(), new Class2()}
				);
		MixinInterface joinDelegate = (MixinInterface) join;
		
		assertEquals("first behaviour", joinDelegate.first());
		assertEquals("second behaviour", joinDelegate.second());
	}
}
