(use 'clojure.test)

(when-not *compile-files*
  (let [results (atom [])]
    (let [report-orig report]
      (binding [report (fn [x] (report-orig x)
                         (swap! results conj (:type x)))]
        (run-all-tests)))
    (shutdown-agents)
    (System/exit (if (empty? (filter {:fail :error} @results)) 0 -1))))
