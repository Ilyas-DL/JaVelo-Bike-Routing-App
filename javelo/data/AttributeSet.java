package ch.epfl.javelo.data;

import ch.epfl.javelo.Preconditions;

import java.util.StringJoiner;

/**
 * Stores the Attributes for a path in a long for performance.
 * @author Eden Kahane (346481).
 */
public record AttributeSet(long bits) {

    /**
     * Stores all {@link Attribute} of a path inside a long.
     * A bit at index b set to 1, indicates that the Attribute of ordinal b is present.
     * @param bits long where each Attribute activated represent a bit set to 1.
     */
    public AttributeSet{
        long maxValue = 1L << (Attribute.COUNT);
        Preconditions.checkArgument(bits < maxValue && bits >= 0);
    }

    /**
     * Generates a AttributeSet where each given Attribute has its
     * Generate an {@link AttributeSet} where each given Attribute has its
     * own bit (index) set to 1.
     * @param attributes a list of Attribute.
     * @return the stored Attributes.
     */
    public static AttributeSet of(Attribute... attributes){
        long cumulate = 0L;
        for (Attribute attribute : attributes) {
            long mask = 1L << attribute.ordinal();
            cumulate = cumulate | mask;
        }
        return new AttributeSet(cumulate);
    }

    /**
     * Checks if an Attribute is present (its index is set to 1).
     * @param attribute the Attribute to check.
     * @return true if the attribute is in it, otherwise false.
     */
    public boolean contains(Attribute attribute){
        return  ((bits >>> attribute.ordinal()) & 1L) == 1L;
    }

    /**
     * Checks if both AttributeSet has any value that intersect between them.
     * @param that Another AttributeSet with which to check the intersection.
     * @return true if there is an Attribute present in both AttributeSet.
     */
    public boolean intersects(AttributeSet that){
        return  (this.bits & that.bits) != 0L;
    }

    @Override
    public String toString(){
        StringJoiner joiner = new StringJoiner(",", "{", "}");
        for (Attribute attribute : Attribute.ALL) {
            if(contains(attribute)) {
                joiner.add(attribute.keyValue());
            }
        }
        return joiner.toString();
    }
}
