package com.zimmermusic.kenobi;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.math3.fraction.Fraction;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by az on 1/19/16.
 */
public class Relationship {

  public static Fraction ONE_NINTH = new Fraction(1, 9);
  public static Fraction ONE_EIGHTH = new Fraction(1, 8);
  public static Fraction ONE_SEVENTH = new Fraction(1, 7);
  public static Fraction ONE_SIXTH = new Fraction(1, 6);
  public static Fraction ONE_FIFTH = new Fraction(1, 5);
  public static Fraction ONE_FOURTH = new Fraction(1, 4);
  public static Fraction ONE_THIRD = new Fraction(1, 3);
  public static Fraction ONE_HALF = new Fraction(1, 2);
  public static Fraction TWO_THIRDS = new Fraction(2, 3);
  public static Fraction THREE_FOURTHS = new Fraction(3,4);
  public static Fraction SAME = new Fraction(1, 1);
  public static Fraction FOUR_THREE = new Fraction(4, 3);
  public static Fraction DOUBLE = new Fraction(2, 1);
  public static Fraction FIVE_TWO = new Fraction(5, 2);
  public static Fraction TWO_AND_HALF = new Fraction(3, 2);
  public static Fraction SEVEN_TWO = new Fraction(7, 2);
  public static Fraction THREE = new Fraction(3, 1);
  public static Fraction FOUR = new Fraction(4, 1);
  public static Fraction FIVE = new Fraction(5, 1);
  public static Fraction NINE_TWO = new Fraction(9, 2);
  public static Fraction SIX = new Fraction(6, 1);
  public static Fraction SEVEN = new Fraction(7, 1);
  public static Fraction EIGHT = new Fraction(8, 1);
  public static Fraction NINE = new Fraction(9, 1);

  public static Set<Fraction> DURATION_MULTIPLIERS = new HashSet<Fraction>();

  static {
    for (int i = 1; i < 10; i++) {
      for (int j = 1; j < 10; j++) {
        DURATION_MULTIPLIERS.add(new Fraction(i, j));
      }
    }
  }

  static List<Relationship> ALL = Lists.newArrayList();
  static Map<Relationship, Integer> IDX = Maps.newHashMap();
  static {
    int counter = 0;
    for (int i = -127; i < 127; i++){
      for (Fraction j : DURATION_MULTIPLIERS) {
        Relationship r = new Relationship(i, j);
        ALL.add(r);
        IDX.put(r, counter);
        counter++;
      }
    }
  }
  /**
   * Can be technically -127 to 127, but the end result must be from 0 to 127
   */
  public int fromPreviousToRoot;

  /**
   * Applied to a note to get the new duration
   */
  public Fraction durationMultiplier;


  public Relationship(int pitchDistance, Fraction durationMultiplier){
    this.fromPreviousToRoot = pitchDistance;
    this.durationMultiplier = durationMultiplier;
  }

  @Override public String toString() {
    return fromPreviousToRoot + ":" + durationMultiplier;
  }

  public Note apply(Note input) {
    int pitch = input.midiPitch + this.fromPreviousToRoot;
    int duration = ((int)(input.duration * this.durationMultiplier.doubleValue()));
    return new Note(pitch, duration);
  }

  public static Relationship ofNotes(Note first, Note second) {
    int diff = second.midiPitch - first.midiPitch;
    Fraction duration = new Fraction(second.duration, first.duration);
    //TODO:  handle boundaries
    return new Relationship(diff, duration);
  }

  public static int indexOf(Relationship r) {
    return IDX.get(r);
  }

  @Override public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;

    Relationship that = (Relationship) o;

    if (fromPreviousToRoot != that.fromPreviousToRoot)
      return false;
    return that.durationMultiplier.equals(this.durationMultiplier);

  }

  @Override public int hashCode() {
    int result;
    long temp;
    result = fromPreviousToRoot;
    temp = Double.doubleToLongBits(durationMultiplier.doubleValue());
    result = 31 * result + (int) (temp ^ (temp >>> 32));
    return result;
  }
}
