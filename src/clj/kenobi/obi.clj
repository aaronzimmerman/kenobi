(ns kenobi.obiwan
  (:import [com.zimmermusic.kenobi Kenobi Note])
  (:use overtone.live))

(def force-powers (Kenobi. "lib/model"))

(defn next-note[note]
   (let [next-note (.step force-powers note)]
      (println "Pitch: " (.getPitch next-note) + " Duration: " + (.getDuration next-note))
     next-note))

(def ms-per-beat 100)

(def starting-note (Note. (int 60) (int 2)))

(defn play[inst note]
  (let [lengthInMs (* (.getDuration note) ms-per-beat)
        next-time (+ (System/currentTimeMillis) lengthInMs)
        next (next-note note)]
    (inst (.getPitch note) (.getDuration note))
    (apply-at next-time #'play[inst next])))
