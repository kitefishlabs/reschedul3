(ns reschedul2.venue.venue-creation-tests
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

(defn create-venue [venue-map]
  ((app) (-> (mock/request :post "/api/v1/venue")
             (mock/content-type "application/json")
             (mock/body (ch/generate-string venue-map)))))

(defn assert-no-dup [venue-1 venue-2 expected-error-message]
  (let [_        (create-venue venue-1)
        response (create-venue venue-2)
        body     (parse-body (:body response))]
    (is (= 409                    (:status response)))
    (is (= 1                      (count (db/all-venues))))
    (is (= expected-error-message (:error body)))))

(defn setup-teardown [f]
  (try
    (db/clear-users!)
    (db/clear-permissions!)
    (db/clear-user-permissions!)
    (db/clear-venues!)
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


(deftest can-successfully-create-a-new-venue
  (testing "Can successfully create a new venue by a user with basic permissions"
    (is (= 0 (count (db/all-venues))))
    (let [userid                (.toString (:_id (db/get-registered-user-details-by-username "Everyman")))
          response              (create-venue {:name "The space"})
          body                  (parse-body (:body response))
          new-venue             (db/get-venue-by-name (:name body))
          created-on            (c/to-long (:created_on new-venue))
          expected-time         (c/to-long (t/now))]
      (is (= 201        (:status response)))
      (is (= 1          (count (db/all-venues))))
      (is (= "The space"  (:name body)))
      (is (= "The space"  (str (:name new-venue))))
      (is (> 1000       (- expected-time created-on))))))


(deftest can-not-create-a-venue-if-name-already-exists-using-the-same-case
  (testing "Can not create a venue if name already exists using the same case"
    (is (= 0 (count (db/all-venues))))
    (assert-no-dup {:name "My venue"}
                   {:name "My venue"}
                   "Venue with this name already exists")
    (is (= 1 (count (db/all-venues))))))

(deftest can-not-create-a-venue-if-name-already-exists-using-mixed-case
  (testing "Can not create a venue if name already exists using mixed case"
    (is (= 0 (count (db/all-venues))))
    (assert-no-dup {:name "My Venue"}
                   {:name "My venue"}
                   "Venue with this name already exists")
    (is (= 1 (count (db/all-venues))))))
