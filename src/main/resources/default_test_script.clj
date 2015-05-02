(ns com.theoryinpractise.clojure.testrunner)

(use 'clojure.test)
(use 'clojure.test.junit)

(dorun (for [ns *command-line-args*]
  (require (symbol ns))))

(def escape-xml-map
  (zipmap "'<>\"&" (map #(str \& % \;) '[apos lt gt quot amp])))

(defn- escape-xml [text]
  (apply str (map #(escape-xml-map % %) text)))

(defn xml-escaping-writer
  [writer]
  (proxy
    [java.io.FilterWriter] [writer]
    (write [text]
      (if (string? text)
        (.write writer (escape-xml text))
        (.write writer text)))
    ))

(defn total_errors [summary]
  (+ (:error summary 0) (:fail summary 0)))

(defn print-results [results]
  (println (str "Tests run: " (:test results)
             ", Assertions: " (:pass results)
             ", Failures: " (:fail results)
             ", Errors: " (:error results)))
  (if (> (total_errors results) 0)
    (println "There are test failures.")))

(.println System/err (str "ARGS: " (pr-str *command-line-args*)))

(when-not *compile-files*
  (let [results (atom {})]
    (let [report-orig report
          junit-report-orig junit-report]
      (binding [report (fn [x] (report-orig x)
                         (swap! results (partial merge-with +)
                           (select-keys (into {} (rest x)) [:pass :test :error :fail ])))
                junit-report (fn [x] (junit-report-orig x)
                               (swap! results (partial merge-with +)
                                 (select-keys (into {} (rest x)) [:pass :test :error :fail ])))]
        (run-tests)))
    (shutdown-agents)
    (print-results @results)
    (System/exit (total_errors @results))))
