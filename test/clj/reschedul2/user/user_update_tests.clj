(ns reschedul2.user.user-update-tests
  (:require [clojure.test :refer :all]
            [reschedul2.handler :refer [app]]
            [reschedul2.test-utils :refer [parse-body]]
            [reschedul2.db.core :as db]
            [reschedul2.test-utils :as helper]
            [buddy.hashers :as hashers]
            [environ.core :as env]
            [ring.mock.request :as mock]
            [cheshire.core :as ch]
            [clj-time.core :as t]
            [clj-time.coerce :as c]
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
      #'reschedul2.config/env
      #'reschedul2.db.core/db
      #'reschedul2.db.core/db*)
    (f)
    (mount/stop)))

(use-fixtures :each setup-teardown)


(deftest can-successfully-update-a-users-information-by-self-admin
  (testing "Can successfully update an admin user's own information by that admin."
    (let [user-1         (db/json-friendly (db/get-registered-user-by-username "JarrodCTaylor"))
          user-id-1      (:_id user-1)
          response       ((app) (-> (mock/request
                                     :patch
                                     (str "/api/v1/user/" user-id-1)
                                     (ch/generate-string
                                      {:user (merge user-1 {:email "new@email.com"})}))
                                    (mock/content-type "application/json")
                                    (helper/get-token-auth-header-for-user "JarrodCTaylor:pass")))
          body           (parse-body (:body response))
          updated-user   (db/get-registered-user-details-by-username (:username body))
          updated-users-permission  (:permission (db/get-permission-for-user (.toString (:_id updated-user))))]
      (is (= 200              (:status response)))
      (is (= 2                (count (db/all-registered-users))))
      (is (= "JarrodCTaylor"  (:username body)))
      (is (= "new@email.com"  (str (:email updated-user))))
      (is (= "admin"          updated-users-permission)))))

(deftest can-successfully-update-a-users-information-by-self-basic
  (testing "Can successfully update a user's own information by that user with basic privileges."
    (let [user-2         (db/json-friendly (db/get-registered-user-by-username "Everyman"))
          user-id-2      (:_id user-2)
          response       ((app) (-> (mock/request
                                     :patch
                                     (str "/api/v1/user/" user-id-2)
                                     (ch/generate-string
                                      {:user (merge user-2 {:username "Everywoman" :email "new2@email.com"})}))
                                    (mock/content-type "application/json")
                                    (helper/get-token-auth-header-for-user "Everyman:pass")))
          body           (parse-body (:body response))
          updated-user   (db/get-registered-user-details-by-username (:username body))
          updated-users-permission  (:permission (db/get-permission-for-user (.toString (:_id updated-user))))]
      (is (= 200              (:status response)))
      (is (= 2                (count (db/all-registered-users))))
      (is (= "Everywoman"       (:username body)))
      (is (= "new2@email.com" (str (:email updated-user))))
      (is (= "basic"          updated-users-permission)))))


(deftest can-not-successfully-update-a-users-password-by-admin
  (testing "Cannot successfully update an admin user's password by that user with admin privileges."
    (let [user-id-1      (:_id (db/get-registered-user-by-username "JarrodCTaylor"))
          user-id-2      (:_id (db/get-registered-user-by-username "Everyman"))
          response       ((app) (-> (mock/request :patch (str "/api/v1/user/" user-id-2) (ch/generate-string {:pass "pass2"}))
                                    (mock/content-type "application/json")
                                    (helper/get-token-auth-header-for-user "JarrodCTaylor:pass")))
          body           (parse-body (:body response))]
      (is (= 400              (:status response)))
      (is (= 2                (count (db/all-registered-users))))
      (is (= nil              (:username body))))))


(deftest can-not-successfully-directly-update-a-users-password-by-self
  (testing "Cannot successfully update an admin user's own information by user with basic privileges."
    (let [user-2         (db/json-friendly (db/get-registered-user-by-username "Everyman"))
          user-id-2      (:_id user-2)
          response       ((app) (-> (mock/request
                                     :patch
                                     (str "/api/v1/user/" user-id-2)
                                     (ch/generate-string
                                      {:user (merge user-2 {:pass "pass2"})}))
                                    (mock/content-type "application/json")
                                    (helper/get-token-auth-header-for-user "Everyman:pass")))
          body           (parse-body (:body response))]
      (is (= 400                              (:status response)))
      (is (= 2                                (count (db/all-registered-users))))
      (is (= {:user {:pass "disallowed-key"}} (:errors body))))))


(deftest can-successfully-update-a-users-information-by-admin
  (testing "Can successfully update an admin user's own information by that admin."
    (let [user-2         (db/json-friendly (db/get-registered-user-by-username "Everyman"))
          user-id-2      (:_id user-2)
          response       ((app) (-> (mock/request
                                     :patch
                                     (str "/api/v1/user/" user-id-2)
                                     (ch/generate-string
                                      {:user (merge user-2 {:email "new2@email.com"})}))
                                    (mock/content-type "application/json")
                                    (helper/get-token-auth-header-for-user "JarrodCTaylor:pass")))
          body           (parse-body (:body response))
          updated-user   (db/get-registered-user-details-by-username (:username body))
          updated-users-permission  (:permission (db/get-permission-for-user (.toString (:_id updated-user))))]
      (is (= 200              (:status response)))
      (is (= 2                (count (db/all-registered-users))))
      (is (= "Everyman"       (:username body)))
      (is (= "new2@email.com" (str (:email updated-user))))
      (is (= "basic"          updated-users-permission)))))

(deftest can-not-update-a-users-information-by-lower-privileged
  (testing "Can successfully update an admin user's own information by that admin."
    (let [user-1         (db/json-friendly (db/get-registered-user-by-username "JarrodCTaylor"))
          user-id-1      (:_id user-1)
          response       ((app) (-> (mock/request
                                     :patch
                                     (str "/api/v1/user/" user-id-1)
                                     (ch/generate-string
                                      {:user (merge user-1 {:username "JarrodDTaylor"})}))
                                    (mock/content-type "application/json")
                                    (helper/get-token-auth-header-for-user "Everyman:pass")))
          body           (parse-body (:body response))]
      (is (= 401              (:status response)))
      (is (= "Not authorized" (:error body)))
      (is (= 2                (count (db/all-registered-users)))))))


(deftest can-successfully-update-a-users-state-by-admin
  (testing "Can successfully update a user's role by an admin user."
    (let [user-id-2      (:_id (db/get-registered-user-by-username "Everyman"))
          response       ((app) (-> (mock/request :patch (str "/api/v1/user/" user-id-2 "/state") (ch/generate-string {:state "verified"}))
                                    (mock/content-type "application/json")
                                    (helper/get-token-auth-header-for-user "JarrodCTaylor:pass")))
          body           (parse-body (:body response))
          updated-user   (db/get-registered-user-details-by-username (:username body))
          updated-users-permission  (:permission (db/get-permission-for-user (.toString (:_id updated-user))))
          updated-users-state  (:state updated-user)]
      (is (= 200              (:status response)))
      (is (= 2                (count (db/all-registered-users))))
      (is (= "Everyman"       (:username body)))
      (is (= "e@man.com"      (str (:email updated-user))))
      (is (= "verified"       (str (:state updated-user))))
      (is (= "basic"          updated-users-permission)))))
