package com.zimmermusic.kenobi;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.Map;

public class EventMemory {
  public static int CAPACITY = 520;
  static final Logger logger = LoggerFactory.getLogger(EventMemory.class);
  private static EventMemory _memory;
  private static String filePath = "lib/model-mem.json";
  private final List<Event> events;
  private final Map<Event, Integer> idx = Maps.newHashMap();

  private EventMemory(List<Event> events) {
    this.events = events;
    for (int i = 0; i < events.size(); i++) {
      idx.put(events.get(i), i);
    }
  }

  public static EventMemory get() {
    if (_memory == null) {
      _memory = loadFromDisk();
    }

    return _memory;
  }

  private void add(Event event) {
    if (events.size() + 1 > CAPACITY) {
      logger.error("Capacity reached!");
    }
    events.add(event);
    idx.put(event, events.size() - 1);
  }


  private static EventMemory loadFromDisk() {
    // Load event memory
    File memOnDisk = new File(filePath);

    List<Event> events = null;

    if (!memOnDisk.exists()) {
      // Initialize new instance
      events = Lists.newArrayList();
      events.add(Event.NONE);
      return new EventMemory(events);
    }

    ObjectInputStream ois = null;
    try {
      ois = new ObjectInputStream(new FileInputStream(memOnDisk));
      events = (List<Event>) ois.readObject();
      logger.info("Loaded {} events from disk", events.size());

      return new EventMemory(events);


    } catch (IOException e) {
      logger.error("Unable to load events", e);
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    } finally {
      try {
        ois.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    return new EventMemory(events);
  }

  public void save() {
    try {
      ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath));
      oos.writeObject(events);
      oos.flush();
      oos.close();
      logger.info("Event memory saved");
    } catch (IOException e) {
      logger.error("Unable to save event list to disk", e);
    }
  }

  public int indexOf(Event event) {
    if (!this.idx.containsKey(event)) {
      logger.info("New event: " + event);
      add(event);
    }
    return this.idx.get(event);
  }
}
