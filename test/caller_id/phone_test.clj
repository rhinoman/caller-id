(ns caller-id.phone-test
  (:require [clojure.test :refer :all]
            [caller-id.phone :refer :all]
            [cats.monad.either :as either]
            [cats.monad.maybe :as maybe]))

(deftest test-number-parsing
  (testing "Test good numbers"
    (let [num1 (parse-number "+41-22-767-6111")
          num2 (parse-number "+1-201-555-5555")]
      (is (either/right? num1))
      (is (either/right? num2))))
  (testing "Test bad numbers"
    (let [badnum1 (parse-number "lizard")
          badnum2 (parse-number "43lizards")]
      (is (either/left? badnum1))
      (is (either/left? badnum2)))))

(deftest test-number-formatting
  (testing "Test formatting"
    (let [num1 (parse-number "+41 22 767 6111")
          num2 (parse-number "1-201-555-5555")]
      (is (= (deref (format-number-m num1)) "+41227676111"))
      (is (= (deref (format-number-m num2)) "+12015555555")))))

(deftest test-e164-from-string
  (testing "Test E164 From String"
     (let [num1 (e164-from-string "+41 22 767 6111")
           num2 (e164-from-string "1-201-555-5555")]
       (is (= (deref num1) "+41227676111"))
       (is (= (deref num2) "+12015555555"))))
  (testing "Test Bad number"
    (let [badnum1 (e164-from-string "lizard")]
      (is (either/left? badnum1)))))