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

(defn validate-create-user [user]
  (format-validation-errors
    (b/validate
      (fn [{:keys [path]}]
        ({[:username]         "Screenname is required"
          [:password]         "Password of 8+ characters is required"
          [:password-confirm] "Password confirmation doesn't match"
          [:admin]            "You must specify whether the user is an admin"}
         path))
      user
      :password [v/required [v/min-count 8]]
      :password-confirm [[pass-matches? (:pass user)]]
      :username v/required
      :admin v/required)))


(defn validate-update-user [user]
  (format-validation-errors
    (b/validate
      (fn [{:keys [path]}]
        ({[:screenname]   "Screenname is required"
          [:is-admin]     "You must specify whether the user is an admin"
          [:pass-confirm] "Password confirmation doesn't match"
          [:active]       "You must pecify whether the user is active"}
          ; [:contact-info :cell-phone] ""
         path))
      user
      :screenname v/required
      :admin v/required
      :pass-confirm [[pass-matches? (:pass user)]]
      :is-active v/required)))
