package friendshipMod.data;

import java.util.Arrays;
import java.util.stream.Stream;

/**
 * This class logically represents an association between two mobs.
 * It does not contain any values associated with that relationship.
 * It uses the mobs' unique IDs to establish the association.
 * Notably, order is irrelevant. (mob1, mob2) = (mob2, mob1)
 */
public class Association {
    public final Integer[] mobIds;

    public Association(Integer first, Integer second) {
        mobIds = new Integer[2];
        if (first.compareTo(second) > 0) {
            mobIds[0] = first;
            mobIds[1] = second;
        } else {
            mobIds[0] = second;
            mobIds[1] = first;
        }
    }

    public boolean isFor(Integer mobId) {
        return mobIds[0].equals(mobId) || mobIds[1].equals(mobId);
    }

    public boolean sameAs(Association other) {
        Stream<Integer> mobIdStream = Arrays.stream(mobIds);
        return mobIdStream.allMatch(x -> x.equals(other.mobIds[0]) || x.equals(other.mobIds[1]));
    }

    public int first() { return mobIds[0]; }
    public int second() { return mobIds[1]; }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) return false;
        if (!(other instanceof Association)) return false;
        Association typedOther = (Association)other;
        return sameAs(typedOther);
    }

    @Override
    public String toString() {
        return String.format("(%s, %s)", mobIds[0], mobIds[1]);
    }
}
