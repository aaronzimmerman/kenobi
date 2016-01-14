(ns keboni.tests
  (:require [org.nfrac.comportex.protocols :as p]
            [org.nfrac.comportex.encoders :as enc]
            [clojure.test :as t :refer (is deftest testing run-tests)]
            [kenobi.obiwan :as k]))


(defn bit-list-to-bit-votes [bit-list]
  (reduce (fn [xs x] (merge-with + xs {x 1}))
    {}
    bit-list
    ))

(deftest encode-decode-test
  (is (= (:value (first (p/decode k/note-encoder (bit-list-to-bit-votes (p/encode k/note-encoder [50 50])) 1))) [50 50]))

  )



(run-all-tests)
