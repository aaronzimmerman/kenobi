package com.zimmermusic.kenobi;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Random;

import org.deeplearning4j.datasets.iterator.DataSetIterator;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.DataSetPreProcessor;
import org.nd4j.linalg.factory.Nd4j;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.midi.Track;

/** A very simple DataSetIterator for use in the GravesLSTMCharModellingExample.
 * Given a text file and a few options, generate feature vectors and labels for training,
 * where we want to predict the next character in the sequence.<br>
 * This is done by randomly choosing a position in the text file to start the sequence and
 * (optionally) scanning backwards to a new line (to ensure we don't start half way through a word
 * for example).<br>
 * Feature vectors and labels are both one-hot vectors of same length
 * @author Alex Black
 */
public class MidiFileNoteIterator implements DataSetIterator {

  private final int numNotes = Util.NOTES.size(); //the total available pitches to choose from
  int batchSize = 10;
  int exampleLength;
  int examplesSoFar = 0;
  List<Note> notes;
  Random random = new Random();
  int numExamplesToFetch = 1;
  int miniBatchSize = 1;
  int timesToRun = 5;

  public MidiFileNoteIterator(String filePath, int track) throws MidiUnavailableException, InvalidMidiDataException, IOException {
    MidiLoader loader = new MidiLoader(filePath);

    notes = loader.trackAsArrayList(track);

    exampleLength = notes.size();
  }

  public int getRandomNote(){
    return random.nextInt(Util.NOTES.size());
  }

  public boolean hasNext() {
    return examplesSoFar <= timesToRun;
  }

  public DataSet next() {
    return next(1);
  }

  public DataSet next(int num) {
    INDArray input = Nd4j.zeros(new int[]{num,Util.NOTES.size(),exampleLength});
    INDArray labels = Nd4j.zeros(new int[]{num,Util.NOTES.size(),exampleLength});

    //Randomly select a subset of the file. No attempt is made to avoid overlapping subsets
    // of the file in the same minibatch
    for( int i=0; i<num; i++ ){
      int startIdx = 0;
      int endIdx = exampleLength;
      int scanLength = 0;

      Note note = notes.get(startIdx);
      if (!Util.NOTE_TO_INDEX.containsKey(note)) {
        System.out.println("note not found = " + note);
        continue;
      }

      int currCharIdx = Util.NOTE_TO_INDEX.get(notes.get(startIdx));	//Current input
      int c=0;
      for( int j=startIdx+1; j<endIdx; j++, c++ ){

        Note newNote = notes.get(j);
        if (!Util.NOTE_TO_INDEX.containsKey(newNote)) {
          System.out.println("note not found = " + newNote);
          continue;
        }
       // System.out.println("newNote = " + newNote);
        int nextCharIdx = Util.NOTE_TO_INDEX.get(newNote);		//Next character to predict
        input.putScalar(new int[]{i,currCharIdx,c}, 1.0);
        labels.putScalar(new int[]{i,nextCharIdx,c}, 1.0);
        currCharIdx = nextCharIdx;
      }
    }

    examplesSoFar += num;
    return new DataSet(input,labels);
  }

  public int totalExamples() {
    return numExamplesToFetch;
  }

  public int inputColumns() {
    return numNotes;
  }

  public int totalOutcomes() {
    return numNotes;
  }

  public void reset() {
    examplesSoFar = 0;
  }

  public int batch() {
    return miniBatchSize;
  }

  public int cursor() {
    return examplesSoFar;
  }

  public int numExamples() {
    return numNotes;
  }

  public void setPreProcessor(DataSetPreProcessor preProcessor) {
    throw new UnsupportedOperationException("Not implemented");
  }

  @Override
  public List<String> getLabels() {
    throw new UnsupportedOperationException("Not implemented");
  }

  @Override
  public void remove() {
    throw new UnsupportedOperationException();
  }

}
