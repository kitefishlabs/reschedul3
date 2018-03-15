(ns reschedul2.routes.services.venue
  (:require [compojure.api.sweet :refer :all]
            [schema.core :as s]))

; basic venue info and embedded contact + social info
(s/defschema Venue {:_id                                 s/Str}
                   :name                            s/Str
                   :owner-userid                         s/Str)


(s/defschema NewVenue (dissoc Venue :_id))
(s/defschema UpdatedVenue NewVenue)
