package com.zimmermusic.kenobi;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;

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
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.lossfunctions.LossFunctions.LossFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ForcePowers {

  static Logger logger = LoggerFactory.getLogger(ForcePowers.class);

  static int numNotes =  Util.NOTES.size();

  static int numEpochs = 10;							//Total number of training + sample generation epochs

  static int lstmLayerSize = 200;					//Number of units in each GravesLSTM layer
//  static int miniBatchSize = 32;						//Size of mini batch to use when  training
//  static int examplesPerEpoch = 50 * miniBatchSize;	//i.e., how many examples to learn on between generating samples
//  static int exampleLength = 100;					//Length of each training example
//  static int nSamplesToGenerate = 4;					//Number of samples to generate after each training epoch
//  static int nCharactersToSample = 20;				//Length of each sample to generate
//  static String generationInitialization = null;		//Optional character initialization; a random character is used if null
//
  public static String confPath(String basePath) {
    return String.format("%s-conf.json", basePath);
  }
  public static String netPath(String basePath) {
    return  String.format("%s-net.bin", basePath);
  }

  public static void saveNetwork(MultiLayerNetwork net, String basePath) {
    String confPath = confPath(basePath);
    String paramPath = netPath(basePath);
    logger.info("Saving model and parameters to {} and {} ...",  confPath, paramPath);

    try {
      // save parameters
      DataOutputStream dos = new DataOutputStream(new FileOutputStream(paramPath));
      Nd4j.write(net.params(), dos);
      dos.flush();
      dos.close();

      // save model configuration
      FileUtils.write(new File(confPath), net.getLayerWiseConfigurations().toJson());
    } catch (IOException e) {
      logger.error("Unable to persist model", e);
    }
  }

  public static MultiLayerNetwork loadNetwork(String basePath) {
    logger.info("Loading saved model and parameters...");

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

  public static MultiLayerNetwork getNetwork() {
  //Set up network configuration:
    MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
        .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT).iterations(1)
        .learningRate(0.1)
        .rmsDecay(0.95)
        .seed(12345)
        .regularization(true)
        .l2(0.001)
        .list(3)
        .layer(0, new GravesLSTM.Builder().nIn(numNotes).nOut(lstmLayerSize)
            .updater(Updater.RMSPROP)
            .activation("tanh").weightInit(WeightInit.DISTRIBUTION)
            .dist(new UniformDistribution(-0.08, 0.08)).build())
        .layer(1, new GravesLSTM.Builder().nIn(lstmLayerSize).nOut(lstmLayerSize)
            .updater(Updater.RMSPROP)
            .activation("tanh").weightInit(WeightInit.DISTRIBUTION)
            .dist(new UniformDistribution(-0.08, 0.08)).build())
        .layer(2, new RnnOutputLayer.Builder(LossFunction.MCXENT).activation("softmax")        //MCXENT + softmax for classification
            .updater(Updater.RMSPROP)
            .nIn(lstmLayerSize).nOut(numNotes).weightInit(WeightInit.DISTRIBUTION)
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
