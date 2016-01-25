package com.zimmermusic.kenobi;

import com.google.common.collect.Lists;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.nd4j.linalg.api.buffer.DataBuffer;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiUnavailableException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Kenobi {
  private static final Logger logger = LoggerFactory.getLogger(Kenobi.class);
  private static final Random random = new Random();

  private final String modelPath;
  private MultiLayerNetwork network;

  // This is the number of notes this system can learn.
  int numOutcomes = Util.NOTES.size();

  static {
    Nd4j.dtype = DataBuffer.Type.DOUBLE;
  }

  public Kenobi(String modelPath) {
    File networkOnDisk = new File(ForcePowers.netPath(modelPath));
    if (networkOnDisk.exists()) {
      network = ForcePowers.loadNetwork(modelPath);
    } else {
      // Training new model;
      network = ForcePowers.getNetwork();
    }

    this.modelPath = modelPath;

    if (network == null) {
      System.err.println("Unable to initialize network, aborting");
      System.exit(1);
    }
  }

  public void save() {
    logger.info("Done with training, saving network");
    ForcePowers.saveNetwork(network, modelPath);
  }

  public void study(String path) {
    File f = new File(path);
    if (!f.exists()) {
      logger.error("File {} does not exist", path);
      return;
    }

    if (f.isDirectory()) {
      for (File file : f.listFiles()) {
        if (file.isHidden()) {
          logger.info("Skipping hidden file {}", file.getName());
          continue;
        }
        try {
          studyMidiFile(file.getAbsolutePath());
        } catch (Exception e) {
          logger.error("Unable to study file {}", file);
          logger.error("Exception", e);
        }
      }
    } else {
      try {
        studyMidiFile(f.getAbsolutePath());
      } catch (Exception e) {
        logger.error("Unable to study file {}", path);
        logger.error("Exception", e);
      }
    }
  }

  public void studyMidiFile(String file) throws MidiUnavailableException, InvalidMidiDataException, IOException {
    MidiFileNoteIterator notes = new MidiFileNoteIterator(file, 1);

    logger.info("Studying {}", file);

    List<Note> firstNoteOfComp = Lists.newArrayList(new Note(60, 2));
    //Do training, and then generate and print samples from network
   // for( int i=0; i<numEpochs; i++ ){

      network.fit(notes);

      List<List<Note>> sample = compose2(1, firstNoteOfComp, 10);
      logger.info("Composing melody");
      for (Note note : sample.get(0) ) {
        logger.info("Pitch: {}, Duration: {}", note.midiPitch, note.duration);
      }
      notes.reset();	//Reset iterator for another epoch
   // }
  }

  /**
   * Adds note to the memory and returns prediction of what the next will be
   * useful for live performing.  Chain this method to generate a melody at will
   * @param note
   * @return
   */
  public Note step(Note note) {
    INDArray nextInput = Nd4j.zeros(1,numOutcomes);
    int s = 0;

    int noteIndex = Util.NOTE_TO_INDEX.get(note);
    nextInput.putScalar(new int[]{s,noteIndex}, 1.0f);		//Prepare next time step input

    INDArray output = network.rnnTimeStep(nextInput);	//Do one time step of forward pass

    double[] outputProbDistribution = new double[numOutcomes];
    for( int j=0; j<outputProbDistribution.length; j++ ) outputProbDistribution[j] = output.getDouble(s,j);

    int sampledNoteIndex = ForcePowers.sampleFromDistribution(outputProbDistribution, random);

    return Util.NOTES.get(sampledNoteIndex);
  }

  /**
   * Create numSamples melodies, each of notesToSample length, starting with the initialization data
   * @param numSamples
   * @param initialization
   * @param notesToSample
   * @return
   */
  private List<List<Note>> compose2(int numSamples, List<Note> initialization, int notesToSample) {
    List<List<Note>> melodies = Lists.newArrayList();

    //Create input for initialization
    INDArray initializationInput = Nd4j.zeros(numSamples,numOutcomes, 1);

    for( int i=0; i<initialization.size(); i++ ){
      int idx = Util.NOTE_TO_INDEX.get(initialization.get(i));
      for( int j=0; j<numSamples; j++ ){
        initializationInput.putScalar(new int[]{j,idx,i}, 1.0f);
      }
    }

    StringBuilder[] sb = new StringBuilder[numSamples];
    for( int i=0; i<numSamples; i++ ) {
      sb[i] = new StringBuilder();
      melodies.add(new ArrayList<Note>());
      for (Note n : initialization) {
        sb[i].append(n.toString());
      }
    }

    network.rnnClearPreviousState();
    INDArray output = network.rnnTimeStep(initializationInput);
    output = output.tensorAlongDimension(output.size(2)-1,1,0);	//Gets the last time step output

    for( int i=0; i<notesToSample; i++ ){
      //Set up next input (single time step) by sampling from previous output
      INDArray nextInput = Nd4j.zeros(numSamples,numOutcomes);
      //Output is a probability distribution. Sample from this for each example we want to generate, and add it to the new input
      for( int s=0; s<numSamples; s++ ){
        double[] outputProbDistribution = new double[numOutcomes];
        for( int j=0; j<outputProbDistribution.length; j++ ) outputProbDistribution[j] = output.getDouble(s,j);
        int sampledCharacterIdx = ForcePowers.sampleFromDistribution(outputProbDistribution, random);

        nextInput.putScalar(new int[]{s,sampledCharacterIdx}, 1.0f);		//Prepare next time step input
        sb[s].append(Util.NOTES.get(sampledCharacterIdx));	//Add sampled character to StringBuilder (human readable output)
        melodies.get(s).add(Util.NOTES.get(sampledCharacterIdx));	//Add sampled character to StringBuilder (human readable output)
      }

      output = network.rnnTimeStep(nextInput);	//Do one time step of forward pass
    }

    return melodies;
  }

  private List<List<Note>>  composeFromRelationships(int numSamples, List<Note> initialization, int notesToSample) {
      List<List<Note>> melodies = Lists.newArrayList();

      //Create input for initialization
      INDArray initializationInput = Nd4j.zeros(numSamples,numOutcomes, 1);

      //MUST be at least two notes
      for( int i=1; i<initialization.size(); i++ ){
        Relationship r = Relationship.ofNotes(initialization.get(i-1), initialization.get(i));
        int idx = Relationship.indexOf(r);
        for( int j=0; j<numSamples; j++ ){
          initializationInput.putScalar(new int[]{j,idx,i}, 1.0f);
        }
      }

      StringBuilder[] sb = new StringBuilder[numSamples];
      for( int i=0; i<numSamples; i++ ) {
        sb[i] = new StringBuilder();
        melodies.add(new ArrayList<Note>());
        for (Note n : initialization) {
          sb[i].append(n.toString());
        }
      }

      network.rnnClearPreviousState();
      INDArray output = network.rnnTimeStep(initializationInput);
      output = output.tensorAlongDimension(output.size(2)-1,1,0);

      Note prev = initialization.get(initialization.size() - 1);

      for( int i=0; i<notesToSample; i++ ){
        //Set up next input (single time step) by sampling from previous output
        INDArray nextInput = Nd4j.zeros(numSamples,numOutcomes);
        //Output is a probability distribution. Sample from this for each example we want to generate, and add it to the new input
        for( int s=0; s<numSamples; s++ ){
          double[] outputProbDistribution = new double[numOutcomes];
          for( int j=0; j<outputProbDistribution.length; j++ ) outputProbDistribution[j] = output.getDouble(s,j);
          int samplesRelationshipIdx = ForcePowers.sampleFromDistribution(outputProbDistribution, random);
          nextInput.putScalar(new int[]{s,samplesRelationshipIdx}, 1.0f);		//Prepare next time step input
          sb[s].append(Util.NOTES.get(samplesRelationshipIdx));	//Add sampled character to StringBuilder (human readable output)

          Relationship r = Relationship.ALL.get(samplesRelationshipIdx);

          Note next = r.apply(prev);
          melodies.get(s).add(next);	//Apply the relationship to a note, getting a pitch and duration from comparisons
          prev = next;
        }

        output = network.rnnTimeStep(nextInput);	//Do one time step of forward pass
      }

      return melodies;
  }


  //java com.zimmermusic.kenobi.Kenobi model train cs1-1.mid
  public static void main(String[] args) throws Exception {
    String modelPath = args[0];
    String action = args[1];  //learn, compose

    Kenobi kenobi = new Kenobi(modelPath);

    switch(action) {
      case "train":
        //java com.zimmermusic.kenobi.Kenobi resources/model train 10 resources
        int numEpochs = Integer.parseInt(args[2]);
        String file = args[3];
        for (int i = 0; i < numEpochs; i++) {
          logger.info("*******************");
          long start = System.currentTimeMillis();
          logger.info("Beginning epoch " + i);

          kenobi.study(file);

          logger.info("Epoch {} complete in {} min", i, (System.currentTimeMillis() - start) / (1000l * 60));
          kenobi.save();
        }
        break;
      case "compose":
        //java com.zimmermusic.kenobi.Kenobi resources/model compose 300 output/1
        int numNotes = Integer.parseInt(args[2]);
        String output = args[3];

        int noteIndex = random.nextInt(Util.NOTES.size());
        List<Note> firstNoteOfComp = Lists.newArrayList(Util.NOTES.get(noteIndex));

        List<List<Note>> notes = kenobi.compose2(1, firstNoteOfComp, numNotes);
        MidiExporter.toFile(notes.get(0), output);
        break;
      default:
        System.err.println("Unknown action: " + action + " specify either train or compose");
    }

    kenobi.save();

    logger.info("My work here is done");
    System.exit(0);

  }
}
