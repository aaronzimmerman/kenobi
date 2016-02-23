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
import java.util.Random;

public class RelationshipIterator implements DataSetIterator {
  private static final Logger logger = LoggerFactory.getLogger(MidiFileNoteIterator.class);

  int exampleLength;
  int examplesSoFar = 0;
  List<Relationship> relationships;
  Random random = new Random();
  int numExamplesToFetch = 1;
  int miniBatchSize = 1;
  int timesToRun = 5;

  public RelationshipIterator(String filePath, int track) throws MidiUnavailableException, InvalidMidiDataException, IOException {
    MidiLoader loader = new MidiLoader(filePath);

    logger.info("Found {} tracks in midi file {}", loader.numTracks(), filePath);

    relationships = loader.getRelationships(track);

    if (relationships.size() == 0){
      logger.error("No events in track {} of {}", track, filePath);
    }

    exampleLength = relationships.size();
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

  public int indexOfRelationship(Relationship r) {
    return Relationship.IDX.get(r);
  }

  public boolean isKnown(Relationship r) {
    if (Relationship.IDX.containsKey(r)) {
      return true;
    } else {
      logger.info("Relationship {} is not known", r);
      return false;
    }
  }

  public int numRelationships() {
    return Relationship.ALL.size();
  }

  public DataSet next(int num) {
    INDArray input = Nd4j.zeros(new int[]{num, numRelationships(), exampleLength});
    INDArray labels = Nd4j.zeros(new int[]{num, numRelationships(), exampleLength});

    //Randomly select a subset of the file. No attempt is made to avoid overlapping subsets
    // of the file in the same minibatch
    for( int i=0; i<num; i++ ){
      int startIdx = 0;
      int endIdx = exampleLength;
      int scanLength = 0;

      Relationship rel = relationships.get(startIdx);
      if (!isKnown(rel)) {
        continue;
      }

      int currRelIdx = indexOfRelationship(relationships.get(startIdx));	//Current input
      int c=0;
      for( int j=startIdx+1; j<endIdx; j++, c++ ){

        Relationship nextRel = relationships.get(j);
        if (!isKnown(rel)) {
          continue;
        }

        int nextRelIdx = indexOfRelationship(nextRel);
        input.putScalar(new int[]{i,currRelIdx,c}, 1.0);
        labels.putScalar(new int[]{i,nextRelIdx,c}, 1.0);
        currRelIdx = nextRelIdx;
      }
    }

    examplesSoFar += num;
    return new DataSet(input,labels);
  }

  public int totalExamples() {
    return numExamplesToFetch;
  }

  public int inputColumns() {
    return numRelationships();
  }

  public int totalOutcomes() {
    return numRelationships();
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
    return numRelationships();
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
