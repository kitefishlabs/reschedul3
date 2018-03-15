(ns reschedul2.route-functions.refresh-token.delete-refresh-token
  (:require [reschedul2.db.core :as db]
            [ring.util.http-response :as respond]
            [taoensso.timbre :as timbre]))

; TODO: fix this to not blindly accept the nullify call
(defn remove-refresh-token-response
  "Remove refresh token (error if doesn't exist)"
  [refresh-token]
  (if-let [should-be-zero (db/nullify-refresh-token! refresh-token)]
    (if (zero? (:refresh_token should-be-zero))
     (respond/ok         {:message "Refresh token successfully deleted"})
     (respond/not-found  {:error "The refresh token does not exist"}))
    (respond/not-found  {:error "The refresh token does not exist"})))
