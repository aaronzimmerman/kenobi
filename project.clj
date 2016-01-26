(defproject kenobi "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0-RC1"]
                 [overtone "0.9.1"]
                [org.deeplearning4j/deeplearning4j-core "0.4-rc3.8"]
                [com.google.guava/guava "19.0"]
                [org.nd4j/nd4j-x86 "0.4-rc3.8"]
                [org.nd4j/canova-nd4j-image "0.0.0.14"]
                [org.nd4j/canova-nd4j-codec "0.0.0.14"]
                [com.fasterxml.jackson.dataformat/jackson-dataformat-yaml "2.5.1"]]
  ;:main com.zimmermusic.main
  :java-source-paths ["src/java"]
  :source-paths ["src/clj"]
  :jvm-opts ["-Xmx3g"]
  :profiles {:dev {:dependencies [[org.clojure/clojurescript "0.0-3211"]
                                  [criterium "0.4.3"]]
                   :plugins [[lein-cljsbuild "1.0.5"]
                             [com.cemerick/clojurescript.test "0.3.3"]
                             [com.cemerick/austin "0.1.6"]
                             [lein-marginalia "0.8.0"]]}
             :repl {:source-paths ["dev" "src"]}})

