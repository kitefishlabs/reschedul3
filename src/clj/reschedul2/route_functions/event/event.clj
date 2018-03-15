(ns reschedul2.routes.services.event
  (:require [compojure.api.sweet :refer :all]
            [schema.core :as s]))

; basic event info and embedded contact + social info
(s/defschema Event {:_id                                 s/Str}
                   :name                            s/Str
                   :owner-userid                         s/Str)


(s/defschema NewEvent (dissoc Event :_id))
(s/defschema UpdatedEvent NewEvent)
