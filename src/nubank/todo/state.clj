(ns nubank.state
  (:gen-class)
  (:require [cheshire.core :as cheshire]))

(defn dequeue-job! [skills jobs]
  (loop [[skill & tail] skills]
    (when (some? skill)
      (let [item (first (get @jobs skill))]
        (if (some? item)
          (do (swap! jobs update skill #(rest %)) item)
          (recur tail))))))

(defn dequeue-job! [skills jobs]
  (loop [[skill & tail] skills]
    (when (some? skill)
      (let [item (first (get @jobs skill))]
        (if (some? item)
          (do (swap! jobs update skill #(rest %)) item)
          (recur tail))))))

(defn dequeue [jobs_pool agent urgents] 
  (def primary (set (:primary_skillset agent)))
  (def secondary (set (:secondary_skillset agent)))
  (defn format-output [jobid agentid] {:job_assigned {:job_id jobid :agent_id agentid}})

  (if-let [jobid (dequeue-job! primary urgents)]
    (format-output jobid (:id agent))
    (if-let [jobid (dequeue-job! secondary urgents)]
      (format-output jobid (:id agent))
      (if-let [jobid (dequeue-job! primary jobs_pool)]
        (format-output jobid (:id agent))
        (if-let [jobid (dequeue-job! secondary jobs_pool)]
          (format-output jobid (:id agent))
          :error )))))

(defn append! [type value jobs_pool agents urgents results]
  (cond
    (= type :new_agent) 
      (swap! agents assoc (:id value) value)
    (= type :new_job) 
      (cond 
        (= (:urgent value) false) (swap! jobs_pool update (:type value) #(conj (vec %) (:id value)))
        (= (:urgent value) true) (swap! urgents update (:type value) #(conj (vec %) (:id value))))
    (= type :job_request) 
      (swap! results conj 
        (dequeue jobs_pool (get @agents (:agent_id value)) urgents))
    :else (throw (Exception. "Unpredicted input format"))))

(defn process [input]
  (def jobs_pool (atom {}))
  (def agents (atom {}))
  (def urgent_jobs (atom {}))
  (def results (atom []))
  
  (doseq [item input] 
    (doseq [[k v] item] 
      (append! k v jobs_pool agents urgent_jobs results)))
  @results)

(defn -main
  "The main entry point."
  [& args]
  (def input (cheshire/parse-stream *in* true))
  (cheshire/generate-stream (process input) *out* {:pretty true}))