package my;

public class TestClass {
	
	String input;
	
	public TestClass() {
		System.out.println("creating TestClass");//, hashcode=" + this.hashCode());
	}
	
	public TestClass(String delegated) {
		this.input = delegated;
	}
	
	class Something {
    	private int i=1;
    	public int getCounter() {
    		return i;
    	}
    }

  public String test() {
    return (input == null) ? "Hello world!" : input;
  }
  
  public String test(String str) {
	    return "Hello world!";
	  }
  
  public Something getSomething() {
	  System.out.println("get something, but hashCode=" + this.hashCode());
	  return new Something();
  }
  
  public String delegate() {
	  return "delegated..";
  }
}