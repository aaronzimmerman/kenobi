package com.zimmermusic.kenobi;

import org.apache.commons.io.FileUtils;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.Updater;
import org.deeplearning4j.nn.conf.distribution.UniformDistribution;
import org.deeplearning4j.nn.conf.layers.GravesLSTM;
import org.deeplearning4j.nn.conf.layers.RnnOutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.nd4j.linalg.api.buffer.DataBuffer;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.lossfunctions.LossFunctions.LossFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiUnavailableException;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;


public class LstmForcePowers implements ForcePowers {

  static Logger logger = LoggerFactory.getLogger(LstmForcePowers.class);

  final int numOutcomes;// = Util.NOTES.size();
  static int lstmLayerSize = 1000;
  final Random random = new Random();

  static {
    Nd4j.dtype = DataBuffer.Type.DOUBLE;
  }

  public LstmForcePowers(int numOutcomes) {
    this.numOutcomes = numOutcomes;
  }

  MultiLayerNetwork network;
  String basePath;

  private String confPath(String basePath) {
    return String.format("%s-conf.json", basePath);
  }
  private  String netPath(String basePath) {
    return  String.format("%s-net.bin", basePath);
  }


  public void reset() {

  }

  public void save() {
    String confPath = confPath(basePath);
    String paramPath = netPath(basePath);
    logger.info("Saving model and parameters to {} and {} ...",  confPath, paramPath);

    try {
      // save parameters
      DataOutputStream dos = new DataOutputStream(new FileOutputStream(paramPath));
      Nd4j.write(network.params(), dos);
      dos.flush();
      dos.close();

      // save model configuration
      FileUtils.write(new File(confPath), network.getLayerWiseConfigurations().toJson());
    } catch (IOException e) {
      logger.error("Unable to persist model", e);
    }

  }


  public void load(String modelPath) {
    this.basePath = modelPath;

    File networkOnDisk = new File(netPath(modelPath));
    if (networkOnDisk.exists()) {
      network = loadFromDisk(modelPath);
    } else {
      // Training new model;
      network = newNetwork();
    }


  }

  @Override
  public void study(String file) {
    MidiEventIterator events = null;
    try {
      events = new MidiEventIterator(file); //MidiFileNoteIterator(file, 1);
    } catch (MidiUnavailableException | InvalidMidiDataException | IOException e) {
      e.printStackTrace();
    }

    if (events != null) {
      logger.info("Studying {}", file);

      network.fit(events);
    }

    //    List<List<Note>> sample = compose2(1, firstNoteOfComp, 10);
    //    logger.info("Composing melody");
    //    for (Note note : sample.get(0) ) {
    //      logger.info("Pitch: {}, Duration: {}", note.midiPitch, note.duration);
    //    }
   // events.reset();	//Reset iterator for another epoch
  }

  @Override
  public Note predict(Note input) {
    INDArray nextInput = Nd4j.zeros(1, numOutcomes);
    int s = 0;

    int noteIndex = Util.NOTE_TO_INDEX.get(input);
    nextInput.putScalar(new int[]{s,noteIndex}, 1.0f);		//Prepare next time step input

    INDArray output = network.rnnTimeStep(nextInput);	//Do one time step of forward pass

    double[] outputProbDistribution = new double[numOutcomes];
    for( int j=0; j<outputProbDistribution.length; j++ ) outputProbDistribution[j] = output.getDouble(s,j);

    int sampledNoteIndex = LstmForcePowers.sampleFromDistribution(outputProbDistribution, random);

    return Util.NOTES.get(sampledNoteIndex);
  }

  private MultiLayerNetwork loadFromDisk(String basePath)  {
    // load parameters
    DataInputStream dis = null;
    MultiLayerConfiguration confFromJson;
    try {
      confFromJson = MultiLayerConfiguration.fromJson(FileUtils.readFileToString(new File(confPath(basePath))));

      dis = new DataInputStream(new FileInputStream(netPath(basePath)));
    } catch (IOException e) {
      logger.error("Unable to initialize network", e);
      return null;
    }

    INDArray newParams = null;
    try {
      newParams = Nd4j.read(dis);
      dis.close();
    } catch (IOException e) {
      e.printStackTrace();
    }

    // load model configuration
    MultiLayerNetwork savedNetwork = new MultiLayerNetwork(confFromJson);
    savedNetwork.init();
    savedNetwork.setParams(newParams);
    savedNetwork.setListeners(new ScoreIterationListener(1));




    logger.info("Model loaded");
    return savedNetwork;
  }

  private MultiLayerNetwork newNetwork() {
  //Set up network configuration:
    MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
        .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT).iterations(1)
        .learningRate(0.1)
        .rmsDecay(0.95)
        .seed(12345)
        .regularization(true)
        .l2(0.001)
        .list(3)
        .layer(0, new GravesLSTM.Builder().nIn(numOutcomes).nOut(lstmLayerSize)
            .updater(Updater.RMSPROP)
            .activation("tanh").weightInit(WeightInit.DISTRIBUTION)
            .dist(new UniformDistribution(-0.08, 0.08)).build())
        .layer(1, new GravesLSTM.Builder().nIn(lstmLayerSize).nOut(lstmLayerSize)
            .updater(Updater.RMSPROP)
            .activation("tanh").weightInit(WeightInit.DISTRIBUTION)
            .dist(new UniformDistribution(-0.08, 0.08)).build())
        .layer(2, new RnnOutputLayer.Builder(LossFunction.MCXENT).activation("softmax")        //MCXENT + softmax for classification
            .updater(Updater.RMSPROP)
            .nIn(lstmLayerSize).nOut(numOutcomes).weightInit(WeightInit.DISTRIBUTION)
            .dist(new UniformDistribution(-0.08, 0.08)).build())
        .pretrain(false).backprop(true)
        .build();

    MultiLayerNetwork net = new MultiLayerNetwork(conf);
    net.init();
    net.setListeners(new ScoreIterationListener(1));

    return net;
  }

  /** Given a probability distribution over discrete classes, sample from the distribution
   * and return the generated class index.
   * @param distribution Probability distribution over classes. Must sum to 1.0
   */
  public static int sampleFromDistribution(double[] distribution, Random rng){
    double d = rng.nextDouble();
    double sum = 0.0;
    for( int i=0; i<distribution.length; i++ ){
      sum += distribution[i];
      if( d <= sum ) return i;
    }
    //Should never happen if distribution is a valid probability distribution
    throw new IllegalArgumentException("Distribution is invalid? d="+d+", sum="+sum);
  }
}
