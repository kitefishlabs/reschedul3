(ns reschedul2.preflight-request-options-tests
  (:require [clojure.test :refer :all]
            [reschedul2.routes.service :refer [service-routes]]
            [ring.mock.request :as mock]))

(deftest preflight-request-options-returns-success-for-valid-path
  (testing "Preflight request options returns success for valid path"
    (let [response (service-routes (mock/request :options "/api/v1/user/token"))]
      (is (= 200 (:status response))))))

(deftest preflight-request-options-returns-success-for-invalid-path
  (testing "Preflight request options returns success for invalid path"
    (let [response (service-routes (mock/request :options "/api/v1/invalid/thing"))]
      (is (= 200 (:status response))))))
