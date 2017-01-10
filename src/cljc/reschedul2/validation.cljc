(ns reschedul2.validation
  (:require [bouncer.core :as b]
            [bouncer.validators :as v]))

(defn format-validation-errors [errors]
  (->>  errors
        first
        (map (fn [[k [v]]] [k v]))
        (into {})
        not-empty))

(defn pass-matches? [pass-confirm pass]
  (= pass pass-confirm))

; (defn validate-create-user [user]
;   (format-validation-errors
;     (b/validate
;       (fn [{:keys [path]}]
;         ({[:username]         "Screenname is required"
;           [:password]         "Password of 8+ characters is required"
;           [:password-confirm] "Password confirmation doesn't match"
;           [:admin]            "You must specify whether the user is an admin"}
;          path))
;       user
;       :password [v/required [v/min-count 8]]
;       :password-confirm [[pass-matches? (:password user)]]
;       :username v/required
;       :admin v/required)))
;
;
; (defn validate-update-user [user]
;   (format-validation-errors
;     (b/validate
;       (fn [{:keys [path]}]
;         ({[:username]       "Screenname is required"
;           [:admin]         "You must specify whether the user is an admin"
;           [:password]         "Password doesn't match"
;           [:password-confirm] "Password doesn't match"}
;           ; [:contact-info :cell-phone] ""
;          path))
;       user
;       :screenname v/required
;       :admin v/required
;       :password-confirm [[pass-matches? (:password user)]])))
