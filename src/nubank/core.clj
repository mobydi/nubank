(ns nubank.core
  (:gen-class)
  (:require [cheshire.core :as cheshire])
  (:use [clojure.pprint]))

(defn filter_agents [input] ( ->> input
  (filter #(:new_agent %))
  (map #(:new_agent %))))

(defn filter_jobs [input] ( ->> input 
  (filter #(:new_job %))
  (map #(:new_job %))))

(defn filter_requests [input] ( ->> input 
  (filter #(:job_request %))
  (map #(:job_request %))))
    
(defn dequeue_job [types jobs_pool]
  (loop [types types
          tail jobs_pool
          head '()]

    (let [job? (first tail)]
      (if (or (contains? types (:type job?))
              (empty? tail))
        {:job job? :jobs_pool (concat head (rest tail))}
        (recur types
                (rest tail)
                (concat head (list (first tail))))))))

(defn dequeue [jobs agent urgents] 
  (def primary (set (:primary_skillset agent)))
  (def secondary (set (:secondary_skillset agent)))
  (let [result (dequeue_job primary urgents)]
    (if (:job result)
      {:job (:job result) :urgents (:jobs_pool result) :jobs jobs}
      (let [result (dequeue_job secondary (:jobs_pool result))]
         (if (:job result)
            {:job (:job result) :urgents (:jobs_pool result) :jobs jobs}
            (let [result (dequeue_job primary jobs)]
               (if (:job result)
                  {:job (:job result) :urgent urgents :jobs (:jobs_pool result)}
                  (let [result (dequeue_job secondary (:jobs_pool result))]
                     (if (:job result)
                        {:job (:job result) :urgent urgents :jobs (:jobs_pool result)}
                        nil)))))))))

(defn get_agent [agents id] (->> agents (filter #(= (:id %) id)) (first)))
(defn filter_urgent_jobs [jobs] (filter #(= (:urgent %) true) jobs))
(defn filter_regular_jobs [jobs] (filter #(= (:urgent %) false) jobs))

(defn process_request [jobs agents requests] 
  (loop [requests requests
      regular_jobs (filter_regular_jobs jobs)
      urgent_jobs (filter_urgent_jobs jobs)
      result []]
    (if requests
      (let [req (first requests)
            agent (get_agent agents (:agent_id req))
            deque (dequeue regular_jobs agent urgent_jobs)
            assigned {:job_id (:id (:job deque)) :agent_id (:id agent)}]
        (recur (next requests) 
          (:urgents deque) 
          (:jobs deque) 
          (conj result {:job_assigned assigned})))
      result
    )))

(defn -main
  "The main entry point."
  [& args]
  (let [input (cheshire/parse-stream *in* true)
        jobs (filter_jobs input)
        agents (filter_agents input)
        requests (filter_requests input)]

        (def output (process_request jobs agents requests))
        (cheshire/generate-stream output *out* {:pretty true})))