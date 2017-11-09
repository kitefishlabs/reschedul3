(ns reschedul2.permission.permission-deletion-tests
  (:require [clojure.test :refer :all]
            [reschedul2.handler :refer :all]
            [reschedul2.test-utils :as helper]
            [reschedul2.db.core :as db]
            [ring.mock.request :as mock]
            [cheshire.core :as ch]
            [mount.core :as mount]))

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

(deftest can-delete-user-permission-with-valid-token-and-admin-permissions
  (testing "Can delete user permission with valid token and admin permissions"
    (let [user-id-1         (:_id (db/get-registered-user-by-username "JarrodCTaylor"))
          user-id-2         (:_id (db/get-registered-user-by-username "Everyman"))
          _                 (db/insert-permission-for-user! (.toString user-id-1) "admin")
          _                 (db/insert-permission-for-user! (.toString user-id-2) "other")
          _                 (is (= "other" (:permission (db/get-permission-for-user (.toString user-id-2)))))
          response          ((app) (-> (mock/request :delete (str "/api/v1/permission/user/" user-id-2))
                                       (mock/content-type "application/json")
                                       (mock/body (ch/generate-string {:permission "other"}))
                                       (helper/get-token-auth-header-for-user "JarrodCTaylor:pass")))
          body              (helper/parse-body (:body response))
          expected-response (str "Permission 'other' for user " user-id-2 " successfully removed")]
      (is (= 200               (:status response)))
      (is (= "basic"           (:permission (db/get-permission-for-user (.toString user-id-2)))))
      (is (= expected-response (:message body))))))

(deftest can-not-delete-user-permission-with-valid-token-and-no-admin-permissions
  (testing "Can not delete user permission with valid token and no admin permissions"
    (let [user-id-1  (:_id (db/get-registered-user-by-username "JarrodCTaylor"))
          _          (db/insert-permission-for-user! (.toString user-id-1) "admin")
          _          (is (= "admin" (:permission (db/get-permission-for-user user-id-1))))
          response   ((app) (-> (mock/request :delete (str "/api/v1/permission/user/" user-id-1))
                                (mock/content-type "application/json")
                                (mock/body (ch/generate-string {:permission "other"}))
                                (helper/get-token-auth-header-for-user "Everyman:pass")))
          body       (helper/parse-body (:body response))]
      (is (= 401              (:status response)))
      (is (= "Not authorized" (:error body)))
      (is (= "admin"    (:permission (helper/get-permission-for-user user-id-1)))))))
