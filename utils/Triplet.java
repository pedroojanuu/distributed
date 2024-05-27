package utils;

public class Triplet<T1, T2, T3> {
    public T1 first;
    public T2 second;
    public T3 third;

    public Triplet(T1 first, T2 second, T3 third) {
        this.first = first;
        this.second = second;
        this.third = third;
    }

    public String toString() {
        return "(" + first + ", " + second + ", " + third + ")";
    }

    public boolean equals(Object obj){
        if (obj instanceof Triplet) {
            Triplet<?, ?, ?> other = (Triplet<?, ?, ?>) obj;
            return this.first.equals(other.first) && this.second.equals(other.second) && this.third.equals(other.third);
        }
        return false;
    }
}
