package com.zimmermusic.kenobi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;
import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Created by az on 1/14/16.
 */
public class MidiExporter {
  static final Logger logger = LoggerFactory.getLogger(MidiExporter.class);

  public static void toFile(List<Note> notes, String path) {
    File outputFile = new File(path + "_" + System.currentTimeMillis() + ".mid");
    Sequence	sequence = null;

    int ticksPerBeat = (int) (MidiLoader.TICKS_IN_SIXTEENTH * 4);
    try
    {
      sequence = new Sequence(Sequence.PPQ, ticksPerBeat);
    }
    catch (InvalidMidiDataException e)
    {
      e.printStackTrace();
      System.exit(1);
    }

    Track track = sequence.createTrack();

    int currentTick = 0;

    for (Note n : notes) {
      long durationInTicks = n.duration * MidiLoader.TICKS_IN_SIXTEENTH;
      logger.info("Writing note {} for {} ticks", n.midiPitch, durationInTicks);
      track.add(createNoteOnEvent(n.midiPitch, currentTick));
      currentTick += durationInTicks;
      track.add(createNoteOffEvent(n.midiPitch, currentTick-1));

    }


		/* Now we just save the Sequence to the file we specified.
		   The '0' (second parameter) means saving as SMF type 0.
		   Since we have only one Track, this is actually the only option
		   (type 1 is for multiple tracks).
		*/
    try
    {
      MidiSystem.write(sequence, 0, outputFile);
    }
    catch (IOException e)
    {
      e.printStackTrace();
      System.exit(1);
    }
  }

  static int VELOCITY = 64;
  private static MidiEvent createNoteOnEvent(int nKey, long lTick)
  {
    return createNoteEvent(ShortMessage.NOTE_ON,
        nKey,
        VELOCITY,
        lTick);
  }



  private static MidiEvent createNoteOffEvent(int nKey, long lTick)
  {
    return createNoteEvent(ShortMessage.NOTE_OFF,
        nKey,
        0,
        lTick);
  }

  private static MidiEvent createNoteEvent(int nCommand,
      int nKey,
      int nVelocity,
      long lTick)
  {
    ShortMessage	message = new ShortMessage();
    try
    {
      message.setMessage(nCommand,
          0,	// always on channel 1
          nKey,
          nVelocity);
    }
    catch (InvalidMidiDataException e)
    {
      e.printStackTrace();
      System.exit(1);
    }
    MidiEvent	event = new MidiEvent(message,
        lTick);
    return event;
  }
}
