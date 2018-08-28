(ns sand-compass.user.user-deletion-tests
  (:require [clojure.test :refer :all]
            [sand-compass.handler :refer :all]
            [sand-compass.test-utils :as helper]
            [sand-compass.db.core :as db]
            [ring.mock.request :as mock]
            [cheshire.core :as ch]
            [mount.core :as mount]
            [taoensso.timbre :as timbre]))

(defn reset-fake-db-data []
  (db/clear-users!)
  (db/clear-permissions!)
  (db/clear-user-permissions!)
  (db/insert-permission! "basic")
  (db/insert-permission! "organizer")
  (db/insert-permission! "admin")
  (helper/add-users)
  (db/insert-permission-for-user!
   (->
    (db/get-registered-user-details-by-username "JarrodCTaylor")
    :_id
    .toString)
   "admin")
  (is (= 2 (count (db/all-registered-users)))))

(defn setup-teardown [f]
 (try
  (reset-fake-db-data)
  (f)))

(use-fixtures :once
  (fn [f]
    (mount/start
      #'sand-compass.config/env
      #'sand-compass.db.core/db
      #'sand-compass.db.core/db*)
    (f)
    (mount/stop)))

(use-fixtures :each setup-teardown)

(deftest can-delete-user-who-is-not-self-and-associated-permissions-with-valid-token-and-admin-permissions
  (testing "Can delete user who is not self and associated permissions with valid token and admin permissions"
    (is (= 2 (count (db/all-registered-users))))
    (let [user-1            (db/json-friendly (db/get-registered-user-by-username "JarrodCTaylor"))
          user-2            (db/json-friendly (db/get-registered-user-by-username "Everyman"))
          user-id-1         (:_id user-1)
          user-id-2         (:_id user-2)
          response          ((app) (-> (mock/request :delete (str "/api/v1/user/" user-id-2))
                                       (mock/content-type "application/json")
                                       (helper/get-token-auth-header-for-user "JarrodCTaylor:pass")))
          body              (helper/parse-body (:body response))
          expected-response (str "User id " user-id-2 " successfully removed")]
      (is (= "admin" (:permission (db/get-permission-for-user user-id-1))))
      (is (= 200               (:status response)))
      (is (= expected-response (:message body)))
      (is (= 1                 (count (db/all-registered-users))))
      (is (= nil (helper/get-permission-for-user user-id-2))))))

; TODO: test that reg. user cannot delete admin
; TODO: test that reg. user cannot delete self
; TODO: test that admin user can delete another admin, but not self
