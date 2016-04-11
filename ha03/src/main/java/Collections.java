import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class Collections {

    public static <A, R> List<R> map(Function1<? super A, R> f, Iterable<A> a) {
        List<R> res = new LinkedList<>();
        for (A elem : a) {
            res.add(f.apply(elem));
        }
        return res;
    }

    public static <A> List<A> filter(Predicate<? super A> p, Iterable<A> a) {
        List<A> res = new LinkedList<>();
        for (A elem : a) {
            if (p.apply(elem)) {
                res.add(elem);
            }
        }
        return res;
    }

    public static <A> List<A> takeWhile(Predicate<? super A> p, Iterable<A> a) {
        List<A> res = new LinkedList<>();
        for (A elem : a) {
            if (!p.apply(elem)) {
                return res;
            }
            res.add(elem);
        }
        return res;
    }

    public static <A> List<A> takeUnless(Predicate<? super A> p, Iterable<A> a) {
        return takeWhile(p.not(), a);
    }

    public static <A, R> R foldl(Function2<R, ? super A, R> f, R z, Iterable<A> a) {
        R res = z;
        for (A elem : a) {
            res = f.apply(res, elem);
        }
        return res;
    }

    public static <A, R> R foldr(Function2<? super A, R, R> f, R z, Iterable<A> a) {
        return foldrRec(f, z, a.iterator());
    }

    private static <A, R> R foldrRec(Function2<? super A, R, R> f, R z, Iterator<A> it) {
        if (!it.hasNext()) {
            return z;
        }
        A elem = it.next();
        return f.apply(elem, foldrRec(f, z, it));
    }
}
