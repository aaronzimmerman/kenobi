package com.zimmermusic.kenobi;

import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by az on 1/7/16.
 */
public class Util {

  public static Map<Note, Integer> NOTE_TO_INDEX = new HashMap<>();
  public static List<Note> NOTES = new ArrayList<>();

  private static final int SIXTEENTH_NOTE = 1;
  private static final int EIGTH_NOTE = 2;
  private static final int QUARTER_NOTE = SIXTEENTH_NOTE * 4;
  private static final int WHOLE_NOTE = QUARTER_NOTE * 4;

  private static final List<Integer> durations = Lists.newArrayList(1, 2,3,4,6,8,10,12,14,16);


  //if we play up to four at once
  //and allow tuples
  static {
    int counter = 0;
    for (int i = 0; i<127; i++) { //available midi notes
      for (int dur : durations) {
        Note n = new Note(i, dur);
        NOTES.add(n);
        NOTE_TO_INDEX.put(n, counter);
        counter++;
      }
    }
  }
}
