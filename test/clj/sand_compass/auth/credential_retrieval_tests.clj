(ns sand-compass.auth.credential-retrieval-tests
  (:require [clojure.test :refer :all]
            [environ.core :refer [env]]
            [sand-compass.handler :refer :all]
            [sand-compass.test-utils :as helper]
            [sand-compass.db.core :as db]
            [buddy.sign.jwt :as jwt]
            [ring.mock.request :as mock]
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
     #'sand-compass.config/env
     #'sand-compass.db.core/db
     #'sand-compass.db.core/db*)
    (f)
    (mount/stop)))

(use-fixtures :each setup-teardown)

(deftest valid-username-and-password-return-correct-auth-credentials
  (testing "Valid username and password return correct auth credentials"
    (let [response       ((app) (-> (mock/request :get "/api/v1/auth")
                                    (helper/basic-auth-header "Everyman:pass")))
          body           (helper/parse-body (:body response))
          id             (:_id body)
          token-contents (jwt/unsign (:token body) (env :auth-key) {:alg :hs512})]
      (is (= 5           (count body)))
      (is (= 200         (:status response)))
      (is (= "Everyman"  (:username body)))
      (is (= "basic"     (:permission-level body)))
      (is (= 36          (count (:refresh_token body))))
      (is (= 5           (count        token-contents)))
      (is (= "basic"     (:permission-level token-contents)))
      (is (= id          (:_id          token-contents)))
      (is (= "e@man.com" (:email       token-contents)))
      (is (= "Everyman"  (:username    token-contents)))
      (is (number?       (:exp         token-contents))))))

(deftest valid-email-and-password-return-correct-auth-credentials
  (testing "Valid email and password return correct auth credentials"
    (let [response       ((app) (-> (mock/request :get "/api/v1/auth")
                                    (helper/basic-auth-header "e@man.com:pass")))
          body           (helper/parse-body (:body response))
          id             (:_id body)
          token-contents (jwt/unsign (:token body) (env :auth-key) {:alg :hs512})]
      (is (= 5           (count body)))
      (is (= 200         (:status response)))
      (is (= "Everyman"  (:username body)))
      (is (= "basic"     (:permission-level body)))
      (is (= 36          (count (:refresh_token body))))
      (is (= 5           (count        token-contents)))
      (is (= "basic"     (:permission-level token-contents)))
      (is (= id          (:_id          token-contents)))
      (is (= "e@man.com" (:email       token-contents)))
      (is (= "Everyman"  (:username    token-contents)))
      (is (number?       (:exp         token-contents))))))

(deftest mutiple-permissions-are-properly-formated
  (testing "Multiple permissions are properly formated: ALWAYS THE HIGHEST LEVEL USED!"
    (let [user-id-1   (:_id (db/get-registered-user-by-username "JarrodCTaylor"))
          _           (db/insert-permission-for-user! (.toString user-id-1) "admin")
          response    ((app) (-> (mock/request :get "/api/v1/auth")
                                 (helper/basic-auth-header "JarrodCTaylor:pass")))
          body        (helper/parse-body (:body response))]
      (is (= 200              (:status response)))
      (is (= "JarrodCTaylor"  (:username body)))
      (is (= 36               (count (:refresh_token body))))
      (is (= "admin"          (:permission-level (jwt/unsign (:token body) (env :auth-key) {:alg :hs512})))))))

(deftest invalid-password-does-not-return-auth-credentials
  (testing "Invalid password does not return auth credentials"
    (let [response ((app) (-> (mock/request :get "/api/v1/auth")
                              (helper/basic-auth-header "JarrodCTaylor:badpass")))
          body     (helper/parse-body (:body response))]
      (is (= 401              (:status response)))
      (is (= "Not authorized" (:error body))))))

(deftest invalid-username-does-not-return-auth-credentials
  (testing "Invalid username does not return auth credentials"
    (let [response ((app) (-> (mock/request :get "/api/v1/auth")
                              (helper/basic-auth-header "baduser:badpass")))
          body     (helper/parse-body (:body response))]
      (is (= 401              (:status response)))
      (is (= "Not authorized" (:error body))))))

(deftest no-auth-credentials-are-returned-when-no-username-and-password-provided
  (testing "No auth credentials are returned when no username and password provided"
    (let [response ((app) (mock/request :get "/api/v1/auth"))
          body     (helper/parse-body (:body response))]
      (is (= 401              (:status response)))
      (is (= "Not authorized" (:error body))))))

(deftest user-can-generate-a-new-token-with-a-valid-refresh-token
  (testing "User can generate a new tokens with a valid refresh-token"
    (let [initial-response   ((app) (-> (mock/request :get "/api/v1/auth")
                                        (helper/basic-auth-header "JarrodCTaylor:pass")))
          initial-body       (helper/parse-body (:body initial-response))
          id                 (:_id initial-body)
          refresh-token      (:refresh_token initial-body)
          refreshed-response ((app) (mock/request :get (str "/api/v1/refresh-token/" refresh-token)))
          body               (helper/parse-body (:body refreshed-response))
          token-contents     (jwt/unsign (:token body) (env :auth-key) {:alg :hs512})]
      (is (= 200              (:status refreshed-response)))
      (is (= 2                (count body)))
      (is (= true             (contains? body :token)))
      (is (= true             (contains? body :refresh_token)))
      (is (not= refresh-token (:refresh_token body)))
      (is (= 5                (count              token-contents)))
      (is (= "basic"          (:permission-level  token-contents)))
      (is (= id               (:_id               token-contents)))
      (is (= "j@man.com"      (:email             token-contents)))
      (is (= "JarrodCTaylor"  (:username          token-contents)))
      (is (number?            (:exp               token-contents))))))

(deftest invalid-refresh-token-does-not-return-a-new-token
  (testing "Invalid refresh token does not return a new token"
    (let [response       ((app) (mock/request :get "/api/v1/refresh-token/abcd1234"))
          body           (helper/parse-body (:body response))]
      (is (= 400           (:status response)))
      (is (= "Bad Request" (:error body))))))
