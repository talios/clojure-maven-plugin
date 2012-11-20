(ns com.theoryinpractise.clojure.testrunner)

(use 'clojure.test)
(use 'clojure.test.junit)

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
  (+ (:error summary) (:fail summary)))

(defn summary [results]
  (first (filter #(-> % :type (= :summary)) results)))

(when-not *compile-files*
  (let [results (atom [])]
    (let [report-orig report
          junit-report-orig junit-report
          out-orig *out*
          test-out-orig *test-out*]
      (binding [report (fn [x] (report-orig x)
                         (swap! results conj x))
                junit-report (fn [x]
                         (junit-report-orig x)
                         (binding [*test-out* test-out-orig
                                   *out*      out-orig]
                           (report-orig x))
                         (swap! results conj x))]
        (run-tests)))
    (shutdown-agents)
    (System/exit (-> @results summary total_errors))))
