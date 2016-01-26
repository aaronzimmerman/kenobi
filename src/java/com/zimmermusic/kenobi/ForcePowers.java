package com.zimmermusic.kenobi;

/**
 * Created by az on 1/26/16.
 */
public interface ForcePowers {

  void save();
  void load(String basePath);
  void study(String inputFile);
  Note predict(Note input);
  void reset();
}
