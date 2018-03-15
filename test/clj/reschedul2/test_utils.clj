(ns reschedul2.test-utils
  (:require [cheshire.core :as ch]
            [ring.mock.request :as mock]
            [reschedul2.handler :refer [app]]
            [reschedul2.db.core :as db]
            [buddy.core.codecs :as codecs]
            [buddy.core.codecs.base64 :as b64]
            [mount.core :as mount]
            [taoensso.timbre :as timbre]))

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
  (let [initial-response ((app) (-> (mock/request :get "/api/v1/auth")
                                  (basic-auth-header username-and-password)))
        initial-body     (parse-body (:body initial-response))]
    (:token initial-body)))

(defn get-token-auth-header-for-user [request username-and-password]
  (token-auth-header request (get-user-token username-and-password)))

(defn get-permission-for-user [uid]
  (db/get-permission-for-user uid))


(defn add-users []
  (let [user-1 {:email "j@man.com" :username "JarrodCTaylor" :password "pass" :state "verified"}
        user-2 {:email "e@man.com" :username "Everyman"      :password "pass" :state "created"}]
    ((app) (-> (mock/request :post "/api/v1/user")
               (mock/content-type "application/json")
               (mock/body (ch/generate-string user-1))))
    ((app) (-> (mock/request :post "/api/v1/user")
               (mock/content-type "application/json")
               (mock/body (ch/generate-string user-2))))))
