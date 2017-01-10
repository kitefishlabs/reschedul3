(ns reschedul2.db.seed
  (:require [monger.core :as mg]
            [monger.collection :as mc]
            [monger.operators :refer :all]
            [monger.result :refer [acknowledged?]]
            [mount.core :refer [defstate]]
            [reschedul2.config :refer [env]]
            [taoensso.timbre :as timbre]))
  ; (:import (org.bson.types ObjectId)))

;;;

(def seed-admin-user
  { :username "tms"
    :password "changethisworld"
    :password-confirm "changethisworld"
    :first_name "Tom"
    :last_name "Stoll"
    :admin true
    :role "owner"
    :contact-info { :cell-phone ""
                    :second-phone ""
                    :email "testing-reschedul@kitefishlabs.com"
                    :address ""
                    :preferred_contact_method "email"}})
