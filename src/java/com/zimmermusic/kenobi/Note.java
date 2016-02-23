package com.zimmermusic.kenobi;

import com.google.common.collect.Sets;

import java.util.Set;

public class Note {

  public Note(int midiPitch, int duration) {
      this.pitches = Sets.newHashSet(midiPitch);

      this.duration = duration;
  }

  public Note(Set<Integer> pitches, long startEpoch, long duration) {
    // epoch and duration and in sixteenth notes
    this.pitches = pitches;
    this.startEpoch = startEpoch;
    this.duration = duration;
  }

  public Set<Integer> pitches;
  public long duration;
  public long startEpoch;


  public int getPitch() {
    return pitches.iterator().next();
  }

  public long getDuration() {
    return duration;
  }
    @Override
    public String toString() {
        return "Note{" +
                "pitches=" + pitches +
                ", duration=" + duration +
                '}';
    }

  @Override public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;

    Note note = (Note) o;

    if (duration != note.duration)
      return false;
    if (startEpoch != note.startEpoch)
      return false;
    return pitches != null ? pitches.equals(note.pitches) : note.pitches == null;

  }

  @Override public int hashCode() {
    int result = pitches != null ? pitches.hashCode() : 0;
    result = 31 * result + (int) (duration ^ (duration >>> 32));
    result = 31 * result + (int) (startEpoch ^ (startEpoch >>> 32));
    return result;
  }
}
