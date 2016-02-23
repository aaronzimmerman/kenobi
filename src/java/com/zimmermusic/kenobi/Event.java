package com.zimmermusic.kenobi;


import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class Event {
  public static int TICKS_PER_SIXTEENTH = 60;
  public static Event NONE = new Event(Type.Empty, 0);

  @Override public String toString() {
    return "Event{" +
        "pitches=" + pitches +
        ", type=" + type +
        '}';
  }

  enum Type {
    On,
    Off,
    Empty
  }
  Set<Integer> pitches;
  Type type;

  public Event(Type type, Integer ... pitches) {
    this.pitches = Sets.newHashSet();
    for (int p : pitches) {
      this.pitches.add(p);
    }
    this.type = type;
  }
  static final Logger logger = LoggerFactory.getLogger(Event.class);

  @Override public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;

    Event event = (Event) o;

    if (pitches != null ? !pitches.equals(event.pitches) : event.pitches != null)
      return false;
    return type == event.type;

  }

  @Override public int hashCode() {
    int result = pitches != null ? pitches.hashCode() : 0;
    result = 31 * result + (type != null ? type.hashCode() : 0);
    return result;
  }

  static List<Note> toNotes(Map<Integer, Event> events) {

    List<Note> output = Lists.newArrayList();

    List<EventWithTick>  eventList = events.entrySet().stream()
        .map(EventWithTick::of)
        .sorted(Comparator.comparingLong(EventWithTick::getTick))
        .collect(Collectors.toList());

    // Eventually have to figure out if stuff is overlapping
    search:
    for (int i = 0; i <eventList.size() - 1; i++) {
      EventWithTick nextOn = eventList.get(i);
      if (nextOn.event.type != Event.Type.On) {
        continue search;
      }
      // Its a note on type, have to look for the coresponding note off
      Set<Integer> pitches = nextOn.event.pitches;
      int start = nextOn.getTick();

      for (int j = i; j < eventList.size(); j++) {
        Event candidate = eventList.get(j).event;
        if (candidate.type == Event.Type.Off && candidate.pitches.equals(pitches) ){
          int durationInBeats = (eventList.get(j).tick - start);
          Note note = new Note(pitches, start, durationInBeats);
          output.add(note);
          continue search;
        }
      }

      // If we get here there was no coresponding note off event - midi file is invalid
      throw new NoteOffNotFound();
    }

    logger.info("Parsed {} events into {} notes", events.size(), output.size());
    return output;
  }

  static class EventWithTick {
    final int tick;
    final Event event;

    public EventWithTick(int tick, Event event) {
      this.tick = tick;
      this.event = event;
    }

    public int getTick() {
      return tick;
    }

    public static EventWithTick of(Map.Entry<Integer, Event> eventEntry) {
      return new EventWithTick(eventEntry.getKey(), eventEntry.getValue());
    }
  }

}
