(ns reschedul2.user.user-creation-tests
  (:require [clojure.test :refer :all]
            [reschedul2.handler :refer [app]]
            [reschedul2.test-utils :refer [parse-body]]
            [reschedul2.db.core :as db]
            [reschedul2.test-utils :as helper]
            [environ.core :as env]
            [ring.mock.request :as mock]
            [cheshire.core :as ch]
            [clj-time.core :as t]
            [clj-time.coerce :as c]
            [mount.core :as mount]
            [taoensso.timbre :as timbre]))

(defn create-user [user-map]
  ((app) (-> (mock/request :post "/api/v1/user")
             (mock/content-type "application/json")
             (mock/body (ch/generate-string user-map)))))

(defn assert-no-dup [user-1 user-2 expected-error-message]
  (let [_        (create-user user-1)
        response (create-user user-2)
        body     (parse-body (:body response))]
    (is (= 409                    (:status response)))
    (is (= 1                      (count (db/all-registered-users))))
    (is (= expected-error-message (:error body)))))

(defn setup-teardown [f]
  (try
    (db/clear-users!)
    (db/clear-permissions!)
    (db/clear-user-permissions!)
    (db/insert-permission! "basic")
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


(deftest can-successfully-create-a-new-user-who-is-given-basic-permission-as-default
  (testing "Can successfully create a new user who is given basic permission as default"
    (is (= 0 (count (db/all-registered-users))))
    (let [response              (create-user {:email "new@user.com" :username "NewUser" :password "pass"})
          body                  (parse-body (:body response))
          new-registered-user   (db/get-registered-user-details-by-username (:username body))
          new-users-permission  (:permission (db/get-permission-for-user (.toString (:_id new-registered-user))))
          created_on            (:created_on new-registered-user)]
          ; registered-at       (subs (str (:created_on new-registered-user)) 0 8)]
          ; expected-time       (subs (str (c/to-long (t/now)) 0 8))]
      (timbre/warn created_on)
      (is (= 201           (:status response)))
      (is (= 1             (count (db/all-registered-users))))
      (is (= "NewUser"     (:username body)))
      (is (= "NewUser"     (str (:username new-registered-user))))
      (is (> created_on    0))
      (is (= "basic"       new-users-permission)))))

(deftest can-not-create-a-user-if-username-already-exists-using-the-same-case
  (testing "Can not create a user if username already exists using the same case"
    (assert-no-dup {:email "Jrock@Taylor.com"   :username "Jarrod" :password "pass"}
                   {:email "Jam@Master.com"     :username "Jarrod" :password "pass"}
                   "Username already exists")))

; (deftest can-not-create-a-user-if-username-already-exists-using-mixed-case
;   (testing "Can not create a user if username already exists using mixed case"
;     (assert-no-dup {:email "Jrock@Taylor.com"   :username "jarrod" :password "pass"}
;                    {:email "Jam@Master.com"     :username "Jarrod" :password "pass"}
;                    "Username already exists")))

(deftest can-not-create-a-user-if-email-already-exists-using-the-same-case
  (testing "Can not create a user if email already exists using the same case"
    (assert-no-dup {:email "jarrod@taylor.com" :username "Jarrod"   :password "the-first-pass"}
                   {:email "jarrod@taylor.com" :username "JarrodCT" :password "the-second-pass"}
                   "Email already exists")))

; (deftest can-not-create-a-user-if-email-already-exists-using-mixed-case
;   (testing "Can not create a user if email already exists using mixed case"
;     (assert-no-dup {:email "wOnkY@email.com" :username "Jarrod" :password "Pass"}
;                    {:email "WonKy@email.com" :username "Jrock"  :password "Pass"}
;                    "Email already exists")))
;
; (deftest can-not-create-a-user-if-username-and-email-already-exist-using-same-and-mixed-case
;   (testing "Can not create a user if username and email already exist using same and mixed case"
;     (assert-no-dup {:email "wOnkY@email.com" :username "jarrod" :password "pass"}
;                    {:email "WonKy@email.com" :username "jarrod" :password "pass"}
;                    "Username and Email already exist")))
