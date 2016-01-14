//package com.zimmermusic.kenobi;
//
//import org.numenta.nupic.Parameters.KEY;
//import org.numenta.nupic.Parameters;
//import org.numenta.nupic.algorithms.Anomaly;
//import org.numenta.nupic.algorithms.SpatialPooler;
//import org.numenta.nupic.algorithms.TemporalMemory;
//import org.numenta.nupic.datagen.ResourceLocator;
//import org.numenta.nupic.encoders.MultiEncoder;
//import org.numenta.nupic.network.Inference;
//import org.numenta.nupic.network.Network;
//import org.numenta.nupic.network.sensor.FileSensor;
//import org.numenta.nupic.network.sensor.ObservableSensor;
//import org.numenta.nupic.network.sensor.Publisher;
//import org.numenta.nupic.network.sensor.Sensor;
//import org.numenta.nupic.network.sensor.SensorParams;
//import org.numenta.nupic.util.MersenneTwister;
//
//import static org.numenta.nupic.algorithms.Anomaly.KEY_MODE;
//import java.util.HashMap;
//import java.util.Map;
//
//public class BasicNetwork {
//
//  private Network network;
//
//  public BasicNetwork () {
//
//    Parameters p = NetworkDemoHarness.getParameters();
//    p = p.union(NetworkDemoHarness.getNetworkDemoTestEncoderParams());
//
////    Publisher manual = Publisher.builder()
////        .addHeader("timestamp,consumption")
////        .addHeader("datetime,float")
////        .addHeader("T") //see SensorFlags.java for more info
////        .build();
////    Sensor sensor = Sensor.create(ObservableSensor::create, SensorParams.create(
////        SensorParams.Keys::obs, "", manual));
//
//    network = Network.create("Network API Demo", p)
//        .add(Network.createRegion("Region 1").add(
//            Network.createLayer("Layer 2/3", p).alterParameter(KEY.AUTO_CLASSIFY, Boolean.TRUE).add(Anomaly.create())
//                .add(new TemporalMemory())
//                .add(MultiEncoder.builder().name("").build()))
//            .add(Network.createLayer("Layer 4", p).add(new SpatialPooler())).connect("Layer 2/3", "Layer 4"));
//
//  }
//
//
//  private void go() {
//    Inference i = network.computeImmediate("7/2/10 0:00,21.2");
//    System.out.println(i.getClassification("consumption"));
//
//  }
//
//
//  public static void main(String[] args) {
//    BasicNetwork basicNetwork = new BasicNetwork();
//    basicNetwork.go();
//  }
//}
