
class Base {
    Integer x;
    Base(Integer x) { this.x=x; }
    Integer getX() { return x; }
}

class Derived extends Base {
    Derived(Integer x) { super(x); }
}
