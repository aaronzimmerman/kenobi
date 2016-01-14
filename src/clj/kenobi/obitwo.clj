(ns kenobi.obitwo
  (:import [com.zimmermusic.kenobi ForcePowers Note])
  (:use [kenobi.util.midi])
  )




(def notes (parse-midi-file "resources/cs1-1.mid" 1))

(def as-vec (for [n notes] [(first (keys (:p n))) (:d n)]))

(def force-powers (ForcePowers. ))

(defn analyze[notes]
  (doseq [[pitch dur] notes]
    (dosync
      (let [temp-dur 60
             note (Note. pitch temp-dur)]
        ;(println "prediction before stepping:  " (.getPredicted force-powers))
        (let [next-note (.step force-powers note)]
          (println "prediction after stepping: " next-note))))))



; left off - how to structure the step to return a prediction, maybe just single thread somehow?
; just return a note from the Step operation?