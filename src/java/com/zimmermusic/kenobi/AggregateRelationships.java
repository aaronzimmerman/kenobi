package com.zimmermusic.kenobi;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AggregateRelationships {
  Logger logger = LoggerFactory.getLogger(AggregateRelationships.class);

  public static void main(String[] args) throws IOException {
    String path = args[0];
    String output = args[1];
    AggregateRelationships aggregator = new AggregateRelationships();
    aggregator.go(path);
//    List<RelationshipCount> counts = aggregator.getCounts();
//    List<String> lines = counts.stream().map(rc -> String.format("%s,%s", rc.rel, rc.count))
//        .collect(Collectors.toList());
//    Files.write(Paths.get(output), lines);

    aggregator.getNotFound().stream()
        .forEach(System.out::println);
  }

  private void go(String path) {
    File f = new File (path);
    if (f.isDirectory()) {
      for (File file : f.listFiles()) {
        if (file.isHidden()) {
          logger.info("Skipping hidden file {}", file.getName());
          continue;
        }
        try {
          addFromMidiFile(file.getAbsolutePath());
        } catch (Exception e) {
          logger.error("Unable to study file {}", file);
          logger.error("Exception", e);
        }
      }
    } else {
      try {
        addFromMidiFile(f.getAbsolutePath());
      } catch (Exception e) {
        logger.error("Unable to study file {}", path);
        logger.error("Exception", e);
      }
    }
  }

  Map<Relationship, Integer> count = Maps.newHashMap();
  List<Relationship> relationships = Lists.newArrayList();
  List<Relationship> notFound = Lists.newArrayList();
  private void addFromMidiFile(String absolutePath) {
    MidiLoader loader = new MidiLoader(absolutePath);
    List<Relationship> s = loader.getRelationships(1);
    s.stream().collect(Collectors.groupingBy(r -> r, Collectors.counting()));
    s.stream().forEach(relationships::add);
    s.stream().filter(r -> !Relationship.IDX.containsKey(r))
        .forEach(notFound::add);

  }

  class RelationshipCount {
    final Long count;
    final Relationship rel;

    public RelationshipCount(Long count, Relationship rel) {
      this.count = count;
      this.rel = rel;
    }
  }
  public List<RelationshipCount> getCounts() {
    return relationships.stream().collect(Collectors.groupingBy(r -> r, Collectors.counting()))
        .entrySet().stream()
        .map(entry -> new RelationshipCount(entry.getValue(), entry.getKey()))
        .sorted((o1, o2) -> o2.count.compareTo(o1.count))
        .collect(Collectors.toList());
  }
  public List<Relationship> getNotFound() {
    return notFound;
  }
}
