//package com.zimmermusic.kenobi;
//
//import org.numenta.nupic.Parameters.KEY;
//import org.numenta.nupic.Parameters;
//import org.numenta.nupic.algorithms.ClassifierResult;
//import org.numenta.nupic.algorithms.SpatialPooler;
//import org.numenta.nupic.algorithms.TemporalMemory;
//import org.numenta.nupic.encoders.MultiEncoder;
//import org.numenta.nupic.network.Inference;
//import org.numenta.nupic.network.Network;
//import org.numenta.nupic.network.sensor.*;
//
//import java.io.*;
//import java.util.HashMap;
//import java.util.Map;
//
//public class Force {
//
//    Network network;
//    private Double predictedPitch=0d;
//    private Double predictedDuration = 0d;
//    private static boolean working = false;
//
//    public Force(String fromFile){
//
//
//        try {
//            FileInputStream fileIn = new FileInputStream(fromFile);
//            ObjectInputStream in = new ObjectInputStream(fileIn);
//            network = (Network) in.readObject();
//            in.close();
//            fileIn.close();
//        } catch(IOException e) {
//            e.printStackTrace();
//
//        } catch (ClassNotFoundException e) {
//            e.printStackTrace();
//        }
//
//    }
//
//    Parameters getParameters() {
//        Parameters p = Parameters.getEncoderDefaultParameters();
//        p.setParameterByKey(KEY.GLOBAL_INHIBITIONS, true);
//        p.setParameterByKey(KEY.COLUMN_DIMENSIONS, new int[] { 2048 });
//        p.setParameterByKey(KEY.CELLS_PER_COLUMN, 32);
//        p.setParameterByKey(KEY.NUM_ACTIVE_COLUMNS_PER_INH_AREA, 40.0);
//        p.setParameterByKey(KEY.POTENTIAL_PCT, 0.8);
//        p.setParameterByKey(KEY.SYN_PERM_CONNECTED,0.1);
//        p.setParameterByKey(KEY.SYN_PERM_ACTIVE_INC, 0.0001);
//        p.setParameterByKey(KEY.SYN_PERM_INACTIVE_DEC, 0.0005);
//        p.setParameterByKey(KEY.MAX_BOOST, 1.0);
//
//        p.setParameterByKey(KEY.MAX_NEW_SYNAPSE_COUNT, 20);
//        p.setParameterByKey(KEY.INITIAL_PERMANENCE, 0.21);
//        p.setParameterByKey(KEY.PERMANENCE_INCREMENT, 0.1);
//        p.setParameterByKey(KEY.PERMANENCE_DECREMENT, 0.1);
//        p.setParameterByKey(KEY.MIN_THRESHOLD, 9);
//        p.setParameterByKey(KEY.ACTIVATION_THRESHOLD, 12);
//
//        p.setParameterByKey(Parameters.KEY.CLIP_INPUT, true);
//
//        return p;
//    }
//
//    Parameters getNetworkDemoTestEncoderParams() {
//
//        Parameters parameters = Parameters.getAllDefaultParameters();
//        parameters.setParameterByKey(KEY.INPUT_DIMENSIONS, new int[] { 8 });
//        parameters.setParameterByKey(KEY.COLUMN_DIMENSIONS, new int[] { 20 });
//        parameters.setParameterByKey(KEY.CELLS_PER_COLUMN, 6);
//
//        //SpatialPooler specific
//        parameters.setParameterByKey(KEY.POTENTIAL_RADIUS, 12);//3
//        parameters.setParameterByKey(KEY.POTENTIAL_PCT, 0.5);//0.5
//        parameters.setParameterByKey(KEY.GLOBAL_INHIBITIONS, false);
//        parameters.setParameterByKey(KEY.LOCAL_AREA_DENSITY, -1.0);
//        parameters.setParameterByKey(KEY.NUM_ACTIVE_COLUMNS_PER_INH_AREA, 5.0);
//        parameters.setParameterByKey(KEY.STIMULUS_THRESHOLD, 1.0);
//        parameters.setParameterByKey(KEY.SYN_PERM_INACTIVE_DEC, 0.01);
//        parameters.setParameterByKey(KEY.SYN_PERM_ACTIVE_INC, 0.1);
//        parameters.setParameterByKey(KEY.SYN_PERM_TRIM_THRESHOLD, 0.05);
//        parameters.setParameterByKey(KEY.SYN_PERM_CONNECTED, 0.1);
//        parameters.setParameterByKey(KEY.MIN_PCT_OVERLAP_DUTY_CYCLE, 0.1);
//        parameters.setParameterByKey(KEY.MIN_PCT_ACTIVE_DUTY_CYCLE, 0.1);
//        parameters.setParameterByKey(KEY.DUTY_CYCLE_PERIOD, 10);
//        parameters.setParameterByKey(KEY.MAX_BOOST, 10.0);
//        parameters.setParameterByKey(KEY.SEED, 42);
//        parameters.setParameterByKey(KEY.SP_VERBOSITY, 0);
//
//        //Temporal Memory specific
//        parameters.setParameterByKey(KEY.INITIAL_PERMANENCE, 0.2);
//        parameters.setParameterByKey(KEY.CONNECTED_PERMANENCE, 0.8);
//        parameters.setParameterByKey(KEY.MIN_THRESHOLD, 5);
//        parameters.setParameterByKey(KEY.MAX_NEW_SYNAPSE_COUNT, 6);
//        parameters.setParameterByKey(KEY.PERMANENCE_INCREMENT, 0.05);
//        parameters.setParameterByKey(KEY.PERMANENCE_DECREMENT, 0.05);
//        parameters.setParameterByKey(KEY.ACTIVATION_THRESHOLD, 4);
//        return parameters;
//    }
//    Publisher publisher;
//
//
//    public static Map<String, Map<String, Object>> setupMap(
//        Map<String, Map<String, Object>> map,
//        int n, int w, double min, double max, double radius, double resolution, Boolean periodic,
//        Boolean clip, Boolean forced, String fieldName, String fieldType, String encoderType) {
//
//        if(map == null) {
//            map = new HashMap<String, Map<String, Object>>();
//        }
//        Map<String, Object> inner = null;
//        if((inner = map.get(fieldName)) == null) {
//            map.put(fieldName, inner = new HashMap<String, Object>());
//        }
//
//        inner.put("n", n);
//        inner.put("w", w);
//        inner.put("minVal", min);
//        inner.put("maxVal", max);
//        inner.put("radius", radius);
//        inner.put("resolution", resolution);
//
//        if(periodic != null) inner.put("periodic", periodic);
//        if(clip != null) inner.put("clipInput", clip);
//        if(forced != null) inner.put("forced", forced);
//        if(fieldName != null) inner.put("fieldName", fieldName);
//        if(fieldType != null) inner.put("fieldType", fieldType);
//        if(encoderType != null) inner.put("encoderType", encoderType);
//
//        return map;
//    }
//
//    public Force() {
//        Parameters p = getParameters();
//        p = p.union(getNetworkDemoTestEncoderParams());
//        p.setParameterByKey(Parameters.KEY.COLUMN_DIMENSIONS, new int[] {500});
//        p.setParameterByKey(Parameters.KEY.CELLS_PER_COLUMN, 10);
//        p.setParameterByKey(Parameters.KEY.SP_PRIMER_DELAY, 0);
//        p.setParameterByKey(Parameters.KEY.CLIP_INPUT, false);
//
//        Map<String, Map<String, Object>> fieldEncodings = setupMap(null, 320, 3, 20D, 127D, 0D, 1.0D, null, null,
//            Boolean.TRUE, "pitch", "number", "ScalarEncoder");
//
//       // fieldEncodings = setupMap(fieldEncodings, 320, 3, 20D, 127D, 0D, 1.0D, null, null, Boolean.TRUE, "duration", "number", "ScalarEncoder");
//
//        p.setParameterByKey(Parameters.KEY.FIELD_ENCODING_MAP, fieldEncodings);
//
//        // --- Manual / Programmatic
//        publisher = Publisher.builder()
//                .addHeader("pitch")
//                .addHeader("int")
//                .addHeader("B")
//                .build();
//
//        Object[] n = { "pitch", publisher};
//        SensorParams parms = SensorParams.create(SensorParams.Keys::obs, n);
//
//        final Sensor<ObservableSensor<Integer>> sensor = Sensor.create(ObservableSensor::create, parms);
//
//        network = Network.create("Network API Demo", p)
//                .add(Network.createRegion("Region 1")
//                        .add(Network.createLayer("Layer 2/3", p)
//                            .alterParameter(Parameters.KEY.AUTO_CLASSIFY, Boolean.TRUE)
//                            .add(new TemporalMemory())
//                            .add(new SpatialPooler())
//                            .add(MultiEncoder.builder().name("").build())
//                            .add(sensor)));
//
//      //  network.start();
//
//
//
//    }
//
//
////    private Subscriber<Inference> getSubscriber() {
////
////        return new Subscriber<Inference>() {
////            public void onCompleted() {
////
////            }
////
////            public void onError(Throwable e) { e.printStackTrace(); }
////            public void onNext(Inference i) {
////
////                predictedDuration = (double) i.getClassification("duration").getMostProbableValue(1);
////                predictedPitch = (double) i.getClassification("pitch").getMostProbableValue(1);
////
////                StringBuilder sb = new StringBuilder()
////                        .append(i.getRecordNum()).append(", (")
////                        .append(i.getClassifierInput().get("pitch").get("inputValue")).append(",")
////                        .append(i.getClassifierInput().get("duration").get("inputValue")).append("), ")
////                        .append(" predicted p: " + predictedPitch)
////                        .append(" predicted d: " + predictedDuration);
////
////                System.out.println(sb.toString());
////                working=false;
////            }
////        };
////    }
//
//
//
//    /**
//     * Pass a note in, prediction made first as to what the next note will be
//     * @return
//     */
//    public Note step(Note next){
//        working = true;
//
//        int[] data = new int[] {next.midiPitch}; // csv = String.format("%s,%s", next.midiPitch, next.duration);
//        //get currently predicted
//
//       Inference i = network.computeImmediate(data);
//
//        if (i != null) {
//            ClassifierResult<Object> durationResult = i.getClassification("pitch");
//            if (durationResult != null) {
//                predictedDuration = (double) durationResult.getMostProbableValue(1);
//            }
//
//            ClassifierResult<Object> pitchResult = i.getClassification("duration");
//            if (pitchResult != null) {
//                predictedPitch = (double) pitchResult.getMostProbableValue(1);
//            }
//        }
//
//      Note n = new Note(predictedPitch.intValue(), predictedDuration.intValue());
//      System.out.println("note: " + next + ",  predicted " + n);
//      return n;
//
//    }
//
//    public Note getPredicted() {
//      return new Note(predictedPitch.intValue(), predictedDuration.intValue());
//    }
//
//
//    public void saveToFile(String file) {
//        try {
//            FileOutputStream fileOut = new FileOutputStream(file);
//            ObjectOutputStream out = new ObjectOutputStream(fileOut);
//            out.writeObject(network);
//        } catch (IOException e){
//            System.err.println("Unable to serialize network");
//            e.printStackTrace();
//        }
//    }
//
//
//    public static void main(String[] args){
//
//        Force powers = new Force();
//
//        for (int i= 0; i<100; i++) {
//            powers.step(new Note((i % 10) + 50, 60));
//        }
//
//
//    }
//
//
//}
