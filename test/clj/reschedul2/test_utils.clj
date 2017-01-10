(ns reschedul2.test-utils
  (:require [cheshire.core :as ch]
            [ring.mock.request :as mock]
            [reschedul2.handler :refer [app]]
            [reschedul2.db.core :as db]
            [buddy.core.codecs :as codecs]
            [buddy.core.codecs.base64 :as b64]))

(def str->base64 (comp codecs/bytes->str b64/encode))

(defn parse-body [body]
  (ch/parse-string (slurp body) true))

(defn basic-auth-header
  [request original]
  (mock/header request "Authorization" (str "Basic " (str->base64 original))))

(defn token-auth-header
  [request token]
  (mock/header request "Authorization" (str "Token " token)))

(defn get-user-token [username-and-password]
  (let [initial-response (app (-> (mock/request :get "/api/v1/auth")
                                  (basic-auth-header username-and-password)))
        initial-body     (parse-body (:body initial-response))]
    (:token initial-body)))

(defn get-token-auth-header-for-user [request username-and-password]
  (token-auth-header request (get-user-token username-and-password)))

(defn get-permissions-for-user [uid]
  (:permissions (db/get-permissions-for-userid uid)))

(defn add-users []
  (let [user-1 {:email "j@man.com" :username "JarrodCTaylor" :password "pass"}
        user-2 {:email "e@man.com" :username "Everyman"      :password "pass"}]
    (app (-> (mock/request :post "/api/v1/user")
             (mock/content-type "application/json")
             (mock/body (ch/generate-string user-1))))
    (app (-> (mock/request :post "/api/v1/user")
             (mock/content-type "application/json")
             (mock/body (ch/generate-string user-2))))))

(defn create-tables [f]
  ; (query/create-registered-user-table-if-not-exists! query/db)
  ; (query/create-permission-table-if-not-exists! query/db)
  ; (query/create-user-permission-table-if-not-exists! query/db)
  ; (query/create-password-reset-key-table-if-not-exists! query/db)
  (db/create-basic-permission-if-not-exists!)
  (f))
