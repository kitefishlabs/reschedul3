(ns reschedul2.route-functions.password.password-reset
  (:require [schema.core :as s]))
            ; [buddy.hashers :as hashers]
            ; [clj-time.coerce :as c]
            ; [clj-time.core :as t]
            ; [ring.util.http-response :as respond]))


(defschema PasswordResetKey
  {:_id s/Str
   :reset_key s/Str
   :already_used s/Bool
   :user_id     s/Str
   :valid_until s/Int})
