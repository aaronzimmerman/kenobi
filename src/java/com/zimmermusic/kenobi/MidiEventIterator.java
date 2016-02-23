package com.zimmermusic.kenobi;

import org.deeplearning4j.datasets.iterator.DataSetIterator;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.DataSetPreProcessor;
import org.nd4j.linalg.factory.Nd4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiUnavailableException;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class MidiEventIterator implements DataSetIterator {
  private static final Logger logger = LoggerFactory.getLogger(MidiFileNoteIterator.class);

  private final int numEvents = EventMemory.CAPACITY;

  private final int TRAINING_LENGTH = 100;
  int exampleLength;
  int examplesSoFar = 0;
  Map<Integer, Event> events;
  Random random = new Random();
  int numExamplesToFetch = 1;
  int miniBatchSize = 1;
  int timesToRun = 5;
  int maxTicks;

  public MidiEventIterator(String filePath) throws MidiUnavailableException, InvalidMidiDataException, IOException {
    MidiEventLoader loader = new MidiEventLoader(filePath);

    events = loader.events;

    maxTicks = loader.maxTicks;

    if (events.size() == 0){
      logger.error("No events in file {}", filePath);
    }
    logger.info("Found {} events to study", events.size());
    exampleLength = maxTicks;
  }

  public boolean hasNext() {
    return examplesSoFar <= timesToRun;
  }

  public DataSet next() {
    return next(1);
  }

  public DataSet next(int num) {
    INDArray input = Nd4j.zeros(num, EventMemory.CAPACITY, exampleLength);
    INDArray labels = Nd4j.zeros(num, EventMemory.CAPACITY, exampleLength);

    int totalEvents = events.size();
    int startEventIdx = random.nextInt(totalEvents - TRAINING_LENGTH);

    logger.info("Training from event {}", startEventIdx);
    Event event = events.get(startEventIdx);
    int currCharIdx = EventMemory.get().indexOf(event);
    int maxEventIdx = startEventIdx + TRAINING_LENGTH;

    for( int i=startEventIdx; i<maxEventIdx; i++ ){
      Event e = Event.NONE;
      if (events.containsKey(i)) {
        e = events.get(i);
      }
      int c = i-1;
      int nextCharIdx = EventMemory.get().indexOf(e);
      input.putScalar(new int[]{ 0,currCharIdx, c}, 1.0);
      labels.putScalar(new int[]{0,nextCharIdx, c}, 1.0);
      currCharIdx = nextCharIdx;
    }

    examplesSoFar += num;
    return new DataSet(input,labels);
  }

  public int totalExamples() {
    return numExamplesToFetch;
  }

  public int inputColumns() {
    return numEvents;
  }

  public int totalOutcomes() {
    return numEvents;
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
    return numEvents;
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
