(ns reschedul2.event.event-creation-tests
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

(defn create-event [event-map]
  ((app) (-> (mock/request :post "/api/v1/event")
             (mock/content-type "application/json")
             (mock/body (ch/generate-string event-map)))))

(defn assert-no-dup [event-1 event-2 expected-error-message]
  (let [_        (create-event event-1)
        response (create-event event-2)
        body     (parse-body (:body response))]
    (is (= 409                    (:status response)))
    (is (= 1                      (count (db/all-events))))
    (is (= expected-error-message (:error body)))))

(defn setup-teardown [f]
  (try
    (db/clear-users!)
    (db/clear-permissions!)
    (db/clear-user-permissions!)
    (db/clear-events!)
    (db/insert-permission! "basic")
    (helper/add-users)
    (db/insert-permission-for-user!
     (->
      (db/get-registered-user-details-by-username "JarrodCTaylor")
      :_id
      .toString)
     "admin")
    (is (= 2 (count (db/all-registered-users))))
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


(deftest can-successfully-create-a-new-event
  (testing "Can successfully create a new event by a user with basic permissions"
    (is (= 0 (count (db/all-events))))
    (let [userid                (.toString (:_id (db/get-registered-user-details-by-username "Everyman")))
          response              (create-event {:name "A happening"})
          body                  (parse-body (:body response))
          new-event             (db/get-event-by-name (:name body))
          created-on            (c/to-long (:created_on new-event))
          expected-time         (c/to-long (t/now))]
      (is (= 201        (:status response)))
      (is (= 1          (count (db/all-events))))
      (is (= "A happening"  (:name body)))
      (is (= "A happening"  (str (:name new-event))))
      (is (> 1000       (- expected-time created-on))))))


(deftest can-not-create-a-event-if-name-already-exists-using-the-same-case
  (testing "Can not create a event if name already exists using the same case"
    (is (= 0 (count (db/all-events))))
    (assert-no-dup {:name "My event"}
                   {:name "My event"}
                   "Event with this name already exists")
    (is (= 1 (count (db/all-events))))))

(deftest can-not-create-a-event-if-name-already-exists-using-mixed-case
  (testing "Can not create a event if name already exists using mixed case"
    (is (= 0 (count (db/all-events))))
    (assert-no-dup {:name "My Event"}
                   {:name "My event"}
                   "Event with this name already exists")
    (is (= 1 (count (db/all-events))))))
