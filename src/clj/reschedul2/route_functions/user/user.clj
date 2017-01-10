(ns reschedul2.routes.services.user
  (:require [compojure.api.sweet :refer :all]
            [schema.core :as s]))

; Non-public contact info
; + all contact info must be protected by auth/perms
; (s/defschema ContactInfo {(s/optional-key :cell-phone)               s/Str
;                           (s/optional-key :second-phone)             s/Str
;                           (s/optional-key :email)                    s/Str
;                           (s/optional-key :address)                  s/Str
;                           (s/optional-key :preferred_contact_method) (s/enum :cell :email :second-phone)})

; basic user info and embedded contact + social info
(s/defschema User {:_id                                 s/Str
                   :username                            s/Str
                   (s/optional-key :password)           (s/maybe s/Str)
                   (s/optional-key :password-confirm)   (s/maybe s/Str)
                   :first_name                          s/Str
                   :last_name                           s/Str
                   :admin                               s/Bool})


                  ;  :contact-info                        ContactInfo})

(s/defschema NewUser (dissoc User :_id))
(s/defschema UpdatedUser NewUser)
