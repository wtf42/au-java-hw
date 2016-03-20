import org.junit.Test;

import static org.junit.Assert.*;

public class Function2Test {

    @Test
    public void testBind1() throws Exception {
        Function2<Long, Base, Long> g = (a1, a2) -> a1 * a2.getX();
        assertEquals(84L, (long)g.bind2(new Derived(42)).apply(2L));
    }

    @Test
    public void testBind2() throws Exception {
        Function2<Base, Long, Long> g = (a1, a2) -> a1.getX() * a2;
        assertEquals(84L, (long)g.bind1(new Derived(42)).apply(2L));
    }

    @Test
    public void testCompose() throws Exception {
        Function2<Integer, Integer, Derived> f = (a1, a2) -> new Derived(a1 * a2);
        Function1<Base, Long> g = arg -> arg.getX() + 1L;
        assertEquals(7L, (long)f.compose(g).apply(2, 3));
    }

    @Test
    public void testCarry() throws Exception {
        Function2<Integer, Integer, Integer> mul = (a1, a2) -> a1 * a2;
        Function1<Integer, Integer> mul2 = mul.carry().apply(2);
        assertEquals(10, (int)mul2.apply(5));
    }
}