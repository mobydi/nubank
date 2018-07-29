(ns nubank.core-test
  (:require [clojure.test :refer :all]
            [clojure.java.io :as io]
            [cheshire.core :as cheshire]
            [nubank.core :as nubank]))

(defn process-and-compare? [input-file output-file]
  (def input-string (slurp (io/resource input-file)))
  (def output (with-out-str (with-in-str input-string (nubank/-main))))
  (def result (cheshire/parse-string output true))

  (def output-string (slurp (io/resource output-file)))
  (def expected (cheshire/parse-string output-string true))
  
  (println "===========")
  (println "!test case:" input-file "->" output-file)
  (println "!expected:\n" expected)
  (println "!result:\n" result)
  (= expected result)
)

(deftest sample-test
  (testing "base file example"
    (is (process-and-compare? "sample-input.json" "sample-output.json"))))

(deftest custom-tests
  (testing "extra job sample"
    (is (process-and-compare? "many-job-input.json" "many-job-output.json")))
  (testing "extra skills agent sample"
    (is (process-and-compare? "many-skills-agent-input.json" "many-skills-agent-output.json"))))
