package com.zimmermusic.kenobi;

/**
 * Created by az on 7/6/15.
 */
public class Note {

    public Note(int midiPitch, int duration) {
        this.midiPitch = midiPitch;
        this.duration = duration;
    }

    public int midiPitch;
    public int duration;

    @Override
    public String toString() {
        return "Note{" +
                "pitch=" + midiPitch +
                ", duration=" + duration +
                '}';
    }

  @Override public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;

    Note note = (Note) o;

    if (midiPitch != note.midiPitch)
      return false;
    return duration == note.duration;

  }

  @Override public int hashCode() {
    int result = midiPitch;
    result = 31 * result + duration;
    return result;
  }


}
