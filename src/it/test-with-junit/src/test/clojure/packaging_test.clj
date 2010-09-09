(ns packaging-test
  (:use
   packaging
   clojure.test)
  )

(deftest atest
  (is (hello-world))
  )
