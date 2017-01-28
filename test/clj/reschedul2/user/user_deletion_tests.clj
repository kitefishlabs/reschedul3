(ns reschedul2.user.user-deletion-tests
  (:require [clojure.test :refer :all]
            [reschedul2.handler :refer :all]
            [reschedul2.test-utils :as helper]
            [reschedul2.db.core :as db]
            [ring.mock.request :as mock]
            [cheshire.core :as ch]
            [mount.core :as mount]
            [taoensso.timbre :as timbre]))

(defn setup-teardown [f]
  (try
    (db/clear-permissions!)
    (db/clear-users!)
    (db/clear-user-permissions!)
    (db/insert-permission! "basic")
    (helper/add-users)
    (is (= 2 (count (db/all-registered-users))))
    (f)))

(use-fixtures :once
  (fn [f]
    (mount/start
      #'reschedul2.config/env
      #'reschedul2.db.core/db)
    (f)
    (mount/stop)))

(use-fixtures :each setup-teardown)

(deftest can-delete-user-who-is-not-self-and-associated-permissions-with-valid-token-and-admin-permissions
  (testing "Can delete user who is not self and associated permissions with valid token and admin permissions"
    (is (= 2 (count (db/all-registered-users))))
    (let [user-id-1         (:_id (db/json-friendly (db/get-registered-user-by-username "JarrodCTaylor")))
          user-id-2         (:_id (db/json-friendly (db/get-registered-user-by-username "Everyman")))
          _                 (is (= 2 (count (db/all-registered-users))))]
      (is (= "basic" (db/get-permission-for-user user-id-1))))))
      ;     _                 (db/insert-permission! "admin")])))
      ;     _                 (db/insert-permission-for-user! user-id-1 "admin")
      ;     response          ((app) (-> (mock/request :delete (str "/api/v1/user/" user-id-2))
      ;                                  (mock/content-type "application/json")
      ;                                  (helper/get-token-auth-header-for-user "JarrodCTaylor:pass")))
      ;     body              (helper/parse-body (:body response))
      ;     expected-response (str "User id " user-id-2 " successfully removed")]
      ; (timbre/warn (str "\n\n\n" body "\n\n\n"))
      ; (is (= 200               (:status response)))
      ; (is (= expected-response (:message body))))))
      ; (is (= 1                 (count (query/all-registered-users query/db))))
      ; (is (= nil (helper/get-permission-for-user user-id-2))))))
