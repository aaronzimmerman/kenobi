(ns kenobi.obitwo
  (:import [com.zimmermusic.kenobi Kenobi Note]))


;(def notes (parse-midi-file "resources/cs1-1.mid" 1))
;
;(def as-vec (for [n notes] [(first (keys (:p n))) (:d n)]))

(def force-powers (Kenobi. "lib/model"))

(def note (Note. 60 2))

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
