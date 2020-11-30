/*
 * Author: Martin Novak
 * Date:   Jul 8, 2005
 */
package puakma.test;

/**
 * @author Martin Novak
 */
public class DumbClass
{
  int a;
  
  class B {
    int b;
    
    class C {
      int c;
    }
  }
  
  interface D {
    public static final int d = 1;
  }
  
  D d = new D() {
  };
}
