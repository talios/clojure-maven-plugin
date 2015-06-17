(ns com.theoryinpractise.clojure.testrunner)

(import `java.util.Properties)
(import `java.io.FileInputStream)
(import `java.io.FileWriter)
(use 'clojure.test)
(use 'clojure.test.junit)

(def props (Properties.))
(.load props (FileInputStream. (first *command-line-args*)))

(def namespaces  (into [] 
                       (for [[key val] props
                             :when (.startsWith key "ns.")]
                               (symbol val))))

(def junit (Boolean/valueOf (.get props "junit")))
(def output-dir (.get props "outputDir"))
(def xml-escape (Boolean/valueOf (.get props "xmlEscape")))

(dorun (for [ns namespaces]
  (require ns)))

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
        (dorun (for [ns namespaces]
          (if junit
            (if xml-escape
              (do
                (with-open [writer (FileWriter. (str output-dir "/" ns ".xml"))
                            escaped (xml-escaping-writer writer)]
                            (binding [*test-out* writer *out* escaped]
                              (with-junit-output
                                (run-tests ns)))))
              (do
                ;;Use with-test-out to fix with-junit-output for Clojure 1.2 (See http://dev.clojure.org/jira/browse/CLJ-431)
                (with-open [writer (FileWriter. (str output-dir "/" ns ".xml"))]
                  (binding [*test-out* writer]
                    (with-test-out
                      (with-junit-output
                        (run-tests ns)))))))
            (run-tests ns))))
    (shutdown-agents)
    (print-results @results)
    (System/exit (total_errors @results))))))
