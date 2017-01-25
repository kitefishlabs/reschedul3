(ns reschedul2.permission.permission-creation-tests
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

(deftest can-add-user-permission-with-valid-token-and-admin-permissions
  (testing "Can add user permission with valid token and admin permissions"
    (let [user-id-1         (:_id (db/get-registered-user-by-username "JarrodCTaylor"))
          user-id-2         (:_id (db/get-registered-user-by-username "Everyman"))
          _                 (is (= "basic" (:permission (db/get-permission-for-user user-id-1))))
          _                 (db/insert-permission-for-user! user-id-1 "admin")
          _                 (is (= "admin" (:permission (db/get-permission-for-user user-id-1))))
          response          ((app) (-> (mock/request :post (str "/api/v1/permission/user/" user-id-2) (ch/generate-string {:permission "organizer"}))
                                       (mock/content-type "application/json")
                                       (helper/get-token-auth-header-for-user "JarrodCTaylor:pass")))

          body              (helper/parse-body (:body response))
          expected-response (str "Permission 'organizer' for user " user-id-2 " successfully added")]
      (is (= 200               (:status response)))
      (is (= expected-response (:message body)))
      (is (= "organizer"     (:permission (helper/get-permission-for-user user-id-2)))))))

(deftest attempting-to-add-a-permission-that-does-not-exist-returns-404
  (testing "Attempting to add a permission that does not exist returns 404"
    (let [user-id-1         (:_id (db/get-registered-user-by-username "JarrodCTaylor"))
          user-id-2         (:_id (db/get-registered-user-by-username "Everyman"))
          _                 (db/insert-permission-for-user! user-id-1 "admin")
          _                 (is (= "basic" (:permission (db/get-permission-for-user user-id-2))))
          response ((app) (-> (mock/request :post (str "/api/v1/permission/user/" user-id-2))
                              (mock/content-type "application/json")
                              (mock/body (ch/generate-string {:permission "stranger"}))
                              (helper/get-token-auth-header-for-user "JarrodCTaylor:pass")))
          body     (helper/parse-body (:body response))]
      (is (= 404                                    (:status response)))
      (is (= "Permission 'stranger' does not exist" (:error body)))
      (is (= "basic"                                (:permission (helper/get-permission-for-user user-id-2)))))))

(deftest can-not-add-user-permission-with-valid-token-and-no-admin-permissions
  (testing "Can not add user permission with valid token and no admin permissions"
    (let [user-id-1         (:_id (db/get-registered-user-by-username "Everyman"))
          _                 (is (= "basic" (:permission (db/get-permission-for-user user-id-1))))
          response ((app) (-> (mock/request :post (str "/api/v1/permission/user/" user-id-1))
                              (mock/content-type "application/json")
                              (mock/body (ch/generate-string {:permission "other"}))
                              (helper/get-token-auth-header-for-user "Everyman:pass")))
          body     (helper/parse-body (:body response))]
      (is (= 401              (:status response)))
      (is (= "Not authorized" (:error body)))
      (is (= "basic"          (:permission (db/get-permission-for-user user-id-1)))))))
