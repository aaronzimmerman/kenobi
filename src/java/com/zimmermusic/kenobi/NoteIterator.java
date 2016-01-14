package com.zimmermusic.kenobi;

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

/** A very simple DataSetIterator for use in the GravesLSTMCharModellingExample.
 * Given a text file and a few options, generate feature vectors and labels for training,
 * where we want to predict the next character in the sequence.<br>
 * This is done by randomly choosing a position in the text file to start the sequence and
 * (optionally) scanning backwards to a new line (to ensure we don't start half way through a word
 * for example).<br>
 * Feature vectors and labels are both one-hot vectors of same length
 * @author Alex Black
 */
public class NoteIterator implements DataSetIterator {
  private static final long serialVersionUID = -7287833919126626356L;
  private static final int MAX_SCAN_LENGTH = 200;
  private char[] validCharacters;
  private Map<Character,Integer> charToIdxMap;
  private char[] fileCharacters;
  private int exampleLength = 100;
  private int miniBatchSize = 10;
  private int numExamplesToFetch = 400;
  private int examplesSoFar = 0;
  private Random rng;
  private final int numNotes = 12; //the total available pitches to choose from

  public NoteIterator() throws IOException {
    this(new Random());
  }

  /**
   *  of no new line characters, to avoid scanning entire file)
   * @throws IOException If text file cannot  be loaded
   */
  public NoteIterator(Random rng) throws IOException {
    this.rng = rng;
  }

  /** A minimal character set, with a-z, A-Z, 0-9 and common punctuation etc */
//  public static char[] getMinimalCharacterSet(){
//    List<Character> validChars = new LinkedList<>();
//    for(char c='a'; c<='z'; c++) validChars.add(c);
//    for(char c='A'; c<='Z'; c++) validChars.add(c);
//    for(char c='0'; c<='9'; c++) validChars.add(c);
//    char[] temp = {'!', '&', '(', ')', '?', '-', '\'', '"', ',', '.', ':', ';', ' ', '\n', '\t'};
//    for( char c : temp ) validChars.add(c);
//    char[] out = new char[validChars.size()];
//    int i=0;
//    for( Character c : validChars ) out[i++] = c;
//    return out;
//  }

//  /** As per getMinimalCharacterSet(), but with a few extra characters */
//  public static char[] getDefaultCharacterSet(){
//    List<Character> validChars = new LinkedList<>();
//    for(char c : getMinimalCharacterSet() ) validChars.add(c);
//    char[] additionalChars = {'@', '#', '$', '%', '^', '*', '{', '}', '[', ']', '/', '+', '_',
//        '\\', '|', '<', '>'};
//    for( char c : additionalChars ) validChars.add(c);
//    char[] out = new char[validChars.size()];
//    int i=0;
//    for( Character c : validChars ) out[i++] = c;
//    return out;
//  }

  public char convertIndexToCharacter( int idx ){
    return validCharacters[idx];
  }

  public int convertCharacterToIndex( char c ){
    return charToIdxMap.get(c);
  }

  public int getRandomNote(){
    return 3;
  }

  public boolean hasNext() {
    return examplesSoFar <= numExamplesToFetch; //true; //examplesSoFar + miniBatchSize <= numExamplesToFetch;
  }

  public DataSet next() {
    return next(miniBatchSize);
  }

  public DataSet next(int num) {
//    if( examplesSoFar+num > numExamplesToFetch ) throw new NoSuchElementException();
    //Allocate space:
    INDArray input = Nd4j.zeros(new int[]{num,numNotes,exampleLength});
    INDArray labels = Nd4j.zeros(new int[]{num,numNotes,exampleLength});

   // int maxStartIdx = fileCharacters.length - exampleLength;

    //Randomly select a subset of the file. No attempt is made to avoid overlapping subsets
    // of the file in the same minibatch
    //eventually I can create a dictionary of possble notes - c4 quarter note, c4 half note, c4 eightnote, etc
    //and then feed into the machine the index of the note, lets me predict two variables
    for( int i=0; i<num; i++ ){
      int startIdx = 0;//(int) (rng.nextDouble()*maxStartIdx);
      int endIdx = startIdx + 100; //exampleLength;
      int scanLength = 0;


      int currCharIdx = 1;// charToIdxMap.get(fileCharacters[startIdx]);	//Current input
      int c=0;
      for( int j=startIdx+1; j<=endIdx; j++, c++ ){
        int nextCharIdx = (currCharIdx + 1) % 10; // charToIdxMap.get(fileCharacters[j]);		//Next character to predict
       // System.out.println("Training with value " + currCharIdx);
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
    return numExamplesToFetch;
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
