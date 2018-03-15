(ns reschedul2.refresh-token.refresh-token-deletion-tests
  (:require [clojure.test :refer :all]
            [reschedul2.handler :refer :all]
            [reschedul2.test-utils :as helper]
            [reschedul2.db.core :as db]
            [ring.mock.request :as mock]
            [cheshire.core :as ch]
            [mount.core :as mount]
            [taoensso.timbre :as timbre]))

(defn reset-fake-db-data []
  (db/clear-users!)
  (db/clear-permissions!)
  (db/clear-user-permissions!)
  (db/insert-permission! "basic")
  (helper/add-users)
  (is (= 2 (count (db/all-registered-users)))))

(defn setup-teardown [f]
  (try
    (reset-fake-db-data)
    (f)))

(use-fixtures :once
  (fn [f]
    (mount/start
     #'reschedul2.config/env
     #'reschedul2.db.core/db
     #'reschedul2.db.core/db*)
    (f)
    (mount/stop)))

(use-fixtures :each setup-teardown)

(deftest can-delete-refresh-token-with-valid-refresh-token
  (testing "Can delete refresh token with valid refresh token"
    (let [user-id-1    (:_id (db/get-registered-user-by-username "JarrodCTaylor"))
          _            (is (= "basic" (:permission (db/get-permission-for-user user-id-1))))
          _            (db/insert-permission-for-user! (.toString user-id-1) "admin")
          _            (is (= "admin" (:permission (db/get-permission-for-user user-id-1))))

          initial-response         ((app) (-> (mock/request :get "/api/v1/auth")
                                              (helper/basic-auth-header "JarrodCTaylor:pass")))
          initial-body             (helper/parse-body (:body initial-response))
          refresh-token            (:refresh_token initial-body)
          refresh-delete-response  ((app) (mock/request :delete (str "/api/v1/refresh-token/" refresh-token)))
          body                     (helper/parse-body (:body refresh-delete-response))
          registered-user-row      (db/get-registered-user-by-id (.toString user-id-1))]
      (is (= 200 (:status refresh-delete-response)))
      (is (= "Refresh token successfully deleted" (:message body)))
      (is (= 0 (:refresh_token registered-user-row))))))

(deftest attempting-to-delete-an-invalid-refresh-token-returns-an-error
  (testing "Attempting to delete an invalid refresh token returns an error"
    (let [refresh-delete-response  ((app) (mock/request :delete (str "/api/v1/refresh-token/" "123abc")))
          body                     (helper/parse-body (:body refresh-delete-response))]
      (is (= 404 (:status refresh-delete-response)))
      (is (= "The refresh token does not exist" (:error body))))))
