
public interface Predicate<A> extends Function1<A, Boolean> {
    Predicate<Object> ALWAYS_TRUE = arg -> true;
    Predicate<Object> ALWAYS_FALSE = arg -> false;

    default Predicate<A> or(Predicate<? super A> g) {
        return arg -> apply(arg) || g.apply(arg);
    }

    default Predicate<A> and(Predicate<? super A> g) {
        return arg -> apply(arg) && g.apply(arg);
    }

    default Predicate<A> not() {
        return arg -> !apply(arg);
    }
}
