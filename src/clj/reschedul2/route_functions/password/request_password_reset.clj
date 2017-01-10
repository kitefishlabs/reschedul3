(ns reschedul2.route-functions.password.request-password-reset
  (:require [reschedul2.db.core :as db]
            [environ.core :refer [env]]
            [postal.core :refer [send-message]]
            [ring.util.http-response :as respond]))

(defn add-response-link-to-plain-body
  "Insert link into plaintext email body"
  [body response-link]
  (str body "\n\n" response-link))

(defn add-response-link-to-html-body
  "Insert link into HTML email body"
  [body response-link]
  (let [body-less-closing-tags (clojure.string/replace body #"</body></html>" "")]
    (str body-less-closing-tags "<br><p>" response-link "</p></body></html>")))

(defn send-reset-email
  "Send password reset email (uses Postal + https://account.sendinblue.com/)"
  [to-email from-email subject html-body plain-body]
  (send-message {:host "smtp-relay.sendinblue.com"
                 :user (env :sendinblue-user-login)
                 :port 587
                 :pass (env :sendinblue-user-password)}
                {:from from-email
                 :to to-email
                 :subject subject
                 :body [:alternative
                        {:type "text/plain" :content plain-body}
                        {:type "text/html"  :content html-body}]}))

(defn process-password-reset-request
  "Safely process password reset request"
  [user from-email subject email-body-plain email-body-html response-base-link]
  (let [reset-key     (str (java.util.UUID/randomUUID))
        the-insert    (db/stringify-id (db/insert-password-reset-key-with-default-valid-until! reset-key (:_id user)))
        response-link (str response-base-link "/" (:reset_key the-insert))
        body-plain    (add-response-link-to-plain-body email-body-plain response-link)
        body-html     (add-response-link-to-html-body email-body-html response-link)]
    (send-reset-email (str (:email user)) from-email subject body-html body-plain)
    (respond/ok {:message (str "Reset email successfully sent to " (str (:email user)))})))

(defn request-password-reset-response
  "Generate response for password reset request"
  [user-email from-email subject email-body-plain email-body-html response-base-link]
  (let [user (db/stringify-id (db/get-registered-user-by-email user-email))]
    (if (empty? user)
      (respond/not-found {:error (str "No user exists with the email " user-email)})
      (process-password-reset-request user from-email subject email-body-plain email-body-html response-base-link))))
