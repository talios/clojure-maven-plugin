(ns failing
  (:use
   clojure.test)
  )

(deftest atest
  (is false "<>")
  )
