(ns ns-binding-test
  (:require [clojure.test :refer :all]))

(deftest atest
  (is (= 'ns-binding-test (ns-name *ns*))))
