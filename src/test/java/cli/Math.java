
import gps.annotations.Constraint;
import gps.annotations.Variable;

/**
 * @author malu@informatik.uni-bremen.de
 */
public class Math{

  @Constraint
  public boolean testBitAnd(int a) {
    return ((6 & a) == 2);
  }

  @Variable
  int y = 1;

  @Constraint
  public boolean testAdd(int a, int b) {
    return a + b + y == 5;
  }

}
