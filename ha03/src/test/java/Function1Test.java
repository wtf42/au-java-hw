import org.junit.Test;

import static org.junit.Assert.*;

public class Function1Test {
    @Test
    public void testCompose() throws Exception {
        Function1<Integer, Derived> f = arg -> new Derived(arg);
        Function1<Base, Long> g = arg -> new Long(arg.getX() + 1);
        assertEquals(2L, (long)f.compose(g).apply(1));
    }
}
