package com.zimmermusic.kenobi;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Set;


class MidiEventLoader {
  /**
   *loads a midi file from disk and stores all events in a nested Array List, using the Helper class "Note"
   * needs some cleanup though
   */
  Map<Integer, Event> events = Maps.newHashMap();
  int maxTicks = 0;

  public static final int TICKS_DIVISOR = 60;

  public MidiEventLoader(String fileName) {

    Track[] trx;

      File myMidiFile = new File(fileName);
    Sequence mySeq = null;
    try {
      mySeq = MidiSystem.getSequence(myMidiFile);
    } catch (InvalidMidiDataException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
    trx = mySeq.getTracks();

      for (int i = 0; i < trx.length; i++) {
        Track t = trx[i];
        for (int n = 0; n<t.size(); n++) {

          MidiEvent midiEvent = t.get(n);

          int startTime = ((int) midiEvent.getTick()) / TICKS_DIVISOR;
          if (startTime > maxTicks) {
            maxTicks = startTime;
          }

          if (midiEvent.getMessage() instanceof ShortMessage) {
            ShortMessage s = (ShortMessage)(midiEvent.getMessage());

            int pitch = s.getData1();
            int velocity = s.getData2();

            if (s.getCommand() == ShortMessage.NOTE_ON && velocity != 0) { // 144
              //store all the values temporarily in order to find the associated note off event

              if (!events.containsKey(startTime)) {
                Event e = new Event(Event.Type.On, pitch);
                events.put(startTime, e);
              } else {
                if (events.get(startTime).type == Event.Type.Off || velocity == 0){
                  // Move the previous back a tick.

                 // System.err.println(startTime + "    off: " + pitch + " existing: " + events.get(startTime));
                  Event previous = events.get(startTime);
                  events.remove(startTime);
                  events.put(startTime - 1, previous);

                  Event e = new Event(Event.Type.On, pitch);
                  events.put(startTime, e);
                  continue;
                }
                Event e = events.get(startTime);
                e.pitches.add(pitch);
              }
            } else if (s.getCommand() == ShortMessage.NOTE_OFF // 128
                || (s.getCommand() == ShortMessage.NOTE_ON && velocity == 0)) {
              
              if (!events.containsKey(startTime)) {
                Event e = new Event(Event.Type.Off, pitch);
                events.put(startTime, e);
              } else {
                if (events.get(startTime).type == Event.Type.On){
                  //BAD THINGS!
                 // System.err.println(startTime + "    on: " + pitch + " existing: " + events.get(startTime));
                  continue;
                }
                Event e = events.get(startTime);
                e.pitches.add(pitch);
              }

            }
          }
        }
       }
    }


  public static void main(String[] args) {

    File directory = new File("resources");
    Set<Event> allEvents = Sets.newHashSet();
    for (File f : directory.listFiles()) {
      if (f.isHidden()) {
        continue;
      }
      MidiEventLoader loader = new MidiEventLoader(f.getAbsolutePath());
      Map<Integer, Event> events = loader.getEvents();

      events.entrySet().stream().forEach(integerEventEntry -> allEvents.add(integerEventEntry.getValue()));
    }

    System.out.println("allEvents.size() = " + allEvents.size());
  }

  public Map<Integer,Event> getEvents() {
    return events;
  }
}




