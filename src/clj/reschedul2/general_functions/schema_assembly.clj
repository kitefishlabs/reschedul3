(ns reschedul2.general-functions.schema-assembly
  (:require [clojure.data.json :as json]
            [schema.core :as s]
            [reschedul2.db.core :as db]))

; (declare convert-simple-schema-type)
;
; ; (defn clear-and-read-base-settings []
; ;  (let [schema (json/read-str raw-schema)]
; ;   (clear-globals!)
; ;   (mc/insert db "globals" schema)))
;
; (defn read-base-settings []
;  (let [raw-schema (slurp "/Users/kfl/dev/git/reschedul2a/infringement-schema.json")]
;   (keywordize-keys (json/read-str raw-schema))))
;
; (defn get-global-db-subtree
;  [keys]
;  (let [globals (read-base-settings)]
;   (get-in globals keys)))
;
;
; (defn convert-nested-types
;  [group-label collection-type-kw]
;  {group-label
;   (apply
;    merge
;    (map
;     (fn [[k v]] (convert-simple-schema-type (keyword (clojure.string/join [group-label "_" (clojure.string/replace (str k) #":" "")])) (second v)))
;     (db/get-global-db-subtree [:custom-types (keyword collection-type-kw)])))})
;
; ; (defn convert-keyword-types
; ;  [label collection-type-kw]
; ;  {label (s/enum (db/get-global-db-subtree [:keywords :user-state]))})
;
; (defn convert-simple-schema-type
;   [label typ]
;   ; (if (> (count (re-find #"kw_" typ)) 0)
;   ;   ; it's a keyword type
;   ;   (convert-keyword-types
;   ;    label
;   ;    (clojure.string/replace typ #"kw_" "")))
;   (case typ
;        "string" {label (s/maybe s/Str)}
;        "bool" { label (s/maybe s/Bool)}
;        "email" { label (s/maybe s/Str)}
;        "phone" { label (s/maybe s/Str)}
;        "zip" { label (s/maybe s/Str)}
;        "url" { label (s/maybe s/Str)}
;        (convert-nested-types
;         label
;         (keyword typ))))
;
; ; (convert-simple-schema-type "username" "string")
; ; (convert-simple-schema-type "zip-code" "zip")
; ; (convert-simple-schema-type "contact" "contact")
;
; (defn convert-entity-to-schema
;  [top-level-subtree]
;  (map
;   (fn
;    [[k v]]
;    (convert-simple-schema-type k (second v))))
;  top-level-subtree)
