; methods to analyze a piece of music, to decode it into states and substates

(ns kenobi.obiwan
  (:import [com.zimmermusic.kenobi ForcePowers])
  (:use [kenobi.util.midi])
  (:require [org.nfrac.comportex.core :as core]
           [org.nfrac.comportex.encoders :as enc]
           [org.nfrac.comportex.util :as util]
            [org.nfrac.comportex.protocols :as p]))

(defprotocol jedi
  (learn [this analysis] "persist an analysis so that it may be drawn upon to compose later")
  (compose [this parameters] "create a new piece"))




(def spec
  {:column-dimensions [1000]
   :distal-punish? false
   :distal-vs-proximal-weight 1.0
   })

(def higher-level-spec-diff
  {:column-dimensions [400]
   :ff-max-segments 5})


(def pitch-encoder (enc/linear-encoder 1000 35 [35 70]))
(def dur-encoder (enc/linear-encoder 1000 5 [60 300]))

(def note-encoder
  (enc/encat 2 pitch-encoder dur-encoder))




(defn bit-list-to-bit-votes [bit-list]
  (reduce (fn [xs x] (merge-with + xs {x 1}))
    {}
    bit-list
    ))


(defn encode-decode[encoder val]
  (let [encoded (p/encode encoder val)
        votes (bit-list-to-bit-votes encoded)
        decoded (p/decode encoder votes 1)
        as-vec (for [p decoded] (:value (first p)))]
    (println  "encoded:" encoded "  votes:"  votes " original: " val " decoded: " as-vec)
    ))




    (defn n-region-model
      ([n]
        (n-region-model n spec))
      ([n spec]
        (core/regions-in-series core/sensory-region (core/sensory-input pitch-encoder)
          n
          (list* spec (repeat (merge spec higher-level-spec-diff))))))





; (def model (n-region-model 1))

; (def model-t1 (p/htm-step model [20 120]))

; how do I persist the model between training
; how do i get the predictions back into a vector?
; how does time figure in - shoudl I replay time, with new samples at t 0 constantly?  or make it additive?
; probably additivs
; Performing will be infering with a dummy sample, save the result and then infer with it



(def notes (parse-midi-file "resources/cs1-1.mid" 1))

(def as-vec (for [n notes] [(first (keys (:p n))) (:d n)]))

(def pitches (filter identity (for [[pitch dur] as-vec] pitch)))
;
;(defn step[model notes]
;  (println "Currently Predicted: " (for [p (core/predictions model 1)] (:value (first p))) " Actual: " notes)
;  (p/htm-step model notes));


; left off - need to figure out appropriate settings for region,
; how to save model?
; train model and then start making notes
; how to deal with model unable to mkae prediction?

;
;(defn step[model notes]
;  (let [predictions (core/predictions model 1)
;        pred2 (core/predictions model 2)
;        val (:value (first predictions))]
;    (if (not (nil? val))
;      (println "off by" (Math/abs (- val notes)))
;      (println "NIL!"))
;;    (when (nil? val)
;;      (println predictions)
;;      (println pred2)))
;
;  (p/htm-step model notes)))




(defn analyze[model notes]
  ; need to break into a sequence of states, each of which is itself a sequence of states.
  ; then analyze sequential states and identify how you move from one to another.
  ; ways to do this:
  ;   Analyze sequential difference between various attributes - difference in pitch, difference in intervals used, etc
  ;   Then take the greatest sequential difference and declase those as state dividers
  ;   Could also just feed each note into the HTM...

  (reduce step model notes))



; (test-model spec higher-level-spec-diff)

(defn test-model[spec]
  (let [model (n-region-model 2 spec)
        input pitches]
    (doseq [data (repeat 300 input)]
      (println "****************************************")
      (analyze model data))))




(def model (analyze model as-vec))

; (core/
(defn compose [model num start]
  ; predict the value for start, then use that as the value for the second prediction)
  )


(defrecord Obiwan[]
  jedi
  (learn [this file]
    (let [notes (parse-midi-file file 1)
          as-vector (for [n notes] [(first (keys (:p n))) (:d n)])
          analysis (analyze notes)]
      )
    (println "much smarter"))
  (compose [this parameters]
    (println "composing")))





