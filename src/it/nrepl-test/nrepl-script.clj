(require '[nrepl.core :as repl])
(require '[clojure.pprint :refer [pprint]])

(defn declare-success []
    (spit "success" ""))

(defn check-lt-mw [client]
    (doall (repl/message client {:op "eval" :code "(+ 2 3)"})))

(defn successful? [nrepl-response]
    (pprint nrepl-response)
    (= ["done"] (:status (last nrepl-response))))

(defn repl-client [& fs]
    (try
        (with-open [conn (repl/connect :port 4007)]
            (let [client (repl/client conn 1000)]
                (println "client " client)
                (mapv #(% client) fs)))
        (catch Exception e
            (println e))))

(defn test-nrepl []
  (loop [n 0]
    (println "n " n)
    (if (> n 500)
      (System/exit 1)
      (if-let [res (repl-client check-lt-mw)]
        (do
          (when (every? successful? res)
            (declare-success))
          (System/exit 0))
        (do
          (Thread/sleep 200)
          (recur (inc n)))))))

(.start (Thread. test-nrepl))
