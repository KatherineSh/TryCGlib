package my;

import static org.junit.Assert.*;

import java.lang.reflect.Method;

import org.junit.Test;

import net.sf.cglib.beans.BeanGenerator;


public class TestCGlibGenerators {

	/**
	 *  BeanGenerator allows us to dynamically create beans and to add fields 
	 *  together with setter and getter methods. 
	 *  It can be used by code generation tools to generate simple POJO objects.
	 */
	@Test
	public void tryBeanGenerator() throws Exception {
		BeanGenerator beanGenerator = new BeanGenerator();
		
		beanGenerator.addProperty("hidenField", String.class);
		Object myBean = beanGenerator.create();

		Method setter = myBean.getClass().getMethod("setHidenField", String.class);
		setter.invoke(myBean, "some string value set by a cglib");

		Method getter = myBean.getClass().getMethod("getHidenField");
		assertEquals("some string value set by a cglib", getter.invoke(myBean));
	}
}
